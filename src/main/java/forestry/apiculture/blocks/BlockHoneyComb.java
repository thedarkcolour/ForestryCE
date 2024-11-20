package forestry.apiculture.blocks;

import javax.annotation.Nullable;

import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.apiculture.items.EnumHoneyComb;
import forestry.core.blocks.IColoredBlock;

public class BlockHoneyComb extends Block implements IColoredBlock {
	public final EnumHoneyComb type;

	public BlockHoneyComb(EnumHoneyComb type) {
		super(Block.Properties.of().sound(SoundType.CORAL_BLOCK).strength(1F));
		this.type = type;
	}

	public EnumHoneyComb getType() {
		return type;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int colorMultiplier(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
		EnumHoneyComb honeyComb = type;
		if (tintIndex == 1) {
			return honeyComb.primaryColor;
		} else {
			return honeyComb.secondaryColor;
		}
	}
}
