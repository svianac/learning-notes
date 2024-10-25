pipeline {
    agent any
    stages {
        stage('Reload JCasC Configuration') {
            steps {
                script {
                    // Trigger a configuration reload in the JCasC plugin
                    def jenkinsInstance = jenkins.model.Jenkins.instance
                    def jcasC = jenkinsInstance.getExtensionList('io.jenkins.plugins.casc.ConfigurationAsCode')[0]

                    if (jcasC) {
                        jcasC.configure()  // Reloads the configuration
                        echo "Jenkins Configuration as Code reloaded successfully."
                    } else {
                        error "JCasC plugin is not available on this Jenkins instance."
                    }
                }
            }
        }
    }
}