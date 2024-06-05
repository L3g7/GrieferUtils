package dev.l3g7.griefer_utils.features.item.recraft.laby3.decompressor;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.features.item.recraft.laby3.RecraftAction;

public class DecompressAction extends RecraftAction {

	final Ingredient ingredient;

	public DecompressAction(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	@Override
	protected JsonElement toJson() {
		return new JsonPrimitive(ingredient.toLong());
	}

	public static DecompressAction fromJson(JsonElement element) {
		long value = element.getAsLong();
		return new DecompressAction(Ingredient.fromLong(value));
	}

}
