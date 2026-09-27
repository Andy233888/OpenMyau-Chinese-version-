package myau.module.modules;

import myau.module.Module;
import myau.property.properties.PercentProperty;

public class NoHurtCam extends Module {
    public final PercentProperty multiplier = new PercentProperty("multiplier", "倍数", 0);

    public NoHurtCam() {
        super("无伤害抖动", false, true);
    }
}
