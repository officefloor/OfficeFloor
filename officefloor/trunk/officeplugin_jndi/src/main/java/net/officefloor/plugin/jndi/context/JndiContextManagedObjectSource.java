/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.jndi.context;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} to provide a JNDI {@link Context}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiContextManagedObjectSource extends
		AbstractManagedObjectSource<None, None> {

	/**
	 * Property specifying the sub {@link Context}. Providing this property is
	 * optional.
	 */
	public static final String PROPERTY_SUB_CONTEXT_NAME = "jndi.sub.context";

	/**
	 * {@link Properties} for creating the {@link Context}.
	 */
	private Properties properties;

	/**
	 * <p>
	 * Name of the sub {@link Context} to return.
	 * <p>
	 * May be <code>null</code> to return the {@link InitialContext} - however
	 * clients should not program against {@link InitialContext} as a
	 * {@link SynchronisedContext} is always returned.
	 */
	private String subContextName = null;

	/*
	 * ==================== ManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required properties
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Provide meta-data
		context.setManagedObjectClass(JndiContextManagedObject.class);
		context.setObjectClass(Context.class);

		// Obtain the properties
		this.properties = mosContext.getProperties();

		// Obtain the sub context name (ensuring not blank)
		this.subContextName = this.properties
				.getProperty(PROPERTY_SUB_CONTEXT_NAME);
		if ((this.subContextName == null)
				|| (this.subContextName.trim().length() == 0)) {
			// No/blank sub context name
			this.subContextName = null;
		}

		// Obtain the Managed Object to validate Context
		this.getManagedObject();
	}

	@Override
	protected ManagedObject getManagedObject() throws Exception {

		// Obtain the InitialContext
		Context context = new InitialContext(this.properties);

		// Obtain the sub context (if required)
		if (this.subContextName != null) {
			context = (Context) context.lookup(this.subContextName);
		}

		// Create and return the Managed Object
		return new JndiContextManagedObject(new SynchronisedContext(context));
	}

	/**
	 * {@link ManagedObject} for the JNDI {@link Context}.
	 */
	private class JndiContextManagedObject implements ManagedObject {

		/**
		 * {@link SynchronisedContext}.
		 */
		private final SynchronisedContext context;

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link SynchronisedContext}.
		 */
		public JndiContextManagedObject(SynchronisedContext context) {
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