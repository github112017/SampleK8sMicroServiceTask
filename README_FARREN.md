# Design Decisions

I needed to make some significant changes to the code base to complete
this task:

1. Reconfigure projects to use gradle multi-projects. This removes the
   need for gradle wrapper in each project and allows easier
   configuration of project dependencies.  Also, the original
   configuration confused IntelliJ causing various hangs.

2. The project doesn't spell out how to ensure that the
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

3. The `Message` POJO class was renamed to `ExternalMessage` and modified
   to contain two strings, the publicText and the privateText. The
   `InternalMessage` POJO returned by the private-service is the same as
   the original `Message`.

4. Updated `RequestErrorController` to return a consistent response when
   encountering unauthorized requests. It was returning a 401 status with
   a JSON body containing a 501 status and an internal service error
   message. The HTTP status code in the response and in the JSON body
   (and error message) are now consistent.

5. All common code has been abstracted to a library, especially
   security-related classes; this ensures common behavior across all, and
   new, services.

6. Multiple levels of testing is provided (there is some overlap):

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
