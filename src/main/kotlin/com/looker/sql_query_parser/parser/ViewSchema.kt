package com.looker.sql_query_parser.parser

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import java.io.ByteArrayInputStream

fun ParseTree.getChildren() : List<ParseTree> {
    val list = MutableList(this.childCount, {index : Int -> this.getChild(index)})
    return list.filter({tree -> true})
}

class Aliases(aList: MutableList<String> = mutableListOf()) : MutableList<String> by aList

data class Column(val name: String, var aliases: Aliases = Aliases()) {
    fun addAlias(alias : String) : Boolean = aliases.add(alias)
}

class Columns(aList: MutableList<Column> = mutableListOf()): MutableList<Column> by aList

data class Table(val name: String, var columns: MutableList<Column> = mutableListOf(), var aliases: MutableList<String> = mutableListOf()) {
    fun addColumn(column: Column) : Boolean = columns.add(column)
    fun addAlias(alias: String) : Boolean = aliases.add(alias)
}

class Tables(aList: MutableList<Table> = mutableListOf()): MutableList<Table> by aList

// Enum based on this great post https://stackoverflow.com/a/17946222/74137
enum class JoinType {
    INNER,
    LEFT_OUTER,
    RIGHT_OUTER,
    FULL_OUTER,
    SELF,
    CROSS,
    NATURAL
}

data class Join(val fromTable: Table, val toTable: Table, val fromKey: Column, val toKey: Column, val joinType: JoinType)

class Joins(aList: MutableList<Join> = mutableListOf()): MutableList<Join> by aList

data class ViewSchema(val name: String, var tables: Tables = Tables(), var columns : Columns = Columns(), var joins: Joins = Joins()) {
    fun addTable(table: Table) : Boolean = tables.add(table)
    fun addJoin(join: Join) : Boolean = joins.add(join)
    fun addColumn(column: Column) : Boolean = columns.add(column)
}

class ViewExtractor(val sql: String) {
    var viewSchema = ViewSchema("top")

    companion object {
        fun ParseSql(sql: String) : MySQLParser.RootContext {
            val upCommand = sql.toUpperCase()
            val caseStream = CharStreams.fromStream(ByteArrayInputStream(sql.toByteArray()))
            val inputStream = ByteArrayInputStream(upCommand.toByteArray())
            val lexer = MySQLLexer(CharStreams.fromStream(inputStream))
            val tokenStream = CommonTokenStream(lexer)

            val parser = MySQLParser(tokenStream)
            // use error listener to throw exception when parser fails (as opposed to just printing to console)
            parser.removeErrorListeners();
            parser.addErrorListener(ThrowingErrorListener.INSTANCE)
            lexer._input = caseStream // set back to original case?

            return parser.root()
        }

        fun originalText(node: ParseTree) : String {
            // TODO: figure out if we need this, and if so, implement it
            // use the source.a._input property with the node's start and stop position to return the original
            // version of the text that was parsed
            return node.text
        }
    }

    fun select(node: ParseTree) {
        node.getChildren().forEach { item ->
            when (item) {
                is MySQLParser.SelectElementsContext -> selectColumns(item)
                is MySQLParser.FromClauseContext -> selectFrom(item)
                is MySQLParser.JoinPartContext -> join(item)
                else -> select(item)
            }
        }

    }

    private fun selectTable(node: ParseTree) {
        var name = ""
        var aliases = Aliases()
        node.getChildren().forEach { item ->
            when (item) {
                is MySQLParser.TableNameContext -> name = item.text
                is MySQLParser.UidContext -> aliases.add(item.text)
            }
        }
        viewSchema.addTable(Table(name, aliases = aliases))
    }

    private fun selectFrom(node: ParseTree) {
        node.getChildren().forEach { item ->
            when (item) {
                is MySQLParser.AtomTableItemContext -> selectTable(item)
                else -> selectFrom(item)
            }
        }
    }

    private fun selectColumn(node: ParseTree) : Column {
        var name = ""
        var aliases = Aliases()
        node.getChildren().forEach { item ->
            when (item) {
                is MySQLParser.FullColumnNameContext -> name = item.text
                is MySQLParser.UidContext -> aliases.add(item.text)
            }
        }
        return Column(name, aliases)
    }

    private fun selectColumns(node: ParseTree) {
        node.getChildren().forEach { item ->
            when (item) {
                is MySQLParser.SelectColumnElementContext -> viewSchema.addColumn(selectColumn(item))
                else -> selectColumns(item)
            }
        }
    }

    private fun naturalJoin(join: MySQLParser.NaturalJoinContext) {

    }

    private fun outerJoin(join: MySQLParser.OuterJoinContext) {

    }

    private fun straightJoin(join: MySQLParser.StraightJoinContext) {

    }

    private fun innerJoin(join: MySQLParser.InnerJoinContext) {

    }

    private fun join(node: ParseTree) {
        when (node) {
            is MySQLParser.NaturalJoinContext -> naturalJoin(node)
            is MySQLParser.OuterJoinContext -> outerJoin(node)
            is MySQLParser.StraightJoinContext -> straightJoin(node)
            is MySQLParser.InnerJoinContext -> innerJoin(node)
            else -> return // Throw error here
        }
    }

    fun processNodes(nodes: List<ParseTree>) {
        nodes.forEach{ node ->
            if (node is MySQLParser.QuerySpecificationContext) select(node)
            else processNodes(node.getChildren())
        }
    }

    fun analyze() : ViewSchema {
        viewSchema = ViewSchema("top")
        var root = ParseSql(sql)
        processNodes(root.children)
        return viewSchema
    }

}