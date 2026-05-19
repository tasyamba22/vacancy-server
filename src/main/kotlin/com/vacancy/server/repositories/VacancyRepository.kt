package com.vacancy.server.repositories

import com.vacancy.server.models.VacancyResponse
import com.vacancy.server.models.Vacancies
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class VacancyRepository {

    fun getAll(currentUserId: Int, favoriteIds: Set<Int>): List<VacancyResponse> = transaction {
        Vacancies.selectAll()
            .orderBy(Vacancies.createdAt, SortOrder.DESC)
            .map { toResponse(it, favoriteIds) }
    }

    fun search(query: String, currentUserId: Int, favoriteIds: Set<Int>): List<VacancyResponse> = transaction {
        val q = "%${query.lowercase()}%"
        Vacancies.select {
            (Vacancies.title.lowerCase() like q) or
                    (Vacancies.company.lowerCase() like q) or
                    (Vacancies.description.lowerCase() like q)
        }
            .orderBy(Vacancies.createdAt, SortOrder.DESC)
            .map { toResponse(it, favoriteIds) }
    }

    fun getByUser(userId: Int, favoriteIds: Set<Int>): List<VacancyResponse> = transaction {
        Vacancies.select { Vacancies.userId eq userId }
            .orderBy(Vacancies.createdAt, SortOrder.DESC)
            .map { toResponse(it, favoriteIds) }
    }

    fun findById(id: Int): VacancyResponse? = transaction {
        Vacancies.select { Vacancies.id eq id }
            .map { toResponse(it, emptySet()) }
            .singleOrNull()
    }

    fun create(
        userId: Int,
        title: String,
        company: String,
        salary: String?,
        location: String?,
        description: String?
    ): Int = transaction {
        Vacancies.insert {
            it[Vacancies.userId] = userId
            it[Vacancies.title] = title
            it[Vacancies.company] = company
            it[Vacancies.salary] = salary
            it[Vacancies.location] = location
            it[Vacancies.description] = description
            it[Vacancies.createdAt] = Instant.now()
        }[Vacancies.id]
    }

    fun update(
        id: Int,
        title: String?,
        company: String?,
        salary: String?,
        location: String?,
        description: String?
    ): Boolean = transaction {
        Vacancies.update({ Vacancies.id eq id }) { stmt ->
            title?.let { stmt[Vacancies.title] = it }
            company?.let { stmt[Vacancies.company] = it }
            salary?.let { stmt[Vacancies.salary] = it }
            location?.let { stmt[Vacancies.location] = it }
            description?.let { stmt[Vacancies.description] = it }
        } > 0
    }

    fun delete(id: Int): Boolean = transaction {
        Vacancies.deleteWhere { Vacancies.id eq id } > 0
    }

    fun getOwner(id: Int): Int? = transaction {
        Vacancies.select { Vacancies.id eq id }
            .map { it[Vacancies.userId] }
            .singleOrNull()
    }

    fun countVacancies(): Long = transaction {
        Vacancies.selectAll().count()
    }

    private fun toResponse(row: ResultRow, favoriteIds: Set<Int>) = VacancyResponse(
        id = row[Vacancies.id],
        userId = row[Vacancies.userId],
        title = row[Vacancies.title],
        company = row[Vacancies.company],
        salary = row[Vacancies.salary],
        location = row[Vacancies.location],
        description = row[Vacancies.description],
        createdAt = row[Vacancies.createdAt].toString(),
        isFavorite = row[Vacancies.id] in favoriteIds
    )
}