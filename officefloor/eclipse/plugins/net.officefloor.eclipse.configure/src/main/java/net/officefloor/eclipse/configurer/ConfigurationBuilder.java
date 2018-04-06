/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.configurer;

import java.util.function.Consumer;

/**
 * Builds the configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConfigurationBuilder<M> extends InputBuilder<M> {

	/**
	 * Configures a {@link Consumer} to apply the configured model.
	 * 
	 * @param applier
	 *            {@link Consumer} to apply the configured model.
	 */
	void apply(Consumer<M> applier);

}