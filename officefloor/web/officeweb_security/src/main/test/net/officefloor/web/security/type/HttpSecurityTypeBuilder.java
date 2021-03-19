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

package net.officefloor.web.security.type;

import java.io.Serializable;

import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Builder for the {@link HttpSecurityType} to validate the loaded
 * {@link HttpSecurityType} from the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityTypeBuilder {

	/**
	 * Specifies the authentication class.
	 * 
	 * @param authenticationClass Class of the authentication.
	 */
	void setAuthenticationClass(Class<?> authenticationClass);

	/**
	 * Specifies the access control class.
	 * 
	 * @param accessControlClass Class of the access control.
	 */
	void setAccessControlClass(Class<?> accessControlClass);

	/**
	 * <p>
	 * Specifies the credentials class.
	 * <p>
	 * May be not specified if no application behaviour required to provide
	 * credentials.
	 * 
	 * @param credentialsClass Class of the credentials.
	 */
	void setCredentialsClass(Class<?> credentialsClass);

	/**
	 * Adds a {@link HttpSecurityDependencyType}.
	 * 
	 * @param name          Name of the {@link HttpSecurityDependencyType}.
	 * @param type          Type of the {@link HttpSecurityDependencyType}.
	 * @param typeQualifier Qualifier for the type of
	 *                      {@link HttpSecurityDependencyType}.
	 * @param index         Index of the {@link HttpSecurityDependencyType}.
	 * @param key           Key identifying the {@link HttpSecurityDependencyType}.
	 */
	void addDependency(String name, Class<?> type, String typeQualifier, int index, Enum<?> key);

	/**
	 * Indicates if input.
	 * 
	 * @param isInput <code>true</code> if input.
	 */
	void setInput(boolean isInput);

	/**
	 * <p>
	 * Convenience method to add a {@link HttpSecurityDependencyType} based on the
	 * key.
	 * <p>
	 * Both the <code>name</code> and <code>index</code> are extracted from the key.
	 * 
	 * @param key           Key identifying the {@link HttpSecurityDependencyType}.
	 * @param type          Type of the {@link HttpSecurityDependencyType}.
	 * @param typeQualifier Qualifier for the type of
	 *                      {@link HttpSecurityDependencyType}.
	 */
	void addDependency(Enum<?> key, Class<?> type, String typeQualifier);

	/**
	 * Adds a {@link HttpSecurityFlowType}.
	 * 
	 * @param name         Name of the {@link HttpSecurityFlowType}.
	 * @param argumentType Type of argument passed to the
	 *                     {@link HttpSecurityFlowType}.
	 * @param index        Index of the {@link HttpSecurityFlowType}.
	 * @param key          Key identifying the {@link HttpSecurityFlowType}.
	 */
	void addFlow(String name, Class<?> argumentType, int index, Enum<?> key);

	/**
	 * <p>
	 * Convenience method to add a {@link HttpSecurityFlowType} based on the key.
	 * <p>
	 * Both the <code>name</code> and <code>index</code> are extracted from the key.
	 * 
	 * @param key          Key identifying the {@link HttpSecurityFlowType}.
	 * @param argumentType Type of argument passed to the
	 *                     {@link HttpSecurityFlowType}.
	 */
	void addFlow(Enum<?> key, Class<?> argumentType);

	/**
	 * Builds the {@link HttpSecurityType}.
	 * 
	 * @param <A>  Authorization type.
	 * @param <AC> Access control type.
	 * @param <C>  Credentials type.
	 * @param <O>  Dependency keys.
	 * @param <F>  Flow keys.
	 * @return {@link HttpSecurityType}.
	 */
	<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityType<A, AC, C, O, F> build();

}
