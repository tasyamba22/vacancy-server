package com.vacancy.server.repositories

import com.vacancy.server.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class ResponseRepository {

    fun create(userId: Int, vacancyId: Int, resumeId: Int, coverLetter: String?): Int = transaction {
        Responses.insert {
            it[Responses.userId] = userId
            it[Responses.vacancyId] = vacancyId
            it[Responses.resumeId] = resumeId
            it[Responses.status] = ResponseStatus.PENDING
            it[Responses.coverLetter] = coverLetter
            it[Responses.createdAt] = Instant.now()
            it[Responses.updatedAt] = Instant.now()
        }[Responses.id]
    }

    fun alreadyResponded(userId: Int, vacancyId: Int): Boolean = transaction {
        Responses.select {
            (Responses.userId eq userId) and (Responses.vacancyId eq vacancyId)
        }.count() > 0
    }

    // Мои отклики с названием вакансии и компании
    fun getByUser(userId: Int): List<VacancyResponseItem> = transaction {
        (Responses innerJoin Vacancies)
            .select { Responses.userId eq userId }
            .orderBy(Responses.createdAt, SortOrder.DESC)
            .map {
                VacancyResponseItem(
                    id = it[Responses.id],
                    userId = it[Responses.userId],
                    vacancyId = it[Responses.vacancyId],
                    resumeId = it[Responses.resumeId],
                    status = it[Responses.status],
                    coverLetter = it[Responses.coverLetter],
                    createdAt = it[Responses.createdAt].toString(),
                    updatedAt = it[Responses.updatedAt].toString(),
                    vacancyTitle = it[Vacancies.title],
                    companyName = it[Vacancies.company]
                )
            }
    }

    // Отклики на вакансию с email соискателя
    fun getByVacancy(vacancyId: Int): List<VacancyResponseItem> = transaction {
        (Responses innerJoin Users)
            .select { Responses.vacancyId eq vacancyId }
            .orderBy(Responses.createdAt, SortOrder.DESC)
            .map {
                VacancyResponseItem(
                    id = it[Responses.id],
                    userId = it[Responses.userId],
                    vacancyId = it[Responses.vacancyId],
                    resumeId = it[Responses.resumeId],
                    status = it[Responses.status],
                    coverLetter = it[Responses.coverLetter],
                    createdAt = it[Responses.createdAt].toString(),
                    updatedAt = it[Responses.updatedAt].toString(),
                    applicantEmail = it[Users.email]
                )
            }
    }

    fun findById(id: Int): VacancyResponseItem? = transaction {
        Responses.select { Responses.id eq id }
            .map {
                VacancyResponseItem(
                    id = it[Responses.id],
                    userId = it[Responses.userId],
                    vacancyId = it[Responses.vacancyId],
                    resumeId = it[Responses.resumeId],
                    status = it[Responses.status],
                    coverLetter = it[Responses.coverLetter],
                    createdAt = it[Responses.createdAt].toString(),
                    updatedAt = it[Responses.updatedAt].toString()
                )
            }
            .singleOrNull()
    }

    fun updateStatus(id: Int, status: String): Boolean = transaction {
        Responses.update({ Responses.id eq id }) {
            it[Responses.status] = status
            it[Responses.updatedAt] = Instant.now()
        } > 0
    }

    fun delete(id: Int): Boolean = transaction {
        Responses.deleteWhere { Responses.id eq id } > 0
    }

    fun getAll(): List<VacancyResponseItem> = transaction {
        Responses.selectAll()
            .orderBy(Responses.createdAt, SortOrder.DESC)
            .map {
                VacancyResponseItem(
                    id = it[Responses.id],
                    userId = it[Responses.userId],
                    vacancyId = it[Responses.vacancyId],
                    resumeId = it[Responses.resumeId],
                    status = it[Responses.status],
                    coverLetter = it[Responses.coverLetter],
                    createdAt = it[Responses.createdAt].toString(),
                    updatedAt = it[Responses.updatedAt].toString()
                )
            }
    }

    fun getOwner(id: Int): Int? = transaction {
        Responses.select { Responses.id eq id }
            .map { it[Responses.userId] }
            .singleOrNull()
    }

    fun getVacancyId(id: Int): Int? = transaction {
        Responses.select { Responses.id eq id }
            .map { it[Responses.vacancyId] }
            .singleOrNull()
    }
}