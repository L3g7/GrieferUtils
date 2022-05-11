package dev.l3g7.griefer_utils.features.modules;


import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import net.labymod.settings.elements.ControlElement.IconData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.MovingObjectPosition;

@Singleton
public class HeadOwner extends Module {

    public HeadOwner() {
        super("HeadOwner", "Zeigt dir den Spieler, dessen Kopf du ansiehst.", "head-owner", new IconData("griefer_utils/icons/info.png"));
    }

    @Override
    public String[] getDefaultValues() {
        return new String[]{"Kein Spielerkopf"};
    }

    @Override
    public String[] getValues() {
        TileEntity e = rayTraceTileEntity();
        if (e instanceof TileEntitySkull) {
            TileEntitySkull s = (TileEntitySkull) e;
            if (s.getPlayerProfile() != null && s.getPlayerProfile().getName() != null && !s.getPlayerProfile().getName().isEmpty())
                return new String[]{s.getPlayerProfile().getName()};
        }
        return getDefaultValues();
    }

    @Override
    public boolean isShown() {
        return super.isShown() && rayTraceTileEntity() instanceof TileEntitySkull;
    }

    private TileEntity rayTraceTileEntity() {
        if (mc.thePlayer == null)
            return null;

        MovingObjectPosition rayTraceResult = mc.thePlayer.rayTrace(1000, 1);
        if (rayTraceResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
            return null;

        return mc.theWorld.getTileEntity(rayTraceResult.getBlockPos());
    }


}
