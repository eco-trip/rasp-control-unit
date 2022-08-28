package io.github.ecotrip.token;

import java.util.Objects;

/**
 * Represents the authorization token
 */
public class Token {
    private final String value;

    private Token(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Token token = (Token) o;
        return getValue().equals(token.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

    public static Token of(final String value) {
        return new Token(value);
    }

    @Override
    public String toString() {
        return "Token{"
                + "value='" + value + '\''
                + '}';
    }
}
