package com.looker.sql_query_parser.parser

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTreeListener
import org.antlr.v4.runtime.tree.TerminalNode

class PrintingListener : ParseTreeListener {
    override fun visitTerminal(p0: TerminalNode?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitErrorNode(p0: ErrorNode?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterEveryRule(p0: ParserRuleContext) {
        println(p0.getText());
    }

    override fun exitEveryRule(p0: ParserRuleContext) {
        println(p0.getText());
    }
}
