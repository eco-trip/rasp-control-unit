package io.github.ecotrip;

import java.util.Objects;

public abstract class Entity<ID> {
    private final ID identifier;

    protected Entity(final ID identifier) {
        if(identifier == null) {
            throw new IllegalStateException("The entity's identifier can not be null!");
        }
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return identifier.equals(entity.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    public ID getIdentifier() {
        return identifier;
    }
}
