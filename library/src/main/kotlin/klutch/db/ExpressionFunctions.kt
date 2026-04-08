package klutch.db

import kabinet.web.Url
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.stringLiteral

fun Expression<String>.eq(url: Url): Op<Boolean> = this.eq(stringLiteral(url.href))