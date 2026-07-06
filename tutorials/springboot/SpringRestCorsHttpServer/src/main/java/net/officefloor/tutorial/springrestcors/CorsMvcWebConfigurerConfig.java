package net.officefloor.tutorial.springrestcors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// START SNIPPET: tutorial
@Configuration
public class CorsMvcWebConfigurerConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/cors/mvc-configurer/**")
				.allowedOrigins("https://example.com");
	}
}
// END SNIPPET: tutorial
