package ru.axu.signer;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.GOST3411_2012_256Digest;
import org.bouncycastle.crypto.engines.GOST3412_2015Engine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.modes.G3413CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.prng.RandomGenerator;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class SdsKeyEntity {
	private RandomGenerator secureRandom;

	private UUID entityId;

	private byte[] entitySecret;

	private String positionText;
	private String personText;

	private final byte[] MAGIC_1 = "AXUSIGNER0000001".getBytes(StandardCharsets.US_ASCII);

	public SdsKeyEntity(RandomGenerator secureRandom) {
		if (secureRandom == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		byte[] uuidBytes = new byte[16];

		secureRandom.nextBytes(uuidBytes);

		entityId = MiscUtils.getUuidFromByteArray(uuidBytes);

		entitySecret = new byte[32];

		secureRandom.nextBytes(entitySecret);

		this.secureRandom = secureRandom;

		this.positionText = "";
		this.personText = "";
	}

	public UUID getEntityId() {
		return entityId;
	}

	public void setPositionText(String positionText) {
		if (positionText == null || positionText.isEmpty()) {
			throw new IllegalArgumentException("Argument value can't be null or empty!");
		}

		if (positionText.length() > 128) {
			throw new IllegalArgumentException("Argument value can't be more than 128 characters long!");
		}

		this.positionText = positionText;
	}

	public String getPositionText() {
		return positionText;
	}

	public void setPersonText(String personText) {
		if (personText == null || personText.isEmpty()) {
			throw new IllegalArgumentException("Argument value can't be null or empty!");
		}

		if (personText.length() > 128) {
			throw new IllegalArgumentException("Argument value can't be more than 128 characters long!");
		}

		this.personText = personText;
	}

	public String getPersonText() {
		return personText;
	}

	public byte[] calcMessageMac(byte[] message) {
		// Calculate message MAC using GOST Kuznyechik in GOST 34.13 MAC mode

		CMac cmac = new CMac(new GOST3412_2015Engine(), 32);

		cmac.init(new KeyParameter(entitySecret));

		cmac.update(message, 0, message.length);

		byte[] cmacBytes = new byte[4];

		cmac.doFinal(cmacBytes, 0);

		return cmacBytes;
	}

	private byte[] calcPasswordHash(String password) {
		// Hash the password using GOST Streebog 256

		GOST3411_2012_256Digest digest = new GOST3411_2012_256Digest();

		byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_16);

		digest.update(passwordBytes, 0, passwordBytes.length);

		byte[] passwordHashBytes = new byte[digest.getDigestSize()];

		digest.doFinal(passwordHashBytes, 0);

		return passwordHashBytes;
	}

	public void saveToFile(File file, String password) throws GeneralException {
		byte[] positionBytes = positionText.getBytes(StandardCharsets.UTF_16);
		byte[] personBytes = personText.getBytes(StandardCharsets.UTF_16);

		// Build clear text data structure into buffer

		ByteBuffer bb = ByteBuffer.allocate(1024);

		bb.put(MAGIC_1);
		bb.put(MiscUtils.getByteArrayFromUuid(entityId));
		bb.put(entitySecret);
		bb.putInt(positionBytes.length);
		bb.put(positionBytes);
		bb.putInt(personBytes.length);
		bb.put(personBytes);

		// Finish buffer and prepare it for reading

		bb.flip();
		byte[] clearBytes = new byte[bb.remaining()];
		bb.get(clearBytes);

		// Get salt

		byte[] saltBytes = new byte[16];
		secureRandom.nextBytes(saltBytes);

		// Hash password

		byte[] passwordHashBytes = calcPasswordHash(password);

		// Encrypt entity bytes using GOST Kuznyechik in GOST 34.13 CBC mode with GOST "Procedure 1" padding

		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new G3413CBCBlockCipher(new GOST3412_2015Engine()), new ZeroBytePadding());

		cipher.init(true, new ParametersWithIV(new KeyParameter(passwordHashBytes), saltBytes));

		byte[] encBytes = new byte[cipher.getOutputSize(clearBytes.length)];

		try {
			cipher.doFinal(encBytes, cipher.processBytes(clearBytes, 0, clearBytes.length, encBytes, 0));
		} catch (InvalidCipherTextException e) {
			throw new GeneralException("Error encrypting key entity data!", e);
		}

		// Write encrypted output to file

		try (RandomAccessFile out = new RandomAccessFile(file, "rw")) {
			out.write(saltBytes);
			out.write(encBytes);
		} catch (Exception e) {
			throw new GeneralException("Error writing key entity file!", e);
		}
	}

	public void loadFromFile(File file, String password) throws GeneralException, PasswordException {
		byte[] saltBytes;
		byte[] encBytes;

		// Read encrypted input from file

		try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
			int len = (int)in.length();

			saltBytes = new byte[16];
			in.read(saltBytes);

			encBytes = new byte[len - 16];
			in.read(encBytes);
		} catch (Exception e) {
			throw new GeneralException("Error reading key entity file!", e);
		}

		// Hash password

		byte[] passwordHashBytes = calcPasswordHash(password);

		// Decrypt entity bytes using GOST Kuznyechik in GOST 34.13 CBC mode with GOST "Procedure 1" padding

		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new G3413CBCBlockCipher(new GOST3412_2015Engine()), new ZeroBytePadding());

		cipher.init(false, new ParametersWithIV(new KeyParameter(passwordHashBytes), saltBytes));

		byte[] clearBytes = new byte[cipher.getOutputSize(encBytes.length)];

		try {
			cipher.doFinal(clearBytes, cipher.processBytes(encBytes, 0, encBytes.length, clearBytes, 0));
		} catch (InvalidCipherTextException e) {
			throw new GeneralException("Error decrypting key entity data, may be key file data was corrupted!", e);
		}

		// Read clear text data from structure buffer

		ByteBuffer bb = ByteBuffer.allocate(clearBytes.length);
		bb.put(clearBytes);
		bb.flip();

		try {
			byte[] magic = new byte[16];
			bb.get(magic);
			if (!Arrays.equals(magic, MAGIC_1)) {
				throw new PasswordException("Invalid decryption password provided, invalid key data format or key file data was corrupted!");
			}

			byte[] entityIdBytes = new byte[16];
			bb.get(entityIdBytes);
			entityId = MiscUtils.getUuidFromByteArray(entityIdBytes);

			bb.get(entitySecret);

			int positionLen = bb.getInt();
			byte[] positionBytes = new byte[positionLen];
			bb.get(positionBytes);
			positionText = new String(positionBytes, StandardCharsets.UTF_16);

			int personLen = bb.getInt();
			byte[] personBytes = new byte[personLen];
			bb.get(personBytes);
			personText = new String(personBytes, StandardCharsets.UTF_16);
		} catch (BufferUnderflowException e) {
			throw new GeneralException("Key file data was corrupted!", e);
		}
	}
}
