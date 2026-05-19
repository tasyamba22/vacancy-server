package com.vacancy.server.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Resumes : Table("resumes") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id).uniqueIndex()
    val fullName = varchar("full_name", 255)
    val phone = varchar("phone", 50).nullable()
    val skills = text("skills").nullable()
    val experience = text("experience").nullable()
    val education = text("education").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class ResumeResponse(
    val id: Int,
    val userId: Int,
    val fullName: String,
    val phone: String?,
    val skills: String?,
    val experience: String?,
    val education: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateResumeRequest(
    val fullName: String,
    val phone: String? = null,
    val skills: String? = null,
    val experience: String? = null,
    val education: String? = null
)

@Serializable
data class UpdateResumeRequest(
    val fullName: String? = null,
    val phone: String? = null,
    val skills: String? = null,
    val experience: String? = null,
    val education: String? = null
)