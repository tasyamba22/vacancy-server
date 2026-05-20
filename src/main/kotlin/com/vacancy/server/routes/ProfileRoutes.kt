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

fun Route.profileRoutes(userRepository: UserRepository){
    authenticate("jwt-auth") {
        route("/profile"){

            get{
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val user = userRepository.findById(userId)
                    ?: run {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                        return@get
                    }
                call.respond(
                    HttpStatusCode.OK, mapOf(
                    "id" to user.id,
                    "email" to user.email,
                    "role" to user.role,
                    "firstName" to user.firstName,
                    "lastName" to user.lastName
                ))
            }

            put {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val request = call.receive<UpdateProfileRequest>()
                userRepository.updateProfile(userId, request.firstName, request.lastName)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Profile updated"))
            }
        }
    }
}