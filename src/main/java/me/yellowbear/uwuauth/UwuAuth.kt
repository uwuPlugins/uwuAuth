package me.yellowbear.uwuauth;

import com.google.inject.Inject
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer
import me.yellowbear.uwuauth.Services.PlayerEventService
import org.slf4j.Logger;

@Plugin(
        id = "uwuauth",
        name = "uwuAuth",
        version = "1.0-SNAPSHOT",
        description = "",
        authors = ["yellowbear"]
)
class UwuAuth @Inject constructor(logger: Logger, server: ProxyServer) {
    private val logger: Logger
    private val server: ProxyServer
    init {
        this.logger = logger
        this.server = server
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("test")
        server.eventManager.register(this, PlayerEventService())
    }
}