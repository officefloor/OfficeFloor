<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
	<head>
		<title>EJB Orchestration Example</title>
	</head>
	<body>
		<h1>EJB Orchestration Example</h1>
		<p>Create Customer</p>
		<s:form action="createCustomer">
			<s:textfield name="name" label="Name" />
			<s:textfield name="email" label="Email" />
			<s:password name="password" label="Password" />
			<s:submit value="Create" />
        </s:form>
		<p><s:property value="error"/></p>
	</body>
</html>