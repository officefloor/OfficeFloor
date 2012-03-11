import java.sql.Connection;

import net.officefloor.plugin.governance.clazz.Disregard;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;

/**
 * This is not used within the application. It is only here for the
 * <code>presentation.woof</code> to give an overview screen shot of the
 * graphical configuration.
 */
public class Transaction {

	@Govern
	public void govern(Connection connection) {
	}

	@Enforce
	public void commit() {
	}

	@Disregard
	public void rollback() {
	}
}
