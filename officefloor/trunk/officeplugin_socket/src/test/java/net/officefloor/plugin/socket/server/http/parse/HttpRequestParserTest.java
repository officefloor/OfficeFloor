/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http.parse;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link HttpRequestParser}.
 * 
 * @author Daniel
 */
public class HttpRequestParserTest extends OfficeFrameTestCase {

	/**
	 * HTTP end of line sequence.
	 */
	public static final String EOL = "\r\n";

	/**
	 * Validates GET request.
	 */
	public void testParseGetRequest() throws Exception {

		// Create the parser to test
		HttpRequestParser parser = new HttpRequestParser();

		// Parse the content
		byte[] get = UsAsciiUtil.convertToUsAscii("GET /path HTTP/1.1" + EOL
				+ "Header1: Value1" + EOL + "Header2: Value2" + EOL + EOL
				+ "body");
		parser.parseMoreContent(get, 0, get.length);

		// Validate request line
		assertEquals("GET", parser.getMethod());
		assertEquals("/path", parser.getPath());
		assertEquals("HTTP/1.1", parser.getVersion());

		// Validate headers
		assertEquals("Incorrect header 1", "Value1", parser
				.getHeader("Header1"));
		assertEquals("Incorrect header 2", "Value2", parser
				.getHeader("Header2"));
		assertEquals("No header 3", null, parser.getHeader("Header3"));

		// Validate the body
		UsAsciiUtil.assertEquals("Incorrect body", "body", parser.getBody());
	}

}
