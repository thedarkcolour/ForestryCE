package forestry.core.data;

import forestry.core.fluids.ForestryFluids;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import thedarkcolour.modkit.data.MKTagsProvider;

public class ForestryFluidTagsProvider {
	public static void addTags(MKTagsProvider<Fluid> tags, HolderLookup.Provider lookup) {
		tags.tag(Tags.Fluids.HONEY).add(ForestryFluids.HONEY.getFluid(), ForestryFluids.HONEY.getFlowing());
	}
}
