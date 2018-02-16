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
    private final ObjectMapper objectMapper;
    private final Class<M> messageClass;

    public MessageDeserializer(
        Class<M> messageClass,
        byte[] privatePgpKey,
        final String privatePgpKeyPassphrase,
        ObjectMapper objectMapper
    ) {
        this.privatePgpKey = privatePgpKey;
        this.privatePgpKeyPassphrase = privatePgpKeyPassphrase;
        this.messageClass = messageClass;
        this.objectMapper = objectMapper;
    }

    public MessageDeserializer(Class<M> messageClass, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.privatePgpKey = null;
        this.privatePgpKeyPassphrase = null;
        this.messageClass = messageClass;
    }

    public String decrypt(String encrypted) throws IOException, CryptographyException {
        if (!anyNull(privatePgpKey, privatePgpKeyPassphrase)) {
            // noinspection ConstantConditions
            try (ByteArrayInputStream keyStream = new ByteArrayInputStream(privatePgpKey)) {
                return new String(PGP.decrypt(encrypted.getBytes(), keyStream, privatePgpKeyPassphrase), "UTF-8");
            }
        }
        return encrypted;
    }

    public M deserialize(String message) throws IOException {
        return objectMapper.readValue(message, messageClass);
    }

    public String removeSnsEnvelope(String message) throws IOException {
        if (!empty(message) && message.startsWith("{")) {
            JsonNode root = objectMapper.readTree(message);
            if (root.has("Subject") && root.has("Message")) {
                return root.get("Message").asText();
            }
        }
        return message; // Message is most likely not from SNS.
    }
}
