/*-
 * #%L
 * Identity for Google Logins
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

package net.officefloor.identity.google;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.google.api.client.auth.openidconnect.IdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.managedobject.poll.StatePollContext;
import net.officefloor.plugin.managedobject.poll.StatePoller;

/**
 * {@link ManagedObjectSource} for the {@link GoogleIdTokenVerifier}.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleIdTokenVerifierManagedObjectSource extends
		AbstractManagedObjectSource<None, GoogleIdTokenVerifierManagedObjectSource.Flows> implements ManagedObject {

	public static enum Flows {
		CONFIGURE
	}

	public static enum ConfigureDependencies {
		POLL_CONTEXT, FACTORY
	}

	/**
	 * Specifies the {@link GoogleIdTokenVerifierFactory}.
	 * 
	 * @param factory {@link GoogleIdTokenVerifierFactory}.
	 */
	public static void setVerifyFactory(GoogleIdTokenVerifierFactory factory) {
		if (factory != null) {
			threadLocalVerifierFactory.set(factory);
		} else {
			threadLocalVerifierFactory.remove();
		}
	}

	/**
	 * Name of {@link Property} for the Google client id.
	 */
	public static final String PROPERTY_CLIENT_ID = "google.client.id";

	/**
	 * {@link GoogleIdTokenVerifierFactory} to create the
	 * {@link GoogleIdTokenVerifier}.
	 */
	private static ThreadLocal<GoogleIdTokenVerifierFactory> threadLocalVerifierFactory = new ThreadLocal<>();

	/**
	 * {@link Clock}.
	 */
	private Clock<Long> clock;

	/**
	 * {@link StatePoller} to load the {@link GoogleIdTokenVerifier}.
	 */
	private StatePoller<GoogleIdTokenVerifier, Flows> googleIdTokenVerifier;

	/*
	 * ==================== ManagedObjectSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
		ManagedObjectSourceContext<Flows> sourceContext = context.getManagedObjectSourceContext();

		// Load meta-data
		context.setObjectClass(GoogleIdTokenVerifier.class);
		context.addFlow(Flows.CONFIGURE, StatePollContext.class);

		// Obtain the clock
		this.clock = sourceContext.getClock((time) -> time);

		// Obtain the verifier factory
		GoogleIdTokenVerifierFactory factory = threadLocalVerifierFactory.get();
		if (factory == null) {

			// Determine if configure via property
			String audienceId = context.getManagedObjectSourceContext().getProperty(PROPERTY_CLIENT_ID, null);
			if (!CompileUtil.isBlank(audienceId)) {
				JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
				HttpTransport transport = new NetHttpTransport();
				factory = () -> new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
						.setAudience(Collections.singletonList(audienceId)).build();
			}
		}

		// Load PayPal environment
		final GoogleIdTokenVerifierFactory finalFactory = factory;
		ManagedObjectFunctionBuilder<Indexed, None> loadVerifier = sourceContext
				.addManagedFunction(Flows.CONFIGURE.name(), () -> (functionContext) -> {

					// Obtain the dependencies
					@SuppressWarnings("unchecked")
					StatePollContext<GoogleIdTokenVerifier> pollContext = (StatePollContext<GoogleIdTokenVerifier>) functionContext
							.getObject(0);
					GoogleIdTokenVerifierFactory verifierFactory = finalFactory;
					if (verifierFactory == null) {
						verifierFactory = (GoogleIdTokenVerifierFactory) functionContext.getObject(1);
					}

					// Create the Google Id Token Verifier
					GoogleIdTokenVerifier verifier = verifierFactory.create();
					if (verifier == null) {
						return; // no verifier available
					}

					// Load Verifier
					pollContext.setFinalState(verifier);
				});
		loadVerifier.linkParameter(0, StatePollContext.class);
		if (finalFactory == null) {
			loadVerifier.linkObject(1, sourceContext.addFunctionDependency(
					GoogleIdTokenVerifierFactory.class.getSimpleName(), GoogleIdTokenVerifierFactory.class));
		}
		sourceContext.getFlow(Flows.CONFIGURE).linkFunction(Flows.CONFIGURE.name());
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {

		// Trigger loading verifier
		this.googleIdTokenVerifier = StatePoller
				.builder(GoogleIdTokenVerifier.class, this.clock, Flows.CONFIGURE, context, (pollContext) -> this)
				.parameter((pollContext) -> pollContext).defaultPollInterval(5, TimeUnit.SECONDS).build();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ======================= ManagedObject ===============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.googleIdTokenVerifier.getState(20, TimeUnit.SECONDS);
	}

}
