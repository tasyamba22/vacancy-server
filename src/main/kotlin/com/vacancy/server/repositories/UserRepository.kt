package com.vacancy.server.repositories

import com.vacancy.server.models.UserResponse
import com.vacancy.server.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

data class UserRecord(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val role: String,
    val isBlocked: Boolean
)

class UserRepository {

    fun findByEmail(email: String): UserRecord? = transaction {
        Users.select { Users.email eq email }
            .map { toRecord(it) }
            .singleOrNull()
    }

    fun findById(id: Int): UserRecord? = transaction {
        Users.select { Users.id eq id }
            .map { toRecord(it) }
            .singleOrNull()
    }

    fun create(email: String, passwordHash: String): Int = transaction {
        Users.insert {
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
            it[Users.role] = "USER"
            it[Users.isBlocked] = false
            it[Users.createdAt] = Instant.now()
        }[Users.id]
    }

    fun getAllUsers(): List<UserResponse> = transaction {
        Users.selectAll().map { toResponse(it) }
    }

    fun blockUser(id: Int): Boolean = transaction {
        Users.update({ Users.id eq id }) {
            it[isBlocked] = true
        } > 0
    }

    fun unblockUser(id: Int): Boolean = transaction {
        Users.update({ Users.id eq id }) {
            it[isBlocked] = false
        } > 0
    }

    fun changeRole(id: Int, role: String): Boolean = transaction {
        Users.update({ Users.id eq id }) {
            it[Users.role] = role
        } > 0
    }

    fun countUsers(): Long = transaction {
        Users.selectAll().count()
    }

    private fun toRecord(row: ResultRow) = UserRecord(
        id = row[Users.id],
        email = row[Users.email],
        passwordHash = row[Users.passwordHash],
        role = row[Users.role],
        isBlocked = row[Users.isBlocked]
    )

    private fun toResponse(row: ResultRow) = UserResponse(
        id = row[Users.id],
        email = row[Users.email],
        role = row[Users.role],
        isBlocked = row[Users.isBlocked],
        createdAt = row[Users.createdAt].toString()
    )
}
