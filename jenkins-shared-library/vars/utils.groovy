import groovy.json.JsonOutput
import org.yaml.snakeyaml.Yaml

class Utils {
    // Emulation of Jenkins writeJSON step
/*    static def writeJSON(Map params) {
        def jsonContent = JsonOutput.toJson(params.json)
        if (params.returnText) {
            return JsonOutput.prettyPrint(jsonContent)
        } else if (params.file) {
            new File(params.file).write(JsonOutput.prettyPrint(jsonContent))
        }
    }
    
    // Emulation of Jenkins readYaml step
    static def readYaml(Map params) {
        Yaml yaml = new Yaml()
        if (params.text) {
            return yaml.load(params.text)
        } else if (params.file) {
            return yaml.load(new File(params.file).text)
        }
    }
    
    static def echo(message) {
        println message
    }
*/
    static def customEcho(message){
        println ">>>>> Custom"
        println message
    }
}