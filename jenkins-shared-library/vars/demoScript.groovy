/**
 * Jenkins Pipeline Step for Demo Script with YAML Configuration
 * 
 * This step implements the sophisticated YAML-based configuration system
 * with hierarchical inheritance: Global → Team → Suite → Test → Custom Parameters
 * 
 * Usage Examples:
 *   demoScript team: 'frontend', suite: 'ui-tests', test: 'smoke-test'
 *   demoScript team: 'backend', suite: 'unit-tests', test: 'service-test', customParams: [greeting: 'Custom Hello!']
 * 
 * @param team - Team name (frontend, backend)
 * @param suite - Test suite name (ui-tests, api-tests, unit-tests, integration-tests)
 * @param test - Test name (smoke-test, regression-test, service-test, etc.)
 * @param customParams - Optional map of custom parameters to override defaults
 */
import groovy.json.JsonOutput
import org.yaml.snakeyaml.Yaml
// import static Utils.*

def call(Map args) {
    // Validate required parameters
    def team = args.team
    def suite = args.suite
    def test = args.test
    def customParams = args.customParams ?: [:]
    
    if (!team || !suite || !test) {
        error("Missing required parameters. Usage: demoScript team: 'frontend', suite: 'ui-tests', test: 'smoke-test'")
    }
    
    echo "=" * 60
    echo "🚀 JENKINS SHARED LIBRARY: DEMO SCRIPT EXECUTION"
    echo "=" * 60
    echo "Team: ${team} | Suite: ${suite} | Test: ${test}"
    
    try {
        // 1. Load global configuration from shared library
        echo "\n📚 Step 1: Loading global configuration from shared library..."
        def globalInfo = [:]
        try {
            def globalConfigText = libraryResource('config/global.yml')
            echo "📄 Global config file loaded, parsing YAML..."
            def globalConfig = readYaml text: globalConfigText
            globalInfo = globalConfig.GLOBAL_SETTINGS ?: [:]
            echo "✅ Global configuration loaded successfully"
        } catch (Exception e) {
            echo "⚠️ Warning: Could not load global config from shared library: ${e.message}"
            echo "📝 Using default global configuration..."
            globalInfo = [
                greeting: "Hello",
                environment: "development",
                logLevel: "INFO",
                timeout: 30,
                enableNotifications: true,
                emailFrom: "jenkins@company.com",
                emailSubject: "Build Notification: \${appName}",
                outputFormat: "json",
                includeTimestamp: true
            ]
        }
        
        // 2. Load team configuration from repository
        echo "\n👥 Step 2: Loading team configuration for: ${team}"
        def teamConfig = [:]
        try {
            // In production, you might store team configs in shared library resources too
            // For now, we'll checkout the repository to get team configs
            checkout([
                $class: 'GitSCM',
                branches: [[name: 'main']],
                userRemoteConfigs: [[
                    url: 'https://github.com/grvsoni/groovy-demo.git'
                ]]
            ])
            
            def teamConfigText = readFile "teams/${team}/markup.yml"
            teamConfig = readYaml text: teamConfigText
            echo "✅ Team configuration loaded for ${team}"
        } catch (Exception e) {
            echo "⚠️ Warning: Could not load team config for ${team}: ${e.message}"
            teamConfig = [common: [:]]
        }
        
        // 3. Extract configuration hierarchy
        echo "\n⚙️ Step 3: Building configuration hierarchy..."
        def teamInfo = teamConfig.common ?: [:]
        teamInfo.remove("suites")
        
        def suiteInfo = [:]
        if (teamConfig.common?.suites?."${suite}") {
            suiteInfo = teamConfig.common.suites."${suite}"
            suiteInfo.remove("tests")
            echo "✅ Suite configuration found for ${suite}"
        } else {
            echo "⚠️ Suite '${suite}' not found, using defaults"
        }
        
        def testInfo = [:]
        if (teamConfig.common?.suites?."{suite}"?.tests?."{test}") {
            testInfo = teamConfig.common.suites."{suite}".tests."{test}"
            echo "✅ Test configuration found for ${test}"
        } else {
            echo "⚠️ Test '${test}' not found, using defaults"
        }
        
        // 4. Combine configurations (hierarchy: global < team < suite < test < custom)
        echo "\n🔄 Step 4: Merging configuration hierarchy..."
        def config = [:]
        config.putAll(globalInfo)
        config.putAll(teamInfo)
        config.putAll(suiteInfo)
        config.putAll(testInfo)
        config.putAll(customParams)
        
        echo "✅ Configuration hierarchy merged successfully"
        
        // 5. Display final configuration
        echo "\n📋 Step 5: Final Configuration Summary:"
        echo "  • App Name: ${config.appName ?: 'N/A'}"
        echo "  • Environment: ${config.environment ?: 'N/A'}"
        echo "  • Greeting: ${config.greeting ?: 'N/A'}"
        echo "  • Build Tool: ${config.buildTool ?: 'N/A'}"
        echo "  • Test Command: ${config.testCommand ?: 'N/A'}"
        echo "  • Timeout: ${config.timeout ?: 'N/A'} seconds"
        echo "  • Log Level: ${config.logLevel ?: 'N/A'}"
        
        // 6. Execute the test (simulated)
        echo "\n🧪 Step 6: Executing Test..."
        echo "Greeting: ${config.greeting}"
        echo "Running: ${config.testCommand ?: 'default test command'}"
        echo "Environment: ${config.environment}"
        echo "✅ Test execution completed successfully!"
        
        // 7. Generate output
        echo "\n📊 Step 7: Generating Output..."
        def output = [
            status: "SUCCESS",
            team: team,
            suite: suite,
            test: test,
            appName: config.appName,
            environment: config.environment,
            greeting: config.greeting,
            timestamp: new Date().format("yyyy-MM-dd HH:mm:ss")
        ]
        
        if (config.outputFormat == "json") {
            echo "JSON Output:"
            def jsonOutput = writeJSON returnText: true, json: output
            echo jsonOutput
        }
        
        // 8. Send notifications
        if (config.enableNotifications) {
            echo "\n📧 Step 8: Sending Notifications..."
            echo "From: ${config.emailFrom}"
            echo "To: ${config.emailTo ?: 'default@company.com'}"
            echo "Subject: ${config.emailSubject?.replace('${appName}', config.appName ?: 'Demo App')}"
            echo "Message: ${config.greeting} - Build completed successfully!"
            echo "✅ Notification sent!"
        }
        
        echo "\n" + "=" * 60
        echo "🎉 DEMO SCRIPT EXECUTION COMPLETED SUCCESSFULLY!"
        echo "=" * 60
        
        return [
            status: 'SUCCESS',
            config: config,
            output: output
        ]
        
    } catch (Exception e) {
        echo "\n❌ Error during demo script execution: ${e.message}"
        echo "=" * 60
        error("Demo script execution failed: ${e.message}")
    }
}