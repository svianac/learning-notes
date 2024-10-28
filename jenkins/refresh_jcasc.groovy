jobs:
  - script: >
      job('seed-job') {
          description('Seed job to create a job that reloads JCasC configuration.')
          steps {
              dsl {
                  scriptText('''
                      job("reload-jcasc-configuration") {
                          description("Reloads the JCasC configuration without restarting Jenkins.")
                          steps {
                              systemGroovyCommand("def jcasC = jenkins.model.Jenkins.instance.getExtensionList(io.jenkins.plugins.casc.ConfigurationAsCode.class)[0]; if (jcasC) { jcasC.configure(); println(\\"JCasC configuration reloaded successfully.\\"); } else { println(\\"JCasC plugin is not available.\\"); }")
                          }
                      }
                  ''')
              }
          }
      }