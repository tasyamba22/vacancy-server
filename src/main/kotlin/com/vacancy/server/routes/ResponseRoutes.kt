package com.vacancy.server.routes

import com.vacancy.server.models.CreateResponseRequest
import com.vacancy.server.models.ResponseStatus
import com.vacancy.server.models.UpdateResponseStatusRequest
import com.vacancy.server.repositories.ResponseRepository
import com.vacancy.server.repositories.ResumeRepository
import com.vacancy.server.repositories.UserRepository
import com.vacancy.server.repositories.VacancyRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.responseRoutes(
    responseRepository: ResponseRepository,
    resumeRepository: ResumeRepository,
    vacancyRepository: VacancyRepository,
    userRepository: UserRepository
) {
    authenticate("jwt-auth") {
        route("/responses") {

            get {
                val principal = call.principal<JWTPrincipal>()!!
                val role = principal.payload.getClaim("role").asString()
                if (role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                    return@get
                }
                call.respond(HttpStatusCode.OK, responseRepository.getAll())
            }

            post {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()

                val user = userRepository.findById(userId)
                if (user?.isBlocked == true) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Your account is blocked"))
                    return@post
                }

                val resume = resumeRepository.findByUserId(userId)
                    ?: run {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Create a resume first. POST /resumes")
                        )
                        return@post
                    }

                val request = call.receive<CreateResponseRequest>()

                val vacancy = vacancyRepository.findById(request.vacancyId)
                    ?: run {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Vacancy not found"))
                        return@post
                    }

                if (vacancy.userId == userId) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "You cannot respond to your own vacancy"))
                    return@post
                }

                if (responseRepository.alreadyResponded(userId, request.vacancyId)) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "You have already responded to this vacancy"))
                    return@post
                }

                val id = responseRepository.create(userId, request.vacancyId, resume.id, request.coverLetter)
                call.respond(HttpStatusCode.Created, responseRepository.findById(id)!!)
            }

            get("/my") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                call.respond(HttpStatusCode.OK, responseRepository.getByUser(userId))
            }

            get("/vacancy/{vacancyId}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val role = principal.payload.getClaim("role").asString()

                val vacancyId = call.parameters["vacancyId"]?.toIntOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid vacancy id"))
                        return@get
                    }

                val owner = vacancyRepository.getOwner(vacancyId)
                    ?: run {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Vacancy not found"))
                        return@get
                    }

                if (owner != userId && role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only the vacancy owner can view responses"))
                    return@get
                }

                call.respond(HttpStatusCode.OK, responseRepository.getByVacancy(vacancyId))
            }

            put("/{id}/status") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val role = principal.payload.getClaim("role").asString()

                val responseId = call.parameters["id"]?.toIntOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid response id"))
                        return@put
                    }

                val response = responseRepository.findById(responseId)
                    ?: run {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Response not found"))
                        return@put
                    }

                val vacancyOwner = vacancyRepository.getOwner(response.vacancyId)
                if (vacancyOwner != userId && role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only the vacancy owner can change response status"))
                    return@put
                }

                val request = call.receive<UpdateResponseStatusRequest>()
                if (request.status !in listOf(ResponseStatus.PENDING, ResponseStatus.ACCEPTED, ResponseStatus.REJECTED)) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Status must be PENDING, ACCEPTED or REJECTED"))
                    return@put
                }

                responseRepository.updateStatus(responseId, request.status)
                call.respond(HttpStatusCode.OK, responseRepository.findById(responseId)!!)
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val role = principal.payload.getClaim("role").asString()

                val responseId = call.parameters["id"]?.toIntOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid response id"))
                        return@delete
                    }

                val owner = responseRepository.getOwner(responseId)
                    ?: run {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Response not found"))
                        return@delete
                    }

                if (owner != userId && role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "You can only delete your own responses"))
                    return@delete
                }

                responseRepository.delete(responseId)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Response withdrawn"))
            }
        }
    }
}