package forestry.compat.pamhccrops;

import forestry.api.plugin.IFarmTypeBuilder;
import forestry.farming.logic.farmables.FarmableAgingCrop;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.block.CropBlock;

public class PamHC2CropsCompat {

	public static void registerPamCrops(IFarmTypeBuilder crops) {
		if (!ModList.get().isLoaded("pamhc2crops")) {
			return;
		}

		addPamCrop(crops, "cornitem", "pamcorncrop");
		// Add more crops here
	}

	private static void addPamCrop(IFarmTypeBuilder crops, String itemId, String blockId) {
		Item seedOrProduce = ForgeRegistries.ITEMS.getValue(new ResourceLocation("pamhc2crops", itemId));
		Block cropBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("pamhc2crops", blockId));

		if (seedOrProduce != null && cropBlock instanceof CropBlock) {
			crops.addFarmable(new FarmableAgingCrop(seedOrProduce, cropBlock, new ItemStack(seedOrProduce), CropBlock.AGE, 7, 0));
		}
	}
}
