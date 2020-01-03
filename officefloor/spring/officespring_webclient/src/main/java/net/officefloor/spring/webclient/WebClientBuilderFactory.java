package net.officefloor.spring.webclient;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Factory to create custom {@link WebClient} {@link Builder}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebClientBuilderFactory {

	/**
	 * Creates a custom {@link WebClient} {@link Builder}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link Builder}.
	 * @throws Exception If fails to create {@link Builder}.
	 */
	Builder createWebClientBuilder(SourceContext context) throws Exception;

}