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

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Provides means for:
 * <ol>
 * <li>a {@link ManagedObjectSource}, or</li>
 * <li>the {@link OfficeFloor}</li>
 * </ol>
 * to handle a {@link Throwable} that is not handled by the
 * {@link EscalationProcedure} of the {@link Flow}.
 * <p>
 * An example of this would be a HTTP server {@link ManagedObjectSource} that
 * would send a status 500 on a {@link Throwable}.
 * <p>
 * Note that this does not just handle {@link Escalation} instances but any type
 * of {@link Throwable}.
 * 
 * @author Daniel Sagenschneider
 */
@Deprecated // use FlowCallback
public interface EscalationHandler {

	/**
	 * Handles the {@link Throwable}.
	 * 
	 * @param escalation
	 *            Escalation.
	 * @throws Throwable
	 *             If fails to handle {@link EscalationFlow}.
	 */
	void handleEscalation(Throwable escalation) throws Throwable;

}