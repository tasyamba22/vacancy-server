package com.vacancy.server.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtHelper {
    fun generateToken(
        userId: Int,
        role: String,
        secret: String,
        issuer: String,
        audience: String,
        expirationMs: Long
    ): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + expirationMs))
            .sign(Algorithm.HMAC256(secret))
    }

    fun generateRefreshToken(
        userId: Int,
        secret: String,
        issuer: String,
        audience: String,
        expirationDays: Long = 30
    ): String {
        val expirationMs = System.currentTimeMillis() + (expirationDays * 24 * 60 * 60 * 1000)

        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("type", "refresh")           // важно отличать
            .withExpiresAt(java.util.Date(expirationMs))
            .sign(Algorithm.HMAC256(secret))
    }
}