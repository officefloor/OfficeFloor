/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.api.execute;

import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Provides means for a {@link ManagedObjectSource} to handle a
 * {@link Throwable} that is not handled by the {@link EscalationProcedure} of
 * the {@link Flow}.
 * <p>
 * An example of this would be a HTTP server {@link ManagedObjectSource} that
 * would send a status 500 on a {@link Throwable}.
 * 
 * @author Daniel
 */
public interface EscalationHandler {

	/**
	 * Handles the {@link Throwable}.
	 * 
	 * @param escalation
	 *            Escalation.
	 * @throws Throwable
	 *             If fails to handle {@link Escalation}.
	 */
	void handleEscalation(Throwable escalation) throws Throwable;

}