package forestry.factory.compat;

import forestry.api.ForestryConstants;
import forestry.api.core.IError;
import forestry.api.core.IProduct;
import forestry.api.recipes.ICentrifugeRecipe;
import forestry.api.recipes.ISqueezerRecipe;
import forestry.core.tiles.TileForestry;
import forestry.core.tiles.TilePowered;
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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

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

	public static final ResourceLocation FABRICATOR_DETAILS =
		ForestryConstants.forestry("machine_fabricator");
	public static final ResourceLocation FABRICATOR_PROGRESS =
		ForestryConstants.forestry("machine_fabricator.progress");
	public static final ResourceLocation FABRICATOR_ERRORS =
		ForestryConstants.forestry("machine_fabricator.errors");
	public static final ResourceLocation FABRICATOR_HEAT_CONFIG =
		ForestryConstants.forestry("machine_fabricator.heat");
	public static final ResourceLocation FABRICATOR_RECIPE_CONFIG =
		ForestryConstants.forestry("machine_fabricator.recipe");

	public static final ResourceLocation SQUEEZER_DETAILS =
		ForestryConstants.forestry("machine_squeezer");
	public static final ResourceLocation SQUEEZER_PROGRESS =
		ForestryConstants.forestry("machine_squeezer.progress");
	public static final ResourceLocation SQUEEZER_ERRORS =
		ForestryConstants.forestry("machine_squeezer.errors");
	public static final ResourceLocation SQUEEZER_REMNANT_CONFIG =
		ForestryConstants.forestry("machine_squeezer.remnant");

	public static final ResourceLocation CENTRIFUGE_DETAILS =
		ForestryConstants.forestry("machine_centrifuge");
	public static final ResourceLocation CENTRIFUGE_PROGRESS =
		ForestryConstants.forestry("machine_centrifuge.progress");
	public static final ResourceLocation CENTRIFUGE_ERRORS =
		ForestryConstants.forestry("machine_centrifuge.errors");
	public static final ResourceLocation CENTRIFUGE_PRODUCTS_CONFIG =
		ForestryConstants.forestry("machine_centrifuge.products");

	public static final ResourceLocation STILL_DETAILS =
		ForestryConstants.forestry("machine_still");
	public static final ResourceLocation STILL_PROGRESS =
		ForestryConstants.forestry("machine_still.progress");
	public static final ResourceLocation STILL_ERRORS =
		ForestryConstants.forestry("machine_still.errors");

	public static final ResourceLocation FERMENTER_DETAILS =
		ForestryConstants.forestry("machine_fermenter");
	public static final ResourceLocation FERMENTER_PROGRESS =
		ForestryConstants.forestry("machine_fermenter.progress");
	public static final ResourceLocation FERMENTER_ERRORS =
		ForestryConstants.forestry("machine_fermenter.errors");
	public static final ResourceLocation FERMENTER_FUEL_CONFIG =
		ForestryConstants.forestry("machine_fermenter.fuel");

	public static final ResourceLocation CARPENTER_DETAILS =
		ForestryConstants.forestry("machine_carpenter");
	public static final ResourceLocation CARPENTER_PROGRESS =
		ForestryConstants.forestry("machine_carpenter.progress");
	public static final ResourceLocation CARPENTER_ERRORS =
		ForestryConstants.forestry("machine_carpenter.errors");
	public static final ResourceLocation CARPENTER_RECIPE_CONFIG =
		ForestryConstants.forestry("machine_carpenter.recipe");

	public static final ResourceLocation BOTTLER_DETAILS =
		ForestryConstants.forestry("machine_bottler");
	public static final ResourceLocation BOTTLER_PROGRESS =
		ForestryConstants.forestry("machine_bottler.progress");
	public static final ResourceLocation BOTTLER_ERRORS =
		ForestryConstants.forestry("machine_bottler.errors");
	public static final ResourceLocation BOTTLER_MODE_CONFIG =
		ForestryConstants.forestry("machine_bottler.mode");

	public static final ResourceLocation RAINMAKER_DETAILS =
		ForestryConstants.forestry("machine_rainmaker");
	public static final ResourceLocation RAINMAKER_PROGRESS =
		ForestryConstants.forestry("machine_rainmaker.progress");
	public static final ResourceLocation RAINMAKER_ERRORS =
		ForestryConstants.forestry("machine_rainmaker.errors");
	public static final ResourceLocation RAINMAKER_STATUS_CONFIG =
		ForestryConstants.forestry("machine_rainmaker.status");

	public static final ResourceLocation MOISTENER_DETAILS =
		ForestryConstants.forestry("machine_moistener");
	public static final ResourceLocation MOISTENER_PROGRESS =
		ForestryConstants.forestry("machine_moistener.progress");
	public static final ResourceLocation MOISTENER_ERRORS =
		ForestryConstants.forestry("machine_moistener.errors");
	public static final ResourceLocation MOISTENER_SPEED_CONFIG =
		ForestryConstants.forestry("machine_moistener.speed");
	public static final ResourceLocation MOISTENER_RESOURCE_PROGRESS_CONFIG =
		ForestryConstants.forestry("machine_moistener.resource_progress");

	private static final String PROGRESS =
		"ForestryMachineProgress";

	private static final String ERROR_COUNT =
		"ForestryMachineErrorCount";

	private static final String ERROR_PREFIX =
		"ForestryMachineError";

	private static final String FABRICATOR_HEAT =
		"ForestryFabricatorHeat";

	private static final String FABRICATOR_MAX_HEAT =
		"ForestryFabricatorMaxHeat";

	private static final String FABRICATOR_MELTING_POINT =
		"ForestryFabricatorMeltingPoint";

	private static final String FABRICATOR_RECIPE =
		"ForestryFabricatorRecipe";

	private static final String FERMENTER_FUEL =
		"ForestryFermenterFuel";

	private static final String CARPENTER_RECIPE =
		"ForestryCarpenterRecipe";

	private static final String BOTTLER_MODE =
		"ForestryBottlerMode";

	private static final int BOTTLER_MODE_FILLING = 1;
	private static final int BOTTLER_MODE_EMPTYING = 2;

	private static final String RAINMAKER_STATUS =
		"ForestryRainmakerStatus";

	private static final int RAINMAKER_STARTING_RAIN = 1;
	private static final int RAINMAKER_STOPPING_RAIN = 2;

	private static final String MOISTENER_SPEED =
		"ForestryMoistenerSpeed";

	private static final String MOISTENER_RESOURCE_PROGRESS =
		"ForestryMoistenerResourceProgress";

	private static final String SQUEEZER_REMNANT =
		"ForestrySqueezerRemnant";

	private static final String SQUEEZER_REMNANT_CHANCE =
		"ForestrySqueezerRemnantChance";

	private static final String CENTRIFUGE_PRODUCTS =
		"ForestryCentrifugeProducts";

	private static final String PRODUCT_STACK = "Stack";
	private static final String PRODUCT_CHANCE = "Chance";

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

		Integer progress = getProgressPercent(tile);

		if (progress != null) {
			data.putInt(PROGRESS, progress);
		}

		appendMachineData(data, tile, accessor);

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

		data.putInt(ERROR_COUNT, errors.size());

		for (int i = 0; i < errors.size(); i++) {
			data.putString(
				ERROR_PREFIX + i,
				errors.get(i).getDescriptionTranslationKey()
			);
		}
	}

	private static void appendMachineData(
		CompoundTag data,
		TileForestry tile,
		BlockAccessor accessor
	) {
		if (tile instanceof TileFabricator fabricator) {
			int heat = fabricator.getHeat();
			int meltingPoint = fabricator.getCurrentMeltingPoint();

			if (heat > 0 || meltingPoint > 0) {
				data.putInt(FABRICATOR_HEAT, heat);
				data.putInt(FABRICATOR_MAX_HEAT, fabricator.getMaxHeat());

				if (meltingPoint > 0) {
					data.putInt(FABRICATOR_MELTING_POINT, meltingPoint);
				}
			}

			putStack(
				data,
				FABRICATOR_RECIPE,
				fabricator.getCurrentResult(),
				accessor
			);
		}

		if (tile instanceof TileFermenter fermenter) {
			int fuel = fermenter.getBurnTimeRemainingScaled(100);

			if (fuel > 0) {
				data.putInt(FERMENTER_FUEL, fuel);
			}
		}

		if (tile instanceof TileCarpenter carpenter) {
			putStack(
				data,
				CARPENTER_RECIPE,
				carpenter.getCurrentResult(),
				accessor
			);
		}

		if (tile instanceof TileBottler bottler && bottler.hasCurrentRecipe()) {
			data.putInt(
				BOTTLER_MODE,
				bottler.isCurrentRecipeFilling()
					? BOTTLER_MODE_FILLING
					: BOTTLER_MODE_EMPTYING
			);
		}

		if (tile instanceof TileMillRainmaker rainmaker && rainmaker.charge > 0) {
			data.putInt(
				RAINMAKER_STATUS,
				rainmaker.isReverse()
					? RAINMAKER_STOPPING_RAIN
					: RAINMAKER_STARTING_RAIN
			);
		}

		if (tile instanceof TileMoistener moistener) {
			data.putInt(
				MOISTENER_SPEED,
				moistener.getCurrentSpeed()
			);

			if (moistener.isWorking()) {
				int remaining = moistener.getConsumptionProgressScaled(100);

				data.putInt(
					MOISTENER_RESOURCE_PROGRESS,
					Mth.clamp(100 - remaining, 0, 100)
				);
			}
		}

		if (tile instanceof TileSqueezer squeezer) {
			ISqueezerRecipe recipe = squeezer.getCurrentRecipe();

			if (recipe != null && !recipe.getRemnants().isEmpty()) {
				putStack(
					data,
					SQUEEZER_REMNANT,
					recipe.getRemnants(),
					accessor
				);

				data.putInt(
					SQUEEZER_REMNANT_CHANCE,
					toPercent(
						recipe.getRemnantsChance()
							* squeezer.getOutputMultiplier()
					)
				);
			}
		}

		if (tile instanceof TileCentrifuge centrifuge) {
			ICentrifugeRecipe recipe = centrifuge.getCurrentRecipe();

			if (recipe != null) {
				ListTag products = new ListTag();

				for (IProduct product : recipe.getAllProducts()) {
					ItemStack stack = product.createStack();

					if (stack.isEmpty()) {
						continue;
					}

					CompoundTag entry = new CompoundTag();

					entry.put(
						PRODUCT_STACK,
						stack.save(
							accessor.getLevel().registryAccess(),
							new CompoundTag()
						)
					);

					entry.putInt(
						PRODUCT_CHANCE,
						toPercent(
							product.chance()
								* centrifuge.getOutputMultiplier()
						)
					);

					products.add(entry);
				}

				if (!products.isEmpty()) {
					data.put(CENTRIFUGE_PRODUCTS, products);
				}
			}
		}
	}

	private static int toPercent(double chance) {
		return Mth.clamp(
			(int) Math.round(chance * 100.0D),
			0,
			100
		);
	}

	private static void putStack(
		CompoundTag data,
		String key,
		ItemStack stack,
		BlockAccessor accessor
	) {
		if (stack.isEmpty()) {
			return;
		}

		data.put(
			key,
			stack.save(
				accessor.getLevel().registryAccess(),
				new CompoundTag()
			)
		);
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

		MachineConfig machine = getMachineConfig(tile);

		if (machine == null) {
			return;
		}

		CompoundTag data = accessor.getServerData();
		boolean showDetails = accessor.showDetails();

		if (!isVisible(config, machine.details(), showDetails)) {
			return;
		}

		if (
			isVisible(config, machine.progress(), showDetails)
				&& data.contains(PROGRESS, Tag.TAG_INT)
		) {
			tooltip.add(
				Component.translatable(
					"jade.forestry.machine.progress",
					data.getInt(PROGRESS)
				)
			);
		}

		if (isVisible(config, machine.errors(), showDetails)) {
			appendErrors(tooltip, data);
		}

		if (
			tile instanceof TileFabricator
				&& isVisible(config, FABRICATOR_HEAT_CONFIG, showDetails)
		) {
			appendFabricatorHeat(tooltip, data);
		}

		if (
			tile instanceof TileFabricator
				&& isVisible(config, FABRICATOR_RECIPE_CONFIG, showDetails)
		) {
			appendFabricatorRecipe(tooltip, data, accessor);
		}

		if (
			tile instanceof TileFermenter
				&& isVisible(config, FERMENTER_FUEL_CONFIG, showDetails)
				&& data.contains(FERMENTER_FUEL, Tag.TAG_INT)
		) {
			tooltip.add(
				Component.translatable(
					"jade.forestry.machine.fermenter.fuel",
					data.getInt(FERMENTER_FUEL)
				)
			);
		}

		if (
			tile instanceof TileCarpenter
				&& isVisible(config, CARPENTER_RECIPE_CONFIG, showDetails)
		) {
			appendCarpenterRecipe(tooltip, data, accessor);
		}

		if (
			tile instanceof TileBottler
				&& isVisible(config, BOTTLER_MODE_CONFIG, showDetails)
		) {
			appendBottlerMode(tooltip, data);
		}

		if (
			tile instanceof TileMillRainmaker
				&& isVisible(config, RAINMAKER_STATUS_CONFIG, showDetails)
		) {
			appendRainmakerStatus(tooltip, data);
		}

		if (
			tile instanceof TileMoistener
				&& isVisible(config, MOISTENER_SPEED_CONFIG, showDetails)
				&& data.contains(MOISTENER_SPEED, Tag.TAG_INT)
		) {
			tooltip.add(
				Component.translatable(
					"jade.forestry.machine.moistener.speed",
					data.getInt(MOISTENER_SPEED)
				)
			);
		}

		if (
			tile instanceof TileMoistener
				&& isVisible(config, MOISTENER_RESOURCE_PROGRESS_CONFIG, showDetails)
				&& data.contains(MOISTENER_RESOURCE_PROGRESS, Tag.TAG_INT)
		) {
			tooltip.add(
				Component.translatable(
					"jade.forestry.machine.moistener.resource_progress",
					data.getInt(MOISTENER_RESOURCE_PROGRESS)
				)
			);
		}

		if (
			tile instanceof TileSqueezer
				&& isVisible(config, SQUEEZER_REMNANT_CONFIG, showDetails)
		) {
			appendSqueezerRemnant(tooltip, data, accessor);
		}

		if (
			tile instanceof TileCentrifuge
				&& isVisible(config, CENTRIFUGE_PRODUCTS_CONFIG, showDetails)
		) {
			appendCentrifugeProducts(tooltip, data, accessor);
		}
	}

	@Nullable
	private static MachineConfig getMachineConfig(TileForestry tile) {
		if (tile instanceof TileFabricator) {
			return new MachineConfig(
				FABRICATOR_DETAILS,
				FABRICATOR_PROGRESS,
				FABRICATOR_ERRORS
			);
		}

		if (tile instanceof TileSqueezer) {
			return new MachineConfig(
				SQUEEZER_DETAILS,
				SQUEEZER_PROGRESS,
				SQUEEZER_ERRORS
			);
		}

		if (tile instanceof TileCentrifuge) {
			return new MachineConfig(
				CENTRIFUGE_DETAILS,
				CENTRIFUGE_PROGRESS,
				CENTRIFUGE_ERRORS
			);
		}

		if (tile instanceof TileStill) {
			return new MachineConfig(
				STILL_DETAILS,
				STILL_PROGRESS,
				STILL_ERRORS
			);
		}

		if (tile instanceof TileFermenter) {
			return new MachineConfig(
				FERMENTER_DETAILS,
				FERMENTER_PROGRESS,
				FERMENTER_ERRORS
			);
		}

		if (tile instanceof TileCarpenter) {
			return new MachineConfig(
				CARPENTER_DETAILS,
				CARPENTER_PROGRESS,
				CARPENTER_ERRORS
			);
		}

		if (tile instanceof TileBottler) {
			return new MachineConfig(
				BOTTLER_DETAILS,
				BOTTLER_PROGRESS,
				BOTTLER_ERRORS
			);
		}

		if (tile instanceof TileMillRainmaker) {
			return new MachineConfig(
				RAINMAKER_DETAILS,
				RAINMAKER_PROGRESS,
				RAINMAKER_ERRORS
			);
		}

		if (tile instanceof TileMoistener) {
			return new MachineConfig(
				MOISTENER_DETAILS,
				MOISTENER_PROGRESS,
				MOISTENER_ERRORS
			);
		}

		return null;
	}

	private record MachineConfig(
		ResourceLocation details,
		ResourceLocation progress,
		ResourceLocation errors
	) {
	}

	private static boolean isVisible(
		IPluginConfig config,
		ResourceLocation key,
		boolean showDetails
	) {
		Visibility visibility = config.getEnum(key);
		return visibility.isVisible(showDetails);
	}

	private static void appendFabricatorHeat(
		ITooltip tooltip,
		CompoundTag data
	) {
		if (
			!data.contains(FABRICATOR_HEAT, Tag.TAG_INT)
				|| !data.contains(FABRICATOR_MAX_HEAT, Tag.TAG_INT)
		) {
			return;
		}

		if (data.contains(FABRICATOR_MELTING_POINT, Tag.TAG_INT)) {
			tooltip.add(
				Component.translatable(
					"jade.forestry.machine.fabricator.heat_melting",
					data.getInt(FABRICATOR_HEAT),
					data.getInt(FABRICATOR_MAX_HEAT),
					data.getInt(FABRICATOR_MELTING_POINT)
				)
			);
		} else {
			tooltip.add(
				Component.translatable(
					"jade.forestry.machine.fabricator.heat",
					data.getInt(FABRICATOR_HEAT),
					data.getInt(FABRICATOR_MAX_HEAT)
				)
			);
		}
	}

	private static void appendFabricatorRecipe(
		ITooltip tooltip,
		CompoundTag data,
		BlockAccessor accessor
	) {
		ItemStack stack = readStack(data, FABRICATOR_RECIPE, accessor);

		if (stack.isEmpty()) {
			return;
		}

		appendItemLine(
			tooltip,
			stack,
			Component.translatable(
				"jade.forestry.machine.fabricator.recipe",
				stack.getCount(),
				stack.getHoverName()
			)
		);
	}

	private static void appendCarpenterRecipe(
		ITooltip tooltip,
		CompoundTag data,
		BlockAccessor accessor
	) {
		ItemStack stack = readStack(data, CARPENTER_RECIPE, accessor);

		if (stack.isEmpty()) {
			return;
		}

		appendItemLine(
			tooltip,
			stack,
			Component.translatable(
				"jade.forestry.machine.carpenter.recipe",
				stack.getCount(),
				stack.getHoverName()
			)
		);
	}

	private static void appendBottlerMode(
		ITooltip tooltip,
		CompoundTag data
	) {
		if (!data.contains(BOTTLER_MODE, Tag.TAG_INT)) {
			return;
		}

		int mode = data.getInt(BOTTLER_MODE);
		Component modeName;

		if (mode == BOTTLER_MODE_FILLING) {
			modeName = Component.translatable(
				"jade.forestry.machine.bottler.filling"
			);
		} else if (mode == BOTTLER_MODE_EMPTYING) {
			modeName = Component.translatable(
				"jade.forestry.machine.bottler.emptying"
			);
		} else {
			return;
		}

		tooltip.add(
			Component.translatable(
				"jade.forestry.machine.bottler.mode",
				modeName
			)
		);
	}

	private static void appendRainmakerStatus(
		ITooltip tooltip,
		CompoundTag data
	) {
		if (!data.contains(RAINMAKER_STATUS, Tag.TAG_INT)) {
			return;
		}

		int status = data.getInt(RAINMAKER_STATUS);
		Component statusName;

		if (status == RAINMAKER_STARTING_RAIN) {
			statusName = Component.translatable(
				"jade.forestry.machine.rainmaker.starting"
			);
		} else if (status == RAINMAKER_STOPPING_RAIN) {
			statusName = Component.translatable(
				"jade.forestry.machine.rainmaker.stopping"
			);
		} else {
			return;
		}

		tooltip.add(
			Component.translatable(
				"jade.forestry.machine.rainmaker.status",
				statusName
			)
		);
	}

	private static void appendSqueezerRemnant(
		ITooltip tooltip,
		CompoundTag data,
		BlockAccessor accessor
	) {
		ItemStack stack = readStack(data, SQUEEZER_REMNANT, accessor);

		if (
			stack.isEmpty()
				|| !data.contains(SQUEEZER_REMNANT_CHANCE, Tag.TAG_INT)
		) {
			return;
		}

		appendItemLine(
			tooltip,
			stack,
			Component.translatable(
				"jade.forestry.machine.squeezer.remnant",
				stack.getCount(),
				stack.getHoverName(),
				data.getInt(SQUEEZER_REMNANT_CHANCE)
			)
		);
	}

	private static void appendCentrifugeProducts(
		ITooltip tooltip,
		CompoundTag data,
		BlockAccessor accessor
	) {
		if (!data.contains(CENTRIFUGE_PRODUCTS, Tag.TAG_LIST)) {
			return;
		}

		ListTag products = data.getList(
			CENTRIFUGE_PRODUCTS,
			Tag.TAG_COMPOUND
		);

		if (products.isEmpty()) {
			return;
		}

		tooltip.add(
			Component.translatable(
				"jade.forestry.machine.centrifuge.products"
			).withStyle(ChatFormatting.GRAY)
		);

		for (int i = 0; i < products.size(); i++) {
			CompoundTag entry = products.getCompound(i);

			if (
				!entry.contains(PRODUCT_STACK)
					|| !entry.contains(PRODUCT_CHANCE, Tag.TAG_INT)
			) {
				continue;
			}

			ItemStack stack = ItemStack.parse(
				accessor.getLevel().registryAccess(),
				entry.get(PRODUCT_STACK)
			).orElse(ItemStack.EMPTY);

			if (stack.isEmpty()) {
				continue;
			}

			appendItemLine(
				tooltip,
				stack,
				Component.translatable(
					"jade.forestry.machine.centrifuge.product",
					stack.getCount(),
					stack.getHoverName(),
					entry.getInt(PRODUCT_CHANCE)
				)
			);
		}
	}

	private static ItemStack readStack(
		CompoundTag data,
		String key,
		BlockAccessor accessor
	) {
		Tag tag = data.get(key);

		if (tag == null) {
			return ItemStack.EMPTY;
		}

		return ItemStack.parse(
			accessor.getLevel().registryAccess(),
			tag
		).orElse(ItemStack.EMPTY);
	}

	private static void appendItemLine(
		ITooltip tooltip,
		ItemStack stack,
		Component text
	) {
		IElementHelper helper = IElementHelper.get();

		tooltip.add(
			List.<IElement>of(
				helper.smallItem(
					stack.copyWithCount(1)
				),
				helper.text(text)
			)
		);
	}

	private static void appendErrors(
		ITooltip tooltip,
		CompoundTag data
	) {
		int count = data.getInt(ERROR_COUNT);

		for (int i = 0; i < count; i++) {
			String translationKey = data.getString(ERROR_PREFIX + i);

			if (!translationKey.isEmpty()) {
				tooltip.add(
					Component.literal("⚠ ")
						.append(
							Component.translatable(translationKey)
						)
						.withStyle(ChatFormatting.RED)
				);
			}
		}
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
		TileForestry tile
	) {
		if (tile instanceof TileFabricator fabricator) {
			return getFabricatorProgress(fabricator);
		}

		if (tile instanceof TileFermenter fermenter) {
			return getFermenterProgress(fermenter);
		}

		if (tile instanceof TileMoistener moistener) {
			return getMoistenerProgress(moistener);
		}

		if (tile instanceof TileMillRainmaker rainmaker) {
			return getRainmakerProgress(rainmaker);
		}

		if (tile instanceof TilePowered powered) {
			return getPoweredProgress(powered);
		}

		return null;
	}

	@Nullable
	private static Integer getFabricatorProgress(
		TileFabricator fabricator
	) {
		int meltingPoint = fabricator.getCurrentMeltingPoint();

		if (meltingPoint > 0) {
			return Mth.clamp(
				Math.round(
					fabricator.getHeat()
						* 100.0F
						/ meltingPoint
				),
				0,
				100
			);
		}

		return getPoweredProgress(fabricator);
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
		int remaining = fermenter.getFermentationProgressScaled(10000);

		if (remaining <= 0) {
			return null;
		}

		return Mth.clamp(
			Math.round(
				100.0F
					- remaining / 100.0F
			),
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

		int remaining = moistener.getProductionProgressScaled(10000);

		return Mth.clamp(
			Math.round(
				100.0F
					- remaining / 100.0F
			),
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

		int completedCycles = rainmaker.charge - 1;

		if (rainmaker.stage == 2) {
			completedCycles--;
		}

		float currentCycle = completedCycles
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

	@Override
	public ResourceLocation getUid() {
		return ForestryConstants.forestry(
			"machine_details"
		);
	}
}
