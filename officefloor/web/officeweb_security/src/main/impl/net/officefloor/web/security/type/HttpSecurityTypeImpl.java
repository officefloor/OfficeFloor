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

package net.officefloor.web.security.type;

import java.io.Serializable;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.web.spi.security.HttpAccessControlFactory;
import net.officefloor.web.spi.security.HttpAuthenticationFactory;

/**
 * {@link HttpSecurityType} adapted from the {@link ManagedObjectType}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityTypeImpl<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>>
		implements HttpSecurityType<A, AC, C, O, F> {

	/**
	 * Authentication type.
	 */
	private final Class<A> authenticationType;

	/**
	 * {@link HttpAuthenticationFactory}.
	 */
	private final HttpAuthenticationFactory<A, C> httpAuthenticationFactory;

	/**
	 * Access control type.
	 */
	private final Class<AC> accessControlType;

	/**
	 * {@link HttpAccessControlFactory}.
	 */
	private final HttpAccessControlFactory<AC> httpAccessControlFactory;

	/**
	 * Credentials type.
	 */
	private final Class<C> credentialsType;

	/**
	 * {@link ManagedObjectType}.
	 */
	private final ManagedObjectType<O> moAccessControlType;

	/**
	 * {@link HttpSecuritySupportingManagedObjectType} instances.
	 */
	private final HttpSecuritySupportingManagedObjectType<?>[] supportingManagedObjectTypes;

	/**
	 * Initiate.
	 * 
	 * @param authenticationType           Authentication type.
	 * @param httpAuthenticationFactory    {@link HttpAccessControlFactory}.
	 * @param moAccessControlType          {@link ManagedObjectType}.
	 * @param httpAccessControlFactory     {@link HttpAccessControlFactory}.
	 * @param credentialsType              Credentials type.
	 * @param supportingManagedObjectTypes {@link HttpSecuritySupportingManagedObjectType}
	 *                                     instances.
	 */
	public HttpSecurityTypeImpl(Class<A> authenticationType, HttpAuthenticationFactory<A, C> httpAuthenticationFactory,
			Class<AC> accessControlType, HttpAccessControlFactory<AC> httpAccessControlFactory,
			Class<C> credentialsType, ManagedObjectType<O> moAccessControlType,
			HttpSecuritySupportingManagedObjectType<?>[] supportingManagedObjectTypes) {
		this.authenticationType = authenticationType;
		this.httpAuthenticationFactory = httpAuthenticationFactory;
		this.accessControlType = accessControlType;
		this.httpAccessControlFactory = httpAccessControlFactory;
		this.credentialsType = credentialsType;
		this.moAccessControlType = moAccessControlType;
		this.supportingManagedObjectTypes = supportingManagedObjectTypes;
	}

	/*
	 * ================= HttpSecurityType =====================
	 */

	@Override
	public Class<A> getAuthenticationType() {
		return this.authenticationType;
	}

	@Override
	public HttpAuthenticationFactory<A, C> getHttpAuthenticationFactory() {
		return this.httpAuthenticationFactory;
	}

	@Override
	public Class<AC> getAccessControlType() {
		return this.accessControlType;
	}

	@Override
	public HttpAccessControlFactory<AC> getHttpAccessControlFactory() {
		return this.httpAccessControlFactory;
	}

	@Override
	public Class<C> getCredentialsType() {
		return this.credentialsType;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HttpSecurityDependencyType<O>[] getDependencyTypes() {
		return AdaptFactory.adaptArray(this.moAccessControlType.getDependencyTypes(), HttpSecurityDependencyType.class,
				new AdaptFactory<HttpSecurityDependencyType, ManagedObjectDependencyType<O>>() {
					@Override
					public HttpSecurityDependencyType<O> createAdaptedObject(ManagedObjectDependencyType<O> delegate) {
						return new HttpSecurityDependencyTypeImpl<O>(delegate);
					}
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HttpSecurityFlowType<F>[] getFlowTypes() {
		return AdaptFactory.adaptArray(this.moAccessControlType.getFlowTypes(), HttpSecurityFlowType.class,
				new AdaptFactory<HttpSecurityFlowType, ManagedObjectFlowType>() {
					@Override
					public HttpSecurityFlowType createAdaptedObject(ManagedObjectFlowType delegate) {
						return new HttpSecurityFlowTypeImpl<F>(delegate);
					}
				});
	}

	@Override
	public HttpSecuritySupportingManagedObjectType<?>[] getSupportingManagedObjectTypes() {
		return this.supportingManagedObjectTypes;
	}
}
