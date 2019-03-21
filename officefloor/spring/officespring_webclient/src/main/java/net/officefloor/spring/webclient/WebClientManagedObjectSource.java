/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.spring.webclient;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
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

		// Configure the meta data
		context.setObjectClass(WebClient.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {

		// Create the web client builder
		this.webClientBuilder = WebClient.builder();
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