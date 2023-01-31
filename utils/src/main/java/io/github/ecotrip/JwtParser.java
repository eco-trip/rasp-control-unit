package io.github.ecotrip;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.json.JSONObject;

/**
 * JwtParser is a utility class for parsing JSON Web Tokens (JWT).
 */
public class JwtParser {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    /**
     * Extracts the payload from a JWT token and returns it as a JSONObject.
     *
     * @param token the JWT token to parse
     * @return the payload of the token, as a JSONObject
     */
    public static JSONObject getPayload(final String token) {
        String[] parts = token.split("\\.");
        var payload = new String(Base64.getUrlDecoder().decode(parts[1]), UTF_8);
        return new JSONObject(payload);
    }
}
