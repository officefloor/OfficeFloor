package net.officefloor.compile.internal.structure;

/**
 * Auto-wiring of a source {@link Node} to target {@link Node}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireLink<S extends Node, T extends Node> {

	/**
	 * Obtains the source {@link Node}.
	 * 
	 * @return Source {@link Node}.
	 */
	S getSourceNode();

	/**
	 * Obtains the matching source {@link AutoWire}.
	 * 
	 * @return Matching source {@link AutoWire}.
	 */
	AutoWire getSourceAutoWire();

	/**
	 * Obtains the target {@link Node}.
	 * 
	 * @param office {@link OfficeNode}.
	 * @return Target {@link Node}.
	 */
	T getTargetNode(OfficeNode office);

	/**
	 * Obtains the matching target {@link AutoWire}.
	 * 
	 * @return Matching target {@link AutoWire}.
	 */
	AutoWire getTargetAutoWire();

}