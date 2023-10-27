# Notes taken during the course - 27/10/2023

- [Notes taken during the course - 27/10/2023](#notes-taken-during-the-course---27102023)
  - [Understanding GitOps](#understanding-gitops)
  - [Introduction to Argo CD](#introduction-to-argo-cd)
  - [Why to use Argo CD?](#why-to-use-argo-cd)
  - [Install Argo CD on your cluster (Helm)](#install-argo-cd-on-your-cluster-helm)


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