package io.quarkus.jsch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

/**
 * Create named session with Quarkus properties.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface JSchSession {

    public static final String DEFAULT_SESSION_NAME = "<default>";

    /**
     * The session name.
     */
    String value() default DEFAULT_SESSION_NAME;
}
