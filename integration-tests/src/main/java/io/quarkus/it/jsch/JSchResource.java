package io.quarkus.it.jsch;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import com.jcraft.jsch.JSch;
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
}
