package br.com.utfpr.gerenciamento.server.security;

import org.springframework.beans.factory.annotation.Value;

public class SecurityConstants {

    @Value("${utfpr.token.secret}")
    public static String SECRET;
    public static final long EXPIRATION_TIME = 86400000; // 1 dia
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    @Value("${utfpr.token.secret}")
    public void setNameStatic(String secret){
        SecurityConstants.SECRET = secret;
    }
}
