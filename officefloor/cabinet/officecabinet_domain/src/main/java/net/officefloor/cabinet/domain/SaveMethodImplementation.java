package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.CabinetManager;

/**
 * {@link MethodImplementation} to save {@link Document} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class SaveMethodImplementation implements MethodImplementation {

	private final SaveParameter[] saveParameters;

	public SaveMethodImplementation(SaveParameter[] saveParameters) {
		this.saveParameters = saveParameters;
	}

	/**
	 * ====================== MethodImplementation =========================
	 */

	@Override
	public Object invoke(CabinetManager cabinetManager, Object[] arguments) throws Exception {
		for (int i = 0; i < arguments.length; i++) {
			SaveParameter save = this.saveParameters[i];
			save.save(cabinetManager, arguments[i]);
		}
		return null;
	}

}