package com.mesalive

import com.mesalive.support.EmbeddedPostgresSupport
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
class BackendApplicationTests {

	companion object {
		@JvmStatic
		@DynamicPropertySource
		fun datasource(registry: DynamicPropertyRegistry) {
			registry.add("spring.datasource.url") { EmbeddedPostgresSupport.jdbcUrl }
			registry.add("spring.datasource.username") { EmbeddedPostgresSupport.USERNAME }
			registry.add("spring.datasource.password") { EmbeddedPostgresSupport.PASSWORD }
		}
	}

	@Test
	fun contextLoads() {
	}

}
