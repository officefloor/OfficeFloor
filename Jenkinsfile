pipeline {
	agent any

    parameters {
        choice(name: 'BUILD_TYPE', choices: [ 'TEST', 'STAGE', 'RELEASE', 'SITE', 'TAG_RELEASE' ], description: 'Indicates what type of build')
        string(name: 'LATEST_JDK', defaultValue: 'jdk11', description: 'Tool name for the latest JDK to support')
	string(name: 'OLDEST_JDK', defaultValue: 'jdk8', description: 'Tool name for the oldest JDK to support')
	string(name: 'HOMEBREW_BIN_DIR', defaultValue: '/home/linuxbrew/.linuxbrew/bin', description: 'Location of Homebrew bin (e.g. for AWS sam)')
    }
    
    environment {
        RESULTS_EMAIL = credentials('results-email')
        REPLY_TO_EMAIL = credentials('reply-to-email')
	    PATH = "${params.HOMEBREW_BIN_DIR}:${PATH}"
    }
    
    triggers {
        parameterizedCron('''
H 1 * * * %BUILD_TYPE=TEST
''')
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
		disableConcurrentBuilds()
		timeout(time: 16, unit: 'HOURS')
    }

	tools {
	    maven 'maven-3.6.0'
	    jdk "${params.LATEST_JDK}"
	}
	
	stages {
	
		stage('Set build name') {
			steps {
				script {
				    currentBuild.displayName = "#${BUILD_NUMBER} (${params.BUILD_TYPE})"
				}
			}
		}
		
		stage('Check external environment') {
			steps {
				script {
					if (fileExists("${params.HOMEBREW_BIN_DIR}/sam")) {
						echo "sam command available"
					} else {
						error "AWS sam command NOT available at ${params.HOMEBREW_BIN_DIR}/sam.  Please following AWS instructions to install sam."
					}
				}
			}
		}
	
		stage('Check master') {
	        when {
	        	allOf {
	        	    not { branch 'master' }
	        	}
	        }
            steps {
            	script {
            		currentBuild.result = 'ABORTED'
            	}
            	error "Aborting ${params.BUILD_TYPE} as not on master branch"
            }
        }
	
		stage('Build') {
		    when {
		    	allOf {
			        expression { params.BUILD_TYPE == 'TEST' }
					branch 'master'
		    	}
		    }
	        steps {
	        	echo "JAVA_HOME = ${env.JAVA_HOME}"
	        	dir('officefloor/bom') {
	        	    sh 'mvn -B -V -e -DskipTests -DskipITs -Dmaven.javadoc.skip=true -Darchetype.test.skip=true clean install'
	        	}
	        }
		}
	
	    stage('Test') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'TEST' }
    				branch 'master'
				}
			}
	        steps {
	        	dir('officefloor/bom') {
					sh 'mvn -B -V -e -Dmaven.test.failure.ignore=true verify'
	        	}
	        }
		    post {
			    always {
					junit allowEmptyResults: true, testResults: 'officefloor/**/target/surefire-reports/TEST-*.xml'
					junit allowEmptyResults: true, testResults: 'officefloor/**/target/failsafe-reports/TEST-*.xml'
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
	        	sh 'mvn -version'
	        	echo "JAVA_HOME = ${env.JAVA_HOME}"
	        	dir('officefloor/bom') {
					sh 'mvn -B -V -e clean'
					sh 'mvn -B -V -e -Dofficefloor.skip.stress.tests=true -Dmaven.test.failure.ignore=true install'
	        	}
			}
		}

		stage('Eclipse versions compatible') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'TEST' }
					branch 'master'
				}
			}
			steps {
	        	sh 'mvn -version'
	        	echo "JAVA_HOME = ${env.JAVA_HOME}"
				dir('officefloor/editor') {
					// Clean build with different Eclipse target
					// Note: latest Eclipse target is default build
					sh 'mvn -B -V -e clean'
					sh 'mvn -B -V -e -P PHOTON.target install'
					sh 'mvn -B -V -e clean'
				    sh 'mvn -B -V -e -P OXYGEN.target install'
				}
			}
		}

	    stage('Stage') {
	        when {
	        	allOf {
					expression { params.BUILD_TYPE == 'STAGE' }
					branch 'master'
	        	}
	        }
			tools {
				// Allow release to be backwards compatible to oldest JVM
            	jdk "${params.OLDEST_JDK}"
            }
	        steps {
	        	sh 'mvn -version'
	        	echo "JAVA_HOME = ${env.JAVA_HOME}"
	        	dir('officefloor/bom') {
			    	sh 'mvn -B -V -e -DskipTests -Dofficefloor-deploy=github clean deploy'
			    }
	        }
	    }
	    
	    stage('Pre release test') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'RELEASE' }
					branch 'master'
				}
			}
			tools {
				// Allow release to be backwards compatible to oldest JVM
            	jdk "${params.OLDEST_JDK}"
            }
			steps {
	        	echo "JAVA_HOME = ${env.JAVA_HOME}"
	        	dir('officefloor/bom') {
					sh 'mvn -B -V -e -DskipStress -Dofficefloor-deploy=sonatype clean install'
				}
			}
	    }

	    stage('Release') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'RELEASE' }
				    branch 'master'
				}
			}
			tools {
				// Allow release to be backwards compatible to oldest JVM
            	jdk "${params.OLDEST_JDK}"
            }
			steps {
				emailext to: "${RESULTS_EMAIL}", replyTo: "${REPLY_TO_EMAIL}", subject: 'OfficeFloor starting release (${BRANCH_NAME} ${BUILD_NUMBER})', body: '''
Starting release
'''
	        	echo "JAVA_HOME = ${env.JAVA_HOME}"
	        	dir('officefloor/bom') {
					sh 'mvn -B -V -e -Dmaven.test.failure.ignore=true -Dofficefloor-deploy=sonatype clean deploy'
				}
			}
			post {
			    always {
					junit allowEmptyResults: true, testResults: 'officefloor/**/target/surefire-reports/TEST-*.xml'
					junit allowEmptyResults: true, testResults: 'officefloor/**/target/failsafe-reports/TEST-*.xml'
			    }
			}
	    }

	    stage('Deploy Site') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'SITE' }
				    branch 'master'
				}
			}
			steps {
	        	sh 'mvn -version'
	        	echo "JAVA_HOME = ${env.JAVA_HOME}"
	        	dir('officefloor/bom') {
					// Build (and open shell to SourceForge)
					sh 'mvn -B -V -e -DskipTests clean install'
				}
				dir('officefloor') {
					sh 'mvn -B -V -e -Dofficefloor-deploy=site site-deploy'
				}
			}
	    }

	    stage('Tag Release') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'TAG_RELEASE' }
				    branch 'master'
				}
			}
			steps {
	        	sh 'mvn -version'
	        	echo "JAVA_HOME = ${env.JAVA_HOME}"
				dir('officefloor/bom') {
					sh 'mvn scm:tag'
				}
			}         
	    }
	}
	
    post {
   		always {
            script {
   				if (currentBuild.result != 'ABORTED') {
	    			emailext to: "${RESULTS_EMAIL}", replyTo: "${REPLY_TO_EMAIL}", subject: 'OfficeFloor ' + "${params.BUILD_TYPE}" + ' ${BUILD_STATUS}! (${BRANCH_NAME} ${BUILD_NUMBER})', body: '''
${PROJECT_NAME} - ${BUILD_NUMBER} - ${BUILD_STATUS}

Tests:
Passed: ${TEST_COUNTS,var="pass"}
Failed: ${TEST_COUNTS,var="fail"}
Skipped: ${TEST_COUNTS,var="skip"}
Total: ${TEST_COUNTS,var="total"}

${FAILED_TESTS}


Changes (since last successful build):
${CHANGES_SINCE_LAST_SUCCESS}


Log (last lines):
...
${BUILD_LOG}
'''
				}
			}
		}
	}

}
