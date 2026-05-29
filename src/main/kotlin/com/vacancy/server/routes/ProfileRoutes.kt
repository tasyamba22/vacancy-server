package com.vacancy.server.routes

import com.vacancy.server.models.UpdateProfileRequest
import com.vacancy.server.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.profileRoutes(userRepository: UserRepository) {
    authenticate("jwt-auth") {
        route("/profile") {

            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()

                val user = userRepository.findById(userId)
                    ?: run {
                        call.respond(HttpStatusCode.NotFound, """{"error": "User not found"}""")
                        return@get
                    }
                
                val response = """
                    {
                        "id": ${user.id},
                        "email": "${user.email}",
                        "role": "${user.role}",
                        "firstName": ${user.firstName?.let { "\"$it\"" } ?: "null"},
                        "lastName": ${user.lastName?.let { "\"$it\"" } ?: "null"}
                    }
                """.trimIndent()

                call.respondText(
                    contentType = ContentType.Application.Json,
                    text = response
                )
            }

            put {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val request = call.receive<UpdateProfileRequest>()

                userRepository.updateProfile(userId, request.firstName, request.lastName)

                call.respondText(
                    contentType = ContentType.Application.Json,
                    text = """{"message": "Profile updated successfully"}"""
                )
            }
        }
    }
}