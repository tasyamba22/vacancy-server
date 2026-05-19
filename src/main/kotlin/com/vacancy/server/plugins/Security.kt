package com.vacancy.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    val jwtSecret = environment.config.propertyOrNull("ktor.security.jwt.secret")?.getString()
        ?: throw IllegalStateException("JWT_SECRET is not configured")
    val jwtIssuer = environment.config.property("ktor.security.jwt.issuer").getString()
    val jwtAudience = environment.config.property("ktor.security.jwt.audience").getString()

    authentication {
        jwt("jwt-auth") {
            realm = "Vacancy Server"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asInt()
                val role = credential.payload.getClaim("role").asString()
                if (userId != null && role != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token is invalid or expired"))
            }
        }
    }
}