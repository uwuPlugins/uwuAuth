package me.yellowbear.uwuauth;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import me.yellowbear.uwuauth.commands.RegisterCommand;
import me.yellowbear.uwuauth.models.enums.StatusCode;
import org.slf4j.Logger;

import java.util.Map;

@Plugin(
        id = "uwuauth",
        name = "uwuAuth",
        version = "1.0-SNAPSHOT",
        authors = {"yellowbear"}
)
public final class UwuAuth {
    private final ProxyServer _proxy;

    @Inject
    public UwuAuth(ProxyServer proxy) {
        this._proxy = proxy;
    }

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = _proxy.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("register")
                .aliases("reg")
                .plugin(this)
                .build();
        BrigadierCommand registerCommand = RegisterCommand.createBrigadierCommand(_proxy);

        commandManager.register(commandMeta, registerCommand);
    }

    private StatusCode registerCommands() {
        return StatusCode.NOT_IMPLEMENTED;
    }
}