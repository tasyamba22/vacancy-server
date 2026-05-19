package com.vacancy.server.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Vacancies : Table("vacancies") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val title = varchar("title", 255)
    val company = varchar("company", 255)
    val salary = varchar("salary", 100).nullable()
    val location = varchar("location", 255).nullable()
    val description = text("description").nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class VacancyResponse(
    val id: Int,
    val userId: Int,
    val title: String,
    val company: String,
    val salary: String?,
    val location: String?,
    val description: String?,
    val createdAt: String,
    val isFavorite: Boolean = false
)

@Serializable
data class CreateVacancyRequest(
    val title: String,
    val company: String,
    val salary: String? = null,
    val location: String? = null,
    val description: String? = null
)

@Serializable
data class UpdateVacancyRequest(
    val title: String? = null,
    val company: String? = null,
    val salary: String? = null,
    val location: String? = null,
    val description: String? = null
)