package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.github.ecotrip.token.Token;
import io.github.ecotrip.token.TokenService;
public class TokenServiceTest {

    @Test
    public void testInit() {
        final var tokenService = TokenService.of();
        assertTrue(tokenService.getActiveToken().isEmpty());
        var dummyToken = Token.of("abc1234");
        tokenService.update(dummyToken);
        assertEquals(tokenService.getActiveToken().get(), dummyToken);
    }

    @Test
    public void testDisable() {
        final var tokenService = TokenService.of();
        tokenService.disable();
        assertTrue(tokenService.getActiveToken().isEmpty());
        var dummyToken = Token.of("abc1234");
        tokenService.update(dummyToken);
        assertTrue(tokenService.getActiveToken().isPresent());
        tokenService.disable();
        assertTrue(tokenService.getActiveToken().isEmpty());
    }

    @Test
    public void testAsyncUpdate() {
        final var tokenService = TokenService.of();
        var dummyToken = Token.of("abc1234");
        var delayedExecutor = CompletableFuture.delayedExecutor(1000, TimeUnit.MILLISECONDS);
        var firstUpdate = CompletableFuture.runAsync(tokenService::disable, delayedExecutor);
        var secondUpdate = CompletableFuture.runAsync(() -> tokenService.update(dummyToken));
        secondUpdate.join();
        assertEquals(tokenService.getActiveToken(), Optional.of(dummyToken));
        firstUpdate.join();
        assertTrue(tokenService.getActiveToken().isEmpty());
    }
}
