package io.github.ecotrip;

import java.util.concurrent.CompletableFuture;

import io.github.ecotrip.execution.engine.Engine;
import io.github.ecotrip.pattern.Observer;
import io.github.ecotrip.token.Token;
import io.github.ecotrip.usecase.AuthorizationUseCases;

/**
 * Contains the logic to receive and transmit a token to an NFC device
 */
public class AuthorizationService implements Observer<Token> {
    private final AuthorizationUseCases authorizationUseCases;
    private final Engine engine;

    private AuthorizationService(final Engine engine, final AuthorizationUseCases authorizationUseCases) {
        this.authorizationUseCases = authorizationUseCases;
        this.engine = engine;
    }

    /**
     * Launches the service using the provided {@link Engine}.
     * @return a {@link CompletableFuture} which represents the process on running state until finish.
     */
    public CompletableFuture<Void> start() {
        return engine.submit(authorizationUseCases::bootstrap)
                .thenCompose(u -> this.startNfcTagEmulation());
    }

    private CompletableFuture<Void> startNfcTagEmulation() {
        return authorizationUseCases.waitNearbyDevice()
                .thenCompose(u -> authorizationUseCases.transmitToken())
                .thenCompose(u -> this.startNfcTagEmulation());
    }

    public static AuthorizationService of(final Engine engine, final AuthorizationUseCases useCases) {
        return new AuthorizationService(engine, useCases);
    }

    @Override
    public void notify(final Token value) {
        if (value != null) {
            authorizationUseCases.activateToken(value);
        } else {
            authorizationUseCases.deactivateToken();
        }
    }
}
