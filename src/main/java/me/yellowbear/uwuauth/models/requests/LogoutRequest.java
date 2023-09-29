package me.yellowbear.uwuauth.models.requests;

import com.velocitypowered.api.proxy.Player;
import me.yellowbear.uwuauth.models.enums.RequestType;

public class LogoutRequest {
    public RequestType type;
    public Player player;
    public LogoutRequest(Player player) {
        this.type = RequestType.LOGOUT;
        this.player = player;
    }
}
