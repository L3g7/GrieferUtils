package dev.l3g7.griefer_utils.features.tweaks.webhooks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.event_bus.EventPriority;
import dev.l3g7.griefer_utils.event.events.OnEnable;
import dev.l3g7.griefer_utils.event.events.chat.ChatLineAddEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.JsonBuilder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import dev.l3g7.griefer_utils.util.VersionUtil;
import net.labymod.ingamechat.tabs.GuiChatFilter;
import net.labymod.ingamechat.tools.filter.Filters;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class FilterWebhooks extends Feature {

    private final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private final JsonObject EMBED_FOOTER = new JsonBuilder()
            .with("text", Constants.ADDON_NAME + " v" + VersionUtil.getAddonVersion())
            .with("icon_url", "https://grieferutils.l3g7.dev/icon/padded/64x64")
            .build();

    static final Map<String, String> webhooks = new HashMap<>();

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Webhooks in Filtern")
            .config("tweaks.webhooks.active")
            .icon("webhook")
            .defaultValue(true);

    public FilterWebhooks() {
        super(Category.TWEAK);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    static void saveWebhooks() {
        JsonObject data = new JsonObject();
        for (Map.Entry<String, String> entry : webhooks.entrySet())
            data.addProperty(entry.getKey(), entry.getValue());
        Config.set("tweaks.webhooks.filter", data);
        Config.save();
    }

    @OnEnable
    private void loadWebhooks() {
        if (Config.has("tweaks.webhooks.filter"))
            for (Map.Entry<String, JsonElement> entry : Config.get("tweaks.webhooks.filter").getAsJsonObject().entrySet()) {
				String value = entry.getValue().getAsString();
				if (!value.trim().isEmpty())
		            webhooks.put(entry.getKey(), entry.getValue().getAsString());
            }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isActive())
            return;

        if (event.gui instanceof GuiChatFilter)
            event.gui = new CustomGuiChatFilter(Reflection.get(event.gui, "defaultInputFieldText", "field_146409_v", "u"));
    }

    @EventListener(priority = EventPriority.LOWEST)
    public void onMessageReceive(ChatLineAddEvent event) {
        if (!isActive())
            return;

        // Check if filters match
        String msg = event.getMessage().toLowerCase().replaceAll("§.", "");
        for (Filters.Filter filter : LabyMod.getInstance().getChatToolManager().getFilters()) {
            if (webhooks.containsKey(filter.getFilterName())
	                && !webhooks.get(filter.getFilterName()).trim().isEmpty()
                    && Arrays.stream(filter.getWordsContains()).anyMatch(w -> msg.contains(w.toLowerCase()))
                    && Arrays.stream(filter.getWordsContainsNot()).noneMatch(w -> msg.contains(w.toLowerCase()))) {
                // Build payload
                JsonObject root = new JsonBuilder()
                        .with("content", null)
                        .with("embeds", JsonBuilder.array(new JsonBuilder()
                                .withSanitized("title", filter.getFilterName())
                                .withSanitized("description", event.getMessage())
                                .withOptional(filter::isHighlightMessage, "color", ((filter.getHighlightColorR() & 0xff) << 16) | ((filter.getHighlightColorG() & 0xff) << 8) | (filter.getHighlightColorB() & 0xff))
                                .with("footer", EMBED_FOOTER)
                                .build()
                        )).build();

                // Send to webhook
                EXECUTOR_SERVICE.execute(() -> IOUtil.request(webhooks.get(filter.getFilterName()).trim())
                        .post("application/json", root.toString().getBytes(StandardCharsets.UTF_8))
                        .close());
            }
        }
    }


}
