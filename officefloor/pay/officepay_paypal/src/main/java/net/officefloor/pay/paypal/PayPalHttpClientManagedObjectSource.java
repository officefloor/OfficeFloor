/*-
 * #%L
 * PayPal
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.pay.paypal;

import java.util.concurrent.TimeUnit;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.managedobject.poll.StatePollContext;
import net.officefloor.plugin.managedobject.poll.StatePoller;

/**
 * {@link ManagedObjectSource} for the {@link PayPalHttpClient}.
 * 
 * @author Daniel Sagenschneider
 */
public class PayPalHttpClientManagedObjectSource
		extends AbstractManagedObjectSource<None, PayPalHttpClientManagedObjectSource.Flows> implements ManagedObject {

	public static enum Flows {
		CONFIGURE
	}

	public static enum ConfigureDependencies {
		POLL_CONTEXT, REPOSITORY
	}

	/**
	 * {@link Runnable} within the context of the {@link PayPalHttpClient}.
	 */
	@FunctionalInterface
	public static interface ContextRunnable<T extends Throwable> {

		/**
		 * Logic to run within the context of the {@link PayPalHttpClient}.
		 */
		void run() throws T;
	}

	/**
	 * <p>
	 * Runs the {@link ContextRunnable} using the {@link PayPalHttpClient}.
	 * <p>
	 * This is typically used for testing to allow overriding the
	 * {@link PayPalHttpClient} being used.
	 * 
	 * @param paypalHttpClient {@link PayPalHttpClient}. May be <code>null</code> to
	 *                         not override.
	 * @param runnable         {@link ContextRunnable}.
	 * @throws T If failure in {@link ContextRunnable}.
	 */
	public static <T extends Throwable> void runWithPayPalHttpClient(PayPalHttpClient paypalHttpClient,
			ContextRunnable<T> runnable) throws T {

		// Initialise the override
		threadLocalPayPalHttpClientOverride.set(paypalHttpClient);
		try {

			// Undertake the logic
			runnable.run();

		} finally {
			// Clear the keys override
			threadLocalPayPalHttpClientOverride.remove();
		}
	}

	/**
	 * {@link ThreadLocal} for the {@link PayPalHttpClient}.
	 */
	private static ThreadLocal<PayPalHttpClient> threadLocalPayPalHttpClientOverride = new ThreadLocal<>();

	/**
	 * {@link StatePoller} to load the {@link PayPalHttpClient}.
	 */
	private StatePoller<PayPalHttpClient, Flows> paypalHttpClient;

	/*
	 * =================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
		ManagedObjectSourceContext<Flows> sourceContext = context.getManagedObjectSourceContext();

		// Load the meta-data
		context.setObjectClass(PayPalHttpClient.class);
		context.addFlow(Flows.CONFIGURE, StatePollContext.class);

		// Load PayPal environment
		ManagedObjectFunctionBuilder<ConfigureDependencies, None> loadPayPal = sourceContext
				.addManagedFunction(Flows.CONFIGURE.name(), () -> (functionContext) -> {

					// Obtain the dependencies
					@SuppressWarnings("unchecked")
					StatePollContext<PayPalHttpClient> pollContext = (StatePollContext<PayPalHttpClient>) functionContext
							.getObject(ConfigureDependencies.POLL_CONTEXT);
					PayPalConfigurationRepository repository = (PayPalConfigurationRepository) functionContext
							.getObject(ConfigureDependencies.REPOSITORY);

					// Retrieve the PayPal environment
					PayPalEnvironment environment = repository.createPayPalEnvironment();
					if (environment == null) {
						return; // no environment available
					}

					// Load PayPal client
					pollContext.setFinalState(new PayPalHttpClient(environment));
				});
		loadPayPal.linkParameter(ConfigureDependencies.POLL_CONTEXT, StatePollContext.class);
		loadPayPal.linkObject(ConfigureDependencies.REPOSITORY, sourceContext.addFunctionDependency(
				PayPalConfigurationRepository.class.getSimpleName(), PayPalConfigurationRepository.class));
		sourceContext.getFlow(Flows.CONFIGURE).linkFunction(Flows.CONFIGURE.name());
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {

		// Determine if override client
		PayPalHttpClient paypalHttpClient = threadLocalPayPalHttpClientOverride.get();
		if (paypalHttpClient != null) {
			this.paypalHttpClient = StatePoller.state(paypalHttpClient);
			return;
		}

		// Trigger loading configuration
		this.paypalHttpClient = StatePoller
				.builder(PayPalHttpClient.class, Flows.CONFIGURE, context, (pollContext) -> this)
				.parameter((pollContext) -> pollContext).defaultPollInterval(5, TimeUnit.SECONDS).build();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ====================== ManagedObject ==============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.paypalHttpClient.getState(20, TimeUnit.SECONDS);
	}

}
