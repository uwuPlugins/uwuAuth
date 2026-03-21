package moe.mapetr.uwuAuth.commands

import com.velocitypowered.api.command.SimpleCommand
import moe.mapetr.uwuAuth.Config
import moe.mapetr.uwuAuth.Database
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class WhitelistCommand(
    private val database: Database,
    private val config: Config
) : SimpleCommand {

    override fun execute(invocation: SimpleCommand.Invocation) {
        val args = invocation.arguments()
        if (args.isEmpty()) {
            sendUsage(invocation)
            return
        }

        when (args[0].lowercase()) {
            "on" -> {
                config.setWhitelistEnabled(true)
                invocation.source().sendMessage(
                    Component.text("Whitelist enabled.", NamedTextColor.GREEN)
                )
            }
            "off" -> {
                config.setWhitelistEnabled(false)
                invocation.source().sendMessage(
                    Component.text("Whitelist disabled.", NamedTextColor.GREEN)
                )
            }
            "add" -> {
                if (args.size < 2) {
                    invocation.source().sendMessage(
                        Component.text("Usage: /uwuwhitelist add <username>", NamedTextColor.RED)
                    )
                    return
                }
                val username = args[1]
                if (database.addToWhitelist(username)) {
                    invocation.source().sendMessage(
                        Component.text("Added $username to the whitelist.", NamedTextColor.GREEN)
                    )
                } else {
                    invocation.source().sendMessage(
                        Component.text("$username is already whitelisted.", NamedTextColor.YELLOW)
                    )
                }
            }
            "remove" -> {
                if (args.size < 2) {
                    invocation.source().sendMessage(
                        Component.text("Usage: /uwuwhitelist remove <username>", NamedTextColor.RED)
                    )
                    return
                }
                val username = args[1]
                if (database.removeFromWhitelist(username)) {
                    invocation.source().sendMessage(
                        Component.text("Removed $username from the whitelist.", NamedTextColor.GREEN)
                    )
                } else {
                    invocation.source().sendMessage(
                        Component.text("$username is not whitelisted.", NamedTextColor.YELLOW)
                    )
                }
            }
            "list" -> {
                val players = database.getWhitelistedPlayers()
                if (players.isEmpty()) {
                    invocation.source().sendMessage(
                        Component.text("The whitelist is empty.", NamedTextColor.YELLOW)
                    )
                } else {
                    invocation.source().sendMessage(
                        Component.text("Whitelisted players (${players.size}): ${players.joinToString(", ")}", NamedTextColor.GREEN)
                    )
                }
            }
            "status" -> {
                val status = if (config.whitelistEnabled) "enabled" else "disabled"
                invocation.source().sendMessage(
                    Component.text("Whitelist is currently $status.", NamedTextColor.GOLD)
                )
            }
            else -> sendUsage(invocation)
        }
    }

    override fun suggest(invocation: SimpleCommand.Invocation): List<String> {
        val args = invocation.arguments()
        return when {
            args.size <= 1 -> listOf("on", "off", "add", "remove", "list", "status")
                .filter { it.startsWith(args.firstOrNull()?.lowercase() ?: "") }
            args.size == 2 && args[0].lowercase() == "remove" -> {
                database.getWhitelistedPlayers().filter { it.startsWith(args[1], ignoreCase = true) }
            }
            else -> emptyList()
        }
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("uwuauth.whitelist")
    }

    private fun sendUsage(invocation: SimpleCommand.Invocation) {
        invocation.source().sendMessage(
            Component.text("Usage: /uwuwhitelist <on|off|add|remove|list|status>", NamedTextColor.RED)
        )
    }
}
