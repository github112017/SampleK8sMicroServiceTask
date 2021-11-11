# Project Submission

[Original project documentation can be found here...](docs/Kubernetes_Cluster_Micro-service_Task.pdf)

## Overview

The project currently provides a user with the ability to retrieve simple messages from a REST endpoint. It consists of a Java service deployed to a Kubernetes (K8s) environment.  The service is exposed from the K8s environment. 

#### Example Payload from current public service:

```JSON
{
  "text": "The wise man is the one that makes you think that he is dumb."
}
```
Your goal is to fulfill the requirements in the original project docs. You will create a private service to go along with the existing public service. Add some security with a JWT token containing scopes, representing each service, and a few other fields.

```JSON
{
  "iat": 1516239022,
  "exp": 1517239022,
  "scope": PUBLIC_SERVICE, PRIVATE_SERVICE
}
```

The messages that are returned should tell us where they came from (public vs private) and anything else you like.  The requirements specify generic "public" and "private" services, but feel free to add a theme to this project. Examples:
- English phrases, French phrases
- He said, she said
- Domestic vehicles, foreign vehicles

```JSON
[
  {
    "language" : "English",
    "phrase" : "It is raining cats and dogs."
  },
  {
    "language" : "French",
    "phrase" : "Mieux vaut prévenir que guérir."
  }
]
```

## Next Steps

All the code for the project is available in this repository, however the image for the service is publicly available via the Docker Hub registry. 

It might be best to start with deploying and running the existing project first.

- [Step 1 - Development environment](docs/environment_overview.md)
- [Step 2 - Building the project](docs/building_the_source.md)
- [Step 3 - Deploying the project](docs/deploying_the_project.md)
- [Step 4 - Verifying the project](docs/verify_the_deployment.md)
- [Step 5 - Playing with the API](docs/play_with_the_api.md)
- [Step 6 - Work and project questions](docs/work_and_project_questions.md)
