package forestry.core.data;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.apiculture.IBeeClientManager;
import forestry.api.client.arboriculture.ILeafSprite;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.RipeningFruit;
import forestry.core.utils.SpeciesUtil;

public class ForestryAtlasProvider extends SpriteSourceProvider {
	public ForestryAtlasProvider(PackOutput output, ExistingFileHelper fileHelper) {
		super(output, fileHelper, ForestryConstants.MOD_ID);
	}

	@Override
	protected void addSources() {
		SourceList blocks = atlas(InventoryMenu.BLOCK_ATLAS);

		// Must be sorted to avoid continuously reordering the file across data gen runs
		IForestryClientApi.INSTANCE.getTreeManager().getAllLeafSprites().stream().sorted(Comparator.comparing(ILeafSprite::getParticle)).forEachOrdered(sprite -> {
			add(blocks, sprite.get(true, true));
			add(blocks, sprite.get(true, false));
			add(blocks, sprite.get(false, true));
			add(blocks, sprite.get(false, false));
		});

		for (IFruit fruit : TreeChromosomes.FRUIT.values()) {
			if (fruit instanceof RipeningFruit ripening && ripening.getDecorativeSprite() != null) {
				add(blocks, ripening.getDecorativeSprite());
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
