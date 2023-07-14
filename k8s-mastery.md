# Notes taken during the course - 26/06/2023

- [Notes taken during the course - 26/06/2023](#notes-taken-during-the-course---26062023)
  - [Multipass](#multipass)
  - [Get and describe](#get-and-describe)
  - [Explain](#explain)
  - [Definitions service, pod, namespace](#definitions-service-pod-namespace)
  - [get and namespace](#get-and-namespace)
  - [Using log command](#using-log-command)
  - [Logs with Stern](#logs-with-stern)
  - [Type of services](#type-of-services)
  - [Deploy an HTTP server with ClusterIP](#deploy-an-http-server-with-clusterip)
  - [Testing HTTP](#testing-http)
  - [Endpoint details](#endpoint-details)
  - [Deployment with Services (Assignment)](#deployment-with-services-assignment)
  - [DockerCoin miner](#dockercoin-miner)
  - [Create DockerCoin miner](#create-dockercoin-miner)
  - [Create Wordsmith (Assignment)](#create-wordsmith-assignment)
  - [Port forwarding](#port-forwarding)
  - [Kubernetes dashboard](#kubernetes-dashboard)
  - [Daemon sets](#daemon-sets)
  - [Labels and selectors](#labels-and-selectors)
    - [Adding labels to pods to change traffic in load balancer](#adding-labels-to-pods-to-change-traffic-in-load-balancer)
  - [Load balancing between nginx and apache (Assignment)](#load-balancing-between-nginx-and-apache-assignment)
  - [YAML](#yaml)
    - [YAML: The hard way](#yaml-the-hard-way)
    - [YAML: Tips and validations](#yaml-tips-and-validations)
    - [YAML: --dry-run and --dry-run=server](#yaml---dry-run-and---dry-runserver)
    - [YAML: kubectl diff](#yaml-kubectl-diff)
  - [Rolling updates](#rolling-updates)
    - [Basics](#basics)
    - [Update Walkthroughs](#update-walkthroughs)
    - [Failed Update Details](#failed-update-details)
    - [Recovering from Failed updates](#recovering-from-failed-updates)
  - [Rollout History](#rollout-history)
  - [YAML patch](#yaml-patch)
  - [Healthchecks](#healthchecks)
    - [Liveness probe](#liveness-probe)
    - [Readiness probe](#readiness-probe)
    - [Startup probe](#startup-probe)
    - [Benefits of using probes](#benefits-of-using-probes)
    - [Different types of probe handlers](#different-types-of-probe-handlers)
      - [HTTP request](#http-request)
      - [TCP connection](#tcp-connection)
      - [Arbitrary exec](#arbitrary-exec)
    - [Timing and thresholds](#timing-and-thresholds)
  - [Adding healthchecks to an app](#adding-healthchecks-to-an-app)
  - [Managing configuration for our APP](#managing-configuration-for-our-app)
  - [Create a ConfigMap for the app haproxy (file)](#create-a-configmap-for-the-app-haproxy-file)
  - [Create a ConfigMap for the app docker registry (as env)](#create-a-configmap-for-the-app-docker-registry-as-env)
  - [Ingress](#ingress)
    - [Principle of operation](#principle-of-operation)
      - [Install NGINX Ingress controller](#install-nginx-ingress-controller)
      - [DNS using nip.io](#dns-using-nipio)
      - [Creation of Ingress in k8s](#creation-of-ingress-in-k8s)


## Multipass
Get an instant Ubuntu VM with a single command. Multipass can launch and run virtual machines and configure them with cloud-init like a public cloud. Prototype your cloud launches locally for free.
https://multipass.run/install
Install microk8s in multipass mode: https://microk8s.io/docs/install-multipass

## Get and describe
Check composition of the cluster

<code>kubectl get node (no/no/nodes is equivalent)</code> 

More info:

<code>kubectl get nodes -o wide
kubectl get nodes -o yaml
kubectl get nodes -o json | jq ".items[] | {name:.metada.name} + .status.capacity"
kubectl describe node microk8s-vm</code>

## Explain
Exploring types and definitions

<code>kubectl api-resources # perform instrospection and what kind of objects the api offers
kubectl explain type // kubectl explain node
kubectl explain node.spec
kubectl explain node --recursive</code>

## Definitions service, pod, namespace
- service: It is a stable endpoint to connect to "something".
- pod: It is a group of containers running together and resources.
- namespaces: Allows us to segregate resoures and isolate them.

## get and namespace

<code>kubectl get namespaces / ns
kubectl get pods --all-namespaces
kubectl get pods --all-namespaces -o wide
kubectl get pods -n kube-system
kubectl get configmaps -n kube-public</code>

<code>kubectl delete all --all</code>

## Using log command
Deployment creates the replicaset, the replicaset creates the pod. The pod creates/run the container. 

<code>kubectl run pingping --image alpine ping 1.1.1.1
kubectl create cronjob every3mins --image alpine --schedule="*/3 * * * *" --restart=OnFailure -- sleep 10
kubectl create deployment ticktock --image="bretfisher/clock"
kubectl scale deploy/pingping --replicas 3
kubectl logs pod/pingping 
kubectl logs pod/pingping -f
kubectl logs pingping --tail 1
kubectl logs pingping --tail 1 --follow
kubectl logs -l run=pingping --tail 1 -f
kubectl logs --selector=app=ticktock --tail 1 / to list selectors k describe rs
kubectl get cronjobs</code>

## Logs with Stern
Stern: Tail multiple pods and containers from kubernetes. 
https://kubernetes.io/blog/2016/10/tail-kubernetes-with-stern/
https://github.com/stern/stern

<code>stern web -c nginx
stern auth -t --since 15m</code>

## Type of services
Services are layer 4. IP+protocol+port.
- ClusterIP: virtual IP for internal communication (nodes and pods).
- Nodeport: port that all the nodes can connect even outside the cluster. Backend services. Expose a service on a TCP port between 30000-32768.
Both of them is kube-proxy + iptables under the hood.
- Loadbalancer: External third party service for connections. Provision a cloud load balancer for our service.
- Externalname: An entry in the CNAME managed by coredDNS.
- Ingress: A special mechanism for HTTP services.
- ExternalIP: Use on node's external IP address.
https://kubernetes.io/docs/concepts/services-networking/service/

## Deploy an HTTP server with ClusterIP

<code>kubectl create deployment httpenv --image="bretfisher/httpenv"
kubectl scale deployment httpenv --replicas 10
kubectl expose deployment httpenv --port 8888 #8888 is where pod is listening internally
k get service</code>

## Testing HTTP 

<code>kubectl apply -f https://bret.run/shpod.yml
kubectl attach -n shpod -ti shpod
kubectl config set-context --current --namespace=shpod
kubectl get svc httpenv -o go-template --template '{{ .spec.clusterIP}}'
IP=($kubectl get svc httpenv -o go-template --template '{{ .spec.clusterIP}}')
curl 10.152.183.225:8888 | jq .HOSTNAME</code>

Headless service: service that does not have an IP. 

## Endpoint details

<code>kubectl get endpoints
kubectl get endpoints httpenv -o yaml
kubectl get pods -l app=httpenv -o wide</code>

## Deployment with Services (Assignment)

<code>kubectl create deployment littletomcat --image=tomcat
kubectl get pod littletomcat-845ffd77d5-tpgnp -o go-template --template '{{ .status.podIP}}' #get IP of the pod
kubectl scale deploy littletomcat --replicas 2 
kubectl exec -ti littletomcat-845ffd77d5-rnlzm -- ping 10.1.254.124
kubectl delete deploy littletomcat
kubectl expose deployment littletomcat --port=8080</code>

## DockerCoin miner
Generate a few random bytes, hash these bytes, increment a counter and repeats forever. 
5 services: 
- rng = web service generating random bytes
- hasher = web service computing hash of POSTed data
- worker = background process calling rng and hasher
- webui = web interface to watch the progress
- redis = data store (holds a counter updated by worker)

How it works: Worker will log HTTP requests to rng and hasher. Rng and hasher will log incoming HTTP requests. Webui will give us a graph on coins mined per second. 

## Create DockerCoin miner
<code>kubectl create deployment redis --image=redis
kubectl create deployment hasher --image=dockercoins/hasher:v0.1
kubectl create deployment rng --image=dockercoins/rng:v0.1
kubectl create deployment webui --image=dockercoins/webui:v0.1
kubectl create deployment worker --image=dockercoins/worker:v0.1</code>

<code>kubectl expose deployment redis --port 6379
kubectl expose deployment rng --port 80
kubectl expose deployment hasher --port 80</code>

The webui will be exposed using NodePort

<code>kubectl expose deployment webui --type=NodePort --port=80
kubectl get services
kubectl scale deployment worker --replicas=5</code>

## Create Wordsmith (Assignment)
<code>kubectl create deployment web --image=bretfisher/wordsmith-web
kubectl create deployment api --image=bretfisher/wordsmith-api
kubectl create deployment db --image=bretfisher/wordsmith-db</code>

<code>kubectl expose deployment web --type=NodePort --port 80
kubectl expose deployment api --port 8080
kubectl expose deployment db --port 5432</code>

<code>kubectl scale deployment api --replicas=5</code>

## Port forwarding
kubectl port-forward <\pod-name> <\locahost-port>:<\pod-port>
  
<code>kubectl port-forward webui-6969bf568c-5x5n8 8080:80</code>

The above command forwards to localhost 8080 from pod 80

## Kubernetes dashboard

<code>kubectl apply -f https://k8smastery.com/insecure-dashboard.yaml
kubectl port-forward dashboard-6778b765cd-44vr6 8080:80</code>

## Daemon sets

We can use it to distribute the containers evenly among nodes. We can use label and selectors to filter to what nodes applies. 
We can't create them using CLI. Only apply command. 
<code>kubectl get deploy/rng -o yaml > rng.yaml
kubectl apply -f rng.yaml #It will fail
kubectl apply -f rng.yaml --validate=false #Remove validation</code>

Daemon creates one pod per node. Master nodes usually have tains to prevent pods running there.

Mission: Make sure that there is a pod matching this spec on each node.

A daemon set is resource that creates more resources (pods).

## Labels and selectors

The rng service is load balancing requests to a set of pods. That set of pods is defined by the selector of the rng service. 

<code>kubectl describe service rng
Selector:          app=rng</code>

If any pod contains this, it will be part of the set of pods. Service is always looking for pods that follows the app=rng approach.

<code>kubectl get pods -l app=rng
kubectl get all -l app=rng
kubectl describe pod rng-5bd86c8566-m4j8s | grep Label</code>

app=rng will be in the Deployment, Replicaset and the pod. 

The multiple labels in the selector are treated as AND condition, so they all labels need to exist in the replicaset.

### Adding labels to pods to change traffic in load balancer

First we label all the pods.

<code>kubectl label pods -l app=rng enabled=yes</code>

Second we edit the service that manages the traffic. We add a new entry .Spec.selector.enabled = "yes"

<code>kubectl edit svc rng</code>

Finally, we edit one of the pods to remove the label. So the service will not forward traffic. 

<code>kubectl label pod -l app=rng,pod-template-hash enabled-</code>

The minus at the end of enabled removes the label. 

## Load balancing between nginx and apache (Assignment)
First we create both deployments

<code>kubectl create deployment nginx --image="nginx"
kubectl create deployment apache --image="httpd"</code>

Then we expose ports.

<code>kubectl expose deployment nginx --port 80
kubectl expose deployment apache --port 80</code>

Now we edit the service and add a new label (traffic will stop). 

<code>kubectl edit svc nginx #and add .Spec.selector.myapp=web and remove the original one</code>

Now we add to the deployment the correct label in the template section.
    
<code>kubectl edit deploy nginx #and add .Spec.template.labels.myapp=web</code>

We also add to the apache deployments defition. 

<code>kubectl edit deploy apache #and add .Spec.template.labels.myapp=web</code>

At this point, when we use curl, sometimes apache replies and other times nginx. 

## YAML

CLI has more limitations than YAML. We can create YAML manifests from an existing objec using:

<code>kubectl get kind name -o yaml</code>

We can also use the create command in next way to get the YAML manifest:

<code>kubectl create deployment web --image nginx -o yaml --dry-run
kubectl create namespace  awesome-app  -o yaml --dry-run</code>

We can clean up empty dicts. 

### YAML: The hard way

To be able to create a manifest offline. 
We need:

<code>apiVersion:   # find with "kubectl api-versions"
kind:               # find with "kubectl api-resources"
metadata:
spec:               #find with "kubectl describe pod"
                    #kubectl explain Pod --recursive
                    kubectl explain Pod.spec
                    kubectl explain Pod.spec --recursive</code>

### YAML: Tips and validations

YAML helps to be declarative, this approach will help to automatize everyting, as you can combine it with a SCM. Changes can be reviewed before being applied. GitOps approach. Kustomize and Helm are based on YAML approach.

Checking standard formating:

https://www.yamllint.com/
https://codebeautify.org/yaml-validator

For CI: 

https://github.com/adrienverge/yamllint
https://github.com/instrumenta/kubeval

### YAML: --dry-run and --dry-run=server

--dry-run builds the YAML. However, it does not do a "real" dry run. The kubernetes api could change the output of the --dry-run. An example:

We genrate a YAML deployment.

<code>kubectl create deployment web --image nginx -o yaml > web.yaml</code>

Change kind in the YAML to make it a DaemonSet and we see what would be applied:

<code>kubectl apply -f web.yaml --dry-run --validate=false -o yaml</code>

The output YAML is not a valid DaemonSet (replica field is present for example)

But if we use --server-dry-run, we will see what really kubernetes is going to save in its DB and the real manifest. 

<code>kubectl apply -f web.yaml --dry-run=server --validate=false -o yaml</code>

This outout has been verified much more extensively. It should be used rather than regular --dry-run. 

### YAML: kubectl diff

We can use kubectl diff to compare an already created resource with a YAML manifest that implements it. For example,

<code>kubectl apply -f just-a-pod.yaml</code>

Now we edit the image tag to :1.17 in the file just-a-pod.yaml and we use diff the spot any differences:

<code>kubectl diff -f just-a-pod.yaml</code>

We can see next output:

<code>diff -u -N /tmp/LIVE-510768210/v1.Pod.default.hello /tmp/MERGED-583742770/v1.Pod.default.hello
--- /tmp/LIVE-510768210/v1.Pod.default.hello	2023-07-06 20:51:12.474104014 +0200
+++ /tmp/MERGED-583742770/v1.Pod.default.hello	2023-07-06 20:51:12.474104014 +0200
@@ -14,7 +14,7 @@
   uid: f143466b-a0b1-4b95-8b88-8a82a0843b9a
 spec:
   containers:
\-  \- image: nginx
\+  \- image: nginx:1.17
     imagePullPolicy: Always
     name: hello
     resources: {}
</code>

It helps to be sure of the things we change. 

## Rolling updates

### Basics

With Rolling updates, a deployment is updated progressively using multiple ReplicaSets. Each RS is a group of identical pods. During the rolling update, we have at least wo RS:

- The new with the target version.
- At least one with the old set

The pace of the rollout is given by maxUnavailable and maxSurge. 

- There will be always at least replicas-maxUnavailable pods available. 
- There will never be more than replicas+maxSurge pods in total.
- There will therefore be up to maxUnavailable+maxSurge pods being updated. 

We have the possibility of rolling back if something goes wrong. 

### Update Walkthroughs

<code>kubectl apply -f https://k8smastery.com/dockercoins.yaml</code>

We check the rolling update data. This deployment has 10 instances.

<code>kubectl get deploy -o json | jq ".items[] | {name:.metadata.name} + .spec.strategy.rollingUpdate"
kubectl get deploy deployment-name -o json | jq "{name:.metadata.name} + .spec.strategy"
{
  "name": "worker",
  "maxSurge": "25%",
  "maxUnavailable": "25%"
}

</code>

Now we update the deployment worker

<code>kubectl set image deploy worker worker=dockercoins/worker:v0.2</code>

We can check the status of the rollour by:

<code>kubectl rollout status deploy worker</code>

### Failed Update Details

After rollout, we will update to a non-existing version to see how it behaves.

<code>kubectl set image deploy worker worker=dockercoins/worker:v0.3</code>

Now some pods fail to start and performance decrease. 

8 pods are running and 5 (maxUnavailable+maxSurge of 10) unhealthy. 

When starts the rollout:

- 2 replicas are taken down (as per MaxUnavailable=25%).
- 2 others are created (with new version) to replace them.
- 3 others are cerated (with the new version) per MaxSurge=25%)

At this point, rollout is stuck. 

### Recovering from Failed updates

<code>kubectl describe deploy worker</code>

We can see sections like:

<code>Replicas:               10 desired | 5 updated | 13 total | 8 available | 5 unavailable
OldReplicaSets:  worker-6bbc87d469 (0/0 replicas created), worker-b864d5ccd (8/8 replicas created)
NewReplicaSet:   worker-7b896c69f9 (5/5 replicas created) </code>

That indicates something went wrong witht the rolling update.

We can rollback the latest rolling update (we will go back to worker:v0.2). It can only used once. 

<code>kubectl rollout undo deploy worker</code>

Now we are fine:

<code>kubectl describe deploy worker
Replicas:               10 desired | 10 updated | 10 total | 10 available | 0 unavailable</code>

## Rollout History

We can see the history of rollours with the next command:

<code>kubectl rollout history deployment worker</code>

Revisions correspond to our ReplicaSets. This is stored as ReplicaSet annotations.

<code>kubectl describe replicasets -l app=worker | grep -A3 Annotations</code>

We can rollout to certain Revision:

<code>kubectl rollout undo deploy worker --to-revision=1</code>

## YAML patch

If we want to change below things all at once without using edit.

- Change image to v0.1
- Be conversative on availability (always have desired number of available workers)
- Go slow on rollout speed (update only one pod at a time)
- Give some time to our workers to "warm up" before starting more. 

We can do all that with next YAML snippet and patch command:

<code>kubectl patch demployment worker -p "
 spec:
   template:
     spec:
       containers:
       - name: worker
         image: dockercoins/worker:v0.1
    strategy:
      rollingUpdate:
        maxUnavailable: 0
        maxSurge: 1
    minReadySeconds: 10
"</code>

## Healthchecks

Healthcheks are key to providing built-in lifecycle automation. They are probes that apply to containers (not to pods), Kubernetes will take action on containers that fail healthchecks. 

Each container can have three (optional) probes:

- liveness: is this container dead or alive? (most important probe)
- readiness: is this container ready to serve traffic? (only needed if a service)
- startup: is this container still starting up? Once started, it will start the 2 above probes.

Different probes handlers are available such as HTTP, TCP, program execution. 

### Liveness probe

Indicates if the container is dead or alive. A dead container cannot come back to life. If the liveness probe fails, the container is killed. What happens next depends on the pod's restartPolicy:

- Never: the container is not restarted. 
- OnFailure or Always: the container is restarted.

When to use a liveness probe? When restarting the app is fixing the issue (deadlock or internal corruption)

### Readiness probe

Indicates if the container is ready to serve traffic. If a container becomes "unready" it might be ready again soon. If the readiness probe fails:

- The container is not killed.
- If the pod is a member of a service, it is temporarily remobed. 
- It is re-added as soon as the readiness probe passes again.

When to use readiness probe? 

- To indicate failure due to an external cause like a database is down or unreachable; mandatory auth or other backed service is unavailable. 
- To indicate temporary failure of unavailability such as application can only service N parallel connections; runtime is busy doing garbage collection or initial data load.

### Startup probe

It can be used to indicate that the container is not ready yet. For example, the main process is still starting or loading external data. 

The idea of this probe was to replace initialDelaySeconds parameter, as this one, is a rigid delay (always wait X before running probes). However, startupProbe works better when a container start time can vary a lot. 

### Benefits of using probes

- Rolling updates proceed when containers are actually ready (as opposed to merely started)
- Containers in a broken state get killed and restarted (instead of serving errors or timeouts)
- Unavailable backends get removed from load balancer rotation (thus improving response times across the board)
- If a probe in not defined, it's as if there was an "always succesful" probe. This is not acceptable in production workflows.

### Different types of probe handlers

#### HTTP request

- Specify URL of the request.
- Any statys code between 200 and 399 indicates success.

#### TCP connection

- The probe succeeds if the TCP port is open.

#### Arbitrary exec

- A command is executed in the container
- exit status of zero indicates success
- It can generate extra load in the container as requires more computational cost. 

### Timing and thresholds

- Probes are executed at intervals of periodSeconds (default: 10)
- The timeout for a probe is set with timeoutSeconds (default: 1)

periodSeconds * timeoutSeconds = It is considered as a FAIL.

- A probe is considered successful after successThreshold successes (default: 1)
- A probe is considered failing after failureThreshold failures (default: 3)
- A probe can have an initialDelaySeconds parameter (default: 0)

## Adding healthchecks to an app

First we apply liveness and define the handlers.

We will use next repo for the exercise: https://github.com/bretfisher/kubercoins

We edit rng-deployment.yaml and add:

<code>        livenessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 5</code>
Finaly, we apply changes:

<code>kubectl apply -f .</code>

Now we generate a high load to make fail the pod. We can use:

<code>kubectl attach -n shpod -ti shpod 
ab -c 10 -n 1000 http://ClusterIp/1</code>

<code>kubectl get events -w
kubectl get pods -w</code>

Result is that pods will be restarting as liveness check will fail. 

## Managing configuration for our APP

Most of the ways our code picks up configuration:

- command-line arguments
- environment variables
- configuration files
- configuration servers

Ways to pass configuration to code running in container:

- baking it into a custom image - This option may look easy but is not convenient. We will need different images for each tier.
- command-line arguments - We can use ARG arguments, but it is not convenient when we have lots of confs.
- environment variables - We can use ENV map in the container specs. 
- injecting configuration files
- exposing it over the kubernetes API
- configuration servers.

We can use Kubernetes Downward API. So pods can access data from outside the container as ENV variable. https://kubernetes.io/docs/concepts/workloads/pods/downward-api/

Environment variables can be used and works great when program expects these variables, but, should you put all tomcat configurations in env variables?

We can use ConfigMaps to store configuration files or individual configuration files and share it to a container. 

Example:

<code>  # Create a ConfigMap with a single key, "app.conf"
  kubectl create configmap my-app-config --from-file=app.conf
  \# Create a ConfigMap with a single key, "app.conf" but another file
  kubectl create configmap my-app-config --from-file=app.conf=app-prod.conf
  \# Create a ConfigMap with multiple keys (one per file in the config.d directory)
  kubectl create configmap my-app-config --from-file=config.d/ </code>

## Create a ConfigMap for the app haproxy (file)

We will use official haproxy and it expects a configuration file at /usr/local/etc/haproxy/haproxy.cfg. 
This app listens on port 80 and load balances connections between IBM and Google. 

<code>  # We get the file
curl https://k8smastery.com/haproxy.cfg -o haproxy.cfg
\#  Now we create the configMap
kubectl create configmap haproxy --from-file=haproxy.cfg
\#  We can check the configMap
kubectl get configmap/haproxy -o yaml</code>

In order to use it, we need to create a volume for the configMap and define the volumeMount in the pod. 

<code>kubectl apply -f https://k8smastery.com/haproxy.yaml</code>

Now we test it from shpod pod:

<code>kubectl exec -ti shpod -n shpod -- bash
kubectl get pods -n default -o wide
curl 10.1.254.88</code>

## Create a ConfigMap for the app docker registry (as env)

<code>kubectl create configmap registry --from-literal=http.addr=0.0.0.0:80
kubectl get configmap/registry -o yaml</code>

The yaml manifest of the pod will use the env parameter.

<code>kubectl apply -f https://k8smastery.com/registry.yaml</code>

Now we test it from shpod pod:

<code>kubectl exec -ti shpod -n shpod -- bash
kubectl get pods -n default -o wide
curl 10.1.254.85/v2/_catalog</code>

## Ingress

Service is not convenient for exposing HTTP traffic to a external network. LoadBalancer could be used but is not always possible and more complex. Custom reverse proxy also requires a lot of configuration files to be updated and maintained. A simpler option would be to use Ingress.


Ingress: It is designed to handle HTTP/S traffic to specific port from external network. 

Basic features: 

- Load balancing
- SSL termination
- name-based virtual hosting
- Can change the headers, redirections, etc.

### Principle of operation

1. Deploy an Ingress controller such us NGINX server + NGINX Ingress controler, or Traefik.
2. Sep up DNS (usually) to associeate DNS entries to the load balancer.
3. Creation of Ingress resources for our Services resources in k8s.

#### Install NGINX Ingress controller

<code>kubectl apply -f https://k8smastery.com/ic-nginx-hn.yaml</code>

This yaml creates:

- Namespace
- ConfigMaps: storing NGINX configs
- ServiceAccpunt: Authentificate to Kubernetes API
- Role/ClysterRole/RoleBinding: Authorization to API parts
- LimitRange: Limit CPU/memory of NGINX
- Service to expose NGINX on 80/443

#### DNS using nip.io

We use nip.io. 10.149.3.214 is the address of our microk8s cluster.

http://cheddar.10.149.3.214.nip.io/

nip.io will forward *.10.149.3.214.nip.io 

#### Creation of Ingress in k8s

First we need to deplot the applications. 

<code>kubectl create deployment cheddar --image=bretfisher/cheese:cheddar
kubectl create deployment stilton --image=bretfisher/cheese:stilton
kubectl create deployment wensleydale --image=bretfisher/cheese:wensleydale</code>

Then the service for each application.

<code>kubectl expose deployment cheddar --port=80
kubectl expose deployment stilton --port=80
kubectl expose deployment wensleydale --port=80</code>

Here the YAML ingress. We apply it for each deploymeny.

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: cheddar
spec:
  rules:
  - host: cheddar.10.149.3.214.nip.io
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: cheddar
            port:
              number: 80
```

Now we should see pictures in: http://cheddar.10.149.3.214.nip.io/ , http://stilton.10.149.3.214.nip.io/ and http://wensleydale.10.149.3.214.nip.io/.