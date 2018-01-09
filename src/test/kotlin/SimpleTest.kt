
import com.looker.sql_query_parser.parser.MySQLParser
import com.looker.sql_query_parser.parser.ExploreSchema
import com.looker.sql_query_parser.parser.getChildren
import org.antlr.v4.runtime.tree.ParseTree
import org.junit.Assert
import org.junit.Test

class SimpleTest {

    val sql = "select birthdate, u.id userid, u.username, o.id orderid, o.orderdate, c.id contactid, c.email FROM users u JOIN orders o ON u.id = o.user_id RIGHT OUTER JOIN contacts c ON c.user_id = u.id"

    fun preserveCase(sql: String) : MySQLParser.RootContext {
        val root = ExploreSchema.parseSql(sql)
        val source = root.getStart().tokenSource.inputStream.toString()
        Assert.assertEquals("Statement casing should be preserved in inputStream", sql, source)
        return root
    }

    @Test fun `tests simple select`() {
        val root = preserveCase("select * FROM USERS")
        Assert.assertNotNull(root)
    }

    @Test fun `tests single join`() {
        val root = preserveCase("select * FROM USERS U JOIN ORDERS O ON U.ID = O.USER_ID")
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
        
        val tableUsers = schema.tables.find("users")
        Assert.assertNotNull("first table exists", tableUsers)
        Assert.assertTrue("Expected users table", tableUsers!!.name.equals("users",true))
        Assert.assertTrue("User alias 'U' exists", tableUsers.aliases.any { alias -> alias.equals("u", true)})
        Assert.assertEquals( "User table column count", 3, tableUsers.columns.size)
        Assert.assertTrue( "All columns have full table name", tableUsers.columns.all { col -> col.tableName.equals(tableUsers.name)})

        val userid = schema.columns.find("userid")
        Assert.assertNotNull("UserID alias column found", userid)
        Assert.assertEquals("UserID Table is USERS", tableUsers, userid!!.table)
        Assert.assertTrue("Users.ID is full name", userid.fullName().equals( "USERS.ID", true))
        Assert.assertTrue(userid.is_id)

        val tableOrders = schema.tables.find("orders")
        Assert.assertNotNull("Orders table exists", tableOrders)
        Assert.assertTrue("Expected Orders table", tableOrders!!.name.equals("orders", true))
        Assert.assertTrue("Orders alias 'O' exists", tableOrders.aliases.any { alias -> alias.equals("O", true)})
        Assert.assertEquals( "Orders table column count", 3, tableOrders.columns.size)
        Assert.assertTrue( "All columns have full table name", tableOrders.columns.all { col -> col.tableName == tableOrders.name})

        val tableContacts = schema.tables.find("contacts")
        Assert.assertNotNull("Contacts table exists", tableContacts)
        Assert.assertTrue("Expected Contacts table", tableContacts!!.name.equals("contacts", true))
        Assert.assertTrue("Contacts alias 'C' exists", tableContacts.aliases.any { alias -> alias.equals("C", true)})
        Assert.assertEquals( "Contacts table column count", 3, tableContacts.columns.size)
        Assert.assertTrue( "All columns have full table name", tableContacts.columns.all { col -> col.tableName == tableContacts.name})

        val joinOrders = schema.joins.find("orders")
        Assert.assertNotNull("Orders join exists", joinOrders)
        Assert.assertTrue("Orders table name", joinOrders!!.table.name.equals("orders", true))
        Assert.assertTrue("Orders is LEFT join", joinOrders.left)
        Assert.assertTrue("Orders is INNER join", joinOrders.inner)
        Assert.assertTrue("USERS.ID", joinOrders.leftColumn.fullName().equals("users.id", true))
        Assert.assertTrue("ORDERS.USER_ID", joinOrders.rightColumn.fullName().equals("orders.user_id", true))

        val joinContacts = schema.joins.find("contacts")
        Assert.assertNotNull("Contacts join exists", joinContacts)
        Assert.assertTrue("Contacts table name", joinContacts!!.table.name.equals("contacts", true))
        Assert.assertTrue("Contacts is RIGHT join", joinContacts.right)
        Assert.assertTrue("Contacts is OUTER join", joinContacts.outer)
        Assert.assertTrue("USERS.ID", joinContacts.leftColumn.fullName().equals("users.id", true))
        Assert.assertTrue("CONTACTS.USER_ID", joinContacts.rightColumn.fullName().equals("contacts.user_id", true))
    }
}
