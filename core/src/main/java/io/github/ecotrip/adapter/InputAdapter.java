package io.github.ecotrip.adapter;

import java.util.concurrent.CompletableFuture;

import io.github.ecotrip.pattern.Observable;
import io.github.ecotrip.token.Token;

/**
 * Authorization token input adapter
 */
public abstract class InputAdapter extends Observable<Token> {
    public abstract CompletableFuture<Void> requireToken();
}
