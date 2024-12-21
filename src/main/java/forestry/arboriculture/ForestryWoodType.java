/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.arboriculture;

import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

import com.mojang.authlib.GameProfile;

import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.blocks.ForestryLeafType;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.modules.features.FeatureBlockGroup;

import org.jetbrains.annotations.Nullable;

public enum ForestryWoodType implements IWoodType {
	LARCH(ForestryLeafType.LARCH),
	TEAK(ForestryLeafType.TEAK),
	ACACIA_DESERT(ForestryLeafType.DESERT_ACACIA),
	LIME(ForestryLeafType.LIME),
	CHESTNUT(ForestryLeafType.CHESTNUT),
	WENGE(ForestryLeafType.WENGE),
	BAOBAB(ForestryLeafType.BAOBAB),
	SEQUOIA(ForestryLeafType.SEQUOIA, 4.0f),

	KAPOK(ForestryLeafType.KAPOK),
	EBONY(ForestryLeafType.EBONY),
	MAHOGANY(ForestryLeafType.MAHOGANY),
	BALSA(ForestryLeafType.BALSA, 1.0f),
	WILLOW(ForestryLeafType.WILLOW),
	WALNUT(ForestryLeafType.WALNUT),
	GREENHEART(ForestryLeafType.SIPIRI, 7.5f),
	BUSH_CHERRY(ForestryLeafType.BUSH_CHERRY),

	MAHOE(ForestryLeafType.MAHOE),
	POPLAR(ForestryLeafType.POPLAR),
	PALM(ForestryLeafType.DATE),
	PAPAYA(ForestryLeafType.PAPAYA),
	PINE(ForestryLeafType.PINE, 3.0f),
	PLUM(ForestryLeafType.PLUM),
	MAPLE(ForestryLeafType.MAPLE),
	CITRUS(ForestryLeafType.LEMON),

	GIGANTEUM(ForestryLeafType.GIANT_SEQUOIA, 4.0f),
	IPE(ForestryLeafType.IPE),
	PADAUK(ForestryLeafType.PADAUK),
	COCOBOLO(ForestryLeafType.COCOBOLO),
	ZEBRAWOOD(ForestryLeafType.ZEBRAWOOD);

	public static final float DEFAULT_HARDNESS = 2.0f;
	public static final ForestryWoodType[] VALUES = values();

	// Lowercase name of this enum
	private final String name;
	private final float hardness;
	private final ForestryLeafType leafType;
	private final WoodType type;

	ForestryWoodType(ForestryLeafType leafType) {
		this(leafType, DEFAULT_HARDNESS);
	}

	ForestryWoodType(ForestryLeafType leafType, float hardness) {
		this.name = name().toLowerCase(Locale.ENGLISH);
		this.leafType = leafType;
		this.hardness = hardness;

		this.type = new WoodType(this.name, new BlockSetType(this.name));
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
}
