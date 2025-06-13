package forestry.arboriculture.blocks;

import com.mojang.authlib.GameProfile;
import forestry.api.ForestryTags;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.BlockUtil;
import forestry.core.utils.ItemStackUtil;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;

public class BlockForestryLeaves extends BlockAbstractLeaves implements BonemealableBlock, EntityBlock {
	@Override
	protected ITree getTree(BlockGetter world, BlockPos pos) {
		TileLeaves leaves = TileUtil.getTile(world, pos, TileLeaves.class);
		if (leaves != null) {
			return leaves.getTree();
		}

		return null;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
		super.randomTick(state, world, pos, rand);

		TileLeaves tileLeaves = TileUtil.getTile(world, pos, TileLeaves.class);

		// check leaves tile because they might have decayed
		if (tileLeaves != null && !tileLeaves.isRemoved() && rand.nextFloat() <= 0.1) {
			tileLeaves.onBlockTick(world, pos, state, rand);
		}
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return !state.getValue(PERSISTENT);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileLeaves(pos, state);
	}

	@Override
	protected void getLeafDrop(List<ItemStack> drops, Level level, BlockPos pos, @Nullable GameProfile profile, float saplingModifier, int fortune, LootParams.Builder context) {
		if (!(level.getBlockEntity(pos) instanceof TileLeaves leaves)) {
			return;
		}

		ITree tree = leaves.getTree();
		if (tree == null) {
			return;
		}

		// Add saplings
		List<ITree> saplings = tree.getSaplings(level, pos, profile, saplingModifier);

		for (ITree sapling : saplings) {
			if (sapling != null) {
				drops.add(SpeciesUtil.TREE_TYPE.get().createStack(sapling, TreeLifeStage.SAPLING));
			}
		}

		// Add fruits
		if (leaves.hasFruit()) {
			drops.addAll(tree.produceStacks(level, pos, leaves.getRipeningTime()));
		}
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (stack.is(ForestryTags.Items.SCOOPS)) {
			if (level.getBlockEntity(pos) instanceof TileLeaves leaves) {
				IButterfly caterpillar = leaves.getCaterpillar();
				if (caterpillar != null) {
					if (!level.isClientSide) {
						ItemStackUtil.dropItemStackAsEntity(caterpillar.createStack(ButterflyLifeStage.CATERPILLAR), level, pos.below());
						leaves.setCaterpillar(null);
					}

					return ItemInteractionResult.sidedSuccess(level.isClientSide);
				}
			}
		}

		return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.getBlockEntity(pos) instanceof TileLeaves leaves) {
			if (leaves.hasFruit() && leaves.getRipeness() >= 0.9f) {
				if (!level.isClientSide) {
					BlockUtil.sendDestroyEffects(level, pos, state);

					for (ItemStack fruit : leaves.pickFruit()) {
						ItemHandlerHelper.giveItemToPlayer(player, fruit);
					}
				}

				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}

		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		TileLeaves leafTile = TileUtil.getTile(level, pos, TileLeaves.class);
		return leafTile != null && leafTile.hasFruit() && leafTile.getRipeness() < 1.0f;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource rand, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource rand, BlockPos pos, BlockState state) {
		TileLeaves leafTile = TileUtil.getTile(level, pos, TileLeaves.class);
		if (leafTile != null) {
			leafTile.addRipeness(0.5f);
		}
	}

	@Override
	public int colorMultiplier(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
		if (level != null && pos != null) {
			TileLeaves leaves = TileUtil.getTile(level, pos, TileLeaves.class);
			if (leaves != null) {
				if (tintIndex == BlockAbstractLeaves.FRUIT_COLOR_INDEX) {
					return leaves.getFruitColour();
				} else {
					return leaves.getFoliageColour();
				}
			}
		}
		return FoliageColor.getDefaultColor();
	}
}
