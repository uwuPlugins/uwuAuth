package moe.mapetr.uwuAuth.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import moe.mapetr.uwuAuth.AuthListener
import moe.mapetr.uwuAuth.AuthSessionManager
import moe.mapetr.uwuAuth.Database
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class LoginCommand(
    private val database: Database,
    private val sessionManager: AuthSessionManager,
    private val authListener: AuthListener
) : SimpleCommand {

    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()
        if (source !is Player) {
            source.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED))
            return
        }

        val args = invocation.arguments()
        if (args.size != 1) {
            source.sendMessage(Component.text("Usage: /login <password>", NamedTextColor.RED))
            return
        }

        if (sessionManager.isAuthenticated(source.uniqueId)) {
            source.sendMessage(Component.text("You are already logged in!", NamedTextColor.YELLOW))
            return
        }

        if (!database.isRegistered(source.username)) {
            source.sendMessage(Component.text("You are not registered! Use /register <password> <password>", NamedTextColor.RED))
            return
        }

        if (database.login(source.username, args[0])) {
            sessionManager.authenticate(source.uniqueId)
            authListener.cancelLoginTimeout(source)
            source.sendMessage(Component.text("Successfully logged in!", NamedTextColor.GREEN))
            authListener.sendToLobby(source)
        } else {
            source.sendMessage(Component.text("Incorrect password!", NamedTextColor.RED))
        }
    }
}
