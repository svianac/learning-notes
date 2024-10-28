jobs:
  - script: >
      job('reload-jcasc-configuration') {
          description('This job reloads the JCasC configuration.')
          steps {
              systemGroovyCommand('''
                  def jcasC = jenkins.model.Jenkins.instance.getExtensionList(io.jenkins.plugins.casc.ConfigurationAsCode.class)[0]
                  if (jcasC) {
                      jcasC.configure()
                      println("JCasC configuration reloaded successfully.")
                  } else {
                      println("JCasC plugin is not available.")
                  }
              ''')
          }
      }