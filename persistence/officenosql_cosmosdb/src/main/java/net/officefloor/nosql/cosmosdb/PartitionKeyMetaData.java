/*-
 * #%L
 * CosmosDB
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.nosql.cosmosdb;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import com.azure.cosmos.models.PartitionKey;

/**
 * Meta-data for the {@link PartitionKey}.
 * 
 * @author Daniel Sagenschneider
 */
public class PartitionKeyMetaData {

	/**
	 * Cache of the entity identifier {@link Method}.
	 */
	private static final Map<Class<?>, Method> identifierMethodCache = new ConcurrentHashMap<>();

	/**
	 * Default {@link PartitionKey} factory that uses the entity's identifier.
	 */
	public static final Function<Object, PartitionKey> DEFAULT_FACTORY = (entity) -> {
		Class<?> entityType = entity.getClass();
		try {

			// Lazy obtain the identifier method
			Method getId = identifierMethodCache.get(entityType);
			if (getId == null) {
				try {
					getId = entityType.getMethod("getId");
				} catch (Exception ex) {
					getId = entityType.getMethod("id");
				}
				identifierMethodCache.put(entityType, getId);
			}

			// Obtain the identifier
			Object identifier = getId.invoke(entity);

			// Use identifier as partition key
			return new PartitionKey(identifier);

		} catch (Exception ex) {
			throw new CosmosNoEntityIdentifierException(entityType);
		}
	};

	/**
	 * Default {@link PartitionKeyMetaData} being the identifier of the entity.
	 */
	public static final PartitionKeyMetaData DEFAULT = new PartitionKeyMetaData("/id", DEFAULT_FACTORY);

	/**
	 * Obtains the {@link PartitionKeyMetaData} for the entity type.
	 * 
	 * @param entityType Entity type.
	 * @return {@link PartitionKeyMetaData} for the entity type.
	 */
	public static PartitionKeyMetaData getPartitionKeyMetaData(Class<?> entityType) {

		// Determine if there is configured entity ownership
		Class<?> type = entityType;
		while (type != null) {

			// Determine if method partition
			for (Method method : type.getDeclaredMethods()) {
				if (method.isAnnotationPresent(CosmosPartitionKey.class)) {

					// Obtain partition key path
					String methodName = method.getName();
					String partitionKeyPath;
					if ((methodName.startsWith("get")) && (!"get".equals(methodName))) {
						// Bean method, so remove get
						String propertyName = methodName.substring("get".length());
						partitionKeyPath = "/" + propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
					} else {
						partitionKeyPath = "/" + methodName;
					}

					// Obtain partition key factory
					final Method partitionKeyMethod = method;
					partitionKeyMethod.setAccessible(true);
					Function<Object, PartitionKey> partitionKeyFactory = (entity) -> {
						try {
							Object partitionKeyValue = partitionKeyMethod.invoke(entity);
							return new PartitionKey(partitionKeyValue);
						} catch (Exception ex) {
							throw new CosmosPartitionKeyException(entityType, ex);
						}
					};

					// Return the partition key meta-data
					return new PartitionKeyMetaData(partitionKeyPath, partitionKeyFactory);
				}
			}

			// Search super class
			type = type.getSuperclass();
		}

		// No declared partition key, so use default
		return DEFAULT;
	}

	/**
	 * {@link PartitionKey} path.
	 */
	private final String path;

	/**
	 * Factory to create {@link PartitionKey} from entity.
	 */
	private final Function<Object, PartitionKey> factory;

	/**
	 * Instantiate.
	 * 
	 * @param path    {@link PartitionKey} path.
	 * @param factory Factory to create {@link PartitionKey} from entity.
	 */
	public PartitionKeyMetaData(String path, Function<Object, PartitionKey> factory) {
		this.path = path;
		this.factory = factory;
	}

	/**
	 * Obtains the {@link PartitionKey} path.
	 * 
	 * @return {@link PartitionKey} path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Obtains the factory to create {@link PartitionKey} from entity.
	 * 
	 * @return Factory to create {@link PartitionKey} from entity.
	 */
	public Function<Object, PartitionKey> getFactory() {
		return factory;
	}

}
