package net.officefloor.tutorial.warhttpserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple {@link HttpServlet}.
 * 
 * @author Daniel Sagenschneider
 */
@WebServlet(urlPatterns = "/simple")
public class SimpleServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/*
	 * ===================== HttpServlet =========================
	 */

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().write("SIMPLE");
	}

}