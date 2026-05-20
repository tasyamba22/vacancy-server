package com.vacancy.server.plugins

import com.vacancy.server.repositories.*
import com.vacancy.server.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepository = UserRepository()
    val vacancyRepository = VacancyRepository()
    val favoriteRepository = FavoriteRepository()
    val tokenRepository = TokenRepository()
    val resumeRepository = ResumeRepository()
    val responseRepository = ResponseRepository()

    val jwtSecret = System.getenv("JWT_SECRET")
        ?: environment.config.propertyOrNull("ktor.security.jwt.secret")?.getString()
        ?: throw IllegalStateException("JWT_SECRET is not configured")
    val jwtIssuer = environment.config.property("ktor.security.jwt.issuer").getString()
    val jwtAudience = environment.config.property("ktor.security.jwt.audience").getString()
    val jwtExpiration = environment.config.propertyOrNull("ktor.security.jwt.expiration")?.getString()?.toLong()
        ?: 86400000L

    routing {
        authRoutes(userRepository, tokenRepository, jwtSecret, jwtIssuer, jwtAudience, jwtExpiration)
        vacancyRoutes(vacancyRepository, userRepository)
        favoriteRoutes(favoriteRepository, userRepository)
        adminRoutes(userRepository, vacancyRepository)
        resumeRoutes(resumeRepository, userRepository)
        responseRoutes(responseRepository, resumeRepository, vacancyRepository, userRepository)
        profileRoutes(userRepository)
    }
}