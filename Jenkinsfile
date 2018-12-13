pipeline {
	agent any

    parameters {
        choice(name: 'BUILD_TYPE', choices: [ 'TEST', 'STAGE', 'RELEASE', 'SITE', 'TAG_RELEASE' ], description: 'Indicates what type of build')
    }
    
//    triggers {
//        cron('0 1 * * *')
//    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
		disableConcurrentBuilds()
		timeout(time: 6, unit: 'HOURS')
    }

//	tools {
//	    maven 'apache-maven-3.6.0'
//	}

	
	stages {
	    stage('Test') {
			when {
				allOf {
				    equals expected: 'TEST', actual: param.BUILD_TYPE 
				}
			}

//	    	tools {
//	    	   jdk 'jdk11' 
//	    	}
	    
	        steps {
	        	echo "Running test for ${param.BUILD_TYPE}"
	 			// sh 'mvn -Dmaven.test.failure.ignore=true clean install'	 			
	        }
	    }
	    
	    stage('Stage') {
	        when {
	            equals expected: 'STAGE', actual: param.BUILD_TYPE
	        }
	        echo "Running stage"
	    }

	    
	    stage('Deploy Site') {
			when {
				allOf {
				    branch 'master'
				    equals expected: 'SITE', actual: param.BUILD_TYPE 
				}
			}             
	    }
	}
}
