package net.officefloor.plugin.managedobject.poll;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
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
		return new Builder<S, F>(stateType, flowKey, -1, executeContext, managedObjectFactory);
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
		return new Builder<S, Indexed>(stateType, null, flowIndex, executeContext, managedObjectFactory);
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
		 */
		private Builder(Class<S> stateType, F flowKey, int flowIndex, ManagedObjectExecuteContext<F> executeContext,
				Function<StatePollContext<S>, ManagedObject> managedObjectFactory) {
			if (stateType == null) {
				throw new IllegalArgumentException("Must provide state type");
			}
			if (executeContext == null) {
				throw new IllegalArgumentException("Must provide " + ManagedObjectExecuteContext.class.getSimpleName());
			}
			if (managedObjectFactory == null) {
				throw new IllegalArgumentException("Must provide " + ManagedObject.class.getSimpleName() + " factory");
			}
			this.stateType = stateType;
			this.flowKey = flowKey;
			this.flowIndex = flowIndex;
			this.executeContext = executeContext;
			this.managedObjectFactory = managedObjectFactory;

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
			this.parameterFactory = parameterFactory;
			return this;
		}

		/**
		 * Builds the {@link StatePoller}.
		 * 
		 * @return {@link StatePoller}.
		 */
		public StatePoller<S, F> build() {
			return new StatePoller<>(this.stateType, this.flowKey, this.flowIndex, this.parameterFactory,
					this.managedObjectFactory, this.executeContext, this.successLogLevel, this.logger,
					this.defaultPollInterval);
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
	 * Default poll interval in milliseconds.
	 */
	private final long defaultPollInterval;

	/**
	 * Triggers the next poll within the specified interval.
	 */
	private final BiConsumer<Long, TimeUnit> invokePoll;

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
	 * @param stateType            State type.
	 * @param flowKey              {@link Flow} key.
	 * @param flowIndex            {@link Flow} index.
	 * @param parameterFactory     {@link Flow} parameter factory.
	 * @param managedObjectFactory {@link ManagedObject} factory.
	 * @param executeContext       {@link ManagedObjectExecuteContext}.
	 * @param su
	 * @param logger               {@link Logger}.
	 */
	private StatePoller(Class<S> stateType, F flowKey, int flowIndex,
			Function<StatePollContext<S>, Object> parameterFactory,
			Function<StatePollContext<S>, ManagedObject> managedObjectFactory,
			ManagedObjectExecuteContext<F> executeContext, Level successLogLevel, Logger logger,
			long defaultPollInterval) {
		this.logger = logger;
		this.stateType = stateType;
		this.defaultPollInterval = defaultPollInterval;

		// Default the parameter factory
		final Function<StatePollContext<S>, Object> finalParameterFactory = (parameterFactory != null)
				? parameterFactory
				: (context) -> null;

		// Invoke the start up process
		StatePollContextImpl startupContext = new StatePollContextImpl();
		Object startupParameter = finalParameterFactory.apply(startupContext);
		ManagedObject startupManagedObject = managedObjectFactory.apply(startupContext);
		Consumer<Long> poller;
		if (flowKey != null) {
			// Configure for flow key
			executeContext.registerStartupProcess(flowKey, startupParameter, startupManagedObject, startupContext);
			poller = (delay) -> {
				StatePollContextImpl pollContext = new StatePollContextImpl();
				Object parameter = finalParameterFactory.apply(pollContext);
				ManagedObject managedObject = managedObjectFactory.apply(pollContext);
				executeContext.invokeProcess(flowKey, parameter, managedObject, delay, pollContext);
			};
		} else {
			// Configure for flow index
			executeContext.registerStartupProcess(flowIndex, startupParameter, startupManagedObject, startupContext);
			poller = (delay) -> {
				StatePollContextImpl pollContext = new StatePollContextImpl();
				Object parameter = finalParameterFactory.apply(pollContext);
				ManagedObject managedObject = managedObjectFactory.apply(pollContext);
				executeContext.invokeProcess(flowIndex, parameter, managedObject, delay, pollContext);
			};
		}

		// Create the date time formatter
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.systemDefault());

		// Create invoke for new process
		this.invokePoll = (nextPollInterval, unit) -> {
			long delay = this.defaultPollInterval;
			boolean isDefault = true;
			try {

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

					// Run next poll
					poller.accept(delay);

				} catch (Throwable ex) {
					// Severe issue as polling now stopped
					isFailure = true;
					this.logger.log(Level.SEVERE, "Polling has failed.  Restart is required to re-establish polling.",
							ex);
				} finally {
					// Log time of next poll (if appropriate and no failure)
					if (!isFailure && this.logger.isLoggable(successLogLevel)) {
						ZonedDateTime nextPollTime = Instant.ofEpochMilli(System.currentTimeMillis() + delay)
								.atZone(ZoneId.systemDefault());
						this.logger.log(successLogLevel, "Next poll in " + (isDefault ? "(default) " : "") + delay
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
	 * {@link StatePollContext} implementation.
	 */
	private class StatePollContextImpl implements StatePollContext<S>, FlowCallback {

		/**
		 * Flags whether complete.
		 */
		private boolean isComplete = false;

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
				// Poll again
				StatePoller.this.invokePoll.accept(nextPollInterval, unit);
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
				StatePoller.this.logger.log(Level.WARNING, "Poll failure", cause);

			} finally {
				// Poll again
				StatePoller.this.invokePoll.accept(nextPollInterval, unit);
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
						StatePoller.this.logger.log(Level.WARNING, "Poll process failed", escalation);
					} else {
						StatePoller.this.logger.log(Level.WARNING, "Poll process completed without providing state");
					}
				}

			} finally {
				// Poll again (by default poll interval)
				StatePoller.this.invokePoll.accept(-1L, null);
			}
		}
	}

}