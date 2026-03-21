package moe.mapetr.uwuAuth

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.slf4j.Logger
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class AuthListener(
    private val database: Database,
    private val sessionManager: AuthSessionManager,
    private val config: Config,
    private val proxy: ProxyServer,
    private val logger: Logger
) {
    private val allowedCommands = setOf("login", "register")
    private val premiumPlayers = ConcurrentHashMap.newKeySet<String>()
    private val loginTimeouts = ConcurrentHashMap<UUID, com.velocitypowered.api.scheduler.ScheduledTask>()

    @Subscribe
    fun onPreLogin(event: PreLoginEvent) {
        val username = event.username

        if (config.whitelistEnabled && !database.isWhitelisted(username)) {
            event.result = PreLoginEvent.PreLoginComponentResult.denied(
                Component.text("You are not whitelisted on this server.", NamedTextColor.RED)
            )
            return
        }

        val forcedMode = database.getForcedMode(username)

        when (forcedMode) {
            ForcedMode.ONLINE -> {
                premiumPlayers.add(username.lowercase())
                event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
                logger.info("$username forced to online-mode by admin")
                return
            }
            ForcedMode.OFFLINE -> {
                premiumPlayers.remove(username.lowercase())
                event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
                logger.info("$username forced to offline-mode by admin")
                return
            }
            ForcedMode.NONE -> { /* fall through to cache/api check */ }
        }

        val cached = database.getCachedPremiumStatus(username)
        val now = System.currentTimeMillis()

        if (cached != null && (now - cached.lastChecked) < config.premiumCacheTtlMs) {
            applyPremiumResult(event, username, cached.isPremium, "cached")
            return
        }

        // Cache miss or stale — query Mojang
        val isPremium = MojangApi.isPremium(username)
        database.updatePremiumCache(username, isPremium)
        applyPremiumResult(event, username, isPremium, "api")
    }

    private fun applyPremiumResult(event: PreLoginEvent, username: String, isPremium: Boolean, source: String) {
        if (isPremium) {
            premiumPlayers.add(username.lowercase())
            event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
            logger.info("$username detected as premium ($source), forcing online-mode")
        } else {
            premiumPlayers.remove(username.lowercase())
            event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
            logger.info("$username detected as cracked ($source), forcing offline-mode")
        }
    }

    @Subscribe
    fun onServerPostConnect(event: ServerPostConnectEvent) {
        val player = event.player

        // Only handle first join (no previous server)
        if (event.previousServer != null) return

        if (premiumPlayers.contains(player.username.lowercase())) {
            sessionManager.authenticate(player.uniqueId)
            player.sendMessage(
                Component.text("Premium account detected. You have been logged in automatically!", NamedTextColor.GREEN)
            )
            return
        }

        // Show title + chat message for cracked players
        if (!database.isRegistered(player.username)) {
            player.showTitle(Title.title(
                Component.text("Register", NamedTextColor.GOLD),
                Component.text("/register <password> <password>", NamedTextColor.YELLOW),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(600), Duration.ZERO)
            ))
            player.sendMessage(
                Component.text("Welcome! Please register with /register <password> <password>", NamedTextColor.GOLD)
            )
        } else {
            player.showTitle(Title.title(
                Component.text("Login", NamedTextColor.GOLD),
                Component.text("/login <password>", NamedTextColor.YELLOW),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(600), Duration.ZERO)
            ))
            player.sendMessage(
                Component.text("Please login with /login <password>", NamedTextColor.GOLD)
            )
        }

        // Start login timeout
        startLoginTimeout(player)
    }

    private fun startLoginTimeout(player: Player) {
        val timeoutSeconds = config.loginTimeoutSeconds
        if (timeoutSeconds <= 0) return

        val task = proxy.scheduler
            .buildTask(proxy.pluginManager.getPlugin("uwuauth").get(), Runnable {
                if (!sessionManager.isAuthenticated(player.uniqueId)) {
                    player.disconnect(
                        Component.text("Login timed out! You have $timeoutSeconds seconds to authenticate.", NamedTextColor.RED)
                    )
                }
            })
            .delay(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .schedule()

        loginTimeouts[player.uniqueId] = task
    }

    fun cancelLoginTimeout(player: Player) {
        loginTimeouts.remove(player.uniqueId)?.cancel()
        // Clear the auth title
        player.clearTitle()
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        val uuid = event.player.uniqueId
        sessionManager.deauthenticate(uuid)
        premiumPlayers.remove(event.player.username.lowercase())
        loginTimeouts.remove(uuid)?.cancel()
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onChat(event: PlayerChatEvent) {
        if (!sessionManager.isAuthenticated(event.player.uniqueId)) {
            event.result = PlayerChatEvent.ChatResult.denied()
            sendAuthReminder(event.player)
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onCommand(event: CommandExecuteEvent) {
        val source = event.commandSource
        if (source !is Player) return

        val command = event.command.split(" ").first().lowercase()
        if (!sessionManager.isAuthenticated(source.uniqueId) && command !in allowedCommands) {
            event.result = CommandExecuteEvent.CommandResult.denied()
            sendAuthReminder(source)
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onServerPreConnect(event: ServerPreConnectEvent) {
        val player = event.player
        if (!sessionManager.isAuthenticated(player.uniqueId) && player.currentServer.isPresent) {
            event.result = ServerPreConnectEvent.ServerResult.denied()
            sendAuthReminder(player)
        }
    }

    fun sendToLobby(player: Player) {
        val serverName = config.lobbyServer
        if (serverName.isBlank()) return
        val server = proxy.getServer(serverName)
        if (server.isPresent) {
            player.createConnectionRequest(server.get()).fireAndForget()
        } else {
            logger.warn("Lobby server '$serverName' not found in Velocity config!")
        }
    }

    private fun sendAuthReminder(player: Player) {
        if (!database.isRegistered(player.username)) {
            player.sendMessage(
                Component.text("Please register first with /register <password> <password>", NamedTextColor.RED)
            )
        } else {
            player.sendMessage(
                Component.text("Please login first with /login <password>", NamedTextColor.RED)
            )
        }
    }
}
