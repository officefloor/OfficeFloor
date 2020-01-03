package net.officefloor.spring;

import net.officefloor.compile.SupplierSourceService;

/**
 * Spring {@link SupplierSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringSupplierSourceService implements SupplierSourceService<SpringSupplierSource> {

	/**
	 * Alias name for the {@link SpringSupplierSource}.
	 */
	public static final String ALIAS = "SPRING";

	/*
	 * =================== SupplierSourceService ========================
	 */

	@Override
	public String getSupplierSourceAlias() {
		return ALIAS;
	}

	@Override
	public Class<SpringSupplierSource> getSupplierSourceClass() {
		return SpringSupplierSource.class;
	}

}