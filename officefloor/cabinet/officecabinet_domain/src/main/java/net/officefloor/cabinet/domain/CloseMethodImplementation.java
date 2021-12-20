package net.officefloor.cabinet.domain;

/**
 * {@link MethodImplementation} for the {@link AutoCloseable#close()}.
 * 
 * @author Daniel Sagenschneider
 */
public class CloseMethodImplementation implements MethodImplementation {

	/*
	 * ====================== MethodImplementation ========================
	 */

	@Override
	public Object invoke(CabinetSession session, Object[] arguments) throws Exception {
		session.close();
		return null;
	}

}