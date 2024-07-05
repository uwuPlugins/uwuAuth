package me.yellowbear.uwuauth.Services

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
class PlayerEventService {
    @Subscribe
    fun onCommand(event: CommandExecuteEvent) {
        println(event.command.length)
    }
}