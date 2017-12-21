package com.looker.sql_query_parser

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import com.looker.sql_query_parser.parser.MySQLLexer;
import com.looker.sql_query_parser.parser.MySQLParser
import java.io.ByteArrayInputStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.lang.Exception


fun main(args: Array<String>) {
//    try {
//        val inputStream = ByteArrayInputStream("SELECT ** FROM USERS".toByteArray())
//
//        val lexer = MySQLLexer(CharStreams.fromStream(inputStream));
//        val tokenStream = CommonTokenStream(lexer)
//
//        val parser = MySQLParser(tokenStream)
//
//        val rootContext = parser.root();
//    } catch() {
//        println(e.message)
//        e.printStackTrace()
//    }

    // hey at this point we've parsed properly!

    println("HEYKT");
}
