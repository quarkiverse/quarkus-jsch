package io.quarkus.it.jsch;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class KeyPairTest {

    @Test
    void shouldBeAbleToDecryptKey() {
        given().get("/key").then().statusCode(is(200));
    }
}
