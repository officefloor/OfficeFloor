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
	 * Specifies the label for this configuration.
	 * 
	 * @param title
	 *            Title for this configuration.
	 * @return <code>this</code>.
	 */
	ConfigurationBuilder<M> title(String title);

	/**
	 * Validates the model.
	 * 
	 * @param validator
	 *            {@link ValueValidator}.
	 */
	void validate(ValueValidator<M, M> validator);

	/**
	 * Specifies the {@link ErrorListener}.
	 * 
	 * @param errorListener
	 *            {@link ErrorListener}.
	 */
	void error(ErrorListener errorListener);

	/**
	 * Configures a {@link Consumer} to apply the configured model.
	 * 
	 * @param label
	 *            Label for the applying {@link Actioner}.
	 * @param applier
	 *            {@link Consumer} to apply the configured model.
	 */
	void apply(String label, Consumer<M> applier);

	/**
	 * Specifies the {@link CloseListener}.
	 * 
	 * @param closeListener
	 *            {@link CloseListener}.
	 */
	void close(CloseListener closeListener);

}