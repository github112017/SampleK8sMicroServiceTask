# Design Decisions

I needed to make some significant changes to the code base to complete
this task:

### Build Changes

1. Reconfigure projects to use gradle multi-projects. This removes the
   need for gradle wrapper in each project and allows easier
   configuration of project dependencies.  Also, the original
   configuration confused IntelliJ causing various hangs.

2. All common code has been abstracted to a library, especially
   security-related classes; this ensures common behavior across all,
   and new, services.

### Code Changes

1. The project doesn't spell out how to ensure that the
   private-service can only accept requests from the
   public-service. There are multiple ways to ensure this, but I
   have taken a simple approach that reuses the existing token code. In
   particular, the private-service uses the same token authentication but
   is configured as:

   1. The private-service only supports the `PRIVATE_SERVICE` scope.
   
   2. Private-service uses a different signing key than
      public-service, this ensures that tokens intended for the
      public-service cannot be reused to access the private service.
   
   3. Public-service obtains a private token using the
      private-service's signing key allowing it to make authenticated
      requests to the private-service. This token must have a very long
      expiration time or be able to renew or create a new token then the
      old one expires.
   
   4. If the private-service's signing key is only known to it and the
      public-service then external parties cannot make authenticated
      calls directly to private-service even if private-service was
      exposed publicly.

2. The `Message` POJO class was extended to include an `origin` attribute
   denoting the origin of the message, either "public" or "private" here.
   The private-service responds with a single `Message` and the public-service
   responds with a list of `Message`s.

3. Updated `RequestErrorController` to return a consistent response when
   encountering unauthorized requests. It was returning a 401 status with
   a JSON body containing a 501 status and an internal service error
   message. The HTTP status code in the response and in the JSON body
   (and error message) are now consistent.

4. I added more logging than needed for observability into the services
   behavior, including correlation ids (for tracking a single request
   through multiple services) and authentication failure reasons. These
   would normally be downgraded to debug or trace levels (or removed 
   completely) but are handy for debugging during development and testing. 

5. Multiple levels of testing is provided (there is some overlap):

   1. Unit tests for classes providing non-trivial business
      logic.  These are intended to focus on algorithmic correctness,
      logical conditional paths, and exception handling.
   
   2. Functional tests for services proving feature-level correctness
      and overall behavior in the application setting (ie. correct use of
      frameworks). Note that external entities are mocked out. Note also,
      that functional tests give a good opportunity to verify logging
      across multiple scenarios.
   
   3. Integration tests for verifying functionality in an
      _as close to production as possible_ environment. These are used to
      prove out the service configuration and plumbing between services.

### Deployment Procedure

My environment is running MacOS 12.0.1 running:
- Docker Desktop v4.2.0
- minukube 1.24.0 with the following builtin addons:
  - dashboard
  - ingress
- kubernetes-cli 1.22.3

1. Build the docker images via `./gradlew clean build docker`, this will ensure a clean build and also run any unit and functional tests.

2. Manually push the docker images to DockerHub (can use DockerDesktop), currently using my `farrenlayton` image name and registry; these can be changed by editting the image names in
   - public-service/build.gradle
   - private-service/build.gradle
   - deployment.yaml

3. Deploy the images via `kubectl apply -f deployment.yaml`, this creates the pods and services in the usual `showbie` namespace. At this point, the pods should run but neither are accessible from outside the cluster.

4. There are multiple ways to expose the public-service publicly. All of these work when the cluster is running locally, but lets skip to the next step and create an ingress for the general case where the cluster may not be on your machine.
   1. Expose the public service via `minikube service publicservice-v1 --namespace=showbie`. This creates gives a tunnel from a random local port to your exposed service.
   2. Expose the public-service via minikube's builtin loadbalancer via `kubectl expose deployment publicservice-v1-deployment --type=LoadBalancer --port=8081 --namespace=showbie`. This also requires running `minikube tunnel` to create a tunnels for the exposed services.
   3. Create a 3rd-party ingress, see next step.
   
5. Create an ingress so public-service can be publicly accessed:
   1. Minikube includes a nginx ingress, enabled with `minikube addons ingress enable`. This only needs to be done once for the installation.
   2. Configrue the ingress via `kubectl apply -f ingress.yaml`, this will create an ingress rule allowing public-service to be accessed via http://<externalIP>/publicservice/v1/<resource>
   3. Minikube requires a tunnel to provide access via localhost. Use `minikube tunnel` then you can access http://localhost/publicservice/v1/message

5. Wait for both the public-service and private-service to startup, for some reason this takes minutes on my machine. Startup can be verifed by watching the logs via ` kubectl logs -f  --all-containers -l app=publicservice-v1 --namespace=showbie` and ` kubectl logs -f  --all-containers -l app=privateservice-v1 --namespace=showbie` and waiting for the "Started Application in XXX.XXX seconds" message.

6. Run the integration tests via `spring_profiles_active=prod request_host=localhost/publicservice/v1 ./gradlew tests:integration:integration` substituting the appropriate IPaddress and port number based on the way the service was exposed above. The spring_profiles_active variable is required for the integration tests to use the same HS256 token signing key as the deployed service.

