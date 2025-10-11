package com.example;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.stringContainsInOrder;

@QuarkusTest
class SlideshowResourceHttpTest {

    @Test
    void rootServesSlideshowHtmlWithDefaults() {
        given()
            .when().get("/")
            .then()
            .statusCode(200)
            .contentType(ContentType.HTML)
            // Meta line shows defaults: 5 images • 3000 ms
            .body(stringContainsInOrder("5 images", "3000 ms"))
            // Ensure at least one data URI is present in the HTML
            .body(containsString("data:image/png;base64,"));
    }
}
