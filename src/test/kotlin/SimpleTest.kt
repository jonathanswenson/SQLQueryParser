import org.antlr.v4.runtime.CharStream
import org.junit.Test

import com.looker.sql_query_parser.parser.MySQLLexer
import java.io.ByteArrayInputStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import com.looker.sql_query_parser.parser.MySQLParser

public class SimpleTest {
    @Test
    fun testsSomethingSimple() {
        val inputStream = ByteArrayInputStream("SELECT * FROM USERS".toByteArray())
        val lexer = MySQLLexer(CharStreams.fromStream(inputStream));
        val tokenStream = CommonTokenStream(lexer)


        val parser = MySQLParser(tokenStream)

        val rootContext = parser.root();

        println("HEYTEST")
    }
}
