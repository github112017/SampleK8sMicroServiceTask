# Deploying the Project

> ### Disclaimer
> There are many ways to run a K8s environment locally. The project instructions do not specify how you should do this. 
>
> You may continue with minikube or move to a K8s supplier of your choice.
>

## Manifests

The setup of K8s is the hard part. Deploying resources is a simple matter of applying manifests to the cluster.

At the root of the repository you will find a **deployment.yaml** file. This manifest file contains all the resources for the project.

The ingress resource has been kept separate so the resources can be applied to a cluster with no ingress controller. The ingress can be found in the **ingress.yaml** file.

Below is a quick summary of each resource.

### Namespace

All the project resources are grouped under a *showbie* namespace. This makes it easier to find and organize the resources in the cluster. It is also useful for cleaning up after we are done, as we can simply delete the namespace.

### Deployment - Public Service

This is the deployment resource for the public service.

> If you built the source code and created a docker image you will need to update the image used by the deployment
> resource. Remember to make sure the cluster can read from the docker registry where the image is located.

### Service - Public Service

This is the service resource for the public service. It exposes the deployment to cluster.

The public service is exposed as a NodePort type, which makes it accessible outside the node via a unique port.

This is intentionally done to allow us to meet the project requirements in the case where there is no ingress resource. With an ingress the public service could remain as a ClusterIP type. 

Networking is discussed in later sections.

### Ingress

The ingress resource is found in the **ingres.yaml** file. The ingress resource manages external access to our services.

For this project we simply define a single path to our public service. There is no static IP or certificate configuration.

Networking is discussed in later sections.

## Deploying

To '*apply*' the manifest file, execute the following command with the kubectl command line tool from the project root directory.

```
kubectl apply -f deployment.yaml
```

This creates all the project resources under a '*showbie*' namespace.

```
namespace/showbie created
deployment.apps/publicservice-v1-deployment created
service/publicservice-v1 created
```

The project is now deployed. Next we will look at how to verify everything worked.
&nbsp;

[Next - Step 4 (Verifying the Deployment)](verify_the_deployment.md)
