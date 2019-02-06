package net.officefloor.plugin.managedobject.poll;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
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
	 * Creates a {@link Builder} for {@link Flow} key.
	 * 
	 * @param stateType            State type.
	 * @param flowKey              {@link Flow} key to use to invoke the polling
	 *                             {@link ProcessState} from the
	 *                             {@link ManagedObjectExecuteContext}.
	 * @param executeContext       {@link ManagedObjectExecuteContext}.
	 * @param managedObjectFactory Factory to create the {@link ManagedObject}.
	 * @return {@link Builder}.
	 */
	public static <S, F extends Enum<F>> Builder<S, F> builder(Class<S> stateType, F flowKey,
			ManagedObjectExecuteContext<F> executeContext,
			Function<StatePollContext<S>, ManagedObject> managedObjectFactory) {
		if (flowKey == null) {
			throw new IllegalArgumentException("Must provide flow key");
		}
		return new Builder<S, F>(stateType, flowKey, -1, executeContext, managedObjectFactory, null, null);
	}

	/**
	 * Creates a {@link Builder} for {@link Flow} key.
	 * 
	 * @param stateType            State type.
	 * @param flowIndex            {@link Flow} index to use to invoke the polling
	 *                             {@link ProcessState} from the
	 *                             {@link ManagedObjectExecuteContext}.
	 * @param executeContext       {@link ManagedObjectExecuteContext}.
	 * @param managedObjectFactory Factory to create the {@link ManagedObject}.
	 * @return {@link Builder}.
	 */
	public static <S> Builder<S, Indexed> builder(Class<S> stateType, int flowIndex,
			ManagedObjectExecuteContext<Indexed> executeContext,
			Function<StatePollContext<S>, ManagedObject> managedObjectFactory) {
		if (flowIndex < 0) {
			throw new IllegalArgumentException("Must provide valid flow index (provided " + flowIndex + ")");
		}
		return new Builder<S, Indexed>(stateType, null, flowIndex, executeContext, managedObjectFactory, null, null);
	}

	/**
	 * <p>
	 * Creates a {@link Builder} for custom {@link Poller}.
	 * <p>
	 * This allows for higher level types that rely on but do not expose the
	 * {@link ManagedObjectSource} to use this {@link StatePoller}.
	 * 
	 * @param stateType   State type.
	 * @param initialiser {@link Initialiser}.
	 * @param poller      {@link Poller}.
	 * @return {@link Builder}.
	 */
	public static <S> Builder<S, None> builder(Class<S> stateType, Initialiser<S> initialiser, Poller<S> poller) {
		return new Builder<S, None>(stateType, null, -1, null, null, initialiser, poller);
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
		 * Factory to create the {@link ManagedObject}.
		 */
		private final Function<StatePollContext<S>, ManagedObject> managedObjectFactory;

		/**
		 * {@link Initialiser}.
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
		 * {@link Level} to log success poll messages.
		 */
		private Level successLogLevel = Level.INFO;

		/**
		 * {@link Logger} to use for logging.
		 */
		private Logger logger;

		/**
		 * Optional factory to create a parameter for the invoked {@link ProcessState}
		 * of the {@link ManagedObjectExecuteContext}.
		 */
		private Function<StatePollContext<S>, Object> parameterFactory = null;

		/**
		 * Instantiate.
		 * 
		 * @param stateType            State type.
		 * @param flowKey              {@link Flow} key.
		 * @param flowIndex            {@link Flow} index.
		 * @param executeContext       {@link ManagedObjectExecuteContext}.
		 * @param managedObjectFactory Factory to create the {@link ManagedObject}.
		 * @param initialiser          {@link Initialiser}.
		 * @param customPoller         Custom {@link Poller}.
		 */
		private Builder(Class<S> stateType, F flowKey, int flowIndex, ManagedObjectExecuteContext<F> executeContext,
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
			this.flowKey = flowKey;
			this.flowIndex = flowIndex;
			this.executeContext = executeContext;
			this.managedObjectFactory = managedObjectFactory;
			this.customInitialiser = initialiser;
			this.customPoller = customPoller;

			// Default logging
			this.logger(null);
			this.successLogLevel(null);
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
			this.logger = logger != null ? logger : Logger.getLogger(StatePoller.class.getName());
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
		 * Builds the {@link StatePoller}.
		 * 
		 * @return {@link StatePoller}.
		 */
		public StatePoller<S, F> build() {

			// Obtain the poller
			Initialiser<S> initialiser = this.customInitialiser;
			Poller<S> poller = this.customPoller;
			if (poller == null) {

				// Ensure have parameter factory
				final Function<StatePollContext<S>, Object> parameterFactory = (this.parameterFactory != null)
						? this.parameterFactory
						: (context) -> null;

				// No custom poller, so configure to flow key/index
				if (this.flowKey != null) {

					// Flow key initialiser
					initialiser = (context, callback) -> {
						Object parameter = parameterFactory.apply(context);
						ManagedObject managedObject = this.managedObjectFactory.apply(context);
						this.executeContext.registerStartupProcess(this.flowKey, parameter, managedObject, callback);
					};

					// Flow key poller
					poller = (delay, context, callback) -> {
						Object parameter = parameterFactory.apply(context);
						ManagedObject managedObject = this.managedObjectFactory.apply(context);
						this.executeContext.invokeProcess(this.flowKey, parameter, managedObject, delay, callback);
					};

				} else {

					// Flow index initialiser
					initialiser = (context, callback) -> {
						Object parameter = parameterFactory.apply(context);
						ManagedObject managedObject = this.managedObjectFactory.apply(context);
						this.executeContext.registerStartupProcess(this.flowIndex, parameter, managedObject, callback);
					};

					// Flow index poller
					poller = (delay, context, callback) -> {
						Object parameter = parameterFactory.apply(context);
						ManagedObject managedObject = this.managedObjectFactory.apply(context);
						this.executeContext.invokeProcess(this.flowIndex, parameter, managedObject, delay, callback);
					};
				}
			}

			// Obtain the identifier text
			String identifierText = this.identifier != null ? " for " + this.identifier : "";

			// Create and return the poller
			return new StatePoller<>(this.stateType, identifierText, initialiser, poller, this.successLogLevel,
					this.logger, this.defaultPollInterval);
		}
	}

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
	 * Instantiate.
	 *
	 * @param stateType           State type.
	 * @param identifierText      Identifier text.
	 * @param initialiser         {@link Initialiser}.
	 * @param poller              {@link Poller}.
	 * @param successLogLevel     Success log {@link Level}.
	 * @param logger              {@link Logger}.
	 * @param defaultPollInterval Default poll interval in milliseconds.
	 */
	private StatePoller(Class<S> stateType, String identifierText, Initialiser<S> initialiser, Poller<S> poller,
			Level successLogLevel, Logger logger, long defaultPollInterval) {
		this.logger = logger;
		this.stateType = stateType;
		this.identifierText = identifierText;
		this.defaultPollInterval = defaultPollInterval;

		// Invoke the start up poll
		if (initialiser != null) {
			StatePollContextImpl startupContext = new StatePollContextImpl(false);
			initialiser.initialise(startupContext, startupContext);
		}

		// Create the date time formatter
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.systemDefault());

		// Create invoke for new process
		this.pollSchedular = (isManualPoll, nextPollInterval, unit) -> {
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
				if (nextPollInterval >= 1) {
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
		InitialisedState initialised = this.initialised;
		return (initialised != null) ? initialised.state : null;
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

		// Ensure initialised
		if (this.initialised == null) {

			// Wait until initialised or times out
			long millisecondsToWait = unit.toMillis(timeout);
			long endTime = System.currentTimeMillis() + millisecondsToWait;
			long waitTime = Math.max(100, millisecondsToWait / 10);
			synchronized (this) {
				while (this.initialised == null) {

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
				}
			}
		}

		// Return the initialised value
		return this.initialised.state;
	}

	/**
	 * Manually trigger poll.
	 */
	public void poll() {
		this.pollSchedular.schedulePoll(true, 0L, null);
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
		public synchronized void setNextState(S nextState, long nextPollInterval, TimeUnit unit) {

			// Ensure correct type
			if ((nextState != null) && (!StatePoller.this.stateType.isAssignableFrom(nextState.getClass()))) {
				throw new IllegalArgumentException("Invalid state type " + nextState.getClass().getName()
						+ " (required " + StatePoller.this.stateType.getName() + ")");
			}

			// Do nothing if already complete
			if (this.isComplete) {
				return;
			}
			this.isComplete = true;

			// Load the state
			try {
				// Ensure initialised
				InitialisedState initialised = StatePoller.this.initialised;
				if (initialised == null) {

					// Load initial state (notifying any waiting get state)
					synchronized (StatePoller.this) {
						initialised = new InitialisedState(nextState);
						StatePoller.this.initialised = initialised;
						StatePoller.this.notifyAll();
					}

				} else {
					// Update to next state
					initialised.state = nextState;
				}

			} finally {
				// Poll again (if not manually triggered)
				if (!this.isManualPoll) {
					StatePoller.this.pollSchedular.schedulePoll(false, nextPollInterval, unit);
				}
			}
		}

		@Override
		public synchronized void setFailure(Throwable cause, long nextPollInterval, TimeUnit unit) {

			// Do nothing if already complete
			if (this.isComplete) {
				return;
			}
			this.isComplete = true;

			// Load the failure
			try {
				// Log failure
				StatePoller.this.logger.log(Level.WARNING, "Poll failure" + StatePoller.this.identifierText, cause);

			} finally {
				// Poll again (if not manually triggered)
				if (!this.isManualPoll) {
					StatePoller.this.pollSchedular.schedulePoll(false, nextPollInterval, unit);
				}
			}
		}

		/*
		 * ================== FlowCallback ===========================
		 */

		@Override
		public synchronized void run(Throwable escalation) throws Throwable {

			// Do nothing if already complete
			if (this.isComplete) {
				return;
			}
			this.isComplete = true;

			// Handle no feedback
			try {
				// Provide log of possible error
				if (StatePoller.this.logger.isLoggable(Level.WARNING)) {
					if (escalation != null) {
						StatePoller.this.logger.log(Level.WARNING,
								"Poll process failed" + StatePoller.this.identifierText, escalation);
					} else {
						StatePoller.this.logger.log(Level.WARNING, "Poll process completed"
								+ StatePoller.this.identifierText + " without providing state");
					}
				}

			} finally {
				// Poll again (by default poll interval if not manually triggered)
				if (!this.isManualPoll) {
					StatePoller.this.pollSchedular.schedulePoll(false, -1L, null);
				}
			}
		}
	}

}