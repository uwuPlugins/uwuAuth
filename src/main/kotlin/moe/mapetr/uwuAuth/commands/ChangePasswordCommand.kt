package moe.mapetr.uwuAuth.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import moe.mapetr.uwuAuth.AuthSessionManager
import moe.mapetr.uwuAuth.Database
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class ChangePasswordCommand(
    private val database: Database,
    private val sessionManager: AuthSessionManager
) : SimpleCommand {

    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()
        if (source !is Player) {
            source.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED))
            return
        }

        if (!sessionManager.isAuthenticated(source.uniqueId)) {
            source.sendMessage(Component.text("You must be logged in to change your password.", NamedTextColor.RED))
            return
        }

        val args = invocation.arguments()
        if (args.size != 3) {
            source.sendMessage(Component.text("Usage: /changepassword <oldPassword> <newPassword> <newPassword>", NamedTextColor.RED))
            return
        }

        val oldPassword = args[0]
        val newPassword = args[1]
        val confirmPassword = args[2]

        if (newPassword != confirmPassword) {
            source.sendMessage(Component.text("New passwords do not match!", NamedTextColor.RED))
            return
        }

        if (newPassword.length < 6) {
            source.sendMessage(Component.text("Password must be at least 6 characters long.", NamedTextColor.RED))
            return
        }

        if (!database.login(source.username, oldPassword)) {
            source.sendMessage(Component.text("Incorrect current password!", NamedTextColor.RED))
            return
        }

        if (database.changePassword(source.username, newPassword)) {
            source.sendMessage(Component.text("Password changed successfully!", NamedTextColor.GREEN))
        } else {
            source.sendMessage(Component.text("Failed to change password.", NamedTextColor.RED))
        }
    }
}
