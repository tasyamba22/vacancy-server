package com.vacancy.server.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 50).default("USER")
    val isBlocked = bool("is_blocked").default(false)
    val firstName = varchar("first_name", 100).nullable()
    val lastName = varchar("last_name", 100).nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val role: String,
    val isBlocked: Boolean,
    val firstName: String?,
    val lastName: String?,
    val createdAt: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val role: String,
    val userId: Int
)

@Serializable
data class UpdateProfileRequest(
    val firstName: String?,
    val lastName: String?
)