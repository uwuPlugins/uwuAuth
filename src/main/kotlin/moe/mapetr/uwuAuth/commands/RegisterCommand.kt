package moe.mapetr.uwuAuth.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import moe.mapetr.uwuAuth.AuthListener
import moe.mapetr.uwuAuth.AuthSessionManager
import moe.mapetr.uwuAuth.Database
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class RegisterCommand(
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
        if (args.size != 2) {
            source.sendMessage(Component.text("Usage: /register <password> <password>", NamedTextColor.RED))
            return
        }

        val password = args[0]
        val confirmPassword = args[1]

        if (password != confirmPassword) {
            source.sendMessage(Component.text("Passwords do not match!", NamedTextColor.RED))
            return
        }

        if (password.length < 6) {
            source.sendMessage(Component.text("Password must be at least 6 characters long.", NamedTextColor.RED))
            return
        }

        if (database.isRegistered(source.username)) {
            source.sendMessage(Component.text("You are already registered! Use /login <password>", NamedTextColor.RED))
            return
        }

        if (database.register(source.username, password)) {
            sessionManager.authenticate(source.uniqueId)
            authListener.cancelLoginTimeout(source)
            source.sendMessage(Component.text("Successfully registered and logged in!", NamedTextColor.GREEN))
            authListener.sendToLobby(source)
        } else {
            source.sendMessage(Component.text("Registration failed. Please try again.", NamedTextColor.RED))
        }
    }
}
