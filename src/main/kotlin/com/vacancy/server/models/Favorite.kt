package com.vacancy.server.models

import org.jetbrains.exposed.sql.Table

object Favorites : Table("favorites") {
    val userId = integer("user_id").references(Users.id)
    val vacancyId = integer("vacancy_id").references(Vacancies.id)

    override val primaryKey = PrimaryKey(userId, vacancyId)
}