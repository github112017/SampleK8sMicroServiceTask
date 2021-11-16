package com.showbie.publicservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
        "auth.token.key=ABC123"
})
class ApplicationTests {

    @Test
    void contextLoads() {
    }

}
