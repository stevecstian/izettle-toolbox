package com.izettle.jackson.paramconverter;

import static com.google.common.truth.Truth.assertThat;

import com.izettle.java.UUIDFactory;
import java.util.UUID;
import javax.ws.rs.ext.ParamConverter;
import org.junit.Test;

/**
 * Created by mattias on 2017-03-15.
 */
public class UUIDParamConverterProviderTest {

    private final ParamConverter<UUID> paramConverter =
        new UUIDParamConverterProvider().getConverter(UUID.class, null, null);
    private final UUID expectedUuid = UUIDFactory.createUUID1();
    private final String base64String = UUIDFactory.toBase64String(expectedUuid);
    private final String hexString = expectedUuid.toString();

    @Test
    public void testDeserializeHexUuid() {
        UUID actualUuid = paramConverter.fromString(hexString);
        assertThat(actualUuid).isEqualTo(expectedUuid);
    }

    @Test
    public void testDeserializeBase64Uuid() {
        UUID actualUuid = paramConverter.fromString(base64String);
        assertThat(actualUuid).isEqualTo(expectedUuid);
    }

    // Serialisation is probably not used, but the functionality is there so...
    @Test
    public void testSerializeHexUuid() {
        String actualString = paramConverter.toString(expectedUuid);
        assertThat(actualString).isEqualTo(hexString);
    }

}
