const Request = Java.type("net.officefloor.tutorial.javascripthttpserver.Request");
const HttpStatus = Java.type("net.officefloor.server.http.HttpStatus")
const HttpException = Java.type("net.officefloor.server.http.HttpException");

function validate(request) {
	if (request.getId() < 1) {
		throw new HttpException(HttpStatus.BAD_REQUEST, null, "Invalid identifier");
	}
	if (!request.getName()) {
		throw new HttpException(HttpStatus.BAD_REQUEST, null, "Must provide name");
	}
}
validate.officefloor = [ 
	{ httpObject : Request }
];