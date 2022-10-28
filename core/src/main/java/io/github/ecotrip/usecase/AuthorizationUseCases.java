package io.github.ecotrip.usecase;

import java.util.concurrent.CompletableFuture;

import io.github.ecotrip.adapter.InputAdapter;
import io.github.ecotrip.adapter.NfcAdapter;
import io.github.ecotrip.token.Token;
import io.github.ecotrip.token.TokenService;

/**
 * Contains all the use cases related to the user's authorization/binding by token.
 */
public class AuthorizationUseCases {
    private final InputAdapter inputAdapter;
    private final NfcAdapter nfcAdapter;
    private final TokenService tokenService;

    private AuthorizationUseCases(final InputAdapter inputAdapter, final NfcAdapter nfcAdapter) {
        this.inputAdapter = inputAdapter;
        this.nfcAdapter = nfcAdapter;
        tokenService = TokenService.of();
    }

    public CompletableFuture<Void> bootstrap() {
        return inputAdapter.requireToken();
    }

    public CompletableFuture<Void> waitNearbyDevice() {
        return nfcAdapter.initTagAndWaitForNearbyDevice();
    }

    public CompletableFuture<Void> deactivateToken() {
        return CompletableFuture.runAsync(tokenService::disable);
    }

    public CompletableFuture<Void> activateToken(final Token token) {
        return CompletableFuture.runAsync(() -> tokenService.update(token));
    }

    /**
     * method to send the current token to the sensor.
     * @return a done future
     */
    public CompletableFuture<Void> transmitToken() {
        var token = tokenService.getActiveToken();
        return token.isPresent() ? nfcAdapter.transmit(token.get())
            .thenCompose(u -> nfcAdapter.reset()) : nfcAdapter.reset();
    }

    public static AuthorizationUseCases of(final InputAdapter adapter, final NfcAdapter nfcAdapter) {
        return new AuthorizationUseCases(adapter, nfcAdapter);
    }
}
