package com.ConselhoDaComunidade.JudicialControl.util;

import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Instant;

public class TOTPUtil {

    private static final Base32 base32 = new Base32();
    private static final SecureRandom random = new SecureRandom();
    private static final int DEFAULT_INTERVAL = 30;
    private static final String HMAC_ALGO = "HmacSHA1";

    public static String generateSecret(){
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return base32.encodeToString(bytes);
    }

    public static String getOtpAuthURL(String issuer, String accountName, String secret){
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30"
            ,urlEncode(issuer), urlEncode(accountName), secret, urlEncode(issuer));

    }

    private static String urlEncode(String s){
        try { return java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20");}
        catch (Exception e){ return s;}
    }

    private static long timeWindow(long timeMillis){
        return timeMillis / 1000L / DEFAULT_INTERVAL;
    }

    public static int generateTOTP(String base32Secret){
        return generateTOTP(base32Secret, Instant.now().toEpochMilli());
    }

    public static int generateTOTP(String base32Secret, long timeMillis){
        try{
            byte[] key = base32.decode(base32Secret);
            long counter = timeWindow(timeMillis);

            byte[] data = new byte[8];
            for (int i = 7; i >=0; i--){
                data[i] = (byte) (counter & 0xFF);
                counter >>=8;
            }

            SecretKeySpec signKey = new SecretKeySpec(key, HMAC_ALGO);
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary =
                    ((hash[offset] & 0x7f) << 24) |
                    ((hash[offset + 1] & 0xff) << 16) |
                    ((hash[offset + 2] & 0xff) << 8) |
                    (hash[offset + 3] & 0xff);

            int otp = binary % 1000000;
            return otp;
        }catch (Exception e) {
            throw new RuntimeException("Erro ao gerar TOTP", e);
        }
    }

    public static boolean verifyCode(String base32Secret, int code, int window){
        long nowMillis = Instant.now().toEpochMilli();
        for (int i = -window; i <= window; ++i){
            long time = nowMillis + (i * DEFAULT_INTERVAL * 1000L);
            int candidate = generateTOTP(base32Secret, time);
            if (candidate == code) return true;
        }
        return false;
    }

}
