import com.looker.sql_query_parser.parser.Column
import com.looker.sql_query_parser.parser.ISchemaFinder
import com.looker.sql_query_parser.parser.RubyScript
import org.junit.Assert
import org.junit.Test

// Goal:
//   - use Calcite for SQL parser which returns schema information to support focused LookML generation
//   - manage Ruby object collections directly from Kotlin
//
// Kotlin/JRuby interop options:
// - Pass Ruby object to Kotlin
//   - Works: easy, clean, and reviewable when using an Interface
//   - IntelliJ/language limitations:
//     - IDE gets confused between snake_case and camelCase Interface declarations
//     - attr_accessor does not fulfill the implementation requirements for Kotlin property interfaces
// - Create Ruby object via script in Kotlin
//   - Works, but probable performance impact even with `RubyScript` utility class
// - Create Ruby object via reflection
//   - Haven't gotten it to work due to namespace resolution issues
//   - e.g "RubySchema$$Finder_1310805313"
class JRubyInteropTest {

    var finder : ISchemaFinder

    init {
        var script  = """
require './src/main/ruby/ruby_schema_finder'
RubySchema::Finder.new"""
        finder = RubyScript.RubyObject(script) as ISchemaFinder
        finder.addColumn(Column("table.user_id"))
        finder.addColumn(Column("table.name"))
    }

    @Test fun `must find existing columns`() {
        println(finder.javaClass.kotlin)
        var column = finder.getColumn("table.user_id")
        Assert.assertEquals(column.originalName, "table.user_id")
        Assert.assertEquals(column.name, "user_id")
        Assert.assertEquals(column.tableName, "table")
        Assert.assertEquals(column.fullName(), "table.user_id")

        column = finder.getColumn("table.name")
        Assert.assertEquals(column.originalName, "table.name")
        Assert.assertEquals(column.name, "name")
        Assert.assertEquals(column.tableName, "table")
        Assert.assertEquals(column.fullName(), "table.name")
    }

    @Test fun `must not find missing columns`() {
        var column = finder.getColumn("table.foo")
        Assert.assertNull(column)
    }
}
