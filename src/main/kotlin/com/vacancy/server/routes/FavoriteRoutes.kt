package com.vacancy.server.routes

import com.vacancy.server.repositories.FavoriteRepository
import com.vacancy.server.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.favoriteRoutes(
    favoriteRepository: FavoriteRepository,
    userRepository: UserRepository
) {
    authenticate("jwt-auth") {
        route("/favorites") {

            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()

                val favorites = favoriteRepository.getFavoriteVacancies(userId)
                call.respond(HttpStatusCode.OK, favorites)
            }

            post("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val vacancyId = call.parameters["id"]?.toIntOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid vacancy id"))
                        return@post
                    }

                val added = favoriteRepository.add(userId, vacancyId)
                if (added) {
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Added to favorites"))
                } else {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "Already in favorites"))
                }
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val vacancyId = call.parameters["id"]?.toIntOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid vacancy id"))
                        return@delete
                    }

                val removed = favoriteRepository.remove(userId, vacancyId)
                if (removed) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Removed from favorites"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not in favorites"))
                }
            }
        }
    }
}