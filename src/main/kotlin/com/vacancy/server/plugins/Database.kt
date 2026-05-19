package com.vacancy.server.plugins

import com.vacancy.server.models.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    // Читаем сначала из переменных окружения, потом из конфига
    val dbUrl = System.getenv("DATABASE_URL")
        ?: environment.config.propertyOrNull("ktor.database.url")?.getString()
        ?: throw IllegalStateException("DATABASE_URL is not configured")

    val dbUser = System.getenv("DB_USER")
        ?: environment.config.propertyOrNull("ktor.database.user")?.getString()
        ?: throw IllegalStateException("DB_USER is not configured")

    val dbPassword = System.getenv("DB_PASSWORD")
        ?: environment.config.propertyOrNull("ktor.database.password")?.getString()
        ?: throw IllegalStateException("DB_PASSWORD is not configured")

    val dbDriver = "org.postgresql.Driver"

    log.info("Connecting to DB: $dbUrl as $dbUser")

    val hikariConfig = HikariConfig().apply {
        jdbcUrl = dbUrl
        driverClassName = dbDriver
        username = dbUser
        password = dbPassword
        maximumPoolSize = 5
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(Users, Vacancies, Favorites, Tokens, Resumes, Responses)
    }

    log.info("Database connected and tables created successfully")
}