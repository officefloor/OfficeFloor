<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
	<head>
		<title>EJB Orchestration Example</title>
	</head>
	<body>
		<h1>Login</h1>
		<s:form action="login">
			<s:textfield name="email" label="Email" />
			<s:password name="password" label="Password" />
			<s:submit name="loginCustomer" value="Login" />
        </s:form>
		<p><s:property value="error"/></p>
		<p><a href="<s:url action='createCustomer'/>">create login</a>
	</body>
</html>