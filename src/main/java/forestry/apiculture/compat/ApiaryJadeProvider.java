package forestry.apiculture.compat;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.core.IError;
import forestry.api.genetics.ClimateHelper;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.apiculture.InventoryBeeHousing;
import forestry.apiculture.inventory.InventoryApiary;
import forestry.apiculture.tiles.TileApiary;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public enum ApiaryJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
	INSTANCE;

	public enum Visibility {
		OFF,
		ON,
		SHIFT;

		public boolean isVisible(boolean showDetails) {
			return this == ON || (this == SHIFT && showDetails);
		}
	}

	private enum BeeStatsView {
		NONE,
		COMPACT,
		EXPANDED
	}

	/*
	 * Dotted keys make these sub-options of the main
	 * forestry:apiary_details Jade provider.
	 */
	public static final ResourceLocation SHOW_PROGRESS =
		ForestryConstants.forestry("apiary_details.progress");

	public static final ResourceLocation SHOW_ERRORS =
		ForestryConstants.forestry("apiary_details.errors");

	public static final ResourceLocation SHOW_QUEEN =
		ForestryConstants.forestry("apiary_details.queen");

	public static final ResourceLocation SHOW_STOCK =
		ForestryConstants.forestry("apiary_details.stock");

	public static final ResourceLocation SHOW_DRONES =
		ForestryConstants.forestry("apiary_details.drones");

	public static final ResourceLocation COMPACT_STATS =
		ForestryConstants.forestry("apiary_details.compact_stats");

	public static final ResourceLocation EXPANDED_STATS =
		ForestryConstants.forestry("apiary_details.expanded_stats");

	public static final ResourceLocation SHOW_FRAMES =
		ForestryConstants.forestry("apiary_details.frames");

	public static final ResourceLocation SHOW_OUTPUT =
		ForestryConstants.forestry("apiary_details.output");

	// Server data
	private static final String PROGRESS = "ForestryApiaryProgress";

	private static final String QUEEN = "ForestryApiaryQueen";
	private static final String DRONE = "ForestryApiaryDrone";

	private static final String FRAMES = "ForestryApiaryFrames";
	private static final String OUTPUTS = "ForestryApiaryOutputs";

	private static final String ERROR_COUNT = "ForestryApiaryErrorCount";
	private static final String ERROR_PREFIX = "ForestryApiaryError";

	// Compact bee data
	private static final String SPECIES = "Species";
	private static final String STAGE = "Stage";
	private static final String COUNT = "Count";
	private static final String ANALYZED = "Analyzed";
	private static final String PRISTINE = "Pristine";

	private static final String LIFESPAN = "Lifespan";
	private static final String SPEED = "Speed";
	private static final String FERTILITY = "Fertility";
	private static final String POLLINATION = "Pollination";
	private static final String TERRITORY = "Territory";
	private static final String FLOWERS = "Flowers";
	private static final String TEMPERATURE_TOLERANCE = "TemperatureTolerance";
	private static final String HUMIDITY_TOLERANCE = "HumidityTolerance";

	private static final String TRANSLATION = "Translation";
	private static final String FALLBACK = "Fallback";

	// Output representation
	private static final String OUTPUT_KIND = "Kind";
	private static final String OUTPUT_BEE = "Bee";
	private static final String OUTPUT_STACK = "Stack";
	private static final String OUTPUT_KIND_BEE = "bee";
	private static final String OUTPUT_KIND_STACK = "stack";

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		if (!(accessor.getBlockEntity() instanceof TileApiary apiary)) {
			return;
		}

		InventoryApiary inventory = (InventoryApiary) apiary.getBeeInventory();

		ItemStack queenStack = inventory.getQueen();
		ILifeStage queenStage = IIndividualHandlerItem.getLifeStage(queenStack);

		int rawProgress = apiary.getBeekeepingLogic().getBeeProgressPercent();
		int progress;

		if (queenStage == BeeLifeStage.QUEEN) {
			/*
			 * Forestry's queen bar represents remaining lifespan and
			 * decreases from 100% to 0%. Jade displays conventional
			 * completion progress instead.
			 */
			progress = 100 - rawProgress;
		} else {
			/*
			 * Princess mating progress already increases from
			 * 0% to 100%.
			 */
			progress = rawProgress;
		}

		data.putInt(
			PROGRESS,
			Math.max(0, Math.min(100, progress))
		);

		putBee(data, QUEEN, queenStack, true);
		putBee(data, DRONE, inventory.getDrone(), true);

		putFrames(data, inventory, accessor);
		putOutputs(data, inventory, accessor);

		List<IError> errors = apiary.getErrorLogic()
			.getErrors()
			.stream()
			.sorted(Comparator.comparing(error -> error.getId().toString()))
			.toList();

		data.putInt(ERROR_COUNT, errors.size());

		for (int i = 0; i < errors.size(); i++) {
			data.putString(
				ERROR_PREFIX + i,
				errors.get(i).getDescriptionTranslationKey()
			);
		}
	}

	private static void putBee(
		CompoundTag data,
		String key,
		ItemStack stack,
		boolean includeStats
	) {
		CompoundTag beeData = createBeeData(stack, includeStats);

		if (beeData != null) {
			data.put(key, beeData);
		}
	}

	private static CompoundTag createBeeData(
		ItemStack stack,
		boolean includeStats
	) {
		if (stack.isEmpty()) {
			return null;
		}

		ILifeStage lifeStage = IIndividualHandlerItem.getLifeStage(stack);

		if (!(lifeStage instanceof BeeLifeStage stage)) {
			return null;
		}

		if (!(IIndividualHandlerItem.getIndividual(stack) instanceof IBee bee)) {
			return null;
		}

		CompoundTag data = new CompoundTag();

		data.putString(
			SPECIES,
			bee.getSpecies().id().toString()
		);

		data.putString(
			STAGE,
			stage.getSerializedName()
		);

		data.putInt(
			COUNT,
			stack.getCount()
		);

		if (!includeStats) {
			return data;
		}

		data.putBoolean(
			ANALYZED,
			bee.isAnalyzed()
		);

		data.putBoolean(
			PRISTINE,
			bee.isPristine()
		);

		if (bee.isAnalyzed()) {
			IGenome genome = bee.getGenome();

			putAllele(
				data,
				LIFESPAN,
				genome,
				BeeChromosomes.LIFESPAN
			);

			putAllele(
				data,
				SPEED,
				genome,
				BeeChromosomes.SPEED
			);

			putAllele(
				data,
				FERTILITY,
				genome,
				BeeChromosomes.FERTILITY
			);

			putAllele(
				data,
				POLLINATION,
				genome,
				BeeChromosomes.POLLINATION
			);

			putAllele(
				data,
				TERRITORY,
				genome,
				BeeChromosomes.TERRITORY
			);

			putAllele(
				data,
				FLOWERS,
				genome,
				BeeChromosomes.FLOWER_TYPE
			);

			putAllele(
				data,
				TEMPERATURE_TOLERANCE,
				genome,
				BeeChromosomes.TEMPERATURE_TOLERANCE
			);

			putAllele(
				data,
				HUMIDITY_TOLERANCE,
				genome,
				BeeChromosomes.HUMIDITY_TOLERANCE
			);
		}

		return data;
	}

	private static <V> void putAllele(
		CompoundTag data,
		String key,
		IGenome genome,
		IChromosome<V> chromosome
	) {
		V value = genome.getActiveValue(chromosome);

		CompoundTag allele = new CompoundTag();

		allele.putString(
			TRANSLATION,
			chromosome.translationKey(value)
		);

		allele.putString(
			FALLBACK,
			String.valueOf(value)
		);

		data.put(
			key,
			allele
		);
	}

	private static void putFrames(
		CompoundTag data,
		InventoryApiary inventory,
		BlockAccessor accessor
	) {
		ListTag stacks = new ListTag();

		for (
			int slot = InventoryApiary.SLOT_FRAMES_1;
			slot < InventoryApiary.SLOT_FRAMES_1
				+ InventoryApiary.SLOT_FRAMES_COUNT;
			slot++
		) {
			ItemStack stack = inventory.getItem(slot);

			if (stack.isEmpty()) {
				continue;
			}

			stacks.add(
				stack.save(
					accessor.getLevel().registryAccess(),
					new CompoundTag()
				)
			);
		}

		if (!stacks.isEmpty()) {
			data.put(
				FRAMES,
				stacks
			);
		}
	}

	private static void putOutputs(
		CompoundTag data,
		InventoryApiary inventory,
		BlockAccessor accessor
	) {
		ListTag outputs = new ListTag();

		for (
			int slot = InventoryBeeHousing.SLOT_PRODUCT_1;
			slot < InventoryBeeHousing.SLOT_PRODUCT_1
				+ InventoryBeeHousing.SLOT_PRODUCT_COUNT;
			slot++
		) {
			ItemStack stack = inventory.getItem(slot);

			if (stack.isEmpty()) {
				continue;
			}

			CompoundTag entry = new CompoundTag();

			/*
			 * Genetic bees are encoded as a very small species/stage/count
			 * snapshot instead of serializing their complete genome.
			 */
			CompoundTag beeData = createBeeData(
				stack,
				false
			);

			if (beeData != null) {
				entry.putString(
					OUTPUT_KIND,
					OUTPUT_KIND_BEE
				);

				entry.put(
					OUTPUT_BEE,
					beeData
				);
			} else {
				entry.putString(
					OUTPUT_KIND,
					OUTPUT_KIND_STACK
				);

				entry.put(
					OUTPUT_STACK,
					stack.save(
						accessor.getLevel().registryAccess(),
						new CompoundTag()
					)
				);
			}

			outputs.add(entry);
		}

		if (!outputs.isEmpty()) {
			data.put(
				OUTPUTS,
				outputs
			);
		}
	}

	@Override
	public void appendTooltip(
		ITooltip tooltip,
		BlockAccessor accessor,
		IPluginConfig config
	) {
		if (!(accessor.getBlockEntity() instanceof TileApiary)) {
			return;
		}

		CompoundTag data = accessor.getServerData();
		boolean showDetails = accessor.showDetails();

		BeeStatsView statsView = resolveBeeStatsView(
			config,
			showDetails
		);

		if (
			isVisible(
				config,
				SHOW_PROGRESS,
				showDetails
			)
				&& data.contains(
					QUEEN,
					Tag.TAG_COMPOUND
				)
		) {
			tooltip.add(
				Component.translatable(
					"jade.forestry.apiary.progress",
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

		if (
			isVisible(
				config,
				SHOW_QUEEN,
				showDetails
			)
				&& data.contains(
					QUEEN,
					Tag.TAG_COMPOUND
				)
		) {
			appendQueen(
				tooltip,
				data.getCompound(QUEEN),
				statsView,
				isVisible(
					config,
					SHOW_STOCK,
					showDetails
				)
			);
		}

		if (
			isVisible(
				config,
				SHOW_DRONES,
				showDetails
			)
				&& data.contains(
					DRONE,
					Tag.TAG_COMPOUND
				)
		) {
			appendDrone(
				tooltip,
				data.getCompound(DRONE),
				statsView
			);
		}

		if (
			isVisible(
				config,
				SHOW_FRAMES,
				showDetails
			)
		) {
			appendFrames(
				tooltip,
				data,
				accessor
			);
		}

		if (
			isVisible(
				config,
				SHOW_OUTPUT,
				showDetails
			)
		) {
			appendOutputs(
				tooltip,
				data,
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

	/*
	 * Stats use slightly different precedence rules because compact and
	 * expanded are alternate views of the same information rather than two
	 * independent blocks.
	 *
	 * Default:
	 *   Compact  = ON
	 *   Expanded = SHIFT
	 *
	 * Normal:
	 *   compact
	 *
	 * Shift:
	 *   expanded
	 *
	 * A compact SHIFT setting explicitly wins over expanded SHIFT. This lets
	 * the user request compact stats only while holding Shift without the
	 * expanded view replacing them.
	 */
	private static BeeStatsView resolveBeeStatsView(
		IPluginConfig config,
		boolean showDetails
	) {
		Visibility compact = config.getEnum(
			COMPACT_STATS
		);

		Visibility expanded = config.getEnum(
			EXPANDED_STATS
		);

		if (showDetails) {
			if (compact == Visibility.SHIFT) {
				return BeeStatsView.COMPACT;
			}

			if (expanded == Visibility.SHIFT) {
				return BeeStatsView.EXPANDED;
			}
		}

		if (expanded == Visibility.ON) {
			return BeeStatsView.EXPANDED;
		}

		if (compact == Visibility.ON) {
			return BeeStatsView.COMPACT;
		}

		return BeeStatsView.NONE;
	}

	private static void appendErrors(
		ITooltip tooltip,
		CompoundTag data
	) {
		int count = data.getInt(ERROR_COUNT);

		for (int i = 0; i < count; i++) {
			String translationKey = data.getString(
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

	private static void appendQueen(
		ITooltip tooltip,
		CompoundTag beeData,
		BeeStatsView statsView,
		boolean showStock
	) {
		BeeLifeStage stage = readBeeStage(beeData);

		if (
			stage != BeeLifeStage.QUEEN
				&& stage != BeeLifeStage.PRINCESS
		) {
			return;
		}

		ItemStack stack = createDisplayBeeStack(
			beeData
		);

		if (stack.isEmpty()) {
			return;
		}

		Component line;

		if (showStock) {
			Component stock = Component.translatable(
				beeData.getBoolean(PRISTINE)
					? "for.bees.stock.pristine"
					: "for.bees.stock.ignoble"
			);

			String key = stage == BeeLifeStage.PRINCESS
				? "jade.forestry.apiary.princess"
				: "jade.forestry.apiary.queen";

			line = Component.translatable(
				key,
				stack.getHoverName(),
				stock
			);
		} else {
			String key = stage == BeeLifeStage.PRINCESS
				? "jade.forestry.apiary.princess_no_stock"
				: "jade.forestry.apiary.queen_no_stock";

			line = Component.translatable(
				key,
				stack.getHoverName()
			);
		}

		appendItemLine(
			tooltip,
			stack,
			line
		);

		if (
			beeData.getBoolean(ANALYZED)
				&& statsView != BeeStatsView.NONE
		) {
			appendBeeStats(
				tooltip,
				beeData,
				statsView
			);
		}
	}

	private static void appendDrone(
		ITooltip tooltip,
		CompoundTag beeData,
		BeeStatsView statsView
	) {
		if (
			readBeeStage(beeData)
				!= BeeLifeStage.DRONE
		) {
			return;
		}

		ItemStack stack = createDisplayBeeStack(
			beeData
		);

		if (stack.isEmpty()) {
			return;
		}

		appendItemLine(
			tooltip,
			stack,
			Component.translatable(
				"jade.forestry.apiary.drones",
				beeData.getInt(COUNT),
				stack.getHoverName()
			)
		);

		if (
			beeData.getBoolean(ANALYZED)
				&& statsView != BeeStatsView.NONE
		) {
			appendBeeStats(
				tooltip,
				beeData,
				statsView
			);
		}
	}

	private static void appendBeeStats(
		ITooltip tooltip,
		CompoundTag beeData,
		BeeStatsView statsView
	) {
		if (statsView == BeeStatsView.EXPANDED) {
			appendPrimaryStats(
				tooltip,
				beeData
			);

			return;
		}

		if (statsView == BeeStatsView.COMPACT) {
			tooltip.add(
				Component.translatable(
					"jade.forestry.apiary.stats.compact",
					readAllele(
						beeData,
						LIFESPAN
					),
					readAllele(
						beeData,
						SPEED
					),
					readAllele(
						beeData,
						FERTILITY
					)
				).withStyle(
					ChatFormatting.GRAY
				)
			);
		}
	}

	private static void appendPrimaryStats(
		ITooltip tooltip,
		CompoundTag beeData
	) {
		tooltip.add(
			Component.translatable(
				"jade.forestry.apiary.stats.core",
				readAllele(
					beeData,
					LIFESPAN
				),
				readAllele(
					beeData,
					SPEED
				),
				readAllele(
					beeData,
					FERTILITY
				)
			).withStyle(
				ChatFormatting.GRAY
			)
		);

		tooltip.add(
			Component.translatable(
				"jade.forestry.apiary.stats.work",
				readAllele(
					beeData,
					POLLINATION
				),
				readAllele(
					beeData,
					TERRITORY
				),
				readAllele(
					beeData,
					FLOWERS
				)
			).withStyle(
				ChatFormatting.GRAY
			)
		);

		ResourceLocation speciesId =
			ResourceLocation.tryParse(
				beeData.getString(SPECIES)
			);

		if (speciesId == null) {
			return;
		}

		IBeeSpecies species;

		try {
			species = SpeciesUtil.getBeeSpecies(
				speciesId
			);
		} catch (RuntimeException ignored) {
			return;
		}

		tooltip.add(
			Component.translatable(
				"jade.forestry.apiary.stats.climate",
				ClimateHelper.toDisplay(
					species.getTemperature()
				),
				readAllele(
					beeData,
					TEMPERATURE_TOLERANCE
				),
				ClimateHelper.toDisplay(
					species.getHumidity()
				),
				readAllele(
					beeData,
					HUMIDITY_TOLERANCE
				)
			).withStyle(
				ChatFormatting.GRAY
			)
		);
	}

	private static Component readAllele(
		CompoundTag data,
		String key
	) {
		if (
			!data.contains(
				key,
				Tag.TAG_COMPOUND
			)
		) {
			return Component.empty();
		}

		CompoundTag allele = data.getCompound(key);

		return Component.translatableWithFallback(
			allele.getString(TRANSLATION),
			allele.getString(FALLBACK)
		);
	}

	private static void appendFrames(
		ITooltip tooltip,
		CompoundTag data,
		BlockAccessor accessor
	) {
		List<ItemStack> frames = readStacks(
			data,
			FRAMES,
			accessor
		);

		if (frames.isEmpty()) {
			return;
		}

		tooltip.add(
			Component.translatable(
				"jade.forestry.apiary.frames"
			).withStyle(
				ChatFormatting.GRAY
			)
		);

		for (ItemStack frame : frames) {
			Component text;

			if (
				frame.isDamageableItem()
					&& frame.getMaxDamage() > 0
			) {
				int durability = Math.round(
					(frame.getMaxDamage()
						- frame.getDamageValue())
						* 100.0F
						/ frame.getMaxDamage()
				);

				text = Component.translatable(
					"jade.forestry.apiary.frame_durability",
					frame.getHoverName(),
					durability
				);
			} else {
				text = Component.translatable(
					"jade.forestry.apiary.frame",
					frame.getHoverName()
				);
			}

			appendItemLine(
				tooltip,
				frame,
				text
			);
		}
	}

	private static void appendOutputs(
		ITooltip tooltip,
		CompoundTag data,
		BlockAccessor accessor
	) {
		List<ItemStack> outputs = mergeStacks(
			readOutputs(
				data,
				accessor
			)
		);

		if (outputs.isEmpty()) {
			return;
		}

		tooltip.add(
			Component.translatable(
				"jade.forestry.apiary.output"
			).withStyle(
				ChatFormatting.GRAY
			)
		);

		for (ItemStack stack : outputs) {
			appendItemLine(
				tooltip,
				stack,
				Component.translatable(
					"jade.forestry.apiary.output_entry",
					stack.getCount(),
					stack.getHoverName()
				)
			);
		}
	}

	private static List<ItemStack> readOutputs(
		CompoundTag data,
		BlockAccessor accessor
	) {
		if (
			!data.contains(
				OUTPUTS,
				Tag.TAG_LIST
			)
		) {
			return List.of();
		}

		ListTag tags = data.getList(
			OUTPUTS,
			Tag.TAG_COMPOUND
		);

		List<ItemStack> stacks =
			new ArrayList<>(tags.size());

		for (int i = 0; i < tags.size(); i++) {
			CompoundTag entry = tags.getCompound(i);

			String kind = entry.getString(
				OUTPUT_KIND
			);

			ItemStack stack = ItemStack.EMPTY;

			if (
				OUTPUT_KIND_BEE.equals(kind)
					&& entry.contains(
						OUTPUT_BEE,
						Tag.TAG_COMPOUND
					)
			) {
				stack = createDisplayBeeStack(
					entry.getCompound(
						OUTPUT_BEE
					)
				);
			} else if (
				OUTPUT_KIND_STACK.equals(kind)
					&& entry.contains(
						OUTPUT_STACK
					)
			) {
				stack = ItemStack.parse(
					accessor.getLevel().registryAccess(),
					entry.get(OUTPUT_STACK)
				).orElse(
					ItemStack.EMPTY
				);
			}

			if (!stack.isEmpty()) {
				stacks.add(stack);
			}
		}

		return stacks;
	}

	private static List<ItemStack> readStacks(
		CompoundTag data,
		String key,
		BlockAccessor accessor
	) {
		if (
			!data.contains(
				key,
				Tag.TAG_LIST
			)
		) {
			return List.of();
		}

		ListTag tags = data.getList(
			key,
			Tag.TAG_COMPOUND
		);

		List<ItemStack> stacks =
			new ArrayList<>(tags.size());

		for (int i = 0; i < tags.size(); i++) {
			ItemStack stack = ItemStack.parse(
				accessor.getLevel().registryAccess(),
				tags.get(i)
			).orElse(
				ItemStack.EMPTY
			);

			if (!stack.isEmpty()) {
				stacks.add(stack);
			}
		}

		return stacks;
	}

	private static ItemStack createDisplayBeeStack(
		CompoundTag beeData
	) {
		ResourceLocation speciesId =
			ResourceLocation.tryParse(
				beeData.getString(SPECIES)
			);

		BeeLifeStage stage = readBeeStage(
			beeData
		);

		if (
			speciesId == null
				|| stage == null
		) {
			return ItemStack.EMPTY;
		}

		try {
			ItemStack stack =
				SpeciesUtil.BEE_TYPE.get()
					.createStack(
						speciesId,
						stage
					);

			stack.setCount(
				Math.max(
					1,
					beeData.getInt(COUNT)
				)
			);

			return stack;
		} catch (RuntimeException ignored) {
			return ItemStack.EMPTY;
		}
	}

	private static BeeLifeStage readBeeStage(
		CompoundTag beeData
	) {
		String name = beeData.getString(STAGE);

		for (BeeLifeStage stage : BeeLifeStage.values()) {
			if (
				stage.getSerializedName()
					.equals(name)
			) {
				return stage;
			}
		}

		return null;
	}

	private static void appendItemLine(
		ITooltip tooltip,
		ItemStack stack,
		Component text
	) {
		IElementHelper helper =
			IElementHelper.get();

		tooltip.add(
			List.<IElement>of(
				helper.smallItem(
					stack.copyWithCount(1)
				),
				helper.text(text)
			)
		);
	}

	private static List<ItemStack> mergeStacks(
		List<ItemStack> stacks
	) {
		List<ItemStack> merged =
			new ArrayList<>();

		for (ItemStack stack : stacks) {
			boolean found = false;

			for (ItemStack existing : merged) {
				if (
					ItemStack.isSameItemSameComponents(
						existing,
						stack
					)
				) {
					existing.grow(
						stack.getCount()
					);

					found = true;
					break;
				}
			}

			if (!found) {
				merged.add(
					stack.copy()
				);
			}
		}

		return merged;
	}

	@Override
	public ResourceLocation getUid() {
		return ForestryConstants.forestry(
			"apiary_details"
		);
	}
}