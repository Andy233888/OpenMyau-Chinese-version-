package myau.module.modules;

import myau.event.EventTarget;
import myau.events.Render2DEvent;
import myau.module.Module;
import myau.util.TimerUtil;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.TextProperty;
import net.minecraft.client.Minecraft;

public class Spammer extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final TimerUtil timer = new TimerUtil();
    private int charOffset = 19968;
    public final TextProperty text = new TextProperty("消息内容", "最强myau使用者");
    public final FloatProperty delay = new FloatProperty("延迟", 3.5F, 0.0F, 3600.0F);
    public final IntProperty random = new IntProperty("随机", 0, 0, 10);

    public Spammer() {
        super("自动发消息", false);
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (this.isEnabled()) {
            if (this.timer.hasTimeElapsed((long) (this.delay.getValue() * 1000.0F))) {
                this.timer.reset();
                String text = this.text.getValue();
                if (this.random.getValue() > 0) {
                    text = String.format("%s ", text);
                    for (int i = 0; i < this.random.getValue(); i++) {
                        text = String.format("%s%s", text, (char) this.charOffset);
                        this.charOffset++;
                        if (this.charOffset > 40959) {
                            this.charOffset = 19968;
                        }
                    }
                }
                mc.thePlayer.sendChatMessage(text);
            }
        }
    }
}
