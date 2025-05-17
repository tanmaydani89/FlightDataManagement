# FlightDataManagement
Flight Data Management Application

Assumptions:
1. The endpoint for CrazySupplier though it is POST we have to get the data using that
As it is the only endpoint available to us.
2. Since we cannot save the data any data from crazy supplier is not present in our data base (no supplier ID present in our DB)
3. If the duplicate flight info found with another supplier Id in our data base and response from supplier will show both assuming both are unique.
4. Since CrazySupplier is only responsible for supplying data for now will only send hardcoded data .

Implementation:
1. Earlier designed Eureka server and 2 client including CrazySupplier service and FLightDataManagement Service
however , as we only need data from Crazyserver I have mocked it using wireMock.

Components:
1. Single service with 5 endpoints
2. 4 for internal Db call and /search for crazySupplier data.
3. Tested using unit tests
4. Integration test Postman collection attached
5. Exception Handler
6. Used Swagger for validation and Jackson for mapping.
7. Wiremock folder contains mocked data from crazySupplier.
8. Not added any security features and other components like load balancers, rate limiter as currently only 1 service is integrated.


![img_2.png](img_2.png)