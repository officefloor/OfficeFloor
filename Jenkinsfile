pipeline {
	agent any

    parameters {
        choice(name: 'BUILD_TYPE', choices: [ 'TEST', 'STAGE', 'RELEASE', 'SITE', 'TAG_RELEASE' ], description: 'Indicates what type of build')
        string(name: 'LATEST_JDK', defaultValue: 'jdk11', description: 'Tool name for the latest JDK to support')
		string(name: 'OLDEST_JDK', defaultValue: 'jdk8', description: 'Tool name for the oldest JDK to support')
    }
    
    triggers {
        cron('0 1 * * *')
    }
    
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
		stage('Build') {
		    when {
		        expression { params.BUILD_TYPE == 'TEST' }
		    }
	        steps {
	        	sh './.travis-install.sh'
	        }
		}
	
	    stage('Test') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'TEST' }
					not { branch 'master' }
				}
			}
	        steps {
	        	dir('officefloor/bom') {
					sh 'mvn -Dmaven.test.failure.ignore=true -Dofficefloor.skip.stress.tests=true verify'
	        	}
	        }
	    }
	    
	    stage('Test and Stress') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'TEST' }
					branch 'master'
				}
			}
	        steps {
	        	dir('officefloor/bom') {
					sh 'mvn -Dmaven.test.failure.ignore=true verify'
	        	}
	        }
	    }
	    
	    stage('Backwards compatible') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'TEST' }
					branch 'master'
				}
			}
			tools {
            	jdk "${params.OLDEST_JDK}"
            }
			steps {
	        	dir('officefloor/bom') {
					sh 'mvn -Dmaven.test.failure.ignore=true -Dofficefloor.skip.stress.tests=true clean install'
	        	}
				dir('officefloor/eclipse') {
				    sh 'mvn clean install -P OXYGEN.target'
					sh 'mvn clean install -P NEON.target'
				}
			}
		}
	    
	    stage('Stage') {
	        when {
	        	allOf {
	        	    branch 'master'
					expression { params.BUILD_TYPE == 'STAGE' }
	        	}
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
	
    post {
   		always {
	    	junit 'officefloor/**/target/surefire-reports/TEST-*.xml'
	    	junit 'officefloor/**/target/failsafe-reports/TEST-*.xml'
	    }
	    success {
	        mail to: 'daniel@officefloor.net', subject: "OF ${params.BUILD_TYPE} successful", body: currentBuild.rawBuild.getLog(100)
	    }
		failure {
	        mail to: 'daniel@officefloor.net', subject: "OF ${params.BUILD_TYPE} failed", body: currentBuild.rawBuild.getLog(100)
		}
	}

}
