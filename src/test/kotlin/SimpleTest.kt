import org.antlr.v4.runtime.CharStream
import org.junit.Test

import com.looker.sql_query_parser.parser.MySQLLexer
import java.io.ByteArrayInputStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import com.looker.sql_query_parser.parser.MySQLParser
import com.looker.sql_query_parser.parser.ThrowingErrorListener


public class SimpleTest {
    @Test
    fun testsSomethingSimple() {
        val inputStream = ByteArrayInputStream("SELECT * FROM USERS".toByteArray())
        val lexer = MySQLLexer(CharStreams.fromStream(inputStream));
        val tokenStream = CommonTokenStream(lexer)


        val parser = MySQLParser(tokenStream)
        // use error listener to throw exception when parser fails (as opposed to just printing to console)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener.INSTANCE)

        val rootContext = parser.root();

        println("HEYTEST")
    }
}
