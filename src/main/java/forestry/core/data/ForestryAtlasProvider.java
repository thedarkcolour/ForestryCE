package forestry.core.data;

import forestry.api.ForestryConstants;
import forestry.core.render.ForestrySpriteUploader;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

import java.util.concurrent.CompletableFuture;

public class ForestryAtlasProvider extends SpriteSourceProvider {
	public ForestryAtlasProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
		super(output, registries, ForestryConstants.MOD_ID, fileHelper);
	}

	@Override
	protected void gather() {
		SourceList icons = atlas(ForestrySpriteUploader.ATLAS_PATH);

		icons.addSource(new DirectoryLister("forestry/atlas/gui", ""));

		// A bit redundant but fixes the issue
		SourceList blocks = atlas(BLOCKS_ATLAS);

		blocks.addSource(new DirectoryLister("forestry/atlas/gui/slots", "slots/"));
	}
}
