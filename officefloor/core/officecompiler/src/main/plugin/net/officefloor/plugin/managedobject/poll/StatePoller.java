/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.managedobject.poll;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupProcess;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService.SafeServicer;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Provides polling within a {@link ManagedObjectSource} to keep particular
 * state up to date.
 * <p>
 * Polling is undertaken by invoking a {@link ProcessState} from the
 * {@link ManagedObjectExecuteContext} of the {@link ManagedObjectSource} to
 * load the {@link StatePollContext} with the next state value. Also, means to
 * provide failures that are logged.
 * <p>
 * This is a convenient and efficient means to make keeping state of a
 * {@link ManagedObjectSource} up to date (without having to manage the
 * {@link ManagedObjectExecuteContext} or separate {@link Timer} that needs to
 * be coordinated with the {@link OfficeFloorListener}).
 * <p>
 * Example use would be keeping JWT keys up to date.
 * 
 * @author Daniel Sagenschneider
 */
public class StatePoller<S, F extends Enum<F>> {

	/**
	 * <p>
	 * Creates an initialised {@link StatePoller} that does not poll.
	 * <p>
	 * This is useful if state is loaded without needing polling. This allows the
	 * {@link ManagedObjectSource} to poll in some cases and load from configuration
	 * in others. This then allows using {@link StatePoller} for single interface to
	 * state.
	 * 
	 * @param <S>   State type.
	 * @param <F>   {@link Flow} {@link Enum}.
	 * @param state State.
	 * @return {@link StatePoller} initialised with state.
	 */
	@SuppressWarnings("unchecked")
	public static <S, F extends Enum<F>> StatePoller<S, F> state(S state) {
		return new StatePoller<S, F>((Class<S>) state.getClass(), () -> 0L, "",
				(context, callback) -> ((StatePoller<S, F>.StatePollContextImpl) context).setState(state, () -> {
				}), null, null, null, -1, -1);
	}

	/**
	 * Creates a {@link Builder} for {@link Flow} key.
	 * 
	 * @param stateType            State type.
	 * @param clock                {@link Clock}.
	 * @param flowKey              {@link Flow} key to use to invoke the polling
	 *                             {@link ProcessState} from the
	 *                             {@link ManagedObjectExecuteContext}.
	 * @param executeContext       {@link ManagedObjectExecuteContext}.
	 * @param managedObjectFactory Factory to create the {@link ManagedObject}.
	 * @return {@link Builder}.
	 */
	public static <S, F extends Enum<F>> Builder<S, F> builder(Class<S> stateType, Clock<Long> clock, F flowKey,
			ManagedObjectExecuteContext<F> executeContext,
			Function<StatePollContext<S>, ManagedObject> managedObjectFactory) {
		if (flowKey == null) {
			throw new IllegalArgumentException("Must provide flow key");
		}
		return new Builder<S, F>(stateType, clock, flowKey, -1, executeContext, managedObjectFactory, null, null);
	}

	/**
	 * Creates a {@link Builder} for {@link Flow} index.
	 * 
	 * @param stateType            State type.
	 * @param clock                {@link Clock}.
	 * @param flowIndex            {@link Flow} index to use to invoke the polling
	 *                             {@link ProcessState} from the
	 *                             {@link ManagedObjectExecuteContext}.
	 * @param executeContext       {@link ManagedObjectExecuteContext}.
	 * @param managedObjectFactory Factory to create the {@link ManagedObject}.
	 * @return {@link Builder}.
	 */
	public static <S> Builder<S, Indexed> builder(Class<S> stateType, Clock<Long> clock, int flowIndex,
			ManagedObjectExecuteContext<Indexed> executeContext,
			Function<StatePollContext<S>, ManagedObject> managedObjectFactory) {
		if (flowIndex < 0) {
			throw new IllegalArgumentException("Must provide valid flow index (provided " + flowIndex + ")");
		}
		return new Builder<S, Indexed>(stateType, clock, null, flowIndex, executeContext, managedObjectFactory, null,
				null);
	}

