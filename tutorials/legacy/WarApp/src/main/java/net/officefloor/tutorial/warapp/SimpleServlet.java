package net.officefloor.tutorial.warapp;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple {@link HttpServlet}.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("serial")
// START SNIPPET: tutorial
@WebServlet(urlPatterns = "/simple")
public class SimpleServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().write("SIMPLE");
	}
}
// END SNIPPET: tutorial