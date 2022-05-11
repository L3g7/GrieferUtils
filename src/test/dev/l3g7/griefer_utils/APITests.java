package dev.l3g7.griefer_utils;

import dev.l3g7.griefer_utils.util.IOUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.imageio.ImageIO;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class APITests {

    @Test
    @Timeout(3)
    public void testRoot() {
        assertEquals(200, IOUtil.request("https://grieferutils.l3g7.dev").getResponseCode(), "root response code");
    }

    @Test
    @Timeout(3)
    public void testIconPadded64x64() {
        assertDoesNotThrow(() -> ImageIO.read(new URL("https://grieferutils.l3g7.dev/icon/padded/64x64")), "/icon/padded/64x64");
    }

    @Test
    @Timeout(3)
    public void testIconPadded128x128() {
        assertDoesNotThrow(() -> ImageIO.read(new URL("https://grieferutils.l3g7.dev/icon/padded/128x128")), "/icon/padded/128x128");
    }

    @Test
    @Timeout(3)
    public void testAddonDescription() {
        IOUtil.request("https://grieferutils.l3g7.dev/addon_description")
                .asJsonString(v -> {})
                .orElse(Assertions::fail);
    }

}
