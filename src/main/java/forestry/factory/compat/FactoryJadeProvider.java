package forestry.factory.compat;

import forestry.api.ForestryConstants;
import forestry.api.core.IError;
import forestry.api.recipes.IFabricatorSmeltingRecipe;
import forestry.core.tiles.TileForestry;
import forestry.core.tiles.TilePowered;
import forestry.core.utils.RecipeUtils;
import forestry.factory.inventory.InventoryFabricator;
import forestry.factory.tiles.TileBottler;
import forestry.factory.tiles.TileCarpenter;
import forestry.factory.tiles.TileCentrifuge;
import forestry.factory.tiles.TileFabricator;
import forestry.factory.tiles.TileFermenter;
import forestry.factory.tiles.TileMillRainmaker;
import forestry.factory.tiles.TileMoistener;
import forestry.factory.tiles.TileSqueezer;
import forestry.factory.tiles.TileStill;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public enum FactoryJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
	INSTANCE;

	public enum Visibility {
		OFF,
		ON,
		SHIFT;

		public boolean isVisible(boolean showDetails) {
			return this == ON || (this == SHIFT && showDetails);
		}
	}

	public static final ResourceLocation SHOW_PROGRESS =
		ForestryConstants.forestry("machine_details.progress");

	public static final ResourceLocation SHOW_ERRORS =
		ForestryConstants.forestry("machine_details.errors");

	private static final String PROGRESS =
		"ForestryMachineProgress";

	private static final String ERROR_COUNT =
		"ForestryMachineErrorCount";

	private static final String ERROR_PREFIX =
		"ForestryMachineError";

	@Override
	public void appendServerData(
		CompoundTag data,
		BlockAccessor accessor
	) {
		if (!(accessor.getBlockEntity() instanceof TileForestry tile)) {
			return;
		}

		if (!isSupportedMachine(tile)) {
			return;
		}

		Integer progress = getProgressPercent(
			tile,
			accessor
		);

		if (progress != null) {
			data.putInt(
				PROGRESS,
				progress
			);
		}

		List<IError> errors = tile
			.getErrorLogic()
			.getErrors()
			.stream()
			.sorted(
				Comparator.comparing(
					error -> error.getId().toString()
				)
			)
			.toList();

		data.putInt(
			ERROR_COUNT,
			errors.size()
		);

		for (int i = 0; i < errors.size(); i++) {
			data.putString(
				ERROR_PREFIX + i,
				errors.get(i).getDescriptionTranslationKey()
			);
		}
	}

	@Override
	public void appendTooltip(
		ITooltip tooltip,
		BlockAccessor accessor,
		IPluginConfig config
	) {
		if (!(accessor.getBlockEntity() instanceof TileForestry tile)) {
			return;
		}

		if (!isSupportedMachine(tile)) {
			return;
		}

		CompoundTag data = accessor.getServerData();
		boolean showDetails = accessor.showDetails();

		if (
			isVisible(
				config,
				SHOW_PROGRESS,
				showDetails
			)
				&& data.contains(PROGRESS)
		) {
			tooltip.add(
				Component.translatable(
					"jade.forestry.machine.progress",
					data.getInt(PROGRESS)
				)
			);
		}

		if (
			isVisible(
				config,
				SHOW_ERRORS,
				showDetails
			)
		) {
			appendErrors(
				tooltip,
				data
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

	private static boolean isSupportedMachine(
		TileForestry tile
	) {
		return tile instanceof TileFabricator
			|| tile instanceof TileSqueezer
			|| tile instanceof TileCentrifuge
			|| tile instanceof TileStill
			|| tile instanceof TileFermenter
			|| tile instanceof TileCarpenter
			|| tile instanceof TileBottler
			|| tile instanceof TileMillRainmaker
			|| tile instanceof TileMoistener;
	}

	@Nullable
	private static Integer getProgressPercent(
		TileForestry tile,
		BlockAccessor accessor
	) {
		if (tile instanceof TileFabricator fabricator) {
			return getFabricatorProgress(
				fabricator,
				accessor
			);
		}

		if (tile instanceof TileFermenter fermenter) {
			return getFermenterProgress(
				fermenter
			);
		}

		if (tile instanceof TileMoistener moistener) {
			return getMoistenerProgress(
				moistener
			);
		}

		if (tile instanceof TileMillRainmaker rainmaker) {
			return getRainmakerProgress(
				rainmaker
			);
		}

		if (tile instanceof TilePowered powered) {
			return getPoweredProgress(
				powered
			);
		}

		return null;
	}

	@Nullable
	private static Integer getFabricatorProgress(
		TileFabricator fabricator,
		BlockAccessor accessor
	) {
		ItemStack metal = fabricator.getItem(
			InventoryFabricator.SLOT_METAL
		);

		if (!metal.isEmpty()) {
			IFabricatorSmeltingRecipe recipe =
				RecipeUtils.getFabricatorMeltingRecipe(
					accessor.getLevel().getRecipeManager(),
					metal
				);

			if (
				recipe != null
					&& recipe.getMeltingPoint() > 0
			) {
				/*
				 * getHeatScaled(5000) gives the exact raw heat because
				 * the Fabricator's internal MAX_HEAT is 5000.
				 *
				 * For melting, the meaningful overall progress is heat
				 * toward this metal's melting point, rather than the
				 * tiny repeating TilePowered work cycle.
				 */
				int heat = fabricator.getHeatScaled(
					5000
				);

				return Mth.clamp(
					Math.round(
						heat
							* 100.0F
							/ recipe.getMeltingPoint()
					),
					0,
					100
				);
			}
		}

		return getPoweredProgress(
			fabricator
		);
	}

	@Nullable
	private static Integer getPoweredProgress(
		TilePowered powered
	) {
		if (powered.getWorkCounter() <= 0) {
			return null;
		}

		return Mth.clamp(
			powered.getProgressScaled(100),
			0,
			100
		);
	}

	@Nullable
	private static Integer getFermenterProgress(
		TileFermenter fermenter
	) {
		/*
		 * Forestry stores/display fermentationTime as the amount
		 * remaining, so its GUI value moves from full toward empty.
		 *
		 * Use a larger scale before converting so very small remaining
		 * values do not disappear prematurely from integer rounding.
		 */
		int remaining =
			fermenter.getFermentationProgressScaled(
				10000
			);

		if (remaining <= 0) {
			return null;
		}

		int progress = Math.round(
			100.0F
				- remaining / 100.0F
		);

		return Mth.clamp(
			progress,
			0,
			100
		);
	}

	@Nullable
	private static Integer getMoistenerProgress(
		TileMoistener moistener
	) {
		if (!moistener.isProducing()) {
			return null;
		}

		/*
		 * productionTime is also a remaining-time counter.
		 */
		int remaining =
			moistener.getProductionProgressScaled(
				10000
			);

		int progress = Math.round(
			100.0F
				- remaining / 100.0F
		);

		return Mth.clamp(
			progress,
			0,
			100
		);
	}

	@Nullable
	private static Integer getRainmakerProgress(
		TileMillRainmaker rainmaker
	) {
		if (rainmaker.charge <= 0) {
			return null;
		}

		/*
		 * Rainmaker starts at charge 1 and activates after six complete
		 * mill cycles. Charge increments halfway through each cycle.
		 *
		 * Correct that half-cycle increment here so the displayed
		 * percentage remains continuous rather than jumping every time
		 * charge increases.
		 */
		int completedCycles =
			rainmaker.charge - 1;

		if (rainmaker.stage == 2) {
			completedCycles--;
		}

		float currentCycle =
			completedCycles
				+ Mth.clamp(
					rainmaker.progress,
					0.0F,
					1.0F
				);

		return Mth.clamp(
			Math.round(
				currentCycle
					* 100.0F
					/ 6.0F
			),
			0,
			100
		);
	}

	private static void appendErrors(
		ITooltip tooltip,
		CompoundTag data
	) {
		int count = data.getInt(
			ERROR_COUNT
		);

		for (int i = 0; i < count; i++) {
			String translationKey =
				data.getString(
					ERROR_PREFIX + i
				);

			if (!translationKey.isEmpty()) {
				tooltip.add(
					Component.literal("⚠ ")
						.append(
							Component.translatable(
								translationKey
							)
						)
						.withStyle(
							ChatFormatting.RED
						)
				);
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return ForestryConstants.forestry(
			"machine_details"
		);
	}
}