# Lab Template
This is a pizza ordering system that consists of four microservices. These can be run using their individual `Application.java` files.
For the gateway the Zuul library was used. This automatically routes the requests to the correct microservices. 

The `authentication-microservice` is responsible for registering new users and authenticating current ones. After successful authentication, this microservice will provide a JWT token which can be used to bypass the security on the other microservices. This token contains the *NetID* of the user that is authenticated.

The `domain` and `application` packages contain the code for the domain layer and application layer. The code for the framework layer is the root package as *Spring* has some limitations on were certain files are located in terms of autowiring.

# Endpoints

This section contains the endpoints of the project, and a short explanation on how to call them. The explanation for what each endpoint does can be found in the JavaDoc for that endpoint. Note that 
http://localhost:8084/{name-of-microservice}/{endpoint} should reach the corresponding microservice via the gateway.
Port 8084 is used for the gateway, which directs all requests. The names of the microservices are: 'user', 'basket' and 'orders'.

The `/register` and the `/authenticate` endpoint are the only endpoints which can be accessed without a token. All other endpoints need a valid JWT token in the authorization header.

# Authentication

## AuthenticationController

POST:
- `/register`: register a user. Uses the RegistrationRequestModel
- `/authenticate`: authenticates a user. Uses the AuthenticationRequestModel
- `/registerStoreOrManager`: registers a pizza store. Uses the RegistrationSpecialRequestModel

GET:
- `/allStores`: display all the registered stores
- `/verify/{storeID}`: verifies if the store with the provided storeID exists. Specify the storeID in the path

## AllergiesController

POST:
- `/addAllergies`: adds allergies to a user profile. Uses the AllergiesRequestModel

GET:
- `/getAllergies`: gets all allergies for a user

# Order

POST:
- `/checkout`: completes an order. Uses the CheckOutRequestModel
- `/cancel`: cancels a order. Uses the CancelOrderRequestModel

GET:
- `/seeOrders`: for users returns all order they made, for  stores all orders they have to prepare.

# Basket

## RepoController(/api/repo)

POST:
- `/pizzas/addToRepo`: adds a pizza to the menu. Uses the PizzaRequestModel
- `/ingredients/add`: adds available ingredients that users may use to customize the pizzas. Uses the IngredientRequestModel
- `/coupons/addToRepo`: adds valid coupons that users may use to get discounts. Uses the CouponRequestModel

DELETE:
- `/coupons/delete`: deletes a used coupon from the store repository. In the request body send a plain text String without quotation marks. 

GET:
- `/pizzas/{filterOut}`: specify whether you want to filter out pizzas you are allergic to, according to the boolean in the request path
- `/ingredients`: returns all available ingredients from a store 
- `/ingredients/allergies`: returns all available ingredients from a store, filtering out the allergenic ingredients 
- `/coupons`: returns all saved coupons
- `/coupons/getCoupon`: returns a specific instance of a coupon. In the request body send a plain text String without quotation marks. 


## BasketController(/api/basket)

POST:
- `/addPizza`: adds a pizza to the basket. In the request body send a plain text String without quotation marks. 
- `/addPizza/custom`: adds a pizza a custom pizza, not available on the menu, specifying the required ingredients. Uses the PizzaRequestModel
- `/applyCoupon`: applies coupon to the order. In the request body send a plain text String without quotation marks. 
- `/selectTime`: updates the desired time of delivery. Uses the TimeRequestModel
- `/setStore`: set the Id of the store the user is currently ordering at.

DELETE:
- `/removePizza`: removes the pizze from the basket. In the request body send a plain text String without quotation marks.
- `/removeCoupon`: removes an applied coupon.

GET:
- `/overview`: prints an overview of the current pizzas in the basket, the current applied coupons and the current price.  
