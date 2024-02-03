# rest-team project

This project uses Spring Boot, makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run".

This API allows CRUD operations on a soccer team.

## Before running the application

Before start running the application we need to start the containers
that this application depends on, for that you need to have docker and
docker-compose installed in your local computer. This is the command
that you have to run:

```shell script
docker-compose -f ../infrastructure/docker-compose-linux.yaml up
```

This command it will start the containers below:
-  Postgres
-  Keycloak
-  Prometheus
-  Grafana

## Running the application

```shell script
..
mvn clean package
java -jar target/rest-team-1.0.jar
```

## Overall Comments

**Final Solution**

Once executing the final solution and accessing the swagger
[page](http://localhost:8081/rest-team/swagger-ui/index.html?configUrl=/rest-team/v3/api-docs/swagger-config#/), you can see the
concern in respecting Restful endpoints (as well as the Http Status
codes).

**Swagger - How to call the endpoints via swagger-ui**
- For the Soccer team application endpoints
    - You have to set the Authorization header for the applications endpoints that have the **LOCKER** signal, to set the Authorization header you have put the access token value inside the **"Authorize"** field in the right side of the page.

      To retrieve the access token for user **teamuser** that belong to the **"team"** role from Keycloak server, you have to run the command below:
        ```shell script
           curl -X POST http://localhost:8082/auth/realms/team-realm/protocol/openid-connect/token -H 'content-type: application/x-www-form-urlencoded' -d 'username=teamuser&password=teamuser&grant_type=password&client_id=team-client&client_secret=6fe5572d-d0f7-4121-8fc4-d2768bf82836'
        ```
      To retrieve the access token for user **test** that does not belong to the **"team"** role from Keycloak server, you have to run the command below:
        ```shell script
          curl -X POST http://localhost:8082/auth/realms/team-realm/protocol/openid-connect/token  -H 'content-type: application/x-www-form-urlencoded' -d 'username=test&password=test&grant_type=password&client_id=team-client&client_secret=6fe5572d-d0f7-4121-8fc4-d2768bf82836'
        ```

 ![Swagger](https://i.ibb.co/p0s5Kpb/swagger.png "Swagger Endpoints")


**Prometheus**

This is the url to access the metrics from [prometheus](http://localhost:9090/graph).

**Grafana**

This is the url to access the [Grafana DashBoard](http://localhost:3000) from Soccer Team application.

The Grafana DashBoard should looks like this:

![Metrics](https://i.ibb.co/tHjstNG/metric.png
"Metrics")

**Keycloak**

This is the url to access the [Keycloak console](http://localhost:8082/auth/) configuration.
The username and password are "admin"

**Testing**

The test strategy adopted was based on the test pyramid where in the base
we have more Unit and towards the top, we have the integrations and
UI/functional.

A caveat to be mentioned specially when it comes to the Integration
Tests: as they were pretty complex and to speed up the integration tests execution,
the test containers are created before the execution of the test methods in the Integration Test Class and they are
destroyed after the execution of them.

The coverage is pretty good and you can take a look at the Jacoco plugin
reports available in the IDE/command line.

![Jacoco](https://i.ibb.co/SJ6stfb/jacoco.png
"Jacoco Execution")

---

**Static Analyzing (SonarQube)**

Running Sonar as Docker container
```shell script
sudo sysctl -w vm.max_map_count=524288
sudo sysctl -w fs.file-max=131072
ulimit -n 131072
ulimit -u 8192
docker-compose -f ../infrastructure/sonarqube-docker-compose.yaml up
```
This is the url to access the [SonarQube](http://localhost:9000/projects?sort=-analysis_date).
The username and password are "admin"

In the first access of the [SonarQube](http://localhost:9000/projects?sort=-analysis_date), you have to change the password, so please 
change the password to "adminadmin".

In case you have a Sonar instance running locally (or a Docker
Container), you can execute the command:
```shell script
..
mvn clean install sonar:sonar
```
In order to observe the potential Bugs, Code smells,
Technical Debt, etc. The results will be similar to this one:

![SonarQube](https://i.ibb.co/L1CfkDw/sonarqube.png
"Sonar Execution")

---




