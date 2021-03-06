/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.template;

import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.woof.model.woof.WoofTemplateExtension;
import net.officefloor.woof.model.woof.WoofTemplateModel;

/**
 * Configuration for a {@link WoofTemplateExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionConfiguration extends SourceProperties {

	/**
	 * Obtains the application path for the {@link WoofTemplateModel}.
	 * 
	 * @return Application path for the {@link WoofTemplateModel}.
	 */
	String getApplicationPath();

}
