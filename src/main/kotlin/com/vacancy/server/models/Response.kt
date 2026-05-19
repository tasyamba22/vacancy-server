package com.vacancy.server.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Responses : Table("responses") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val vacancyId = integer("vacancy_id").references(Vacancies.id)
    val resumeId = integer("resume_id").references(Resumes.id)
    val status = varchar("status", 50).default("PENDING")
    val coverLetter = text("cover_letter").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object ResponseStatus {
    const val PENDING = "PENDING"
    const val ACCEPTED = "ACCEPTED"
    const val REJECTED = "REJECTED"
}

@Serializable
data class VacancyResponseItem(
    val id: Int,
    val userId: Int,
    val vacancyId: Int,
    val resumeId: Int,
    val status: String,
    val coverLetter: String?,
    val createdAt: String,
    val updatedAt: String,
    val vacancyTitle: String? = null,
    val companyName: String? = null,
    val applicantEmail: String? = null
)

@Serializable
data class CreateResponseRequest(
    val vacancyId: Int,
    val coverLetter: String? = null
)

@Serializable
data class UpdateResponseStatusRequest(
    val status: String
)