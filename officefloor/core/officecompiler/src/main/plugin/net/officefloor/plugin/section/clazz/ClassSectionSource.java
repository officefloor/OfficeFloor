/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.section.clazz;

import net.officefloor.compile.SectionSourceService;
import net.officefloor.compile.SectionSourceServiceFactory;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * <p>
 * Class {@link SectionSource}.
 * <p>
 * The implementation has been segregated into smaller methods to allow
 * overriding to re-use {@link ClassSectionSource} for other uses.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionSource extends AbstractClassSectionSource
		implements SectionSourceService<ClassSectionSource>, SectionSourceServiceFactory {

	/*
	 * ================ SectionSourceService ========================
	 */

	@Override
	public SectionSourceService<?> createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public String getSectionSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassSectionSource> getSectionSourceClass() {
		return ClassSectionSource.class;
	}

}