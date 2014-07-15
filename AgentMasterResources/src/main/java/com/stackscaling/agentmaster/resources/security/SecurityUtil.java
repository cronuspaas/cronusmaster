package com.stackscaling.agentmaster.resources.security;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

/**
 *
 * RSA - Encrypt Data using Public Key RSA - Descypt Data using Private Key
 */
public class SecurityUtil {

	/**
	 *
	 * @param encryptedDateStr
	 * @return
	 */
	public static String decryptPki(String encryptedDateStr, String crtLocation) {

		String descryptedDataStr = null;
		try {

			// String encryptedDateStr =
			// "OvDKf5DIc36HsVIT15noxXH8uviBdc7YYT/Y4whvVU9IXZXIFXaIATqQ0eDWgTQr8mGv1/nAJeUwoOzCoCzEBG1dX9lFY9s8ZoHijsNIOL5z6rd4yqTpKkb0g78WNGs9wZfrJ6/XIL/g+Yl4uN1xCo1XjJWMKt4aGPoxt9Vh8oXT2UtlgirWt/TB/YH+LL3NU3vrWTWOiXGLfXmHmdaXD9aEODJa7PeHqAUXjR4sZ+ebRdf/YHRmKGfdvRikb9AX63rxZ26XObKQNaJIbI//F4UJSLMeQyxhgLOSEdgE4ThwCteRQRg0Vt9mSTqShLuBtz2kfmnWJGENC1fRzTbOXA==";
			byte[] encryptedData = encryptedDateStr.getBytes();

			// Descypt Data using Private Key
			descryptedDataStr = SecurityUtil.decryptData(encryptedData, crtLocation);

		} catch (Throwable t) {
			t.printStackTrace();
		}

		return descryptedDataStr;

	}


	public static void main(String[] args) throws IOException {


		try {

			 String encryptedDateStr =
			 "AHe78Q/xjAzHKGRCUPRTaHtCAiJsrFoEuQToZclbgEnGOPYw2rALze/onxKJyFoS7gk3ay/fpYU2rN7qRsN4sk6UG+I7MLTQuSkrsy4pib5jpT9+xUjf8Lj2haoncRxryfBuJb0JFnG/91isA3atNBZa8uul8E3YCMTzuPuHtAFhNNk4HG/1QyvcPUn5BQ85iwTRbVFGsoiS3XDD7UgwlJ/uecVCvt6pGM2dZwb96q2XqnzEkUfsn+3+JYXC59ii2zpvsfcR62bH4Oa8sEfMbKHJSby2kP45OAG4bpIC91GbJbUotsqZdJCs2ApussF5beW3Iea75+PAiHKXSLhiuQ==";

			byte[] encryptedData = encryptedDateStr.getBytes();

			// Descypt Data using Private Key
			String token = SecurityUtil.decryptData(encryptedData, "conf/park.der");
			System.out.println(Base64.encodeBase64String(token.getBytes()));



		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static PrivateKey getPrivateKeyPkcs8(String filename)
			throws Exception {

		File f = new File(filename);
		FileInputStream fis = new FileInputStream(f);
		DataInputStream dis = new DataInputStream(fis);
		byte[] keyBytes = new byte[(int) f.length()];
		dis.readFully(keyBytes);
		dis.close();

		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

	/**
	 * Encrypt Data
	 *
	 * @param data
	 * @throws IOException
	 */
	public static String decryptData(byte[] data, String crtLocation) throws IOException {
		byte[] descryptedData = null;

		String descryptedDataStr = null;
		try {
			PrivateKey privateKey = getPrivateKeyPkcs8(crtLocation);
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			descryptedData = cipher.doFinal(data);

			descryptedDataStr = new String(descryptedData);

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return descryptedDataStr;
	}

}