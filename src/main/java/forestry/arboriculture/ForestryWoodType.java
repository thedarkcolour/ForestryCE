package forestry.arboriculture;

import com.mojang.authlib.GameProfile;
import forestry.api.ForestryConstants;
import forestry.api.ForestryTags;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.blocks.ForestryLeafType;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.modules.features.FeatureBlock;
import forestry.modules.features.FeatureBlockGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.stream.Stream;

public enum ForestryWoodType implements IWoodType {
	LARCH(ForestryLeafType.LARCH),
	TEAK(ForestryLeafType.TEAK),
	ACACIA_DESERT(ForestryLeafType.CAMELTHORN), // TODO - 1.21.1 - Rename to Camelthorn
	LIME(ForestryLeafType.LIME),
	CHESTNUT(ForestryLeafType.CHESTNUT),
	WENGE(ForestryLeafType.WENGE),
	BAOBAB(ForestryLeafType.BAOBAB),
	SEQUOIA(ForestryLeafType.SEQUOIA, 4.0f),

	KAPOK(ForestryLeafType.KAPOK),
	EBONY(ForestryLeafType.EBONY),
	ELM(ForestryLeafType.ELM),
	MAHOGANY(ForestryLeafType.MAHOGANY),
	BALSA(ForestryLeafType.BALSA, 1.0f),
	WILLOW(ForestryLeafType.WILLOW),
	WALNUT(ForestryLeafType.WALNUT),
	GREENHEART(ForestryLeafType.GREENHEART, 7.5f),
	HILL_CHERRY(ForestryLeafType.SOUR_CHERRY), // TODO - 1.21.1 - Rename to Sour Cherry

	MAHOE(ForestryLeafType.MAHOE),
	POPLAR(ForestryLeafType.POPLAR),
	PALM(ForestryLeafType.DATE),
	PAPAYA(ForestryLeafType.PAPAYA),
	PINE(ForestryLeafType.PINE, 3.0f),
	PLUM(ForestryLeafType.PLUM),
	MAPLE(ForestryLeafType.MAPLE),
	CITRUS(ForestryLeafType.LEMON), //TODO - 1.21.1 - Change from Citrus to Lemon, as Orange is also Citrus.

	GIGANTEUM(ForestryLeafType.GIANT_SEQUOIA, 4.0f), // TODO - 1.21.1 - Rename to Giant Sequoia
	IPE(ForestryLeafType.IPE),
	PADAUK(ForestryLeafType.PADAUK),
	COCOBOLO(ForestryLeafType.COCOBOLO),
	FIR(ForestryLeafType.FIR),
	COCONUT(ForestryLeafType.COCONUT),
	BEECH(ForestryLeafType.BEECH),
	FEIJOA(ForestryLeafType.FEIJOA),
	DOGWOOD(ForestryLeafType.DOGWOOD),
	GINKGO(ForestryLeafType.GINKGO),
	JACARANDA(ForestryLeafType.JACARANDA),
	PEWEN(ForestryLeafType.PEWEN),
	MACROCARPA(ForestryLeafType.MACROCARPA),
	OLIVE(ForestryLeafType.OLIVE),
	ORANGE(ForestryLeafType.ORANGE),
	PEAR(ForestryLeafType.PEAR),
	KAURI(ForestryLeafType.KAURI),
	ZEBRAWOOD(ForestryLeafType.ZEBRANO); //TODO - 1.21.1 - Change to Zebrano

	public static final float DEFAULT_HARDNESS = 2.0f;
	public static final ForestryWoodType[] VALUES = values();

	// Lowercase name of this enum
	private final String name;
	private final float hardness;
	private final ForestryLeafType leafType;
	private final WoodType type;
	public final TagKey<Block> blockTag;
	public final TagKey<Item> itemTag;
	public final TagKey<Block> fireproofBlockTag;
	public final TagKey<Item> fireproofItemTag;

	ForestryWoodType(ForestryLeafType leafType) {
		this(leafType, DEFAULT_HARDNESS);
	}

	ForestryWoodType(ForestryLeafType leafType, float hardness) {
		this.name = name().toLowerCase(Locale.ENGLISH);
		this.leafType = leafType;
		this.hardness = hardness;

		this.type = new WoodType(ForestryConstants.forestry(this.name).toString(), new BlockSetType(this.name));

		this.blockTag = ForestryTags.blockTag(this.name + "_logs");
		this.itemTag = ForestryTags.itemTag(this.name + "_logs");
		this.fireproofBlockTag = ForestryTags.blockTag("fireproof_" + this.name + "_logs");
		this.fireproofItemTag = ForestryTags.itemTag("fireproof_" + this.name + "_logs");
	}

	@Override
	public float getHardness() {
		return this.hardness;
	}

	public static ForestryWoodType getRandom(RandomSource random) {
		return VALUES[random.nextInt(VALUES.length)];
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public boolean setDefaultLeaves(LevelAccessor level, BlockPos pos, IGenome genome, RandomSource rand, @Nullable GameProfile owner) {
		return setDefaultLeavesImpl(level, pos, genome, rand, this.leafType);
	}

	static boolean setDefaultLeavesImpl(LevelAccessor level, BlockPos pos, IGenome genome, RandomSource rand, ForestryLeafType leafType) {
		IFruit fruit = genome.getActiveValue(TreeChromosomes.FRUIT);
		BlockState defaultLeaves;
		FeatureBlockGroup<? extends Block, ForestryLeafType> leavesGroup;
		if (fruit.isFruitLeaf() && rand.nextFloat() <= fruit.getFruitChance(genome, level)) {
			leavesGroup = ArboricultureBlocks.LEAVES_DEFAULT_FRUIT;
		} else {
			leavesGroup = ArboricultureBlocks.LEAVES_DEFAULT;
		}
		defaultLeaves = leavesGroup.get(leafType).defaultState();
		return level.setBlock(pos, defaultLeaves, 19);
	}

	@Override
	public String getSerializedName() {
		return toString();
	}

	public BlockSetType getBlockSetType() {
		return this.type.setType();
	}

	public WoodType getWoodType() {
		return this.type;
	}

	public Stream<FeatureBlock<?, ?>> getBurnables() {
		return Stream.of(
			ArboricultureBlocks.LOGS.get(this),
			ArboricultureBlocks.WOOD.get(this),
			ArboricultureBlocks.STRIPPED_LOGS.get(this),
			ArboricultureBlocks.STRIPPED_WOOD.get(this)
		);
	}

	public Stream<FeatureBlock<?, ?>> getFireproof() {
		return Stream.of(
			ArboricultureBlocks.LOGS_FIREPROOF.get(this),
			ArboricultureBlocks.WOOD_FIREPROOF.get(this),
			ArboricultureBlocks.STRIPPED_LOGS_FIREPROOF.get(this),
			ArboricultureBlocks.STRIPPED_WOOD_FIREPROOF.get(this)
		);
	}
}
