pipeline {
	agent any

    parameters {
        choice(name: 'BUILD_TYPE', choices: [ 'TEST', 'PERFORMANCE', 'STAGE', 'RELEASE', 'SITE', 'TAG_RELEASE' ], description: 'Indicates what type of build')
        string(name: 'LATEST_JDK', defaultValue: 'jdk11', description: 'Tool name for the latest JDK to support')
		string(name: 'OLDEST_JDK', defaultValue: 'jdk8', description: 'Tool name for the oldest JDK to support')
    }
    
    environment {
        PERFORMANCE_EMAIL = credentials('performance-email')
        RESULTS_EMAIL = credentials('results-email')
        REPLY_TO_EMAIL = credentials('reply-to-email')
    }
    
    triggers {
        parameterizedCron('''
0 1 * * * %BUILD_TYPE=TEST
0 4 * * * %BUILD_TYPE=PERFORMANCE
''')
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
	
		stage('Backwards compatible') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'TEST' }
				}
			}
			tools {
            	jdk "${params.OLDEST_JDK}"
            }
			steps {
	        	dir('officefloor/bom') {
	        		sh 'mvn clean'
					sh 'mvn -Dofficefloor.skip.stress.tests=true install'
	        	}
				dir('officefloor/eclipse') {
					sh 'mvn clean'
				    sh 'mvn install -P OXYGEN.target'
				}
			}
		}
	
		stage('Build') {
		    when {
		        expression { params.BUILD_TYPE == 'TEST' }
		    }
	        steps {
	        	dir('officefloor/bom') {
	        	    sh 'mvn clean'
	        	}
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
					sh 'mvn -Dofficefloor.skip.stress.tests=true verify'
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
	    	    
	    stage('Check master') {
	        when {
	        	allOf {
					not { expression { params.BUILD_TYPE == 'TEST' } }
	        	    not { branch 'master' }
	        	}
	        }
            steps {
            	script {
            		currentBuild.result = 'ABORTED'
            	}
            	error "Attempting to ${params.BUILD_TYPE} when not on master branch"
            }
        }

		stage('Performance') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'PERFORMANCE' }
					branch 'master'
				}
			}
			steps {
	        	sh 'mvn -version'
				sh './benchmarks/run_comparison.sh'
			}
			post {
			    always {
	    			junit allowEmptyResults: true, testResults: 'benchmarks/test/**/TEST-*.xml'

					emailext to: "${PERFORMANCE_EMAIL}", replyTo: "${REPLY_TO_EMAIL}", subject: 'OF ' + "${params.BUILD_TYPE}" + ' RESULTS (${BUILD_NUMBER})', attachmentsPattern: 'benchmarks/results.txt, benchmarks/results.zip', body: '''
${PROJECT_NAME} - ${BUILD_NUMBER} - ${BUILD_STATUS}
'''
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
	        	dir('officefloor/bom') {
			    	sh 'mvn -DskipTests -Dofficefloor-deploy=sourceforge clean deploy'
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
	        	sh 'mvn -version'
	        	dir('officefloor/bom') {
					sh 'mvn -Dmaven.test.failure.ignore=true -Dofficefloor-deploy=sonatype clean deploy'
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
	        	dir('officefloor') {
					sh 'mvn -DskipTests clean install'
				}
				dir('officefloor/bom') {
					sh 'mvn -DskipTests -Dofficefloor-deploy=sourceforge clean install site-deploy'
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
				dir('officefloor') {
					sh 'mvn scm:tag'
				}
			}         
	    }
	}
	
    post {
   		always {
			junit allowEmptyResults: true, testResults: 'officefloor/**/target/surefire-reports/TEST-*.xml'
			junit allowEmptyResults: true, testResults: 'officefloor/**/target/failsafe-reports/TEST-*.xml'

    		emailext to: "${RESULTS_EMAIL}", replyTo: "${REPLY_TO_EMAIL}", subject: 'OF ' + "${params.BUILD_TYPE}" + ' ${BUILD_STATUS}! (${BRANCH_NAME} ${BUILD_NUMBER})', body: '''
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
