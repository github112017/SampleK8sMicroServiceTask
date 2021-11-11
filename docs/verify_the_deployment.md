# Verifying the Deployment

## Manual Verification

You should now be able to retrieve the '*showbie*' resources.

```
kubectl get all --namespace=showbie
```

```
NAME                                               READY   STATUS    RESTARTS   AGE
pod/publicservice-v1-deployment-6cb4c77bc4-bhcpk   1/1     Running   0          41m

NAME                       TYPE       CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
service/publicservice-v1   NodePort   10.103.68.49   <none>        8081:32466/TCP   41m

NAME                                          READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/publicservice-v1-deployment   1/1     1            1           41m

NAME                                                     DESIRED   CURRENT   READY   AGE
replicaset.apps/publicservice-v1-deployment-6cb4c77bc4   1         1         1       41m
```

The output shows the '*showbie*' resources and their statuses. We can use this information to verify that the deployments and their respective services were created, as well as how many pods are running.

Verify that we have a deployment for the service and that it has a running pod. Also verify that the service is of type NodePort.

>### K8s Networking
>
> ---
>
> Full disclosure, I am very new to K8s. For this project I have used minikube and the '*service*'
> command to launch the cluster. The ingress.yaml was not required in this situation.
> 

```
minikube service publicservice-v1 --namespace=showbie
```

```
|-----------|------------------|-------------|---------------------------|
| NAMESPACE |       NAME       | TARGET PORT |            URL            |
|-----------|------------------|-------------|---------------------------|
| showbie   | publicservice-v1 |        8081 | http://192.168.49.2:32466 |
|-----------|------------------|-------------|---------------------------|
🏃  Starting tunnel for service publicservice-v1.
|-----------|------------------|-------------|------------------------|
| NAMESPACE |       NAME       | TARGET PORT |          URL           |
|-----------|------------------|-------------|------------------------|
| showbie   | publicservice-v1 |             | http://127.0.0.1:54559 |
|-----------|------------------|-------------|------------------------|
🎉  Opening service showbie/publicservice-v1 in default browser...
❗  Because you are using a Docker driver on darwin, the terminal needs to be open to run it.
```

So from the host environment I simply need to access the public service via the URL and path.

For example:

```
http://127.0.0.1:54559/message
```

Now that we know the host endpoint we can execute the integration tests.

The integration test project is a simple Java/Spring project that can be run from the command line.

To execute the tests, run the '*integration*' Gradle task from the integration project root. The public service host can be passed in as a parameter.

For example:

```
./gradlew clean integration -P"host"="127.0.0.1:54559"
```

If the tests passed then we can say that we successfully validated the deployed project.
&nbsp;

[Next - Step 5 (Play with the API)](play_with_the_api.md)