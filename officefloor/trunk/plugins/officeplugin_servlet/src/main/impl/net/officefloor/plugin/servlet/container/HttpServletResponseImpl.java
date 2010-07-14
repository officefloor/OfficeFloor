/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.container;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.plugin.socket.server.http.HttpResponse;

/**
 * {@link HttpServletResponse} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletResponseImpl implements HttpServletResponse {

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponse response;

	/**
	 * Initiate.
	 * 
	 * @param response
	 *            {@link HttpResponse}.
	 */
	public HttpServletResponseImpl(HttpResponse response) {
		this.response = response;
	}

	/*
	 * ======================= HttpServletResponse =======================
	 */

	@Override
	public void addCookie(Cookie cookie) {
		// TODO implement HttpServletResponse.addCookie
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.addCookie");
	}

	@Override
	public void addDateHeader(String name, long date) {
		// TODO implement HttpServletResponse.addDateHeader
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.addDateHeader");
	}

	@Override
	public void addHeader(String name, String value) {
		// TODO implement HttpServletResponse.addHeader
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.addHeader");
	}

	@Override
	public void addIntHeader(String name, int value) {
		// TODO implement HttpServletResponse.addIntHeader
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.addIntHeader");
	}

	@Override
	public boolean containsHeader(String name) {
		// TODO implement HttpServletResponse.containsHeader
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.containsHeader");
	}

	@Override
	public String encodeRedirectURL(String url) {
		// TODO implement HttpServletResponse.encodeRedirectURL
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.encodeRedirectURL");
	}

	@Override
	public String encodeRedirectUrl(String url) {
		// TODO implement HttpServletResponse.encodeRedirectUrl
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.encodeRedirectUrl");
	}

	@Override
	public String encodeURL(String url) {
		// TODO implement HttpServletResponse.encodeURL
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.encodeURL");
	}

	@Override
	public String encodeUrl(String url) {
		// TODO implement HttpServletResponse.encodeUrl
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.encodeUrl");
	}

	@Override
	public void sendError(int sc) throws IOException {
		// TODO implement HttpServletResponse.sendError
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.sendError");
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		// TODO implement HttpServletResponse.sendError
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.sendError");
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		// TODO implement HttpServletResponse.sendRedirect
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.sendRedirect");
	}

	@Override
	public void setDateHeader(String name, long date) {
		// TODO implement HttpServletResponse.setDateHeader
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.setDateHeader");
	}

	@Override
	public void setHeader(String name, String value) {
		// TODO implement HttpServletResponse.setHeader
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.setHeader");
	}

	@Override
	public void setIntHeader(String name, int value) {
		// TODO implement HttpServletResponse.setIntHeader
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.setIntHeader");
	}

	@Override
	public void setStatus(int sc) {
		// TODO implement HttpServletResponse.setStatus
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.setStatus");
	}

	@Override
	public void setStatus(int sc, String sm) {
		// TODO implement HttpServletResponse.setStatus
		throw new UnsupportedOperationException(
				"TODO implement HttpServletResponse.setStatus");
	}

	@Override
	public void flushBuffer() throws IOException {
		// TODO implement ServletResponse.flushBuffer
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.flushBuffer");
	}

	@Override
	public int getBufferSize() {
		// TODO implement ServletResponse.getBufferSize
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.getBufferSize");
	}

	@Override
	public String getCharacterEncoding() {
		// TODO implement ServletResponse.getCharacterEncoding
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.getCharacterEncoding");
	}

	@Override
	public String getContentType() {
		// TODO implement ServletResponse.getContentType
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.getContentType");
	}

	@Override
	public Locale getLocale() {
		// TODO implement ServletResponse.getLocale
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.getLocale");
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		// TODO implement ServletResponse.getOutputStream
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.getOutputStream");
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		// TODO implement ServletResponse.getWriter
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.getWriter");
	}

	@Override
	public boolean isCommitted() {
		// TODO implement ServletResponse.isCommitted
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.isCommitted");
	}

	@Override
	public void reset() {
		// TODO implement ServletResponse.reset
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.reset");
	}

	@Override
	public void resetBuffer() {
		// TODO implement ServletResponse.resetBuffer
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.resetBuffer");
	}

	@Override
	public void setBufferSize(int size) {
		// TODO implement ServletResponse.setBufferSize
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.setBufferSize");
	}

	@Override
	public void setCharacterEncoding(String charset) {
		// TODO implement ServletResponse.setCharacterEncoding
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.setCharacterEncoding");
	}

	@Override
	public void setContentLength(int len) {
		// TODO implement ServletResponse.setContentLength
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.setContentLength");
	}

	@Override
	public void setContentType(String type) {
		// TODO implement ServletResponse.setContentType
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.setContentType");
	}

	@Override
	public void setLocale(Locale loc) {
		// TODO implement ServletResponse.setLocale
		throw new UnsupportedOperationException(
				"TODO implement ServletResponse.setLocale");
	}

}