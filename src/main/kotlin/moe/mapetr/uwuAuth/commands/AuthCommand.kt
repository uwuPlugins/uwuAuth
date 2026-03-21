package moe.mapetr.uwuAuth.commands

import com.velocitypowered.api.command.SimpleCommand
import moe.mapetr.uwuAuth.Config
import moe.mapetr.uwuAuth.Database
import moe.mapetr.uwuAuth.ForcedMode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class AuthCommand(
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
            "force" -> handleForce(invocation, args)
            "status" -> handleStatus(invocation, args)
            "cachettl" -> handleCacheTtl(invocation, args)
            else -> sendUsage(invocation)
        }
    }

    private fun handleForce(invocation: SimpleCommand.Invocation, args: Array<String>) {
        if (args.size < 3) {
            invocation.source().sendMessage(
                Component.text("Usage: /uwuauth force <player> <online|offline|reset>", NamedTextColor.RED)
            )
            return
        }

        val username = args[1]
        val mode = when (args[2].lowercase()) {
            "online" -> ForcedMode.ONLINE
            "offline" -> ForcedMode.OFFLINE
            "reset" -> ForcedMode.NONE
            else -> {
                invocation.source().sendMessage(
                    Component.text("Invalid mode. Use: online, offline, or reset", NamedTextColor.RED)
                )
                return
            }
        }

        database.setForcedMode(username, mode)

        val message = when (mode) {
            ForcedMode.ONLINE -> "Forced $username to online-mode (premium). Takes effect on next join."
            ForcedMode.OFFLINE -> "Forced $username to offline-mode (cracked). Takes effect on next join."
            ForcedMode.NONE -> "Reset $username to automatic detection. Takes effect on next join."
        }

        invocation.source().sendMessage(Component.text(message, NamedTextColor.GREEN))
    }

    private fun handleStatus(invocation: SimpleCommand.Invocation, args: Array<String>) {
        if (args.size < 2) {
            invocation.source().sendMessage(
                Component.text("Usage: /uwuauth status <player>", NamedTextColor.RED)
            )
            return
        }

        val username = args[1]
        val cached = database.getCachedPremiumStatus(username)

        if (cached == null) {
            invocation.source().sendMessage(
                Component.text("No cached data for $username.", NamedTextColor.YELLOW)
            )
            return
        }

        val premiumStr = if (cached.isPremium) "premium" else "cracked"
        val forcedStr = when (cached.forcedMode) {
            ForcedMode.NONE -> "auto"
            ForcedMode.ONLINE -> "forced online"
            ForcedMode.OFFLINE -> "forced offline"
        }
        val ageMinutes = (System.currentTimeMillis() - cached.lastChecked) / 60000
        val stale = if (ageMinutes >= config.premiumCacheTtlMinutes) " (stale)" else ""

        invocation.source().sendMessage(
            Component.text("$username: $premiumStr, mode=$forcedStr, checked ${ageMinutes}m ago$stale", NamedTextColor.GOLD)
        )
    }

    private fun handleCacheTtl(invocation: SimpleCommand.Invocation, args: Array<String>) {
        if (args.size < 2) {
            invocation.source().sendMessage(
                Component.text("Current cache TTL: ${config.premiumCacheTtlMinutes} minutes", NamedTextColor.GOLD)
            )
            return
        }

        val minutes = args[1].toLongOrNull()
        if (minutes == null || minutes < 1) {
            invocation.source().sendMessage(
                Component.text("TTL must be a positive number of minutes.", NamedTextColor.RED)
            )
            return
        }

        config.setPremiumCacheTtlMinutes(minutes)
        invocation.source().sendMessage(
            Component.text("Premium cache TTL set to $minutes minutes.", NamedTextColor.GREEN)
        )
    }

    override fun suggest(invocation: SimpleCommand.Invocation): List<String> {
        val args = invocation.arguments()
        return when {
            args.size <= 1 -> listOf("force", "status", "cachettl")
                .filter { it.startsWith(args.firstOrNull()?.lowercase() ?: "") }
            args.size == 3 && args[0].lowercase() == "force" ->
                listOf("online", "offline", "reset")
                    .filter { it.startsWith(args[2].lowercase()) }
            else -> emptyList()
        }
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("uwuauth.admin")
    }

    private fun sendUsage(invocation: SimpleCommand.Invocation) {
        invocation.source().sendMessage(Component.text("Usage:", NamedTextColor.RED))
        invocation.source().sendMessage(Component.text("  /uwuauth force <player> <online|offline|reset>", NamedTextColor.RED))
        invocation.source().sendMessage(Component.text("  /uwuauth status <player>", NamedTextColor.RED))
        invocation.source().sendMessage(Component.text("  /uwuauth cachettl [minutes]", NamedTextColor.RED))
    }
}
