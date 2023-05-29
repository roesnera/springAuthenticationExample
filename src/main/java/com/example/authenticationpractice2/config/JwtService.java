package com.example.authenticationpractice2.config;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // this is generated using the following site https://allkeysgenerator.com/random/security-encryption-key-generator.aspx
    private static final String SECRET_KEY = "7638792F423F4528482B4D6251655368566D597133743677397A24432646294A";

    // throughout this program, you will see the term Claims
    // claims are part of a JWT that make assertions about the requesting party
    // common claims are name, authorities, and subject
    // there are three types of claims
    // registered - not mandatory but recommended
        // e.g. issuer, subject, expiration
    // private
    // public
        // claims defined in jwt registry

    public String extractUsername(String token){

        // we leverage the extractClaim method to only extract the subject claim from the jwt
        // the subject claim will be the username, which in our case is their email
        return extractClaim(token, Claims::getSubject);
    }


    // this function allows you to extract a single claim from all the claims in the JWT
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(
            // will contain extra claims that we want added to our token
            // allows us to pass authorities or any extra claim we want to be present in our token
            Map<String, Object> extraClaims,
            //
            UserDetails userDetails
    ) {
        // this will build our token
        return Jwts.builder()
                // this sets any extra claims we might want to add
                .setClaims(extraClaims)
                // this sets the username as the subject in the token
                .setSubject(userDetails.getUsername())
                // this sets an IAT in the token
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // this, combined with the IAT, creates a window of usability on the token
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                // this signs the token with our key using the 256 bit algorithm
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                // this generates the token
                .compact();
    }

    // this method does the same as above but with no custom claims
    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>() {
        }, userDetails);
    }

    // this method checks if a token is valid
    // we need both the token itself and the user details because each token is unique to a user
    // we already have an extractUsername method, so we can just compare the extracted username with the supplied details
    // then we just make sure that the token is not expired
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // checks if a token is expired by invoking our claims extraction method
    // specifically looking for the expiration claim
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // the Claims package already has functionality to extract the expiration
    // we just leverage that here with our specific token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    // method to extract claims from jwt

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // creates a bytes array from our generated, 256-bit secret key
    // converts that array into a Key object using an algorithm from Keys and returns it
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
