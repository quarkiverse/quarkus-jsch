package io.quarkus.jsch.deployment;

import io.quarkus.builder.item.MultiBuildItem;

/**
 * A build item that registers a JSch session created by annotation.
 */
public final class JSchSessionBuildItem extends MultiBuildItem {

    private final String name;

    public JSchSessionBuildItem(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof JSchSessionBuildItem)) {
            return false;
        }

        JSchSessionBuildItem other = (JSchSessionBuildItem) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
