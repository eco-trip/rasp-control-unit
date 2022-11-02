package io.github.ecotrip;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.ecotrip.execution.engine.Engine;
import io.github.ecotrip.pattern.Observer;
import io.github.ecotrip.token.Token;
import io.github.ecotrip.usecase.AuthorizationUseCases;

/**
 * Contains the logic to receive and transmit a token to an NFC device
 */
public class AuthorizationService implements Observer<Token> {
    private final Logger logger = Logger.getLogger(AuthorizationService.class.getName());
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
        return CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    authorizationUseCases.waitNearbyDevice().join();
                    authorizationUseCases.transmitToken().join();
                } catch (CancellationException | CompletionException ex) {
                    logger.log(Level.WARNING, ex.getMessage());
                }
            }
        });
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
