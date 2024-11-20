package forestry.core.worldgen;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import forestry.api.IForestryApi;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.apiculture.hives.IHiveManager;
import forestry.api.climate.ClimateState;
import forestry.api.genetics.ClimateHelper;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.apiculture.InventoryBeeHousing;
import forestry.apiculture.VillageHive;
import forestry.apiculture.blocks.BlockTypeApiculture;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureFeatures;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.inventory.InventoryApiary;
import forestry.apiculture.tiles.TileApiary;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.SpeciesUtil;

public class ApiaristPoolElement extends SinglePoolElement {
	public static final Codec<ApiaristPoolElement> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(templateCodec(), processorsCodec()).apply(instance, ApiaristPoolElement::new);
	});

	public ApiaristPoolElement(Either<ResourceLocation, StructureTemplate> template, Holder<StructureProcessorList> processors) {
		super(template, processors, StructureTemplatePool.Projection.RIGID);
	}

	@Override
	public void handleDataMarker(LevelAccessor level, StructureTemplate.StructureBlockInfo info, BlockPos pos, Rotation rotation, RandomSource random, BoundingBox box) {
		// yes this is nullable, shut up intellij
		if (info.nbt() != null) {
			if (StructureMode.valueOf(info.nbt().getString("mode")) == StructureMode.DATA) {
				String marker = info.nbt().getString("metadata");

				if ("apiary".equals(marker)) {
					replaceWithApiary(level, info, random);
				}
			}
		}
	}

	@Override
	protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox bounds, boolean keepJigsaws) {
		// data markers get wiped (ignored) if we don't remove the structure block processor
		return super.getSettings(rotation, bounds, keepJigsaws)
				.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
	}

	private static void replaceWithApiary(LevelAccessor level, StructureTemplate.StructureBlockInfo info, RandomSource random) {
		BlockPos markerPos = info.pos();

		// remove the block entity of the Data block beforehand so that its NBT doesn't overwrite the apiary
		level.removeBlock(markerPos, false);
		level.setBlock(markerPos, ApicultureBlocks.BASE.get(BlockTypeApiculture.APIARY).defaultState(), Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);

		// add a queen and some frames
		TileUtil.actOnTile(level, markerPos, TileApiary.class, apiary -> {
			ItemStack queen = chooseRandomVillageQueen(level, markerPos, random);

			apiary.setItem(InventoryBeeHousing.SLOT_QUEEN, queen);

			// this method gets called multiple times so having random number of frames is impossible :)))
			for (int i = 0; i < 3; ++i) {
				ItemStack frame = ApicultureItems.FRAME_PROVEN.stack();
				int maxDamage = frame.getMaxDamage();
				frame.setDamageValue(random.nextIntBetweenInclusive(maxDamage / 4, maxDamage - maxDamage / 4));
				apiary.setItem(InventoryApiary.SLOT_FRAMES_1 + i, frame);
			}
		});
	}

	private static ItemStack chooseRandomVillageQueen(LevelAccessor level, BlockPos markerPos, RandomSource random) {
		IHiveManager manager = IForestryApi.INSTANCE.getHiveManager();
		// 25% chance to pick from rare pool instead of common pool
		boolean rarePool = random.nextInt(4) == 0;
		List<VillageHive> pool = rarePool ? manager.getRareVillageHives() : manager.getCommonVillageHives();
		IBeeSpeciesType type = SpeciesUtil.BEE_TYPE.get();
		ClimateState biomeState = IForestryApi.INSTANCE.getClimateManager().getBiomeState(level, markerPos);
		ArrayList<Pair<IBeeSpecies, Map<IChromosome<?>, IAllele>>> candidates = getCandidates(type, pool, biomeState);
		// if no rare bees match, get common candidates
		if (rarePool && candidates.isEmpty()) {
			candidates = getCandidates(type, manager.getCommonVillageHives(), biomeState);
		}

		if (candidates.isEmpty()) {
			// if no candidates matches the given climate, pick a random common bee
			ImmutableList<VillageHive> commons = manager.getCommonVillageHives();
			VillageHive hive = commons.get(random.nextInt(commons.size()));
			IBeeSpecies species = type.getSpecies(hive.speciesId());
			return createQueen(species, hive.alleles());
		} else {
			// pick random candidate
			Pair<IBeeSpecies, Map<IChromosome<?>, IAllele>> candidate = candidates.get(random.nextInt(candidates.size()));
			return createQueen(candidate.getFirst(), candidate.getSecond());
		}
	}

	private static ArrayList<Pair<IBeeSpecies, Map<IChromosome<?>, IAllele>>> getCandidates(IBeeSpeciesType type, List<VillageHive> pool, ClimateState biomeState) {
		// collect list of candidates that can work in this climate
		ArrayList<Pair<IBeeSpecies, Map<IChromosome<?>, IAllele>>> candidates = new ArrayList<>();
		for (VillageHive hive : pool) {
			IBeeSpecies species = type.getSpecies(hive.speciesId());
			IGenome defaultGenome = species.getDefaultGenome();

			// check temperature and humidity
			if (ClimateHelper.isWithinLimits(biomeState.temperature(), species.getTemperature(), defaultGenome.getActiveValue(BeeChromosomes.TEMPERATURE_TOLERANCE)) &&
					ClimateHelper.isWithinLimits(biomeState.humidity(), species.getHumidity(), defaultGenome.getActiveValue(BeeChromosomes.HUMIDITY_TOLERANCE))) {
				candidates.add(Pair.of(species, hive.alleles()));
			}
		}
		return candidates;
	}

	private static ItemStack createQueen(IBeeSpecies species, Map<IChromosome<?>, IAllele> extraAlleles) {
		IBee bee = species.createIndividual(extraAlleles);
		// purebred
		bee.setMate(bee.getGenome());
		return bee.createStack(BeeLifeStage.QUEEN);
	}

	@Override
	public StructurePoolElementType<?> getType() {
		return ApicultureFeatures.APIARIST_POOL_ELEMENT_TYPE.get();
	}

	@Override
	public String toString() {
		return "ApiaristPoolElement[" + this.template + "]";
	}
}
