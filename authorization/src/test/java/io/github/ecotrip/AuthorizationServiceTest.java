package io.github.ecotrip;

// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.ecotrip.adapter.InputAdapter;
import io.github.ecotrip.adapter.NfcAdapter;
import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.execution.engine.EngineFactory;
import io.github.ecotrip.token.Token;
import io.github.ecotrip.usecase.AuthorizationUseCases;

@ExtendWith(MockitoExtension.class)
public class AuthorizationServiceTest {

    private void lazyFuture(CompletableFuture fut) {
        try {
            System.out.println("SLEEEEP START");
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            fut.complete(null);
        }
    }
    @Test
    public void testStart() {
        var engine = EngineFactory.createScheduledEngine(1);

        //Create use cases
        InputAdapter mockedInput = Mockito.mock(InputAdapter.class);
        when(mockedInput.requireToken()).thenReturn(CompletableFuture.completedFuture(null));

        NfcAdapter mockedNfc = Mockito.mock(NfcAdapter.class);

        var initFut = new CompletableFuture();
        when(mockedNfc.initTagAndWaitForNearbyDevice()).thenReturn(initFut);
        when(mockedNfc.transmit(any(Token.class))).thenReturn(CompletableFuture.completedFuture(null));
        when(mockedNfc.reset()).thenReturn(CompletableFuture.completedFuture(null));

        var authorizationUseCases = AuthorizationUseCases.of(mockedInput, mockedNfc);

        var service = AuthorizationService.of(engine, authorizationUseCases);

        var future = service.start();
        lazyFuture(initFut);
        Execution.safeSleep(100);
        service.notify(Token.of("TEST"));

        future.complete(null);
        future.join();
    }
}
