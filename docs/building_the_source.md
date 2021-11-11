# Building the Project

The project currently consists of two individual Java projects.

* Public Service - Contains the code for a public message service.
* Tests/Integration - Contains the tests to validate the service.

### Public Service

To build the public service, execute the Gradle '*docker*' task from the project root:

```
./gradlew docker
```

This will build the service and then build a docker image for the service using the default image name.

```
> docker images
REPOSITORY                     TAG          IMAGE ID       CREATED          SIZE
jamieshowbie/public-service    latest       dcda6cd5e439   14 seconds ago   533MB
```
> If you want to use this image in your K8s environment you can either:
>
> 1. Tag the image to create a new target image referring to this image, then push it to your registry
> 2. Modify the build.gradle file to build a different image name, then push it to your registry
>

### Test/Integration

The integration project is used to validate the services deployed to a K8s environment. Building this project up front is not really necessary, and the use of this project is covered later.
&nbsp;

[Next - Step 3 (Deploying the Project)](deploying_the_project.md)
