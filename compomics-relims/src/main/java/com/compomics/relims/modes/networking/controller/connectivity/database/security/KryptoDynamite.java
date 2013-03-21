/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.database.security;


import java.security.MessageDigest;
import java.util.Random;

/**
 *
 * @author Kenneth
 */
public class KryptoDynamite {

    public static String kryptonize(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        //convert the byte to hex format method 2
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String getSalt() {
        StringBuilder salt = new StringBuilder();
        final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final int count = alphabet.length();

        Random r = new Random();

        for (int i = 0; i < 50; i++) {
         salt.append(alphabet.charAt(r.nextInt(count)));
        }
        return salt.toString();
    }
    
}
