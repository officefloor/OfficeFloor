pipeline {
	agent any

    parameters {
        choice(name: 'BUILD_TYPE', choices: [ 'TEST', 'STAGE', 'RELEASE', 'SITE', 'TAG_RELEASE' ], description: 'Indicates what type of build')
        string(name: 'LATEST_JDK', defaultValue: 'jdk11', description: 'Tool name for the latest JDK to support')
		string(name: 'OLDEST_JDK', defaultValue: 'jdk8', description: 'Tool name for the oldest JDK to support')
    }
    
//    triggers {
//        cron('0 1 * * *')
//    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
		disableConcurrentBuilds()
		timeout(time: 6, unit: 'HOURS')
    }

	tools {
	    maven 'maven-3.6.0'
	    jdk "${params.LATEST_JDK}"
	}

	
	stages {
	    stage('Test') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'TEST' }
				}
			}
	    
	        steps {
	        	echo "Running ${params.BUILD_TYPE}"
		        echo "JAVA_HOME = ${env.JAVA_HOME}"
	        	sh 'java -version'
	        	sh 'mvn -version'
	 			// sh 'mvn -Dmaven.test.failure.ignore=true clean install'	 			
	        }
	    }
	    
	    stage('Backwards compatible') {
			tools {
            	jdk "${params.OLDEST_JDK}"
            }
			steps {
		        echo "JAVA_HOME = ${env.JAVA_HOME}"
	        	sh 'java -version'
	        	sh 'mvn -version'
			}
		}
	    
	    stage('Stage') {
	        when {
				expression { params.BUILD_TYPE == 'STAGE' }
	        }
	        steps {
		        echo "JAVA_HOME = ${env.JAVA_HOME}"
	        	sh 'java -version'
	        	sh 'mvn -version'
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
