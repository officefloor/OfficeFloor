package net.officefloor.gef.editor.internal.parts;

import org.eclipse.gef.mvc.fx.parts.IContentPart;

import net.officefloor.gef.editor.AdaptedConnectable;
import net.officefloor.gef.editor.AdaptedConnector;
import net.officefloor.gef.editor.internal.handlers.CreateAdaptedConnectionOnDragHandler;

/**
 * {@link IContentPart} for the {@link AdaptedConnectable}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnectablePart {

	/**
	 * Obtains the {@link AdaptedConnector}.
	 * 
	 * @return {@link AdaptedConnector}.
	 */
	AdaptedConnector<?> getContent();

	/**
	 * Specifies this as the active {@link AdaptedConnectablePart} for the
	 * {@link CreateAdaptedConnectionOnDragHandler}.
	 * 
	 * @param isActive Indicates if active.
	 */
	void setActiveConnector(boolean isActive);

}