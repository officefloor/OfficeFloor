/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.building.console;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.process.ManagedProcess;

/**
 * Help {@link OfficeFloorCommandParameter}.
 * 
 * @author Daniel Sagenschneider
 */
public class HelpOfficeFloorCommandFactory implements OfficeFloorCommandFactory {

	/**
	 * Delegate {@link OfficeFloorCommandFactory} for single
	 * {@link OfficeFloorCommand}.
	 */
	private final OfficeFloorCommandFactory delegate;

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link OfficeFloorCommandFactory} for single
	 *            {@link OfficeFloorCommand}.
	 */
	public HelpOfficeFloorCommandFactory(OfficeFloorCommandFactory delegate) {
		this.delegate = delegate;
	}

	/*
	 * =============== OfficeFloorCommandFactory ===================
	 */

	@Override
	public String getCommandName() {
		if (this.delegate != null) {
			// Single command (so use help flags)
			return this.delegate.getCommandName();
		} else {
			// Multiple commands (so use help command)
			return OfficeFloorConsole.COMMAND_HELP;
		}
	}

	@Override
	public OfficeFloorCommand createCommand() {
		if (this.delegate != null) {
			// Single command
			return new HelpOfficeFloorCommand(this.delegate.createCommand());
		} else {
			// Multiple commands
			return new HelpOfficeFloorCommand(null);
		}
	}

	/**
	 * Help {@link OfficeFloorCommand}.
	 */
	private class HelpOfficeFloorCommand implements OfficeFloorCommand {

		/**
		 * Delegate for single {@link OfficeFloorCommand}.
		 */
		private final OfficeFloorCommand delegate;

		/**
		 * Delegate {@link OfficeFloorCommandParameter} instances.
		 */
		private final OfficeFloorCommandParameter[] delegateParameters;

		/**
		 * {@link HelpOfficeFloorCommandParameter}.
		 */
		private final HelpOfficeFloorCommandParameter helpParameter;

		/**
		 * Initiate for new instance of {@link OfficeFloorCommand}.
		 * 
		 * @param delegate
		 *            Delegate for single {@link OfficeFloorCommand}.
		 */
		private HelpOfficeFloorCommand(OfficeFloorCommand delegate) {
			this.delegate = delegate;

			// Provide help parameter if single command
			if (this.delegate != null) {
				// Single command (with help flags)
				this.helpParameter = new HelpOfficeFloorCommandParameter();

				// Add help flags for delegate command
				OfficeFloorCommandParameter[] parameters = this.delegate
						.getParameters();
				this.delegateParameters = new OfficeFloorCommandParameter[parameters.length + 1];
				System.arraycopy(parameters, 0, this.delegateParameters, 0,
						parameters.length);
				this.delegateParameters[parameters.length] = this.helpParameter;

			} else {
				// Multiple commands
				this.helpParameter = null;
				this.delegateParameters = null;
			}
		}

		/*
		 * ================== OfficeFloorCommand =======================
		 */

		@Override
		public String getDescription() {
			if (this.delegate != null) {
				// Single command so provide the command's description
				return this.delegate.getDescription();
			} else {
				// Multiple commands (so describe the help command)
				return "This help message";
			}
		}

		@Override
		public OfficeFloorCommandParameter[] getParameters() {
			if (this.delegate != null) {
				// Single command (include help flags)
				return this.delegateParameters;
			} else {
				// Multiple commands (own command so no parameters)
				return new OfficeFloorCommandParameter[0];
			}
		}

		@Override
		public void initialiseEnvironment(OfficeFloorCommandContext context)
				throws Exception {
			if (this.delegate != null) {
				// Single command (ensure delegate is initialised)
				this.delegate.initialiseEnvironment(context);
			}
		}

		@Override
		public ManagedProcess createManagedProcess(
				OfficeFloorCommandEnvironment environment) throws Exception {
			if (this.delegate != null) {
				// Single command (check if require help)
				if (this.helpParameter.isRequireHelp) {
					// Requiring help for command
					return new HelpManagedProcess();
				} else {
					// No help required so execute single command
					return this.delegate.createManagedProcess(environment);
				}
			} else {
				// Help command requested on multiple commands
				return new HelpManagedProcess();
			}
		}
	}

	/**
	 * Help {@link OfficeFloorCommandParameter}.
	 */
	private static class HelpOfficeFloorCommandParameter implements
			OfficeFloorCommandParameter {

		/**
		 * Flag if help required.
		 */
		public boolean isRequireHelp = false;

		/*
		 * =================== OfficeFloorCommandParameter ==============
		 */

		@Override
		public String getName() {
			return OfficeFloorConsole.COMMAND_HELP;
		}

		@Override
		public String getShortName() {
			return OfficeFloorConsole.COMMAND_HELP.substring(0, 1);
		}

		@Override
		public String getDescription() {
			return "This help message";
		}

		@Override
		public boolean isRequireValue() {
			return false; // flag
		}

		@Override
		public void addValue(String value) {
			// Flagged to require help
			this.isRequireHelp = true;
		}
	}

}