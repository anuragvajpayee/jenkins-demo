import groovy.json.JsonOutput
import org.yaml.snakeyaml.Yaml

// Jenkins shared library utility functions
def readYaml(Map params) {
    Yaml yaml = new Yaml()
    if (params.text) {
        return yaml.load(params.text)
    } else if (params.file) {
        return yaml.load(new File(params.file).text)
    }
}
*/

// Default call method - not typically used but required for Jenkins
def call(Map args = [:]) {
    error("Utils is a utility library. Use utils.methodName() to call specific methods.")
}
