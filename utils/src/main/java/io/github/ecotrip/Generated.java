package io.github.ecotrip;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * A special annotation used to exclude java classes
 * from Jacoco coverage.
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Generated {
}
