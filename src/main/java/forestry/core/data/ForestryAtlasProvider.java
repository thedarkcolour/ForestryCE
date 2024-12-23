package forestry.core.data;

import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.data.PackOutput;

import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;

import forestry.api.ForestryConstants;
import forestry.core.render.ForestrySpriteUploader;

public class ForestryAtlasProvider extends SpriteSourceProvider {
	public ForestryAtlasProvider(PackOutput output, ExistingFileHelper fileHelper) {
		super(output, fileHelper, ForestryConstants.MOD_ID);
	}

	@Override
	protected void addSources() {
		SourceList icons = atlas(ForestrySpriteUploader.ATLAS_PATH);

		icons.addSource(new DirectoryLister("forestry/atlas/gui", ""));

		// A bit redundant but fixes the issue
		SourceList blocks = atlas(BLOCKS_ATLAS);

		blocks.addSource(new DirectoryLister("forestry/atlas/gui/slots", "slots/"));
	}
}
