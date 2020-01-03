package net.officefloor.gef.editor;

import org.eclipse.gef.geometry.planar.Dimension;

import net.officefloor.model.Model;

/**
 * Adapted area.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedArea<M extends Model> extends AdaptedConnectable<M>, AdaptedConnector<M> {

	/**
	 * Obtains the minimum {@link Dimension} for the {@link AdaptedArea}.
	 * 
	 * @return Minimum {@link Dimension} for the {@link AdaptedArea}.
	 */
	Dimension getMinimumDimension();

	/**
	 * Obtains the {@link Dimension}.
	 * 
	 * @return {@link Dimension}.
	 */
	Dimension getDimension();

	/**
	 * Specifies the {@link Dimension}.
	 * 
	 * @param dimension {@link Dimension}.
	 */
	void setDimension(Dimension dimension);

	/**
	 * Obtains the {@link ParentToAreaConnectionModel}.
	 * 
	 * @return {@link ParentToAreaConnectionModel}.
	 */
	ParentToAreaConnectionModel getParentConnection();

	/**
	 * Obtains the adapter.
	 * 
	 * @param          <T> Adapted type.
	 * @param classKey {@link Class} key.
	 * @return Adapter or <code>null</code> if no adapter available.
	 */
	<T> T getAdapter(Class<T> classKey);

}