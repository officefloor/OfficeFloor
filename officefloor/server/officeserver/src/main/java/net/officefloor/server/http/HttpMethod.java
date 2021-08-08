/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.http;

import java.io.Serializable;
import java.util.jar.Attributes.Name;

/**
 * HTTP method.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpMethod implements Serializable {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Obtains the {@link HttpMethod}.
	 * 
	 * @param methodName Name of the {@link HttpMethod}.
	 * @return {@link HttpMethod}.
	 */
	public static HttpMethod getHttpMethod(String methodName) {
		switch (methodName) {
		case "CONNECT":
			return CONNECT;
		case "DELETE":
			return DELETE;
		case "GET":
			return GET;
		case "HEAD":
			return HEAD;
		case "OPTIONS":
			return OPTIONS;
		case "PUT":
			return PUT;
		case "POST":
			return POST;
		default:
			return new HttpMethod(methodName);
		}
	}

	/**
	 * {@link HttpMethod} {@link Enum} for common HTTP methods.
	 */
	public enum HttpMethodEnum {

		/**
		 * Common {@link HttpMethod} instances.
		 */
		CONNECT("CONNECT"), DELETE("DELETE"), GET("GET"), HEAD("HEAD"), OPTIONS("OPTIONS"), PUT("PUT"), POST("POST"),

		/**
		 * Non common {@link HttpMethod}.
		 */
		OTHER(null);

		/**
		 * {@link HttpMethod}.
		 */
		private final HttpMethod httpMethod;

		/**
		 * Instantiate.
		 * 
		 * @param httpMethodName {@link Name} of the {@link HttpMethod}.
		 */
		private HttpMethodEnum(String httpMethodName) {
			this.httpMethod = new HttpMethod(httpMethodName, this);
		}

		/**
		 * <p>
		 * Obtains the singleton {@link HttpMethod} for this {@link HttpMethodEnum}.
		 * <p>
		 * Note for {@link HttpMethodEnum#OTHER} this returns <code>null</code>.
		 * 
		 * @return {@link HttpMethod}.
		 */
		public HttpMethod getHttpMethod() {
			return this.httpMethod;
		}
	}

	/**
	 * CONNECT {@link HttpMethod} singleton.
	 */
	public static HttpMethod CONNECT = HttpMethodEnum.CONNECT.getHttpMethod();

	/**
	 * DELETE {@link HttpMethod} singleton.
	 */
	public static HttpMethod DELETE = HttpMethodEnum.DELETE.getHttpMethod();

	/**
	 * GET {@link HttpMethod} singleton.
	 */
	public static HttpMethod GET = HttpMethodEnum.GET.getHttpMethod();

	/**
	 * HEAD {@link HttpMethod} singleton.
	 */
	public static HttpMethod HEAD = HttpMethodEnum.HEAD.getHttpMethod();

	/**
	 * OPTIONS {@link HttpMethod} singleton.
	 */
	public static HttpMethod OPTIONS = HttpMethodEnum.OPTIONS.getHttpMethod();

	/**
	 * PUT {@link HttpMethod} singleton.
	 */
	public static HttpMethod PUT = HttpMethodEnum.PUT.getHttpMethod();

	/**
	 * POST {@link HttpMethod} singleton.
	 */
	public static HttpMethod POST = HttpMethodEnum.POST.getHttpMethod();

	/**
	 * Name of the {@link HttpMethod}.
	 */
	private final String name;

	/**
	 * Hash code.
	 */
	private final int hashCode;

	/**
	 * {@link HttpMethodEnum}.
	 */
	private final HttpMethodEnum httpMethodEnum;

	/**
	 * Instantiate a dynamic {@link HttpMethod}.
	 * 
	 * @param name Name of the {@link HttpMethod}.
	 */
	public HttpMethod(String name) {
		this(name, HttpMethodEnum.OTHER);
	}

	/**
	 * Instantiate.
	 * 
	 * @param name           Name of the {@link HttpMethod}.
	 * @param httpMethodEnum {@link HttpMethodEnum}.
	 */
	HttpMethod(String name, HttpMethodEnum httpMethodEnum) {
		this.name = name;
		this.hashCode = (this.name == null ? "".hashCode() : this.name.hashCode());
		this.httpMethodEnum = httpMethodEnum;
	}

	/**
	 * Equals without the type checking.
	 * 
	 * @param httpMethod {@link HttpMethod}.
	 * @return <code>true</code> if same {@link HttpMethod}.
	 */
	public boolean isEqual(HttpMethod httpMethod) {

		// Determine if match on enum
		if ((this.httpMethodEnum != HttpMethodEnum.OTHER) && (this.httpMethodEnum == httpMethod.httpMethodEnum)) {
			return true;
		}

		// Match based on name
		return this.name.equals(httpMethod.name);
	}

	/**
	 * Obtains the {@link HttpMethodEnum} for this {@link HttpMethod}.
	 * 
	 * @return {@link HttpMethodEnum}.
	 */
	public HttpMethodEnum getEnum() {
		return this.httpMethodEnum;
	}

	/**
	 * Obtains the {@link HttpMethod} name.
	 * 
	 * @return {@link HttpMethod} name.
	 */
	public String getName() {
		return this.name;
	}

	/*
	 * ================= Object ===============
	 */

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj) {

		// Determine if appropriate type
		if (!(obj instanceof HttpMethod)) {
			return false;
		}
		HttpMethod that = (HttpMethod) obj;

		// Determine if equal
		return this.isEqual(that);
	}

	@Override
	public String toString() {
		return this.name;
	}

}
