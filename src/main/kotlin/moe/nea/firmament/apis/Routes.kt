

package moe.nea.firmament.apis

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import java.util.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.MutableMap
import kotlin.collections.listOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.MinecraftDispatcher

object Routes {
    private val nameToUUID: MutableMap<String, Deferred<UUID?>> = CaseInsensitiveMap()
    private val profiles: MutableMap<UUID, Deferred<Profiles?>> = mutableMapOf()
    private val accounts: MutableMap<UUID, Deferred<PlayerData?>> = mutableMapOf()
    private val UUIDToName: MutableMap<UUID, Deferred<String?>> = mutableMapOf()

    suspend fun getPlayerNameForUUID(uuid: UUID): String? {
        return withContext(MinecraftDispatcher) {
            UUIDToName.computeIfAbsent(uuid) {
                async(Firmament.coroutineScope.coroutineContext) {
                    val response = Firmament.httpClient.get("https://api.ashcon.app/mojang/v2/user/$uuid")
                    if (!response.status.isSuccess()) return@async null
                    val data = response.body<AshconNameLookup>()
                    launch(MinecraftDispatcher) {
                        nameToUUID[data.username] = async { data.uuid }
                    }
                    data.username
                }
            }
        }.await()
    }

    suspend fun getUUIDForPlayerName(name: String): UUID? {
        return withContext(MinecraftDispatcher) {
            nameToUUID.computeIfAbsent(name) {
                async(Firmament.coroutineScope.coroutineContext) {
                    val response = Firmament.httpClient.get("https://api.ashcon.app/mojang/v2/user/$name")
                    if (!response.status.isSuccess()) return@async null
                    val data = response.body<AshconNameLookup>()
                    launch(MinecraftDispatcher) {
                        UUIDToName[data.uuid] = async { data.username }
                    }
                    data.uuid
                }
            }
        }.await()
    }

    suspend fun getAccountData(uuid: UUID): PlayerData? {
        return withContext(MinecraftDispatcher) {
            accounts.computeIfAbsent(uuid) {
                async(Firmament.coroutineScope.coroutineContext) {
                    val response = UrsaManager.request(listOf("v1", "hypixel","player", uuid.toString()))
                    if (!response.status.isSuccess()) {
                        launch(MinecraftDispatcher) {
                            @Suppress("DeferredResultUnused")
                            accounts.remove(uuid)
                        }
                        return@async null
                    }
                    response.body<PlayerResponse>().player
                }
            }
        }.await()
    }

    suspend fun getProfiles(uuid: UUID): Profiles? {
        return withContext(MinecraftDispatcher) {
            profiles.computeIfAbsent(uuid) {
                async(Firmament.coroutineScope.coroutineContext) {
                    val response = UrsaManager.request(listOf("v1", "hypixel","profiles", uuid.toString()))
                    if (!response.status.isSuccess()) {
                        launch(MinecraftDispatcher) {
                            @Suppress("DeferredResultUnused")
                            profiles.remove(uuid)
                        }
                        return@async null
                    }
                    response.body<Profiles>()
                }
            }
        }.await()
    }

}
