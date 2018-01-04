
import com.looker.sql_query_parser.parser.MySQLParser
import com.looker.sql_query_parser.parser.ViewExtractor
import com.looker.sql_query_parser.parser.getChildren
import org.antlr.v4.runtime.tree.ParseTree
import org.junit.Assert
import org.junit.Test

class SimpleTest {

    fun preserveCase(sql: String) : MySQLParser.RootContext {
        val root = ViewExtractor.ParseSql(sql)
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

    @Test fun `tests and displays hierarchy for multiple joins`() {
        val sql = "SELECT birthdate, u.id userid, u.username, o.id orderid, o.orderdate, p.id partid, p.partname FROM users u LEFT OUTER JOIN orders o ON u.id = o.user_id RIGHT OUTER JOIN part p ON p.id = p.part_id"
//        val sql = "SELECT * FROM USERS U JOIN ORDERS O ON U.ID = O.USER_ID JOIN PART P ON P.ID = P.PART_ID"
        val root = preserveCase(sql)
        Assert.assertEquals("Should be 2 children", 2, root.childCount)
        family(root.children)
    }

    private fun family(children: List<ParseTree>, indent: String = "") {
        children.forEach { child ->
            println("$indent ${child.text} (${child.javaClass.simpleName})")
            family(child.getChildren(), indent + "\t")
        }
    }

    @Test fun `extracts viewSchema`() {
        val sql = "SELECT birthdate, u.id userid, u.username, o.id orderid, o.orderdate, p.id partid, p.partname FROM users u LEFT OUTER JOIN orders o ON u.id = o.user_id RIGHT OUTER JOIN part p ON p.id = p.part_id"
        val extractor = ViewExtractor(sql)
        val view = extractor.analyze()
        Assert.assertEquals("Column count",7, view.columns.size)
        Assert.assertEquals("Table count", 1, view.tables.size)
    }
}
