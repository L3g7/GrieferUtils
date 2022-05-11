package dev.l3g7.griefer_utils.features.tweaks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.MMCustomPayloadEvent;
import dev.l3g7.griefer_utils.event.events.render.EntityRenderEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.RenderUtil;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.entity.AbstractClientPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class ClanTags extends Feature {

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Clantags")
            .description("Zeigt den Clantag eines Spielers unter seinem Nametag.")
            .config("tweaks.clan_tags.active")
            .icon("rainbow_name")
            .defaultValue(true);

    public ClanTags() {
        super(Category.TWEAK);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    private final Map<UUID, String> subtitles = new HashMap<>();

    @EventListener
    public void onMMCustomPayload(MMCustomPayloadEvent event) {
        if (event.getChannel().equals("user_subtitle")) {
            for (JsonElement elem : event.getPayload().getAsJsonArray()) {
                JsonObject obj = elem.getAsJsonObject();
                subtitles.put(UUID.fromString(obj.get("targetId").getAsString()), obj.get("text").getAsString().replace('&', '§'));
            }
        }
    }

    @EventListener
    public void onRender(EntityRenderEvent event) {
        if (!isActive() || !isOnGrieferGames())
            return;

        if (!(event.getEntity() instanceof AbstractClientPlayer))
            return;

        RenderUtil.renderSubOrSuperTitles((AbstractClientPlayer) event.getEntity(), event.getX(), event.getY(), event.getZ(), subtitles.get(event.getEntity().getUniqueID()), null);
    }


}
