package com.vacancy.server.repositories

import com.vacancy.server.models.Favorites
import com.vacancy.server.models.Vacancies
import com.vacancy.server.models.VacancyResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class FavoriteRepository {

    fun getFavoriteIds(userId: Int): Set<Int> = transaction {
        Favorites.select { Favorites.userId eq userId }
            .map { it[Favorites.vacancyId] }
            .toSet()
    }

    fun getFavoriteVacancies(userId: Int): List<VacancyResponse> = transaction {
        val favoriteIds = getFavoriteIds(userId)
        (Vacancies innerJoin Favorites)
            .select { Favorites.userId eq userId }
            .map { row ->
                VacancyResponse(
                    id = row[Vacancies.id],
                    userId = row[Vacancies.userId],
                    title = row[Vacancies.title],
                    company = row[Vacancies.company],
                    salary = row[Vacancies.salary],
                    location = row[Vacancies.location],
                    description = row[Vacancies.description],
                    createdAt = row[Vacancies.createdAt].toString(),
                    isFavorite = true
                )
            }
    }

    fun add(userId: Int, vacancyId: Int): Boolean = transaction {
        val exists = Favorites.select {
            (Favorites.userId eq userId) and (Favorites.vacancyId eq vacancyId)
        }.count() > 0

        if (exists) return@transaction false

        Favorites.insert {
            it[Favorites.userId] = userId
            it[Favorites.vacancyId] = vacancyId
        }
        true
    }

    fun remove(userId: Int, vacancyId: Int): Boolean = transaction {
        Favorites.deleteWhere {
            (Favorites.userId eq userId) and (Favorites.vacancyId eq vacancyId)
        } > 0
    }
}