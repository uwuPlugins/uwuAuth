package me.yellowbear.uwuauth.models.enums;

public enum AuthResult {
    SUCCESS, //Request successful
    FAILURE, //Unspecified failure (might be needed for security reasons later on)
    INCORRECT_PASSWORD, //Password was incorrect
    USER_NOT_REGISTERED, //User is not yet registered
    MINECRAFT_API_FAILURE, //Failed online auth attempt
    TIME_OUT, //Request timed out
    AUTH_NOT_ALLOWED, //Auth was not allowed by server (e.g. malicious request)
    INVALID_TOKEN
}
