package com.izettle.messaging.serialization;

import static com.izettle.java.ValueChecks.anyNull;
import static com.izettle.java.ValueChecks.empty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izettle.cryptography.CryptographyException;
import com.izettle.cryptography.PGP;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MessageDeserializer<M> {

    private final byte[] privatePgpKey;
    private final String privatePgpKeyPassphrase;
    private static final ObjectMapper JSON_MAPPER = JsonSerializer.getInstance();
    private final Class<M> messageClass;

    public MessageDeserializer(Class<M> messageClass, byte[] privatePgpKey, final String privatePgpKeyPassphrase) {
        this.privatePgpKey = privatePgpKey;
        this.privatePgpKeyPassphrase = privatePgpKeyPassphrase;
        this.messageClass = messageClass;
    }

    public MessageDeserializer(Class<M> messageClass) {
        this.privatePgpKey = null;
        this.privatePgpKeyPassphrase = null;
        this.messageClass = messageClass;
    }

    public String decrypt(String encrypted) throws IOException, CryptographyException {
        if (!anyNull(privatePgpKey, privatePgpKeyPassphrase)) {
            final ByteArrayInputStream keyStream = new ByteArrayInputStream(privatePgpKey);
            return new String(PGP.decrypt(encrypted.getBytes(), keyStream, privatePgpKeyPassphrase), "UTF-8");
        }
        return encrypted;
    }

    public M deserialize(String message) throws IOException {
        return JSON_MAPPER.readValue(message, messageClass);
    }

    public static String removeSnsEnvelope(String message) throws IOException {
        if (!empty(message) && message.startsWith("{")) {
            JsonNode root = JSON_MAPPER.readTree(message);
            if (root.has("TopicArn") && root.has("Message")) {
                return root.get("Message").asText();
            }
        }
        return message; // Message is most likely not from SNS.
    }
}
