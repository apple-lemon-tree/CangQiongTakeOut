package com.abc.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret-key}")
    private String secretKey;//jwt中尾部签名，可以防止Token被篡改，确保安全性
    @Value("${jwt.expiration}")
    private Long expirationTime;


    // 获取JWT中第三部分，签名
    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     * @param subject 用户唯一标识
     * @param claims 设置的信息
     * @return
     */
    public String generateToken(String subject, Map<String,Object> claims){

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .signWith(getSecretKey())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    /**
     * 解析JWT令牌
     * @param jwt jwt令牌
     * @return
     */
    public Claims parseJWT(String jwt){
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException e){
            throw new RuntimeException("令牌已过期", e);
        } catch (io.jsonwebtoken.security.SecurityException e) {
            throw new RuntimeException("令牌签名无效，可能被篡改",e);
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new RuntimeException("令牌格式错误",e);
        } catch (Exception e){
            throw new RuntimeException("令牌解析失败", e);
        }
    }
}
