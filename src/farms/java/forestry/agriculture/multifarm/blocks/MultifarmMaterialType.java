package forestry.agriculture.multifarm.blocks;

import forestry.api.core.IBlockSubtype;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Locale;

public enum MultifarmMaterialType implements IBlockSubtype {
	STONE_BRICK(Blocks.STONE_BRICKS),
	MOSSY_STONE_BRICK(Blocks.MOSSY_STONE_BRICKS),
	CRACKED_STONE_BRICK(Blocks.CRACKED_STONE_BRICKS),
	BRICK(Blocks.BRICKS),
	CUT_SANDSTONE(Blocks.CUT_SANDSTONE),
	CHISELED_SANDSTONE(Blocks.CHISELED_SANDSTONE),
	NETHER_BRICK(Blocks.NETHER_BRICKS),
	CHISELED_STONE_BRICK(Blocks.CHISELED_STONE_BRICKS),
	QUARTZ(Blocks.QUARTZ_BLOCK),
	CHISELED_QUARTZ(Blocks.CHISELED_QUARTZ_BLOCK),
	QUARTZ_PILLAR(Blocks.QUARTZ_PILLAR);

	private final Block base;

	MultifarmMaterialType(Block base) {
		this.base = base;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}

	public Block getBase() {
		return this.base;
	}
}
