package com.looker.sql_query_parser.parser

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNodeImpl
import java.io.ByteArrayInputStream

fun ParseTree.getChildren() : List<ParseTree> {
    val list = MutableList(this.childCount, {index : Int -> this.getChild(index)})
    return list.filter({tree -> true})
}

class Aliases(aList: MutableSet<String> = mutableSetOf()) : MutableSet<String> by aList

data class Column(var originalName: String, var aliases: Aliases = Aliases()) {
    var database : String = ""
    var tableName : String = ""
    var name : String = ""
    init {
        val parts = originalName.split('.')
        when (parts.size) {
            1 -> name = originalName
            2 -> {
                tableName = parts[0]
                name = parts[1]
            }
            3 -> {
                database = parts[0]
                tableName = parts[1]
                name = parts[2]
            }
        }
    }
    var table: Table? = null
    fun addAlias(alias : String) : Boolean = aliases.add(alias)
    fun fullName() : String {
        if (database.isBlank()) {
            if (tableName.isBlank()) return name
            return "${tableName}.${name}"
        }
        return "${database}.${tableName}.${name}"
    }
}

class Columns(aList: MutableSet<Column> = mutableSetOf()): MutableSet<Column> by aList {
    fun find(nameOrAlias: String) : Column? {
        return this.firstOrNull { col -> col.fullName().equals(nameOrAlias, true)
                || col.originalName.equals(nameOrAlias,true)
                || col.aliases.any { alias -> alias.equals(nameOrAlias, true) }
        }
    }

    fun find(column: Column) : Column? {
        var found = find(column.originalName)
        if (found == null) found = find(column.fullName())
        if (found != null) column.aliases.forEach{ alias -> found.aliases.add(alias) } // add any missing aliases
        return found
    }
}

data class Table(var originalName: String, var columns: Columns = Columns(),
                 var aliases: Aliases = Aliases()) {
    var database : String = ""
    var name : String = ""
    init {
        val parts = originalName.split('.')
        when (parts.size) {
            1 -> name = originalName
            2 -> {
                database = parts[0]
                name = parts[1]
            }
        }
    }

    fun fullName() : String {
        if (database.isBlank()) return name
        return database + "." + name
    }

    fun addColumn(column: Column) : Column {
        var found = columns.find(column)
        if (found == null) {
            found = column
            columns.add(found)
        }
        return found
    }
    fun addAlias(alias: String) : Boolean = aliases.add(alias)
}

class Tables(aList: LinkedHashSet<Table> = linkedSetOf()): MutableSet<Table> by aList {
    fun find(nameOrAlias: String) : Table? {
        return this.firstOrNull { table -> table.name.equals(nameOrAlias,true) ||
                table.aliases.any { alias -> alias.equals(nameOrAlias, true) }
        }
    }
}

// Enum based on this great post https://stackoverflow.com/a/17946222/74137
enum class JoinType {
    INNER,
    OUTER,
    FULL_OUTER,
    SELF,
    CROSS,
    NATURAL
}

data class Join(val table: Table, val leftColumn: Column, val rightColumn: Column,
                val inner: Boolean = true, val left: Boolean = true, var type : JoinType = JoinType.INNER) {
    val outer = !inner
    val right = !left
}

class Joins(aList: MutableSet<Join> = mutableSetOf()): MutableSet<Join> by aList {
    fun find(nameOrAlias: String) : Join? {
        return this.firstOrNull { col -> col.table.name.equals(nameOrAlias,true) ||
                col.table.aliases.any { alias -> alias.equals(nameOrAlias, true) }
        }
    }

}

