/*-
 * #%L
 * JDBC Persistence
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

package net.officefloor.jdbc.test;

import java.sql.Connection;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.jdbc.decorate.ConnectionDecorator;
import net.officefloor.jdbc.decorate.ConnectionDecoratorServiceFactory;

/**
 * {@link ConnectionDecorator} to validate the {@link Connection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateConnectionDecorator implements ConnectionDecoratorServiceFactory, ConnectionDecorator {

	/*
	 * ============= ConnectionDecoratorServiceFactory =========================
	 */

	@Override
	public ConnectionDecorator createService(ServiceContext context) throws Exception {
		return this;
	}

	/*
	 * ===================== ConnectionDecorator ===============================
	 */

	@Override
	public Connection decorate(Connection connection) {
		return ValidateConnections.addConnection(connection);
	}

}
