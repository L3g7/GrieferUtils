package dev.l3g7.griefer_utils.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.minecraft.client.Minecraft;

import java.io.File;

public class Config {

    public static boolean has(String path) {
        if (path == null)
            return false;

        String[] parts = path.split("\\.");
        return resolve(parts).has(parts[parts.length - 1]);
    }

    public static JsonElement get(String path) {
        String[] parts = path.split("\\.");
        return resolve(parts).get(parts[parts.length - 1]);
    }

    public static void set(String path, JsonElement val) {
        String[] parts = path.split("\\.");
        resolve(parts).add(parts[parts.length - 1], val);
    }

    public static void set(String path, Boolean val) {
        set(path, val == null ? JsonNull.INSTANCE : new JsonPrimitive(val));
    }

    public static void set(String path, Integer val) {
        set(path, val == null ? JsonNull.INSTANCE : new JsonPrimitive(val));
    }

    public static void set(String path, String val) {
        set(path, val == null ? JsonNull.INSTANCE : new JsonPrimitive(val));
    }

    private static JsonObject resolve(String[] parts) {
        JsonObject o = loadConfig();
        for (int i = 0; i < parts.length - 1; i++) {
            if (!o.has(parts[i]) || !(o.get(parts[i]).isJsonObject()))
                o.add(parts[i], new JsonObject());
            o = o.get(parts[i]).getAsJsonObject();
        }
        return o;
    }

    private static final File configFile = new File(new File(Minecraft.getMinecraft().mcDataDir, "config"), "GrieferUtils.json");
    private static JsonObject config = null;

    public static void save() {
        if (config == null)
            config = new JsonObject();
        IOUtil.file(configFile).writeJson(config);
    }

    private static JsonObject loadConfig() {
        if (config == null) {
            IOUtil.file(configFile)
                    .readJsonObject(v -> config = v)
                    .orElse(t -> config = new JsonObject());
        }
        return config;
    }
}