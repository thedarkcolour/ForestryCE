package forestry.farming.blocks;

import forestry.api.core.IBlockSubtype;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Locale;

public enum EnumFarmMaterial implements IBlockSubtype {
	STONE_BRICK(Blocks.STONE_BRICKS, ChatFormatting.DARK_GRAY),
	MOSSY_STONE_BRICK(Blocks.MOSSY_STONE_BRICKS, ChatFormatting.DARK_GRAY),
	CRACKED_STONE_BRICK(Blocks.CRACKED_STONE_BRICKS, ChatFormatting.DARK_GRAY),
	BRICK(Blocks.BRICKS, ChatFormatting.GOLD),
	CUT_SANDSTONE(Blocks.CUT_SANDSTONE, ChatFormatting.YELLOW),
	SANDSTONE_CHISELED(Blocks.CHISELED_SANDSTONE, ChatFormatting.YELLOW),
	BRICK_NETHER(Blocks.NETHER_BRICKS, ChatFormatting.DARK_RED),
	BRICK_CHISELED(Blocks.CHISELED_STONE_BRICKS, ChatFormatting.GOLD),
	QUARTZ(Blocks.QUARTZ_BLOCK, ChatFormatting.WHITE),
	QUARTZ_CHISELED(Blocks.CHISELED_QUARTZ_BLOCK, ChatFormatting.WHITE),
	QUARTZ_LINES(Blocks.QUARTZ_PILLAR, ChatFormatting.WHITE);

	private final Block base;
	private final ChatFormatting formatting;

	EnumFarmMaterial(Block base, ChatFormatting formatting) {
		this.base = base;
		this.formatting = formatting;
	}

	public ChatFormatting getFormatting() {
		return this.formatting;
	}

	public void saveToCompound(CompoundTag compound) {
		compound.putInt("FarmBlock", this.ordinal());
	}

	public Component getDisplayName() {
		return this.base.getName();
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}

	public Block getBase() {
		return this.base;
	}
}
