/*-
 * #%L
 * net.officefloor.gef.woof.tests
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

package net.officefloor.gef.woof.test;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureMethodContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Manual {@link ProcedureSourceServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManualProcedureSourceServiceFactory implements ProcedureSourceServiceFactory, ProcedureSource {

	public static void procedure() {
	}

	/*
	 * ===================== ProcedureSourceServiceFactory ===================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================= ProcedureSource ================================
	 */

	@Override
	public String getSourceName() {
		return "Manual";
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {
		if (MockSection.class.getName().equals(context.getResource())) {
			context.addProcedure(null);
		}
	}

	@Override
	public Method loadMethod(ProcedureMethodContext context) throws Exception {
		return this.getClass().getMethod(context.getProcedureName());
	}

}
