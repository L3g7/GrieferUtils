package dev.l3g7.griefer_utils.features.features.player_list;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.event.events.OnEnable;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.util.IOUtil;

import java.util.*;

@Singleton
public class PlayerListProvider {

    static final List<PlayerListEntry> scammerList = new ArrayList<>();
    static final List<PlayerListEntry> trustedList = new ArrayList<>();
    static final Map<Provider, Boolean> providerAvailability = new HashMap<>();

    @OnEnable
    private void init() {
        loadJson("http://newh1ve.de:8080/scammer/scammers", Provider.SCAMMER_RADAR, scammerList);
        loadJson("http://newh1ve.de:8080/mm/middlemans", Provider.SCAMMER_RADAR, trustedList);

        if (IOUtil.request("http://api.realmates.tk/mmapi.php?CB=aktimagalles").asString(res -> {
            for (String name : res.split(";"))
                trustedList.add(new PlayerListEntry(name, null, Provider.REAL_MATES));
        }).successful() && IOUtil.request("http://api.realmates.tk/mmapi.php?member").asString(res2 -> {
            for (String name : res2.split(";"))
                trustedList.add(new PlayerListEntry(name, null, Provider.REAL_MATES));
        }).successful())
            providerAvailability.put(Provider.REAL_MATES, true);
    }

    private void loadJson(String url, Provider provider, List<PlayerListEntry> list) {

        IOUtil.request(url).asJsonArray(array -> {
            for (JsonElement entry : array)
                list.add(new PlayerListEntry(entry, provider));
            providerAvailability.put(provider, true);
        });
    }

    public static class PlayerListEntry {

        private final String name;
        private final UUID uuid;
        private final Provider provider;

        public PlayerListEntry(JsonElement elem, Provider provider) {
            this(elem.getAsJsonObject().get("name").getAsString(), elem.getAsJsonObject().get("uuid").isJsonNull() ? null : elem.getAsJsonObject().get("uuid").getAsString(), provider);
        }

        public PlayerListEntry(String name, String uuid, Provider provider) {
            this.name = name;
            this.uuid = uuid == null ? null : UUID.fromString(uuid);
            this.provider = provider;
        }

        public String getName() {
            return name;
        }

        public UUID getUuid() {
            return uuid;
        }

        public Provider getProvider() {
            return provider;
        }

        @Override
        public String toString() {
            return "{'" + name + "', " + uuid + ", " + provider + '}';
        }
    }

    public enum Provider {

        SCAMMER_RADAR("ScammerRadar"), REAL_MATES("RealMates");

        private final String name;

        Provider(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
