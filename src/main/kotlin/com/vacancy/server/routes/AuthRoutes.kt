package com.vacancy.server.routes

import com.vacancy.server.models.AuthResponse
import com.vacancy.server.models.LoginRequest
import com.vacancy.server.models.RegisterRequest
import com.vacancy.server.repositories.TokenRepository
import com.vacancy.server.repositories.UserRepository
import com.vacancy.server.utils.HashHelper
import com.vacancy.server.utils.JwtHelper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(
    userRepository: UserRepository,
    tokenRepository: TokenRepository,
    jwtSecret: String,
    jwtIssuer: String,
    jwtAudience: String,
    jwtExpiration: Long
) {
    route("/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()

            if (request.email.isBlank() || request.password.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email and password are required"))
                return@post
            }

            if (request.password.length < 6) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Password must be at least 6 characters"))
                return@post
            }

            if (userRepository.findByEmail(request.email) != null) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "User with this email already exists"))
                return@post
            }

            val passwordHash = HashHelper.hashPassword(request.password)
            val userId = userRepository.create(request.email, passwordHash)

            val accessToken = JwtHelper.generateToken(userId, "USER", jwtSecret, jwtIssuer, jwtAudience, jwtExpiration)
            val refreshToken = JwtHelper.generateRefreshToken(userId, jwtSecret, jwtIssuer, jwtAudience)

            tokenRepository.save(userId, accessToken)
            call.respond(HttpStatusCode.Created, AuthResponse(token = accessToken, refreshToken = refreshToken, role = "USER", userId = userId))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val user = userRepository.findByEmail(request.email)
                ?: run {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid email or password"))
                    return@post
                }

            if (user.isBlocked) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Your account is blocked"))
                return@post
            }

            if (!HashHelper.verifyPassword(request.password, user.passwordHash)) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid email or password"))
                return@post
            }

            val accessToken = JwtHelper.generateToken(user.id, user.role, jwtSecret, jwtIssuer, jwtAudience, jwtExpiration)
            val refreshToken = JwtHelper.generateRefreshToken(user.id, jwtSecret, jwtIssuer, jwtAudience)

            tokenRepository.save(user.id, accessToken)

            call.respond(HttpStatusCode.OK, AuthResponse(token = accessToken,  refreshToken = refreshToken, role = user.role, userId = user.id))
        }

        authenticate("jwt-auth") {
            post("/logout") {
                val principal = call.principal<JWTPrincipal>()!!
                val token = call.request.headers["Authorization"]
                    ?.removePrefix("Bearer ")
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No token provided"))
                        return@post
                    }

                tokenRepository.delete(token)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Logged out successfully"))
            }
        }
    }
}