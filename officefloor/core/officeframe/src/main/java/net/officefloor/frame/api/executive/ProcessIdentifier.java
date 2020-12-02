package net.officefloor.frame.api.executive;

import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Identifier for {@link ProcessState}.
 * <p>
 * The requirements are that:
 * <ul>
 * <li>the same {@link ProcessIdentifier} equals itself</li>
 * <li>no two separate {@link ProcessIdentifier} instance equal each other</li>
 * <ul>
 * <p>
 * The easiest way to ensure this is create a new instance each time and allow
 * default {@link Object} equality.
 * <p>
 * Other than the above, the {@link Executive} is free to provide any
 * implementation of this interface as a momento about the {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessIdentifier {
}