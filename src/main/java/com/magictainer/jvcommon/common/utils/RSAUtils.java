package com.magictainer.jvcommon.common.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@UtilityClass
public class RSAUtils {

    public KeyPair generateRSAKeyPair() {
        KeyPair keyPair = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            // Create the public and private keys
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
            SecureRandom random = new SecureRandom();
            generator.initialize(2048, random);
            keyPair = generator.generateKeyPair();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return keyPair;
    }

    public RSAPublicKey getRSAPublicKey(String base64PublicKey) {
        RSAPublicKey publicKey = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.getBytes()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error(e.getMessage(), e);
        }
        return publicKey;
    }

    public RSAPrivateKey getRSAPrivateKey(String base64PrivateKey) {
        RSAPrivateKey privateKey = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey.getBytes()));
            KeyFactory fact = KeyFactory.getInstance("RSA");
            privateKey = (RSAPrivateKey) fact.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error(e.getMessage(), e);
        }
        return privateKey;
    }

    public String encrypt(String rawData, String base64PublicKey) {
        try {
            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getRSAPublicKey(base64PublicKey));
            final byte[] bytes = rawData.getBytes(StandardCharsets.UTF_8);
            final int len = bytes.length; // string length
            int offset = 0; // offset
            int i = 0; // number of segments divided by the
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int MAX_ENCRYPT_BLOCK = 117;
            while (len > offset) {
                byte[] cache;
                if (len - offset > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(bytes, offset, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(bytes, offset, len - offset);
                }
                bos.write(cache);
                i++;
                offset = 117 * i;
            }
            bos.close();
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException |
                 BadPaddingException | IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public String decrypt(String base64EncryptedData, String base64PrivateKey) {
        if (base64EncryptedData.contains(" ")) {
            base64EncryptedData = base64EncryptedData.replaceAll(" ", "+");
        }
        byte[] data = Base64.getDecoder().decode(base64EncryptedData.getBytes());
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, getRSAPrivateKey(base64PrivateKey));
            final int len = data.length; // ciphertext
            int offset = 0; // offset
            int i = 0; // number of segments
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int MAX_ENCRYPT_BLOCK = 256;
            while (len - offset > 0) {
                byte[] cache;
                if (len - offset > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offset, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offset, len - offset);
                }
                bos.write(cache);
                i++;
                offset = MAX_ENCRYPT_BLOCK * i;
            }
            bos.close();
            return bos.toString(StandardCharsets.UTF_8);
        } catch (InvalidKeyException | NoSuchAlgorithmException
                 | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
