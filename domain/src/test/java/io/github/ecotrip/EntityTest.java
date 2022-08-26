package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;


public class EntityTest {
    @Test
    public void testIdentifier() {
        var stringId = "abc123";
        var entity1 = new Entity<>(stringId) {};
        var numberId = 123;
        var entity2 = new Entity<>(numberId) {};
        var uuid = UUID.randomUUID();
        var entity3 = new Entity<>(uuid) {};
        assertEquals(entity1.getIdentifier(), stringId);
        assertEquals(entity2.getIdentifier(), numberId);
        assertEquals(entity3.getIdentifier(), uuid);
        assertThrows(IllegalStateException.class, () -> new Entity<UUID>(null){});
    }

    @Test
    public void testHashCode() {
        var uuid = UUID.randomUUID();
        var entity1 = new Entity<>(uuid) {};
        var entity2 = new Entity<>(uuid) {};
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }
}
