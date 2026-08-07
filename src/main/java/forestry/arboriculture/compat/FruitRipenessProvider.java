package forestry.arboriculture.compat;

import forestry.api.ForestryConstants;
import forestry.api.genetics.IFruitBearer;
import forestry.arboriculture.blocks.BlockDefaultLeavesFruit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum FruitRipenessProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
	INSTANCE;

	private static final String RIPENESS = "ForestryRipeness";

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		if (accessor.getBlockEntity() instanceof IFruitBearer bearer && bearer.hasFruit()) {
			int ripeness = Mth.clamp(
				Math.round(bearer.getRipeness() * 100.0F),
				0,
				100
			);

			data.putInt(RIPENESS, ripeness);
		}
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getServerData().contains(RIPENESS)) {
			tooltip.add(Component.translatable(
				"jade.forestry.ripeness",
				accessor.getServerData().getInt(RIPENESS)
			));
		} else if (accessor.getBlock() instanceof BlockDefaultLeavesFruit) {
			// These blocks are the fully ripe, fruit-bearing form of the
			// non-genetic default leaves and have no block entity.
			tooltip.add(Component.translatable(
				"jade.forestry.ripeness",
				100
			));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return ForestryConstants.forestry("fruit_ripeness");
	}
}