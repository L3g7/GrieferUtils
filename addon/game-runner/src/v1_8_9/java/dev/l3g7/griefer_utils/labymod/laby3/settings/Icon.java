package dev.l3g7.griefer_utils.labymod.laby3.settings;

import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.ControlElement.IconData;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.labymod.laby3.bridges.Laby3MinecraftBridge.laby3MinecraftBridge;

public abstract class Icon {

	public static Icon of(Object icon) {
		return laby3MinecraftBridge.createIcon(icon);
	}

	public abstract void draw(int x, int y, float scale);

	public static class WrappedIcon extends IconData {

		private final Icon icon;

		public WrappedIcon(Icon icon) {
			super((ResourceLocation) null);
			this.icon = icon;
		}

		@Override
		public boolean hasTextureIcon() {
			throw new UnsupportedOperationException("Cannot render custom icon");
		}

	}

	@Mixin(ControlElement.class)
	public static class MixinControlElement {

		@Unique
		private int grieferUtils$x, grieferUtils$y;

		@Inject(method = "draw", at = @At("HEAD"), remap = false)
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY, CallbackInfo ci) {
			grieferUtils$x = x;
			grieferUtils$y = y;
		}

		@Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lnet/labymod/settings/elements/ControlElement$IconData;hasTextureIcon()Z"), remap = false)
		public boolean draw(IconData data) {
			if (!(data instanceof WrappedIcon wrappedIcon))
				return data.hasTextureIcon();

			wrappedIcon.icon.draw(grieferUtils$x, grieferUtils$y, 1);
			return false;
		}

	}

}
