package io.quarkus.it.jsch;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JSchDevServiceTest {

    @Test
    void shouldConnectWithDefaultSession() {
        given()
                .log().all()
                .get("/jsch/session-default")
                .then()
                .statusCode(is(200));
    }

    @Test
    void shouldConnectWithNamedSession() {
        given()
                .get("/jsch/session-named")
                .then()
                .statusCode(is(200));
    }

    @Test
    void shouldConnectWithProgSession() {
        given()
                .get("/jsch/session-program")
                .then()
                .statusCode(is(200));
    }

    @Test
    void shouldConnectWithoutMockSession() {
        given()
                .get("/jsch/session-no-mock")
                .then()
                .statusCode(is(200));
    }

    @Test
    void shouldNotConnect() {
        given()
                .get("/jsch/session-not-found")
                .then()
                .statusCode(is(500));
    }

}