	/**
	 * <p>
	 * Creates a {@link Builder} for custom {@link Poller}.
	 * <p>
	 * This allows for higher level types that rely on but do not expose the
	 * {@link ManagedObjectSource} to use this {@link StatePoller}.
	 * 
	 * @param stateType   State type.
	 * @param clock       {@link Clock}.
	 * @param initialiser {@link Initialiser}.
	 * @param poller      {@link Poller}.
	 * @return {@link Builder}.
	 */
	public static <S> Builder<S, None> builder(Class<S> stateType, Clock<Long> clock, Initialiser<S> initialiser,
			Poller<S> poller) {
		return new Builder<S, None>(stateType, clock, null, -1, null, null, initialiser, poller);
	}

	/**
	 * Initialiser for polling state.
	 */
	@FunctionalInterface
	public static interface Initialiser<S> {

		/**
		 * Invoked to initialise polling state.
		 * 
		 * @param context  {@link StatePollContext}.
		 * @param callback {@link FlowCallback} to invoke on completion of the custom
		 *                 poll.
		 */
		void initialise(StatePollContext<S> context, FlowCallback callback);
	}

	/**
	 * Custom poller.
	 */
	@FunctionalInterface
	public static interface Poller<S> {

		/**
		 * Invoked to setup the next poll.
		 * 
		 * @param delay    Delay for poll. Will be <code>0</code> for start up (first)
		 *                 poll for state.
		 * @param context  {@link StatePollContext}.
		 * @param callback {@link FlowCallback} to invoke on completion of the custom
		 *                 poll.
		 */
		void nextPoll(long delay, StatePollContext<S> context, FlowCallback callback);
	}

	/**
	 * Builder for the {@link StatePoller}.
	 */
	public static class Builder<S, F extends Enum<F>> {

		/**
		 * State type.
		 */
		private final Class<S> stateType;

		/**
		 * {@link Clock}.
		 */
		private final Clock<Long> clock;

		/**
		 * {@link Flow} key. May be <code>null</code>.
		 */
		private final F flowKey;

		/**
		 * {@link Flow} index. Should be specified if no {@link Flow} key.
		 */
		private final int flowIndex;

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private final ManagedObjectExecuteContext<F> executeContext;

		/**
		 * {@link SafeManagedObjectService}.
		 */
		private final SafeManagedObjectService<F> servicer;

		/**
		 * Factory to create the {@link ManagedObject}.
		 */
		private final Function<StatePollContext<S>, ManagedObject> managedObjectFactory;

		/**
		 * Custom {@link Initialiser}.
		 */
		private final Initialiser<S> customInitialiser;

		/**
		 * Custom {@link Poller}.
		 */
		private final Poller<S> customPoller;

		/**
		 * Identifier.
		 */
		private String identifier;

		/**
		 * Default poll interval.
		 */
		private long defaultPollInterval = TimeUnit.HOURS.toMillis(1);

		/**
		 * Time expecting poll to complete.
		 */
		private long pollMargin = TimeUnit.SECONDS.toMillis(10);

		/**
		 * {@link Level} to log success poll messages.
		 */
		private Level successLogLevel = Level.INFO;

		/**
		 * {@link Logger} to use for logging.
		 */
		private Logger logger;

		/**
		 * Decorator for the {@link ManagedObjectStartupProcess}.
		 */
		private Consumer<ManagedObjectStartupProcess> startupProcessDecorator;

		/**
		 * Optional factory to create a parameter for the invoked {@link ProcessState}
		 * of the {@link ManagedObjectExecuteContext}.
		 */
		private Function<StatePollContext<S>, Object> parameterFactory = null;

