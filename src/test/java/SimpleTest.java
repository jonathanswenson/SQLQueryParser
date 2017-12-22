import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Test;
import org.antlr.v4.runtime.CharStream;
import com.looker.sql_query_parser.parser.MySQLLexer;
import com.looker.sql_query_parser.parser.MySQLParser;
import java.io.ByteArrayInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import com.looker.sql_query_parser.parser.ThrowingErrorListener;


public class SimpleTest {
    @Test
    public void testsSomethingSimple() {
        ANTLRInputStream inputStream = new ANTLRInputStream("SELECT * FROM USERS");
        MySQLLexer lexer = new MySQLLexer(inputStream);

        CommonTokenStream tokenStream = new CommonTokenStream(lexer);


        MySQLParser parser = new MySQLParser(tokenStream);
        // use error listener to throw exception when parser fails (as opposed to just printing to console)
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);

    }
}
