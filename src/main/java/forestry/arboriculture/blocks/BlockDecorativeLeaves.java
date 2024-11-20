package forestry.arboriculture.blocks;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IForgeShearable;

import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.client.IForestryClientApi;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.core.blocks.IColoredBlock;
import forestry.core.utils.BlockUtil;

public class BlockDecorativeLeaves extends Block implements IColoredBlock, IForgeShearable {
	private final ForestryLeafType type;

	public BlockDecorativeLeaves(ForestryLeafType type) {
		super(Properties.of()
				.strength(0.2f)
				.sound(SoundType.GRASS)
				.noOcclusion()
				.isValidSpawn(BlockUtil.IS_PARROT_OR_OCELOT)
				.isSuffocating(BlockUtil.ALWAYS)
				.isRedstoneConductor(BlockUtil.NEVER)
				.isViewBlocking(BlockUtil.NEVER));
		this.type = type;
	}

	public ResourceLocation getSpeciesId() {
		return this.type.getSpeciesId();
	}

	public ForestryLeafType getType() {
		return this.type;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (this.type == ForestryLeafType.WILLOW) {
			return Shapes.empty();
		}
		return super.getCollisionShape(state, worldIn, pos, context);
	}

	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		Vec3 motion = entityIn.getDeltaMovement();
		entityIn.setDeltaMovement(motion.multiply(0.4D, 1.0D, 0.4D));
	}

	/* PROPERTIES */
	@Override
	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return 60;
	}

	@Override
	public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return true;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		if (face == Direction.DOWN) {
			return 20;
		} else if (face != Direction.UP) {
			return 10;
		} else {
			return 5;
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int colorMultiplier(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
		ITree individual = type.getIndividual();

		if (tintIndex == BlockAbstractLeaves.FRUIT_COLOR_INDEX) {
			IFruit fruitProvider = individual.getGenome().getActiveValue(TreeChromosomes.FRUIT);
			return fruitProvider.getDecorativeColor();
		}
		return IForestryClientApi.INSTANCE.getTreeManager().getTint(individual.getSpecies()).get(level, pos);
	}
}
