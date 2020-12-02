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

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * Builder of a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamBuilder<TS extends TeamSource> {

	/**
	 * Specifies the {@link Team} size (typically being the maximum number of
	 * {@link Thread} instances within the {@link Team}).
	 * 
	 * @param teamSize {@link Team} size.
	 */
	void setTeamSize(int teamSize);

	/**
	 * <p>
	 * Requests to the {@link Executive} that there be no {@link TeamOversight} for
	 * the {@link Team}.
	 * <p>
	 * Respecting this request is {@link Executive} implementation specific.
	 */
	void requestNoTeamOversight();

	/**
	 * Specifies a property for the {@link TeamSource}.
	 * 
	 * @param name  Name of property.
	 * @param value Value of property.
	 */
	void addProperty(String name, String value);

}
