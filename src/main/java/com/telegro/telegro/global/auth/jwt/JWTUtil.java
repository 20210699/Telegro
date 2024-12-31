package com.telegro.telegro.global.auth.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;

@Slf4j
@Component
public class JWTUtil {
  private final SecretKey secretKey;

  public JWTUtil(@Value("${spring.jwt.secret}")String secret) {
    this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
  }

  public Long getId(String token) {

    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("id", Long.class);
  }

  public Boolean isExpired(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
  }

  public String createJwt(Long id, Long expiredMs) {
    return Jwts.builder()
        .claim("id", id)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiredMs))
        .signWith(secretKey)
        .compact();
  }

  public Claims getAppleTokenClaims(String token) throws IOException, JOSEException, ParseException {
    URL jwkSetURL = new URL("https://appleid.apple.com/auth/keys");
    JWKSet jwkSet = JWKSet.load(jwkSetURL);

    SignedJWT signedJWT = SignedJWT.parse(token);
    JWSHeader header = signedJWT.getHeader();
    RSAKey rsaKey = (RSAKey) jwkSet.getKeyByKeyId(header.getKeyID());

    if (rsaKey == null) {
      throw new RuntimeException("Unable to find key with kid: " + header.getKeyID());
    }

    RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
    return Jwts.parser()
        .verifyWith(publicKey)
        .build()
        .parseSignedClaims(token)
        .getBody();
  }
}
