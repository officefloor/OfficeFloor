pipeline {
	agent any

    parameters {
        choice(name: 'BUILD_TYPE', choices: [ 'NIGHTLY', 'STAGE', 'RELEASE', 'SITE' ], description: 'Indicates what type of build')
    }
	
	stages {
	    stage('Build') {
	        steps {
	 			echo 'Hello World'           
	            
	        }
	    }
	}
}
