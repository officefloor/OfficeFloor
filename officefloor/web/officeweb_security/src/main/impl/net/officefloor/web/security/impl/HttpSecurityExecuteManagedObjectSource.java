/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.impl;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupProcess;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpSecurityExecuteContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * {@link ManagedObjectSource} providing {@link HttpSecurityExecuteContext} to
 * the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpSecurityExecuteManagedObjectSource<F extends Enum<F>> extends AbstractManagedObjectSource<None, F>
		implements ManagedObject {

	/**
	 * {@link HttpSecuritySource}.
	 */
	private final HttpSecuritySource<?, ?, ?, ?, F> securitySource;

	/**
	 * {@link HttpSecurityType}.
	 */
	private final HttpSecurityType<?, ?, ?, ?, F> securityType;

	/**
	 * Instantiate.
	 * 
	 * @param securitySource {@link HttpSecuritySource}.
	 * @param securityType   {@link HttpSecurityType}.
	 */
	public HttpSecurityExecuteManagedObjectSource(HttpSecuritySource<?, ?, ?, ?, F> securitySource,
			HttpSecurityType<?, ?, ?, ?, F> securityType) {
		this.securitySource = securitySource;
		this.securityType = securityType;
	}

	/*
	 * ================== ManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// Uses type information
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void loadMetaData(MetaDataContext<None, F> context) throws Exception {
		context.setObjectClass(HttpSecurityExecuteManagedObjectSource.class);

		// Load the flows
		for (HttpSecurityFlowType<?> flowType : this.securityType.getFlowTypes()) {
			F flowKey = (F) flowType.getKey();
			context.addFlow(flowKey, flowType.getArgumentType());
		}
	}

	@Override
	public void start(ManagedObjectExecuteContext<F> context) throws Exception {

		// Start the security source
		this.securitySource.start(new HttpSecurityExecuteContextImpl(context));
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	@Override
	public void stop() {

		// Stop the security source
		this.securitySource.stop();
	}

	/*
	 * ===================== ManagedObject =====================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this;
	}

	/**
	 * {@link HttpSecurityExecuteContext} implementation.
	 */
	private class HttpSecurityExecuteContextImpl implements HttpSecurityExecuteContext<F> {

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private final ManagedObjectExecuteContext<F> context;

		/**
		 * Initiate.
		 * 
		 * @param context {@link ManagedObjectExecuteContext}
		 */
		private HttpSecurityExecuteContextImpl(ManagedObjectExecuteContext<F> context) {
			this.context = context;
		}

		/*
		 * ================== HttpSecurityExecuteContext ==================
		 */

		@Override
		public ManagedObjectStartupProcess registerStartupProcess(F key, Object parameter, FlowCallback callback)
				throws IllegalArgumentException {
			return this.context.registerStartupProcess(key, parameter, HttpSecurityExecuteManagedObjectSource.this,
					callback);
		}

		@Override
		public ProcessManager invokeProcess(F key, Object parameter, long delay, FlowCallback callback)
				throws IllegalArgumentException {
			return this.context.invokeProcess(key, parameter, HttpSecurityExecuteManagedObjectSource.this, delay,
					callback);
		}
	}

}
