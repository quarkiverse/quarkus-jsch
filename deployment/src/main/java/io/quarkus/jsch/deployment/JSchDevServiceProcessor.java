package io.quarkus.jsch.deployment;

import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;

@BuildSteps(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
public class JSchDevServiceProcessor {

    @BuildStep
    DevServicesResultBuildItem startDevServices(JSchBuildTimeConfig config) {
        if (!config.devservices().enabled()) {
            return null;
        }

        DockerImageName dockerImageName = DockerImageName.parse(config.devservices().image());
        GenericContainer container = new GenericContainer<>(dockerImageName)
                .withEnv("USER_NAME", config.devservices().username())
                .withEnv("USER_PASSWORD", config.devservices().password())
                .withEnv("PASSWORD_ACCESS", "true")
                .withExposedPorts(config.devservices().port())
                .withReuse(config.devservices().reuse());

        container.start();

        Map<String, String> configOverrides = Map.of(
                "quarkus.jsch.session.host", container.getHost(),
                "quarkus.jsch.session.port", container.getMappedPort(config.devservices().port()).toString(),
                "quarkus.jsch.session.username", config.devservices().username(),
                "quarkus.jsch.session.password", config.devservices().password(),
                "quarkus.jsch.session.config.StrictHostKeyChecking", "no");

        return new DevServicesResultBuildItem.RunningDevService(JSchProcessor.FEATURE, container.getContainerId(),
                container::close, configOverrides)
                .toBuildItem();
    }
}
