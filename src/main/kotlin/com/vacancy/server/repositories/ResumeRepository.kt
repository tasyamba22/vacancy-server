package com.vacancy.server.repositories

import com.vacancy.server.models.ResumeResponse
import com.vacancy.server.models.Resumes
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class ResumeRepository {

    fun findByUserId(userId: Int): ResumeResponse? = transaction {
        Resumes.select { Resumes.userId eq userId }
            .map { toResponse(it) }
            .singleOrNull()
    }

    fun findById(id: Int): ResumeResponse? = transaction {
        Resumes.select { Resumes.id eq id }
            .map { toResponse(it) }
            .singleOrNull()
    }

    fun create(
        userId: Int,
        fullName: String,
        phone: String?,
        skills: String?,
        experience: String?,
        education: String?
    ): Int = transaction {
        Resumes.insert {
            it[Resumes.userId] = userId
            it[Resumes.fullName] = fullName
            it[Resumes.phone] = phone
            it[Resumes.skills] = skills
            it[Resumes.experience] = experience
            it[Resumes.education] = education
            it[Resumes.createdAt] = Instant.now()
            it[Resumes.updatedAt] = Instant.now()
        }[Resumes.id]
    }

    fun update(
        userId: Int,
        fullName: String?,
        phone: String?,
        skills: String?,
        experience: String?,
        education: String?
    ): Boolean = transaction {
        Resumes.update({ Resumes.userId eq userId }) { stmt ->
            fullName?.let { stmt[Resumes.fullName] = it }
            phone?.let { stmt[Resumes.phone] = it }
            skills?.let { stmt[Resumes.skills] = it }
            experience?.let { stmt[Resumes.experience] = it }
            education?.let { stmt[Resumes.education] = it }
            stmt[Resumes.updatedAt] = Instant.now()
        } > 0
    }

    fun delete(userId: Int): Boolean = transaction {
        Resumes.deleteWhere { Resumes.userId eq userId } > 0
    }

    fun getAll(): List<ResumeResponse> = transaction {
        Resumes.selectAll().map { toResponse(it) }
    }

    fun existsForUser(userId: Int): Boolean = transaction {
        Resumes.select { Resumes.userId eq userId }.count() > 0
    }

    private fun toResponse(row: ResultRow) = ResumeResponse(
        id = row[Resumes.id],
        userId = row[Resumes.userId],
        fullName = row[Resumes.fullName],
        phone = row[Resumes.phone],
        skills = row[Resumes.skills],
        experience = row[Resumes.experience],
        education = row[Resumes.education],
        createdAt = row[Resumes.createdAt].toString(),
        updatedAt = row[Resumes.updatedAt].toString()
    )
}