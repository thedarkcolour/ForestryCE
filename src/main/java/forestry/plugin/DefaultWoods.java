package forestry.plugin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import forestry.api.plugin.IArboricultureRegistration;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.VanillaWoodType;
import forestry.arboriculture.features.ArboricultureBlocks;

class DefaultWoods {
	static void register(IArboricultureRegistration arboriculture) {
		for (ForestryWoodType woodType : ForestryWoodType.VALUES) {
			arboriculture.registerRefractoryWaxable(ArboricultureBlocks.LOGS.get(woodType).block(), ArboricultureBlocks.LOGS_FIREPROOF.get(woodType).block());
			arboriculture.registerRefractoryWaxable(ArboricultureBlocks.STRIPPED_LOGS.get(woodType).block(), ArboricultureBlocks.STRIPPED_LOGS_FIREPROOF.get(woodType).block());
			arboriculture.registerRefractoryWaxable(ArboricultureBlocks.WOOD.get(woodType).block(), ArboricultureBlocks.WOOD_FIREPROOF.get(woodType).block());
			arboriculture.registerRefractoryWaxable(ArboricultureBlocks.STRIPPED_WOOD.get(woodType).block(), ArboricultureBlocks.STRIPPED_WOOD_FIREPROOF.get(woodType).block());
			arboriculture.registerRefractoryWaxable(ArboricultureBlocks.PLANKS.get(woodType).block(), ArboricultureBlocks.PLANKS_FIREPROOF.get(woodType).block());
			arboriculture.registerRefractoryWaxable(ArboricultureBlocks.SLABS.get(woodType).block(), ArboricultureBlocks.SLABS_FIREPROOF.get(woodType).block());
			arboriculture.registerRefractoryWaxable(ArboricultureBlocks.FENCES.get(woodType).block(), ArboricultureBlocks.FENCES_FIREPROOF.get(woodType).block());
			arboriculture.registerRefractoryWaxable(ArboricultureBlocks.FENCE_GATES.get(woodType).block(), ArboricultureBlocks.FENCE_GATES_FIREPROOF.get(woodType).block());
			arboriculture.registerRefractoryWaxable(ArboricultureBlocks.STAIRS.get(woodType).block(), ArboricultureBlocks.STAIRS_FIREPROOF.get(woodType).block());
		}

		registerVanillaRefractory(arboriculture, VanillaWoodType.OAK, Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG, Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD, Blocks.OAK_PLANKS, Blocks.OAK_SLAB, Blocks.OAK_FENCE, Blocks.OAK_FENCE_GATE, Blocks.OAK_STAIRS);
		registerVanillaRefractory(arboriculture, VanillaWoodType.SPRUCE, Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_FENCE_GATE, Blocks.SPRUCE_STAIRS);
		registerVanillaRefractory(arboriculture, VanillaWoodType.BIRCH, Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD, Blocks.BIRCH_PLANKS, Blocks.BIRCH_SLAB, Blocks.BIRCH_FENCE, Blocks.BIRCH_FENCE_GATE, Blocks.BIRCH_STAIRS);
		registerVanillaRefractory(arboriculture, VanillaWoodType.JUNGLE, Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_SLAB, Blocks.JUNGLE_FENCE, Blocks.JUNGLE_FENCE_GATE, Blocks.JUNGLE_STAIRS);
		registerVanillaRefractory(arboriculture, VanillaWoodType.ACACIA, Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD, Blocks.ACACIA_PLANKS, Blocks.ACACIA_SLAB, Blocks.ACACIA_FENCE, Blocks.ACACIA_FENCE_GATE, Blocks.ACACIA_STAIRS);
		registerVanillaRefractory(arboriculture, VanillaWoodType.DARK_OAK, Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_FENCE_GATE, Blocks.DARK_OAK_STAIRS);
		// todo mangrove
		registerVanillaRefractory(arboriculture, VanillaWoodType.CHERRY, Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD, Blocks.CHERRY_PLANKS, Blocks.CHERRY_SLAB, Blocks.CHERRY_FENCE, Blocks.CHERRY_FENCE_GATE, Blocks.CHERRY_STAIRS);
	}

	private static void registerVanillaRefractory(IArboricultureRegistration arboriculture, VanillaWoodType type, Block log, Block strippedLog, Block wood, Block strippedWood, Block planks, Block slab, Block fence, Block fenceGate, Block stairs) {
		arboriculture.registerRefractoryWaxable(log, ArboricultureBlocks.LOGS_VANILLA_FIREPROOF.get(type).block());
		arboriculture.registerRefractoryWaxable(strippedLog, ArboricultureBlocks.STRIPPED_LOGS_VANILLA_FIREPROOF.get(type).block());
		arboriculture.registerRefractoryWaxable(wood, ArboricultureBlocks.WOOD_VANILLA_FIREPROOF.get(type).block());
		arboriculture.registerRefractoryWaxable(strippedWood, ArboricultureBlocks.STRIPPED_WOOD_VANILLA_FIREPROOF.get(type).block());
		arboriculture.registerRefractoryWaxable(planks, ArboricultureBlocks.PLANKS_VANILLA_FIREPROOF.get(type).block());
		arboriculture.registerRefractoryWaxable(slab, ArboricultureBlocks.SLABS_VANILLA_FIREPROOF.get(type).block());
		arboriculture.registerRefractoryWaxable(fence, ArboricultureBlocks.FENCES_VANILLA_FIREPROOF.get(type).block());
		arboriculture.registerRefractoryWaxable(fenceGate, ArboricultureBlocks.FENCE_GATES_VANILLA_FIREPROOF.get(type).block());
		arboriculture.registerRefractoryWaxable(stairs, ArboricultureBlocks.STAIRS_VANILLA_FIREPROOF.get(type).block());
	}
}
