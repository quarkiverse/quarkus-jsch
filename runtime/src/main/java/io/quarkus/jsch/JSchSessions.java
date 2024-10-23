package io.quarkus.jsch;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PreDestroy;

import org.eclipse.microprofile.config.ConfigProvider;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Slf4jLogger;

import io.quarkus.arc.Arc;
import io.quarkus.jsch.runtime.JSchRuntimeConfig;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ConfigurationException;

public class JSchSessions {

    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    private final JSchRuntimeConfig jschConfig;

    public JSchSessions(JSchRuntimeConfig jschConfig) {
        JSch.setLogger(new Slf4jLogger());
        this.jschConfig = jschConfig;
    }

    /**
     * Get a session by name
     *
     * @param sessionName the session name
     * @return the session
     */
    public static Session fromName(String sessionName) {
        return Arc.container().instance(JSchSessions.class).get().getSession(sessionName);
    }

    /**
     * Disconnect all sessions
     */
    public static void shutdown() {
        Arc.container().instance(JSchSessions.class).get().disconnect();
    }

    /**
     * Get or create a session by name
     *
     * @param sessionName the session name
     * @return the session
     */
    public Session getSession(String sessionName) {
        return sessions.computeIfAbsent(sessionName, this::createSession);
    }

    /**
     * Disconnect all sessions
     */
    @PreDestroy
    public void disconnect() {
        sessions.values().forEach(Session::disconnect);
    }

    private synchronized Session createSession(String sessionName) {
        try {
            JSchRuntimeConfig.Session config = getConfig(sessionName);
            JSch jsch = new JSch();
            String baseProperty = this.getBaseProperty(sessionName);
            Session session = jsch.getSession(
                    config.username().orElse(null),
                    config.host()
                            .orElseThrow(() -> new ConfigurationException(
                                    String.format("Host is required for session '%s'", sessionName),
                                    Set.of(baseProperty + "host"))),
                    config.port().orElse(JSchRuntimeConfig.Session.DEFAULT_PORT));
            session.setServerAliveInterval(
                    config.keepAliveInterval().orElse(JSchRuntimeConfig.Session.DEFAULT_KEEP_ALIVE_INTERVAL));

            proxyConfig(config).ifPresent(c -> {
                ProxyHTTP proxy = new ProxyHTTP(c.host(), c.port().orElse(JSchRuntimeConfig.Proxy.DEFAULT_PORT));
                c.auth().ifPresent(a -> proxy.setUserPasswd(a.username(), a.password()));
                session.setProxy(proxy);
            });

            config.password().ifPresent(session::setPassword);

            config.privateKey().ifPresent(k -> {
                try {
                    jsch.addIdentity(k, config.passphrase().orElse(null));
                } catch (JSchException e) {
                    throw new ConfigurationException("Failed to add identity", e, Set.of(
                            baseProperty + "key",
                            baseProperty + "passphrase"));
                }
            });

            config.config().forEach(session::setConfig);
            session.connect();

            return session;
        } catch (JSchException e) {
            throw new ConfigurationException("Failed to create session: " + sessionName, e);
        }
    }

    private String getBaseProperty(String sessionName) {
        String baseProperty = "quarkus.jsch.sessions.";
        if (!sessionName.equals(JSchSession.DEFAULT_SESSION_NAME)) {
            baseProperty += sessionName + ".";
        }

        return baseProperty;
    }

    private JSchRuntimeConfig.Session getConfig(String sessionName) {
        if (isDevOrTest() && !JSchSession.DEFAULT_SESSION_NAME.equals(sessionName)) {
            // return the default session except if the mock flag is set to false
            JSchRuntimeConfig.Session c = jschConfig.sessions().get(sessionName);
            if (c == null || c.host().isEmpty()) {
                return jschConfig.sessions().get(JSchSession.DEFAULT_SESSION_NAME);
            }

            return c;
        }

        return jschConfig.sessions().computeIfAbsent(sessionName, n -> {
            throw new ConfigurationException("Session not found: " + sessionName);
        });
    }

    private Optional<JSchRuntimeConfig.Proxy> proxyConfig(JSchRuntimeConfig.Session sessionConfig) {
        Optional<JSchRuntimeConfig.Proxy> proxyConfig = sessionConfig.proxy();

        if (proxyConfig.isEmpty()) {
            return jschConfig.proxy().isPresent() ? jschConfig.proxy() : Optional.empty();
        }

        return proxyConfig.get().enabled().orElse(true) ? proxyConfig : Optional.empty();
    }

    private boolean isDevOrTest() {
        return LaunchMode.current().isDevOrTest()
                || "test".equals(ConfigProvider.getConfig().getValue("quarkus.profile", String.class));
    }
}
