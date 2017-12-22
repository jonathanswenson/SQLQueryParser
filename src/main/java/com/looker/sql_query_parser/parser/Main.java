package com.looker.sql_query_parser.parser;

import com.looker.sql_query_parser.parser.ThrowingErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;


public class Main {
    public static void main(String[] args) {
        ANTLRInputStream inputStream = new ANTLRInputStream("SELECT ** FROM USERS");
        MySQLLexer lexer = new MySQLLexer(inputStream);

        CommonTokenStream tokenStream = new CommonTokenStream(lexer);

        MySQLParser parser = new MySQLParser(tokenStream);

        // use error listener to throw exception when parser fails (as opposed to just printing to console)
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);

        System.out.println("HEY");
    }
}
