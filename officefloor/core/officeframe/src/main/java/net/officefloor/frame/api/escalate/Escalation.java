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
package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.administration.Duty;

/**
 * <p>
 * An escalation is &quot;thrown&quot; (escalated) on processing of {@link ManagedFunction}
 * and {@link Duty} instances.
 * <p>
 * They are not &quot;thrown&quot; in the sense of a java {@link Throwable}
 * though act similar by the execution path looking upwards in the {@link Flow}
 * for an {@link EscalationFlow}/{@link EscalationHandler} to handle it.
 * <p>
 * Typically they are escalated on timing out of processing.
 * <p>
 * {@link Escalation} extends {@link Throwable} so that generic handlers can be
 * written to handle all types of failures - rather than having to have separate
 * handling for {@link Throwable} and {@link Escalation}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class Escalation extends Throwable {

	/**
	 * Default constructor.
	 */
	public Escalation() {
		super();
	}

	/**
	 * Allows for a cause of the {@link Escalation}.
	 * 
	 * @param cause
	 *            Cause of the {@link Escalation}.
	 */
	public Escalation(Throwable cause) {
		super(cause);
	}

}
