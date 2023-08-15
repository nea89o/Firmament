/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.apis

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import net.minecraft.client.MinecraftClient
import moe.nea.firmament.Firmament

object UrsaManager {
    private data class Token(
        val validUntil: Instant,
        val token: String,
        val obtainedFrom: String,
    ) {
        fun isValid(host: String) = Instant.now().plusSeconds(60) < validUntil && obtainedFrom == host
    }

    private var currentToken: Token? = null
    private val lock = Mutex()
    private fun getToken(host: String) = currentToken?.takeIf { it.isValid(host) }

    suspend fun request(path: List<String>): HttpResponse {
        var didLock = false
        try {
            val host = "ursa.notenoughupdates.org"
            var token = getToken(host)
            if (token == null) {
                lock.lock()
                didLock = true
                token = getToken(host)
            }
            val response = Firmament.httpClient.get {
                url {
                    this.host = host
                    appendPathSegments(path, encodeSlash = true)
                }
                if (token == null) {
                    withContext(Dispatchers.IO) {
                        val mc = MinecraftClient.getInstance()
                        val serverId = UUID.randomUUID().toString()
                        mc.sessionService.joinServer(mc.session.profile, mc.session.accessToken, serverId)
                        header("x-ursa-username", mc.session.profile.name)
                        header("x-ursa-serverid", serverId)
                    }
                } else {
                    header("x-ursa-token", token.token)
                }
            }
            val savedToken = response.headers["x-ursa-token"]
            if (savedToken != null) {
                val validUntil = response.headers["x-ursa-expires"]?.toLongOrNull()?.let { Instant.ofEpochMilli(it) }
                    ?: (Instant.now() + Duration.ofMinutes(55))
                currentToken = Token(validUntil, savedToken, host)
            }
            return response
        } finally {
            if (didLock)
                lock.unlock()
        }
    }
}
