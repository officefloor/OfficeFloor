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
package net.officefloor.plugin.woof.template;

import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;
import net.officefloor.web.state.HttpTemplateAutoWireSectionExtension;
import net.officefloor.web.state.HttpTemplateSection;

/**
 * Source that allows extending behaviour of a
 * {@link HttpTemplateAutoWireSectionExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionSource {

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
	WoofTemplateExtensionSourceSpecification getSpecification();

	/**
	 * <p>
	 * This is only invoked by the WoOF editor to enable managing configuration
	 * for the {@link WoofTemplateExtensionSource}. It is not used during
	 * extension of the {@link HttpTemplateSection}.
	 * <p>
	 * This method is to create a potential {@link Change} to the configuration
	 * necessary for the {@link WoofTemplateExtensionSource}. Should no
	 * {@link Change} be required it should return <code>null</code>.
	 * <p>
	 * {@link WoofTemplateExtensionSource} implementations may require
	 * configuration by extra files within the application. This method allows
	 * the {@link WoofTemplateExtensionSource} to create/update/delete the files
	 * within the {@link ConfigurationContext} (i.e. Java raw source project).
	 * <p>
	 * Note that all actions must be undertaken by the returned {@link Change}
	 * as this method may be invoked to validate configuration. This is to avoid
	 * side effects by the WoOF editor.
	 * <p>
	 * Should configuration of the {@link WoofTemplateExtensionSource} be
	 * invalid, this method should return a {@link Change} with a
	 * {@link Conflict} instance explaining the reason the configuration is
	 * invalid.
	 * 
	 * @param context
	 *            {@link WoofTemplateExtensionChangeContext}.
	 * @return {@link Change} or <code>null</code> if no change is necessary.
	 */
	Change<?> createConfigurationChange(
			WoofTemplateExtensionChangeContext context);

	/**
	 * Extends the {@link HttpTemplateSection}.
	 * 
	 * @param context
	 *            {@link WoofTemplateExtensionSourceContext}.
	 * @throws Exception
	 *             If fails to extend the {@link HttpTemplateSection}.
	 */
	void extendTemplate(WoofTemplateExtensionSourceContext context)
			throws Exception;

}