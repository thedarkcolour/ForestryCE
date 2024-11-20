package forestry.core.data;

import java.util.Optional;

import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;

import forestry.api.ForestryConstants;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.arboriculture.ILeafSprite;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.RipeningFruit;

public class ForestryAtlasProvider extends SpriteSourceProvider {
	public ForestryAtlasProvider(PackOutput output, ExistingFileHelper fileHelper) {
		super(output, fileHelper, ForestryConstants.MOD_ID);
	}

	@Override
	protected void addSources() {
		SourceList block = atlas(InventoryMenu.BLOCK_ATLAS);

		for (ILeafSprite sprite : IForestryClientApi.INSTANCE.getTreeManager().getAllLeafSprites()) {
			add(block, sprite.get(true, true));
			add(block, sprite.get(true, false));
			add(block, sprite.get(false, true));
			add(block, sprite.get(false, false));
		}

		for (IFruit fruit : TreeChromosomes.FRUIT.values()) {
			if (fruit instanceof RipeningFruit ripening && ripening.getDecorativeSprite() != null) {
				add(block, ripening.getDecorativeSprite());
			}
		}

		// todo do we need these?
		//add(block, ForestryConstants.forestry("block/farm/plain"));
		//add(block, ForestryConstants.forestry("block/farm/reverse"));
		//add(block, ForestryConstants.forestry("block/farm/top"));
		//add(block, ForestryConstants.forestry("block/farm/band"));
		//add(block, ForestryConstants.forestry("block/farm/gears"));
		//add(block, ForestryConstants.forestry("block/farm/hatch"));
		//add(block, ForestryConstants.forestry("block/farm/valve"));
		//add(block, ForestryConstants.forestry("block/farm/control"));
	}

	private static void add(SourceList sources, ResourceLocation sprite) {
		sources.addSource(new SingleFile(sprite, Optional.empty()));
	}
}