data class ViewSchema(val sql: String, var tables: Tables = Tables(), var columns : Columns = Columns(),
                      var joins: Joins = Joins()) {
    var ast : MySQLParser.RootContext

    private var leftJoin = true
    private var innerJoin = true
    private var joinTable : Table? = null
    private var leftColumn : Column? = null
    private var rightColumn : Column? = null

    init {
        ast = ParseSql(sql)
        processNodes(ast.children)
        resolve()
    }

    companion object {
        fun ParseSql(sql: String) : MySQLParser.RootContext {
            val upCommand = sql.toUpperCase()
            val caseStream = CharStreams.fromStream(ByteArrayInputStream(sql.toByteArray()))
            val inputStream = ByteArrayInputStream(upCommand.toByteArray())
            val lexer = MySQLLexer(CharStreams.fromStream(inputStream))
            val tokenStream = CommonTokenStream(lexer)

            val parser = MySQLParser(tokenStream)
            // use error listener to throw exception when parser fails (as opposed to just printing to console)
            parser.removeErrorListeners()
            parser.addErrorListener(ThrowingErrorListener.INSTANCE)
            lexer._input = caseStream // set back to original case

            return parser.root()
        }

        fun originalText(node: ParseTree) : String {
            // TODO: figure out if we need this, and if so, implement it
            // use the source.a._input property with the node's start and stop position to return the original
            // version of the text that was parsed
            return node.text
        }
    }

    fun addTable(table: Table) : Table {
        // TODO handle addition error
        tables.add(table)
        return table
    }

    fun addJoin(join: Join) : Join {
        // TODO handle addition error
        joins.add(join)
        return join
    }

    private fun findTableForColumn(column: Column) {
        val defaultTable = tables.firstOrNull()
        if (defaultTable == null) return
        if (column.table == null) {
            val table = if (!column.tableName.isBlank()) tables.find(column.tableName)
            else {
                // TODO 1. look for a column name in one of the SELECT tables that matches the column name
                // if that's not found, assign the column's table to the default table
                defaultTable
            }
            if (table != null) {
                table.addColumn(column)
                column.table = table
                column.tableName = table.name
            }
        }

    }

    fun addColumn(column: Column) : Column {
        // TODO handle addition error
        findTableForColumn(column)
        var found = columns.find(column)
        if (found == null) {
            columns.add(column)
            found = column
        }
        return found
    }

    private fun resolve() {
        columns.forEach { column ->
            findTableForColumn(column)
        }
    }

    private fun select(node: ParseTree) {
        node.getChildren().forEach { item ->
            when (item) {
                is MySQLParser.SelectElementsContext -> selectColumns(item)
                is MySQLParser.FromClauseContext -> selectFrom(item)
                else -> select(item)
            }
        }
    }

    private fun selectTable(node: ParseTree) : Table {
        var name = ""
        val aliases = Aliases()
        node.getChildren().forEach { item ->
            when (item) {
                is MySQLParser.TableNameContext -> name = item.text
                is MySQLParser.UidContext -> aliases.add(item.text)
            }
        }
        return addTable(Table(name, aliases = aliases))
    }

    private fun selectFrom(node: ParseTree) {
        node.getChildren().forEach { item ->
            when (item) {
                is MySQLParser.AtomTableItemContext -> selectTable(item)
                is MySQLParser.JoinPartContext -> join(item)
                else -> selectFrom(item)
            }
        }
    }

    private fun selectColumn(node: ParseTree) : Column {
        var name = ""
        val aliases = Aliases()
        node.getChildren().forEach { item ->
            when (item) {
                is MySQLParser.FullColumnNameContext -> name = item.text
                is MySQLParser.UidContext -> aliases.add(item.text)
            }
        }
        return addColumn(Column(name, aliases))
    }

    private fun selectColumns(node: ParseTree) {
        node.getChildren().forEach { item ->
            when (item) {
                is MySQLParser.SelectColumnElementContext -> selectColumn(item)
                else -> selectColumns(item)
            }
        }
    }

    private fun joinCompare(node: ParseTree) {
        var column : Column
        node.getChildren().forEach { item ->
            when (item) {
                is MySQLParser.FullColumnNameExpressionAtomContext -> {
                    column = selectColumn(item)
                    if (leftColumn == null) leftColumn = column
                    else rightColumn = column
                }
                else -> joinCompare(item)
            }
        }
    }

    private fun joinNode(node: MySQLParser.JoinPartContext) : Join {
        node.getChildren().forEach { item ->
            when (item) {
                is TerminalNodeImpl -> when (item.text) {
                    "RIGHT" -> leftJoin = false
                    "OUTER" -> innerJoin = false
                }
                is MySQLParser.AtomTableItemContext -> joinTable = selectTable(item)
                is MySQLParser.PredicateExpressionContext -> joinCompare(item)
            }
        }
        return Join(joinTable!!, leftColumn!!, rightColumn!!, innerJoin, leftJoin)
    }

    private fun initJoin() {
        joinTable = null
        leftColumn = null
        rightColumn = null
        innerJoin = true
        leftJoin = true
    }

    private fun naturalJoin(node: MySQLParser.NaturalJoinContext) : Join {
        val join = joinNode(node)
        join.type = JoinType.NATURAL
        return join
    }

    private fun outerJoin(node: MySQLParser.OuterJoinContext) : Join {
        val join = joinNode(node)
        join.type = JoinType.OUTER
        return join
    }

    private fun straightJoin(node: MySQLParser.StraightJoinContext) : Join {
        val join = joinNode(node)
        join.type = JoinType.INNER
        return join
    }

    private fun innerJoin(node: MySQLParser.InnerJoinContext) : Join {
        val join = joinNode(node)
        join.type = JoinType.INNER
        return join
    }

    private fun join(node: ParseTree) {
        initJoin()
        when (node) {
            is MySQLParser.NaturalJoinContext -> addJoin(naturalJoin(node))
            is MySQLParser.OuterJoinContext -> addJoin(outerJoin(node))
            is MySQLParser.StraightJoinContext -> addJoin(straightJoin(node))
            is MySQLParser.InnerJoinContext -> addJoin(innerJoin(node))
            else -> return // Throw error here
        }
    }

    fun processNodes(nodes: List<ParseTree>) {
        nodes.forEach{ node ->
            if (node is MySQLParser.QuerySpecificationContext) select(node)
            else processNodes(node.getChildren())
        }
    }
}
