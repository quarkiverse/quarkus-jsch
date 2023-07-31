package io.quarkus.it.jsch;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.core.Is.is;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.RSAKeyGenParameterSpec;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.hostbased.AcceptAllHostBasedAuthenticator;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.UnknownCommandFactory;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.MiscPEMGenerator;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class JSchTest {

    private SshServer sshd;

    @BeforeEach
    public void setupSSHDServer() throws Exception {
        sshd = SshServer.setUpDefaultServer();
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Files.createTempFile("host", "key")));
        sshd.setHostBasedAuthenticator(AcceptAllHostBasedAuthenticator.INSTANCE);
        sshd.setPasswordAuthenticator(AcceptAllPasswordAuthenticator.INSTANCE);
        sshd.setPublickeyAuthenticator(AcceptAllPublickeyAuthenticator.INSTANCE);
        sshd.setCommandFactory(UnknownCommandFactory.INSTANCE);
        sshd.setHost("localhost");
        sshd.start();
    }

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
    void shouldDecryptUsingKeyPair(@TempDir Path keypairDir) throws Exception {
        String passphrase = "password";
        byte[] seed = passphrase.getBytes(UTF_8);

        SecureRandom rnd = SecureRandom.getInstanceStrong();
        rnd.setSeed(seed);

        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F0);
        // Generate a Keypair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(spec, rnd);

        java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();
        // Save the private key
        Path privateKeyPath = keypairDir.resolve("test_rsa");
        try (PemWriter writer = new PemWriter(Files.newBufferedWriter(privateKeyPath))) {
            PrivateKey privateKey = keyPair.getPrivate();
            writer.writeObject(new MiscPEMGenerator(PrivateKeyInfo.getInstance(privateKey.getEncoded())).generate());
        }

        // Save the public key
        Path publicKeyPath = keypairDir.resolve("test_rsa.pub");
        try (PemWriter writer = new PemWriter(Files.newBufferedWriter(publicKeyPath))) {
            PublicKey publicKey = keyPair.getPublic();
            writer.writeObject(new MiscPEMGenerator(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded())).generate());
        }
        given().queryParam("privateKey", privateKeyPath.toAbsolutePath().toString())
                .queryParam("passphrase", passphrase)
                .get("/jsch/keypair/decrypt")
                .then()
                .statusCode(is(200))
                .body(is("true"));
    }

    @AfterEach
    void stopServer() throws Exception {
        if (sshd != null) {
            sshd.stop(true);
        }
    }
}
