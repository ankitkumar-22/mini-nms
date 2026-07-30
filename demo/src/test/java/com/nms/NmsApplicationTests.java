package com.nms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = NmsApplication.class)
@ActiveProfiles("local")
class NmsApplicationTests {

    @Test
    @DisplayName("Should successfully load Spring Application Context")
    void contextLoads() {
    }

}
