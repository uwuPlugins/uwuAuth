package me.yellowbear.uwuauth.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.ProxyServer;
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

                    Component message = Component.text("uwu", NamedTextColor.AQUA);
                    source.sendMessage(message);

                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("argument", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            proxy.getAllPlayers().forEach(player -> builder.suggest(
                                    player.getUsername(),
                                    VelocityBrigadierMessage.tooltip(
                                            MiniMessage.miniMessage().deserialize("<rainbow>" + player.getUsername())
                                    )
                            ));
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String argumentProvided = ctx.getArgument("argument", String.class);
                            proxy.getPlayer(argumentProvided).ifPresent(player ->
                                    player.sendMessage(Component.text("Hello!"))
                            );
                            // Returning Command.SINGLE_SUCCESS means that the execution was successful
                            // Returning BrigadierCommand.FORWARD will send the command to the server
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();
        return new BrigadierCommand(registerNode);
    }
}