package com.example.safenote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
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
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

public class KeyManager {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "Alias";
    public static final String SHARED_PREFENCE = "shared_preference";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private String IV;
    public KeyManager (String iv){
        IV = iv;
    }
    private SecretKey getKey() throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException, KeyStoreException, UnrecoverableEntryException, CertificateException, IOException {
        KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        ks.load(null);
        final KeyGenerator keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        if (!ks.containsAlias(KEY_ALIAS)) {
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




    public byte[] encrypt(Context context, byte[] input) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, UnsupportedEncodingException {
        Cipher c = null;
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFENCE, Context.MODE_PRIVATE);
        String iv = pref.getString(IV, null);

        c = Cipher.getInstance(AES_MODE);
        try {
            c.init(Cipher.ENCRYPT_MODE, getKey());
            iv = Base64.encodeToString(c.getIV(), Base64.DEFAULT);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(IV, iv);
            edit.apply();
        } catch (Exception e) {
            e.printStackTrace();

        }
        byte[] encodedBytes = c.doFinal(input);
        return encodedBytes;
    }

    public byte[] decrypt(Context context, byte[] encrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        Cipher c = null;
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFENCE, Context.MODE_PRIVATE);
        String iv = pref.getString(IV, null);
        c = Cipher.getInstance(AES_MODE);
        try {
            c.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(128, Base64.decode(iv, Base64.DEFAULT)));

        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] decryptedVal = c.doFinal(encrypted);
        return decryptedVal;
    }
}
