package moe.mapetr.uwuAuth

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import moe.mapetr.uwuAuth.commands.*
import org.slf4j.Logger
import java.nio.file.Path

@Plugin(
    id = "uwuauth", name = "uwuAuth", version = BuildConstants.VERSION
)
class UwuAuth @Inject constructor(
    val logger: Logger,
    val proxy: ProxyServer,
    @DataDirectory val dataDirectory: Path
) {
    private lateinit var database: Database

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        val dataFolder = dataDirectory.toFile()
        database = Database(dataFolder)
        val sessionManager = AuthSessionManager()
        val config = Config(dataFolder)

        val authListener = AuthListener(database, sessionManager, config, proxy, logger)
        proxy.eventManager.register(this, authListener)

        val commandManager = proxy.commandManager
        commandManager.register(commandManager.metaBuilder("register").build(), RegisterCommand(database, sessionManager, authListener))
        commandManager.register(commandManager.metaBuilder("login").aliases("l").build(), LoginCommand(database, sessionManager, authListener))
        commandManager.register(commandManager.metaBuilder("changepassword").aliases("changepw").build(), ChangePasswordCommand(database, sessionManager))
        commandManager.register(commandManager.metaBuilder("uwuwhitelist").aliases("uwuwl").build(), WhitelistCommand(database, config))
        commandManager.register(commandManager.metaBuilder("uwuauth").build(), AuthCommand(database, config))

        logger.info("uwuAuth has been enabled! Premium cache TTL: ${config.premiumCacheTtlMinutes}m")
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        if (::database.isInitialized) {
            database.close()
        }
        logger.info("uwuAuth has been disabled!")
    }
}
