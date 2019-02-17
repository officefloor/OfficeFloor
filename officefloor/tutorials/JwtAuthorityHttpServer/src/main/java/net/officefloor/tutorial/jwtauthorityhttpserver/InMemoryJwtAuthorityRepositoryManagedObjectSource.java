package net.officefloor.tutorial.jwtauthorityhttpserver;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	public List<JwtAccessKey> retrieveJwtAccessKeys(Instant activeAfter) throws Exception {
		return this.accessKeys;
	}

	@Override
	public void saveJwtAccessKeys(JwtAccessKey... accessKeys) throws Exception {
		this.accessKeys.addAll(Arrays.asList(accessKeys));
	}

	@Override
	public List<JwtRefreshKey> retrieveJwtRefreshKeys(Instant activeAfter) throws Exception {
		return this.refreshKeys;
	}

	@Override
	public void saveJwtRefreshKeys(JwtRefreshKey... refreshKeys) {
		this.refreshKeys.addAll(Arrays.asList(refreshKeys));
	}

}
