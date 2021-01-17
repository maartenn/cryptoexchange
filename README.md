# CryptoExchange
I created this project to learn about event sourcing with Spring Boot and Axon in Java. It's a partial implementation of a Crypto Exchange. The application is exposed with a simple rest api.   

The application is basically divided in 3 parts 

public 
 - get highest buy / lowest sell 
 - get orderbooks
 
user 
 - create buy/sell order
 
admin
 - create/read orderbook per cryptocurrencymarket

# Running this project
Please make sure to first run the runtime dependencies, after you've done so please continue.
You can easily run the project by opening your console by running this command
`mvnw spring-boot:run`

OR

Open the project in your IDE and run `CryptoExchangeApplication`. 

## Runtime dependencies 

### Keycloak
Project uses Keycloak for authentication/authorization. Keycloak can be started (from keycloak folder) with 
`docker-compose.exe -f .\keycloak-docker-postgres.yml up`
You can navigate to localhost:8080 (credentials can be found in yaml) then import my export to have a basic configuration. After this you need to add a user with role user and a user with role admin.

### Axon event store

Default axon event store is used, use the jar or docker found on https://axoniq.io/download#0 to start this.

# Very basic manual how you can use the application: 

1. login as admin and get a token from keycloak and use this in your requests (
2. create an orderbook. example; `POST http://localhost:8081/admin/createOrderbook?pair=BTC_EUR`
3. login as user and add buy / sell order  :
`POST http://localhost:8081/buyOrder?amountOfEur=2&price=48000&pair=BTC_EUR`
`POST http://localhost:8081/sellOrder?amountOfCrypto=.1&pair=BTC_EUR`
if no price is given then the order will be executed on the market level.
4. view open orders; `GET http://localhost:8081/all-orders` or  `GET http://localhost:8081/all-orders/getPriceInfo`

# TODO 

error handling
usage of different coins in view 
ordering in views
cancelling orders
add orders to account
calculate crypto for price/amount and vice versa
connect db to events for snapshot storage
integration tests
split application in read / CUD projects
keep better track of balance/liquidity

and much more...
