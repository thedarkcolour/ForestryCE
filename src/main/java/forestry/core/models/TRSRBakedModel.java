package forestry.core.models;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Transformation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.TransformationHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

// for those wondering TRSR stands for Translation Rotation Scale Rotation
public class TRSRBakedModel extends BakedModelWrapper<BakedModel> {

	protected final Transformation transformation;
	private final TRSROverride override;
	private final int faceOffset;

	public TRSRBakedModel(BakedModel original, float x, float y, float z, float scale) {
		this(original, x, y, z, 0, 0, 0, scale, scale, scale);
	}

	public TRSRBakedModel(BakedModel original, float x, float y, float z, float rotX, float rotY, float rotZ, float scaleX, float scaleY, float scaleZ) {
		this(original, new Transformation(new Vector3f(x, y, z),
			null,
			new Vector3f(scaleX, scaleY, scaleZ),
			TransformationHelper.quatFromXYZ(rotX, rotY, rotZ, false)));
	}

	public TRSRBakedModel(BakedModel original, Transformation transform) {
		super(original);
		this.transformation = transform.blockCenterToCorner();
		this.override = new TRSROverride(this);
		this.faceOffset = 0;
	}

	/**
	 * Rotates around the Y axis and adjusts culling appropriately. South is default.
	 */
	public TRSRBakedModel(BakedModel original, Direction facing) {
		super(original);
		this.override = new TRSROverride(this);

		this.faceOffset = 4 + Direction.NORTH.get2DDataValue() - facing.get2DDataValue();

		double r = Math.PI * (360 - facing.getOpposite().get2DDataValue() * 90) / 180d;
		this.transformation = new Transformation(null, null, null, TransformationHelper.quatFromXYZ(0, (float) r, 0, false)).blockCenterToCorner();
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
		// transform quads obtained from parent
		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
		if (!this.originalModel.isCustomRenderer()) {
			try {
				// adjust side to facing-rotation
				if (side != null && side.get2DDataValue() > -1) {
					side = Direction.from2DDataValue((side.get2DDataValue() + this.faceOffset) % 4);
				}
				for (BakedQuad quad : this.originalModel.getQuads(state, side, rand, data, renderType)) {
					/*Transformer transformer = new Transformer(this.transformation, quad.getSprite());
					//quad.pipe(transformer);
					builder.add(transformer.build());*/
				}
			} catch (Exception ignored) {
			}
		}

		return builder.build();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
		return this.getQuads(state, side, rand, ModelData.EMPTY, null);
	}

	@Nonnull
	@Override
	public ItemOverrides getOverrides() {
		return this.override;
	}

	private static class TRSROverride extends ItemOverrides {
		private final TRSRBakedModel model;

		public TRSROverride(TRSRBakedModel model) {
			this.model = model;
		}

		@Nonnull
		@Override
		public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int p_173469_) {
			BakedModel baked = this.model.originalModel.getOverrides().resolve(originalModel, stack, world, entity, p_173469_);
			if (baked == null) {
				baked = originalModel;
			}
			return new TRSRBakedModel(baked, this.model.transformation);
		}
	}
}
