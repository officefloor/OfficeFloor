package net.officefloor.servlet.tomcat;

import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.Processor;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.UpgradeToken;
import org.apache.coyote.http11.Constants;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SocketWrapperBase;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link ProtocolHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorProtocol<S> extends AbstractProtocol<S> {

	/**
	 * {@link Log}.
	 */
	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Instantiate.
	 */
	public OfficeFloorProtocol() {
		super(new OfficeFloorEndPoint<>());
		setConnectionTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
		ConnectionHandler<S> cHandler = new ConnectionHandler<>(this);
		setHandler(cHandler);
		getEndpoint().setHandler(cHandler);
	}

	/**
	 * Escalates that should not use protocol.
	 * 
	 * @return {@link UnsupportedOperationException} for failure.
	 */
	private UnsupportedOperationException shouldNotBeUsed() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " should not be used");
	}

	/*
	 * =================== ProtocolHandler =====================
	 */

	@Override
	public void addSslHostConfig(SSLHostConfig sslHostConfig) {
		throw this.shouldNotBeUsed();
	}

	@Override
	public SSLHostConfig[] findSslHostConfigs() {
		throw this.shouldNotBeUsed();
	}

	@Override
	public void addUpgradeProtocol(UpgradeProtocol upgradeProtocol) {
		throw this.shouldNotBeUsed();
	}

	@Override
	public UpgradeProtocol[] findUpgradeProtocols() {
		throw this.shouldNotBeUsed();
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected String getNamePrefix() {
		return "OfficeFloor";
	}

	@Override
	protected String getProtocolName() {
		return "OfficeFloor";
	}

	@Override
	protected UpgradeProtocol getNegotiatedProtocol(String name) {
		throw this.shouldNotBeUsed();
	}

	@Override
	protected UpgradeProtocol getUpgradeProtocol(String name) {
		throw this.shouldNotBeUsed();
	}

	@Override
	protected Processor createProcessor() {
		throw this.shouldNotBeUsed();
	}

	@Override
	protected Processor createUpgradeProcessor(SocketWrapperBase<?> socket, UpgradeToken upgradeToken) {
		throw this.shouldNotBeUsed();
	}

}