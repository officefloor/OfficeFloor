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
package net.officefloor.woof.model.woof;

import java.util.Set;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.web.template.section.WebTemplateSectionSource;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputModel;

/**
 * Provides inheritance information regarding a {@link WoofTemplateModel} being
 * a super {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateInheritance {

	/**
	 * Obtains the super {@link WoofTemplateModel} this
	 * {@link WoofTemplateInheritance} represents.
	 * 
	 * @return Super {@link WoofTemplateModel} this
	 *         {@link WoofTemplateInheritance} represents.
	 */
	WoofTemplateModel getSuperTemplate();

	/**
	 * <p>
	 * Obtains the inheritance hierarchy for the particular super
	 * {@link WoofTemplateModel}.
	 * <p>
	 * The first entry is the super {@link WoofTemplateModel} followed by its
	 * parent {@link WoofTemplateModel} and so forth until the highest
	 * {@link WoofTemplateModel} in the inheritance hierarchy.
	 * 
	 * @return Inheritance hierarchy for the particular super
	 *         {@link WoofTemplateModel}.
	 */
	WoofTemplateModel[] getInheritanceHierarchy();

	/**
	 * Convenience method to provide the
	 * {@link WebTemplateSectionSource#PROPERTY_INHERITED_TEMPLATES}
	 * {@link Property} value.
	 * 
	 * @return Inherited template paths {@link Property} value.
	 */
	String getInheritedTemplatePathsPropertyValue();

	/**
	 * <p>
	 * Obtains the names of all the inherited {@link WoofTemplateOutputModel}
	 * instances.
	 * <p>
	 * The {@link WoofTemplateOutputModel} names provided do not need to be
	 * configured as they can inherit their configuration.
	 * 
	 * @return Names of all the inherited {@link WoofTemplateOutputModel}
	 *         instances.
	 */
	Set<String> getInheritedWoofTemplateOutputNames();

}