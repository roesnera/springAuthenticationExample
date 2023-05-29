# General Explanation of this project

## Credits

This project was developed largely following <a href="https://www.youtube.com/watch?v=KxqlJblhzfI&t=292s&ab_channel=Amigoscode" target="_blank">a walkthrough video from Amigoscode</a>.

Some information in that video is outdated by recent changes to Spring Security (version 6.1.0), which I have updated to the extent that I could.

## Guide

I have made notes on most of the classes involved in the authentication and security of this app.

Note: I used Lombok which leverages annotations to fill in a lot of boilerplate code

The general pipeline I followed was as outlined below:


Configure database connection (I used MySQL)
User entity class (including role enum and UserRepository) --> <br/>
JwtAuthenticationFilter class -->  <br/>
JwtService class --> <br/>
SecurityConfiguration class --> <br/>
ApplicationConfig class --> <br/>
AuthenticationController class --> <br/>
AuthenticationRequest, AuthenticationResponse, RegisterRequest classes (planning to update these to records) --> <br/>
AuthenticationService class --> <br/>
DemoController class

I will break down each class and their important features here

### `User.java`

The User class represents users of our site. It will need to store their emails and passwords.
In addition to that required information, I have included first and last name field. These are completely optional.
This class implements a UserDetails interface in order to take advantage of a fair amount of SpringSecurity functionality. I have noted requirements that are not obvious in comments in the User class.

<strong>Note: many databases use a table called "users" by default, and so you should explicitly rename your table as to avoid name clashing. Most sql-type languages have a convention for this, such as prepending the name with an "_", as is used in this project.</strong>

### `JwtAuthenticationFilter.java`

This class represents our JWT filter. Spring Security uses a filter chain to implement authentication, and to this chain we will add this filter.
We extend from the OncePerRequestFilter class from Spring Web to indicate that this is both a filter and one that should trigger once on each request to our site.
(aside: we are able to avoid implementing this filter for absolutely every request by specifications we will make in our SecurityFilterChain bean later on)
Overriding the doFilterInternal method, we follow the following general approach:

    get the header called "Authorization"

    if there is no such header, or if it is not formatted correctly for our JWT
        pass this along to the next filter in our filter chain

    extract the JWT from the header

    determine to which user the token belongs using a method from our `jwtService` class (more on that later)
    
    if that user exists (meaning the token is valid for someone) and this user is not already authenticated
        grab the user from our repository that is specified by the token
        
        if the user specified by the token is the same as the retrieved user
            authenticate this user in our SecurityContextHolder

    move to the next filter in our chain

### `JwtService.java`

This class provides some important methods for working with our Json Web Tokens. Things like extracting claims (notes on what this means in the class itself), generating new tokens, validating tokens, and so on are here.


### `SecurityConfiguration.java`

Here we create our specific SecurityFilterChain. This is what Spring Security uses to secure our application, and in order to take advantage of our jwt filter, we need to tell Spring where and when to use it.
We create a bean that returns a SecurityFilterChain and accepts an HttpSecurity object to be injected by Spring.
The order of events inside the method is as follows:

    disable cross-site resource forgery filtering (we are not worried about this for now, although it is an important consideration)
    
    declare which endpoints require authentication

    provide and authentication provider

    insert our JwtAuthFilter before the UsernamePasswordAuthenticationFilter that is part of Spring Security

<strong>Note: I have left the deprecated strategy from the referenced video in the method but commented out. It still works but we should always use the most up-to-date structure we can!</strong>

### `ApplicationConfig.java`

Here we defined some of the beans that our SecurityConfiguration and JwtAuthenticationFilter classes need.

This is one of the times when implementing the UserDetails interface pays off! Check our UserDetailsService bean and the accompanying comments for a look at why.

### `AuthenticationController.java`

Just like with any controller, here we are defining endpoints to listen for http requests.
What's special about this one is that it listens for requests that are specifically related to authentication, including registration and validation.

### `AuthenticationRequest.java, AuthenticationResponse.java, and RegisterRequest.java`

These are more or less record-types, DTOs for authentication-related requests.

### `AuthenticationService.java`

This is our last security related class. Woohoo 🙌!

Here we provide the business logic for our authentication endpoints.

#### `register(RegisterRequest request)`

    we create a user based on the request data and save it to our user repository
    
    then we generate a token for this user and send it back

#### `authenticate(AuthenticationRequest request)`

    we use our authentication manager to authenticate the user details specified in the request body
        this method will throw an exception if it fails

    then we grab our user from the repository
        note: we have an orElseThrow() statement here but we can be certain that this user exists if the previous statement executes

    then we generate the token and send it back

### `DemoController.java`

This controller only exists to demonstrate that it is secured. Nothing fancier than that here.