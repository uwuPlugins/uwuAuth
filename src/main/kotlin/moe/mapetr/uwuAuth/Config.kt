package moe.mapetr.uwuAuth

import java.io.File
import java.util.Properties

class Config(dataFolder: File) {
    private val configFile = File(dataFolder, "config.properties")
    private val properties = Properties()

    var whitelistEnabled: Boolean = false
        private set

    /** How long (in minutes) before a cached premium status is rechecked. */
    var premiumCacheTtlMinutes: Long = 60
        private set

    /** Server name in velocity.toml where authenticated players are sent (empty = don't move). */
    var lobbyServer: String = ""
        private set

    /** Seconds before unauthenticated players are kicked (0 = no timeout). */
    var loginTimeoutSeconds: Int = 60
        private set

    init {
        dataFolder.mkdirs()
        if (configFile.exists()) {
            configFile.inputStream().use { properties.load(it) }
        } else {
            properties.setProperty("whitelist-enabled", "false")
            properties.setProperty("premium-cache-ttl-minutes", "60")
            properties.setProperty("lobby-server", "")
            properties.setProperty("login-timeout-seconds", "60")
            save()
        }
        whitelistEnabled = properties.getProperty("whitelist-enabled", "false").toBoolean()
        premiumCacheTtlMinutes = properties.getProperty("premium-cache-ttl-minutes", "60").toLongOrNull() ?: 60
        lobbyServer = properties.getProperty("lobby-server", "")
        loginTimeoutSeconds = properties.getProperty("login-timeout-seconds", "60").toIntOrNull() ?: 60
    }

    fun setWhitelistEnabled(enabled: Boolean) {
        whitelistEnabled = enabled
        properties.setProperty("whitelist-enabled", enabled.toString())
        save()
    }

    fun setPremiumCacheTtlMinutes(minutes: Long) {
        premiumCacheTtlMinutes = minutes
        properties.setProperty("premium-cache-ttl-minutes", minutes.toString())
        save()
    }

    /** Cache TTL in milliseconds. */
    val premiumCacheTtlMs: Long get() = premiumCacheTtlMinutes * 60 * 1000

    private fun save() {
        configFile.outputStream().use { properties.store(it, "uwuAuth Configuration") }
    }
}
