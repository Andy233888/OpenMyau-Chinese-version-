package myau.module.modules;

import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Jesus extends Module {
    private static final DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
    public final FloatProperty speed = new FloatProperty("速度", 2.5F, 0.0F, 3.0F);
    public final BooleanProperty noPush = new BooleanProperty("不被水冲走", true);
    public final BooleanProperty groundOnly = new BooleanProperty("仅地面", true);

    public Jesus() {
        super("水上行走", false);
    }

    @Override
    public String[] getSuffix() {
        return new String[]{df.format(this.speed.getValue())};
    }
}
