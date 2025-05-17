/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.arboriculture;

import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;

import com.mojang.authlib.GameProfile;

import forestry.api.ForestryTags;
import forestry.api.arboriculture.IWoodType;
import forestry.api.genetics.IGenome;
import forestry.arboriculture.blocks.ForestryLeafType;

import org.jetbrains.annotations.Nullable;

// todo add Mangrove
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

	public final TagKey<Block> fireproofBlockTag;
	public final TagKey<Item> fireproofItemTag;

	VanillaWoodType(ForestryLeafType leafType) {
		this.leafType = leafType;

		String name = name().toLowerCase(Locale.ENGLISH);
		this.fireproofBlockTag = ForestryTags.blockTag("fireproof_" + name + "_logs");
		this.fireproofItemTag = ForestryTags.itemTag("fireproof_" + name + "_logs");
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
