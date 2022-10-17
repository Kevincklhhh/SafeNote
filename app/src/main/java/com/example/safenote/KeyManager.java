package com.example.safenote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class KeyManager {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "Alias";
    public static final String SHARED_PREFENCE = "SHARED_PREFENCE";
    public static final String IV = "IV";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private SecretKey getKey() throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException, KeyStoreException, UnrecoverableEntryException, CertificateException, IOException {
        KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        ks.load(null);
        final KeyGenerator keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        if(!ks.containsAlias(KEY_ALIAS)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build());
            }

        } else {
            return ((KeyStore.SecretKeyEntry) ks.getEntry(KEY_ALIAS, null)).getSecretKey();
        }
        return keyGenerator.generateKey();
    }
    private void generateRandomIV(Context ctx){
        SharedPreferences pref = ctx.getSharedPreferences(SHARED_PREFENCE, Context.MODE_PRIVATE);
        String publicIV = pref.getString(IV, null);
        if(publicIV == null){
            SecureRandom random = new SecureRandom();
            byte[] generated = random.generateSeed(12);
            String generatedIVstr = Base64.encodeToString(generated, Base64.DEFAULT);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(IV, generatedIVstr);
            edit.apply();
        }
    }
    public String encrypt(Context context, String input) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, UnsupportedEncodingException {
        generateRandomIV(context);
        Cipher c = null;
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFENCE, Context.MODE_PRIVATE);
        String iv = pref.getString(IV, null);
        c = Cipher.getInstance(AES_MODE);
            try{
                c.init(Cipher.ENCRYPT_MODE, getKey(), new GCMParameterSpec(128, Base64.decode(iv, Base64.DEFAULT)));
            } catch(Exception e){
                e.printStackTrace();

        }
        byte[] encodedBytes = c.doFinal(input.getBytes("UTF-8"));
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }
    public String decrypt(Context context, String encrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        generateRandomIV(context);
        Cipher c = null;
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFENCE, Context.MODE_PRIVATE);
        String iv = pref.getString(IV, null);
            c = Cipher.getInstance(AES_MODE);
            try{
                c.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(128,Base64.decode(iv, Base64.DEFAULT)));

            } catch(Exception e){
                e.printStackTrace();
            }
        byte[] decodedValue = Base64.decode(encrypted.getBytes("UTF-8"), Base64.DEFAULT);
        byte[] decryptedVal = c.doFinal(decodedValue);
        return new String(decryptedVal);
    }
}
