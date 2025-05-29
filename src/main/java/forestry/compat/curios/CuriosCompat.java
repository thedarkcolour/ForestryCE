package forestry.compat.curios;

import forestry.core.utils.GeneticsUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.EntityCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

public class CuriosCompat {
	public static final boolean IS_LOADED = ModList.get().isLoaded("curios");

	public static final EntityCapability<ICuriosItemHandler, Void> CURIOS_INVENTORY = EntityCapability.createVoid(ResourceLocation.fromNamespaceAndPath("curios", "item_handler"), ICuriosItemHandler.class);

	public static boolean hasNaturalistEye(Player player) {
		ICuriosItemHandler inventory = player.getCapability(CURIOS_INVENTORY);

		if (inventory != null) {
			Optional<ICurioStacksHandler> head = inventory.getStacksHandler("head");

			if (head.isPresent()) {
				IDynamicStackHandler stacks = head.get().getStacks();

				for (int i = 0; i < stacks.getSlots(); i++) {
					if (GeneticsUtil.hasNaturalistEye(player, stacks.getStackInSlot(i))) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
