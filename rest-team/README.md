# rest-team project

This project uses Spring Boot, makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run".

This API allows CRUD operations on a soccer team.

## Running the application
### Approach 1 - Using Docker Compose at Development Time

With this approach you need to have docker and
docker-compose installed in your local computer. This is the command
that you have to run:

```shell script
mvn spring-boot:run
```

This command will start the containers below and the rest-team application will connect with the first two containers:
-  Postgres
-  Keycloak
-  Prometheus
-  Grafana

### Approach 2 - Using Test Containers at Development Time

With this approach you need to have only docker installed in your local computer. This is the command that you have to run:

```shell script
mvn spring-boot:test-run
```

This command will start the containers below and the rest-team application will connect with both containers:
-  Postgres
-  Keycloak

## Overall Comments

**Final Solution**

Once executing the final solution and accessing the swagger
[page](http://localhost:8081/rest-team/swagger-ui/index.html?configUrl=/rest-team/v3/api-docs/swagger-config#/), you can see the
concern in respecting Restful endpoints (as well as the Http Status
codes).

**Swagger - How to call the endpoints via swagger-ui**
- For the Soccer team application endpoints
    - You have to set the Authorization header for the applications endpoints that have the **LOCKER** signal, to generate the Authorization header you have put the username and password values inside the **"Authorize"** field in the right side of the page.

      - To generate the access token for the Authorization header for the user **teamuser** that belong to the **"team"** role from Keycloak server, you have fill the fields "username" and "password" with the value "teamuser" inside of the section "Keycloak (OAuth2, password)"

      - To generate the access token for the Authorization header for the user **test** that does not belong to the **"team"** role from Keycloak server, you have fill the fields "username" and "password" with the value "test" inside of the section "Keycloak (OAuth2, password)"


 ![Swagger](https://i.ibb.co/p0s5Kpb/swagger.png "Swagger Endpoints")


**Prometheus (available only for the Approach 1 - Using Docker Compose at Development Time)**

This is the url to access the metrics from [prometheus](http://localhost:9090/graph).

**Grafana (available only for the Approach 1 - Using Docker Compose at Development Time)**

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
mvn clean install -Psonar
```
In order to observe the potential Bugs, Code smells,
Technical Debt, etc. The results will be similar to this one:

![SonarQube](https://i.ibb.co/L1CfkDw/sonarqube.png
"Sonar Execution")

---




