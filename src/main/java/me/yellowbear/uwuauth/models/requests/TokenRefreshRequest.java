package me.yellowbear.uwuauth.models.requests;

import com.velocitypowered.api.proxy.Player;
import me.yellowbear.uwuauth.models.enums.RequestType;

public class TokenRefreshRequest {
    public RequestType type;
    public String token;
    public Player player;
    public TokenRefreshRequest(Player player, String token) {
        this.type = RequestType.TOKEN_REFRESH;
        this.player = player;
        this.token = token;
    }
}
