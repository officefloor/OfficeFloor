package net.officefloor.servlet.tomcat;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SocketProcessorBase;
import org.apache.tomcat.util.net.SocketWrapperBase;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link AbstractEndpoint}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorEndPoint extends AbstractEndpoint<Void, OfficeFloorEndPoint> {

	/**
	 * {@link OfficeFloorSocketWrapper}.
	 */
	private final OfficeFloorSocketWrapper socketWrapper;

	/**
	 * {@link Log}.
	 */
	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Initiate.
	 */
	public OfficeFloorEndPoint() {
		this.socketWrapper = new OfficeFloorSocketWrapper(this);
		this.connections.put(this, this.socketWrapper);
		this.running = true;
	}

	/**
	 * Obtains the {@link OfficeFloorSocketWrapper}.
	 * 
	 * @return {@link OfficeFloorSocketWrapper}.
	 */
	public OfficeFloorSocketWrapper getOfficeFloorSocketWrapper() {
		return this.socketWrapper;
	}

	/*
	 * =================== OfficeFloorEndPoint ========================
	 */

	@Override
	protected Log getLog() {
		return this.log;
	}

	@Override
	public void bind() throws Exception {
		// no socket
	}

	@Override
	public void startInternal() throws Exception {
		// no socket
	}

	@Override
	public void stopInternal() throws Exception {
		// no socket
	}

	@Override
	public void unbind() throws Exception {
		// no socket
	}

	@Override
	public boolean isAlpnSupported() {
		return false;
	}

	/*
	 * =============== OfficeFloorEndPoint (unused) ===================
	 */

	@Override
	protected void createSSLContext(SSLHostConfig sslHostConfig) throws Exception {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected InetSocketAddress getLocalAddress() throws IOException {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected boolean getDeferAccept() {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected SocketProcessorBase<Void> createSocketProcessor(SocketWrapperBase<Void> socketWrapper,
			SocketEvent event) {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected void doCloseServerSocket() throws IOException {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected OfficeFloorEndPoint serverSocketAccept() throws Exception {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected boolean setSocketOptions(OfficeFloorEndPoint socket) {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected void destroySocket(OfficeFloorEndPoint socket) {
		throw OfficeFloorSocketWrapper.noSocket();
	}

}