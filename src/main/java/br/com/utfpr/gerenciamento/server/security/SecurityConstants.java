package br.com.utfpr.gerenciamento.server.security;

public class SecurityConstants {

    public static final String SECRET = "L@bOr@T0r!o";
    public static final long EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
}