		/**
		 * Instantiate.
		 * 
		 * @param stateType            State type.
		 * @param clock                {@link Clock}.
		 * @param flowKey              {@link Flow} key.
		 * @param flowIndex            {@link Flow} index.
		 * @param executeContext       {@link ManagedObjectExecuteContext}.
		 * @param managedObjectFactory Factory to create the {@link ManagedObject}.
		 * @param initialiser          {@link Initialiser}.
		 * @param customPoller         Custom {@link Poller}.
		 */
		private Builder(Class<S> stateType, Clock<Long> clock, F flowKey, int flowIndex,
				ManagedObjectExecuteContext<F> executeContext,
				Function<StatePollContext<S>, ManagedObject> managedObjectFactory, Initialiser<S> initialiser,
				Poller<S> customPoller) {
			if (stateType == null) {
				throw new IllegalArgumentException("Must provide state type");
			}
			if (customPoller == null) {
				if (executeContext == null) {
					throw new IllegalArgumentException(
							"Must provide " + ManagedObjectExecuteContext.class.getSimpleName());
				}
				if (managedObjectFactory == null) {
					throw new IllegalArgumentException(
							"Must provide " + ManagedObject.class.getSimpleName() + " factory");
				}
			}
			this.stateType = stateType;
			this.clock = clock;
			this.flowKey = flowKey;
			this.flowIndex = flowIndex;
			this.executeContext = executeContext;
			this.servicer = executeContext != null ? new SafeManagedObjectService<>(executeContext) : null;
			this.managedObjectFactory = managedObjectFactory;
			this.customInitialiser = initialiser;
			this.customPoller = customPoller;

			// Default logging
			this.logger(null);
			this.successLogLevel(null);
		}

		/**
		 * Obtains the default {@link Logger}.
		 * 
		 * @return Default {@link Logger}.
		 */
		private Logger getDefaultLogger() {
			return this.executeContext != null ? this.executeContext.getLogger()
					: Logger.getLogger(StatePoller.class.getName());
		}

		/**
		 * Allows specifying the default poll interval.
		 * 
		 * @param defaultPollInterval Default poll interval.
		 * @param unit                {@link TimeUnit} for the default poll interval.
		 * @return <cod>this</code>.
		 */
		public Builder<S, F> defaultPollInterval(long defaultPollInterval, TimeUnit unit) {
			unit = unit != null ? unit : TimeUnit.MILLISECONDS;
			long interval = unit.toMillis(defaultPollInterval);
			if (interval <= 0) {
				throw new IllegalArgumentException(
						"Poll interval of " + defaultPollInterval + " " + unit.toString().toLowerCase()
								+ " will result in " + interval + " milliseconds. Must be at least 1 millisecond.");
			}
			this.defaultPollInterval = interval;
			return this;
		}

		/**
		 * Allows specifying the poll margin. This is the margin before considering
		 * polling not triggering.
		 * 
		 * @param margin Margin before assuming polling not triggering.
		 * @param unit   {@link TimeUnit}.
		 * @return <code>this</code>.
		 */
		public Builder<S, F> pollMargin(long margin, TimeUnit unit) {
			unit = unit != null ? unit : TimeUnit.MILLISECONDS;
			long pollMargin = unit.toMillis(margin);
			if (pollMargin <= 0) {
				throw new IllegalArgumentException("Poll margin of " + margin + " " + unit.toString().toLowerCase()
						+ " will result in " + pollMargin + " milliseconds. Must be at least 1 millisecond.");
			}
			this.pollMargin = pollMargin;
			return this;
		}

		/**
		 * Allows specifying the success log {@link Level}.
		 * 
		 * @param successLogLevel Success log {@link Level}. Providing <code>null</code>
		 *                        will reset to default.
		 * @return <code>this</code>.
		 */
		public Builder<S, F> successLogLevel(Level successLogLevel) {
			this.successLogLevel = successLogLevel != null ? successLogLevel : Level.INFO;
			return this;
		}

		/**
		 * Allows specifying the {@link Logger}.
		 * 
		 * @param logger {@link Logger}. Providing <code>null</code> will reset to
		 *               default.
		 * @return <code>this</code>.
		 */
		public Builder<S, F> logger(Logger logger) {
			this.logger = logger != null ? logger : this.getDefaultLogger();
			return this;
		}

		/**
		 * Allows providing a parameter to the {@link ProcessState} invoked from the
		 * {@link ManagedObjectExecuteContext} for polling.
		 * 
		 * @param parameterFactory Factory for parameter.
		 * @return <code>this</code>.
		 */
		public Builder<S, F> parameter(Function<StatePollContext<S>, Object> parameterFactory) {
			if (this.customPoller != null) {
				throw new IllegalArgumentException(
						"Custom " + Poller.class.getSimpleName() + " used so may not configure parameter factory");
			}
			this.parameterFactory = parameterFactory;
			return this;
		}

