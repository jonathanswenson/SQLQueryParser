package com.looker.sql_query_parser.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;


public class Main {
    public static void main(String[] args) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("SELECT ** FROM USERS".getBytes());
        try {
            MySQLLexer lexer = new MySQLLexer(CharStreams.fromStream(inputStream));
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);

            MySQLParser parser = new MySQLParser(tokenStream);

            // use error listener to throw exception when parser fails (as opposed to just printing to console)
            parser.removeErrorListeners();
            // unfortunately this does not work because the kotlin needs to be compiled first.
            //  parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }



        System.out.println("HEY I'M USELESS NOW");
    }
}
