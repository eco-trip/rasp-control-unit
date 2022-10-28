package io.github.ecotrip.adapter;

import java.util.concurrent.CompletableFuture;

import io.github.ecotrip.token.Token;

/**
 * Authorization output nfc adapter
 */
public interface NfcAdapter {
    CompletableFuture<Void> initTagAndWaitForNearbyDevice();

    CompletableFuture<Void> reset();

    CompletableFuture<Integer> transmit(Token token);
}
