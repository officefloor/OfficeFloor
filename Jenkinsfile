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
				expression { params.BUILD_TYPE == 'TEST' }
			}

//	    	tools {
//	    	   jdk 'jdk11' 
//	    	}
	    
	        steps {
	        	echo "Running test for ${params.BUILD_TYPE}"
	 			// sh 'mvn -Dmaven.test.failure.ignore=true clean install'	 			
	        }
	    }
	    
	    stage('Stage') {
	        when {
				expression { params.BUILD_TYPE == 'STAGE' }
	        }
	        steps {
		        echo "Running stage"            
	        }
	    }

	    
	    stage('Deploy Site') {
			when {
				allOf {
				    branch 'master'
					expression { params.BUILD_TYPE == 'SITE' }
				}
			}
			steps {
				echo "Running site deploy"
			}
         
	    }
	}
}
