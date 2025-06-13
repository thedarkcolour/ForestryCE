package forestry.core.inventory;

import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.tiles.IFilterSlotDelegate;
import net.minecraft.world.WorldlyContainer;

public interface IInventoryAdapter extends WorldlyContainer, IFilterSlotDelegate, INbtWritable, INbtReadable {
}
