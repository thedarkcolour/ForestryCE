package forestry.farming.logic;

import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class FarmLogicMushroom extends FarmLogicArboreal {
	public FarmLogicMushroom(IFarmType properties, boolean isManual) {
		super(properties, isManual);
	}

	@Override
	public List<ItemStack> collect(Level level, IFarmHousing farmHousing) {
		return List.of();//Needed to override Arboreal #collect
	}
}
