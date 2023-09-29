package me.yellowbear.uwuauth.services;

import me.yellowbear.uwuauth.models.AuthRequestResult;
import me.yellowbear.uwuauth.models.enums.AuthResult;
import me.yellowbear.uwuauth.models.enums.StatusCode;
import me.yellowbear.uwuauth.models.requests.LoginRequest;
import me.yellowbear.uwuauth.models.requests.LogoutRequest;
import me.yellowbear.uwuauth.models.requests.RegisterRequest;
import me.yellowbear.uwuauth.models.requests.TokenRefreshRequest;

public class AuthService {
    public static AuthRequestResult loginPlayer(LoginRequest request) {
        return new AuthRequestResult(StatusCode.NOT_IMPLEMENTED, AuthResult.FAILURE);
    }
    public static AuthRequestResult registerPlayer(RegisterRequest request) {
        return new AuthRequestResult(StatusCode.NOT_IMPLEMENTED, AuthResult.FAILURE);
    }
    public static AuthRequestResult logoutPlayer(LogoutRequest request) {
        return new AuthRequestResult(StatusCode.NOT_IMPLEMENTED, AuthResult.FAILURE);
    }
    public static AuthRequestResult refreshToken(TokenRefreshRequest request) {
        return new AuthRequestResult(StatusCode.NOT_IMPLEMENTED, AuthResult.FAILURE);
    }
}
