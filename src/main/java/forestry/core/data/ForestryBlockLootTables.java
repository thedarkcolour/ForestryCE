package forestry.core.data;

import forestry.api.ForestryConstants;
import forestry.arboriculture.leaves.BlockDecorativeLeaves;
import forestry.arboriculture.leaves.BlockDefaultLeaves;
import forestry.arboriculture.leaves.BlockDefaultLeavesFruit;
import forestry.arboriculture.wood.BlockForestryDoor;
import forestry.arboriculture.leaves.ForestryLeafType;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.features.CharcoalBlocks;
import forestry.arboriculture.loot.CountBlockFunction;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.platform.loot.OrganismFunction;
import forestry.core.platform.util.SpeciesUtil;
import forestry.core.platform.registration.FeatureBlock;
import forestry.core.platform.registration.FeatureBlockGroup;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import thedarkcolour.modkit.MKUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Data generator class that generates the block drop loot tables for forestry blocks.
 */
public class ForestryBlockLootTables extends BlockLootSubProvider {
	private static final float[] LEAVES_STICK_CHANCES = {0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};
	private static final ResourceKey<LootTable> LEAF_STICK_DROPS = ResourceKey.create(Registries.LOOT_TABLE, ForestryConstants.forestry("blocks/shared_leaf_sticks"));
	private final LinkedHashSet<Block> added = new LinkedHashSet<>();
	// The pass below covers every forestry block, including the ones a content jar registers. Those get their loot
	// from that jar's own provider, so they are skipped rather than written into core's root as well. Handed in by
	// Data from the modules the content jars declare, the same set that scopes core's names
	private final Set<ResourceLocation> contentOwned;

	protected ForestryBlockLootTables(HolderLookup.Provider registries, Set<ResourceLocation> contentOwned) {
		super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
		this.contentOwned = contentOwned;
	}

