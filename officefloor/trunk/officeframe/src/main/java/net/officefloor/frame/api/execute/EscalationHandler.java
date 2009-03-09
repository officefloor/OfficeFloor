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

/**
 * Provides top level escalation handling. Provided to {@link HandlerContext} of
 * the {@link Handler} to do catch all escalation handling.
 * 
 * @author Daniel
 */
public interface EscalationHandler {

	/**
	 * Handles the top level escalation.
	 * 
	 * @param escalation
	 *            Escalation.
	 * @throws Throwable
	 *             If fails to handle top level escalation. To be exact this is
	 *             the second last level before the top level which will just
	 *             log failure to stderr.
	 */
	void handleEscalation(Throwable escalation) throws Throwable;

}