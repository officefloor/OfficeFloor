let Request = Java.type('net.officefloor.tutorial.javascripthttpserver.Request')

function validate(request) {
	if (request.getId() < 1) {
		throw new Error('Invalid identifier')
	}
	if (!request.getName()) {
		throw new Error('Must provide name')
	}
}
validate.officefloor = [
	Request,
	{next: 'valid'}
]