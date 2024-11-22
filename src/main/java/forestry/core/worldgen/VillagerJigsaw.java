package forestry.core.worldgen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import forestry.api.ForestryConstants;

public class VillagerJigsaw {
	private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(Registries.PROCESSOR_LIST, new ResourceLocation("minecraft", "empty"));

	public static void init(Registry<StructureTemplatePool> pools, Registry<StructureProcessorList> processors) {
		addVillagerHouse(pools, processors, "plains", 15);
		addVillagerHouse(pools, processors, "snowy", 15);
		addVillagerHouse(pools, processors, "savanna", 15);
		addVillagerHouse(pools, processors, "desert", 15);
		addVillagerHouse(pools, processors, "taiga", 15);
	}

	private static void addVillagerHouse(Registry<StructureTemplatePool> pools, Registry<StructureProcessorList> processors, String biome, int weight) {
		addToJigsawPattern(pools, new ResourceLocation("village/" + biome + "/houses"), new ApiaristPoolElement(Either.left(ForestryConstants.forestry("village/apiarist_house_" + biome + "_1")), processors.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY)), weight);
	}

	public static void addToJigsawPattern(Registry<StructureTemplatePool> pools, ResourceLocation pool, StructurePoolElement newPiece, int weight) {
		StructureTemplatePool oldPool = pools.get(pool);
		if (oldPool != null) {
			List<StructurePoolElement> jigsawPieces = oldPool.templates;

			for (int i = 0; i < weight; ++i) {
				jigsawPieces.add(newPiece);
			}

			List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(oldPool.rawTemplates);
			listOfPieceEntries.add(new Pair<>(newPiece, weight));
			oldPool.rawTemplates = listOfPieceEntries;
		}
	}
/*
	private static void addVillagerHouse(Registry<StructureTemplatePool> pools, Registry<StructureProcessorList> processors, String biome, int weight) {
		// Grabs the processor list we want to use along with our piece.
		// This is a requirement as using the ProcessorLists.EMPTY field will cause the game to throw errors.
		// The reason why is the empty processor list in the world's registry is not the same instance as in that field once the world is started up.
		Holder<StructureProcessorList> emptyProcessorList = processors.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);

		// Grab the pool we want to add to
		StructureTemplatePool pool = pools.get(new ResourceLocation("village/" + biome + "/houses"));
		if (pool == null) return;

		// Grabs the nbt piece and creates a SinglePoolElement of it that we can add to a structure's pool.
		// Use .legacy( for villages/outposts and .single( for everything else
		ApiaristPoolElement piece = new ApiaristPoolElement(Either.left(ForestryConstants.forestry("village/apiarist_house_" + biome + "_1")), emptyProcessorList);

		// Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's templates field public for us to see.
		// Weight is handled by how many times the entry appears in this list.
		// We do not need to worry about immutability as this field is created using Lists.newArrayList(); which makes a mutable list.
		for (int i = 0; i < weight; i++) {
			pool.templates.add(piece);
		}

		// Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's rawTemplates field public for us to see.
		// This list of pairs of pieces and weights is not used by vanilla by default but another mod may need it for efficiency.
		// So lets add to this list for completeness. We need to make a copy of the array as it can be an immutable list.
		//   NOTE: This is a com.mojang.datafixers.util.Pair. It is NOT a fastUtil pair class. Use the mojang class.
		List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
		listOfPieceEntries.add(new Pair<>(piece, weight));
		pool.rawTemplates = listOfPieceEntries;
	}*/
}
