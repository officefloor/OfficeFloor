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
package net.officefloor.compile;

import java.util.ServiceLoader;

import net.officefloor.compile.spi.section.source.SectionSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link SectionSource}
 * {@link Class} alias by including the extension {@link SectionSource} jar on
 * the class path.
 * <p>
 * {@link OfficeFloorCompiler#addSectionSourceAlias(String, Class)} will be
 * invoked for each found {@link SectionSourceService}.
 * 
 * @author Daniel
 */
public interface SectionSourceService {

	/**
	 * Obtains the alias for the {@link SectionSource} {@link Class}.
	 * 
	 * @return Alias for the {@link SectionSource} {@link Class}.
	 */
	String getSectionSourceAlias();

	/**
	 * Obtains the {@link SectionSource} {@link Class}.
	 * 
	 * @return {@link SectionSource} {@link Class}.
	 */
	Class<? extends SectionSource> getSectionSourceClass();

}