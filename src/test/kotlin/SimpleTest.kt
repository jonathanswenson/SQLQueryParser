
import com.looker.sql_query_parser.parser.CalciteExploreSchema
import com.looker.sql_query_parser.parser.MySQLParser
import com.looker.sql_query_parser.parser.ExploreSchema
import com.looker.sql_query_parser.parser.getChildren
import org.antlr.v4.runtime.tree.ParseTree
import org.apache.calcite.avatica.util.Quoting
import org.apache.calcite.plan.RelOptUtil
import org.apache.calcite.rel.RelNode
import org.apache.calcite.rel.type.RelDataType
import org.apache.calcite.rel.type.RelDataTypeFactory
import org.apache.calcite.schema.SchemaPlus
import org.apache.calcite.schema.impl.AbstractTable
import org.apache.calcite.sql.parser.SqlParser
import org.apache.calcite.sql.type.SqlTypeName
import org.junit.Assert
import org.junit.Test
import org.apache.calcite.tools.Frameworks


class SimpleTest {

    val sql = "select birthdate, u.id userid, u.username, o.id orderid, o.orderdate, c.id contactid, c.email FROM users u JOIN orders o ON u.id = o.user_id RIGHT OUTER JOIN contacts c ON c.user_id = u.id"

    fun preserveCase(sql: String) : MySQLParser.RootContext {
        val root = ExploreSchema.parseSql(sql)
        val source = root.getStart().tokenSource.inputStream.toString()
        Assert.assertEquals("Statement casing should be preserved in inputStream", sql, source)
        return root
    }

    class SimpleTable(val types : List<Pair<String, SqlTypeName>>) : AbstractTable() {
    //        protected var fieldTypes: List<RelDataType> = ArrayList<RelDataType>()

        override fun getRowType(typeFactory: RelDataTypeFactory): RelDataType {
            // not sure how to convert to the right type other than doing this
            val mutableMap = HashMap<String, RelDataType>()
            for ((x, y) in types) {
                mutableMap.put(x, typeFactory.createSqlType(y))
            }
            val entries = mutableMap.entries.toList()
            return typeFactory.createStructType(entries)
        }
    }

    fun basicParse(sql : String, schema : SchemaPlus): RelNode {
        val parserConfig = SqlParser.configBuilder().setQuoting(Quoting.BACK_TICK).build()
        val configBuilder = Frameworks.newConfigBuilder()

        configBuilder.defaultSchema(schema)
        configBuilder.parserConfig(parserConfig)
        val config = configBuilder.build()

        val planner = Frameworks.getPlanner(config)

        val relRoot = planner.rel(planner.validate(planner.parse(sql)))

        return relRoot.rel
    }

    @Test fun someStuffHere() {
        val sql1 = "select u.id + 10 * oi.amount as super, u.name as upper_name, o.status from users as u join orders as o on u.id = o.user_id + 1 join order_items as oi on oi.order_id = o.id where status LIKE '%c%'"
        val sql2 = "Select * from (select orders.user_id, orders.id, sum(amount) as total_amount from orders join order_items on orders.id = order_items.order_id group by orders.id, orders.user_id) as o join users on o.user_id = users.id"
        val sql3 = "Select orders.user_id, orders.id, users.id, users.name, sum(amount) total_amount from users join orders on users.id = orders.user_id join order_items on order_items.order_id = orders.id group by orders.id, orders.user_id, users.id, users.name"

        val schema = Frameworks.createRootSchema(false)

        schema.add("USERS", SimpleTable(arrayListOf("ID", "NAME").zip(arrayListOf(SqlTypeName.INTEGER, SqlTypeName.VARCHAR))))
        schema.add("ORDERS", SimpleTable( arrayListOf("USER_ID", "STATUS", "ID").zip(arrayListOf(SqlTypeName.INTEGER, SqlTypeName.VARCHAR, SqlTypeName.INTEGER))))
        schema.add("ORDER_ITEMS", SimpleTable(arrayListOf("ORDER_ID", "AMOUNT", "ID").zip(arrayListOf(SqlTypeName.INTEGER, SqlTypeName.INTEGER, SqlTypeName.INTEGER))))

        val rel = basicParse(sql2, schema)
        println(RelOptUtil.findAllTables(rel).map{ it.qualifiedName.joinToString(".") })
        println(RelOptUtil.toString(rel))

        println(RelOptUtil.toString(basicParse(sql1, schema)))
        println(RelOptUtil.toString(basicParse(sql3, schema)))
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
