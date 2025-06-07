package forestry.apiculture.blocks;

import forestry.api.core.IBlockSubtype;
import forestry.apiculture.features.ApicultureTiles;
import forestry.apiculture.multiblock.*;
import forestry.apiculture.network.packets.PacketAlvearyChange;
import forestry.core.blocks.BlockStructure;
import forestry.core.tiles.IActivatable;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.ItemTooltipUtil;
import forestry.core.utils.NetworkUtil;
import forestry.modules.features.FeatureTileType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlockAlveary extends BlockStructure implements EntityBlock {
	public static final EnumProperty<State> STATE = EnumProperty.create("state", State.class);
	private static final EnumProperty<AlvearyPlainType> PLAIN_TYPE = EnumProperty.create("type", AlvearyPlainType.class);

	public enum State implements StringRepresentable {
		ON, OFF;

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}

	private enum AlvearyPlainType implements StringRepresentable {
		NORMAL, ENTRANCE, ENTRANCE_LEFT, ENTRANCE_RIGHT;

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}

	private final Type type;

	public BlockAlveary(Type type) {
		super(Block.Properties.of().strength(1f).sound(SoundType.WOOD));
		this.type = type;
		BlockState defaultState = this.getStateDefinition().any();
		if (type == Type.PLAIN) {
			defaultState = defaultState.setValue(PLAIN_TYPE, AlvearyPlainType.NORMAL);
		} else if (type.activatable()) {
			defaultState = defaultState.setValue(STATE, State.OFF);
		}
		registerDefaultState(defaultState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PLAIN_TYPE, STATE);
	}

	public Type getType() {
		return this.type;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return this.type.tileFeature.tileType().create(pos, state);
	}

	public BlockState getNewState(TileAlveary tile) {
		BlockState state = this.defaultBlockState();

		if (tile instanceof IActivatable activatable) {
			state = state.setValue(STATE, activatable.isActive() ? State.ON : State.OFF);
		} else if (getType() == Type.PLAIN) {
			Level level = tile.getLevel();
			BlockPos pos = tile.getBlockPos();

			if (!tile.getMultiblockLogic().getController().isAssembled()) {
				state = state.setValue(PLAIN_TYPE, AlvearyPlainType.NORMAL);
			} else {
				BlockState blockStateAbove = level.getBlockState(pos.above());
				if (blockStateAbove.is(BlockTags.WOODEN_SLABS)) {
					List<Direction> blocksTouching = getBlocksTouching(level, pos);
					switch (blocksTouching.size()) {
						case 3:
							state = state.setValue(PLAIN_TYPE, AlvearyPlainType.ENTRANCE);
							break;
						case 2:
							if (blocksTouching.contains(Direction.SOUTH) && blocksTouching.contains(Direction.EAST) ||
								blocksTouching.contains(Direction.NORTH) && blocksTouching.contains(Direction.WEST)) {
								state = state.setValue(PLAIN_TYPE, AlvearyPlainType.ENTRANCE_LEFT);
							} else {
								state = state.setValue(PLAIN_TYPE, AlvearyPlainType.ENTRANCE_RIGHT);
							}
							break;
						default:
							state = state.setValue(PLAIN_TYPE, AlvearyPlainType.NORMAL);
							break;
					}
				} else {
					state = state.setValue(PLAIN_TYPE, AlvearyPlainType.NORMAL);
				}
			}
		}
		return state;
	}

	private static List<Direction> getBlocksTouching(BlockGetter world, BlockPos blockPos) {
		List<Direction> touching = new ArrayList<>();
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockState blockState = world.getBlockState(blockPos.relative(direction));
			if (blockState.getBlock() instanceof BlockAlveary) {
				touching.add(direction);
			}
		}
		return touching;
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean movedByPiston) {
		TileUtil.actOnTile(level, pos, TileAlveary.class, tileAlveary -> {
			// We must check that the slabs on top were not removed
			IAlvearyControllerInternal alveary = tileAlveary.getMultiblockLogic().getController();
			alveary.reassemble();
			BlockPos referenceCoord = alveary.getReferenceCoord();
			if (level instanceof ServerLevel serverLevel) {
				NetworkUtil.sendToPlayersTrackingPos(new PacketAlvearyChange(referenceCoord), referenceCoord, serverLevel);
			}
		});
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
		if (Screen.hasShiftDown()) {
			tooltip.add(Component.translatable("block.forestry.alveary_tooltip"));
		} else {
			ItemTooltipUtil.addShiftInformation(tooltip);
		}
	}

	public record Type(String name, boolean activatable, FeatureTileType<? extends TileAlveary> tileFeature) implements IBlockSubtype {
		public static final Type PLAIN = new Type("plain", false, ApicultureTiles.ALVEARY_PLAIN);
		public static final Type SWARMER = new Type("swarmer", true, ApicultureTiles.ALVEARY_SWARMER);
		public static final Type FAN = new Type("fan", true, ApicultureTiles.ALVEARY_FAN);
		public static final Type HEATER = new Type("heater", true, ApicultureTiles.ALVEARY_HEATER);
		public static final Type HYGRO = new Type("hygro", false, ApicultureTiles.ALVEARY_HYGROREGULATOR);
		public static final Type STABILISER = new Type("stabiliser", false, ApicultureTiles.ALVEARY_STABILISER);
		public static final Type SIEVE = new Type("sieve", false, ApicultureTiles.ALVEARY_SIEVE);

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
