package io.github.ecotrip.token;

import java.util.Optional;

/**
 * Represents a singleton to manage the current Token
 */
public class TokenService {
    private Token token;

    public synchronized void update(final Token token) {
        this.token = token;
    }

    public synchronized void disable() {
        this.token = null;
    }

    public Optional<Token> getActiveToken() {
        return Optional.ofNullable(token);
    }

    public static TokenService of() {
        return new TokenService();
    }
}
