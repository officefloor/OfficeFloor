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
package net.officefloor.plugin.web.http.template.section;

import java.io.Serializable;

import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;

/**
 * Provides logic for using stateful object.
 * 
 * @author Daniel Sagenschneider
 */
public class StatefulObjectLogic {

	/**
	 * Obtains the {@link StatefulObject} for template rendering.
	 * 
	 * @param object
	 *            {@link StatefulObject}.
	 * @return {@link StatefulObject}.
	 */
	public StatefulObject getTemplate(StatefulObject object) {

		// Increment the count
		object.incrementCount();

		// Return for next value
		return object;
	}

	/**
	 * Stateful object.
	 */
	@HttpSessionStateful
	public static class StatefulObject implements Serializable {

		/**
		 * Count.
		 */
		private int count = 0;

		/**
		 * Increments the count.
		 */
		public void incrementCount() {
			this.count++;
		}

		/**
		 * Obtains the count.
		 * 
		 * @return Count.
		 */
		public int getCount() {
			return this.count;
		}
	}

	/**
	 * Required from test setup.
	 */
	@NextTask("doExternalFlow")
	public void required() {
	}

}