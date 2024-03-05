## Deploy a Spring Boot Application to the Civo Kubernetes Cluster
This is a Spring Boot application that was developed to be deployed to the Civo Kubernetes cluster. The following are the steps that you use to deploy the application if you are using a Linux machine.

## Create a Kubernetes cluster

```bash
sudo civo kubernetes create spring-boot-app --size=g4s.kube.medium --nodes=3 --wait
```

## Save Kubeconfig file

```bash
sudo civo kubernetes config spring-boot-app -s -w -p ~/.kube/config
```

## Verify connection to the cluster

```bash
kubectl cluster-info
```

## Create a Spring Boot image
The *Dockerfile* for the Spring Boot app is provided at the root of the application.

```yaml
# initialize build and set base image for first stage
FROM maven:3-eclipse-temurin-17-alpine as build
# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
# set working directory
WORKDIR /home/app
# copy just pom.xml
COPY pom.xml .
# go-offline using the pom.xml
RUN mvn dependency:go-offline
# copy your other files
COPY ./src ./src
# compile the source code and package it in a jar file
RUN mvn clean install -Dmaven.test.skip=true
#Stage 2
# set base image for second stage
FROM amazoncorretto:17-alpine-jdk
# set deployment directory
WORKDIR /home/app
# copy over the built artifact from the maven image
COPY --from=build /home/app/target/spring-boot-app*.jar /home/app/spring-boot-app.jar

CMD ["java", "-jar", "/home/app/spring-boot-app.jar"]
```
To create an image of the Spring Boot app using the *Dockerfile*, use the following command.

```bash
docker build -t springbootguru/spring-boot-app:0.0.1.RELEASE .
```

## Create a container to test the image

```bash
docker run -d -p 8080:8080 springbootguru/spring-boot-app:0.0.1.RELEASE
```

## Deploy Spring Boot app image to Docker hub

```bash
docker image push springbootguru/spring-boot-app:0.0.1.RELEASE
```

## Create a deployment definition file
The *deployment.yaml* file is provided at the root of the application. Here is the configuration:

```yaml
apiVersion: v1
items:
- apiVersion: apps/v1
  kind: Deployment
  metadata:
    labels:
      app: spring-boot-app
    name: spring-boot-app-deployment
    namespace: default
  spec:
    replicas: 3
    selector:
      matchLabels:
        app: spring-boot-app-deployment
    strategy:
      rollingUpdate:
        maxSurge: 25%
        maxUnavailable: 25%
      type: RollingUpdate
    template:
      metadata:
        labels:
          app: spring-boot-app-deployment
      spec:
        containers:
        - image: springbootguru/spring-boot-app:0.0.1.RELEASE
          imagePullPolicy: IfNotPresent
          name: spring-boot-app
        restartPolicy: Always
        terminationGracePeriodSeconds: 30
kind: List
metadata:
  resourceVersion: ""
```
To create a deployment on the Civo Kubernetes cluster, use the following command.

```bash
kubectl apply -f deployment.yaml
```

## Create a service definition file
The *service.yaml* file definition file is provided at the root of the application. Here is the configuration:

```yaml
apiVersion: v1
kind: Service
metadata:
  labels:
    app: spring-boot-app
  name: spring-boot-app-service
  namespace: default
spec:
  ports:
  - nodePort: 32182
    port: 8080
    protocol: TCP
    targetPort: 8080
  selector:
    app: spring-boot-app-deployment
  sessionAffinity: None
  type: NodePort
```
To expose a deployment using the service definition file defined above, use the following command.

```bash
kubectl apply -f service.yaml
```
## Create an Ingress definition file
The *ingress.yaml* definition file is provided at the root of the application. Here is the configuration:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    kubernetes.io/ingress.class: traefik
  labels:
    app: spring-boot-app
  name: spring-boot-app-ingress
spec:
  rules:
    - host: spring-boot-app.<YOUR_CLUSTER_ID>.k8s.civo.com
      http:
        paths:
          -
            backend:
              service:
                name: spring-boot-app-service
                port:
                  number: 8080
            path: /
            pathType: "Prefix"
```
To ensure the requests applied by the service are being routed by Ingres, use the following command.

```bash
kubectl apply -f ingress.yaml
```
## Test the application
With this in place, your application is now deployed to the Civo Kubernetes cluster. To verify your application is working as expected go to `spring-boot-app.<YOUR_CLUSTER_ID>.k8s.civo.com`.

As a result, the following JSON response should be returned to the browser.

```JSON
[
  {
    "id": 1,
    "fistName": "john",
    "lastName": "doe",
    "email": "john@javawhizz.com"
  },
  {
    "id": 2,
    "fistName": "mary",
    "lastName": "public",
    "email": "mary@javawhizz.com"
  },
  {
    "id": 3,
    "fistName": "nelson",
    "lastName": "jamal",
    "email": "nelson@javawhizz.com"
  },
  {
    "id": 4,
    "fistName": "Diane",
    "lastName": "Phane",
    "email": "diane@javawhizz.com"
  },
  {
    "id": 5,
    "fistName": "steve",
    "lastName": "jobs",
    "email": "steve@javawhizz.com"
  }
]
```