		/**
		 * Allows providing an identifier in the log message to identify the
		 * {@link StatePoller}.
		 * 
		 * @param identifier Identifier.
		 * @return <code>this</code>.
		 */
		public Builder<S, F> identifier(String identifier) {
			this.identifier = identifier;
			return this;
		}

		/**
		 * Provides a decorator of the {@link ManagedObjectStartupProcess}.
		 * 
		 * @param startupProcessDecorator Decorate of the
		 *                                {@link ManagedObjectStartupProcess}.
		 * @return <code>this</code>.
		 */
		public Builder<S, F> startup(Consumer<ManagedObjectStartupProcess> startupProcessDecorator) {
			this.startupProcessDecorator = startupProcessDecorator;
			return this;
		}

		/**
		 * Builds the {@link StatePoller}.
		 * 
		 * @return {@link StatePoller}.
		 */
		public StatePoller<S, F> build() {

			// Obtain the identifier text
			String identifierText = this.identifier != null ? " for " + this.identifier : "";

			// Obtain the poller
			Initialiser<S> initialiser = this.customInitialiser;
			Poller<S> poller = this.customPoller;
			if (poller == null) {

				// Ensure have parameter factory
				final Function<StatePollContext<S>, Object> parameterFactory = (this.parameterFactory != null)
						? this.parameterFactory
						: (context) -> null;

				// Capture the decorator
				Consumer<ManagedObjectStartupProcess> startupDecorator = this.startupProcessDecorator != null
						? this.startupProcessDecorator
						: (startupProcess) -> {
							// No decoration
						};

				// No custom poller, so configure to flow key/index
				if (this.flowKey != null) {

					// Flow key initialiser
					initialiser = (context, callback) -> {
						Object parameter = parameterFactory.apply(context);
						ManagedObject managedObject = this.managedObjectFactory.apply(context);
						ManagedObjectStartupProcess process = this.executeContext.invokeStartupProcess(this.flowKey,
								parameter, managedObject, callback);
						startupDecorator.accept(process);
						process.setConcurrent(true); // concurrent to block for servicing
					};

					// Flow key poller
					poller = (delay, context, callback) -> {
						Object parameter = parameterFactory.apply(context);
						ManagedObject managedObject = this.managedObjectFactory.apply(context);
						this.invokeProcess(identifierText, (invokeContext) -> {
							invokeContext.invokeProcess(this.flowKey, parameter, managedObject, delay, callback);
						});
					};

				} else {

					// Flow index initialiser
					initialiser = (context, callback) -> {
						Object parameter = parameterFactory.apply(context);
						ManagedObject managedObject = this.managedObjectFactory.apply(context);
						ManagedObjectStartupProcess process = this.executeContext.invokeStartupProcess(this.flowIndex,
								parameter, managedObject, callback);
						startupDecorator.accept(process);
						process.setConcurrent(true); // concurrent to block for servicing
					};

					// Flow index poller
					poller = (delay, context, callback) -> {
						Object parameter = parameterFactory.apply(context);
						ManagedObject managedObject = this.managedObjectFactory.apply(context);
						this.invokeProcess(identifierText, (invokeContext) -> {
							invokeContext.invokeProcess(this.flowIndex, parameter, managedObject, delay, callback);
						});
					};
				}
			}

			// Create and return the poller
			return new StatePoller<>(this.stateType, this.clock, identifierText, initialiser, poller,
					this.successLogLevel, this.logger, this.defaultPollInterval, this.pollMargin);
		}

		/**
		 * Waits for the {@link ManagedObjectServiceContext}.
		 */
		private <T extends Exception> void invokeProcess(String identifierText, SafeServicer<F, T> invoker) throws T {
			try {
				this.servicer.service(invoker);
			} catch (InterruptedException ex) {
				this.logger.log(Level.SEVERE, "Polling has been interruped" + identifierText, ex);
			}
		}
	}

