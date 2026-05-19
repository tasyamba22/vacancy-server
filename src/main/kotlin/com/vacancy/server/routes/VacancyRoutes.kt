package com.vacancy.server.routes

import com.vacancy.server.models.CreateVacancyRequest
import com.vacancy.server.models.UpdateVacancyRequest
import com.vacancy.server.repositories.FavoriteRepository
import com.vacancy.server.repositories.UserRepository
import com.vacancy.server.repositories.VacancyRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.vacancyRoutes(
    vacancyRepository: VacancyRepository,
    userRepository: UserRepository
) {
    val favoriteRepository = FavoriteRepository()

    authenticate("jwt-auth") {
        route("/vacancies") {

            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()

                val user = userRepository.findById(userId)
                if (user?.isBlocked == true) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Your account is blocked"))
                    return@get
                }

                val favoriteIds = favoriteRepository.getFavoriteIds(userId)
                val vacancies = vacancyRepository.getAll(userId, favoriteIds)
                call.respond(HttpStatusCode.OK, vacancies)
            }

            get("/search") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val query = call.request.queryParameters["query"] ?: ""

                val favoriteIds = favoriteRepository.getFavoriteIds(userId)
                val vacancies = vacancyRepository.search(query, userId, favoriteIds)
                call.respond(HttpStatusCode.OK, vacancies)
            }

            get("/my") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()

                val favoriteIds = favoriteRepository.getFavoriteIds(userId)
                val vacancies = vacancyRepository.getByUser(userId, favoriteIds)
                call.respond(HttpStatusCode.OK, vacancies)
            }

            post {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()

                val user = userRepository.findById(userId)
                if (user?.isBlocked == true) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Your account is blocked"))
                    return@post
                }

                val request = call.receive<CreateVacancyRequest>()

                if (request.title.isBlank() || request.company.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Title and company are required"))
                    return@post
                }

                val id = vacancyRepository.create(
                    userId = userId,
                    title = request.title,
                    company = request.company,
                    salary = request.salary,
                    location = request.location,
                    description = request.description
                )
                val created = vacancyRepository.findById(id)
                call.respond(HttpStatusCode.Created, created!!)
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val vacancyId = call.parameters["id"]?.toIntOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid vacancy id"))
                        return@put
                    }

                val owner = vacancyRepository.getOwner(vacancyId)
                if (owner == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Vacancy not found"))
                    return@put
                }
                if (owner != userId) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "You can only edit your own vacancies"))
                    return@put
                }

                val request = call.receive<UpdateVacancyRequest>()
                vacancyRepository.update(vacancyId, request.title, request.company, request.salary, request.location, request.description)

                val updated = vacancyRepository.findById(vacancyId)
                call.respond(HttpStatusCode.OK, updated!!)
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val role = principal.payload.getClaim("role").asString()
                val vacancyId = call.parameters["id"]?.toIntOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid vacancy id"))
                        return@delete
                    }

                val owner = vacancyRepository.getOwner(vacancyId)
                if (owner == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Vacancy not found"))
                    return@delete
                }

                if (role != "ADMIN" && owner != userId) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "You can only delete your own vacancies"))
                    return@delete
                }

                vacancyRepository.delete(vacancyId)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Vacancy deleted"))
            }
        }
    }
}