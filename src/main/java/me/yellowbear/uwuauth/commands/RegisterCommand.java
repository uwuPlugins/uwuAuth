package me.yellowbear.uwuauth.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.yellowbear.uwuauth.models.AuthRequestResult;
import me.yellowbear.uwuauth.models.requests.RegisterRequest;
import me.yellowbear.uwuauth.services.AuthService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class RegisterCommand {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        LiteralCommandNode<CommandSource> registerNode = LiteralArgumentBuilder
                .<CommandSource>literal("register")
                //.requires(source -> source.hasPermission("uwu.auth.register"))
                .executes(ctx -> {
                    CommandSource source = ctx.getSource();

                    Component message = Component.text("/register <password> <password>", NamedTextColor.AQUA);
                    source.sendMessage(message);

                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("passwd1", StringArgumentType.word())
                        .executes(ctx -> {
                            CommandSource source = ctx.getSource();

                            Component message = Component.text("/register <password> <password>", NamedTextColor.AQUA);
                            source.sendMessage(message);

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("passwd2", StringArgumentType.word())
                                .executes(ctx -> {
                                    String argumentProvided = ctx.getArgument("passwd1", String.class);
                                    String argumentProvided2 = ctx.getArgument("passwd2", String.class);

                                    if (argumentProvided.equals(argumentProvided2)) {
                                        Player player = (Player) ctx.getSource();
                                        AuthRequestResult result = AuthService.registerPlayer(new RegisterRequest(
                                                player,
                                                argumentProvided
                                        ));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )

                .build();
        return new BrigadierCommand(registerNode);
    }
}