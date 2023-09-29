package me.yellowbear.uwuauth.models;

import me.yellowbear.uwuauth.models.enums.AuthResult;
import me.yellowbear.uwuauth.models.enums.StatusCode;

public class AuthRequestResult {
    public StatusCode statusCode;
    public AuthResult authResult;
    public String token;
    public AuthRequestResult(StatusCode statusCode, AuthResult result, String token) {
        this.authResult = result;
        this.token = token;
        this.statusCode = statusCode;
    }

    public AuthRequestResult(StatusCode statusCode, AuthResult result) {
        this.authResult = result;
        this.token = null;
        this.statusCode = statusCode;
    }
}
