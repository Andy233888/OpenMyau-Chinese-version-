package myau.module.modules;

import myau.module.Module;
import myau.util.ItemUtil;
import myau.util.TeamUtil;
import myau.property.properties.BooleanProperty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class GhostHand extends Module {
    public final BooleanProperty teamsOnly = new BooleanProperty("team-only", "仅在团队", true);
    public final BooleanProperty ignoreWeapons = new BooleanProperty("忽略武器", false);

    public GhostHand() {
        super("隔空拿箱子", false);
    }

    public boolean shouldSkip(Entity entity) {
        return entity instanceof EntityPlayer
                && !TeamUtil.isBot((EntityPlayer) entity)
                && (!this.teamsOnly.getValue() || TeamUtil.isSameTeam((EntityPlayer) entity))
                && (!this.ignoreWeapons.getValue() || !ItemUtil.hasRawUnbreakingEnchant());
    }
}
