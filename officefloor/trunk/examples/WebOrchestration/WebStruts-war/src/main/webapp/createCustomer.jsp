<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
	<head>
		<title>EJB Orchestration Example</title>
	</head>
	<body>
		<h1>Create Customer</h1>
		<s:form action="createCustomer">
			<s:textfield name="name" label="Name" />
			<s:textfield name="email" label="Email" />
			<s:password name="password" label="Password" />
			<s:submit name="create" value="Create" />
        </s:form>
		<p><s:property value="error"/></p>
	</body>
</html>