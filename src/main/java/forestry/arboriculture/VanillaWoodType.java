/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.arboriculture;

import java.util.Locale;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import com.mojang.authlib.GameProfile;

import forestry.api.arboriculture.IWoodType;
import forestry.api.genetics.IGenome;
import forestry.arboriculture.blocks.ForestryLeafType;

import org.jetbrains.annotations.Nullable;

public enum VanillaWoodType implements IWoodType {
	OAK(ForestryLeafType.OAK),
	SPRUCE(ForestryLeafType.SPRUCE),
	BIRCH(ForestryLeafType.BIRCH),
	JUNGLE(ForestryLeafType.JUNGLE),
	ACACIA(ForestryLeafType.ACACIA_VANILLA),
	DARK_OAK(ForestryLeafType.DARK_OAK),
	CHERRY(ForestryLeafType.CHERRY_VANILLA);

	public static final VanillaWoodType[] VALUES = values();

	private final ForestryLeafType leafType;

	VanillaWoodType(ForestryLeafType leafType) {
		this.leafType = leafType;
	}

	public static VanillaWoodType getRandom(Random random) {
		return VALUES[random.nextInt(VALUES.length)];
	}

	@Override
	public float getHardness() {
		return 2.0F;
	}

	@Override
	public String toString() {
		return super.toString().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public boolean setDefaultLeaves(LevelAccessor level, BlockPos pos, IGenome genome, RandomSource rand, @Nullable GameProfile owner) {
		return ForestryWoodType.setDefaultLeavesImpl(level, pos, genome, rand, this.leafType);
	}

	@Override
	public String getSerializedName() {
		return toString();
	}
}
