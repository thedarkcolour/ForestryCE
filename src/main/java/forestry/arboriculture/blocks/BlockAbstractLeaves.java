package forestry.arboriculture.blocks;

import com.mojang.authlib.GameProfile;
import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.genetics.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.core.blocks.IColoredBlock;
import forestry.core.utils.BlockUtil;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parent class for shared behavior between {@link BlockDefaultLeaves} and {@link BlockForestryLeaves}
 */
public abstract class BlockAbstractLeaves extends BlockExtendedLeaves implements IColoredBlock {
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
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		ITree tree = getTree(level, pos);
		if (tree == null) {
			return ItemStack.EMPTY;
		}
		ITreeSpecies species = tree.getSpecies();
		return species.getDecorativeLeaves();
	}

	@Override
	public List<ItemStack> onSheared(@Nullable Player player, ItemStack stack, Level level, BlockPos pos) {
		ITree tree = getTree(level, pos);
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
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		ITree tree = getTree(level, pos);
		if (tree != null && tree.getSpecies().id().equals(ForestryTreeSpecies.WILLOW)) {
			return Shapes.empty();
		}
		return super.getCollisionShape(state, level, pos, context);
	}

	/**
	 * Used for walking through willow leaves.
	 */
	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entityIn) {
		super.entityInside(state, level, pos, entityIn);
		Vec3 motion = entityIn.getDeltaMovement();
		entityIn.setDeltaMovement(motion.x() * 0.4D, motion.y(), motion.z() * 0.4D);
	}

	/* PROPERTIES */
	@Override
	public final int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
		return 60;
	}

	@Override
	public final boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
		return true;
	}

	@Override
	public final int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
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
		getLeafDrop(drops, context.getLevel(), pos, profile, 1f, tool != null ? tool.getEnchantmentLevel(context.getLevel().holderLookup(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE)) : 0, context);
		return drops;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
		super.animateTick(state, level, pos, rand);

		ITree tree = getTree(level, pos);

		if (tree != null) {
			IGenome genome = tree.getGenome();
			genome.getActiveValue(TreeChromosomes.EFFECT).doAnimationEffect(genome, level, pos, rand);
		}
	}
}
