
import com.looker.sql_query_parser.parser.MySQLParser
import com.looker.sql_query_parser.parser.ViewSchema
import com.looker.sql_query_parser.parser.getChildren
import org.antlr.v4.runtime.tree.ParseTree
import org.junit.Assert
import org.junit.Test

class SimpleTest {

    val sql = "SELECT birthdate, u.id userid, u.username, o.id orderid, o.orderdate, p.id partid, p.partname FROM users u JOIN orders o ON u.id = o.user_id RIGHT OUTER JOIN parts p ON p.id = o.part_id"

    fun preserveCase(sql: String) : MySQLParser.RootContext {
        val root = ViewSchema.ParseSql(sql)
        val source = root.getStart().tokenSource.inputStream.toString()
        Assert.assertEquals("Statement casing should be preserved in inputStream", sql, source)
        return root
    }

    @Test fun `tests simple select`() {
        val root = preserveCase("SELECT * FROM USERS")
        Assert.assertNotNull(root)
    }

    @Test fun `tests single join`() {
        val root = preserveCase("SELECT * FROM USERS U JOIN ORDERS O ON U.ID = O.USER_ID")
        Assert.assertNotNull(root)
    }

    @Test fun `tests and displays hierarchy for multiple joins`() {
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
        val schema = ViewSchema(sql)
        Assert.assertEquals("Column count",9, schema.columns.size)
        Assert.assertEquals("Table count", 3, schema.tables.size)
        Assert.assertEquals("Join count", 2, schema.joins.size)
        
        val tableUsers = schema.tables.first()
        Assert.assertNotNull("Users table exists", tableUsers)
        Assert.assertEquals("Expected USERS table", "USERS", tableUsers.name)
        Assert.assertTrue("User alias 'U' exists", tableUsers.aliases.any { alias -> alias == "U"})
        Assert.assertEquals( "User table column count", 3, tableUsers.columns.size)
        Assert.assertTrue( "All columns have full table name", tableUsers.columns.all { col -> col.tableName.equals(tableUsers.name)})

        val userid = schema.columns.find("userid")
        Assert.assertNotNull("UserID alias column found", userid)
        Assert.assertEquals("UserID Table is USERS", tableUsers, userid!!.table)
        Assert.assertEquals("USERS.ID", userid.fullName())

        val tableOrders = schema.tables.find("orders")
        Assert.assertNotNull("Orders table exists", tableOrders)
        Assert.assertEquals("Expected Orders table", "ORDERS", tableOrders!!.name)
        Assert.assertTrue("Orders alias 'O' exists", tableOrders.aliases.any { alias -> alias.equals("O", true)})
        Assert.assertEquals( "Orders table column count", 4, tableOrders.columns.size)
        Assert.assertTrue( "All columns have full table name", tableOrders.columns.all { col -> col.tableName == tableOrders.name})

        val tableParts = schema.tables.find("parts")
        Assert.assertNotNull("Parts table exists", tableParts)
        Assert.assertEquals("Expected Parts table", "PARTS", tableParts!!.name)
        Assert.assertTrue("Parts alias 'P' exists", tableParts.aliases.any { alias -> alias.equals("P", true)})
        Assert.assertEquals( "Parts table column count", 2, tableParts.columns.size)
        Assert.assertTrue( "All columns have full table name", tableParts.columns.all { col -> col.tableName == tableParts.name})

        val joinOrders = schema.joins.find("orders")
        Assert.assertNotNull("Orders join exists", joinOrders)
        Assert.assertEquals("Orders table name", "ORDERS", joinOrders!!.table.name)
        Assert.assertTrue("Orders is LEFT join", joinOrders.left)
        Assert.assertTrue("Orders is INNER join", joinOrders.inner)
        Assert.assertEquals("USERS.ID", joinOrders.leftColumn.fullName())
        Assert.assertEquals("ORDERS.USER_ID", joinOrders.rightColumn.fullName())

        val joinParts = schema.joins.find("parts")
        Assert.assertNotNull("Parts join exists", joinParts)
        Assert.assertEquals("Parts table name", "PARTS", joinParts!!.table.name)
        Assert.assertTrue("Parts is RIGHT join", joinParts.right)
        Assert.assertTrue("Parts is OUTER join", joinParts.outer)
        Assert.assertEquals("PARTS.ID", joinParts.leftColumn.fullName())
        Assert.assertEquals("ORDERS.PART_ID", joinParts.rightColumn.fullName())
    }
}
