package net.officefloor.model.generate;

import java.io.InputStream;

/**
 * Context for generating a Model.
 *
 * @author Daniel Sagenschneider
 */
public interface ModelContext {

	/**
	 * Creates the {@link ModelFile} within this {@link ModelContext}.
	 *
	 * @param relativeLocation
	 *            Relative location within this {@link ModelContext} to create
	 *            the {@link ModelFile}.
	 * @param contents
	 *            Contents to be written to the {@link ModelFile}.
	 * @return Created {@link ModelFile}.
	 * @throws Exception
	 *             If fails to create the {@link ModelFile}.
	 */
	ModelFile createModelFile(String relativeLocation, InputStream contents)
			throws Exception;

}