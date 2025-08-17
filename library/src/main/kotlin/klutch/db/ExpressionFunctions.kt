package klutch.db

import kabinet.web.Url
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.stringLiteral

fun Expression<String>.eq(url: Url): Op<Boolean> = this.eq(stringLiteral(url.href))