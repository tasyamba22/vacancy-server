package com.vacancy.server.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Tokens : Table("tokens") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val token = text("token")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}