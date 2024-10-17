import requests
from requests.auth import HTTPBasicAuth

# Jenkins details
jenkins_url = "<jenkinsUrl>"
username = "<your-username>"
api_token = "<your-api-token>"

# Jenkins JCasC URL
jcas_config_url = f"{jenkins_url}/manage/configuration-as-code/download"

# Download the YAML configuration
def download_jcasc_yaml():
    try:
        response = requests.get(jcas_config_url, auth=HTTPBasicAuth(username, api_token))

        if response.status_code == 200:
            # Save the YAML configuration to a file
            with open("jenkins-configuration.yaml", "w") as file:
                file.write(response.text)
            print("Configuration as Code YAML file downloaded successfully.")
        else:
            print(f"Failed to download YAML: {response.status_code}, {response.text}")
    
    except Exception as e:
        print(f"Error: {str(e)}")

# Run the function
download_jcasc_yaml()