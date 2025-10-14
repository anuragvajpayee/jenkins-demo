import groovy.json.JsonOutput
import org.yaml.snakeyaml.Yaml

// Jenkins shared library utility functions
def readYaml(String yaml) {
    echo "The utility to print YAML"
    echo "=============================="
    echo "${yaml}"
    echo "=============================="
}
*/

// Default call method - not typically used but required for Jenkins
def call(Map args = [:]) {
    error("Utils is a utility library. Use utils.methodName() to call specific methods.")
}
