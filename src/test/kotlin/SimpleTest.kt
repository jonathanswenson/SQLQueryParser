
import org.junit.Test
import com.looker.sql_query_parser.parser.MySQLLexer
import java.io.ByteArrayInputStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import com.looker.sql_query_parser.parser.MySQLParser
import com.looker.sql_query_parser.parser.ThrowingErrorListener
import org.antlr.runtime.tree.ParseTree
import org.junit.Assert

class SimpleTest {

    private fun parseSql(sql: String) : MySQLParser.RootContext {
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

    private fun preserveCase(sql: String) : MySQLParser.RootContext {
        val root = parseSql(sql)
        val source = root.getStart().tokenSource.inputStream.toString()
        Assert.assertEquals("Statement casing should be preserved in inputStream", sql, source)
        return root
    }

    @Test fun `tests simple select`() {
        val root = preserveCase("SELECT * FROM USERS")
    }

    @Test fun `tests single join`() {
        val root = preserveCase("SELECT * FROM USERS U JOIN ORDERS O ON U.ID = O.USER_ID")
    }

    @Test fun `tests multiple joins`() {
        val sql = "SELECT * FROM users u JOIN orders o ON u.id = o.user_id JOIN part p ON p.id = p.part_id"
//        val sql = "SELECT * FROM USERS U JOIN ORDERS O ON U.ID = O.USER_ID JOIN PART P ON P.ID = P.PART_ID"
        val root = preserveCase(sql)
        Assert.assertEquals("Should be 2 children", 2, root.childCount)
        val children = root.children.filter({ parseTree -> true })
        family(children)
    }

    private fun getChildren(node: ParseTree) : List<ParseTree> {
        val list = MutableList(node.childCount, {index : Int -> node.getChild(index)})
        return list.filter({tree -> true})
    }

    private fun family(children: List<ParseTree>, indent: String = "") {
        for (child in children) {
            println(indent + child.text)
            family(getChildren(child), indent + "\t")
        }
    }
}
