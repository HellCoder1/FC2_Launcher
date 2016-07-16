/*
 * Copyright © 2014.
 * This file is part of Friendscraft2 Launcher.
 * Friendscraft2 Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fc2.util;

import net.fc2.log.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;

public class CryptoUtils {

    @Deprecated
    public static String decryptLegacy(String str, byte[] key) {
        BigInteger in = new BigInteger(str, 16).xor(new BigInteger(1, key));
        try {
            return new String(in.toByteArray(), "utf8");
        } catch (UnsupportedEncodingException e) {
            return "";
        } catch (NumberFormatException e) {
            Logger.logError("Error occurred during legacy decryption");
            return "";
        }
    }

    @Deprecated
    public static String encryptLegacy(String str, byte[] key) {
        BigInteger str2;
        try {
            str2 = new BigInteger(str.getBytes("utf8")).xor(new BigInteger(1, key));
        } catch (UnsupportedEncodingException e) {
            return "";
        }
        return String.format("%040x", str2);
    }

    public static String decrypt(String str, byte[] key) {
        try {
            Cipher aes = Cipher.getInstance("AES");
            aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(pad(key), "AES"));
            String s = new String(aes.doFinal(Base64.decodeBase64(str)), "utf8");
            if (s.startsWith("FDT:") && s.length() > 4)
                return s.split(":", 2)[1];//we don't want the decryption test
            else
                return decryptLegacy(str, key);
        } catch (Exception e) {
            Logger.logError("Error Decrypting information, attempting legacy decryption", e);
            return decryptLegacy(str, key);
        }
    }

    public static String encrypt(String str, byte[] key) {
        try {
            Cipher aes = Cipher.getInstance("AES");
            aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(pad(key), "AES"));
            return Base64.encodeBase64String(aes.doFinal(("FDT:" + str).getBytes("utf8")));
        } catch (Exception e) {
            Logger.logError("Error Encrypting information, reverting to legacy format", e);
            return encryptLegacy(str, key);
        }
    }

    public static byte[] pad(byte[] key) {
        try {
            return Arrays.copyOf(DigestUtils.sha1Hex(key).getBytes("utf8"), 16);
        } catch (UnsupportedEncodingException e) {
            Logger.logError("error encoding padded key!", e);
            return Arrays.copyOf(DigestUtils.sha1Hex(key).getBytes(), 16);
        }
    }
}