	/**
	 * Successful {@link Level}.
	 */
	private final Level successLogLevel;

	/**
	 * {@link Logger}.
	 */
	private final Logger logger;

	/**
	 * State type.
	 */
	private final Class<S> stateType;

	/**
	 * Identifier test.
	 */
	private final String identifierText;

	/**
	 * Default poll interval in milliseconds.
	 */
	private final long defaultPollInterval;

	/**
	 * Time expecting poll to complete.
	 */
	private final long pollMargin;

	/**
	 * {@link Clock}.
	 */
	private final Clock<Long> clock;

	/**
	 * {@link PollScedular}.
	 */
	private final PollScedular pollSchedular;

	/**
	 * Schedules a poll.
	 */
	@FunctionalInterface
	private interface PollScedular {

		/**
		 * Schedules a poll.
		 * 
		 * @param isManualPoll Indicates if manually triggered.
		 * @param delay        Delay until the poll is fired.
		 * @param timeUnit     {@link TimeUnit} for the delay.
		 */
		void schedulePoll(boolean isManualPoll, Long delay, TimeUnit timeUnit);
	}

	/**
	 * State being refreshed by polling.
	 */
	private volatile InitialisedState initialised = null;

	/**
	 * Initialised state.
	 */
	private class InitialisedState {

		/**
		 * Actual state.
		 */
		private volatile S state;

		/**
		 * Initialise.
		 * 
		 * @param state Initial state.
		 */
		private InitialisedState(S state) {
			this.state = state;
		}
	}

	/**
	 * Time of next expected poll.
	 */
	private volatile long expectedNextPollTime = -1;

	/**
	 * Instantiate.
	 *
	 * @param stateType           State type.
	 * @param identifierText      Identifier text.
	 * @param initialiser         {@link Initialiser}.
	 * @param clock               {@link Clock}.
	 * @param poller              {@link Poller}.
	 * @param successLogLevel     Success log {@link Level}.
	 * @param logger              {@link Logger}.
	 * @param defaultPollInterval Default poll interval in milliseconds.
	 * @param pollMargin          Time expecting poll to complete.
	 */
	private StatePoller(Class<S> stateType, Clock<Long> clock, String identifierText, Initialiser<S> initialiser,
			Poller<S> poller, Level successLogLevel, Logger logger, long defaultPollInterval, long pollMargin) {
		this.stateType = stateType;
		this.clock = clock;
		this.identifierText = identifierText;
		this.successLogLevel = successLogLevel;
		this.logger = logger;
		this.defaultPollInterval = defaultPollInterval;
		this.pollMargin = pollMargin;

		// Invoke the start up poll
		if (initialiser != null) {
			StatePollContextImpl startupContext = new StatePollContextImpl(false);
			initialiser.initialise(startupContext, startupContext);
		}

		// Create the date time formatter
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.systemDefault());

