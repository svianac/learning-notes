jobs:
  - script: >
      job('reload-jcasc-configuration') {
          description('Job to reload Jenkins Configuration as Code (JCasC) configuration.')
          steps {
              dsl {
                  scriptText('''
                      job("reload-jcasc-configuration") {
                          description("Reloads the JCasC configuration without restarting Jenkins.")
                          steps {
                              systemGroovyCommand('''
                                  import jenkins.model.*
                                  import io.jenkins.plugins.casc.ConfigurationAsCode

                                  def jcasC = Jenkins.instance.getExtensionList(ConfigurationAsCode.class)[0]
                                  if (jcasC) {
                                      jcasC.configure()
                                      println("JCasC configuration reloaded successfully.")
                                  } else {
                                      println("JCasC plugin is not available.")
                                  }
                              ''')
                          }
                      }
                  ''')
              }
          }
      }