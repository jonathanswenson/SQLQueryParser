package com.looker.sql_query_parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import com.looker.sql_query_parser.parser.MySQLLexer;


public class Main {
    public static void main(String[] args) {
        ANTLRInputStream inputStream = new ANTLRInputStream("SELECT * FROM USERS");
        MySQLLexer lexer = new MySQLLexer(inputStream);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        System.out.println(tokens);

        System.out.println("HEY");
    }
}
