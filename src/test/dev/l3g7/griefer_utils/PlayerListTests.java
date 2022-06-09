package dev.l3g7.griefer_utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.util.IOUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerListTests {

    private void testDefaultList(String url) {
        IOUtil.request(url).asJsonArray(array -> {
            int index = 0;
            for (JsonElement entry : array) {
                String id = " (#" + index++ + ")";
                JsonObject object = entry.getAsJsonObject();
                assertFalse(object.get("name").getAsString().isEmpty(), "name empty" + id);
                assertDoesNotThrow(() -> UUID.fromString(object.get("uuid").getAsString()), "invalid uuid" + id);
            }
        }).orElse(Assertions::fail);
    }

    @Test
    @Timeout(5)
    public void testScammerRadarScammers() {
        testDefaultList("http://newh1ve.de:8080/scammer/scammers");
    }

    @Test
    @Timeout(5)
    public void testScammerRadarMMs() {
        testDefaultList("http://newh1ve.de:8080/mm/middlemans");
    }

    @Test
    @Timeout(5)
    public void testRealMatesMMs() {
        IOUtil.request("http://api.realmates.tk/mmapi.php?CB=aktimagalles")
                .asString(v -> assertTrue(v.contains(";")))
                .orElse(Assertions::fail);
    }

    @Test
    @Timeout(5)
    public void testRealMatesMembers() {
        IOUtil.request("http://api.realmates.tk/mmapi.php?member")
                .asString(v -> assertTrue(v.contains(";")))
                .orElse(Assertions::fail);
    }

}
