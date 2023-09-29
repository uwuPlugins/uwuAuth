package me.yellowbear.uwuauth.models.requests;

import com.velocitypowered.api.proxy.Player;
import me.yellowbear.uwuauth.models.enums.RequestType;
public class RegisterRequest {
    public RequestType type;
    public String password;
    public Player player;
    public RegisterRequest(Player player, String password) {
        this.type = RequestType.REGISTER;
        this.password = password;
        this.player = player;
    }
}
