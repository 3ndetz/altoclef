package adris.altoclef.tasks.multiplayer.minigames;

import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Stub — full implementation in Phase 3.
 */
public class MurderMysteryTask {

    public static boolean hasKillerWeapon(PlayerEntity entity) {
        for (net.minecraft.item.Item weapon : ItemHelper.MMKillerWeapons) {
            if (entity.getMainHandStack().isOf(weapon)) return true;
        }
        return false;
    }
}
