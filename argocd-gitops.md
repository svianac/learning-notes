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