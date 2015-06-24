package au.id.villar.crypt.cl;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Random;

class EncryptUtil {

	private int saltLength;

	public EncryptUtil(int saltLength) {
		this.saltLength = saltLength;
	}

	public void encrypt(InputStream input, OutputStream output, char[] password)
			throws GeneralSecurityException, IOException {
		SecretKey secret;
		byte[] salt;
		byte[] ivParam;

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

		salt = generateSalt(saltLength);
		secret = generateKey(password, salt);
		cipher.init(Cipher.ENCRYPT_MODE, secret);
		ivParam = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
		output.write(salt);
		output.write(ivParam);
		output = new CipherOutputStream(output, cipher);
		transferAndClose(input, output);
	}

	public void decrypt(InputStream input, OutputStream output, char[] password)
			throws GeneralSecurityException, IOException {
		SecretKey secret;
		byte[] salt;
		byte[] ivParam;

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

		ivParam = new byte[16];
		salt = new byte[saltLength];
		input.read(salt);
		input.read(ivParam);
		secret = generateKey(password, salt);
		cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivParam));
		input = new CipherInputStream(input, cipher);
		transferAndClose(input, output);
	}

	private void transferAndClose(InputStream input, OutputStream output) throws IOException {
		try (InputStream i = input; OutputStream o = output) {
			byte[] buffer = new byte[4096];
			for (int read; (read = i.read(buffer)) != -1;) o.write(buffer, 0, read);
		}
	}

	private byte[] generateSalt(int length) {
		byte[] salt = new byte[length];
		new Random().nextBytes(salt);
		return salt;
	}

	private SecretKey generateKey(char[] password, byte[] salt) throws GeneralSecurityException {
		KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		SecretKey tmp = factory.generateSecret(spec);
		return new SecretKeySpec(tmp.getEncoded(), "AES");
	}

}
