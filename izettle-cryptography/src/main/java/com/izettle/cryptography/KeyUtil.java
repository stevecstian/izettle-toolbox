package com.izettle.cryptography;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;

public final class KeyUtil {

	public static PGPPublicKey findPublicKey(final InputStream input) throws CryptographyException {
		PublicKeyReader reader;
		try {
			reader = new PublicKeyReader(input);
		} catch (IOException | PGPException e) {
			throw new CryptographyException("Failed to read public key.", e);
		}
		return findKey(reader);
	}

	@SuppressWarnings("unchecked")
	private static <T> T findKey(final KeyReader reader) {
		try {
			final Iterator<?> rings = reader.getKeyRings();
			while (rings.hasNext()) {
				final Iterator<?> keys = reader.getKeys(rings);
				while (keys.hasNext()) {
					final Object key = keys.next();
					if (reader.isValid(key)) {
						return (T) key;
					}
				}
			}
			throw new IllegalArgumentException("Can’t find encryption key using: " + reader);
		} finally {
			reader.close();
		}
	}

	private static abstract class KeyReader {

		protected final InputStream input;

		public abstract Iterator<?> getKeyRings();

		public abstract Iterator<?> getKeys(Iterator<?> keyRings);

		public abstract boolean isValid(Object key);

		public KeyReader(final InputStream input) throws IOException {
			this.input = PGPUtil.getDecoderStream(input);
		}

		public final void close() {
			try {
				this.input.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public final String toString() {
			return getClass().getSimpleName();
		}
	}

	private static final class PublicKeyReader extends KeyReader {

		private final PGPPublicKeyRingCollection keyring;

		public PublicKeyReader(InputStream input) throws IOException, PGPException {
			super(input);
			this.keyring = new PGPPublicKeyRingCollection(this.input);
		}

		@Override
		public Iterator<?> getKeyRings() {
			return this.keyring.getKeyRings();
		}

		@Override
		public Iterator<?> getKeys(Iterator<?> keyRings) {
			return ((PGPPublicKeyRing) keyRings.next()).getPublicKeys();
		}

		@Override
		public boolean isValid(Object key) {
			return ((PGPPublicKey) key).isEncryptionKey();
		}
	}
}
