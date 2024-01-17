/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.features.uncategorized.griefer_info;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.event.events.WindowClickEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiGrieferInfo;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.render.AsyncSkullRenderer;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class GrieferInfo extends Feature {

	private static final String openProfilNBT = "{id:\"minecraft:skull\",Count:1b,tag:{SkullOwner:{SkullOwner:{Id:\"6c55365e-8165-4525-aff9-7c65fabb0c99\",Properties:{textures:[0:{Signature:\"CEX3vg9fyRI34YFPsNySZ5qBMbgqsZF54XGfnC60DGTvEBxfOuFmGb3KKMsnUf6QDuzvWurtV1F9TD+88c35UkTwMXzFzKVbNZEOHWt/wCXrQ6Ay6nJ9At7zyReh+27iPxqQzYB+JmoHH7uUmzKsLTfogpAGuKPv7gxOpGj1fVImTH4euHOtEkDU6+hEpCs2PNnlVRQLPsJZTB9Fkaq31Eu+5DhL14UfLgG4nHk757ZVpkAee9E/kc+UfLZeHAw6xeR/6VUZhRqSk9CebJQ70XwA/h51phFILnIT3xJrk4gfQO15zF6jnpDmftr+xj/QZZ3VLAfKmvJoxadOFrHp/bh5abOrv3guph1yj0ugQHaLnwJp+Fp691E4q2a/334Xs5Kisxis0zgbLF5Rijx9zo4Zpj2+Rq40TaFPxkGNcPqSmWoGxN6ZyS/b8n+mgF+DU8iSGVnh8oZQNzXxVdma7phg19EmSJmeYWd/cpRlACBIdSThCxzZvJLdVP0WIWWCRwLIBlfPfE3dgNblaXYf6IdPmgPQRnQdCoM14nUXQrmypHc1PzHPyvxsmew2Zpqo7AGydc+hO2eVv+TeGjdvsmxXCWCNmINjF6YuNp0ECovFpz9b/aR+T2dYqDAexl8y3ZOJGGAjqLSkgPU5ojmtTSnno0W/mI8/arxBMIW8q6w=\",Value:\"ewogICJ0aW1lc3RhbXAiIDogMTcwMjEzMDQ3MjE4MiwKICAicHJvZmlsZUlkIiA6ICI2YzU1MzY1ZTgxNjU0NTI1YWZmOTdjNjVmYWJiMGM5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJHcmllZmVySW5mbyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNTFlMDQwZDE4OWNmMmY4OGZjMGZhM2VlMzE3MDc2MzNiYmU2ZjAyMzZjZjVkZjU4ODAwMTU5Y2UwMDczZmRlIgogICAgfQogIH0KfQ==\"}]},Name:\"GrieferInfo\"},Id:\"6c55365e-8165-4525-aff9-7c65fabb0c99\",Properties:{textures:[0:{Signature:\"CEX3vg9fyRI34YFPsNySZ5qBMbgqsZF54XGfnC60DGTvEBxfOuFmGb3KKMsnUf6QDuzvWurtV1F9TD+88c35UkTwMXzFzKVbNZEOHWt/wCXrQ6Ay6nJ9At7zyReh+27iPxqQzYB+JmoHH7uUmzKsLTfogpAGuKPv7gxOpGj1fVImTH4euHOtEkDU6+hEpCs2PNnlVRQLPsJZTB9Fkaq31Eu+5DhL14UfLgG4nHk757ZVpkAee9E/kc+UfLZeHAw6xeR/6VUZhRqSk9CebJQ70XwA/h51phFILnIT3xJrk4gfQO15zF6jnpDmftr+xj/QZZ3VLAfKmvJoxadOFrHp/bh5abOrv3guph1yj0ugQHaLnwJp+Fp691E4q2a/334Xs5Kisxis0zgbLF5Rijx9zo4Zpj2+Rq40TaFPxkGNcPqSmWoGxN6ZyS/b8n+mgF+DU8iSGVnh8oZQNzXxVdma7phg19EmSJmeYWd/cpRlACBIdSThCxzZvJLdVP0WIWWCRwLIBlfPfE3dgNblaXYf6IdPmgPQRnQdCoM14nUXQrmypHc1PzHPyvxsmew2Zpqo7AGydc+hO2eVv+TeGjdvsmxXCWCNmINjF6YuNp0ECovFpz9b/aR+T2dYqDAexl8y3ZOJGGAjqLSkgPU5ojmtTSnno0W/mI8/arxBMIW8q6w=\",Value:\"ewogICJ0aW1lc3RhbXAiIDogMTcwMjEzMDQ3MjE4MiwKICAicHJvZmlsZUlkIiA6ICI2YzU1MzY1ZTgxNjU0NTI1YWZmOTdjNjVmYWJiMGM5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJHcmllZmVySW5mbyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNTFlMDQwZDE4OWNmMmY4OGZjMGZhM2VlMzE3MDc2MzNiYmU2ZjAyMzZjZjVkZjU4ODAwMTU5Y2UwMDczZmRlIgogICAgfQogIH0KfQ==\"}]},Name:\"GrieferInfo\"},display:{Lore:[0:\"\",1:\"§fKlicke hier, um das Profil von §e%s§f auf §aGriefer.Info§f zu öffnen.\"],Name:\"§aGriefer.Info §fProfil öffnen\"},Player:\"%s\"},Damage:3s}";
	private static final Map<String, CompletableFuture<Optional<String>>> profileLinks = new ConcurrentHashMap<>();

	private final KeySetting setting = new KeySetting()
		.name("Gui öffnen")
		.icon("key")
		.description("Die Taste, mit der das Gui geöffnet werden soll.")
		.pressCallback(b -> { if (b) GuiGrieferInfo.GUI.open(); });

	private static final BooleanSetting profile = new BooleanSetting() {
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			AsyncSkullRenderer.renderSkull(x + 3, y + 2, "GrieferInfo");
		}
	}
		.name("Griefer.Info-Link im Profil")
		.description("Fügt einen Knopf zum Öffnen eines Spielers auf Griefer.Info zu seinem Profil hinzu.")
		.icon(new ControlElement.IconData());

	@MainElement
	private final CategorySetting button = new CategorySetting()
		.name("§xGriefer.Info")
		.icon("griefer_info")
		.subSettings(profile, setting, new HeaderSetting(), new HeaderSetting("Das Griefer.Info Gui lässt sich auch mit /info oder /gi öffnen."));

	@EventListener
	private static void onGuiModify(GuiModifyItemsEvent event) {
		if (!profile.get())
			return;

		if (!event.getTitle().startsWith("§6Profil"))
			return;

		ItemStack skull = event.getItem(13);
		if (skull == null || skull.getItem() != Items.skull)
			return;

		String name = skull.getDisplayName().substring(2);
		event.setItem(11, ItemUtil.fromNBT(String.format(openProfilNBT, name, name)));
		profileLinks.putIfAbsent(name, CompletableFuture.supplyAsync(() -> IOUtil.read("https://griefer.info/grieferutils/profile-link-by-name?name=" + name).asJsonString()));
	}

	@EventListener
	private static void onWindowClick(WindowClickEvent event) {
		if (event.itemStack == null || !event.itemStack.getDisplayName().equals("§aGriefer.Info §fProfil öffnen"))
			return;

		String player = event.itemStack.getTagCompound().getString("Player");

		Optional<String> profileLink = profileLinks.get(player).getNow(Optional.empty());
		if (profileLink.isPresent() && profileLink.get().isEmpty())
			MinecraftUtil.displayAchievement("§e§lFehler \u26A0", "§eDieser Spieler hat kein Profil!");
		else
			Util.openWebsite(profileLink.orElse("https://griefer.info/profile?keyword=" + player));
	}

}
