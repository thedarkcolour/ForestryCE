package forestry.arboriculture.compat;

import forestry.api.ForestryConstants;
import forestry.api.arboriculture.ForestryFruits;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.core.genetics.IFruitBearer;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.leaves.BlockDefaultLeavesFruit;
import forestry.arboriculture.leaves.TileLeaves;
import forestry.core.platform.util.GeneticsUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum FruitJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
	INSTANCE;

	public enum Visibility {
		OFF,
		ON,
		SHIFT;

		public boolean isVisible(boolean showDetails) {
			return this == ON || (this == SHIFT && showDetails);
		}
	}

	public static final ResourceLocation SHOW_FRUIT_TYPE =
		ForestryConstants.forestry("fruit_details.fruit_type");

	public static final ResourceLocation SHOW_GROWTH =
		ForestryConstants.forestry("fruit_details.growth");

	private static final String GROWTH =
		"ForestryFruitGrowth";

	@Override
	public void appendServerData(
		CompoundTag data,
		BlockAccessor accessor
	) {
		if (
			accessor.getBlockEntity() instanceof IFruitBearer bearer
				&& bearer.hasFruit()
		) {
			int growth = Mth.clamp(
				Math.round(
					bearer.getRipeness() * 100.0F
				),
				0,
				100
			);

			data.putInt(
				GROWTH,
				growth
			);
		}
	}

	@Override
	public void appendTooltip(
		ITooltip tooltip,
		BlockAccessor accessor,
		IPluginConfig config
	) {
		boolean showDetails = accessor.showDetails();

		if (
			isVisible(
				config,
				SHOW_FRUIT_TYPE,
				showDetails
			)
		) {
			appendFruitType(
				tooltip,
				accessor
			);
		}

		if (
			isVisible(
				config,
				SHOW_GROWTH,
				showDetails
			)
		) {
			appendGrowth(
				tooltip,
				accessor
			);
		}
	}

	private static boolean isVisible(
		IPluginConfig config,
		ResourceLocation key,
		boolean showDetails
	) {
		Visibility visibility = config.getEnum(key);

		return visibility.isVisible(showDetails);
	}

	private static void appendFruitType(
		ITooltip tooltip,
		BlockAccessor accessor
	) {
		IGenome genome = getTreeGenome(accessor);

		if (genome == null) {
			return;
		}

		ResourceLocation fruitId =
			genome.getActiveValue(
				TreeChromosomes.FRUIT
			);

		if (ForestryFruits.NONE.equals(fruitId)) {
			return;
		}

		tooltip.add(
			Component.translatable(
				"jade.forestry.fruit",
				GeneticsUtil.getActiveName(
					genome,
					TreeChromosomes.FRUIT
				)
			)
		);
	}

	private static IGenome getTreeGenome(
		BlockAccessor accessor
	) {
		if (
			accessor.getBlockEntity()
				instanceof TileLeaves leaves
		) {
			ITree tree = leaves.getTree();

			if (tree != null) {
				return tree.getGenome();
			}
		}

		if (
			accessor.getBlock()
				instanceof BlockDefaultLeavesFruit leaves
		) {
			return leaves
				.getType()
				.getIndividual()
				.getGenome();
		}

		return null;
	}

	private static void appendGrowth(
		ITooltip tooltip,
		BlockAccessor accessor
	) {
		CompoundTag data = accessor.getServerData();

		if (data.contains(GROWTH, Tag.TAG_INT)) {
			tooltip.add(
				Component.translatable(
					"jade.forestry.ripeness",
					data.getInt(GROWTH)
				)
			);

			return;
		}

		/*
		 * Default fruit leaves have no block entity. They represent
		 * already mature worldgen fruit leaves, so their fruit is
		 * effectively at 100% growth.
		 */
		if (
			accessor.getBlock()
				instanceof BlockDefaultLeavesFruit
		) {
			tooltip.add(
				Component.translatable(
					"jade.forestry.ripeness",
					100
				)
			);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return ForestryConstants.forestry(
			"fruit_details"
		);
	}
}