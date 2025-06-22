package com.example.springhttpclientdatajpademo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("mariadb")
class SpringHttpClientDataJpaDemoApplicationTests {

    @Test
    void contextLoads() {
        // Basic test to verify Spring context loads
    }
}
