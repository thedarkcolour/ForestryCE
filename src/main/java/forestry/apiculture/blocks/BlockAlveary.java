package forestry.apiculture.blocks;

import forestry.apiculture.multiblock.*;
import forestry.apiculture.network.packets.PacketAlvearyChange;
import forestry.core.TranslationKeys;
import forestry.core.blocks.BlockStructure;
import forestry.core.tiles.IActivatable;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
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

	private final BlockAlvearyType type;

	public BlockAlveary(BlockAlvearyType type) {
		super(Block.Properties.of().strength(1f).sound(SoundType.WOOD));
		this.type = type;
		BlockState defaultState = this.getStateDefinition().any();
		if (type == BlockAlvearyType.PLAIN) {
			defaultState = defaultState.setValue(PLAIN_TYPE, AlvearyPlainType.NORMAL);
		} else if (type.activatable) {
			defaultState = defaultState.setValue(STATE, State.OFF);
		}
		registerDefaultState(defaultState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PLAIN_TYPE, STATE);
	}

	public BlockAlvearyType getType() {
		return this.type;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return switch (this.type) {
			case SWARMER -> new TileAlvearySwarmer(pos, state);
			case FAN -> new TileAlvearyFan(pos, state);
			case HEATER -> new TileAlvearyHeater(pos, state);
			case HYGRO -> new TileAlvearyHygroregulator(pos, state);
			case STABILISER -> new TileAlvearyStabiliser(pos, state);
			case SIEVE -> new TileAlvearySieve(pos, state);
			default -> new TileAlvearyPlain(pos, state);
		};
	}

	public BlockState getNewState(TileAlveary tile) {
		BlockState state = this.defaultBlockState();

		if (tile instanceof IActivatable activatable) {
			state = state.setValue(STATE, activatable.isActive() ? State.ON : State.OFF);
		} else if (getType() == BlockAlvearyType.PLAIN) {
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
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean p_220069_6_) {
		TileUtil.actOnTile(worldIn, pos, TileAlveary.class, tileAlveary -> {
			// We must check that the slabs on top were not removed
			IAlvearyControllerInternal alveary = tileAlveary.getMultiblockLogic().getController();
			alveary.reassemble();
			BlockPos referenceCoord = alveary.getReferenceCoord();
			NetworkUtil.sendNetworkPacket(new PacketAlvearyChange(referenceCoord), referenceCoord, worldIn);
		});
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag flag) {
		if (Screen.hasShiftDown()) {
			tooltip.add(Component.translatable("block.forestry.alveary_tooltip"));
		} else {
			tooltip.add(Component.translatable(TranslationKeys.HOLD_SHIFT_FOR_DETAILS).withStyle(ChatFormatting.GRAY));
		}
	}
}
