package moe.mapetr.uwuAuth

import de.mkammerer.argon2.Argon2Factory
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

enum class ForcedMode { NONE, ONLINE, OFFLINE }

data class PremiumCacheEntry(
    val username: String,
    val isPremium: Boolean,
    val forcedMode: ForcedMode,
    val lastChecked: Long
)

class Database(dataFolder: File) {
    private val connection: Connection
    private val argon2 = Argon2Factory.create()

    init {
        dataFolder.mkdirs()
        Class.forName("org.sqlite.JDBC")
        val dbFile = File(dataFolder, "uwuauth.db")
        connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
        createTables()
    }

    private fun createTables() {
        connection.createStatement().use { stmt ->
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                    username TEXT PRIMARY KEY COLLATE NOCASE,
                    password_hash TEXT NOT NULL
                )
            """)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS whitelist (
                    username TEXT PRIMARY KEY COLLATE NOCASE
                )
            """)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS premium_cache (
                    username TEXT PRIMARY KEY COLLATE NOCASE,
                    is_premium INTEGER NOT NULL DEFAULT 0,
                    forced_mode TEXT NOT NULL DEFAULT 'NONE',
                    last_checked INTEGER NOT NULL
                )
            """)
        }
    }

    // --- Player auth ---

    fun isRegistered(username: String): Boolean {
        connection.prepareStatement("SELECT 1 FROM players WHERE username = ?").use { stmt ->
            stmt.setString(1, username.lowercase())
            return stmt.executeQuery().next()
        }
    }

    fun register(username: String, password: String): Boolean {
        if (isRegistered(username)) return false
        val hash = argon2.hash(3, 65536, 1, password.toCharArray())
        connection.prepareStatement("INSERT INTO players (username, password_hash) VALUES (?, ?)").use { stmt ->
            stmt.setString(1, username.lowercase())
            stmt.setString(2, hash)
            stmt.executeUpdate()
        }
        return true
    }

    fun login(username: String, password: String): Boolean {
        connection.prepareStatement("SELECT password_hash FROM players WHERE username = ?").use { stmt ->
            stmt.setString(1, username.lowercase())
            val rs = stmt.executeQuery()
            if (!rs.next()) return false
            val hash = rs.getString("password_hash")
            return argon2.verify(hash, password.toCharArray())
        }
    }

    fun changePassword(username: String, password: String): Boolean {
        if (!isRegistered(username)) return false
        val hash = argon2.hash(3, 65536, 1, password.toCharArray())
        connection.prepareStatement("UPDATE players SET password_hash = ? WHERE username = ?").use { stmt ->
            stmt.setString(1, hash)
            stmt.setString(2, username.lowercase())
            stmt.executeUpdate()
        }
        return true
    }

    // --- Whitelist ---

    fun isWhitelisted(username: String): Boolean {
        connection.prepareStatement("SELECT 1 FROM whitelist WHERE username = ?").use { stmt ->
            stmt.setString(1, username.lowercase())
            return stmt.executeQuery().next()
        }
    }

    fun addToWhitelist(username: String): Boolean {
        if (isWhitelisted(username)) return false
        connection.prepareStatement("INSERT INTO whitelist (username) VALUES (?)").use { stmt ->
            stmt.setString(1, username.lowercase())
            stmt.executeUpdate()
        }
        return true
    }

    fun removeFromWhitelist(username: String): Boolean {
        if (!isWhitelisted(username)) return false
        connection.prepareStatement("DELETE FROM whitelist WHERE username = ?").use { stmt ->
            stmt.setString(1, username.lowercase())
            stmt.executeUpdate()
        }
        return true
    }

    fun getWhitelistedPlayers(): List<String> {
        val players = mutableListOf<String>()
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT username FROM whitelist ORDER BY username")
            while (rs.next()) {
                players.add(rs.getString("username"))
            }
        }
        return players
    }

    // --- Premium cache ---

    fun getCachedPremiumStatus(username: String): PremiumCacheEntry? {
        connection.prepareStatement("SELECT username, is_premium, forced_mode, last_checked FROM premium_cache WHERE username = ?").use { stmt ->
            stmt.setString(1, username.lowercase())
            val rs = stmt.executeQuery()
            if (!rs.next()) return null
            return PremiumCacheEntry(
                username = rs.getString("username"),
                isPremium = rs.getInt("is_premium") == 1,
                forcedMode = try { ForcedMode.valueOf(rs.getString("forced_mode")) } catch (_: Exception) { ForcedMode.NONE },
                lastChecked = rs.getLong("last_checked")
            )
        }
    }

    fun updatePremiumCache(username: String, isPremium: Boolean) {
        val existing = getCachedPremiumStatus(username)
        if (existing == null) {
            connection.prepareStatement("INSERT INTO premium_cache (username, is_premium, forced_mode, last_checked) VALUES (?, ?, 'NONE', ?)").use { stmt ->
                stmt.setString(1, username.lowercase())
                stmt.setInt(2, if (isPremium) 1 else 0)
                stmt.setLong(3, System.currentTimeMillis())
                stmt.executeUpdate()
            }
        } else {
            connection.prepareStatement("UPDATE premium_cache SET is_premium = ?, last_checked = ? WHERE username = ?").use { stmt ->
                stmt.setInt(1, if (isPremium) 1 else 0)
                stmt.setLong(2, System.currentTimeMillis())
                stmt.setString(3, username.lowercase())
                stmt.executeUpdate()
            }
        }
    }

    fun setForcedMode(username: String, mode: ForcedMode) {
        val existing = getCachedPremiumStatus(username)
        if (existing == null) {
            connection.prepareStatement("INSERT INTO premium_cache (username, is_premium, forced_mode, last_checked) VALUES (?, 0, ?, ?)").use { stmt ->
                stmt.setString(1, username.lowercase())
                stmt.setString(2, mode.name)
                stmt.setLong(3, System.currentTimeMillis())
                stmt.executeUpdate()
            }
        } else {
            connection.prepareStatement("UPDATE premium_cache SET forced_mode = ? WHERE username = ?").use { stmt ->
                stmt.setString(1, mode.name)
                stmt.setString(2, username.lowercase())
                stmt.executeUpdate()
            }
        }
    }

    fun getForcedMode(username: String): ForcedMode {
        return getCachedPremiumStatus(username)?.forcedMode ?: ForcedMode.NONE
    }

    fun close() {
        if (!connection.isClosed) connection.close()
    }
}
