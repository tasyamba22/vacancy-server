package com.vacancy.server.routes

import com.vacancy.server.models.CreateResumeRequest
import com.vacancy.server.models.UpdateResumeRequest
import com.vacancy.server.repositories.ResumeRepository
import com.vacancy.server.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.resumeRoutes(
    resumeRepository: ResumeRepository,
    userRepository: UserRepository
) {
    authenticate("jwt-auth") {
        route("/resumes") {

            get {
                val principal = call.principal<JWTPrincipal>()!!
                val role = principal.payload.getClaim("role").asString()
                if (role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                    return@get
                }
                call.respond(HttpStatusCode.OK, resumeRepository.getAll())
            }

            post {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()

                val user = userRepository.findById(userId)
                if (user?.isBlocked == true) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Your account is blocked"))
                    return@post
                }
                if (resumeRepository.existsForUser(userId)) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "You already have a resume. Use PUT to update it"))
                    return@post
                }

                val request = call.receive<CreateResumeRequest>()
                if (request.fullName.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Full name is required"))
                    return@post
                }

                val id = resumeRepository.create(userId, request.fullName, request.phone, request.skills, request.experience, request.education)
                call.respond(HttpStatusCode.Created, resumeRepository.findById(id)!!)
            }

            get("/my") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val resume = resumeRepository.findByUserId(userId)
                    ?: run {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "You don't have a resume yet"))
                        return@get
                    }
                call.respond(HttpStatusCode.OK, resume)
            }

            put {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                if (!resumeRepository.existsForUser(userId)) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Create a resume first. Use POST /resumes"))
                    return@put
                }
                val request = call.receive<UpdateResumeRequest>()
                resumeRepository.update(userId, request.fullName, request.phone, request.skills, request.experience, request.education)
                call.respond(HttpStatusCode.OK, resumeRepository.findByUserId(userId)!!)
            }

            delete {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                if (resumeRepository.delete(userId)) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Resume deleted"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Resume not found"))
                }
            }

            get("/user/{userId}") {
                val targetUserId = call.parameters["userId"]?.toIntOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))
                        return@get
                    }
                val resume = resumeRepository.findByUserId(targetUserId)
                    ?: run {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Resume not found"))
                        return@get
                    }
                call.respond(HttpStatusCode.OK, resume)
            }
        }
    }
}