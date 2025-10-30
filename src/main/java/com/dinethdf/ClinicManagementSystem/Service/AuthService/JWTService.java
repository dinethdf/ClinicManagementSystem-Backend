package com.dinethdf.ClinicManagementSystem.Service.AuthService;


import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientAuth;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.PatientDetailsService;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.UserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

//
    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    PatientDetailsService patientDetailsService;


    @Value("${jwt.secret}")
    private String secretkey;


    public JWTService() {

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = keyGen.generateKey();
            secretkey = Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public String generateUserToken(String userEmail) {
        Map<String, Object> claims = new HashMap<>();
        UserAuth userAuth = userDetailsService.getUserByEmail(userEmail);
        claims.put("userName", "UserIDNo");
        claims.put("role", userAuth.getRole().getName());

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(userAuth.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 604800000))
                .and()
                .signWith(getKey())
                .compact();
    }

    public String generatePatientToken(String userEmail) {
        Map<String, Object> claims = new HashMap<>();
        PatientAuth patientAuth = patientDetailsService.getUserByEmail(userEmail);
        claims.put("userName", "PatientIDNo");
        claims.put("role", patientAuth.getRole().getName());

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(patientAuth.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 604800000))
                .and()
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretkey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token) throws RuntimeException{
        // extract the username from jwt token
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class)); // custom claim
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && isTokenExpired(token));
    }

    public boolean isRouteValidateToken(String token) {
        return (isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return !extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateRefreshToken(HashMap<String, Object> extraClaims, String userEmail) {

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userEmail)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 604800000))
                .signWith(getKey())
                .compact();
    }
}
