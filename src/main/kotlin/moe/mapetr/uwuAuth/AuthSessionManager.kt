package moe.mapetr.uwuAuth

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class AuthSessionManager {
    private val authenticatedPlayers = ConcurrentHashMap.newKeySet<UUID>()

    fun isAuthenticated(uuid: UUID): Boolean = uuid in authenticatedPlayers

    fun authenticate(uuid: UUID) {
        authenticatedPlayers.add(uuid)
    }

    fun deauthenticate(uuid: UUID) {
        authenticatedPlayers.remove(uuid)
    }
}
