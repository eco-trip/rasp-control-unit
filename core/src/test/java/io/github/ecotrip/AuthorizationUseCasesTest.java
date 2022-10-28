package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// import java.util.List;
// import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.ecotrip.adapter.InputAdapter;
import io.github.ecotrip.adapter.NfcAdapter;
import io.github.ecotrip.token.Token;
import io.github.ecotrip.usecase.AuthorizationUseCases;

@ExtendWith(MockitoExtension.class)
public class AuthorizationUseCasesTest {

    @Test
    public void testBootstrapUseCase() {
        InputAdapter mockedInput = Mockito.mock(InputAdapter.class);
        when(mockedInput.requireToken()).thenReturn(CompletableFuture.completedFuture(null));

        NfcAdapter mockedNfc = Mockito.mock(NfcAdapter.class);

        var auth = AuthorizationUseCases.of(mockedInput, mockedNfc);
        var f1 = auth.bootstrap();
        assertTrue(f1.isDone());
        assertFalse(f1.isCompletedExceptionally());
    }

    @Test
    public void testWaitNearbyDevice() {
        InputAdapter mockedInput = Mockito.mock(InputAdapter.class);

        NfcAdapter mockedNfc = Mockito.mock(NfcAdapter.class);
        when(mockedNfc.initTagAndWaitForNearbyDevice()).thenReturn(CompletableFuture.completedFuture(null));

        var auth = AuthorizationUseCases.of(mockedInput, mockedNfc);
        var f1 = auth.waitNearbyDevice();
        assertTrue(f1.isDone());
        assertFalse(f1.isCompletedExceptionally());
    }

    @Test
    public void testDeactivateToken() {
        InputAdapter mockedInput = Mockito.mock(InputAdapter.class);
        NfcAdapter mockedNfc = Mockito.mock(NfcAdapter.class);

        var auth = AuthorizationUseCases.of(mockedInput, mockedNfc);
        var f1 = auth.deactivateToken();
        f1.join();
        assertFalse(f1.isCompletedExceptionally());
    }

    @Test
    public void testActivateToken() {
        InputAdapter mockedInput = Mockito.mock(InputAdapter.class);
        NfcAdapter mockedNfc = Mockito.mock(NfcAdapter.class);

        var auth = AuthorizationUseCases.of(mockedInput, mockedNfc);
        var f1 = auth.activateToken(Token.of("test"));
        f1.join();
        assertFalse(f1.isCompletedExceptionally());
    }

    @Test
    public void testTransmitToken() {
        InputAdapter mockedInput = Mockito.mock(InputAdapter.class);
        NfcAdapter mockedNfc = Mockito.mock(NfcAdapter.class);
        when(mockedNfc.transmit(any(Token.class))).thenReturn(CompletableFuture.completedFuture(null));
        when(mockedNfc.reset()).thenReturn(CompletableFuture.completedFuture(null));

        var auth = AuthorizationUseCases.of(mockedInput, mockedNfc);
        var f1 = auth.activateToken(Token.of("test"));
        f1.join();

        var f2 = auth.transmitToken();
        assertTrue(f2.isDone());
        assertFalse(f2.isCompletedExceptionally());
    }

    @Test
    public void testTransmitResetToken() {
        InputAdapter mockedInput = Mockito.mock(InputAdapter.class);
        NfcAdapter mockedNfc = Mockito.mock(NfcAdapter.class);
        when(mockedNfc.reset()).thenReturn(CompletableFuture.completedFuture(null));

        var auth = AuthorizationUseCases.of(mockedInput, mockedNfc);
        var f1 = auth.transmitToken();
        assertTrue(f1.isDone());
        assertFalse(f1.isCompletedExceptionally());
    }
}
