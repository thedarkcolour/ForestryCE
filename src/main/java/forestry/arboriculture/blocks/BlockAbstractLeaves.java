package forestry.arboriculture.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.mojang.authlib.GameProfile;

import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.core.blocks.IColoredBlock;
import forestry.core.utils.BlockUtil;
import forestry.core.utils.SpeciesUtil;

/**
 * Parent class for shared behavior between {@link BlockDefaultLeaves} and {@link BlockForestryLeaves}
 */
public abstract class BlockAbstractLeaves extends LeavesBlock implements IColoredBlock {
	public static final int FOLIAGE_COLOR_INDEX = 0;
	public static final int FRUIT_COLOR_INDEX = 2;

	public BlockAbstractLeaves() {
		super(Block.Properties.of()
				.strength(0.2f)
				.sound(SoundType.GRASS)
				.randomTicks()
				.noOcclusion()
				.isValidSpawn(BlockUtil.IS_PARROT_OR_OCELOT)
				.isSuffocating(BlockUtil.NEVER)
				.isViewBlocking(BlockUtil.NEVER));
	}

	@Nullable
	protected abstract ITree getTree(BlockGetter world, BlockPos pos);

	@Override
	public String getDescriptionId() {
		return "block.forestry.leaves";// Use the same for all leaves, so the default leaves don't have an other name than the pollinated ones
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
		ITree tree = getTree(world, pos);
		if (tree == null) {
			return ItemStack.EMPTY;
		}
		ITreeSpecies species = tree.getSpecies();
		return species.getDecorativeLeaves();
	}

	@Nonnull
	@Override
	public List<ItemStack> onSheared(@Nullable Player player, @Nonnull ItemStack item, Level world, BlockPos pos, int fortune) {
		ITree tree = getTree(world, pos);
		ITreeSpecies species;
		if (tree == null) {
			species = SpeciesUtil.getTreeSpecies(ForestryTreeSpecies.OAK);
		} else {
			species = tree.getGenome().getActiveValue(TreeChromosomes.SPECIES);
		}
		ItemStack decorativeLeaves = species.getDecorativeLeaves();
		if (decorativeLeaves.isEmpty()) {
			return Collections.emptyList();
		} else {
			return Collections.singletonList(decorativeLeaves);
		}
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		ITree tree = getTree(worldIn, pos);
		if (tree != null && tree.getSpecies().id().equals(ForestryTreeSpecies.WILLOW)) {
			return Shapes.empty();
		}
		return super.getCollisionShape(state, worldIn, pos, context);
	}

	/**
	 * Used for walking through willow leaves.
	 */
	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		super.entityInside(state, worldIn, pos, entityIn);
		Vec3 motion = entityIn.getDeltaMovement();
		entityIn.setDeltaMovement(motion.x() * 0.4D, motion.y(), motion.z() * 0.4D);
	}

	/* PROPERTIES */
	@Override
	public final int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return 60;
	}

	@Override
	public final boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return true;
	}

	@Override
	public final int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		if (face == Direction.DOWN) {
			return 20;
		} else if (face != Direction.UP) {
			return 10;
		} else {
			return 5;
		}
	}

	protected abstract void getLeafDrop(List<ItemStack> drops, Level level, BlockPos pos, @Nullable GameProfile profile, float saplingModifier, int fortune, LootParams.Builder context);

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder context) {
		ArrayList<ItemStack> drops = new ArrayList<>(super.getDrops(state, context));
		GameProfile profile = null;
		if (context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player) {
			profile = player.getGameProfile();
		}
		ItemStack tool = context.getOptionalParameter(LootContextParams.TOOL);
		BlockPos pos = BlockUtil.getPos(context);
		getLeafDrop(drops, context.getLevel(), pos, profile, 1f, tool != null ? tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE) : 0, context);
		return drops;
	}
}
