<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
	<head>
		<title>EJB Orchestration Example</title>
	</head>
	<body>
		<h1>EJB Orchestration Example</h1>
		<p>Hello <s:property value="customer.name" /></p>
		<p>Welcome to the EJB Orchestration Example</p>
		<a href="<s:url action='login'/>"/>Login</a>
		<a href="<s:url action='shoppingCart'/>">Shopping Cart</a>
	</body>
</html>