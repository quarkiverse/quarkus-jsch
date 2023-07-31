package io.quarkus.it.jsch;

import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

@Path("/key")
public class KeyPairResource {

    @GET
    public Response checkKey() throws Exception {
        try (var is = getClass().getResourceAsStream(("/id_rsa.pem"))) {
            return Response.ok(verifyPemEncodedPrivateKey(is.readAllBytes(), "PrettyPlease")).build();
        }
    }

    /**
     * Verify that the private key is valid and can be decrypted with the given passphrase.
     *
     * @param privateKeyBytes The private key file (PEM encoded) as raw bytes.
     * @param passphrase The passphrase, may be null for plain text keys.
     * @return finger print.
     */
    private String verifyPemEncodedPrivateKey(byte[] privateKeyBytes, String passphrase) {
        KeyPair keyPair;
        try {
            keyPair = KeyPair.load(new JSch(), privateKeyBytes, null);
        } catch (JSchException e) {
            throw new RuntimeException("Failed to parse SSH private key", e);
        }
        if (keyPair.isEncrypted()) {
            keyPair.decrypt(passphrase.getBytes(StandardCharsets.UTF_8));
        }
        return keyPair.getFingerPrint();
    }
}
