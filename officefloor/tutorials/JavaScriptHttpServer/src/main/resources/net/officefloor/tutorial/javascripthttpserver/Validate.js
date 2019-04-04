const Request = Java.type('net.officefloor.tutorial.javascripthttpserver.Request')
const HttpException = Java.type('net.officefloor.server.http.HttpException')

function validate(request) {
	if (request.getId() < 1) {
		throw new HttpException(400, 'Invalid identifier')
	}
	if (!request.getName()) {
		throw new HttpException(400, 'Must provide name')
	}
}
validate.officefloor = [
	{httpObject: Request},
	{next: 'valid'}
]