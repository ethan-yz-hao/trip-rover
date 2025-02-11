package org.ethanhao.triprover.utils;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT token generation and parsing
 */
@Component
@Slf4j
public class JwtUtil {
    @Value("${JWT_SECRET_KEY}")
    private String jwtKey;

    public static String getUUID() {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        return token;
    }

    /**
     * Generate JWT token
     *
     * @param subject   Data to be stored in the token (in JSON format)
     * @param ttlMillis Expiration time of the token (in milliseconds)
     * @return
     */
    public String createJWT(String subject, Long ttlMillis) {
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, getUUID()); // Set expiration time
        return builder.compact();
    }

    private JwtBuilder getJwtBuilder(String subject, Long ttlMillis,
                                            String uuid) {
        // log JWT token
        log.info("JWT token subject: {}", jwtKey);

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        SecretKey secretKey = generalKey();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);
        return Jwts.builder()
                .setId(uuid) // unique ID
                .setSubject(subject) // subject (JSON format)
                .setIssuer("sg") // issuer
                .setIssuedAt(now) // issue time
                .signWith(signatureAlgorithm, secretKey) // use SHA256 encryption, the second parameter is the secret key
                .setExpiration(expDate);
    }

    /**
     * Generate JWT token
     *
     * @param id
     * @param subject
     * @param ttlMillis
     * @return
     */
    public String createJWT(String id, String subject, Long ttlMillis) {
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, id); // Set expiration time
        return builder.compact();
    }

    /**
     * Generate secret key
     *
     * @return SecretKey
     */
    public SecretKey generalKey() {
        byte[] encodedKey = Base64.getDecoder().decode(jwtKey);
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length,
                "AES");
        return key;
    }

    /**
     * Decrypt JWT token
     *
     * @param jwt
     * @return
     * @throws Exception
     */
    public Claims parseJWT(String jwt) throws Exception {
        SecretKey secretKey = generalKey();
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();
    }
}