package com.looker.sql_query_parser.parser

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

/*
 * Throws an exception (ParseCancellationException) when a syntax error is found.
 */
class ThrowingErrorListener : BaseErrorListener() {
    @Throws(ParseCancellationException::class)
    override fun syntaxError(recognizer: Recognizer<*, *>, offendingSymbol: Any, line: Int, charPositionInLine: Int, msg: String, e: RecognitionException) {
        throw ParseCancellationException("line $line:$charPositionInLine $msg")
    }

    companion object {
        @JvmField
        val INSTANCE = ThrowingErrorListener()
    }
}
