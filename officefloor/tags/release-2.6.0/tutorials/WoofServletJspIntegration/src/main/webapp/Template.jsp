<jsp:useBean id="RequestBean" scope="request"
	class="net.officefloor.tutorial.woofservletjspintegration.RequestBean" />
<jsp:useBean id="SessionBean" scope="session"
	class="net.officefloor.tutorial.woofservletjspintegration.SessionBean" />
<jsp:useBean id="ApplicationBean" scope="application"
	class="net.officefloor.tutorial.woofservletjspintegration.ApplicationBean" />
<html>
    <body>
        <jsp:getProperty name="RequestBean" property="text" />
        <jsp:getProperty name="SessionBean" property="text" />
        <jsp:getProperty name="ApplicationBean" property="text" />
        <a href='/template.woof'>WoOF</a>
    </body>
</html>