		// Create invoke for new process
		this.pollSchedular = poller == null ? null : (isManualPoll, nextPollInterval, unit) -> {
			long delay = this.defaultPollInterval;
			boolean isDefault = true;
			try {

				// Indicate if manual poll
				if (isManualPoll) {
					this.logger.log(successLogLevel, "Manual poll" + this.identifierText);
				}

				// Calculate the delay
				unit = unit != null ? unit : TimeUnit.MILLISECONDS;
				nextPollInterval = unit.toMillis(nextPollInterval);
				if (nextPollInterval >= 0) {
					isDefault = false;
					delay = nextPollInterval;
				}

			} finally {
				// Ensure best effort made to trigger poll again
				boolean isFailure = false;
				try {

					// Setup next poll
					StatePollContextImpl pollContext = new StatePollContextImpl(isManualPoll);
					poller.nextPoll(delay, pollContext, pollContext);

				} catch (Throwable ex) {
					// Severe issue as polling now stopped
					isFailure = true;
					this.logger.log(Level.SEVERE, "Polling has failed" + this.identifierText
							+ ".  Restart is required to re-establish polling.", ex);
				} finally {
					// Log time of next poll (if appropriate and no failure)
					if (!isManualPoll && !isFailure && this.logger.isLoggable(successLogLevel)) {
						ZonedDateTime nextPollTime = Instant.ofEpochMilli(System.currentTimeMillis() + delay)
								.atZone(ZoneId.systemDefault());
						this.logger.log(successLogLevel,
								"Next poll" + this.identifierText + " in " + (isDefault ? "(default) " : "") + delay
										+ " milliseconds (approx " + dateTimeFormatter.format(nextPollTime) + ")");
					}
				}
			}
		};
	}

	/**
	 * <p>
	 * Obtains the current state or <code>null</code> if not yet initialised.
	 * <p>
	 * This is a non-blocking method to enable obtaining the current state
	 * immediately. Useful if state is optional or has defaults, so does not need to
	 * wait to be initialised with first poll.
	 * 
	 * @return Current state or <code>null</code> if not yet initialised.
	 */
	public S getStateNow() {
		InitialisedState state = this.initialised;
		return (state != null) ? state.state : null;
	}

	/**
	 * Obtains the current state.
	 * 
	 * @param timeout The maximum time to wait.
	 * @param unit    The time unit of the timeout.
	 * @return State.
	 * @throws TimeoutException If times out waiting on the initial state.
	 */
	public S getState(long timeout, TimeUnit unit) throws TimeoutException {

		// Determine if require trigger poll
		long nextPollTime = this.expectedNextPollTime;
		if (nextPollTime >= 0) {
			// Not finalised, so determine if trigger poll
			long currentTime = this.clock.getTime() * 1000;
			if (currentTime > (nextPollTime + this.pollMargin)) {

				// Undertake poll only once for expected time
				synchronized (this) {
					nextPollTime = this.expectedNextPollTime;
					if (currentTime > (nextPollTime + this.pollMargin)) {

						// Given chance for poll, and avoid duplicate polls
						this.expectedNextPollTime = currentTime;

						// No poll in time, so must trigger poll
						this.poll();
					}
				}
			}
		}

		// Ensure initialised
		InitialisedState state = this.initialised;
		if (state == null) {

			// Wait until initialised or times out
			long millisecondsToWait = unit.toMillis(timeout);
			long endTime = System.currentTimeMillis() + millisecondsToWait;
			long waitTime = Math.max(100, millisecondsToWait / 10);
			synchronized (this) {
				while (state == null) {

					// Determine if timed out
					if (endTime < System.currentTimeMillis()) {
						throw new TimeoutException();
					}

					// Wait some time for the value
					try {
						this.wait(waitTime);
					} catch (InterruptedException ex) {
						// Likely forcing shutdown, so consider it timeout
						throw new TimeoutException("Thread interrupted");
					}

					// Capture if initialised
					state = this.initialised;
				}
			}
		}

		// Return the initialised value
		return state.state;
	}

	/**
	 * Manually trigger poll.
	 */
	public void poll() {
		if (this.pollSchedular != null) {
			this.pollSchedular.schedulePoll(true, 0L, null);
		}
	}

	/**
	 * Allows clearing the state.
	 */
	public void clear() {
		this.initialised = null;
	}

	/**
	 * {@link StatePollContext} implementation.
	 */
	private class StatePollContextImpl implements StatePollContext<S>, FlowCallback {

		/**
		 * Indicates if manually triggered.
		 */
		private final boolean isManualPoll;

		/**
		 * Flags whether complete.
		 */
		private boolean isComplete = false;

		/**
		 * Indicates if manual poll.
		 * 
		 * @param isManualPoll <code>true</code> if manual poll.
		 */
		private StatePollContextImpl(boolean isManualPoll) {
			this.isManualPoll = isManualPoll;
		}

		/*
		 * ================ StatePollContext =========================
		 */

		@Override
		public S getCurrentState() {
			return StatePoller.this.getStateNow();
		}

		@Override
		public void setNextState(S nextState, long nextPollInterval, TimeUnit unit) {
			this.setState(nextState, () -> {

				// Easy access to poller
				StatePoller<S, F> poller = StatePoller.this;

				// Trigger the next poll
				this.setNextExpectedPollTime(nextPollInterval, unit);
				poller.pollSchedular.schedulePoll(false, nextPollInterval, unit);
			});
		}

		@Override
		public void setFinalState(S finalState) {
			this.setState(finalState, () -> {

				// Easy access to poller
				StatePoller<S, F> poller = StatePoller.this;

				// Flag in final state
				poller.expectedNextPollTime = -1;
				poller.logger.log(poller.successLogLevel,
						"Final state set" + poller.identifierText + ". No further polling.");
			});
		}

		/**
		 * Specifies the state and possible triggers next poll.
		 * 
		 * @param nextState Next state.
		 * @param nextPoll  Triggers the next poll.
		 */
		private synchronized void setState(S nextState, Runnable nextPoll) {

			// Easy access to poller
			StatePoller<S, F> poller = StatePoller.this;

			// Ensure correct type
			if ((nextState != null) && (!poller.stateType.isAssignableFrom(nextState.getClass()))) {
				throw new IllegalArgumentException("Invalid state type " + nextState.getClass().getName()
						+ " (required " + poller.stateType.getName() + ")");
			}

			// Do nothing if already complete
			if (this.isComplete) {
				return;
			}
			this.isComplete = true;

			// Load the state
			try {
				// Ensure initialised
				InitialisedState initialised = poller.initialised;
				if (initialised == null) {

					// Load initial state (notifying any waiting get state)
					synchronized (poller) {
						initialised = new InitialisedState(nextState);
						poller.initialised = initialised;
						poller.notifyAll();
					}

				} else {
					// Update to next state
					initialised.state = nextState;
				}

			} finally {
				// Poll again (if not manually triggered)
				if (!this.isManualPoll) {
					nextPoll.run();
				}
			}
		}

		@Override
		public synchronized void setFailure(Throwable cause, long nextPollInterval, TimeUnit unit) {

			// Easy access to poller
			StatePoller<S, F> poller = StatePoller.this;

			// Do nothing if already complete
			if (this.isComplete) {
				return;
			}
			this.isComplete = true;

			// Flag the next expected poll time
			this.setNextExpectedPollTime(nextPollInterval, unit);

			// Load the failure
			try {
				// Log failure
				poller.logger.log(Level.WARNING, "Poll failure" + poller.identifierText, cause);

			} finally {
				// Poll again (if not manually triggered)
				if (!this.isManualPoll) {
					poller.pollSchedular.schedulePoll(false, nextPollInterval, unit);
				}
			}
		}

		/**
		 * Sets the next expected poll time.
		 * 
		 * @param nextPollInterval Next poll interval.
		 * @param unit             {@link TimeUnit}.
		 */
		private void setNextExpectedPollTime(long nextPollInterval, TimeUnit unit) {

			// Easy access to poller
			StatePoller<S, F> poller = StatePoller.this;

			// Flag expected next poll time
			long delay = poller.defaultPollInterval;
			long specifiedDelay = (unit != null ? unit : TimeUnit.MILLISECONDS).toMillis(nextPollInterval);
			if (specifiedDelay >= 0) {
				delay = specifiedDelay;
			}
			poller.expectedNextPollTime = (poller.clock.getTime() * 1000) + delay;
		}

		/*
		 * ================== FlowCallback ===========================
		 */

		@Override
		public synchronized void run(Throwable escalation) throws Throwable {

			// Easy access to poller
			StatePoller<S, F> poller = StatePoller.this;

			// Do nothing if already complete
			if (this.isComplete) {
				return;
			}
			this.isComplete = true;

			// Flag the next expected poll time
			this.setNextExpectedPollTime(-1, null);

			// Handle no feedback
			try {
				// Provide log of possible error
				if (poller.logger.isLoggable(Level.WARNING)) {
					if (escalation != null) {
						poller.logger.log(Level.WARNING, "Poll process failed" + poller.identifierText, escalation);
					} else {
						poller.logger.log(Level.WARNING,
								"Poll process completed" + poller.identifierText + " without providing state");
					}
				}

			} finally {
				// Poll again (by default poll interval if not manually triggered)
				if (!this.isManualPoll) {
					poller.pollSchedular.schedulePoll(false, -1L, null);
				}
			}
		}
	}

}
