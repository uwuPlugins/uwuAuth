package me.yellowbear.uwuauth.models.requests;

import com.velocitypowered.api.proxy.Player;
import me.yellowbear.uwuauth.models.enums.RequestType;

public class LoginRequest {
    public RequestType type;
    public String password;
    public Player player;
    public LoginRequest(Player player, String password) {
        this.type = RequestType.LOGIN;
        this.password = password;
        this.player = player;
    }
}