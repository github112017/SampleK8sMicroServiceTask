# Play with the API

Let's make a call to the public API and get a message...

## Make a Request

We can hit the url in a browser or build a curl command:

```
curl http://127.0.0.1:54559/message | json_pp
```

Executing this command from my environment generates the following response:

```
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100    78    0    78    0     0   1691      0 --:--:-- --:--:-- --:--:--  8666
{
   "text" : "The early bird gets the worm, but the second mouse gets the cheese."
}
```

## View the Logs

To get the name of the pod for this service, execute the following command:

```
kubectl get pods --namespace=showbie
```

```
NAME                                           READY   STATUS    RESTARTS   AGE
publicservice-v1-deployment-6cb4c77bc4-bhcpk   1/1     Running   0          163m
```

> The pod names will have a different suffix for your deployment. Remember to account for that in any kubectl commands using the pod name.

To get the logs for our service.

```
kubectl logs publicservice-v1-deployment-6cb4c77bc4-bhcpk --namespace=showbie
```

```
...

2021-11-10 20:19:21.531  INFO 1 --- [nio-8081-exec-7] c.s.p.controllers.MessageController      : Starting message request
2021-11-10 20:19:21.532  INFO 1 --- [nio-8081-exec-7] c.s.p.controllers.MessageController      : Completed message request
...
```

I've omitted a large portion of the logs for readability, but above you can see the trivial output for the processing of our request.

## Test a Failure

Finally let hit an unkonwn endpoint.

```
curl http://127.0.0.1:54559/unknown | json_pp
```

```
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   143    0   143    0     0   5198      0 --:--:-- --:--:-- --:--:--  8411
{
   "error" : "Not Found",
   "message" : "Oops, we can't seem to find what you are looking for",
   "status" : 404,
   "timestamp" : "2021-11-10T20:36:09.327+00:00"
}
```

&nbsp;

[Next - Step 6 (Work and Review Project Questions)](work_and_project_questions.md)