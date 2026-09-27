package myau.module.modules;

import com.google.common.base.CaseFormat;
import myau.Myau;
import myau.enums.BlinkModules;
import myau.event.EventManager;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.event.types.Priority;
import myau.events.*;
import myau.management.RotationState;
import myau.mixin.IAccessorMinecraft;
import myau.mixin.IAccessorPlayerControllerMP;
import myau.module.Module;
import myau.property.properties.*;
import myau.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.DataWatcher.WatchableObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.util.*;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.WorldSettings.GameType;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class KillAura extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final DecimalFormat df = new DecimalFormat("+0.0;-0.0", new DecimalFormatSymbols(Locale.US));
    private final TimerUtil timer = new TimerUtil();
    private AttackData target = null;
    private int switchTick = 0;
    private boolean hitRegistered = false;
    private boolean blockingState = false;
    private boolean isBlocking = false;
    private boolean fakeBlockState = false;
    private boolean blinkReset = false;
    private long attackDelayMS = 0L;
    private int blockTick = 0;
    private int lastTickProcessed;
    private boolean swapped = false;
    private boolean postBlock = false;
    private boolean postSwap = false;
    private int testAttackTick = 0;
    public final ModeProperty mode;
    public final ModeProperty sort;
    public final ModeProperty autoBlock;
    private final BooleanProperty noStop;
    private final BooleanProperty test;
    private final IntProperty moreAttackDelay;
    public final IntProperty maxTick;
    private final IntProperty startBlinkTick;
    private final IntProperty stopBlinkTick;
    private final IntProperty swapTick;
    private final IntProperty switchBackTick;
    private final IntProperty stopBlockTick;
    public final IntProperty attackTick;
    private final IntProperty startBlockTick;
    private final BooleanProperty postStartBlock;
    private final BooleanProperty alwaysRenderBlocking;
    private final BooleanProperty c09Instead;
    private final BooleanProperty secondSword;
    public final BooleanProperty autoBlockRequirePress;
    public final FloatProperty autoBlockMinCPS;
    public final FloatProperty autoBlockMaxCPS;
    public final FloatProperty autoBlockRange;
    public final FloatProperty swingRange;
    public final FloatProperty attackRange;
    public final IntProperty fov;
    public final IntProperty minCPS;
    public final IntProperty maxCPS;
    public final IntProperty switchDelay;
    public final ModeProperty rotations;
    public final ModeProperty moveFix;
    public final ModeProperty rotationMode;
    public final PercentProperty smoothing;
    public final IntProperty angleStep;
    public final BooleanProperty throughWalls;
    public final BooleanProperty requirePress;
    public final BooleanProperty allowMining;
    public final BooleanProperty allowPlayerBlocking;
    public final BooleanProperty weaponsOnly;
    public final BooleanProperty allowTools;
    public final BooleanProperty inventoryCheck;
    public final BooleanProperty lowTimerCheck;
    public final BooleanProperty botCheck;
    public final BooleanProperty players;
    public final BooleanProperty bosses;
    public final BooleanProperty mobs;
    public final BooleanProperty animals;
    public final BooleanProperty golems;
    public final BooleanProperty silverfish;
    public final BooleanProperty teams;
    public final ModeProperty showTarget;
    public final ModeProperty debugLog;

    private long getAttackDelay() {
        return this.isBlocking ? (long) (1000.0F / RandomUtil.nextLong(this.autoBlockMinCPS.getValue().longValue(), this.autoBlockMaxCPS.getValue().longValue())) : 1000L / RandomUtil.nextLong(this.minCPS.getValue(), this.maxCPS.getValue());
    }

    private boolean performAttack(float yaw, float pitch) {
        if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
            if (Velocity.stoppedBlock) {
                return false;
            } else if (this.isPlayerBlocking() && this.autoBlock.getValue() != 1) {
                return false;
            } else if (this.attackDelayMS > 0L) {
                return false;
            } else if (((IAccessorMinecraft) mc).getTimer().timerSpeed < 1F && lowTimerCheck.getValue()) {
                return false;
            } else if (Velocity.extraAttacked && this.autoBlock.getValue() >= 2 && this.autoBlock.getValue() <= 7) {
                Velocity.extraAttacked = false;
                Velocity velocity = (Velocity) Myau.moduleManager.modules.get(Velocity.class);
                int ab = this.autoBlock.getValue();
                if (velocity.reduceMode.getValue() == 2) {
                    if (ab == 2 || ab == 3) {
                        blockTick = 0;
                    } else if (ab == 4) {
                        blockTick = attackTick.getValue();
                    } else if (ab == 5) {
                        blockTick = (blockTick == 3) ? 0 : 2;
                    } else if (ab == 6) {
                        blockTick = 0;
                    } else if (ab == 7) {
                        blockTick = 0;
                    }
                } else if (velocity.reduceMode.getValue() == 1) {
                    if (ab == 2 || ab == 3) {
                        blockTick = 2;
                    } else if (ab == 4) {
                        blockTick = attackTick.getValue();
                    } else if (ab == 5) {
                        blockTick = (blockTick == 0) ? 1 : 3;
                    } else if (ab == 6) {
                        blockTick = 2;
                    } else if (ab == 7) {
                        blockTick = 1;
                    }
                }
                return false;
            } else {
                this.attackDelayMS = this.attackDelayMS + this.getAttackDelay();
                mc.thePlayer.swingItem();
                if ((this.rotations.getValue() != 0 || !this.isBoxInAttackRange(this.target.getBox()))
                        && RotationUtil.rayTrace(this.target.getBox(), yaw, pitch, this.attackRange.getValue()) == null) {
                    return false;
                } else {
                    AttackEvent event = new AttackEvent(this.target.getEntity());
                    EventManager.call(event);
                    ((IAccessorPlayerControllerMP) mc.playerController).callSyncCurrentPlayItem();
                    PacketUtil.sendPacket(new C02PacketUseEntity(this.target.getEntity(), Action.ATTACK));
                    if (mc.playerController.getCurrentGameType() != GameType.SPECTATOR) {
                        PlayerUtil.attackEntity(this.target.getEntity());
                    }
                    this.hitRegistered = true;
                    return true;
                }
            }
        } else {
            return false;
        }
    }

    private void sendUseItem() {
        ((IAccessorPlayerControllerMP) mc.playerController).callSyncCurrentPlayItem();
        this.startBlock(mc.thePlayer.getHeldItem());
    }

    private void startBlock(ItemStack itemStack) {
        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(itemStack));
        mc.thePlayer.setItemInUse(itemStack, itemStack.getMaxItemUseDuration());
        this.blockingState = true;
    }

    private void stopBlock() {
        PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        mc.thePlayer.stopUsingItem();
        this.blockingState = false;
    }

    private void interactAttack(float yaw, float pitch) {
        if (this.target != null) {
            MovingObjectPosition mop = RotationUtil.rayTrace(this.target.getBox(), yaw, pitch, 8.0);
            if (mop != null) {
                ((IAccessorPlayerControllerMP) mc.playerController).callSyncCurrentPlayItem();
                PacketUtil.sendPacket(
                        new C02PacketUseEntity(
                                this.target.getEntity(),
                                new Vec3(mop.hitVec.xCoord - this.target.getX(), mop.hitVec.yCoord - this.target.getY(), mop.hitVec.zCoord - this.target.getZ())
                        )
                );
                PacketUtil.sendPacket(new C02PacketUseEntity(this.target.getEntity(), Action.INTERACT));
                PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                mc.thePlayer.setItemInUse(mc.thePlayer.getHeldItem(), mc.thePlayer.getHeldItem().getMaxItemUseDuration());
                this.blockingState = true;
            }
        }
    }

    private boolean canAttack() {
        if (this.inventoryCheck.getValue() && mc.currentScreen instanceof GuiContainer) {
            return false;
        } else if (!(Boolean) this.weaponsOnly.getValue()
                || ItemUtil.hasRawUnbreakingEnchant()
                || this.allowTools.getValue() && ItemUtil.isHoldingTool()) {
            if (((IAccessorPlayerControllerMP) mc.playerController).getIsHittingBlock()) {
                return false;
            } else if ((ItemUtil.isEating() || ItemUtil.isUsingBow()) && PlayerUtil.isUsingItem()) {
                return false;
            } else {
                AutoHeal autoHeal = (AutoHeal) Myau.moduleManager.modules.get(AutoHeal.class);
                if (autoHeal.isEnabled() && autoHeal.isSwitching()) {
                    return false;
                } else {
                    BedNuker bedNuker = (BedNuker) Myau.moduleManager.modules.get(BedNuker.class);
                    AutoBlockIn autoBlockIn = (AutoBlockIn) Myau.moduleManager.modules.get(AutoBlockIn.class);
                    if (bedNuker.isEnabled() && bedNuker.isReady()) {
                        return false;
                    } else if (Myau.moduleManager.modules.get(Scaffold.class).isEnabled()) {
                        return false;
                    } else if (autoBlockIn.isEnabled()) {
                        return false;
                    } else if (this.requirePress.getValue()) {
                        return PlayerUtil.isAttacking();
                    } else {
                        return !this.allowMining.getValue() || !mc.objectMouseOver.typeOfHit.equals(MovingObjectType.BLOCK) || !PlayerUtil.isAttacking();
                    }
                }
            }
        } else {
            return false;
        }
    }

    private boolean canAutoBlock() {
        if (Velocity.stoppedBlock) {
            return false;
        } else if (!ItemUtil.isHoldingSword()) {
            return false;
        } else {
            return !this.autoBlockRequirePress.getValue() || PlayerUtil.isUsingItem();
        }
    }

    private boolean isNormalTargetVisible(AxisAlignedBB box) {
        if (mc.thePlayer == null || mc.theWorld == null) return false;
        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0F);
        double minTargetY = box.minY + 0.05 * (box.maxY - box.minY);
        double maxTargetY = box.minY + 0.75 * (box.maxY - box.minY);
        double targetY = MathHelper.clamp_double(eyePos.yCoord, minTargetY, maxTargetY);
        double targetX = (box.minX + box.maxX) / 2.0;
        double targetZ = (box.minZ + box.maxZ) / 2.0;
        Vec3 targetPoint = new Vec3(targetX, targetY, targetZ);
        MovingObjectPosition mop = mc.theWorld.rayTraceBlocks(eyePos, targetPoint, false, true, false);
        return mop == null;
    }

    private boolean hasValidTarget() {
        return mc.theWorld
                .loadedEntityList
                .stream()
                .anyMatch(
                        entity -> entity instanceof EntityLivingBase
                                && this.isValidTarget((EntityLivingBase) entity)
                                && this.isInBlockRange((EntityLivingBase) entity)
                );
    }

    private boolean isValidTarget(EntityLivingBase entityLivingBase) {
        if (!mc.theWorld.loadedEntityList.contains(entityLivingBase)) {
            return false;
        } else if (entityLivingBase != mc.thePlayer && entityLivingBase != mc.thePlayer.ridingEntity) {
            if (entityLivingBase == mc.getRenderViewEntity() || entityLivingBase == mc.getRenderViewEntity().ridingEntity) {
                return false;
            } else if (entityLivingBase.deathTime > 0) {
                return false;
            } else if (RotationUtil.angleToEntity(entityLivingBase) > this.fov.getValue().floatValue()) {
                return false;
            } else if (!this.throughWalls.getValue() && RotationUtil.rayTrace(entityLivingBase) != null) {
                return false;
            } else if (entityLivingBase instanceof EntityOtherPlayerMP) {
                if (!this.players.getValue()) {
                    return false;
                } else if (TeamUtil.isFriend((EntityPlayer) entityLivingBase)) {
                    return false;
                } else {
                    return (!this.teams.getValue() || !TeamUtil.isSameTeam((EntityPlayer) entityLivingBase)) && (!this.botCheck.getValue() || !TeamUtil.isBot((EntityPlayer) entityLivingBase));
                }
            } else if (entityLivingBase instanceof EntityDragon || entityLivingBase instanceof EntityWither) {
                return this.bosses.getValue();
            } else if (!(entityLivingBase instanceof EntityMob) && !(entityLivingBase instanceof EntitySlime)) {
                if (entityLivingBase instanceof EntityAnimal
                        || entityLivingBase instanceof EntityBat
                        || entityLivingBase instanceof EntitySquid
                        || entityLivingBase instanceof EntityVillager) {
                    return this.animals.getValue();
                } else if (!(entityLivingBase instanceof EntityIronGolem)) {
                    return false;
                } else {
                    return this.golems.getValue() && (!this.teams.getValue() || !TeamUtil.hasTeamColor(entityLivingBase));
                }
            } else if (!(entityLivingBase instanceof EntitySilverfish)) {
                return this.mobs.getValue();
            } else {
                return this.silverfish.getValue() && (!this.teams.getValue() || !TeamUtil.hasTeamColor(entityLivingBase));
            }
        } else {
            return false;
        }
    }

    private boolean isInRange(EntityLivingBase entityLivingBase) {
        return this.isInBlockRange(entityLivingBase) || this.isInSwingRange(entityLivingBase) || this.isInAttackRange(entityLivingBase);
    }

    private boolean isInBlockRange(EntityLivingBase entityLivingBase) {
        return RotationUtil.distanceToEntity(entityLivingBase) <= (double) this.autoBlockRange.getValue();
    }

    private boolean isInSwingRange(EntityLivingBase entityLivingBase) {
        return RotationUtil.distanceToEntity(entityLivingBase) <= (double) this.swingRange.getValue();
    }

    private boolean isBoxInSwingRange(AxisAlignedBB axisAlignedBB) {
        return RotationUtil.distanceToBox(axisAlignedBB) <= (double) this.swingRange.getValue();
    }

    private boolean isInAttackRange(EntityLivingBase entityLivingBase) {
        return RotationUtil.distanceToEntity(entityLivingBase) <= (double) this.attackRange.getValue();
    }

    private boolean isBoxInAttackRange(AxisAlignedBB axisAlignedBB) {
        return RotationUtil.distanceToBox(axisAlignedBB) <= (double) this.attackRange.getValue();
    }

    private boolean isPlayerTarget(EntityLivingBase entityLivingBase) {
        return entityLivingBase instanceof EntityPlayer && TeamUtil.isTarget((EntityPlayer) entityLivingBase);
    }

    private int findEmptySlot(int currentSlot) {
        for (int i = 0; i < 9; i++) {
            if (i != currentSlot && mc.thePlayer.inventory.getStackInSlot(i) == null) {
                return i;
            }
        }
        for (int i = 0; i < 9; i++) {
            if (i != currentSlot) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (stack != null && !stack.hasDisplayName()) {
                    return i;
                }
            }
        }
        return Math.floorMod(currentSlot - 1, 9);
    }

    private int findSwordSlot(int currentSlot) {
        for (int i = 0; i < 9; i++) {
            if (i != currentSlot) {
                ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
                if (item != null && item.getItem() instanceof ItemSword) {
                    return i;
                }
            }
        }
        return -1;
    }

    public KillAura() {
        super("杀戮", false);
        this.lastTickProcessed = 0;
        this.mode = new ModeProperty("模式", 0, new String[]{"SINGLE", "SWITCH"});
        this.sort = new ModeProperty("排序", 0, new String[]{"DISTANCE", "HEALTH", "HURT_TIME", "FOV"});
        this.autoBlock = new ModeProperty(
                "自动格挡(ab)", 2, new String[]{"NONE", "VANILLA", "OLD_HYPIXEL", "HYPIXEL_NO_NOSLOW", "HYPIXEL_CUSTOM", "HYPIXEL_LONG_BLINK", "HYPIXEL_LAG", "LEGIT", "FAKE"}
        );
        this.noStop = new BooleanProperty("不切换", true, () -> this.autoBlock.getValue() == 2);
        this.test = new BooleanProperty("更多攻击", false, () -> this.autoBlock.getValue() == 2);
        this.moreAttackDelay = new IntProperty("更多攻击延迟", 1, 0, 3, () -> this.autoBlock.getValue() == 2 && test.getValue());
        this.maxTick = new IntProperty("最大Tick", 3, 1, 5, () -> this.autoBlock.getValue() == 4);
        this.startBlinkTick = new IntProperty("开始BlinkTick", 0, 1, 5, () -> this.autoBlock.getValue() == 4);
        this.stopBlinkTick = new IntProperty("停止BlinkTick", 2, 1, 5, () -> this.autoBlock.getValue() == 4);
        this.swapTick = new IntProperty("切换Tick", 2, 1, 5, () -> this.autoBlock.getValue() == 4);
        this.switchBackTick = new IntProperty("切回Tick", 2, 1, 5, () -> this.autoBlock.getValue() == 4);
        this.stopBlockTick = new IntProperty("停止格挡Tick", 2, 1, 5, () -> this.autoBlock.getValue() == 4);
        this.attackTick = new IntProperty("攻击Tick", 0, 1, 5, () -> this.autoBlock.getValue() == 4);
        this.startBlockTick = new IntProperty("开始格挡Tick", 0, 1, 5, () -> this.autoBlock.getValue() == 4);
        this.postStartBlock = new BooleanProperty("后置格挡", false, () -> this.autoBlock.getValue() == 4);
        this.alwaysRenderBlocking = new BooleanProperty("总是渲染格挡", true, () -> this.autoBlock.getValue() == 6);
        this.c09Instead = new BooleanProperty("C09替代", true, () -> this.autoBlock.getValue() == 6);
        this.secondSword = new BooleanProperty("第二把剑", true, () -> this.autoBlock.getValue() == 6 && c09Instead.getValue());
        this.autoBlockRequirePress = new BooleanProperty("自动格挡需要按", false);
        this.autoBlockMinCPS = new FloatProperty("auto-block-min-aps", "最小格挡aps", 8.0F, 1.0F, 20.0F);
        this.autoBlockMaxCPS = new FloatProperty("auto-block-max-aps", "最大格挡aps", 10.0F, 1.0F, 20.0F);
        this.autoBlockRange = new FloatProperty("范围", 6.0F, 3.0F, 8.0F);
        this.swingRange = new FloatProperty("摆动范围", 3.5F, 3.0F, 6.0F);
        this.attackRange = new FloatProperty("攻击范围", 3.0F, 3.0F, 6.0F);
        this.fov = new IntProperty("视角", 360, 30, 360);
        this.minCPS = new IntProperty("min-aps", "最小攻击aps", 14, 1, 20);
        this.maxCPS = new IntProperty("max-aps", "最大攻击aps", 14, 1, 20);
        this.switchDelay = new IntProperty("切换延迟", 150, 0, 1000);
        this.rotations = new ModeProperty("旋转", 2, new String[]{"NONE", "LEGIT", "SILENT", "LOCK_VIEW"});
        this.moveFix = new ModeProperty("移动修复", 1, new String[]{"NONE", "SILENT", "STRICT"});
        this.rotationMode = new ModeProperty("旋转模式", 2, new String[]{"NORMAL", "NEAREST", "SMART"});
        this.smoothing = new PercentProperty("平滑度", 0);
        this.angleStep = new IntProperty("角度步长", 90, 30, 180);
        this.throughWalls = new BooleanProperty("穿墙", true);
        this.requirePress = new BooleanProperty("需要按", false);
        this.allowMining = new BooleanProperty("允许挖掘", true);
        this.allowPlayerBlocking = new BooleanProperty("允许玩家格挡", true);
        this.weaponsOnly = new BooleanProperty("仅武器", true);
        this.allowTools = new BooleanProperty("允许工具使用", false, this.weaponsOnly::getValue);
        this.inventoryCheck = new BooleanProperty("检查库存", true);
        this.lowTimerCheck = new BooleanProperty("低Timer检查", true);
        this.botCheck = new BooleanProperty("检查机器人", true);
        this.players = new BooleanProperty("玩家", true);
        this.bosses = new BooleanProperty("bosses", "Boss", false);
        this.mobs = new BooleanProperty("敌对生物", false);
        this.animals = new BooleanProperty("动物", false);
        this.golems = new BooleanProperty("铁傀儡", false);
        this.silverfish = new BooleanProperty("蠹虫", false);
        this.teams = new BooleanProperty("团队", true);
        this.showTarget = new ModeProperty("显示目标", 0, new String[]{"NONE", "DEFAULT", "HUD"});
        this.debugLog = new ModeProperty("调试日志", 0, new String[]{"NONE", "HEALTH"});
    }

    public EntityLivingBase getTarget() {
        return this.target != null ? this.target.getEntity() : null;
    }

    public boolean isAttackAllowed() {
        Scaffold scaffold = (Scaffold) Myau.moduleManager.modules.get(Scaffold.class);
        if (scaffold.isEnabled()) {
            return false;
        } else if (!this.weaponsOnly.getValue()
                || ItemUtil.hasRawUnbreakingEnchant()
                || this.allowTools.getValue() && ItemUtil.isHoldingTool()) {
            return !this.requirePress.getValue() || KeyBindUtil.isKeyDown(mc.gameSettings.keyBindAttack.getKeyCode());
        } else {
            return false;
        }
    }

    public boolean shouldAutoBlock() {
        if (this.autoBlock.getValue() <= 1 || this.autoBlock.getValue() == 8) {
            return this.hasValidTarget();
        }
        if (this.isPlayerBlocking() && this.isBlocking) {
            return !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava() && (this.autoBlock.getValue() == 2 || this.autoBlock.getValue() == 3 || this.autoBlock.getValue() == 4 || this.autoBlock.getValue() == 5 || this.autoBlock.getValue() == 6 || this.autoBlock.getValue() == 7);
        } else {
            return false;
        }
    }

    public boolean isBlocking() {
        return this.fakeBlockState && ItemUtil.isHoldingSword();
    }

    public boolean isPlayerBlocking() {
        return (mc.thePlayer.isUsingItem() || this.blockingState) && ItemUtil.isHoldingSword();
    }

    @EventTarget(Priority.LOW)
    public void onUpdate(UpdateEvent event) throws AWTException {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (this.attackDelayMS > 0L) {
                this.attackDelayMS -= 50L;
            }
            boolean attack = this.target != null && this.canAttack();
            boolean block = attack && this.canAutoBlock();
            if (!block) {
                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                if (autoBlock.getValue() == 2 && isBlocking && Myau.moduleManager.modules.get(NoSlow.class).isEnabled()) {
                    this.isBlocking = false;
                    stopBlock();
                }
                if (swapped) {
                    int handle = mc.thePlayer.inventory.currentItem;
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                    swapped = false;
                } else this.isBlocking = false;
                this.fakeBlockState = false;
                this.blockTick = 0;
            }
            if (attack) {
                boolean swap = false;
                boolean postBlink = false;
                boolean blocked = false;
                if (block) {
                    switch (this.autoBlock.getValue()) {
                        case 0:
                            if (PlayerUtil.isUsingItem()) {
                                this.isBlocking = true;
                                if (!this.isPlayerBlocking() && !Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    swap = true;
                                }
                            } else {
                                this.isBlocking = false;
                                if (this.isPlayerBlocking() && !Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    this.stopBlock();
                                }
                            }
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.fakeBlockState = false;
                            break;
                        case 1:
                            if (this.hasValidTarget()) {
                                if (!this.isPlayerBlocking() && !Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    swap = true;
                                }
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = true;
                                this.fakeBlockState = false;
                            } else {
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = false;
                                this.fakeBlockState = false;
                            }
                            break;
                        case 2:
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0:
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            blocked = true;
                                            this.blockTick = 1;
                                            break;
                                        case 1:
                                            attack = false;
                                            this.blockTick = 2;
                                            break;
                                        case 2:
                                            if (this.isPlayerBlocking()) {
                                                if (!noStop.getValue()) {
                                                    int handle = mc.thePlayer.inventory.currentItem;
                                                    int altSlot = this.findEmptySlot(handle);
                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(altSlot));
                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                }
                                                this.stopBlock();
                                            }
                                            if (test.getValue()) {
                                                if (testAttackTick >= moreAttackDelay.getValue()) {
                                                    testAttackTick = 0;
                                                } else {
                                                    testAttackTick++;
                                                    attack = false;
                                                }
                                            } else {
                                                attack = false;
                                            }
                                            this.blockTick = 0;
                                            break;
                                        default:
                                            this.blockTick = 0;
                                            break;
                                    }
                                }
                                this.isBlocking = true;
                                this.fakeBlockState = true;
                            } else {
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = false;
                                this.fakeBlockState = false;
                                int swapSlot = this.findEmptySlot(mc.thePlayer.inventory.currentItem);
                                PacketUtil.sendPacket(new C09PacketHeldItemChange(swapSlot));
                                PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                Velocity.extraAttacked = false;
                            }
                            break;
                        case 3:
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0:
                                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            this.blockTick = 1;
                                            break;
                                        case 1:
                                            attack = false;
                                            blockTick = 2;
                                            break;
                                        case 2:
                                            Myau.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                                            if (this.isPlayerBlocking()) {
                                                this.stopBlock();
                                            }
                                            if (test.getValue()) {
                                                if (testAttackTick >= moreAttackDelay.getValue()) {
                                                    testAttackTick = 0;
                                                } else {
                                                    testAttackTick++;
                                                    attack = false;
                                                }
                                            }
                                            this.blockTick = 0;
                                            break;
                                        default:
                                            this.blockTick = 0;
                                    }
                                }
                                this.isBlocking = true;
                                this.fakeBlockState = true;
                            } else {
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = false;
                                this.fakeBlockState = false;
                                Velocity.extraAttacked = false;
                            }
                            break;
                        case 4:
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    if (blockTick + 1 == startBlinkTick.getValue()) {
                                        blocked = true;
                                    }
                                    if (blockTick + 1 != attackTick.getValue()) {
                                        attack = false;
                                    }
                                    if (blockTick + 1 == startBlockTick.getValue()) {
                                        if (!this.isPlayerBlocking()) {
                                            swap = true;
                                            if (postStartBlock.getValue()) postBlock = true;
                                        }
                                    }
                                    if (blockTick + 1 == stopBlinkTick.getValue()) {
                                        Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                    }
                                    if (blockTick + 1 == swapTick.getValue()) {
                                        int swapSlot = this.findEmptySlot(mc.thePlayer.inventory.currentItem);
                                        PacketUtil.sendPacket(new C09PacketHeldItemChange(swapSlot));
                                        swapped = true;
                                    }
                                    if (blockTick + 1 == switchBackTick.getValue()) {
                                        if (swapped) {
                                            PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                            swapped = false;
                                        }
                                    }
                                    if (blockTick + 1 == stopBlockTick.getValue()) {
                                        if (this.isPlayerBlocking()) {
                                            this.stopBlock();
                                        }
                                    }
                                    blockTick++;
                                    if (blockTick >= maxTick.getValue() - 1) {
                                        blockTick = 0;
                                    }
                                }
                                this.isBlocking = true;
                                this.fakeBlockState = true;
                            } else {
                                if (swapped) {
                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                    swapped = false;
                                }
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = false;
                                this.fakeBlockState = false;
                                Velocity.extraAttacked = false;
                            }
                            break;
                        case 5:
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0:
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            blocked = true;
                                            this.blockTick = 1;
                                            break;
                                        case 1:
                                            attack = false;
                                            blockTick = 2;
                                            break;
                                        case 2:
                                            if (this.isPlayerBlocking()) {
                                                int handle = mc.thePlayer.inventory.currentItem;
                                                int altSlot = this.findEmptySlot(handle);
                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(altSlot));
                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                this.stopBlock();
                                                blockTick = 3;
                                            }
                                            attack = false;
                                            break;
                                        case 3:
                                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                            blockTick = 0;
                                            break;
                                        default:
                                            this.blockTick = 0;
                                    }
                                }
                                this.isBlocking = true;
                                this.fakeBlockState = false;
                            } else {
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = false;
                                this.fakeBlockState = false;
                                Velocity.extraAttacked = false;
                            }
                            break;
                        case 6:
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0:
                                            blocked = true;
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            this.blockTick = 1;
                                            break;
                                        case 1:
                                            if (this.isPlayerBlocking()) {
                                                if (c09Instead.getValue()) {
                                                    int handle = mc.thePlayer.inventory.currentItem;
                                                    int altSlot = this.findEmptySlot(handle);
                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(altSlot));
                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                } else this.stopBlock();
                                            }
                                            attack = false;
                                            blockTick = 2;
                                            break;
                                        case 2:
                                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                            if (this.attackDelayMS <= 50L) {
                                                this.blockTick = 0;
                                            }
                                            break;
                                        default:
                                            this.blockTick = 0;
                                    }
                                }
                                this.isBlocking = true;
                                this.fakeBlockState = alwaysRenderBlocking.getValue();
                            } else {
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = false;
                                this.fakeBlockState = false;
                                Velocity.extraAttacked = false;
                            }
                            break;
                        case 7:
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0:
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            this.blockTick = 1;
                                            break;
                                        case 1:
                                            if (this.isPlayerBlocking()) {
                                                this.stopBlock();
                                                attack = false;
                                            }
                                            if (this.attackDelayMS <= 50L) {
                                                this.blockTick = 0;
                                            }
                                            break;
                                        default:
                                            this.blockTick = 0;
                                    }
                                }
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = true;
                                this.fakeBlockState = false;
                            } else {
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = false;
                                this.fakeBlockState = false;
                                Velocity.extraAttacked = false;
                            }
                            break;
                        case 8:
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = this.hasValidTarget();
                            if (PlayerUtil.isUsingItem()
                                    && !this.isPlayerBlocking()
                                    && !Myau.playerStateManager.digging
                                    && !Myau.playerStateManager.placing) {
                                swap = true;
                            }
                            break;
                    }
                }
                boolean attacked = false;
                if (this.isBoxInSwingRange(this.target.getBox())) {
                    if (this.rotations.getValue() == 2 || this.rotations.getValue() == 3) {
                        AxisAlignedBB box = this.target.getBox();
                        float currentYaw = event.getYaw();
                        float currentPitch = event.getPitch();
                        float angleStep = (float) this.angleStep.getValue() + RandomUtil.nextFloat(-5.0F, 5.0F);
                        float smooth = (float) this.smoothing.getValue() / 100.0F;
                        float[] rotations;
                        int mode = this.rotationMode.getValue();
                        if (mode == 1) {
                            rotations = RotationUtil.nearestRotation(box, currentYaw, currentPitch, angleStep, smooth);
                        } else if (mode == 2) {
                            if (this.isNormalTargetVisible(box)) {
                                rotations = RotationUtil.getRotationsToBox(box, currentYaw, currentPitch, angleStep, smooth);
                            } else {
                                rotations = RotationUtil.nearestRotation(box, currentYaw, currentPitch, angleStep, smooth);
                            }
                        } else {
                            rotations = RotationUtil.getRotationsToBox(box, currentYaw, currentPitch, angleStep, smooth);
                        }
                        if (rotations != null) {
                            event.setRotation(rotations[0], rotations[1], 1);
                        }
                        if (this.rotations.getValue() == 3) {
                            if (rotations != null) {
                                Myau.rotationManager.setRotation(rotations[0], rotations[1], 1, true);
                            }
                        }
                        if (this.moveFix.getValue() != 0 || this.rotations.getValue() == 3) {
                            if (rotations != null) {
                                event.setPervRotation(rotations[0], 1);
                            }
                        }
                    }
                    if (attack) {
                        attacked = this.performAttack(event.getNewYaw(), event.getNewPitch());
                    }
                }
                if (postBlink) {
                    Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                }
                if (swap) {
                    if (attacked) {
                        this.interactAttack(event.getNewYaw(), event.getNewPitch());
                    } else {
                        if (!postBlock) this.sendUseItem();
                    }
                }
                if (blocked) {
                    Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                    Myau.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                }
            }
        }
        if (event.getType() == EventType.POST && this.isEnabled()) {
            if (postSwap) {
                int swapSlot = this.findEmptySlot(mc.thePlayer.inventory.currentItem);
                PacketUtil.sendPacket(new C09PacketHeldItemChange(swapSlot));
                PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                this.stopBlock();
                postSwap = false;
            }
            if (postBlock) {
                sendUseItem();
                postBlock = false;
            }
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled()) {
            switch (event.getType()) {
                case PRE:
                    if (this.target == null
                            || !this.isValidTarget(this.target.getEntity())
                            || !this.isBoxInAttackRange(this.target.getBox())
                            || !this.isBoxInSwingRange(this.target.getBox())
                            || this.timer.hasTimeElapsed(this.switchDelay.getValue().longValue())) {
                        this.timer.reset();
                        ArrayList<EntityLivingBase> targets = new ArrayList<>();
                        for (Entity entity : mc.theWorld.loadedEntityList) {
                            if (entity instanceof EntityLivingBase
                                    && this.isValidTarget((EntityLivingBase) entity)
                                    && this.isInRange((EntityLivingBase) entity)) {
                                targets.add((EntityLivingBase) entity);
                            }
                        }
                        if (targets.isEmpty()) {
                            this.target = null;
                        } else {
                            if (targets.stream().anyMatch(this::isInSwingRange)) {
                                targets.removeIf(entityLivingBase -> !this.isInSwingRange(entityLivingBase));
                            }
                            if (targets.stream().anyMatch(this::isInAttackRange)) {
                                targets.removeIf(entityLivingBase -> !this.isInAttackRange(entityLivingBase));
                            }
                            if (targets.stream().anyMatch(this::isPlayerTarget)) {
                                targets.removeIf(entityLivingBase -> !this.isPlayerTarget(entityLivingBase));
                            }
                            targets.sort(
                                    (entityLivingBase1, entityLivingBase2) -> {
                                        int sortBase = 0;
                                        switch (this.sort.getValue()) {
                                            case 1:
                                                sortBase = Float.compare(TeamUtil.getHealthScore(entityLivingBase1), TeamUtil.getHealthScore(entityLivingBase2));
                                                break;
                                            case 2:
                                                sortBase = Integer.compare(entityLivingBase1.hurtResistantTime, entityLivingBase2.hurtResistantTime);
                                                break;
                                            case 3:
                                                sortBase = Float.compare(
                                                        RotationUtil.angleToEntity(entityLivingBase1),
                                                        RotationUtil.angleToEntity(entityLivingBase2)
                                                );
                                        }
                                        return sortBase != 0
                                                ? sortBase
                                                : Double.compare(RotationUtil.distanceToEntity(entityLivingBase1), RotationUtil.distanceToEntity(entityLivingBase2));
                                    }
                            );
                            if (this.mode.getValue() == 1 && this.hitRegistered) {
                                this.hitRegistered = false;
                                this.switchTick++;
                            }
                            if (this.mode.getValue() == 0 || this.switchTick >= targets.size()) {
                                this.switchTick = 0;
                            }
                            this.target = new AttackData(targets.get(this.switchTick));
                        }
                    }
                    if (this.target != null) {
                        this.target = new AttackData(this.target.getEntity());
                    }
                    break;
                case POST:
                    if (this.isPlayerBlocking() && !mc.thePlayer.isBlocking()) {
                        mc.thePlayer.setItemInUse(mc.thePlayer.getHeldItem(), mc.thePlayer.getHeldItem().getMaxItemUseDuration());
                    }
                default:
                    break;
            }
        }
    }

    @EventTarget(Priority.LOWEST)
    public void onPacket(PacketEvent event) {
        if (this.isEnabled() && !event.isCancelled() && mc.thePlayer != null && mc.theWorld != null) {
            if (event.getPacket() instanceof C07PacketPlayerDigging) {
                C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.getPacket();
                if (packet.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                    this.blockingState = false;
                }
            }
            if (event.getPacket() instanceof C09PacketHeldItemChange) {
                this.blockingState = false;
                if (this.isBlocking) {
                    mc.thePlayer.stopUsingItem();
                }
            }
            if (this.debugLog.getValue() == 1 && this.isAttackAllowed()) {
                if (event.getPacket() instanceof S06PacketUpdateHealth) {
                    float packet = ((S06PacketUpdateHealth) event.getPacket()).getHealth() - mc.thePlayer.getHealth();
                    if (packet != 0.0F && this.lastTickProcessed != mc.thePlayer.ticksExisted) {
                        this.lastTickProcessed = mc.thePlayer.ticksExisted;
                        ChatUtil.sendFormatted(
                                String.format(
                                        "%sHealth: %s&l%s&r (&otick: %d&r)&r",
                                        Myau.clientName,
                                        packet > 0.0F ? "&a" : "&c",
                                        df.format(packet),
                                        mc.thePlayer.ticksExisted
                                )
                        );
                    }
                }
                if (event.getPacket() instanceof S1CPacketEntityMetadata) {
                    S1CPacketEntityMetadata packet = (S1CPacketEntityMetadata) event.getPacket();
                    if (packet.getEntityId() == mc.thePlayer.getEntityId()) {
                        for (WatchableObject watchableObject : packet.func_149376_c()) {
                            if (watchableObject.getDataValueId() == 6) {
                                float diff = (Float) watchableObject.getObject() - mc.thePlayer.getHealth();
                                if (diff != 0.0F && this.lastTickProcessed != mc.thePlayer.ticksExisted) {
                                    this.lastTickProcessed = mc.thePlayer.ticksExisted;
                                    ChatUtil.sendFormatted(
                                            String.format(
                                                    "%sHealth: %s&l%s&r (&otick: %d&r)&r",
                                                    Myau.clientName,
                                                    diff > 0.0F ? "&a" : "&c",
                                                    df.format(diff),
                                                    mc.thePlayer.ticksExisted
                                            )
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onMove(MoveInputEvent event) {
        if (this.isEnabled()) {
            if (this.moveFix.getValue() == 1
                    && this.rotations.getValue() != 3
                    && RotationState.isActived()
                    && RotationState.getPriority() == 1.0F
                    && MoveUtil.isForwardPressed()) {
                MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
            }
            if (this.shouldAutoBlock()) {
                mc.thePlayer.movementInput.jump = false;
            }
        }
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        if (this.isEnabled() && target != null) {
            if (this.showTarget.getValue() != 0
                    && TeamUtil.isEntityLoaded(this.target.getEntity())
                    && this.isAttackAllowed()) {
                Color color = new Color(-1);
                switch (this.showTarget.getValue()) {
                    case 1:
                        if (this.target.getEntity().hurtTime > 0) {
                            color = new Color(16733525);
                        } else {
                            color = new Color(5635925);
                        }
                        break;
                    case 2:
                        color = ((HUD) Myau.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis());
                }
                RenderUtil.enableRenderState();
                RenderUtil.drawEntityBox(this.target.getEntity(), color.getRed(), color.getGreen(), color.getBlue());
                RenderUtil.disableRenderState();
            }
        }
    }

    @EventTarget
    public void onLeftClick(LeftClickMouseEvent event) {
        if (this.isBlocking) {
            event.setCancelled(true);
        } else {
            if (this.isEnabled() && this.target != null && this.canAttack()) {
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (this.isBlocking) {
            event.setCancelled(true);
        } else {
            if (this.isEnabled() && this.target != null && this.canAttack() && !allowPlayerBlocking.getValue()) {
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onHitBlock(HitBlockEvent event) {
        if (this.isBlocking) {
            event.setCancelled(true);
        } else {
            if (this.isEnabled() && this.target != null && this.canAttack()) {
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onCancelUse(CancelUseEvent event) {
        if (this.isBlocking) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onEnabled() {
        this.target = null;
        this.switchTick = 0;
        this.hitRegistered = false;
        this.attackDelayMS = 0L;
        this.blockTick = 0;
        this.swapped = false;
        this.postBlock = false;
        this.postSwap = false;
        this.testAttackTick = 0;
    }

    @Override
    public void onDisabled() {
        Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
        Velocity.extraAttacked = false;
        this.blockingState = false;
        this.fakeBlockState = false;
        if (swapped) {
            int handle = mc.thePlayer.inventory.currentItem;
            PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
            swapped = false;
        }
        if (autoBlock.getValue() == 2 && isBlocking && Myau.moduleManager.modules.get(NoSlow.class).isEnabled()) {
            this.isBlocking = false;
            stopBlock();
        } else this.isBlocking = false;
    }

    @Override
    public void verifyValue(String value) {
        boolean badCps = this.autoBlock.getValue() == 2
                || this.autoBlock.getValue() == 3
                || this.autoBlock.getValue() == 4
                || this.autoBlock.getValue() == 5
                || this.autoBlock.getValue() == 6
                || this.autoBlock.getValue() == 7;
        if (!this.autoBlock.getName().equals(value)) {
            if (this.swingRange.getName().equals(value)) {
                if (this.swingRange.getValue() < this.attackRange.getValue()) {
                    this.attackRange.setValue(this.swingRange.getValue());
                }
            } else if (this.attackRange.getName().equals(value)) {
                if (this.swingRange.getValue() < this.attackRange.getValue()) {
                    this.swingRange.setValue(this.attackRange.getValue());
                }
            } else if (this.minCPS.getName().equals(value)) {
                if (this.minCPS.getValue() > this.maxCPS.getValue()) {
                    this.maxCPS.setValue(this.minCPS.getValue());
                }
            } else if (this.autoBlockMinCPS.getName().equals(value)) {
                if (this.autoBlockMinCPS.getValue() > this.autoBlockMaxCPS.getValue()) {
                    this.autoBlockMaxCPS.setValue(this.autoBlockMinCPS.getValue());
                }
                if(autoBlockMinCPS.getValue() > 10.0F && badCps){
                    autoBlockMinCPS.setValue(10.0F);
                }
            } else if (this.autoBlockMaxCPS.getName().equals(value)) {
                if (this.autoBlockMinCPS.getValue() > this.autoBlockMaxCPS.getValue()) {
                    this.autoBlockMinCPS.setValue(this.autoBlockMaxCPS.getValue());
                }
                if(autoBlockMaxCPS.getValue() > 10.0F && badCps){
                    autoBlockMaxCPS.setValue(10.0F);
                }
            } else {
                if (this.maxCPS.getName().equals(value) && this.minCPS.getValue() > this.maxCPS.getValue()) {
                    this.minCPS.setValue(this.maxCPS.getValue());
                }
            }
        } else {
            if (badCps && (this.autoBlockMinCPS.getValue() > 10.0F || this.autoBlockMaxCPS.getValue() > 10.0F)) {
                this.autoBlockMinCPS.setValue(8.0F);
                this.autoBlockMaxCPS.setValue(10.0F);
            }
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }

    public static class AttackData {
        private final EntityLivingBase entity;
        private final AxisAlignedBB box;
        private final double x;
        private final double y;
        private final double z;

        public AttackData(EntityLivingBase entityLivingBase) {
            this.entity = entityLivingBase;
            double collisionBorderSize = entityLivingBase.getCollisionBorderSize();
            this.box = entityLivingBase.getEntityBoundingBox().expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
            this.x = entityLivingBase.posX;
            this.y = entityLivingBase.posY;
            this.z = entityLivingBase.posZ;
        }

        public EntityLivingBase getEntity() {
            return this.entity;
        }

        public AxisAlignedBB getBox() {
            return this.box;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }
    }
}
