package forestry.apiculture.blocks;

import forestry.apiculture.items.EnumHoneyComb;
import forestry.core.blocks.IColoredBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class BlockHoneyComb extends Block implements IColoredBlock {
	public final EnumHoneyComb type;

	public BlockHoneyComb(EnumHoneyComb type) {
		super(Block.Properties.of().sound(SoundType.CORAL_BLOCK).strength(1F));
		this.type = type;
	}

	public EnumHoneyComb getType() {
		return this.type;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int colorMultiplier(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
		EnumHoneyComb honeyComb = this.type;
		if (tintIndex == 1) {
			return honeyComb.primaryColor;
		} else {
			return honeyComb.secondaryColor;
		}
	}
}
