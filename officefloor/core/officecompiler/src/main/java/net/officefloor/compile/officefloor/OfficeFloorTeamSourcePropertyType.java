package net.officefloor.compile.officefloor;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceProperty;

/**
 * <code>Type definition</code> of a {@link Property} that may be configured on
 * the {@link TeamSource}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorTeamSourcePropertyType extends TeamSourceProperty {

	/**
	 * Obtains the default value for this {@link Property}.
	 * 
	 * @return Default value for this {@link Property}.
	 */
	String getDefaultValue();

}