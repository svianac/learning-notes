# Notes taken during the course - 27/10/2023

- [Notes taken during the course - 27/10/2023](#notes-taken-during-the-course---27102023)
  - [Understanding GitOps](#understanding-gitops)
  - [Introduction to Argo CD](#introduction-to-argo-cd)
  - [Why to use Argo CD?](#why-to-use-argo-cd)
  - [Install Argo CD on your cluster (Helm)](#install-argo-cd-on-your-cluster-helm)
  - [Git Repository Structure for GitOps](#git-repository-structure-for-gitops)
    - [Manifests](#manifests)
    - [Helm](#helm)
    - [Kustomize](#kustomize)
    - [Choosing the Right Tool](#choosing-the-right-tool)
  - [GitOps best practices](#gitops-best-practices)
  - [Argo CD Architecture](#argo-cd-architecture)
    - [API server](#api-server)
    - [Repository server](#repository-server)
    - [Application controller](#application-controller)
    - [Redis](#redis)
  - [Argo CD best practices](#argo-cd-best-practices)
  - [Argo CD deployment](#argo-cd-deployment)
    - [Add a repository](#add-a-repository)
  - [Deploying Helm charts to Argo CD](#deploying-helm-charts-to-argo-cd)
    - [From a remote registry](#from-a-remote-registry)
    - [When to use packaged charts?](#when-to-use-packaged-charts)
    - [When to use local charts?](#when-to-use-local-charts)
  - [Deploying application to Argo CD using Kustomize](#deploying-application-to-argo-cd-using-kustomize)
  - [Managing secrets in GitOps](#managing-secrets-in-gitops)
    - [Sealed Secrets Overview](#sealed-secrets-overview)
    - [Lab: Implementing Sealed Secrets with Argo CD.](#lab-implementing-sealed-secrets-with-argo-cd)
  - [Synchronization and Rollbacks](#synchronization-and-rollbacks)
  - [Rollbacks practical example](#rollbacks-practical-example)
  - [Multi-Cluster Deployment in Argo CD](#multi-cluster-deployment-in-argo-cd)
  - [Introducing Argo CD ApplicationSets](#introducing-argo-cd-applicationsets)
  - [Implementing Blue-Green Deployments](#implementing-blue-green-deployments)
  - [Implementing Canary Deployments](#implementing-canary-deployments)


## Understanding GitOps

- Set of practices that leverages Git as the single source of truth for declarative infrastructure and application configurations.
- Enables teams to streamline their application delivery process, automate deployments and improve collaboration. 

The four principles of GitOps

1. Declarative configuration.
2. Version control.
3. Automated Synchronization. 
4. Continuos Feedback. 

Benefits:

1. Increase productivity.
2. Improve collaboration.
3. Enhanced Security.
4. Faster Recovery. 

Argo CD will be the GitOps tool of this course.  

## Introduction to Argo CD

Argo CD is a declarative and GitOps continuos delivery tool for Kubernetes. It uses Git as single source of truth and it can manage multiple Kubernetes environments. 
The main key features are:

- Declarative and Versioned (GitOps strategy).
- Multi-Cluster Support (it can manage several clusters at the same time).
- Automated Sync and Rollbacks.
- Supports different deployment strategies. 
- It is extensible and can be used with tools such as Helm or Kustomization. 

It follows a client-server architecture:

- Argo CD API Server.
- Repository Server.
- Application Controller.
- Argo CD CLI.

Advantages of using Argo CD:

- Streamlined Deployments.
- Enhanced Collaboration.
- Improve security.
- Faster Incident Response.
- Scalability.

## Why to use Argo CD?

Traditional deployments methods often lack the necessary automation, consistency and reliability needed in modern environments. However, GitOps relies on Git as single source of truth for declarative infrastructure and provides a clear, version-controlled history of changes. 

Argo CD is a popular choice to manage GitOps workflows:

- Kubernetes-native.
- Provides automated deployments.
- Supports various configuration management tools. 
- Enhances security and compliance.
- Facilitates collaboration and transparency.

Use cases of Argo CD: Intuit, Red Hat, IBM, etc. 

## Install Argo CD on your cluster (Helm)

To get Helm
```yaml
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh
```

We add the argo helm repo

```yaml
helm repo add argo https://argoproj.github.io/argo-helm
```

We will create a separated argo cd kunernetes namespace and install it here with Helm. 

```yaml
kubectl create namespace argocd
helm install argocd -n argocd argo/argo-cd
```
Once we installed it, we follow the instructions:

```yaml
ubuntu@microk8s-vm:~$ helm install argocd -n argocd argo/argo-cd
WARNING: Kubernetes configuration file is group-readable. This is insecure. Location: /home/ubuntu/.kube/config
WARNING: Kubernetes configuration file is world-readable. This is insecure. Location: /home/ubuntu/.kube/config
NAME: argocd
LAST DEPLOYED: Mon Aug  7 20:10:58 2023
NAMESPACE: argocd
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
In order to access the server UI you have the following options:

1. kubectl port-forward service/argocd-server -n argocd 8090:443 --address="0.0.0.0"

    and then open the browser on http://localhost:8090 and accept the certificate

2. enable ingress in the values file `server.ingress.enabled` and either
      - Add the annotation for ssl passthrough: https://argo-cd.readthedocs.io/en/stable/operator-manual/ingress/#option-1-ssl-passthrough
      - Set the `configs.params."server.insecure"` in the values file and terminate SSL at your ingress: https://argo-cd.readthedocs.io/en/stable/operator-manual/ingress/#option-2-multiple-ingress-objects-and-hosts


After reaching the UI the first time you can login with username: admin and the random password generated during the installation. You can find the password by running:

kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
admin:ztHtn6o5fpViZ7D7

(You should delete the initial secret afterwards as suggested by the Getting Started Guide: https://argo-cd.readthedocs.io/en/stable/getting_started/#4-login-using-the-cli)
```

Now we can install the Argo CD CLI.

```yaml
curl -sSL -o argocd-linux-amd64 https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
sudo install -m 555 argocd-linux-amd64 /usr/local/bin/argocd
rm argocd-linux-amd64
```

Finally, we login and we can change admin password:

```yaml
argocd login 10.152.183.27
argocd repo list
argocd account update-password --current-password ztHtn6o5fpViZ7D7 --new-password ztHtn7o9fpVoZ7r6
```

## Git Repository Structure for GitOps

Best practices:
- User a single repository for application or environment.
- Use branches to manage different stages of the development and deployment process.
- Store configuration data in a separate directory from application code.
- Use descriptive names for directories and files.
- Use Git Submodules to manage shared configuration data. 

### Manifests

They: 
- Are simple and easy to understand.
- Provide a clear and complete picture of the desired state of the kubernetes object.
- Can be customized to meet specific requirements.  

But:

- Can become cumbersome to manage when number increases.
- Require manual updates when changes are made.
- Difficult to reuse across different environments.
- Managing secrets in manifests can be a security risk.

### Helm

A package manager for Kubernetes. 

- It provides a way to package, distribute, and manage kubernetes applications as a single unit.
- Allow for parameterization, so you can reuse a chart with different values based on your environment. 

But:

- It can become complex to create and manage.
- They can introduce risk to your deployment pipeline.

### Kustomize

- It provides a way to customize kubernetes objects without modifying the original YAML files.
- It provides a way to manage complex configurations and apply multiple customizations in a predictable and repeatable manner.
  
But: 

- It can be complex to create and manage, especially for complex configurations.
- Requires an understanding of YAML and kubernetes resources, as well as a familiarity with Kustomize configuration files and parches.

### Choosing the Right Tool

- Consider the complexity of your configuration, the number of objects you need to manage, and your team's experience and expertise.
- Manifests are a good choice for small to medium-sized deployments with a limited number of objects and simple configurations. 
- Helm Charts are a good choice for managing complex applications with multiple objects and configurations.
- Kustomize is a good choice for customizing YAML files or generating new ones based on a set of rules and patches. 

## GitOps best practices

- Use version control for all your infrastructure code.
- Follow a pull-based model for deployments.
- Ensure that all changes are auditable and traceable.
- Automate as much as possible. 
- Ensure that all configurations are tested and validated before deployment.
- Implement best security practices.

## Argo CD Architecture

Main compoments: 

### API server

- The central component of the architecture.
- Processes API requests from users.
- Interacts with the Kubernetes API.
- Monitor the state of the cluster.
- Built on top of the Kubernetes API server.
- Uses Kubernetes RBAC for authentication and authorization.

### Repository server

- Manages the Git repositories that contain the configuration and deployments instructions.
- Interacts with Git to pull the configuration and deployments data.
- Stores the data in a local cache for faster retrieval.

### Application controller

- Updates the Kubernetes objects based on data retrieved from Git.
- Uses Kubernetes controllers (Deployments, StatefulSets, etc) to manage the cluster state.
  
### Redis

- Stores metadata about the applications and resources in the cluster.
- For example: the status of the application deployments, the cluster resources state, and the history of changes. 

## Argo CD best practices

1. Use Git as the source of truth for the configuration and deployment information.
2. Use a version control system for the Git repository, such as GitHub or GitLab.
3. Use Kubernetes namespaces to organize and manage the resources in the cluster.
4. Use Kubernetes RBAC to control access to the resources in the cluster.
5. Use Helm charts or Kustomize to manage the deployment of complex applications. 

## Argo CD deployment

### Add a repository

We have several methods. In this case, we will set up the authentication using a Personal access token from github that we created previously.

```yaml
argocd login 10.152.183.27
argocd repo add https://github.com/svianac/learning-notes.git --username "xxx@gmail.com" --password "Personal access token"

Repository 'https://github.com/svianac/learning-notes.git' added
```
Above task created a Kubernetes secret that contains the login data. 

```yaml
kubectl get secrets -n argocd
kubectl describe secret repo-1833370407 -n argocd

Name:         repo-1833370407
Namespace:    argocd
Labels:       argocd.argoproj.io/secret-type=repository
Annotations:  managed-by: argocd.argoproj.io

Type:  Opaque

Data
====
username:  26 bytes
password:  40 bytes
type:      3 bytes
url:       45 bytes
```

We can also see the new repo here: https://10.149.3.214:8090/settings/repos

Once we added the repo, we need to connect the Argo CD to our application present our nginx.yml manifest from here: https://github.com/svianac/learning-notes/blob/main/argocd-app-sample/nginx.yml. 

We checkout our repo and apply the nginx.yml defined. 

```yaml
# nginx.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: nginx
  namespace: argocd
spec:
  project: default
  source:
    repoURL: 'https://github.com/svianac/learning-notes.git'
    path: argocd-app-sample
    targetRevision: main
  destination:
    server: 'https://kubernetes.default.svc'
    namespace: default
  syncPolicy:
    automated:
      selfHeal: true
      prune: true
```


```yaml
git clone git@github.com:svianac/learning-notes.git
cd learning-notes/argocd-app-sample/
kubectl apply -f nginx.yml 

application.argoproj.io/nginx created
```
Now if we visit https://10.149.3.214:8090/applications we will see a new application called nginx.
Now we can create the next deployment.yml in the repo. 

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
spec:
  selector:
    matchLabels:
      app: nginx
  replicas: 2 # tells deployment to run 2 pods matching the template
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:1.14.2
        ports:
        - containerPort: 80
```

After we commit the changes in the repo, due selfHeal set up to true, Argo CD will apply this manifest automatically and create the specified objects. This is called Reconcile process.

```yaml
kubectl get pods

nginx-deployment-cbdccf466-w2gvb   1/1     Running   0             2m56s
nginx-deployment-cbdccf466-592hr   1/1     Running   0             2m56s
```

## Deploying Helm charts to Argo CD

Helm is a package manager for Kubernetes that helps you to install and manage application on your cluster. It uses charts, which are packages of preconfigured Kubernetes resources, to manage deployments. 

Argo CD supports for Helm:

1. Using ready-made charts stored in remote registries. 
2. Using local charts stored in the git repository.

### From a remote registry

We will install the application [httpbin](https://httpbin.org/) (A simple HTTP Request & Response Service.) using the Helm chart available in this repo: https://matheusfm.dev/charts  

First we will create a new application for argocd to manage its own applications. The link that ties the Argo CD with the Applications manifests. 
```yaml
# argocd.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: argocd
  namespace: argocd
spec:
  project: default
  source:
    repoURL: 'https://github.com/svianac/learning-notes.git'
    path: argocd-apps/argocd
    targetRevision: main
  destination:
    server: 'https://kubernetes.default.svc'
    namespace: default
  syncPolicy:
    automated:
      selfHeal: true
      prune: true
```

And then we define another application which will be the Helm chart. 

```yaml
# httpbin.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: httpbin
  namespace: argocd
spec:
  project: default
  source:
    chart: httpbin
    repoURL: 'https://matheusfm.dev/charts'
    targetRevision: 0.1.1
    helm:
      releaseName: httpbin
  destination:
    server: 'https://kubernetes.default.svc'
    namespace: default
  syncPolicy:
    automated:
      selfHeal: true
      prune: true
```

We can add new values to the Helm by adding:

```yaml
# httpbin.yaml
[...]
    helm:
      releaseName: httpbin
      values: |
        service:
          type: NodePort
[...]
```

Once all is sync, we can see that the changes has been already applied in the kubernetes cluster.

### When to use packaged charts?

- When you don't need to modify the chart contents. Just use the vales file to customize the installation.
- When the Helm chart is stored and maintained in a different repository by a different team or organization.

### When to use local charts?

- When the chart is developed locally as part of the application.
- When you need to have control over the chart contents and not just the values file.

## Deploying application to Argo CD using Kustomize

- A kubernetes configuration management tool.
- User to customize raw, template-free YAML files for multiple purposes.
- Advantages: 
  - Allows you to create reusable configuration templates.
  - Can manage complex applications with multiple components and configurations.
  - Eliminates the need for templating languages like Helm.
- Argo CD supports Kustomize natively. 

## Managing secrets in GitOps

Importance of secrets managementÑ:

- A crucial aspect of any modern application development process.
- Should be managed securely and separately from application code.
- We need to ensure that secrets are:
  - Encrypted: To protect against unauthorized access.
  - Versioned: To maintain an audit trail and rollback capabilities.
  - Automated: To reduce manual intervention and human error.

Storing secrets in git repositories:

- We need to store Secrets in Git,
- Yet, this present a security risk.
- We can use tools like HashiCorp's Vault or Sealed Secrets to mitigate this risk.

### Sealed Secrets Overview

It is an open-source project by Bitnami. It can securely store, version, and manage secrets in a GitOps workflow using Argo CD. It has 3 components: SealedSecret CRD, KubeSeal and Sealed Secrets Controller. 

### Lab: Implementing Sealed Secrets with Argo CD.

First we install the Bitnami sealed secrets in our kubernetes cluster: https://github.com/bitnami-labs/sealed-secrets/releases 

We install the controller in Kubernetes and the command line.

```yaml
wget https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.24.3/controller.yaml 
kubectl apply -f controller.yaml

wget https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.24.3/kubeseal-0.24.3-linux-amd64.tar.gz
tar -xvf kubeseal-0.24.3-linux-amd64.tar.gz
sudo mv kubeseal /usr/local/bin
sudo chmod +x /usr/local/bin/kubeseal
```

Then, we are going to create an application that is using a secrets for property handling.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: busybox-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: busybox
  template:
    metadata:
      labels:
        app: busybox
    spec:
      containers:
      - name: busybox
        image: busybox
        command: ["sh", "-c", "while true; do sleep 3600; done"]
        env:
        - name: APIKEY
          valueFrom:
            secretKeyRef:
              name: appsecret
              key: apikey
      restartPolicy: Always
```

Now we are going to create the secret. We encode the dummy API into base64 format:

```yaml
echo api_key_2a6f1d23eabc482f9032165de5a8c7 | base64

YXBpX2tleV8yYTZmMWQyM2VhYmM0ODJmOTAzMjE2NWRlNWE4YzcK
```

Then, it is time to create the secret object:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: appsecret
type: Opaque
data:
  apikey: YXBpX2tleV8yYTZmMWQyM2VhYmM0ODJmOTAzMjE2NWRlNWE4Yzc=
```

However, now we face a security problem if we push this to our git repository. Anybody can access to the git repository and decode the base64 string, obtaining the original value of apikey.

We will use the kubeseal tool to get rid of this security problem. First we obtain the public key using:

```yaml
kubeseal --fetch-cert > publickey.pem
```

Now we encrypt the content of the secret using the following command:

```yaml
kubeseal --format=yaml --cert=publickey.pem < secret.yaml > sealedsecret.yaml
```

See the difference of the content once the secret has been sealed.

```yaml
---
apiVersion: bitnami.com/v1alpha1
kind: SealedSecret
metadata:
  creationTimestamp: null
  name: appsecret
  namespace: default
spec:
  encryptedData:
    apikey: AgBnHn16ScbC3tWgLyS9jLHTAKIAfhxzaNdK4IhoyJHvFcC8M8nMN4CbJ/L5HiZKeP/10PC45yWZJRSEOWsDHz8+g+/2bAom6w1E6dUmBDr/LqKwHkd9iKRY/FcrDf5jjdvCtDVnhki/tXaKPcWvroWmM5lFOvyHk4PEJRHVrxT51SjgG/HAFrTV02TiO8bhaFjsvgObaF3hsjTLMKScRXJfMq8KgW+/q+blCz+WiGpz+JRQg737GjuphrjfM+DMFJe/PH+rQubSI8m9y98nFcEw5llv9tAuZBRL4B4gwsLazJfN4mzsz7NsohsrNA8yrJS5Rt+R6UufPyW5mYWiWZ9Sjnm2IQoSWojiL1s1VeJ3PHwzT5q56ycWkozUSqIcMOPD1j1zGdKPXEBiJoG7S8NoAQJpYKWzg+cjjkov8dsXO4PlTzJ6GDOKq7fcu6Mwx0sEU85xzDdn/3SGQNOqXu26pQUZToeycpKsFMLzn2jtO+TM1dPS0I92QpNXE2nqOHtHAE8Myo3qKM+zeBHtoI2bmczJEWPYwhWDZEMKxzkrV9hqGHYkWZ74o93gOGbZCK5FcZRSk1gYlxjhkoCpQsrbsIj2jEqYTOZ2xJIr7ONdZ5hd0TB5pj+UBB6RwgHWmlpnxra4Bng2ny9GoFmquMKr90DzBPBogxMh6SuHdWi8GFhS/iBPdvcbGwlKw8UYH3gNtIygRELaiOTTlbj/Zq84sysknbOY7qhdgh/tB/RXp+W0BycVcQ==
  template:
    metadata:
      creationTimestamp: null
      name: appsecret
      namespace: default
    type: Opaque
```

Now we are safe to commit this to the git repository and remove the secret.yaml and publickey.pem files. 

Finally, we are going to create a new argo CD application that will use the sealed secret.

```yaml
# apiservice.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: apiservice
  namespace: argocd
spec:
  project: default
  source:
    repoURL: 'https://github.com/svianac/learning-notes.git'
    path: argocd-apps/argocd/apiservice
    targetRevision: main
  destination:
    server: 'https://kubernetes.default.svc'
    namespace: default
  syncPolicy:
    automated:
      selfHeal: true
      prune: true
```

After this, we can refresh our argocd application and a new apiservice will appear. 

Inside the pod we will manage to see the value of the environmental variable. However, this won't be possible from git perspective. As there won't be any secret.yaml. 

```yaml
kubectl exec -ti busybox-deployment-576bf8d585-bt4n2 -- sh
/ # echo $APIKEY
api_key_2a6f1d23eabc482f9032165de5a8c7
```

## Synchronization and Rollbacks 

Synchronization in GitOps:

- It is a fundamental concept in GitOps.
- It involves maintaining consistency between the desired state of your application or infrastructure as defined in your GIt repository, and the actual state in your Kubernetes cluster.
- In a GitOps workflow, synchronization is performed automatically and continuously to ensure that your environment stays up-to-date with the latest changes. 

Argo CD Sync:

- Argo CD  is a powerful tool that enables continuos synchronization of your application resources in Kubernetes with the desired state defined in your Git repository.
- When you create or update an application in Argo CD, it compares the actual state in the cluster with the desired state from the repository.
- If there are any differences, Argo CD will automatically apply the necessary changes to synchronize the states. This process is called an Argo CD sync.  

Rollbacks in GitOps:

- In software development, rollbacks are essential for quickly reverting an application to a previous stable state in case of errors or issues. 
- In GitOps, rollbacks involve reverting the changes in the Git repository to a previous commit, and then synchronizing the cluster to that commit.
- This allows you to a quickly recover from any issues and restore the desired state of your application or infrastructure. 

Argo CD Rollbacks:

- Argo CD provides an easy way to perform rollbacks by using the "rollback" feature.
- With Argo CD, you can select a specific commit or version of your application, and it will automatically synchronize the cluster to that version, effectively rolling back your application to that state.
- This is a powerful feature that enables you to quickly recover from issues without the need for manual intervention. 

## Rollbacks practical example

When we need to do a rollback, we have 2 ways. We can use the web interface or the CLI. In this example, we will use the CLI. 

Let's introduce a change that breaks the application. We added a non existing command in the start of the application which leaded in the next status: 

```yaml
Container is waiting because of CrashLoopBackOff. It is not started and not ready.
The container last terminated with exit code 2 because of Error.
```

First of all, we need to disable the sync policy, otherwise, each time we rollback, the automatic sync will remove our rollback. In order to do it, we remove this section from the manifest related to the Argo CD application:

```yaml
  syncPolicy:
    automated:
      selfHeal: true
      prune: true
```

Once application has resync and refreshed, it will be the last time it will do it by itself. 

At this point we will use the next command in the CLI to check the history of the application:

```yaml
argocd login 10.152.183.27
argocd app history apiservice
ID  DATE                           REVISION
0   2023-11-12 19:21:11 +0100 CET  main (ad75d38)
1   2023-11-12 19:26:43 +0100 CET  main (b88eb17)
2   2023-11-12 19:30:59 +0100 CET  main (b88eb17)
3   2023-11-17 19:50:36 +0100 CET  main (3ef8b13)
4   2023-11-17 19:54:44 +0100 CET  main (c663333)
5   2023-11-17 19:55:46 +0100 CET  main (c663333)
6   2023-11-17 19:56:27 +0100 CET  main (c663333)
```
And we can cross check with the git history:


```yaml
git log --oneline
c663333 (HEAD -> main, origin/main, origin/HEAD) Disable sync
3ef8b13 Break the application
1b66480 Revert "Add a change that breaks the application"
feb7061 Revert "Disable sync"
[...]
b88eb17 Delete argocd-apps/apiservice.yaml
```

Based on previous output, we know we want to rollback to the b88eb17, which was last time that our application was working. 

```yaml
argocd app rollback apiservice 1

TIMESTAMP                  GROUP              KIND     NAMESPACE                  NAME    STATUS   HEALTH         HOOK  MESSAGE
2023-11-17T20:05:21+01:00   apps        Deployment       default    busybox-deployment    Synced  Degraded              
2023-11-17T20:05:21+01:00  bitnami.com  SealedSecret     default             appsecret    Synced  Healthy               
2023-11-17T20:05:22+01:00  bitnami.com  SealedSecret     default             appsecret    Synced  Healthy               sealedsecret.bitnami.com/appsecret unchanged
2023-11-17T20:05:22+01:00   apps        Deployment       default    busybox-deployment    Synced  Degraded              deployment.apps/busybox-deployment configured
2023-11-17T20:05:22+01:00   apps  Deployment     default    busybox-deployment  OutOfSync  Progressing              deployment.apps/busybox-deployment configured

Name:               argocd/apiservice
Project:            default
Server:             https://kubernetes.default.svc
Namespace:          default
URL:                https://10.152.183.27/applications/argocd/apiservice
Repo:               https://github.com/svianac/learning-notes.git
Target:             main
Path:               argocd-apps/argocd/apiservice
SyncWindow:         Sync Allowed
Sync Policy:        <none>
Sync Status:        OutOfSync from main (c663333)
Health Status:      Healthy

Operation:          Sync
Sync Revision:      b88eb17366ffa00171e5f2c9ea744076646118fa
Phase:              Succeeded
Start:              2023-11-17 20:05:21 +0100 CET
Finished:           2023-11-17 20:05:22 +0100 CET
Duration:           1s
Message:            successfully synced (all tasks run)

GROUP        KIND          NAMESPACE  NAME                STATUS     HEALTH   HOOK  MESSAGE
apps         Deployment    default    busybox-deployment  OutOfSync  Healthy        deployment.apps/busybox-deployment configured
bitnami.com  SealedSecret  default    appsecret           Synced     Healthy        sealedsecret.bitnami.com/appsecret unchanged
``` 

At this point, the deployment is healthy again. However, the status is OutOfSync. Before synchronizing again, we need to make sure that our git repo is fixed (we can revert commits or fix as required). Finally, sync status will change to synced once we manually trigger this operation. 

## Multi-Cluster Deployment in Argo CD

Why to have multiple cluster?

- We can use one cluster for production and other for staging. 
- Enables Blue / Green deployments.
- Enables separation of Development and QA.
- Enables Multi-region deployments.
- Cluster segmentation where each of them can have dedicated hardware. 
- Enables isolated workloads.
- Enables different environments for different teams. 
- Achieve high availability.

We can select what cluster will receive the deployment in the next section of the Argo CD application manifest:


```yaml
[...]
destination:
  server: 'https://192.168.2.36:6443'
  namespace: default
[...]
```

We can have 2 Argo CD application manifest where one will point to one cluster and the other the second cluster. For example,  onepageserver-primary.yaml and onepageserver-secondary.yaml. 

## Introducing Argo CD ApplicationSets

One of the disadvantages of the above method, is that we need to maintain 1 Argo CD application manifest for each cluster. However, we can use ApplicationSets manifest, which allow us to target applications to multiple Kubernetes.

This is an example of ApplicationSet manifest:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
  name: onepageserver
spec:
  generators:
  - list:
      elements:
      - cluster: production
        url: 'https://192.168.2.35:6443'
        revision: main
      - cluster: development
        url: 'https://192.168.2.36:6443'
        revision: dev
  template:
    metadata:
      name: '{{.cluster}}-onepageserver'
    spec:
      project: default
      source:
        repoURL: 'https://github.com/svianac/learning-notes.git'
        targetRevision: '{{revision}}'
        path: onepageserver
      destination:
        server: '{{.url}}'
        namespace: default
```

It uses Go template language, so we can use conditionals. 

## Implementing Blue-Green Deployments

What are Blue/Green deployments? 

- There are two identical environments both of with serve production. 
- Only one of them is active at any given time. For example, blue.
- The green environment is prepared for the next release: 
  - Unit tests
  - Integration tests
  - System tests
  - Performance test
  - Security tests
  - Usability tests
  - Regression tests
- Once the tests are passed, the green environment can serve production, when the blue one will be prepared for the next release. 
- To switch the environments from role can be, for example, change the proper endpoints.
- Deployments and rollbacks are easy and achieve zero downtime.

We can use previous ApplicationSet to prepare a Blue-Green deployment:

```yaml
[[...]]
      elements:
      - cluster: blue
        url: 'https://192.168.2.35:6443'
        revision: 1.0.0
      - cluster: green
        url: 'https://192.168.2.36:6443'
        revision: dev
[[...]]
```

Where blue will serve as production the release 1.0.0 and green still is used for development purposes. Simultaneously, we would need to edit our Load Balancer to only forward traffic to our blue cluster (https://192.168.2.35:6443), as this one is behaving as production. All new connections will be forwarded to the production environment, with zero downtime.

In the case we want to do deploy the next release, we can change the ApplicationSet. 

```yaml
[[...]]
      elements:
      - cluster: blue
        url: 'https://192.168.2.35:6443'
        revision: 1.0.0
      - cluster: green
        url: 'https://192.168.2.36:6443'
        revision: 1.1.0
[[...]]
```

After the green environment has passed all the testing, we can go back to our Load Balancer and switch the traffic from blue cluster to the green cluster, which contains the newer release version. 

## Implementing Canary Deployments

What are Canary deployments?

- It is a software release management strategy that involves deploying a new version of an application alongside the existing one, but only exposing a small subset of users to the new version.
- We can roll out a new feature only to the 5% of the users.
- Gradually increase the Canary percentage while monitoring and collecting user feedback. 
- In the event of failure, Canary percentage is switched back to the stable release. 
- It has the advantage of not requiring dual environments like Blue/Green. 

Name origins: Canary birds were used in coal mines to test for the existence of toxic gases. 

For a detailed explanation about how to implement Canary deployments: https://argo-rollouts.readthedocs.io/en/stable/features/canary/ 
We can algo use Ingress Nginx Canary Deployments: https://kubernetes.github.io/ingress-nginx/examples/canary/