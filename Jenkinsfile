@Library('groovy-demo-shared-library@main') _

pipeline {
    agent any
    
    stages {
        stage('Execute Demo Script') {
            steps {
                script {
                    // Build custom parameters map
                    def customParams = [:]
                    if (params.CUSTOM_GREETING) {
                        customParams.greeting = params.CUSTOM_GREETING
                    }
                    if (params.CUSTOM_ENVIRONMENT) {
                        customParams.environment = params.CUSTOM_ENVIRONMENT
                    }
                    
                    // Call the shared library step with error handling
                    try {
                        def result = demoScript(
                            team: params.TEAM ?: 'frontend',
                            suite: params.SUITE ?: 'ui-tests',
                            test: params.TEST ?: 'smoke-test',
                            customParams: customParams
                        )
                        
                        if (result && result.status) {
                            echo "✅ Demo script completed with status: ${result.status}"
                        } else {
                            echo "✅ Demo script completed successfully"
                        }
                    } catch (Exception e) {
                        echo "❌ Demo script execution failed: ${e.message}"
                        throw e
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo "Demo script execution completed!"
        }
        success {
            echo "🎉 Demo script executed successfully!"
        }
        failure {
            echo "❌ Demo script execution failed!"
        }
    }
}
