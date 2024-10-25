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
### also templates
import jenkins.model.Jenkins
import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud

// Get the Jenkins instance
def jenkinsInstance = Jenkins.instance

// Fetch the list of all configured clouds
jenkinsInstance.clouds.each { cloud ->
    def cloudName = cloud.displayName ?: cloud.getClass().getSimpleName()
    println "Cloud Name: ${cloudName}"
    
    // Check if the cloud is of type KubernetesCloud
    if (cloud instanceof KubernetesCloud) {
        println "  Pod Templates:"
        
        // Iterate over each pod template in this Kubernetes cloud
        cloud.templates.each { podTemplate ->
            println "    - ${podTemplate.name}"
        }
    } else {
        println "  (No pod templates for this cloud type)"
    }
    
    println ""
}