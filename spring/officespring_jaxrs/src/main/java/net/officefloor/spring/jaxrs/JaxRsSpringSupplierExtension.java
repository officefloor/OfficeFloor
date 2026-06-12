/*-
 * #%L
 * JAX-RS with Spring Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.spring.jaxrs;

import java.util.EnumSet;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import jakarta.servlet.DispatcherType;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionServiceFactory;

/**
 * JAX-RS {@link SpringSupplierExtension}.
 *
 * @author Daniel Sagenschneider
 */
public class JaxRsSpringSupplierExtension implements SpringSupplierExtension, SpringSupplierExtensionServiceFactory {

	/*
	 * ================== SpringSupplierExtensionServiceFactory ==================
	 */

	@Override
	public SpringSupplierExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== SpringSupplierExtension ===========================
	 */

	@Override
	public void configureSpring(SpringApplicationBuilder builder) throws Exception {

		// Register Jersey filter (Spring Boot 4 no longer auto-configures JerseyAutoConfiguration)
		builder.sources(JerseyFilterConfiguration.class);
	}

	/**
	 * {@link Configuration} to register Jersey as a filter.
	 * Spring Boot 4 removed JerseyAutoConfiguration from auto-configuration.
	 */
	@Configuration(proxyBeanMethods = false)
	public static class JerseyFilterConfiguration {

		@Bean
		public FilterRegistrationBean<ServletContainer> jerseyFilterRegistration(ResourceConfig config) {
			FilterRegistrationBean<ServletContainer> registration = new FilterRegistrationBean<>();
			registration.setFilter(new ServletContainer(config));
			registration.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
			registration.addUrlPatterns("/*");
			registration.setName("jerseyFilter");
			registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC,
					DispatcherType.FORWARD));
			registration.addInitParameter(ServletProperties.FILTER_FORWARD_ON_404, Boolean.TRUE.toString());
			return registration;
		}
	}

}
