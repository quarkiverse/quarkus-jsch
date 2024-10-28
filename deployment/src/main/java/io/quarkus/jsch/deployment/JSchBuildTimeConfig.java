package io.quarkus.jsch.deployment;

import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.jsch")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface JSchBuildTimeConfig {

    /**
     * The JSch DevService configuration.
     */
    @ConfigDocSection
    JSchDevServiceConfig devservices();

    @ConfigGroup
    public interface JSchDevServiceConfig {

        /**
         * Enable the JSch DevService.
         */
        @WithDefault("true")
        boolean enabled();

        /**
         * The image to use for the JSch DevService.
         */
        @WithDefault("linuxserver/openssh-server")
        String image();

        /**
         * The port to use for the JSch DevService.
         */
        @WithDefault("2222")
        int port();

        /**
         * The username to use for the JSch DevService.
         */
        @WithDefault("quarkus")
        String username();

        /**
         * The password to use for the JSch DevService.
         */
        @WithDefault("quarkus")
        String password();

        /**
         * Whether to reuse the container for the JSch DevService.
         */
        @WithDefault("false")
        boolean reuse();

    }
}
