package com.montes.url_shortener.infrastructure.user;

public class UserConstants {
    public static final String JWT_SECRET = "replace_this_with_a_real_secret_key";
    public static final long JWT_EXPIRATION_MS = 86400000; // 1 day
    
    // URL validation constants
    public static final String URL_REGEX = "^(https?://)?(www\\.)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(/.*)?$";
    public static final int MAX_URL_LENGTH = 2048;
    public static final int SHORT_CODE_LENGTH = 8;
}
