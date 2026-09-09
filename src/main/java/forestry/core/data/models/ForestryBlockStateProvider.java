package forestry.core.data.models;

import forestry.api.ForestryConstants;
import forestry.api.client.IForestryClientApi;
import forestry.apiculture.alveary.AlvearyBlock;
import forestry.apiculture.hives.BlockBeeHive;
import forestry.apiculture.hives.HiveBlockType;
import forestry.apiculture.apiary.ApicultureBlockType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.leaves.ForestryLeafType;
import forestry.arboriculture.features.CharcoalBlocks;
import forestry.core.content.burnbarrel.BlockBurnBarrel;
import forestry.core.content.decorative.BlockJumboCandle;
import forestry.core.content.decorative.BlockPlywoodBlock;
import forestry.core.content.decorative.BlockTypeBigCandle;
import forestry.core.content.decorative.BlockTypeJumboCandle;
import forestry.core.content.decorative.BlockTypeMetalPlating;
import forestry.core.content.resources.EnumResourceType;
import forestry.core.content.soil.BlockBogEarth;
import forestry.core.content.soil.BlockHumus;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.platform.block.BlockTypeCoreTesr;
import forestry.core.platform.block.IMachineProperties;
import forestry.core.platform.fluids.ForestryFluids;
import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.machines.blocks.BlockFactoryPlain;
import forestry.core.content.machines.blocks.BlockTypeFactoryPlain;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.platform.util.ModUtil;
import forestry.core.content.worktable.features.WorktableBlocks;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import forestry.core.platform.block.BlockBase;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ForestryBlockStateProvider extends BlockStateProvider {
	public ForestryBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
		super(output, ForestryConstants.MOD_ID, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels() {
		// Resources
		agingSoil(CoreBlocks.BOG_EARTH.block(), BlockBogEarth.MATURITY);
		agingSoil(CoreBlocks.HUMUS.block(), BlockHumus.DEGRADE);

		simpleBlock(CoreBlocks.APATITE_ORE.block());
		simpleBlock(CoreBlocks.DEEPSLATE_APATITE_ORE.block());
		simpleBlock(CoreBlocks.TIN_ORE.block());
		simpleBlock(CoreBlocks.DEEPSLATE_TIN_ORE.block());
		simpleBlock(CoreBlocks.RAW_TIN_BLOCK.block());
		generic3d(CoreBlocks.APATITE_ORE.block());
		generic3d(CoreBlocks.DEEPSLATE_APATITE_ORE.block());
		generic3d(CoreBlocks.TIN_ORE.block());
		generic3d(CoreBlocks.DEEPSLATE_TIN_ORE.block());
		generic3d(CoreBlocks.RAW_TIN_BLOCK.block());

		generic2d(CoreItems.RAW_TIN);
		generic2d(CoreItems.TIN_INGOT);
		generic2d(CoreItems.GEAR_TIN);
		generic2d(CoreItems.BRONZE_INGOT);
		generic2d(CoreItems.GEAR_BRONZE);
		generic2d(CoreItems.GEAR_COPPER);
		generic2d(CoreItems.GEAR_IRON);

		// Fluids (doesn't actually show in game, but silences the warning spam from Minecraft)
		for (ForestryFluids fluid : ForestryFluids.values()) {
			Block block = fluid.getFeature().fluidBlock().block();
			ModelFile blockModel = particleOnly(this, path(block), fluid.getFeature().properties().resources[0]);
			singleModelBlock(this, block, blockModel);
		}

		// Leaves (same as with fluids)
		for (ForestryLeafType treeType : ForestryLeafType.values()) {
			Block defaultBlock = ArboricultureBlocks.LEAVES_DEFAULT.get(treeType).block();
			Block defaultFruitBlock = ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(treeType).block();
			Block decorativeBlock = ArboricultureBlocks.LEAVES_DECORATIVE.get(treeType).block();
			ResourceLocation particle = IForestryClientApi.INSTANCE.getTreeManager().getLeafSprite(treeType.getIndividual().getSpecies()).get(false, true);
			ModelFile file = models().getBuilder(path(defaultBlock)).texture("particle", particle);

			singleModelBlock(this, defaultBlock, file);
			singleModelBlock(this, defaultFruitBlock, file);
			singleModelBlock(this, decorativeBlock, file);

			generic3d(defaultBlock);
			generic3d(defaultFruitBlock, defaultBlock);
			generic3d(decorativeBlock, defaultBlock);
		}
		singleModelBlock(this, ArboricultureBlocks.LEAVES.block(), particleOnly(this, ArboricultureBlocks.LEAVES.getName(), blockTexture(Blocks.OAK_LEAVES)));

		for (HiveBlockType type : HiveBlockType.values()) {
			BlockBeeHive feature = ApicultureBlocks.HIVE.get(type).block();
			String path = path(feature);

			ResourceLocation side = modBlock(this, "hive/" + type.getSerializedName() + "_side");
			ResourceLocation top = modBlock(this, "hive/" + type.getSerializedName() + "_top");
			ResourceLocation bottom = modBlock(this, "hive/" + type.getSerializedName() + "_bottom");

			singleModelBlock(this, feature, models().cubeBottomTop(path, side, bottom, top));
			generic3d(feature);
		}

		// Single-variant blocks migrated from hand-authored blockstates. Each renders one existing
		// (hand-authored) model for all states, mirroring the old {"variants":{"":{"model":...}}}.
		// Item models for these blocks stay hand-authored (custom display transforms), so no generic3d here.
		for (Block block : FactoryBlocks.TESR.blockArray()) existingModelBlock(block);          // rainmaker
		for (Block block : EnergyBlocks.ENGINES.blockArray()) existingModelBlock(block);        // biogas/clockwork/peat engine
		for (Block block : CoreBlocks.NATURALIST_CHEST.blockArray()) existingModelBlock(block); // apiarist/arborist/lepidopterist chest
		existingModelBlock(CharcoalBlocks.ASH.block());
		existingModelBlock(CharcoalBlocks.CHARCOAL.block());
		existingModelBlock(CharcoalBlocks.LOG_PILE.block());
		existingModelBlock(ArboricultureBlocks.SAPLING_GE.block());
		// Peat gets four random Y rotations of its one model rather than a single "" variant
		getVariantBuilder(CoreBlocks.PEAT.block()).partialState().setModels(
			ConfiguredModel.allYRotations(models().getExistingFile(modBlock(this, path(CoreBlocks.PEAT.block()))), 0, false)
		);

		// Comb blocks all share the block_bee_combs model.
		ModelFile combModel = models().getExistingFile(modBlock(this, "block_bee_combs"));
		for (Block block : ApicultureBlocks.COMB_BLOCK.blockArray()) singleModelBlock(this, block, combModel);

		// Resource storage blocks use block/storage/<type>.
		for (EnumResourceType type : EnumResourceType.values()) {
			existingModelBlock(CoreBlocks.RESOURCE_STORAGE.get(type).block(), "storage/" + type.getSerializedName());
		}

		// The wax blocks are not resource storage subtypes but share its model layout.
		existingModelBlock(ApicultureBlocks.WAX_BLOCK.block(), "storage/wax");
		existingModelBlock(ApicultureBlocks.REFRACTORY_WAX_BLOCK.block(), "storage/refractory_wax");

		// Alveary components (the single-variant subset of the alveary block group).
		existingModelBlock(ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.HYGROREGULATOR).block(), "apiculture/alveary_hygroregulator");
		existingModelBlock(ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.STABILIZER).block(), "apiculture/alveary_stabilizer");
		existingModelBlock(ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.SIEVE).block(), "apiculture/alveary_sieve");

		// Horizontal-facing machines migrated from hand-authored blockstates + models. Each is a
		// block/cube with per-face textures <prefix>.<n>, rotated by BlockBase.FACING. Item models
		// stay hand-authored (custom display transforms), so no generic3d here.
		horizontalMachine(this, ApicultureBlocks.BASE.get(ApicultureBlockType.APIARY).block(), "apiary", 0, 1, 2, 4, 4, 4, 4);
		horizontalMachine(this, ApicultureBlocks.BASE.get(ApicultureBlockType.BEE_HOUSE).block(), "beehouse", 0, 1, 2, 4, 4, 4, 4);
		horizontalMachine(this, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.FABRICATOR).block(), "thermionic_fabricator", 0, 1, 3, 2, 4, 4, 4);
		horizontalMachine(this, WorktableBlocks.WORKTABLE.block(), "worktable", 0, 1, 3, 2, 4, 4, 4);

		// The smelter keeps its hand-authored 1.20.1 model, so only the facing blockstate is generated.
		// Deviation from 1.20.1: that model parented block/machines/base_machine, whose only other job
		// was to hold two tank slices the smelter never shows. The body is inlined instead
		horizontalForestryBlock(this, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.SMELTER).block(), models().getExistingFile(modBlock(this, "smelter")));

		// The packaging machines moved from a BlockEntityRenderer to generated .json models + blockstates,
		// with the fluid tank levels expressed as blockstate properties. Each model parents
		// block/machines/base_machine and layers per-level tank textures on top of the base
		machineBlock(BlockTypeFactoryPlain.BOTTLER, IMachineProperties.TankLayout.RESOURCE);
		machineBlock(BlockTypeFactoryPlain.CARPENTER, IMachineProperties.TankLayout.RESOURCE);
		machineBlock(BlockTypeFactoryPlain.CENTRIFUGE, IMachineProperties.TankLayout.NONE);
		machineBlock(BlockTypeFactoryPlain.FERMENTER, IMachineProperties.TankLayout.BOTH);
		machineBlock(BlockTypeFactoryPlain.MOISTENER, IMachineProperties.TankLayout.RESOURCE);
		machineBlock(BlockTypeFactoryPlain.SQUEEZER, IMachineProperties.TankLayout.PRODUCT);
		machineBlock(BlockTypeFactoryPlain.STILL, IMachineProperties.TankLayout.BOTH);

		// The analyzer and escritoire keep their hand-authored 1.20.1 Blockbench models, so only the
		// facing blockstate is generated
		horizontalForestryBlock(this, CoreBlocks.BASE.get(BlockTypeCoreTesr.ANALYZER).block(), models().getExistingFile(modBlock(this, "analyzer")));
		horizontalForestryBlock(this, CoreBlocks.BASE.get(BlockTypeCoreTesr.ESCRITOIRE).block(), models().getExistingFile(modBlock(this, "escritoire")));

		// The burn barrel keeps its four hand-authored 1.20.1 models, one per LIT / HAS_ASH pair. Deviation from
		// 1.20.1: the blockstate was hand-authored there, this tree generates it. The item model stays hand-authored,
		// it parents the empty block model rather than a generated cube
		getVariantBuilder(CoreBlocks.BURN_BARREL.block()).forAllStates(state -> {
			boolean lit = state.getValue(BlockBurnBarrel.LIT);
			boolean hasAsh = state.getValue(BlockBurnBarrel.HAS_ASH);
			String name = hasAsh
				? (lit ? "burn_barrel_full_burning" : "burn_barrel_full")
				: (lit ? "burn_barrel_burning" : "burn_barrel_empty");
			return ConfiguredModel.builder().modelFile(models().getExistingFile(modBlock(this, name))).build();
		});

		// Turf. Both keep their hand-authored 1.20.1 models, an inline cube tinted by the grass colour, which no
		// vanilla parent writes. Deviation from 1.20.1: the two blockstates listed that one model four times,
		// once per y rotation, and both models wear one texture on all six faces, so the rotation was never
		// visible. One variant is written instead
		existingModelBlock(CoreBlocks.TURF_BLOCK.block());
		generic3d(CoreBlocks.TURF_BLOCK.block());
		existingModelBlock(CoreBlocks.TURF.block());
		generic3d(CoreBlocks.TURF.block());

		// Plywood and cork
		plywood();
		simpleBlock(CoreBlocks.CORK.block());
		generic3d(CoreBlocks.CORK.block());

		// Decorative stone and brick blocks
		simpleBlock(CoreBlocks.ASHEN_WAX_BLOCK.block());
		generic3d(CoreBlocks.ASHEN_WAX_BLOCK.block());
		simpleBlock(CoreBlocks.CRISPY_HONEY_BLOCK.block());
		generic3d(CoreBlocks.CRISPY_HONEY_BLOCK.block());

		for (CoreBlocks.StoneFamily family : CoreBlocks.DECORATIVE_FAMILIES) {
			stoneFamily(family);
		}
		// A chiseled brick block wears one texture on all six faces
		chiseledCube(CoreBlocks.CHISELED_ASH_BRICKS.block());
		chiseledCube(CoreBlocks.CHISELED_WAX_BRICKS.block());
		chiseledCube(CoreBlocks.CHISELED_REFRACTORY_WAX_BRICKS.block());
		// A chiseled stone block wears a separate top
		for (CoreBlocks.StoneSet set : CoreBlocks.STONE_SETS) {
			chiseledColumn(set.chiseled().block());
		}

		// Metal plating
		for (BlockTypeMetalPlating type : BlockTypeMetalPlating.values()) {
			Block block = CoreBlocks.METAL_PLATING.get(type).block();
			simpleBlock(block);
			generic3d(block);
		}

		// Candles
		for (BlockTypeJumboCandle type : BlockTypeJumboCandle.values()) {
			jumboCandle(type);
		}
		for (BlockTypeBigCandle type : BlockTypeBigCandle.values()) {
			bigCandle(type);
		}
		vanillaCandle(CoreBlocks.REFRACTORY_CANDLE.block());
		vanillaCandle(CoreBlocks.RAINBOW_CANDLE.block());
	}

	/**
	 * Emits the blockstate, the four shape models and the item model of one jumbo candle. Each shape gets its
	 * own model, since a candle in the middle of a stack shows neither a wick nor a melted top.
	 */
	private void jumboCandle(BlockTypeJumboCandle type) {
		String name = type.getSerializedName();
		Block candle = CoreBlocks.JUMBO_CANDLES.get(type).block();

		jumboCandleModel(name, "single", "bottom", "side", "top");
		jumboCandleModel(name, "top", "bottom_top", "side_top", "top");
		jumboCandleModel(name, "middle", "bottom_top", "side_middle", "middle_top");
		jumboCandleModel(name, "bottom", "bottom", "side_bottom", "bottom_top");

		getVariantBuilder(candle).forAllStates(state -> {
			String shape = state.getValue(BlockJumboCandle.SHAPE).getSerializedName();
			return ConfiguredModel.builder()
				.modelFile(models().getExistingFile(modBlock(this, "candles/" + name + "_jumbo_" + shape)))
				.build();
		});

		// Deviation from 1.20.1: the item models of the 38 jumbo and big candles were hand-authored one file
		// each there, with simpleBlockItem left commented out. They are generated here, from the same model
		// the block shows when it stands alone
		itemModels().withExistingParent(path(candle), modBlock(this, "candles/" + name + "_jumbo_single"));
	}

	/**
	 * Emits one shape model of one jumbo candle.
	 *
	 * @param name   The candle's colour, which the model and texture ids are built from
	 * @param shape  The shape of the block states the model covers
	 * @param bottom The texture on the bottom face
	 * @param side   The texture on the four sides, which is also the particle
	 * @param top    The texture on the top face
	 */
	private void jumboCandleModel(String name, String shape, String bottom, String side, String top) {
		ResourceLocation sideTexture = modBlock(this, "candles/" + name + "_" + side);

		models().withExistingParent("block/candles/" + name + "_jumbo_" + shape, modBlock(this, "candles/jumbo_" + shape))
			.texture("bottom", modBlock(this, "candles/" + name + "_" + bottom))
			.texture("side", sideTexture)
			.texture("top", modBlock(this, "candles/" + name + "_" + top))
			.texture("particle", sideTexture);
	}

	/**
	 * Emits the blockstate, the one model and the item model of one big candle. A big candle shows the same
	 * model in every state.
	 * <p>
	 * Deviation from 1.20.1: the blockstate listed all four lit and waterlogged combinations there, each
	 * pointing at the one model. This writes the single empty variant, which matches every state.
	 */
	private void bigCandle(BlockTypeBigCandle type) {
		Block candle = CoreBlocks.BIG_CANDLES.get(type).block();
		String model = "candles/" + type.getSerializedName() + "_big";

		models().withExistingParent("block/" + model, modBlock(this, "candles/big"))
			.texture("0", modBlock(this, model))
			.texture("particle", modBlock(this, model));

		singleModelBlock(this, candle, models().getExistingFile(modBlock(this, model)));
		itemModels().withExistingParent(path(candle), modBlock(this, model));
	}

	/**
	 * Emits the blockstate, the eight models and the item model of one vanilla-shaped candle, which shows one
	 * model per candle count and a second set of the four while lit.
	 */
	private void vanillaCandle(Block block) {
		String name = path(block);
		ResourceLocation texture = modBlock(this, "candles/" + name);
		ResourceLocation litTexture = modBlock(this, "candles/" + name + "_lit");

		ModelFile one = candleCountModel(name, "one", "template_candle", texture);
		ModelFile oneLit = candleCountModel(name, "one_lit", "template_candle", litTexture);
		ModelFile two = candleCountModel(name, "two", "template_two_candles", texture);
		ModelFile twoLit = candleCountModel(name, "two_lit", "template_two_candles", litTexture);
		ModelFile three = candleCountModel(name, "three", "template_three_candles", texture);
		ModelFile threeLit = candleCountModel(name, "three_lit", "template_three_candles", litTexture);
		ModelFile four = candleCountModel(name, "four", "template_four_candles", texture);
		ModelFile fourLit = candleCountModel(name, "four_lit", "template_four_candles", litTexture);

		getVariantBuilder(block).forAllStates(state -> {
			int count = state.getValue(CandleBlock.CANDLES);
			boolean lit = state.getValue(CandleBlock.LIT);

			ModelFile model = switch (count) {
				case 2 -> lit ? twoLit : two;
				case 3 -> lit ? threeLit : three;
				case 4 -> lit ? fourLit : four;
				default -> lit ? oneLit : one;
			};

			return ConfiguredModel.builder().modelFile(model).build();
		});

		// Deviation from 1.20.1: the two item models were hand-authored there. They are generated here, from
		// the item textures those two files already named
		layer0(ModUtil.getRegistryName(block.asItem()), "item/generated");
	}

	/**
	 * Emits one count model of one vanilla-shaped candle.
	 *
	 * @param name    The candle's registry id, which the model id is built from
	 * @param count   The candle count the model covers, with a lit suffix where it is lit
	 * @param parent  The vanilla template of the matching candle count
	 * @param texture The texture on every face, which is also the particle
	 * @return The registered model
	 */
	private ModelFile candleCountModel(String name, String count, String parent, ResourceLocation texture) {
		return models().withExistingParent("block/candles/" + name + "_" + count, mcBlock(this, parent))
			.texture("all", texture)
			.texture("particle", texture);
	}

	/**
	 * Emits the blockstates, block models and item models of the plywood sheet and the plywood block. Both
	 * sample block/plywood_top on the two end faces and block/plywood_side on the four others.
	 * <p>
	 * Deviation from 1.20.1: both blockstates were hand-authored there, and both are generated here. The sheet
	 * keeps its hand-authored model, a one-pixel slab no vanilla parent writes. The plywood block's model was
	 * hand-authored too, as an inline cube carrying the display block vanilla's block/block parent already
	 * gives it. cube_column writes the same six faces.
	 */
	private void plywood() {
		Block sheet = CoreBlocks.PLYWOOD_SHEET.block();
		directionalBlock(sheet, models().getExistingFile(modBlock(this, path(sheet))));
		generic3d(sheet);

		BlockPlywoodBlock block = CoreBlocks.PLYWOOD_BLOCK.block();
		ResourceLocation top = modBlock(this, "plywood_top");
		ModelFile model = models().withExistingParent(path(block), mcBlock(this, "cube_column"))
			.texture("end", top)
			.texture("side", modBlock(this, "plywood_side"))
			.texture("particle", top);

		// One model for all three axes, rotated, the way 1.20.1 did it. cube_column_horizontal would turn the
		// side texture with the grain, which a plain plywood side does not show
		axisBlock(block, model, model);
		generic3d(block);
	}

	/**
	 * Emits the blockstates, block models and item models of one decorative shape family. Every block in the
	 * family samples block/&lt;base block id&gt;.
	 */
	private void stoneFamily(CoreBlocks.StoneFamily family) {
		Block base = family.base().block();
		ResourceLocation texture = modBlock(this, path(base));

		simpleBlock(base);
		generic3d(base);

		stairsBlock(family.stairs().block(), texture);
		generic3d(family.stairs().block());

		// The double-slab variant reuses the base block's cube_all, which sits at the texture's own path
		slabBlock(family.slab().block(), texture, texture);
		generic3d(family.slab().block());

		String wall = path(family.wall().block());
		wallBlock(family.wall().block(), texture);
		// wallBlock emits post and side models only, so the item needs an inventory model of its own
		models().wallInventory(wall + "_inventory", texture);
		itemModels().withExistingParent(wall, modBlock(this, wall + "_inventory"));
	}

	/**
	 * Emits the blockstate, block model and item model of a chiseled brick block, which samples
	 * block/&lt;id&gt; on all six faces.
	 */
	private void chiseledCube(Block block) {
		simpleBlock(block);
		generic3d(block);
	}

	/**
	 * Emits the blockstate, block model and item model of a chiseled stone block, which samples
	 * block/&lt;id&gt;_top on the top and bottom and block/&lt;id&gt;_side on the four sides.
	 * <p>
	 * Deviation from 1.20.1: these three models were hand-authored there, as an inline cube carrying the
	 * display block vanilla's block/block parent already gives it. cube_bottom_top writes the same six faces.
	 */
	private void chiseledColumn(Block block) {
		String name = path(block);
		ResourceLocation top = modBlock(this, name + "_top");

		singleModelBlock(this, block, models().cubeBottomTop(name, modBlock(this, name + "_side"), top, top));
		generic3d(block);
	}

	// Builds a block/cube model whose faces map to textures block/<prefix>.<n>, then emits a
	// horizontal-facing blockstate for it. Face suffixes are given in JSON order:
	// down, up, north, south, east, west, particle.
	// Static and taking the provider, because a content jar's blocks are generated by that jar's own
	// provider rather than by this one. Every static below does the same and takes BlockStateProvider,
	// so a content jar's provider can call any of them through the one handle it already holds, without
	// checking which sibling wants a narrower type
	public static void horizontalMachine(BlockStateProvider states, Block block, String prefix, int down, int up, int north, int south, int east, int west, int particle) {
		ModelFile model = states.models().withExistingParent(path(block), states.mcLoc("block/cube"))
			.texture("particle", states.modLoc("block/" + prefix + "." + particle))
			.texture("down", states.modLoc("block/" + prefix + "." + down))
			.texture("up", states.modLoc("block/" + prefix + "." + up))
			.texture("north", states.modLoc("block/" + prefix + "." + north))
			.texture("east", states.modLoc("block/" + prefix + "." + east))
			.texture("south", states.modLoc("block/" + prefix + "." + south))
			.texture("west", states.modLoc("block/" + prefix + "." + west));
		horizontalForestryBlock(states, block, model);
	}

	/**
	 * Emits the blockstate and the per-tank-level models of one packaging machine. Every model parents
	 * block/machines/base_machine and layers the base texture with a resource and/or product tank slice,
	 * picked by the matching BlockFactoryPlain tank level property.
	 *
	 * @param block  The machine's block type, which the model and texture ids are built from
	 * @param layout How many tanks the model shows
	 */
	private void machineBlock(BlockTypeFactoryPlain block, IMachineProperties.TankLayout layout) {
		BlockFactoryPlain machine = FactoryBlocks.PLAIN.get(block).block();
		String name = block.name().toLowerCase();

		String baseTexture = "block/machines/" + name + "/base";
		String particleTexture = "block/machines/" + name + "/particles";

		switch (layout) {
			case NONE -> {
				models().withExistingParent(name, modLoc("block/machines/base_machine"))
					.renderType("cutout")
					.texture("base", modLoc(baseTexture))
					.texture("particle", modLoc(particleTexture));

				getVariantBuilder(machine).forAllStates(state -> ConfiguredModel.builder()
					.modelFile(models().getExistingFile(modLoc("block/" + name)))
					.rotationY(rotationFromFacing(state.getValue(BlockBase.FACING)))
					.build());
			}
			case RESOURCE -> {
				for (int level = 0; level <= 4; level++) {
					String modelName = name + "_res_" + level;

					models().withExistingParent(modelName, modLoc("block/machines/base_machine"))
						.renderType("cutout")
						.texture("base", modLoc(baseTexture))
						.texture("particle", modLoc(particleTexture))
						.texture("resource_tank", modLoc("block/machines/" + name + "/tank_res_" + level));
				}

				getVariantBuilder(machine).forAllStates(state -> {
					int level = state.getValue(BlockFactoryPlain.TANK_RESOURCE_LEVEL);
					return ConfiguredModel.builder()
						.modelFile(models().getExistingFile(modLoc("block/" + name + "_res_" + level)))
						.rotationY(rotationFromFacing(state.getValue(BlockBase.FACING)))
						.build();
				});
			}
			case PRODUCT -> {
				for (int level = 0; level <= 4; level++) {
					String modelName = name + "_prod_" + level;

					models().withExistingParent(modelName, modLoc("block/machines/base_machine"))
						.renderType("cutout")
						.texture("base", modLoc(baseTexture))
						.texture("particle", modLoc(particleTexture))
						.texture("product_tank", modLoc("block/machines/" + name + "/tank_prod_" + level));
				}

				getVariantBuilder(machine).forAllStates(state -> {
					int level = state.getValue(BlockFactoryPlain.TANK_PRODUCT_LEVEL);
					return ConfiguredModel.builder()
						.modelFile(models().getExistingFile(modLoc("block/" + name + "_prod_" + level)))
						.rotationY(rotationFromFacing(state.getValue(BlockBase.FACING)))
						.build();
				});
			}
			case BOTH -> {
				for (int left = 0; left <= 4; left++) {
					for (int right = 0; right <= 4; right++) {
						String modelName = name + "_res_" + left + "_prod_" + right;

						models().withExistingParent(modelName, modLoc("block/machines/base_machine"))
							.renderType("cutout")
							.texture("base", modLoc(baseTexture))
							.texture("particle", modLoc(particleTexture))
							.texture("resource_tank", modLoc("block/machines/" + name + "/tank_res_" + left))
							.texture("product_tank", modLoc("block/machines/" + name + "/tank_prod_" + right));
					}
				}

				getVariantBuilder(machine).forAllStates(state -> {
					int left = state.getValue(BlockFactoryPlain.TANK_RESOURCE_LEVEL);
					int right = state.getValue(BlockFactoryPlain.TANK_PRODUCT_LEVEL);
					String modelName = name + "_res_" + left + "_prod_" + right;

					return ConfiguredModel.builder()
						.modelFile(models().getExistingFile(modLoc("block/" + modelName)))
						.rotationY(rotationFromFacing(state.getValue(BlockBase.FACING)))
						.build();
				});
			}
		}
	}

	private int rotationFromFacing(Direction facing) {
		return switch (facing) {
			case SOUTH -> 180;
			case WEST -> 270;
			case EAST -> 90;
			default -> 0;
		};
	}

	// Emits a single "" variant pointing at an existing hand-authored model named after the block.
	private void existingModelBlock(Block block) {
		existingModelBlock(block, path(block));
	}

	// Emits a single "" variant pointing at the existing hand-authored model at block/<modelPath>.
	private void existingModelBlock(Block block, String modelPath) {
		singleModelBlock(this, block, models().getExistingFile(modBlock(this, modelPath)));
	}

	public static void singleModelBlock(BlockStateProvider states, Block defaultBlock, ModelFile file) {
		states.getVariantBuilder(defaultBlock).partialState().modelForState().modelFile(file).addModel();
	}

	public static ModelFile particleOnly(BlockStateProvider states, String path, ResourceLocation particleTexture) {
		return states.models().getBuilder(path).texture("particle", particleTexture);
	}

	// Makes a 3d cube of a block for item model
	public static void generic3d(BlockStateProvider states, Block block) {
		String path = path(block);
		states.itemModels().withExistingParent(path, states.modLoc("block/" + path));
	}

	/**
	 * BlockStateProvider#horizontalBlock keys off vanilla's BlockStateProperties.HORIZONTAL_FACING,
	 * which Forestry's BlockBase doesn't carry. Its FACING is a custom EnumProperty&lt;Direction&gt;
	 * (different identity, same name). Build the variant manually using BlockBase.FACING so the
	 * lookup succeeds.
	 */
	public static void horizontalForestryBlock(BlockStateProvider states, Block block, ModelFile model) {
		states.getVariantBuilder(block).forAllStates(state -> {
			Direction facing = state.getValue(BlockBase.FACING);
			int yRot = ((int) facing.toYRot() + 180) % 360;
			return ConfiguredModel.builder()
				.modelFile(model)
				.rotationY(yRot)
				.build();
		});
	}

	/**
	 * Used to build the blockstate and the cube models for a soil block that retextures itself as it ages.
	 * Deviation from 1.20.1: the blockstate there listed each model four times, once per y rotation, but
	 * a cube_all model wears one texture on all six faces, so only the top face ever showed the rotation.
	 * One variant per age is written instead, matching the turf blocks above
	 *
	 * @param block    The soil block
	 * @param property The age property, which gets one model per value
	 */
	private void agingSoil(Block block, IntegerProperty property) {
		String name = path(block);
		Map<Integer, ModelFile> models = new HashMap<>();

		for (int age : property.getPossibleValues()) {
			// age 0 keeps the bare name so the hand-written item model can still parent it
			String model = age == 0 ? name : name + "_" + age;
			models.put(age, models().cubeAll(model, modBlock(this, model)));
		}

		getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
			.modelFile(models.get(state.getValue(property)))
			.build());
	}

	protected static ResourceLocation withSuffix(ResourceLocation loc, String suffix) {
		return loc.withSuffix(suffix);
	}

	protected static ResourceLocation withPrefix(String prefix, ResourceLocation loc) {
		String oldPath = loc.getPath();
		int slash = oldPath.lastIndexOf('/') + 1;

		if (slash != 0) {
			return loc.withPath(oldPath.substring(0, slash) + prefix + oldPath.substring(slash));
		}
		return loc.withPrefix(prefix);
	}

	public void generic3d(Block block, Block otherParent) {
		itemModels().withExistingParent(path(block), modLoc("block/" + path(otherParent)));
	}

	public void generic3d(Block block, ResourceLocation otherParentId) {
		itemModels().withExistingParent(path(block), ResourceLocation.fromNamespaceAndPath(otherParentId.getNamespace(), "block/" + otherParentId.getPath()));
	}

	protected ModelFile existingMcBlock(String path) {
		return models().getExistingFile(mcBlock(this, path));
	}

	// Everything below this line is boilerplate code adapted from https://github.com/thedarkcolour/ModKit
	public void generic3d(Block block) {
		generic3d(this, block);
	}

	public static String path(Block block) {
		return ModUtil.getRegistryName(block).getPath();
	}

	public static ModelFile.UncheckedModelFile file(ResourceLocation resourceLoc) {
		return new ModelFile.UncheckedModelFile(resourceLoc);
	}

	public static ResourceLocation modBlock(BlockStateProvider states, String name) {
		return states.modLoc("block/" + name);
	}

	public static ResourceLocation mcBlock(BlockStateProvider states, String name) {
		return states.mcLoc("block/" + name);
	}

	public void generic2d(ItemLike item) {
		generic2d(ModUtil.getRegistryName(item.asItem()));
	}

	/**
	 * Makes a 2d single layer item like hopper, gold ingot, or redstone dust item models
	 */
	public void generic2d(ResourceLocation itemId) {
		layer0(itemId, "item/generated");
	}

	public void layer0(ResourceLocation itemId, String parentName) {
		String path = itemId.getPath();

		itemModels().getBuilder(path)
			.parent(new ModelFile.UncheckedModelFile(parentName))
			.texture("layer0", ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "item/" + path));
	}
}
