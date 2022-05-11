package dev.l3g7.griefer_utils.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.function.Supplier;

public class JsonBuilder {

    public static JsonArray array(JsonElement... entries) {
        JsonArray array = new JsonArray();
        Arrays.stream(entries).forEach(array::add);
        return array;
    }

    private final JsonObject root;

    public JsonBuilder() {
        root = new JsonObject();
    }

    public JsonBuilder withSanitized(String property, String value) {
        if(value != null)
            value = value.replaceAll("([^a-zA-Z\\d ])", "\\\\$1");

        root.addProperty(property, value);
        return this;
    }

    public JsonBuilder with(String property, Object value) {
        if(value == null)
            root.add(property, JsonNull.INSTANCE);
        else if(value instanceof String)
            root.addProperty(property, (String) value);
        else if(value instanceof Number)
            root.addProperty(property, (Number) value);
        else if(value instanceof JsonElement)
            root.add(property, (JsonElement) value);
        return this;
    }

    public JsonBuilder withOptional(Supplier<Boolean> check, String property, Object value) {
        return check.get() ? with(property, value) : this;
    }

    public JsonObject build() {
        return root;
    }
}
