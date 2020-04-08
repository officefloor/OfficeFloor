/*-
 * #%L
 * Servlet
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.servlet.tomcat;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SendfileDataBase;
import org.apache.tomcat.util.net.SendfileState;
import org.apache.tomcat.util.net.SocketWrapperBase;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link SocketWrapperBase}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorSocketWrapper extends SocketWrapperBase<Void> {

	/**
	 * Escalates that should not require direct {@link Socket}.
	 * 
	 * @return {@link UnsupportedOperationException} for failure.
	 */
	public static UnsupportedOperationException noSocket() {
		throw new UnsupportedOperationException(OfficeFloor.class.getSimpleName() + " does not provide Socket access");
	}

	/**
	 * Instantiate.
	 * 
	 * @param endpoint
	 */
	public OfficeFloorSocketWrapper(AbstractEndpoint<Void, ?> endpoint) {
		super(null, endpoint);
	}

	/*
	 * ======================= SocketWrapperBase ============================
	 */

	@Override
	protected void populateRemoteHost() {
		throw noSocket();
	}

	@Override
	protected void populateRemoteAddr() {
		throw noSocket();
	}

	@Override
	protected void populateRemotePort() {
		throw noSocket();
	}

	@Override
	protected void populateLocalName() {
		throw noSocket();
	}

	@Override
	protected void populateLocalAddr() {
		throw noSocket();
	}

	@Override
	protected void populateLocalPort() {
		throw noSocket();
	}

	@Override
	public int read(boolean block, byte[] b, int off, int len) throws IOException {
		throw noSocket();
	}

	@Override
	public int read(boolean block, ByteBuffer to) throws IOException {
		throw noSocket();
	}

	@Override
	public boolean isReadyForRead() throws IOException {
		throw noSocket();
	}

	@Override
	public void setAppReadBufHandler(ApplicationBufferHandler handler) {
		throw noSocket();
	}

	@Override
	protected void doClose() {
		throw noSocket();
	}

	@Override
	protected void doWrite(boolean block, ByteBuffer from) throws IOException {
		throw noSocket();
	}

	@Override
	public void registerReadInterest() {
		throw noSocket();
	}

	@Override
	public void registerWriteInterest() {
		throw noSocket();
	}

	@Override
	public SendfileDataBase createSendfileData(String filename, long pos, long length) {
		throw noSocket();
	}

	@Override
	public SendfileState processSendfile(SendfileDataBase sendfileData) {
		throw noSocket();
	}

	@Override
	public void doClientAuth(SSLSupport sslSupport) throws IOException {
		throw noSocket();
	}

	@Override
	public SSLSupport getSslSupport(String clientCertProvider) {
		throw noSocket();
	}

	@Override
	protected <A> SocketWrapperBase<Void>.OperationState<A> newOperationState(boolean arg0, ByteBuffer[] arg1, int arg2,
			int arg3, BlockingMode arg4, long arg5, TimeUnit arg6, A arg7, CompletionCheck arg8,
			CompletionHandler<Long, ? super A> arg9, Semaphore arg10,
			SocketWrapperBase<Void>.VectoredIOCompletionHandler<A> arg11) {
		throw noSocket();
	}

}
