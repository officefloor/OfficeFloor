/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.security.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupProcess;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
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
		List<HttpSecurityFlowType<?>> sortedFlowTypes = Arrays.asList(this.securityType.getFlowTypes());
		Collections.sort(sortedFlowTypes, (a, b) -> a.getIndex() - b.getIndex());
		for (HttpSecurityFlowType<?> flowType : sortedFlowTypes) {
			F flowKey = (F) flowType.getKey();
			Labeller<F> labeller;
			if (flowKey != null) {
				labeller = context.addFlow(flowKey, flowType.getArgumentType());
			} else {
				labeller = context.addFlow(flowType.getArgumentType());
			}
			labeller.setLabel(flowType.getFlowName());
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
		 * {@link SafeManagedObjectService}.
		 */
		private final SafeManagedObjectService<F> servicer;

		/**
		 * Initiate.
		 * 
		 * @param context {@link ManagedObjectExecuteContext}
		 */
		private HttpSecurityExecuteContextImpl(ManagedObjectExecuteContext<F> context) {
			this.context = context;
			this.servicer = new SafeManagedObjectService<>(context);
		}

		/*
		 * ================== HttpSecurityExecuteContext ==================
		 */

		@Override
		public ManagedObjectStartupProcess registerStartupProcess(F key, Object parameter, FlowCallback callback)
				throws IllegalArgumentException {
			return this.context.invokeStartupProcess(key, parameter, HttpSecurityExecuteManagedObjectSource.this,
					callback);
		}

		@Override
		public ProcessManager invokeProcess(F key, Object parameter, long delay, FlowCallback callback)
				throws IllegalArgumentException {
			return this.servicer.invokeProcess(key, parameter, HttpSecurityExecuteManagedObjectSource.this, delay,
					callback);
		}
	}

}
