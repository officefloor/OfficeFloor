/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.api.executive;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;

/**
 * Context for the {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveContext extends TeamSourceContext {

	/**
	 * <p>
	 * Indicates if requesting no {@link TeamOversight}.
	 * <p>
	 * The {@link Executive} may ignore this request.
	 * 
	 * @return <code>true</code> to request no {@link TeamOversight}.
	 */
	boolean isRequestNoTeamOversight();

	/**
	 * Obtains the {@link TeamSource} to create the {@link Team}.
	 * 
	 * @return {@link TeamSource} to create the {@link Team}.
	 */
	TeamSource getTeamSource();

	/**
	 * <p>
	 * Creates a {@link ThreadFactory} for the {@link Team} name.
	 * <p>
	 * The {@link Executive} may decide to create multiple {@link Team} instances
	 * for the actual {@link Team}. This allows identifying which {@link Thread}
	 * will belong to each {@link Team}.
	 * 
	 * @param teamName Name of the {@link Team}.
	 * @return {@link ThreadFactory}.
	 */
	ThreadFactory createThreadFactory(String teamName);

}
