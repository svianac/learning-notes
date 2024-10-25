// Import Jenkins classes
import jenkins.model.Jenkins

// Get the Jenkins instance
def jenkinsInstance = Jenkins.instance

// Fetch the list of all configured clouds
def clouds = jenkinsInstance.clouds

// Check if there are any configured clouds
if (clouds.isEmpty()) {
    println "No clouds are configured in Jenkins."
} else {
    println "Configured clouds in Jenkins:"
    clouds.each { cloud ->
        // Print cloud details
        println "- Name: ${cloud.name} (Class: ${cloud.getClass().getSimpleName()})"
    }
} 

#### only name
import jenkins.model.Jenkins

// Get the Jenkins instance
def jenkinsInstance = Jenkins.instance

// Fetch the list of all configured clouds and print only their names
jenkinsInstance.clouds.each { cloud ->
    println cloud.name
}