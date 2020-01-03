package net.officefloor.web.security.impl;

import java.security.Principal;

import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.impl.AbstractAdministrationSource;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.security.HttpAccess;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link HttpSecurity} {@link AdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpAccessAdministrationSource extends AbstractAdministrationSource<HttpAccessControl, None, None> {

	/**
	 * {@link Principal} requires only one of these roles.
	 */
	private final String[] anyRoles;

	/**
	 * {@link Principal} must have all these roles.
	 */
	private final String[] allRoles;

	/**
	 * Instantiate with the {@link HttpAccess}.
	 * 
	 * @param anyRoles
	 *            Any roles.
	 * @param allRoles
	 *            All roles.
	 */
	public HttpAccessAdministrationSource(String[] anyRoles, String[] allRoles) {
		this.anyRoles = anyRoles;
		this.allRoles = allRoles;
	}

	/*
	 * ==================== AdministratorSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<HttpAccessControl, None, None> context) throws Exception {
		context.setAdministrationFactory(new HttpAccessAdministration());
		context.setExtensionInterface(HttpAccessControl.class);
	}

	/**
	 * {@link HttpAccess} {@link Administration}.
	 */
	private class HttpAccessAdministration implements AdministrationFactory<HttpAccessControl, None, None>,
			Administration<HttpAccessControl, None, None> {

		/*
		 * ==================== AdministrationFactory ================
		 */

		@Override
		public Administration<HttpAccessControl, None, None> createAdministration() {
			return this;
		}

		/*
		 * ======================= Administration ====================
		 */

		@Override
		public void administer(AdministrationContext<HttpAccessControl, None, None> context) throws HttpException {

			// Easy access roles
			String[] anyRoles = HttpAccessAdministrationSource.this.anyRoles;
			String[] allRoles = HttpAccessAdministrationSource.this.allRoles;

			// Obtain the HTTP Access Control
			HttpAccessControl[] accessControls = context.getExtensions();

			// Provide means to load access control
			HttpAccessControl accessControl = accessControls[0];

			// Ensure has access
			if (!accessControl.isAccess(anyRoles, allRoles)) {
				throw new HttpException(HttpStatus.FORBIDDEN);
			}

			// As here, has access
		}
	}

}