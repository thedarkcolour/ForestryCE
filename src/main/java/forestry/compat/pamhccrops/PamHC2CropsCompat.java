package forestry.compat.pamhccrops;

import forestry.api.plugin.IFarmTypeBuilder;
import forestry.farming.logic.farmables.FarmableAgingCrop;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.block.CropBlock;

public class PamHC2CropsCompat {

	public static void registerPamCrops(IFarmTypeBuilder crops) {
		if (!ModList.get().isLoaded("pamhc2crops")) {
			return;
		}

		for (ResourceLocation id : ForgeRegistries.BLOCKS.getKeys()) {  // testing automated gathering of registerys
			if (!id.getNamespace().equals("pamhc2crops")) continue;

			String path = id.getPath(); // like "pamcorncrop"
			if (!path.startsWith("pam") || !path.endsWith("crop")) continue;

			// extract crop name: pam + corn + crop → corn
			String cropName = path.substring(3, path.length() - 4); // between "pam" and "crop"
			String CropItemID = cropName + "item";
			String SeedItemID = cropName + "seed" + "item";
			//example "agaveseeditem" 
			//example "agaveitem"
			addPamCrop(crops, CropItemID, path, SeedItemID);
		}
	}

	private static void addPamCrop(IFarmTypeBuilder crops, String CropItemID, String blockId, String SeedItemID) {
		Item cropitem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("pamhc2crops", CropItemID));  // Pams crops can be planted directly or converted to seeds before planting so both are needed
		Item seeditem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("pamhc2crops", SeedItemID));
		Block cropBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("pamhc2crops", blockId));
		if (cropitem != null && cropBlock instanceof CropBlock) {
			//crops.addFarmable(new FarmableAgingCrop(Items.CARROT, Blocks.CARROTS, new ItemStack(Items.CARROT), CropBlock.AGE, 7, 0));
			//crops.addFarmable(new FarmableAgingCrop(Items.BEETROOT_SEEDS, Blocks.BEETROOTS, new ItemStack(Items.BEETROOT), BeetrootBlock.AGE, 3, 0));
			crops.addFarmable(new FarmableAgingCrop(cropitem, cropBlock, new ItemStack(cropitem), CropBlock.AGE, 7, 0)); // check that minharvestage of pam's crops is 7
			crops.addFarmable(new FarmableAgingCrop(seeditem, cropBlock, new ItemStack(cropitem), CropBlock.AGE, 7, 0));
		}
	}
}
