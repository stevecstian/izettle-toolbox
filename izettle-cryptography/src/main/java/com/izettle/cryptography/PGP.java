package com.izettle.cryptography;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.util.io.Streams;

/**
 * This code is a result of reworking and cleaning up example and test code from the Bouncy Castle projects source.
 */
public class PGP {

	public static BouncyCastleProvider provider = new BouncyCastleProvider();

	static {
		Security.addProvider(provider);
	}

	public static byte[] decrypt(
			final byte[] data,
			final InputStream privateKey,
			final String passphrase)
			throws CryptographyException {
		final ByteArrayOutputStream out;
		try {
			final PGPLiteralData message = asLiteral(data, privateKey, passphrase);
			out = new ByteArrayOutputStream();
			Streams.pipeAll(message.getInputStream(), out);
		} catch (IOException | PGPException e) {
			throw new CryptographyException("Failed to decrypt.", e);
		}
		return out.toByteArray();
	}

	public static byte[] encrypt(
			final byte[] secret,
			final PGPPublicKey... keys)
			throws CryptographyException {
		final ByteArrayOutputStream out;
		try {
			final ByteArrayInputStream in = new ByteArrayInputStream(secret);
			final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			final PGPLiteralDataGenerator literal = new PGPLiteralDataGenerator();
			final PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(CompressionAlgorithmTags.UNCOMPRESSED);
			final OutputStream pOut = literal.open(
					comData.open(bOut),
					PGPLiteralData.BINARY,
					"filename",
					in.available(),
					new Date());
			Streams.pipeAll(in, pOut);
			comData.close();
			final byte[] bytes = bOut.toByteArray();
			final PGPEncryptedDataGenerator generator = new PGPEncryptedDataGenerator(
					new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
							.setWithIntegrityPacket(true)
							.setSecureRandom(new SecureRandom())
							.setProvider(provider));
			for (final PGPPublicKey key : keys) {
				generator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(key).setProvider(provider));
			}
			out = new ByteArrayOutputStream();
			final ArmoredOutputStream armor = new ArmoredOutputStream(out);
			final OutputStream cOut = generator.open(armor, bytes.length);
			cOut.write(bytes);
			cOut.close();
			armor.close();
		} catch (IOException | PGPException e) {
			throw new CryptographyException("Failed to encrypt.", e);
		}
		return out.toByteArray();
	}

	@SuppressWarnings("unchecked")
	private static Iterator<PGPPublicKeyEncryptedData> getEncryptedObjects(final byte[] data) throws IOException {
		final PGPObjectFactory factory = new PGPObjectFactory(PGPUtil.getDecoderStream(new ByteArrayInputStream(data)));
		final Object first = factory.nextObject();
		final Object list = (first instanceof PGPEncryptedDataList) ? first : factory.nextObject();
		return ((PGPEncryptedDataList) list).getEncryptedDataObjects();
	}

	private static PGPLiteralData asLiteral(
			final byte[] data,
			final InputStream keyfile,
			final String passphrase) throws IOException, PGPException {
		PGPPrivateKey key = null;
		PGPPublicKeyEncryptedData encrypted = null;
		final PGPSecretKeyRingCollection keys = new PGPSecretKeyRingCollection(new ArmoredInputStream(keyfile));
		for (final Iterator<PGPPublicKeyEncryptedData> i = getEncryptedObjects(data); key == null && i.hasNext();) {
			encrypted = i.next();
			key = findSecretKey(keys, encrypted.getKeyID(), passphrase);
		}
		if (key == null) {
			throw new IllegalArgumentException("secret key for message not found.");
		}
		final InputStream stream = encrypted.getDataStream(
				new JcePublicKeyDataDecryptorFactoryBuilder()
						.setProvider(provider)
						.build(key));
		return asLiteral(stream);
	}

	private static PGPLiteralData asLiteral(final InputStream clear) throws IOException, PGPException {
		final PGPObjectFactory plainFact = new PGPObjectFactory(clear);
		final Object message = plainFact.nextObject();
		if (message instanceof PGPCompressedData) {
			final PGPCompressedData cData = (PGPCompressedData) message;
			final PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream());
			// Find the first PGPLiteralData object
			Object object = null;
			for (int safety = 0; safety++ < 1000 && !(object instanceof PGPLiteralData); object = pgpFact.nextObject()) {
            }
			return (PGPLiteralData) object;
		} else if (message instanceof PGPLiteralData) {
			return (PGPLiteralData) message;
		} else if (message instanceof PGPOnePassSignatureList) {
			throw new PGPException("encrypted message contains a signed message - not literal data.");
		} else {
			throw new PGPException("message is not a simple encrypted file - type unknown: "
					+ message.getClass().getName());
		}
	}

	private static PGPPrivateKey findSecretKey(
			final PGPSecretKeyRingCollection keys,
			final long id,
			final String passphrase) {
		try {
			final PGPSecretKey key = keys.getSecretKey(id);
			if (key != null) {
				return key.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder()
						.setProvider(provider)
						.build(passphrase.toCharArray()));
			}
		} catch (Exception e) {
			final String passphraseMessage = (passphrase == null) ? "null" : passphrase.length() + " character";
			throw new RuntimeException("Unable to extract key " + id + " using " + passphraseMessage + " passphrase", e);
		}
		return null;
	}
}