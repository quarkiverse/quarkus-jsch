package io.quarkus.it.jsch;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;

@Path("/jsch")
public class JSchResource {

    @GET
    public Response connect(@QueryParam("host") String host, @QueryParam("port") int port) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(null, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        String serverVersion = session.getServerVersion();
        session.disconnect();
        return Response.ok(serverVersion).build();
    }

    @GET
    @Path("/keypair/decrypt")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean decryptKeypair(@QueryParam("privateKey") String privateKey,
            @QueryParam("passphrase") String passphrase) throws Exception {
        KeyPair keyPair = KeyPair.load(new JSch(), privateKey, null);
        return keyPair.decrypt(passphrase);
    }

    @GET
    @Path("/zlib")
    public Response zlib(@QueryParam("host") String host, @QueryParam("port") int port) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(null, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("compression.c2s", "zlib@openssh.com,zlib");
        session.setConfig("compression_level", Integer.toString(9));
        session.connect();
        String serverVersion = session.getServerVersion();
        session.disconnect();
        return Response.ok(serverVersion).build();
    }
}
