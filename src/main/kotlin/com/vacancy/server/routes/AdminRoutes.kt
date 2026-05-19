package com.vacancy.server.routes

import com.vacancy.server.repositories.UserRepository
import com.vacancy.server.repositories.VacancyRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ChangeRoleRequest(val role: String)

fun Route.adminRoutes(
    userRepository: UserRepository,
    vacancyRepository: VacancyRepository
) {
    authenticate("jwt-auth") {
        route("/admin") {

            intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                    finish()
                    return@intercept
                }
                proceed()
            }

            get("/users") {
                val users = userRepository.getAllUsers()
                call.respond(HttpStatusCode.OK, users)
            }

            put("/users/{id}/block") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))
                        return@put
                    }
                val success = userRepository.blockUser(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User blocked"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                }
            }

            put("/users/{id}/unblock") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))
                        return@put
                    }
                val success = userRepository.unblockUser(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User unblocked"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                }
            }

            put("/users/{id}/role") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))
                        return@put
                    }
                val request = call.receive<ChangeRoleRequest>()
                if (request.role !in listOf("USER", "ADMIN")) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Role must be USER or ADMIN"))
                    return@put
                }
                val success = userRepository.changeRole(id, request.role)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Role changed to ${request.role}"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                }
            }

            delete("/vacancies/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid vacancy id"))
                        return@delete
                    }
                val success = vacancyRepository.delete(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Vacancy deleted by admin"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Vacancy not found"))
                }
            }

            get("/stats") {
                val usersCount = userRepository.countUsers()
                val vacanciesCount = vacancyRepository.countVacancies()
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "totalUsers" to usersCount,
                        "totalVacancies" to vacanciesCount
                    )
                )
            }
        }
    }
}
