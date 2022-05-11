package dev.l3g7.griefer_utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.util.IOUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class GrieferWertTests {

    @Test
    @Timeout(5)
    public void testPrices() { // Check for data I currently do not process
        IOUtil.request("http://server1.mysterymod.net:5600/api/v1/itemwert/griefergames").asJsonArray(array -> {
            int index = -1;
            for (JsonElement element : array) {
                String id = " (#" + ++index + ")";
                JsonObject object = element.getAsJsonObject();
                JsonObject mapping = object.get("mapping").getAsJsonObject();
                assertFalse(mapping.get("id").getAsString().isEmpty(), "id empty" + id);
                assertTrue(mapping.get("nbt").isJsonNull(), "nbt not null" + id);
                assertFalse(mapping.get("wert").getAsString().isEmpty(), "wert null" + id);
                assertDoesNotThrow(() -> mapping.get("damage").getAsInt(), "damage not int" + id);
                assertTrue(mapping.get("lore").isJsonNull(), "lore not null" + id);
                assertTrue(mapping.get("enchantments").isJsonNull(), "enchantments not null" + id);
                assertEquals(6, mapping.entrySet().size(), "mapping size not 6" + id);

                JsonObject wertItem = object.get("wertItem").getAsJsonObject();
                assertEquals(0, wertItem.get("id").getAsInt(), "id not 0" + id);
                assertFalse(wertItem.get("name").getAsString().isEmpty(), "name empty" + id);
                assertEquals(wertItem.get("name").getAsString(), mapping.get("wert").getAsString(), "wertItem not matching mapping" + id);
                assertFalse(wertItem.get("prices").getAsJsonObject().get("default").getAsString().isEmpty(), "default price empty" + id);
                assertEquals(1, wertItem.get("prices").getAsJsonObject().entrySet().size(), "prices size not 0" + id);
                assertFalse(wertItem.get("priceRange").getAsString().isEmpty(), "priceRange empty" + id);
                assertEquals(1, wertItem.get("descriptionLines").getAsJsonArray().size(), "descriptionLines size not 1" + id);
                assertDoesNotThrow(() -> new URL(wertItem.get("imageUrl").getAsString()), "imageUrl invalid" + id);
                assertNotEquals(0, wertItem.get("category").getAsJsonArray().size(), "category empty" + id);
                assertTrue(wertItem.get("url").isJsonNull(), "url not null" + id);
                assertEquals(8, wertItem.entrySet().size(), "wertItem size not 8" + id);
            }
        }).orElse(Assertions::fail);
    }

}
