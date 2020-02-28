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
public class OfficeFloorEndPoint<S, U> extends AbstractEndpoint<S, U> {

	/**
	 * {@link Log}.
	 */
	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Escalates that should not use end point.
	 * 
	 * @return {@link UnsupportedOperationException} for failure.
	 */
	private UnsupportedOperationException shouldNotBeUsed() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " should not be used");
	}

	/*
	 * =================== OfficeFloorEndPoint ========================
	 */

	@Override
	protected void createSSLContext(SSLHostConfig sslHostConfig) throws Exception {
		throw this.shouldNotBeUsed();
	}

	@Override
	protected InetSocketAddress getLocalAddress() throws IOException {
		throw this.shouldNotBeUsed();
	}

	@Override
	public boolean isAlpnSupported() {
		return false;
	}

	@Override
	protected boolean getDeferAccept() {
		throw this.shouldNotBeUsed();
	}

	@Override
	protected SocketProcessorBase<S> createSocketProcessor(SocketWrapperBase<S> socketWrapper, SocketEvent event) {
		throw this.shouldNotBeUsed();
	}

	@Override
	public void bind() throws Exception {
	}

	@Override
	public void unbind() throws Exception {
	}

	@Override
	public void startInternal() throws Exception {
	}

	@Override
	public void stopInternal() throws Exception {
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected void doCloseServerSocket() throws IOException {
		throw this.shouldNotBeUsed();
	}

	@Override
	protected U serverSocketAccept() throws Exception {
		throw this.shouldNotBeUsed();
	}

	@Override
	protected boolean setSocketOptions(U socket) {
		throw this.shouldNotBeUsed();
	}

	@Override
	protected void destroySocket(U socket) {
		throw this.shouldNotBeUsed();
	}

}