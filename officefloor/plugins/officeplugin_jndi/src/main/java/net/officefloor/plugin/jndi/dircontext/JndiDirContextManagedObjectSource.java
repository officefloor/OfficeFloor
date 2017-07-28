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
package net.officefloor.plugin.jndi.dircontext;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for a JNDI {@link DirContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiDirContextManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * Property to specifying the sub {@link DirContext} to return. May be
	 * <code>null</code> to return top level {@link DirContext}.
	 */
	public static final String PROPERTY_SUB_CONTEXT_NAME = "jndi.sub.context";

	/**
	 * {@link Properties} for the {@link InitialDirContext}.
	 */
	private Properties properties;

	/**
	 * <p>
	 * Name of the sub {@link DirContext} to return.
	 * <p>
	 * May be <code>null</code> to return the {@link InitialDirContext} -
	 * however clients should not program against {@link InitialContext} as a
	 * {@link SynchronisedDirContext} is always returned.
	 */
	private String subContextName = null;

	/**
	 * Obtains the {@link Properties} for the {@link InitialDirContext}.
	 * 
	 * @param context
	 *            {@link ManagedObjectSourceContext}.
	 * @return {@link Properties}.
	 * @throws Exception
	 *             If fails to obtain the {@link Properties}.
	 */
	protected Properties getProperties(ManagedObjectSourceContext<None> context) throws Exception {
		return context.getProperties();
	}

	/*
	 * ======================= ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required properties
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the properties for the directory context
		this.properties = this.getProperties(mosContext);

		// Obtain the sub context name
		this.subContextName = mosContext.getProperty(PROPERTY_SUB_CONTEXT_NAME, null);

		// Load the meta-data
		context.setObjectClass(DirContext.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {

		// Create the directory context
		DirContext context = new InitialDirContext(this.properties);

		// Obtain the sub directory
		if (this.subContextName != null) {
			context = (DirContext) context.lookup(this.subContextName);
		}

		// Create the managed object
		return new JndiDirContextManagedObject(new SynchronisedDirContext(context));
	}

	/**
	 * {@link ManagedObject} for the JNDI {@link DirContext}.
	 */
	private class JndiDirContextManagedObject implements ManagedObject {

		/**
		 * {@link SynchronisedDirContext}.
		 */
		private final SynchronisedDirContext context;

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link SynchronisedDirContext}.
		 */
		public JndiDirContextManagedObject(SynchronisedDirContext context) {
			this.context = context;
		}

		/*
		 * ======================= ManagedObject ============================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this.context;
		}
	}

}