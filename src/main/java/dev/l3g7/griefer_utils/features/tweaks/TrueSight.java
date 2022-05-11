package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.render.RenderInvisibilityCheckEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.SliderSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Singleton
public class TrueSight extends Feature {

    private static final SliderSetting opacity = new SliderSetting()
            .name("Durchsichtigkeit (%)")
            .icon("fading_steve")
            .min(0).max(100)
            .config("tweaks.true_sight.opacity")
            .defaultValue(85);

    private static final BooleanSetting enabled = new BooleanSetting()
            .name("TrueSight")
            .config("tweaks.true_sight.active")
            .icon("blue_lightbulb")
            .description("Macht unsichtbare Entities sichtbar")
            .defaultValue(true)
            .subSettingsWithHeader("TrueSight", opacity, new HeaderSetting());

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    public TrueSight() {
        super(Category.TWEAK);

        // Populate subsettings
        addEntity(EntityArmorStand.class, "Armorstand", new ItemStack(Items.armor_stand));
        addEntity(EntityBat.class, "Fledermaus", new ItemStack(Items.spawn_egg, 1, 65));
        addEntity(EntityBlaze.class, "Blaze", new ItemStack(Items.spawn_egg, 1, 61));
        addEntity(EntityCaveSpider.class, "Höhlenspinne", new ItemStack(Items.spawn_egg, 1, 59));
        addEntity(EntityChicken.class, "Huhn", new ItemStack(Items.spawn_egg, 1, 93));
        addEntity(EntityCow.class, "Kuh", new ItemStack(Items.spawn_egg, 1, 92));
        addEntity(EntityCreeper.class, "Creeper", new ItemStack(Items.spawn_egg, 1, 50));
        addEntity(EntityDragon.class, "Enderdrache", new ItemStack(Item.getItemFromBlock(Blocks.dragon_egg)));
        addEntity(EntityEnderman.class, "Enderman", new ItemStack(Items.spawn_egg, 1, 58));
        addEntity(EntityEndermite.class, "Endermite", new ItemStack(Items.spawn_egg, 1, 67));
        addEntity(EntityFallingBlock.class, "FallingBlock", new ItemStack(Item.getItemFromBlock(Blocks.sand)));
        addEntity(EntityGhast.class, "Ghast", new ItemStack(Items.spawn_egg, 1, 56));
        addEntity(EntityGiantZombie.class, "Riese", new ItemStack(Items.spawn_egg, 1, 54));
        addEntity(EntityGuardian.class, "Guardian", new ItemStack(Items.spawn_egg, 1, 68));
        addEntity(EntityHorse.class, "Pferd", new ItemStack(Items.spawn_egg, 1, 100));
        addEntity(EntityIronGolem.class, "Eisengolem", new ItemStack(Item.getItemFromBlock(Blocks.iron_block)));
        addEntity(EntityMagmaCube.class, "Magmawürfel", new ItemStack(Items.spawn_egg, 1, 62));
        addEntity(EntityMooshroom.class, "Pilzkuh", new ItemStack(Items.spawn_egg, 1, 96));
        addEntity(EntityOcelot.class, "Ozelot", new ItemStack(Items.spawn_egg, 1, 98));
        addEntity(EntityPig.class, "Schwein", new ItemStack(Items.spawn_egg, 1, 90));
        addEntity(EntityPigZombie.class, "Schweinezombie", new ItemStack(Items.spawn_egg, 1, 57));
        addEntity(EntityPlayer.class, "Spieler", new ItemStack(Items.skull, 1, 3));
        addEntity(EntityRabbit.class, "Hase", new ItemStack(Items.spawn_egg, 1, 101));
        addEntity(EntitySheep.class, "Schaf", new ItemStack(Items.spawn_egg, 1, 91));
        addEntity(EntitySilverfish.class, "Silberfischchen", new ItemStack(Items.spawn_egg, 1, 60));
        addEntity(EntitySkeleton.class, "Skelett", new ItemStack(Items.spawn_egg, 1, 51));
        addEntity(EntitySlime.class, "Slime", new ItemStack(Items.spawn_egg, 1, 55));
        addEntity(EntitySnowman.class, "Schneegolem", new ItemStack(Items.snowball));
        addEntity(EntitySpider.class, "Spinne", new ItemStack(Items.spawn_egg, 1, 52));
        addEntity(EntitySquid.class, "Tintenfisch", new ItemStack(Items.spawn_egg, 1, 94));
        addEntity(EntityVillager.class, "Dorfbewohner", new ItemStack(Items.spawn_egg, 1, 120));
        addEntity(EntityWitch.class, "Hexe", new ItemStack(Items.spawn_egg, 1, 66));
        addEntity(EntityWolf.class, "Wolf", new ItemStack(Items.spawn_egg, 1, 95));
        addEntity(EntityZombie.class, "Zombie", new ItemStack(Items.spawn_egg, 1, 54));
    }

    private void addEntity(Class<?> entityClass, String name, ItemStack icon) {
        enabled.subSettings(new BooleanSetting()
                .name(name)
                .config("tweaks.truesight.entities." + entityClass.getSimpleName().toLowerCase())
                .icon(icon)
                .defaultValue(entityClass == EntityPlayer.class));
    }

    @EventListener
    public void onRenderInvisibleEntity(RenderInvisibilityCheckEvent event) {
        if(!isActive())
            return;

        // Iterate through entity's super classes
        Entity entity = event.getEntity();
        Class<?> clazz = entity.getClass();
        do {
            // Check if config key exists
            String key = "tweaks.truesight.entities." + clazz.getSimpleName().toLowerCase();
            if(Config.has(key)) {
                // Render if TrueSight is enabled for this entity
                if(Config.get(key).getAsBoolean())
                    event.render();
                return;
            }
        } while((clazz = clazz.getSuperclass()) != null);
    }

    // Called by RendererLivingEntityEditor
    public static float getRenderModelAlpha() {
        if(!enabled.get() || !ServerCheck.isOnGrieferGames() || !Category.TWEAK.setting.get())
            return 0.15f;

        return 0.01f * (100f - opacity.get());
    }

}
