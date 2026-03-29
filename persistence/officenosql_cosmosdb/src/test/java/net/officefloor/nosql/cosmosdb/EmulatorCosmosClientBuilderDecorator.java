/*-
 * #%L
 * CosmosDB
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.nosql.cosmosdb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

import com.azure.cosmos.CosmosClientBuilder;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.nosql.cosmosdb.test.AbstractCosmosDbJunit;
import net.officefloor.nosql.cosmosdb.test.CosmosEmulatorInstance;
import net.officefloor.nosql.cosmosdb.test.CosmosEmulatorInstance.Configuration;
import net.officefloor.nosql.cosmosdb.test.CosmosSelfSignedCertificate;

/**
 * {@link CosmosClientBuilderDecorator} for Cosmos emulator.
 * 
 * @author Daniel Sagenschneider
 */
public class EmulatorCosmosClientBuilderDecorator
		implements CosmosClientBuilderDecorator, CosmosClientBuilderDecoratorServiceFactory {

	/*
	 * ================= CosmosClientBuilderDecoratorServiceFactory ==============
	 */

	@Override
	public CosmosClientBuilderDecorator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ===================== CosmosClientBuilderDecorator ========================
	 */

	@Override
	public CosmosClientBuilder decorate(CosmosClientBuilder builder) throws Exception {
		CosmosSelfSignedCertificate.initialise(builder,
				new CosmosEmulatorInstance(new Configuration(), (message, cause) -> {
					String failureMessage = message + (cause != null ? "\n\n" + cause : "");
					if (AbstractCosmosDbJunit.isSkipFailure()) {
						Assumptions.assumeTrue(false, failureMessage);
					} else {
						Assertions.fail(message);
					}
					return null;
				}).getCosmosEmulatorCertificate());
		return builder;
	}

}
