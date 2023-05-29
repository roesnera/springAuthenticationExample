package com.example.authenticationpractice2.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.CachingUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// could use @Service, or @Repository as well
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            // request
            @NonNull HttpServletRequest request,
            // response
            @NonNull HttpServletResponse response,
            // chain of responsibility, contains list of other filters we need to execute
            // when we call filterChain.doInternalFilter(), or filterChain.doFilter()
            // the next filter is called
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // this string represents the value of the authorization header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        if(authHeader==null || !authHeader.startsWith("Bearer ")){
            // if there is no auth header, or it doesn't start with "Bearer " (i.e. there isn't a properly formatted JWT in the header)
            // we want to pass this request along to the next filter in the chain
            filterChain.doFilter(request,response);
            return;
        }
        // extracts JWT from authentication header
        jwt = authHeader.substring(7);
        // we will extract the user's email (which is denoted by the name "username" per the UserDetails interface from Spring Security) from the JWT
        // in order to do this, we will implement a class to provide jwt processing
        userEmail = jwtService.extractUsername(jwt);

        // if the user email is not null, and the user has not been authenticated before
        if(userEmail!=null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // uses our jwtService to validate token given user details
            if (jwtService.isTokenValid(jwt, userDetails)){
                // this token is needed to update our security context
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        // which user is this
                        userDetails,
                        // what are their credentials
                        // our user class doesn't have any credentials, so this is null for us
                        null,
                        // what authorities do they have
                        userDetails.getAuthorities()
                        );
                // adds details to our context auth token based on the client's request
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // this updates our security context using the token we've generated here
                // note that this token is different from the JWT we will send back to our client
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request,response);
    }
}
