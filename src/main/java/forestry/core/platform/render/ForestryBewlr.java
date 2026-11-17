package forestry.core.platform.render;

import com.mojang.blaze3d.vertex.PoseStack;
import forestry.core.content.energy.blocks.EngineBlockType;
import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.energy.tiles.*;
import forestry.core.content.machines.blocks.FactoryBlockType;
import forestry.core.content.machines.blocks.SpecialFactoryBlockType;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.content.machines.tiles.*;
import forestry.core.features.CoreBlocks;
import forestry.core.platform.block.BaseBlock;
import forestry.core.platform.block.NaturalistChestBlockType;
import forestry.core.platform.registration.FeatureBlock;
import forestry.core.platform.tile.ApiaristChestBlockEntity;
import forestry.core.platform.tile.ArboristChestBlockEntity;
import forestry.core.platform.tile.LepidopteristChestBlockEntity;
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

import java.util.IdentityHashMap;
import java.util.function.BiFunction;

public class ForestryBewlr extends BlockEntityWithoutLevelRenderer {
	private final BlockEntityRenderDispatcher dispatcher;
	private final IdentityHashMap<Item, BlockEntity> tiles;

	public ForestryBewlr(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher, null);

		this.dispatcher = dispatcher;

		IdentityHashMap<Item, BlockEntity> tiles = new IdentityHashMap<>();

		addTile(tiles, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST), ApiaristChestBlockEntity::new);
		addTile(tiles, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.ARBORIST_CHEST), ArboristChestBlockEntity::new);
		addTile(tiles, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.LEPIDOPTERIST_CHEST), LepidopteristChestBlockEntity::new);

		addTile(tiles, FactoryBlocks.PLAIN.get(FactoryBlockType.BOTTLER), BottlerBlockEntity::new);
		addTile(tiles, FactoryBlocks.PLAIN.get(FactoryBlockType.CARPENTER), CarpenterBlockEntity::new);
		addTile(tiles, FactoryBlocks.PLAIN.get(FactoryBlockType.CENTRIFUGE), CentrifugeBlockEntity::new);
		addTile(tiles, FactoryBlocks.PLAIN.get(FactoryBlockType.FERMENTER), FermenterBlockEntity::new);
		addTile(tiles, FactoryBlocks.PLAIN.get(FactoryBlockType.MOISTENER), MoistenerBlockEntity::new);
		addTile(tiles, FactoryBlocks.PLAIN.get(FactoryBlockType.SQUEEZER), SqueezerBlockEntity::new);
		addTile(tiles, FactoryBlocks.PLAIN.get(FactoryBlockType.STILL), StillBlockEntity::new);
		addTile(tiles, FactoryBlocks.TESR.get(SpecialFactoryBlockType.RAINMAKER), RainmakerBlockEntity::new);

		addTile(tiles, EnergyBlocks.ENGINES.get(EngineBlockType.PEAT), PeatEngineBlockEntity::new);
		addTile(tiles, EnergyBlocks.ENGINES.get(EngineBlockType.BIOGAS), BiogasEngineBlockEntity::new);
		addTile(tiles, EnergyBlocks.ENGINES.get(EngineBlockType.CLOCKWORK), ClockworkEngineBlockEntity::new);
		addTile(tiles, EnergyBlocks.ENGINES.get(EngineBlockType.COMBUSTION), CombustionEngineBlockEntity::new);
		addTile(tiles, EnergyBlocks.ENGINES.get(EngineBlockType.SOLAR), SolarEngineBlockEntity::new);

		this.tiles = tiles;
	}

	private static void addTile(IdentityHashMap<Item, BlockEntity> map, FeatureBlock<?, ?> block, BiFunction<BlockPos, BlockState, BlockEntity> factory) {
		BlockState state = block.defaultState();
		if (state.hasProperty(BaseBlock.FACING)) {
			state = state.setValue(BaseBlock.FACING, Direction.SOUTH);
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
