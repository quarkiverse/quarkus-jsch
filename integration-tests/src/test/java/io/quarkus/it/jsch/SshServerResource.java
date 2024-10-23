package io.quarkus.it.jsch;

import java.nio.file.Files;
import java.util.Map;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.hostbased.AcceptAllHostBasedAuthenticator;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.UnknownCommandFactory;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class SshServerResource implements QuarkusTestResourceLifecycleManager {

    private SshServer sshd;

    @Override
    public Map<String, String> start() {
        try {
            sshd = SshServer.setUpDefaultServer();
            sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Files.createTempFile("host", "key")));
            sshd.setHostBasedAuthenticator(AcceptAllHostBasedAuthenticator.INSTANCE);
            sshd.setPasswordAuthenticator(AcceptAllPasswordAuthenticator.INSTANCE);
            sshd.setPublickeyAuthenticator(AcceptAllPublickeyAuthenticator.INSTANCE);
            sshd.setCommandFactory(UnknownCommandFactory.INSTANCE);
            sshd.setHost("localhost");
            sshd.start();

            return Map.of(
                    "quarkus.jsch.session.host", sshd.getHost(),
                    "quarkus.jsch.session.port", Integer.toString(sshd.getPort()),
                    "quarkus.jsch.session.config.StrictHostKeyChecking", "no",
                    "quarkus.jsch.session.no-mock.host", sshd.getHost(),
                    "quarkus.jsch.session.no-mock.port", Integer.toString(sshd.getPort()),
                    "quarkus.jsch.session.no-mock.mock", "false",
                    "quarkus.jsch.session.no-mock.config.StrictHostKeyChecking", "no");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            sshd.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(sshd, new TestInjector.AnnotatedAndMatchesType(WithSshTestServer.class, SshServer.class));
    }

}
