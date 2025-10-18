package com.montes.url_shortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UrlShortenerApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring application context loads successfully
	}

	@Test
	void mainMethodShouldRun() {
		// Test that the main method can be called without exceptions
		// This is a simple smoke test
		UrlShortenerApplication.main(new String[]{});
	}
}
