package io.quarkus.jsch.runtime;

import java.util.function.Supplier;

import com.jcraft.jsch.Session;

import io.quarkus.jsch.JSchSessions;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class JSchSessionRecorder {

    public Supplier<Session> jschSessionSupplier(String sessionName) {
        return () -> JSchSessions.fromName(sessionName);
    }

}
