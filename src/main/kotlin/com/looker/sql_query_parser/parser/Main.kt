package com.looker.sql_query_parser.parser

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.ByteArrayInputStream

fun main(args: Array<String>) {
    val inputStream = ByteArrayInputStream("SELECT * FROM USERS".toByteArray())
    val lexer = MySQLLexer(CharStreams.fromStream(inputStream));
    val tokenStream = CommonTokenStream(lexer)

    val parser = MySQLParser(tokenStream)
    parser.removeErrorListeners();
    parser.addErrorListener(ThrowingErrorListener.INSTANCE)

    val rootContext = parser.root();

    rootContext
}
