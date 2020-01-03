/*-
 * #%L
 * JWT Tutorial
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

package net.officefloor.tutorial.jwthttpserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.web.jwt.authority.repository.JwtAccessKey;
import net.officefloor.web.jwt.authority.repository.JwtAuthorityRepository;
import net.officefloor.web.jwt.authority.repository.JwtRefreshKey;
import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * <p>
 * In memory {@link JwtAuthorityRepository} {@link ManagedObjectSource}.
 * <p>
 * <strong>Production environments should persist {@link JwtValidateKey}
 * instances to persistent storage.</strong> This is only provided for an easier
 * tutorial.
 * 
 * @author Daniel Sagenschneider
 */
public class InMemoryJwtAuthorityRepositoryManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject, JwtAuthorityRepository {

	/**
	 * {@link JwtRefreshKey} instances.
	 */
	private List<JwtRefreshKey> refreshKeys = Collections.synchronizedList(new ArrayList<>());

	/**
	 * {@link JwtAccessKey} instances.
	 */
	private List<JwtAccessKey> accessKeys = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Clears old keys.
	 * 
	 * @param list          List of keys.
	 * @param context       {@link RetrieveKeysContext}.
	 * @param getExpireTime Obtains the expire time from the key.
	 */
	private <K> void clearOldKeys(List<K> list, RetrieveKeysContext context, Function<K, Long> getExpireTime) {
		long activeAfter = context.getActiveAfter();
		Iterator<K> iterator = list.iterator();
		while (iterator.hasNext()) {
			K key = iterator.next();
			long expireTime = getExpireTime.apply(key);
			if (expireTime < activeAfter) {
				iterator.remove();
			}
		}
	}

	/*
	 * ================= ManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(JwtAuthorityRepository.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * =================== ManagedObject ==========================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this;
	}

	/*
	 * ================= JwtAuthorityRepository ===================
	 */

	@Override
	public List<JwtAccessKey> retrieveJwtAccessKeys(RetrieveKeysContext context) throws Exception {
		this.clearOldKeys(this.accessKeys, context, (key) -> key.getExpireTime());
		return this.accessKeys;
	}

	@Override
	public void saveJwtAccessKeys(SaveKeysContext context, JwtAccessKey... accessKeys) throws Exception {
		this.accessKeys.addAll(Arrays.asList(accessKeys));
	}

	@Override
	public List<JwtRefreshKey> retrieveJwtRefreshKeys(RetrieveKeysContext context) throws Exception {
		this.clearOldKeys(this.refreshKeys, context, (key) -> key.getExpireTime());
		return this.refreshKeys;
	}

	@Override
	public void saveJwtRefreshKeys(SaveKeysContext context, JwtRefreshKey... refreshKeys) {
		this.refreshKeys.addAll(Arrays.asList(refreshKeys));
	}

}
