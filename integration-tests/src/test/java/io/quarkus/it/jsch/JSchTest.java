package io.quarkus.it.jsch;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.core.Is.is;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.sshd.server.SshServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(SshServerResource.class)
class JSchTest {

    @WithSshTestServer
    SshServer sshd;

    @Test
    void shouldConnect() {
        given().queryParam("host", sshd.getHost())
                .queryParam("port", sshd.getPort())
                .get("/jsch")
                .then()
                .statusCode(is(200))
                .body(endsWith(sshd.getVersion()));
    }

    @Test
    void shouldConnectWithDefaultSession() {
        given()
                .get("/jsch/session-default")
                .then()
                .statusCode(is(200))
                .body(endsWith(sshd.getVersion()));
    }

    @Test
    void shouldConnectWithNamedSession() {
        given()
                .get("/jsch/session-named")
                .then()
                .statusCode(is(200))
                .body(endsWith(sshd.getVersion()));
    }

    @Test
    void shouldConnectWithProgSession() {
        given()
                .get("/jsch/session-program")
                .then()
                .statusCode(is(200))
                .body(endsWith(sshd.getVersion()));
    }

    @Test
    void shouldConnectWithoutMockSession() {
        given()
                .get("/jsch/session-no-mock")
                .then()
                .statusCode(is(200))
                .body(endsWith(sshd.getVersion()));
    }

    @Test
    void shouldNotConnect() {
        given()
                .get("/jsch/session-not-found")
                .then()
                .statusCode(is(500));
    }

    @Test
    void shouldDecryptUsingKeyPair(@TempDir Path keypairDir) throws Exception {
        String passphrase = "password";
        // Generate a Keypair
        KeyPair keyPair = KeyPair.genKeyPair(new JSch(), KeyPair.RSA, 2048);
        // Save the private key
        Path privateKeyPath = keypairDir.resolve("test_rsa");
        try (OutputStream out = Files.newOutputStream(privateKeyPath)) {
            keyPair.writePrivateKey(out, passphrase.getBytes(UTF_8));
        }
        // Save the public key
        Path publicKeyPath = keypairDir.resolve("test_rsa.pub");
        try (OutputStream out = Files.newOutputStream(publicKeyPath)) {
            keyPair.writePublicKey(out, "test_rsa");
        }
        given().queryParam("privateKey", privateKeyPath.toAbsolutePath().toString())
                .queryParam("passphrase", passphrase)
                .get("/jsch/keypair/decrypt")
                .then()
                .statusCode(is(200))
                .body(is("true"));
    }

    @Test
    void shouldDecryptOpenSSLKeyUsingKeyPair() {
        String passphrase = "PrettyPlease";
        String privateKeyPath = getClass().getClassLoader().getResource("openssh_private_key").getFile();
        given().queryParam("privateKey", privateKeyPath)
                .queryParam("passphrase", passphrase)
                .get("/jsch/keypair/decrypt")
                .then()
                .statusCode(is(200))
                .body(is("true"));
    }

    @Test
    void shouldSupportZLibCompression() {
        given().queryParam("host", sshd.getHost())
                .queryParam("port", sshd.getPort())
                .get("/jsch/zlib")
                .then()
                .statusCode(is(200))
                .body(endsWith(sshd.getVersion()));
    }
}
