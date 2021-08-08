/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * Configuration of a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamConfiguration<TS extends TeamSource> {

	/**
	 * Obtains the name of the {@link Team}.
	 * 
	 * @return Name of the {@link Team}.
	 */
	String getTeamName();

	/**
	 * Obtains the size of the {@link Team}.
	 * 
	 * @return {@link Team} size.
	 */
	int getTeamSize();

	/**
	 * Indicates if requested no {@link TeamOversight}.
	 * 
	 * @return <cod>true</code> to request no {@link TeamOversight}.
	 */
	boolean isRequestNoTeamOversight();

	/**
	 * Obtains the {@link TeamSource} instance to use.
	 * 
	 * @return {@link TeamSource} instance to use. This may be <code>null</code> and
	 *         therefore the {@link #getTeamSourceClass()} should be used to obtain
	 *         the {@link TeamSource}.
	 */
	TS getTeamSource();

	/**
	 * Obtains the {@link Class} of the {@link TeamSource}.
	 * 
	 * @return {@link Class} of the {@link TeamSource}.
	 */
	Class<TS> getTeamSourceClass();

	/**
	 * Obtains the {@link SourceProperties} for initialising the {@link TeamSource}.
	 * 
	 * @return {@link SourceProperties} for initialising the {@link TeamSource}.
	 */
	SourceProperties getProperties();

}
