/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.server.http.rapidoid;

import org.rapidoid.buffer.Buf;
import org.rapidoid.http.AbstractHttpServer;
import org.rapidoid.http.HttpStatus;
import org.rapidoid.http.MediaType;
import org.rapidoid.http.impl.lowlevel.HttpIO;
import org.rapidoid.net.Server;
import org.rapidoid.net.abstracts.Channel;
import org.rapidoid.net.impl.RapidoidHelper;
import org.rapidoid.web.Rapidoid;

import net.officefloor.server.http.AbstractHttpServerImplementationTest;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerLocation;

/**
 * Tests the {@link RapidoidHttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class RapidoidHttpServerImplementationTest extends AbstractHttpServerImplementationTest<Server> {

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		return RapidoidHttpServerImplementation.class;
	}

	@Override
	protected HttpHeader[] getServerResponseHeaderValues() {
		return new HttpHeader[] { newHttpHeader("Content-Length", "?"), newHttpHeader("Content-Type", "?") };
	}

	@Override
	protected String getServerNameSuffix() {
		return "Rapidoid";
	}

	@Override
	protected Server startRawHttpServer(HttpServerLocation serverLocation) throws Exception {
		RawRapidoidHttpServer server = new RawRapidoidHttpServer();
		return server.listen(serverLocation.getClusterHttpPort());
	}

	@Override
	protected void stopRawHttpServer(Server server) throws Exception {
		server.shutdown();
	}

	/**
	 * Raw {@link Rapidoid} {@link AbstractHttpServer}.
	 */
	public static class RawRapidoidHttpServer extends AbstractHttpServer {

		private static final byte[] HELLO_WORLD = "hello world".getBytes();

		@Override
		protected HttpStatus handle(Channel ctx, Buf buf, RapidoidHelper data) {
			ctx.write(STATUS_200);
			HttpIO.INSTANCE.writeContentLengthHeader(ctx, HELLO_WORLD.length);
			ctx.write(CONTENT_TYPE_TXT);
			ctx.write(MediaType.TEXT_PLAIN.getBytes());
			ctx.write(CR_LF);
			ctx.write(CR_LF);
			ctx.write(HELLO_WORLD, 0, HELLO_WORLD.length);
			return HttpStatus.DONE;
		}
	}

}