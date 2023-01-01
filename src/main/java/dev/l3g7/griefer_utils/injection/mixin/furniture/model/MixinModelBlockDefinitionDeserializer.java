/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.injection.mixin.furniture.model;

import com.google.gson.*;
import dev.l3g7.griefer_utils.features.world.furniture.abstraction.ModModelBlockDefinition;
import dev.l3g7.griefer_utils.features.world.furniture.multipart.Multipart;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.util.JsonUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

@Mixin(ModelBlockDefinition.Deserializer.class)
public abstract class MixinModelBlockDefinitionDeserializer {

	@Shadow
	protected abstract List<ModelBlockDefinition.Variants> parseVariantsList(JsonDeserializationContext p_178334_1_, JsonObject p_178334_2_);

	@Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/ModelBlockDefinition;", at = @At("HEAD"), cancellable = true)
	public void injectDeserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext, CallbackInfoReturnable<ModelBlockDefinition> cir) {
		JsonObject jsonobject = jsonElement.getAsJsonObject();
		List<ModelBlockDefinition.Variants> variants = parseVariantsList(jsonDeserializationContext, jsonobject);
		Multipart multipart = parseMultipart(jsonDeserializationContext, jsonobject);

		if (variants.isEmpty() && (multipart == null || multipart.getVariants().isEmpty()))
			throw new JsonParseException("Neither 'variants' nor 'multipart' found");

		ModelBlockDefinition modelBlockDefinition = new ModelBlockDefinition(variants);
		((ModModelBlockDefinition) modelBlockDefinition).setMultipart(multipart);
		cir.setReturnValue(modelBlockDefinition);
	}

	@Inject(method = "parseVariantsList", at = @At("HEAD"), cancellable = true)
	public void injectParseVariantsList(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject, CallbackInfoReturnable<List<ModelBlockDefinition.Variants>> cir) {
		if (!jsonObject.has("variants"))
			cir.setReturnValue(Collections.emptyList());
	}

	protected Multipart parseMultipart(JsonDeserializationContext deserializationContext, JsonObject object) {
		if (!object.has("multipart"))
			return null;

		JsonArray jsonarray = JsonUtils.getJsonArray(object, "multipart");
		return deserializationContext.deserialize(jsonarray, Multipart.class);
	}

}
