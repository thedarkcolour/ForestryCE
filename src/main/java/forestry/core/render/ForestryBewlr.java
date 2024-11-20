package forestry.core.render;

import java.util.IdentityHashMap;
import java.util.function.BiFunction;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;

import forestry.apiculture.blocks.NaturalistChestBlockType;
import forestry.core.blocks.BlockBase;
import forestry.core.blocks.BlockTypeCoreTesr;
import forestry.core.features.CoreBlocks;
import forestry.core.tiles.TileAnalyzer;
import forestry.core.tiles.TileApiaristChest;
import forestry.core.tiles.TileArboristChest;
import forestry.core.tiles.TileEscritoire;
import forestry.core.tiles.TileLepidopteristChest;
import forestry.energy.blocks.EngineBlockType;
import forestry.energy.features.EnergyBlocks;
import forestry.energy.tiles.BiogasEngineBlockEntity;
import forestry.energy.tiles.ClockworkEngineBlockEntity;
import forestry.energy.tiles.PeatEngineBlockEntity;
import forestry.factory.blocks.BlockTypeFactoryTesr;
import forestry.factory.features.FactoryBlocks;
import forestry.factory.tiles.TileBottler;
import forestry.factory.tiles.TileCarpenter;
import forestry.factory.tiles.TileCentrifuge;
import forestry.factory.tiles.TileFermenter;
import forestry.factory.tiles.TileMillRainmaker;
import forestry.factory.tiles.TileMoistener;
import forestry.factory.tiles.TileSqueezer;
import forestry.factory.tiles.TileStill;
import forestry.modules.features.FeatureBlock;

public class ForestryBewlr extends BlockEntityWithoutLevelRenderer {
	private final BlockEntityRenderDispatcher dispatcher;
	private final IdentityHashMap<Item, BlockEntity> tiles;

	public ForestryBewlr(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher, null);

		this.dispatcher = dispatcher;

		IdentityHashMap<Item, BlockEntity> tiles = new IdentityHashMap<>();

		addTile(tiles, CoreBlocks.BASE.get(BlockTypeCoreTesr.ESCRITOIRE), TileEscritoire::new);
		addTile(tiles, CoreBlocks.BASE.get(BlockTypeCoreTesr.ANALYZER), TileAnalyzer::new);

		addTile(tiles, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST), TileApiaristChest::new);
		addTile(tiles, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.ARBORIST_CHEST), TileArboristChest::new);
		addTile(tiles, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.LEPIDOPTERIST_CHEST), TileLepidopteristChest::new);

		addTile(tiles, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.BOTTLER), TileBottler::new);
		addTile(tiles, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CARPENTER), TileCarpenter::new);
		addTile(tiles, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CENTRIFUGE), TileCentrifuge::new);
		addTile(tiles, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.FERMENTER), TileFermenter::new);
		addTile(tiles, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.MOISTENER), TileMoistener::new);
		addTile(tiles, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.SQUEEZER), TileSqueezer::new);
		addTile(tiles, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.STILL), TileStill::new);
		addTile(tiles, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.RAINMAKER), TileMillRainmaker::new);

		addTile(tiles, EnergyBlocks.ENGINES.get(EngineBlockType.PEAT), PeatEngineBlockEntity::new);
		addTile(tiles, EnergyBlocks.ENGINES.get(EngineBlockType.BIOGAS), BiogasEngineBlockEntity::new);
		addTile(tiles, EnergyBlocks.ENGINES.get(EngineBlockType.CLOCKWORK), ClockworkEngineBlockEntity::new);

		this.tiles = tiles;
	}

	private static void addTile(IdentityHashMap<Item, BlockEntity> map, FeatureBlock<?, ?> block, BiFunction<BlockPos, BlockState, BlockEntity> factory) {
		BlockState state = block.defaultState();
		if (state.hasProperty(BlockBase.FACING)) {
			state = state.setValue(BlockBase.FACING, Direction.SOUTH);
		}
		map.put(block.item(), factory.apply(BlockPos.ZERO, state));
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
		Item item = stack.getItem();
		BlockEntity blockEntity = this.tiles.get(item);

		if (blockEntity != null) {
			this.dispatcher.renderItem(blockEntity, poseStack, buffers, light, overlay);
		}
	}
}
