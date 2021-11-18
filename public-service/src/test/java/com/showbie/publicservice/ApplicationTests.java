package com.showbie.publicservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
        "auth.token.key=ABC123",                 // required property
        "private.service.auth.token.key=DEF456", // required property
})
class ApplicationTests {

    @Test
    void contextLoads() {
    }

}
