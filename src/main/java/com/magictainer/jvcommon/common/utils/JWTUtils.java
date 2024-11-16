package com.magictainer.jvcommon.common.utils;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
@UtilityClass
public class JWTUtils {

    public JwtToken decodeToken(String base64Token, String base64PrivateKey) {
        JwtToken token = new JwtToken();
        try {
            RSAPrivateKey priKey = RSAUtils.getRSAPrivateKey(base64PrivateKey);
            EncryptedJWT jwtDecrypt = EncryptedJWT.parse(new String(Base64.getDecoder().decode(base64Token)));
            RSADecrypter decrypt = new RSADecrypter(priKey);
            jwtDecrypt.decrypt(decrypt);
            if (System.currentTimeMillis() > jwtDecrypt.getJWTClaimsSet().getExpirationTime().getTime()) {
                return null;
            }
            token.setSubject(jwtDecrypt.getJWTClaimsSet().getSubject());
            token.setIssuer(jwtDecrypt.getJWTClaimsSet().getIssuer());
        } catch (Exception ex) {
            log.error("DecodeToken error: ", ex);
            return null;
        }
        return token;
    }

    public JwtToken encodeToken(String Base64PublicKey, String subject, String issuer, int sessionTokenExpirationInMinute) {
        JwtToken token = new JwtToken();
        try {
            RSAPublicKey pubKey = RSAUtils.getRSAPublicKey(Base64PublicKey);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer(issuer)
                    .expirationTime(new Date((new Date()).getTime() + ((long) sessionTokenExpirationInMinute * 60 * 1000)))
                    .notBeforeTime(new Date())
                    .issueTime(new Date())
                    .jwtID(UUID.randomUUID().toString())
                    .claim("sessionId", RequestContextHolder.currentRequestAttributes().getSessionId())
                    .build();
            JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256
                    , EncryptionMethod.A256GCM);
            EncryptedJWT jwtEncrypt = new EncryptedJWT(header, claimsSet);
            RSAEncrypter encryptors = new RSAEncrypter(pubKey);
            jwtEncrypt.encrypt(encryptors);
            String jwtToken = jwtEncrypt.serialize();
            token.setToken(Base64.getEncoder().encodeToString(jwtToken.getBytes()));
            token.setExpiration(String.valueOf(claimsSet.getExpirationTime().getTime()));
        } catch (Exception e) {
            log.error("EncodeToken error: ", e);
        }
        return token;
    }

    @Data
    public static class JwtToken {
        String token;
        String expiration;
        String subject;
        String issuer;
    }
}
