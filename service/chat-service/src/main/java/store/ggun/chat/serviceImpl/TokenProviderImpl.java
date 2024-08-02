package store.ggun.chat.serviceImpl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import store.ggun.chat.exception.JwtAuthenticationException;
import store.ggun.chat.domain.UserModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import store.ggun.chat.service.TokenProvider;


@Service
class TokenProviderImpl implements TokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.access}")
    private long accessTokenExpiration;
    @Value("${jwt.expiration.refresh}")
    private long refreshTokenExpiration;

    String extractUsername(String jwt){
        return extractClaim(jwt, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    List<String> extractRoles(String jwt){
        return extractClaim(jwt, claims -> (List<String>) claims.get("roles"));
    }

    @Override
    public boolean isTokenValid(String jwt){
        return !isTokenExpired(jwt);
    }

    private boolean isTokenExpired(String jwt){
        return extractClaim(jwt, Claims::getExpiration).before(new Date());
    }

    public String generateToken(Map<String, Object> extraClaims, UserModel userModelDetails, String isRefreshToken) {

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userModelDetails.getEmail())
                .claim("roles", List.of("user"))
                .expiration(Date.from(Instant.now().plusSeconds(isRefreshToken.equals("accessToken") ? refreshTokenExpiration : accessTokenExpiration)))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();



    }

    private <T> T extractClaim(String jwt, Function<Claims, T> claimResolver){
        Claims claims = extractAllClaims(jwt);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String jwt){
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (JwtException e) {
            throw new JwtAuthenticationException(e.getMessage());
        }
    }

    private SecretKey getSigningKey() {
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }


}