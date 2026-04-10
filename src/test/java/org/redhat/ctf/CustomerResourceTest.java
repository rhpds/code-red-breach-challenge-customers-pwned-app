package org.redhat.ctf;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;

// Force REST-assured to parse text/plain responses as JSON

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerResourceTest {

    @Test
    @Order(1)
    void get() {

        RestAssured.registerParser("text/plain", Parser.JSON);

        given()
                .when()
                .get("/customers")
                .then()
                .statusCode(200)
                .body("size()", equalTo(13))
                .body("name", hasItems("Peter","Saul") );
    }

    @Test
    @Order(2)
    void getById() {
        RestAssured.registerParser("text/plain", Parser.JSON);

        given()
                .when()
                .get("/customers/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Saul"))
                .body("lastname", equalTo("Goodman"));
    }


}