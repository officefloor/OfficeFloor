/*-
 * #%L
 * OfficeFloor Cache of static data
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.cache.constant;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import net.officefloor.cache.Cache;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.managedobject.poll.StatePollContext;
import net.officefloor.plugin.managedobject.poll.StatePoller;

/**
 * {@link Cache} {@link ManagedObjectSource} that loads a constant data set.
 * 
 * @author Daniel Sagenschneider
 */
public class ConstantCacheManagedObjectSource<K, V>
		extends AbstractManagedObjectSource<None, ConstantCacheManagedObjectSource.Flows> implements ManagedObject {

	/**
	 * {@link Property} name for the poll interval in milliseconds.
	 */
	public static final String POLL_INTERVAL = "poll.interval";

	/**
	 * Qualifier for the {@link ConstantCacheDataRetriever} dependency.
	 */
	public static final String DATA_RETRIEVER_QUALIFIER = "qualifier";

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		REFRESH
	}

	/**
	 * {@link Logger}.
	 */
	private Logger logger;

	/**
	 * {@link Clock}.
	 */
	private Clock<Long> clock;

	/**
	 * Poll interval in milliseconds. <code>null</code> to use default.
	 */
	private Long pollIntervalMilliseconds = null;

	/**
	 * {@link StatePoller} of {@link Cache} data.
	 */
	private StatePoller<Map<K, V>, Flows> state;

	/**
	 * {@link Cache}.
	 */
	private final Cache<K, V> cache = (key) -> {
		try {
			return this.state.getState(this.pollIntervalMilliseconds != null ? this.pollIntervalMilliseconds : 1000,
					TimeUnit.MILLISECONDS).get(key);
		} catch (TimeoutException ex) {
			this.logger.warning("Timed out obtaining cached data");
			return null;
		}
	};

	/**
	 * Obtains the {@link Cache}.
	 * 
	 * @return {@link Cache}.
	 */
	public Cache<K, V> getCache() {
		return this.cache;
	}

	/**
	 * ====================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
		ManagedObjectSourceContext<Flows> mosContext = context.getManagedObjectSourceContext();
		this.logger = mosContext.getLogger();

		// Obtain the poll interval
		String pollInterval = mosContext.getProperty(POLL_INTERVAL, null);
		if (pollInterval != null) {
			this.pollIntervalMilliseconds = Long.valueOf(pollInterval);
		}

		// Obtain the qualifier
		String qualifier = mosContext.getProperty(DATA_RETRIEVER_QUALIFIER, null);

		// Provide type
		context.setObjectClass(Cache.class);

		// Obtain necessary poller details
		this.clock = mosContext.getClock((time) -> time);

		// Register flow to refresh cache
		final String FUNCTION_NAME = "REFRESH";
		@SuppressWarnings("unchecked")
		ManagedObjectFunctionBuilder<Indexed, None> function = mosContext.addManagedFunction(FUNCTION_NAME,
				() -> (functionContext) -> {

					// Obtain the dependencies
					StatePollContext<Map<K, V>> pollContext = (StatePollContext<Map<K, V>>) functionContext
							.getObject(0);
					ConstantCacheDataRetriever<K, V> retriever = (ConstantCacheDataRetriever<K, V>) functionContext
							.getObject(1);

					// Obtain the data
					Map<K, V> data = retriever.getData();

					// Load new cache data
					Map<K, V> cacheData = new HashMap<>(data);

					// Specify state
					pollContext.setNextState(cacheData, -1, null);
				});
		function.linkParameter(0, StatePollContext.class);
		ManagedObjectFunctionDependency retriever = mosContext.addFunctionDependency("RETRIEVER",
				ConstantCacheDataRetriever.class);
		if (qualifier != null) {
			retriever.setTypeQualifier(qualifier);
		}
		function.linkObject(1, retriever);
		mosContext.getFlow(Flows.REFRESH).linkFunction(FUNCTION_NAME);
		context.addFlow(Flows.REFRESH, null);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {

		// Undertake polling of state
		StatePoller.Builder<Map, Flows> builder = StatePoller
				.builder(Map.class, this.clock, Flows.REFRESH, context, (pollContext) -> this)
				.parameter((pollContext) -> pollContext);
		if (this.pollIntervalMilliseconds != null) {
			builder.defaultPollInterval(this.pollIntervalMilliseconds, TimeUnit.MILLISECONDS);
		}
		this.state = (StatePoller) builder.build();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ======================= ManagedObject =================================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.cache;
	}

}
