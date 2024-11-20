package forestry.core.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.material.Fluid;

import forestry.api.ForestryTags;
import forestry.core.fluids.ForestryFluids;

import thedarkcolour.modkit.data.MKTagsProvider;

public class ForestryFluidTagsProvider {
	public static void addTags(MKTagsProvider<Fluid> tags, HolderLookup.Provider lookup) {
		tags.tag(ForestryTags.Fluids.HONEY).add(ForestryFluids.HONEY.getFluid(), ForestryFluids.HONEY.getFlowing());
	}
}
