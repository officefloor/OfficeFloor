<jsp:useBean id="ApplicationBean" scope="application" class="net.officefloor.plugin.servlet.OfficeFloorServletFilterIntegrationToContainerTest$MockApplicationObject" />
<jsp:useBean id="SessionBean" scope="session" class="net.officefloor.plugin.servlet.OfficeFloorServletFilterIntegrationToContainerTest$MockSessionObject" />
<jsp:useBean id="RequestBean" scope="request" class="net.officefloor.plugin.servlet.OfficeFloorServletFilterIntegrationToContainerTest$MockRequestObject" />
<jsp:getProperty name="ApplicationBean" property="text" /> <jsp:getProperty name="SessionBean" property="text" /> <jsp:getProperty name="RequestBean" property="text" />