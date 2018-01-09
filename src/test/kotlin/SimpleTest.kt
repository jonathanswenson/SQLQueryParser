
import com.looker.sql_query_parser.parser.MySQLParser
import com.looker.sql_query_parser.parser.ExploreSchema
import com.looker.sql_query_parser.parser.getChildren
import org.antlr.v4.runtime.tree.ParseTree
import org.junit.Assert
import org.junit.Test

class SimpleTest {

    val sql = "SELECT birthdate, u.id userid, u.username, o.id orderid, o.orderdate, c.id contactid, c.email FROM users u JOIN orders o ON u.id = o.user_id RIGHT OUTER JOIN contacts c ON c.user_id = u.id"

    fun preserveCase(sql: String) : MySQLParser.RootContext {
        val root = ExploreSchema.ParseSql(sql)
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

    @Test fun `extracts ExploreSchema`() {
        val schema = ExploreSchema(sql)
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
        Assert.assertTrue(userid.is_id)

        val tableOrders = schema.tables.find("orders")
        Assert.assertNotNull("Orders table exists", tableOrders)
        Assert.assertEquals("Expected Orders table", "ORDERS", tableOrders!!.name)
        Assert.assertTrue("Orders alias 'O' exists", tableOrders.aliases.any { alias -> alias.equals("O", true)})
        Assert.assertEquals( "Orders table column count", 3, tableOrders.columns.size)
        Assert.assertTrue( "All columns have full table name", tableOrders.columns.all { col -> col.tableName == tableOrders.name})

        val tableContacts = schema.tables.find("contacts")
        Assert.assertNotNull("Contacts table exists", tableContacts)
        Assert.assertEquals("Expected Contacts table", "CONTACTS", tableContacts!!.name)
        Assert.assertTrue("Contacts alias 'C' exists", tableContacts.aliases.any { alias -> alias.equals("C", true)})
        Assert.assertEquals( "Contacts table column count", 3, tableContacts.columns.size)
        Assert.assertTrue( "All columns have full table name", tableContacts.columns.all { col -> col.tableName == tableContacts.name})

        val joinOrders = schema.joins.find("orders")
        Assert.assertNotNull("Orders join exists", joinOrders)
        Assert.assertEquals("Orders table name", "ORDERS", joinOrders!!.table.name)
        Assert.assertTrue("Orders is LEFT join", joinOrders.left)
        Assert.assertTrue("Orders is INNER join", joinOrders.inner)
        Assert.assertEquals("USERS.ID", joinOrders.leftColumn.fullName())
        Assert.assertEquals("ORDERS.USER_ID", joinOrders.rightColumn.fullName())

        val joinContacts = schema.joins.find("contacts")
        Assert.assertNotNull("Contacts join exists", joinContacts)
        Assert.assertEquals("Contacts table name", "CONTACTS", joinContacts!!.table.name)
        Assert.assertTrue("Contacts is RIGHT join", joinContacts.right)
        Assert.assertTrue("Contacts is OUTER join", joinContacts.outer)
        Assert.assertEquals("USERS.ID", joinContacts.leftColumn.fullName())
        Assert.assertEquals("CONTACTS.USER_ID", joinContacts.rightColumn.fullName())
    }
}