### Testing

I've provided unit tests for verifying the business logic and exception handling of the common library classes, especially the authentication tokens. The services themselves contain classes simple enough not to need unit tests, but functional tests are included to verify functionality and correct use of the Spring framework and common library classes. Both unit and functional tests are run as part of the build target. Lastly, the integration tests to verify the plumbing and communication beween the services.

The integration tests can be run agains locally running services via `./gradlew tests:integration:integration`, by default they will run against http://localhost:8081/message using the token key defined in the integration test's local profile properties file (matching the token key used by the public-service's local profile).

When the services are deployed to minikube (or both are running locally), the integration tests can be run via `spring_profiles_active=prod request_host=127.0.0.1:8081 ./gradlew tests:integration:integration` where the request_host is the IP address/hostname and port where the public-service can be reached. The `spring_profiles_active=prod` tells the integration tests to use the prod profile configuration including the required token key used by the public-service in it's prod profile.

# Comments

1. The HS256 encryption algorithm is a symmetric algorithm meaning
   that a single secret is used to both encrypt and decrypt.  This is
   simpler for assignment purposes but I strongly recommend using an
   asymmetric algorithm using a public and private key pair allowing the
   public key to be used for token creation and the private key used for
   token verification. Authentication secrets should be protected and we
   should minimize the number of actors with access to those secrets.

2. I quite like this assignment. Despite having experience with Java
   Spring and Kubernetes, it was a good exercise to implement a custom
   security mechanism and get a local Kubernetes cluster up and
   running. As I mentioned, both of these I have been curious about but
   neither have been priorities in my current work position.

# Assignment Questions

### What ways can you have visibility into the services?

There are multiple ways to view you services, but it depends on what
information you are looking for. In particular, here are the tools I
commonly use from high-level to low-level access:

Kubernetes tools can be used to view all deployed assets and metadata
about the services:

- The CLI command `kubectl get all --namespace=showbie`.  This is not
  usually used in production.

- The kubernetes dashboard (may be provided by the cloud provider)
  provides a graphical webpage containing equivalent information
  usually with additional information (like service resource usage)
  and links to lower-level access (like logs and metrics).

The ways of determining if a service is behaving correctly are:

- By issuing common API requests to the running service using a tool
  like Postman.

- Watching the service log files as it runs and responds to
  requests.  This is the most common method I use with production
  services.

- Some cloud providers collect various runtime metrics that can be
  viewed over time (ie.  on a dashboard), this is a good way of
  verifying multiple services at a high level.

- There are tools allowing the service code itself to generate similar
  metrics.

- Some cloud providers provide alerting on those metrics or on
  availability of the service (by making requests to a known API
  point); these are often used to inform support or developers
  immediately when some condition is reached (ie. service outage or
  metrics exceed some limit).

Lastly, these services are running in containers thus you should
be able to:

- Gain SSH access directly into the container to view or edit files
  and configuration.

- Similarly, use a remote debugger to attach to the running service to
  debug problems live. This is rarely used in production, but one can
  always run the same container locally with similar configuration and
  use the remote debugger there.

### How can you ensure the services are behaving as expected?

First off, I use automated testing to verify behavior before the
services are put into production:

1. Unit tests for classes providing non-trivial business logic. These
   are intended to focus on algorithmic correctness, logical conditional
   paths, and exception handling.

2. Functional tests for services proving feature-level correctness and
   overall behavior in the application setting (ie. correct use of
   frameworks). Note that external entities are mocked out.  Note also,
   that functional tests give a good opportunity to verify logging across
   multiple scenarios.

3. Integration tests for verifying functionality in an
   _as close to production as possible_ environment. These are used to
   prove out the service configuration and plumbing between services.

These tests can/should be run both locally by the developer and during
any automated build and release cycles prior to deployment.

Secondly, during deployment I verify that Kubernetes has successfully
deployed the service and verify the service logging during it's
startup procedure.

Thirdly, post-deployment, the behaviors are confirmed using a manual
smoke-test by issuing API requests to the service(s) using a tool like
Postman.

Fourthly, the service log file is again verified by looking for
evidence of my manual smoke-test and/or other requests.

In a production cloud environment, I've also set up custom dashboards
to view pertinent metrics over time (does today's metrics look like
yesterday's?) and make use of alerting when critical metrics exhibit
unexpected values (is some abnormal behavior happening now?).

### If we require our services to scale, what modification would you make to support horizontal scaling

In general there are a few prerequisites to horizontal scaling:

- service should be small in scope

- service must be stateless

- service should minimize it's external runtime dependencies

- service must be running behind a load-balancer or ingress

For this assignment, the services are already small in scope and
stateless, so all we really require is a load-balancer or ingress to
ensure that users can access the public-service via a single URI.

However, because the public-service calls the private-service, both
services must be scaled in unison so that a public-service request is
not waiting too long for a response from the private-service. Because
not all user calls to public-service require an internal call to
private-service the services need not scale to the exact same number
of instances; since the percentage of private service calls is <=100%
and dependent on the user scopes, it may be better to configure the
services to auto-scale the number of instances based on a resource
metric (ie.  CPU, number of requests, or private-service response
times).
