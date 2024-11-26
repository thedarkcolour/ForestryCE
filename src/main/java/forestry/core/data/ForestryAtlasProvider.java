package forestry.core.data;

import java.util.Optional;

import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

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
	}

	private static void add(SourceList sources, ResourceLocation sprite) {
		sources.addSource(new SingleFile(sprite, Optional.empty()));
	}
}
