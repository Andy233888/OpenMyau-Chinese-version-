package myau.module.modules;

import myau.module.Module;
import myau.property.properties.BooleanProperty;

public class AntiDebuff extends Module {
    public final BooleanProperty blindness = new BooleanProperty("blindness", "失明", true);
    public final BooleanProperty nausea = new BooleanProperty("nausea", "恶心", true);

    public AntiDebuff() {
        super("反负面效果", false);
    }
}
