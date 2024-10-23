package io.quarkus.jsch.runtime;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import io.quarkus.jsch.JSchSession;
import io.quarkus.runtime.annotations.ConfigDocDefault;
import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithName;
import io.smallrye.config.WithUnnamedKey;

@ConfigMapping(prefix = "quarkus.jsch")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface JSchRuntimeConfig {

    /**
     * JSch sessions.
     */
    @ConfigDocMapKey("session-name")
    @WithDefaults
    @WithUnnamedKey(JSchSession.DEFAULT_SESSION_NAME)
    @WithName("session")
    @ConfigDocSection
    Map<String, Session> sessions();

    /**
     * The default proxy configuration to use for each JSch session.
     */
    @ConfigDocSection
    Optional<Proxy> proxy();

    @ConfigGroup
    public interface Proxy {

        boolean DEFAULT_ENABLED = true;
        int DEFAULT_PORT = 80;

        /**
         * Enable the proxy for the JSch session.
         *
         * <p>
         * Defaults to {@code true} if host is set.
         */
        Optional<Boolean> enabled();

        /**
         * The host to use for the JSch proxy.
         */
        String host();

        /**
         * The port to use for the JSch proxy.
         */
        @ConfigDocDefault("80")
        OptionalInt port();

        /**
         * The username to use for the JSch proxy.
         */
        Optional<Auth> auth();

        @ConfigGroup
        interface Auth {

            /**
             * The username to use for the JSch proxy.
             *
             * <p>
             * {@code proxy.auth} is optional.
             */
            String username();

            /**
             * The password to use for the JSch proxy.
             *
             * <p>
             * {@code proxy.auth} is optional.
             */
            String password();

        }

    }

    @ConfigGroup
    public interface Session {

        int DEFAULT_PORT = 22;
        int DEFAULT_KEEP_ALIVE_INTERVAL = 0;

        /**
         * The host to use for the JSch session.
         *
         * <p>
         * This is a required configuration property in production mode.
         */
        Optional<String> host();

        /**
         * The port to use for the JSch session.
         */
        @ConfigDocDefault("22")
        OptionalInt port();

        /**
         * The username to use for the JSch session.
         */
        Optional<String> username();

        /**
         * The password to use for the JSch session.
         */
        Optional<String> password();

        /**
         * The private key to use for the JSch session.
         */
        Optional<String> privateKey();

        /**
         * The passphrase to use for the JSch session.
         */
        Optional<String> passphrase();

        /**
         * The configuration to use for the JSch session.
         */
        @ConfigDocMapKey("config-name")
        Map<String, String> config();

        /**
         * The keep alive interval to use for the JSch session.
         * <p>
         * If zero is specified, any keep-alive message.
         */
        @ConfigDocDefault("0")
        OptionalInt keepAliveInterval();

        /**
         * The proxy to use for the JSch session.
         */
        @ConfigDocSection
        Optional<Proxy> proxy();

    }
}
