package io.quarkus.it.jsch;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;

import io.quarkus.jsch.JSchSession;
import io.quarkus.jsch.JSchSessions;

@Path("/jsch")
@RequestScoped
public class JSchResource {

    @JSchSession("doudou")
    Session namedSession;

    @JSchSession
    Session defaultSession;

    @GET
    public Response connect(@QueryParam("host") String host, @QueryParam("port") int port) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(null, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        String serverVersion = session.getServerVersion();
        session.disconnect();
        return Response.ok(serverVersion).build();
    }

    @GET
    @Path("/session-default")
    public Response connectSessionDefault() {
        return Response.ok(defaultSession.getServerVersion()).build();
    }

    @GET
    @Path("/session-named")
    public Response connectSessionNamed() {
        return Response.ok(namedSession.getServerVersion()).build();
    }

    @GET
    @Path("/session-program")
    public Response connectSessionProgram() {
        Session localSession = JSchSessions.fromName("toto");
        return Response.ok(localSession.getServerVersion()).build();
    }

    @GET
    @Path("/session-no-mock")
    public Response connectSessionProgramNoMock() {
        Session localSession = JSchSessions.fromName("no-mock");
        return Response.ok(localSession.getServerVersion()).build();
    }

    @GET
    @Path("/session-not-found")
    public Response connectSessionNotFound() {
        Session localSession = JSchSessions.fromName("not-found");
        return Response.ok(localSession.getServerVersion()).build();
    }

    @GET
    @Path("/keypair/decrypt")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean decryptKeypair(@QueryParam("privateKey") String privateKey,
            @QueryParam("passphrase") String passphrase) throws JSchException {
        KeyPair keyPair = KeyPair.load(new JSch(), privateKey, null);
        return keyPair.decrypt(passphrase);
    }

    @GET
    @Path("/zlib")
    public Response zlib(@QueryParam("host") String host, @QueryParam("port") int port) throws JSchException {
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
