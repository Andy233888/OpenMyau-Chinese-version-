package myau;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.ksyz.accountmanager.AccountManager;
import myau.command.CommandManager;
import myau.command.commands.*;
import myau.config.Config;
import myau.event.EventManager;
import myau.management.*;
import myau.module.Module;
import myau.module.ModuleManager;
import myau.module.modules.*;
import myau.property.Property;
import myau.property.PropertyManager;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class Myau {
    public static String clientName = "&7[&cM&6y&ea&au&7]&r ";
    public static String version;
    public static RotationManager rotationManager;
    public static FloatManager floatManager;
    public static BlinkManager blinkManager;
    public static DelayManager delayManager;
    public static LagManager lagManager;
    public static PlayerStateManager playerStateManager;
    public static FriendManager friendManager;
    public static TargetManager targetManager;
    public static PropertyManager propertyManager;
    public static ModuleManager moduleManager;
    public static CommandManager commandManager;

    public Myau() {
        this.init();
    }

    /** 将 Java 字段名转换为连字符小写英文键（正确处理 CPS、PVP 等缩写）：autoBlockMinCPS -> auto-block-min-cps */
    static String kebabCase(String fieldName) {
        StringBuilder sb = new StringBuilder();
        int length = fieldName.length();
        for (int i = 0; i < length; i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i != 0) {
                    char prev = fieldName.charAt(i - 1);
                    char next = (i + 1 < length) ? fieldName.charAt(i + 1) : 0;
                    if (Character.isLowerCase(prev) || (Character.isUpperCase(prev) && Character.isLowerCase(next))) {
                        sb.append('-');
                    }
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public void init() {
        rotationManager = new RotationManager();
        floatManager = new FloatManager();
        blinkManager = new BlinkManager();
        delayManager = new DelayManager();
        lagManager = new LagManager();
        playerStateManager = new PlayerStateManager();
        friendManager = new FriendManager();
        targetManager = new TargetManager();
        propertyManager = new PropertyManager();
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        EventManager.register(rotationManager);
        EventManager.register(floatManager);
        EventManager.register(blinkManager);
        EventManager.register(delayManager);
        EventManager.register(lagManager);
        EventManager.register(moduleManager);
        EventManager.register(commandManager);
        moduleManager.modules.put(AimAssist.class, new AimAssist());
        moduleManager.modules.put(AntiAFK.class, new AntiAFK());
        moduleManager.modules.put(AntiDebuff.class, new AntiDebuff());
        moduleManager.modules.put(AntiFireball.class, new AntiFireball());
        moduleManager.modules.put(AntiObbyTrap.class, new AntiObbyTrap());
        moduleManager.modules.put(AntiObfuscate.class, new AntiObfuscate());
        moduleManager.modules.put(AntiVoid.class, new AntiVoid());
        moduleManager.modules.put(AutoClicker.class, new AutoClicker());
        moduleManager.modules.put(AutoHeal.class, new AutoHeal());
        moduleManager.modules.put(AutoTool.class, new AutoTool());
        moduleManager.modules.put(BedNuker.class, new BedNuker());
        moduleManager.modules.put(BedESP.class, new BedESP());
        moduleManager.modules.put(BedTracker.class, new BedTracker());
        moduleManager.modules.put(Blink.class, new Blink());
        moduleManager.modules.put(Chams.class, new Chams());
        moduleManager.modules.put(ChestESP.class, new ChestESP());
        moduleManager.modules.put(ChestStealer.class, new ChestStealer());
        moduleManager.modules.put(Eagle.class, new Eagle());
        moduleManager.modules.put(ESP.class, new ESP());
        moduleManager.modules.put(FastPlace.class, new FastPlace());
        moduleManager.modules.put(Freeze.class, new Freeze());
        moduleManager.modules.put(Fly.class, new Fly());
        moduleManager.modules.put(FullBright.class, new FullBright());
        moduleManager.modules.put(GhostHand.class, new GhostHand());
        moduleManager.modules.put(GuiModule.class, new GuiModule());
        moduleManager.modules.put(HitSelect.class, new HitSelect());
        moduleManager.modules.put(HUD.class, new HUD());
        moduleManager.modules.put(MoreKB.class, new MoreKB());
        moduleManager.modules.put(Indicators.class, new Indicators());
        moduleManager.modules.put(InventoryClicker.class, new InventoryClicker());
        moduleManager.modules.put(InvManager.class, new InvManager());
        moduleManager.modules.put(InvWalk.class, new InvWalk());
        moduleManager.modules.put(ItemESP.class, new ItemESP());
        moduleManager.modules.put(Jesus.class, new Jesus());
        moduleManager.modules.put(KeepSprint.class, new KeepSprint());
        moduleManager.modules.put(HitBox.class, new HitBox());
        moduleManager.modules.put(KillAura.class, new KillAura());
        moduleManager.modules.put(LagRange.class, new LagRange());
        moduleManager.modules.put(LightningTracker.class, new LightningTracker());
        moduleManager.modules.put(LongJump.class, new LongJump());
        moduleManager.modules.put(MCF.class, new MCF());
        moduleManager.modules.put(NameTags.class, new NameTags());
        moduleManager.modules.put(NickHider.class, new NickHider());
        moduleManager.modules.put(NoFall.class, new NoFall());
        moduleManager.modules.put(NoHitDelay.class, new NoHitDelay());
        moduleManager.modules.put(NoHurtCam.class, new NoHurtCam());
        moduleManager.modules.put(NoJumpDelay.class, new NoJumpDelay());
        moduleManager.modules.put(NoRotate.class, new NoRotate());
        moduleManager.modules.put(NoSlow.class, new NoSlow());
        moduleManager.modules.put(Radar.class, new Radar());
        moduleManager.modules.put(Reach.class, new Reach());
        moduleManager.modules.put(Refill.class, new Refill());
        moduleManager.modules.put(SafeWalk.class, new SafeWalk());
        moduleManager.modules.put(Scaffold.class, new Scaffold());
        moduleManager.modules.put(AutoBlockIn.class, new AutoBlockIn());
        moduleManager.modules.put(Spammer.class, new Spammer());
        moduleManager.modules.put(Speed.class, new Speed());
        moduleManager.modules.put(SpeedMine.class, new SpeedMine());
        moduleManager.modules.put(Sprint.class, new Sprint());
        moduleManager.modules.put(TargetHUD.class, new TargetHUD());
        moduleManager.modules.put(TargetStrafe.class, new TargetStrafe());
        moduleManager.modules.put(Tracers.class, new Tracers());
        moduleManager.modules.put(Trajectories.class, new Trajectories());
        moduleManager.modules.put(Velocity.class, new Velocity());
        moduleManager.modules.put(ViewClip.class, new ViewClip());
        moduleManager.modules.put(Wtap.class, new Wtap());
        moduleManager.modules.put(Xray.class, new Xray());
        commandManager.commands.add(new BindCommand());
        commandManager.commands.add(new ConfigCommand());
        commandManager.commands.add(new DenickCommand());
        commandManager.commands.add(new FriendCommand());
        commandManager.commands.add(new HelpCommand());
        commandManager.commands.add(new HideCommand());
        commandManager.commands.add(new IgnCommand());
        commandManager.commands.add(new ItemCommand());
        commandManager.commands.add(new ListCommand());
        commandManager.commands.add(new ModuleCommand());
        commandManager.commands.add(new PlayerCommand());
        commandManager.commands.add(new ShowCommand());
        commandManager.commands.add(new TargetCommand());
        commandManager.commands.add(new ToggleCommand());
        commandManager.commands.add(new VclipCommand());
        for (Module module : moduleManager.modules.values()) {
            ArrayList<Property<?>> properties = new ArrayList<>();
            for (final Field field : module.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                final Object obj;
                try {
                    obj = field.get(module);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (obj instanceof Property<?>) {
                    Property<?> property = (Property<?>) obj;
                    // 配置键采用与语言无关的英文键：优先显式设置，否则由字段名自动派生
                    if (property.getConfigName().equals(property.getName())) {
                        property.setConfigName(kebabCase(field.getName()));
                    }
                    property.setOwner(module);
                    properties.add(property);
                }
            }
            propertyManager.properties.put(module.getClass(), properties);
            EventManager.register(module);
        }
        Config config = new Config("default", true);
        if (config.file.exists()) {
            config.load();
        }
        if (friendManager.file.exists()) {
            friendManager.load();
        }
        if (targetManager.file.exists()) {
            targetManager.load();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(config::save));

        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(Myau.class.getResourceAsStream("/version.json")), StandardCharsets.UTF_8)) {
            JsonObject modInfo = new JsonParser().parse(reader).getAsJsonObject();
            version = modInfo.get("version").getAsString();
        } catch (Exception e) {
            version = "dev";
        }

        AccountManager.init();
    }
}
