package myau.module.modules;

import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.PercentProperty;
import net.minecraft.client.Minecraft;

public class KeepSprint extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final PercentProperty slowdown = new PercentProperty("减速", 0);
    public final BooleanProperty groundOnly = new BooleanProperty("仅地面", false);
    public final BooleanProperty reachOnly = new BooleanProperty("仅触及", false);

    public KeepSprint() {
        super("保持疾跑", false);
    }

    public boolean shouldKeepSprint() {
        if (this.groundOnly.getValue() && !mc.thePlayer.onGround) {
            return false;
        } else {
            return !this.reachOnly.getValue() || mc.objectMouseOver.hitVec.distanceTo(mc.getRenderViewEntity().getPositionEyes(1.0F)) > 3.0;
        }
    }
}
