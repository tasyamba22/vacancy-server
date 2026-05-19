package com.vacancy.server.repositories

import com.vacancy.server.models.Tokens
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class TokenRepository {

    fun save(userId: Int, token: String) = transaction {
        Tokens.insert {
            it[Tokens.userId] = userId
            it[Tokens.token] = token
            it[Tokens.createdAt] = Instant.now()
        }
    }

    fun exists(token: String): Boolean = transaction {
        Tokens.select { Tokens.token eq token }.count() > 0
    }

    fun delete(token: String): Boolean = transaction {
        Tokens.deleteWhere { Tokens.token eq token } > 0
    }

    fun deleteByUser(userId: Int) = transaction {
        Tokens.deleteWhere { Tokens.userId eq userId }
    }
}