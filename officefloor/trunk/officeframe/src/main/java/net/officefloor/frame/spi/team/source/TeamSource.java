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
package net.officefloor.frame.spi.team.source;

import net.officefloor.frame.spi.team.Team;

/**
 * Source to obtain {@link Team} instances.
 * 
 * @author Daniel
 */
public interface TeamSource {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	TeamSourceSpecification getSpecification();

	/**
	 * Initialises and configures the {@link TeamSource}.
	 * 
	 * @param context
	 *            {@link TeamSourceContext}.
	 * @throws Exception
	 *             If fails to configure the {@link TeamSource}.
	 */
	void init(TeamSourceContext context) throws Exception;

	/**
	 * Creates the {@link Team}.
	 * 
	 * @return {@link Team}.
	 */
	Team createTeam();
}
