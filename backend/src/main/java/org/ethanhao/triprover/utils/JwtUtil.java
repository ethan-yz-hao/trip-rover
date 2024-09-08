package org.ethanhao.triprover.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * JWT token generation and parsing
 */
public class JwtUtil {
    // Set the expiration time of the token
    public static final Long JWT_TTL = 60 * 60 * 1000L;// 60 * 60 * 1000 milliseconds = 1 hour
    // Set the plaintext of the token
    public static final String JWT_KEY = "ethanhao";

    public static String getUUID() {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        return token;
    }

    /**
     * Generate JWT token
     *
     * @param subject Data to be stored in the token (in JSON format)
     * @return
     */
    public static String createJWT(String subject) {
        JwtBuilder builder = getJwtBuilder(subject, null, getUUID()); // Set expiration time
        return builder.compact();
    }

    /**
     * Generate JWT token
     *
     * @param subject   Data to be stored in the token (in JSON format)
     * @param ttlMillis Expiration time of the token (in milliseconds)
     * @return
     */
    public static String createJWT(String subject, Long ttlMillis) {
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, getUUID()); // Set expiration time
        return builder.compact();
    }

    private static JwtBuilder getJwtBuilder(String subject, Long ttlMillis,
                                            String uuid) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        SecretKey secretKey = generalKey();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if (ttlMillis == null) {
            ttlMillis = JwtUtil.JWT_TTL;
        }
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
    public static String createJWT(String id, String subject, Long ttlMillis) {
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, id); // Set expiration time
        return builder.compact();
    }

    public static void main(String[] args) throws Exception {
        String token = createJWT("hello");
        Claims claims = parseJWT(token);
        System.out.println(claims);
    }

    /**
     * Generate secret key
     *
     * @return
     */
    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
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
    public static Claims parseJWT(String jwt) throws Exception {
        SecretKey secretKey = generalKey();
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();
    }
}