	@Override
	protected void generate() {
		MKUtils.forModRegistry(Registries.BLOCK, ForestryConstants.MOD_ID, (id, block) -> {
			if (!this.contentOwned.contains(id) && block.getLootTable() != BuiltInLootTables.EMPTY) {
				dropSelf(block);
			}
		});

		for (BlockDecorativeLeaves leaves : ArboricultureBlocks.LEAVES_DECORATIVE.getList()) {
			add(leaves, block -> droppingWithChances(block, leaves.getType(), NORMAL_LEAVES_SAPLING_CHANCES));
		}
		for (BlockDefaultLeaves leaves : ArboricultureBlocks.LEAVES_DEFAULT.getList()) {
			add(leaves, block -> droppingWithChances(block, leaves.getType(), NORMAL_LEAVES_SAPLING_CHANCES));
		}
		for (Map.Entry<ForestryLeafType, FeatureBlock<BlockDefaultLeavesFruit, BlockItem>> entry : ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.getFeatureByType().entrySet()) {
			FeatureBlock<BlockDefaultLeaves, BlockItem> defaultLeaves = ArboricultureBlocks.LEAVES_DEFAULT.get(entry.getKey());
			Block defaultLeavesBlock = defaultLeaves.block();
			Block fruitLeavesBlock = entry.getValue().block();
			add(fruitLeavesBlock, (block) -> droppingWithChances(defaultLeavesBlock, entry.getKey(), NORMAL_LEAVES_SAPLING_CHANCES));
		}
		for (BlockForestryDoor door : ArboricultureBlocks.DOORS.getList()) {
			add(door, createDoorTable(door));
		}
		registerLootTable(CharcoalBlocks.ASH, (block) -> LootTable.lootTable().setParamSet(LootContextParamSets.BLOCK)
			.withPool(LootPool.lootPool().add(LootItem.lootTableItem(CoreItems.ASH)).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(2, 1.0f / 3.0f))))
			.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.CHARCOAL)).apply(CountBlockFunction.builder()).apply(ApplyBonusCount.addBonusBinomialDistributionCount(enchantments().getOrThrow(Enchantments.FORTUNE), 23.0f / 40, 2))));
		registerLootTable(CoreBlocks.PEAT, block -> LootTable.lootTable()
			.withPool(LootPool.lootPool()
				.add(LootItem.lootTableItem(Blocks.DIRT)))
			.withPool(LootPool.lootPool()
				.apply(SetItemCountFunction.setCount(ConstantValue.exactly(2)))
				.add(LootItem.lootTableItem(CoreItems.PEAT.item()))));
		registerDropping(CoreBlocks.HUMUS, Blocks.DIRT);

		// todo fix all of these
		registerEmptyTables(ArboricultureBlocks.PODS); // Handled by internal logic
		registerEmptyTables(ArboricultureBlocks.SAPLING_GE); // Handled by internal logic
		registerLootTable(ArboricultureBlocks.LEAVES, block -> LootTable.lootTable()
			.withPool(createLeafStickDropPool())); // Other drops are handled by internal logic

		registerLootTable(CoreBlocks.APATITE_ORE, this::createApatiteOreDrops);
		registerLootTable(CoreBlocks.DEEPSLATE_APATITE_ORE, this::createApatiteOreDrops);

		registerLootTable(CoreBlocks.TIN_ORE, block -> createOreDrop(block, CoreItems.RAW_TIN.item()));
		registerLootTable(CoreBlocks.DEEPSLATE_TIN_ORE, block -> createOreDrop(block, CoreItems.RAW_TIN.item()));

		dropSelf(CoreBlocks.RAW_TIN_BLOCK.block());
	}

	private HolderLookup.RegistryLookup<net.minecraft.world.item.enchantment.Enchantment> enchantments() {
		return this.registries.lookupOrThrow(Registries.ENCHANTMENT);
	}

	private LootTable.Builder createApatiteOreDrops(Block block) {
		return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(CoreItems.APATITE.item()).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 7.0F))).apply(ApplyBonusCount.addUniformBonusCount(enchantments().getOrThrow(Enchantments.FORTUNE), 2))));
	}

	public LootTable.Builder droppingWithChances(Block block, ForestryLeafType definition, float... chances) {
		return createSilkTouchOrShearsDispatchTable(block,
			applyExplosionCondition(block, LootItem.lootTableItem(ArboricultureItems.TREE_SAPLING)
				.apply(OrganismFunction.fromId(SpeciesUtil.TREE_TYPE.get().id(), definition.getSpeciesId())))
				.when(BonusLevelTableCondition.bonusLevelFlatChance(enchantments().getOrThrow(Enchantments.FORTUNE), chances)))
			.withPool(createLeafStickDropPool());
	}

	private static LootPool.Builder createLeafStickDropPool() {
		return LootPool.lootPool().add(NestedLootTable.lootTableReference(LEAF_STICK_DROPS));
	}

	private LootTable.Builder createLeafStickDrops() {
		return LootTable.lootTable().withPool(LootPool.lootPool()
			.setRolls(ConstantValue.exactly(1.0F))
			.when(HAS_SHEARS.or(hasSilkTouch()).invert())
			.add(LootItem.lootTableItem(Items.STICK)
				.apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
				.apply(ApplyExplosionDecay.explosionDecay())
				.when(BonusLevelTableCondition.bonusLevelFlatChance(enchantments().getOrThrow(Enchantments.FORTUNE), LEAVES_STICK_CHANCES))));
	}

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
		super.generate(output);
		output.accept(LEAF_STICK_DROPS, createLeafStickDrops());
	}

	public void registerLootTable(FeatureBlock<?, ?> featureBlock, Function<Block, LootTable.Builder> builderFunction) {
		add(featureBlock.block(), builderFunction);
	}

	public void registerDropping(FeatureBlock<?, ?> featureBlock, ItemLike drop) {
		dropOther(featureBlock.block(), drop);
	}

	public void registerEmptyTables(FeatureBlockGroup<?, ?> blockGroup) {
		registerEmptyTables(blockGroup.blockArray());
	}

	public void registerEmptyTables(FeatureBlock<?, ?> featureBlock) {
		registerEmptyTables(featureBlock.block());
	}

	public void registerEmptyTables(Block... blocks) {
		for (Block block : blocks) {
			add(block, noDrop());
		}
	}

	@Override
	protected void add(Block block, LootTable.Builder builder) {
		super.add(block, builder);
		this.added.add(block);
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return this.added;
	}
}
