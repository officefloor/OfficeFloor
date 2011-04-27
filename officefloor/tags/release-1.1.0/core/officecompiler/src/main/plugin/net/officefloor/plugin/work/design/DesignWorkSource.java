/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.work.design;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.WorkModel;

/**
 * <p>
 * {@link WorkSource} that enables designing a {@link WorkModel} to be later
 * replaced with an actual working {@link WorkSource}.
 * <p>
 * This enables a {@link DeskModel} to be drawn up before the required
 * {@link WorkSource} is available. In other words, top down design.
 * 
 * @author Daniel Sagenschneider
 */
public class DesignWorkSource extends AbstractWorkSource<Work> {

	/**
	 * <p>
	 * Transforms the {@link WorkType} into a {@link PropertyList} that this
	 * {@link DesignWorkSource} can use to source the {@link WorkType} again.
	 * <p>
	 * The {@link WorkType} however has had the {@link WorkFactory} and
	 * {@link TaskFactory} substituted so the {@link WorkType} can not actually
	 * be used.
	 * 
	 * @param workType
	 *            {@link WorkType}.
	 * @return {@link PropertyList} containing the information to reconstruct
	 *         the {@link WorkType}.
	 */
	public PropertyList transformToProperties(WorkType<?> workType) {
		// TODO implement turning WorkType into properties
		throw new UnsupportedOperationException(
				"TODO implement turning WorkType into properties");
	}

	/*
	 * =================== AbstractWorkSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification as designing
	}

	@Override
	public void sourceWork(WorkTypeBuilder<Work> workTypeBuilder,
			WorkSourceContext context) throws Exception {
		// TODO implement obtaining WorkType from properties
		throw new UnsupportedOperationException(
				"TODO implement obtaining work type from design properties");
	}

}