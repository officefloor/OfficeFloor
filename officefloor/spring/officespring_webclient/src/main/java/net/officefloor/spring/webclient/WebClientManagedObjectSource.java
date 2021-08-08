/*-
 * #%L
 * Spring WebClient
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

package net.officefloor.spring.webclient;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for a {@link WebClient}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebClientManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * {@link Property} name for a custom {@link WebClientBuilderFactory}.
	 */
	public static final String PROPERTY_WEB_CLIENT_BUILDER_FACTORY = "web.client.builder.factory";

	/**
	 * {@link WebClient} {@link Builder}.
	 */
	private Builder webClientBuilder;

	/*
	 * ================== ManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Configure the meta data
		context.setObjectClass(WebClient.class);

		// Determine if web client builder factory
		String factoryClassName = mosContext.getProperty(PROPERTY_WEB_CLIENT_BUILDER_FACTORY, null);
		WebClientBuilderFactory builderFactory;
		if (factoryClassName == null) {

			// Create default builder factory
			builderFactory = (sourceContext) -> WebClient.builder();

		} else {
			// Load custom builder factory
			Class<?> builderFactoryClass = mosContext.loadClass(factoryClassName);
			builderFactory = (WebClientBuilderFactory) builderFactoryClass.getConstructor().newInstance();
		}

		// Create the web client builder
		this.webClientBuilder = builderFactory.createWebClientBuilder(mosContext);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new WebClientManagedObject(this.webClientBuilder.build());
	}

	/**
	 * {@link WebClient} {@link ManagedObject}.
	 */
	private class WebClientManagedObject implements ManagedObject {

		/**
		 * {@link WebClient}.
		 */
		private final WebClient webClient;

		/**
		 * Instantiate.
		 * 
		 * @param webClient {@link WebClient}.
		 */
		private WebClientManagedObject(WebClient webClient) {
			this.webClient = webClient;
		}

		/*
		 * ================= ManagedObject ======================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this.webClient;
		}
	}

}
