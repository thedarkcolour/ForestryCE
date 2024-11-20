package forestry.core.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import forestry.api.ForestryConstants;
import forestry.core.config.Constants;
import forestry.core.tiles.TemperatureState;
import forestry.energy.blocks.EngineBlock;
import forestry.energy.tiles.EngineBlockEntity;

public class RenderEngine implements BlockEntityRenderer<EngineBlockEntity> {
	private static final float[] ANGLE_MAP = new float[6];

	private enum Textures {
		BASE, PISTON, EXTENSION, TRUNK_HIGHEST, TRUNK_HIGHER, TRUNK_HIGH, TRUNK_MEDIUM, TRUNK_LOW
	}

	private final ResourceLocation[] textures;
	private final ModelPart boiler;
	private final ModelPart trunk;
	private final ModelPart piston;
	private final ModelPart extension;

	static {
		ANGLE_MAP[Direction.EAST.ordinal()] = -Mth.HALF_PI;
		ANGLE_MAP[Direction.NORTH.ordinal()] = -Mth.HALF_PI;
		ANGLE_MAP[Direction.WEST.ordinal()] = Mth.HALF_PI;
		ANGLE_MAP[Direction.SOUTH.ordinal()] = Mth.HALF_PI;
		ANGLE_MAP[Direction.UP.ordinal()] = 0;
		ANGLE_MAP[Direction.DOWN.ordinal()] = Mth.PI;
	}

	public RenderEngine(BlockEntityRendererProvider.Context ctx, String baseTexture) {
		ModelPart root = ctx.bakeLayer(ForestryModelLayers.ENGINE_LAYER);

		this.boiler = root.getChild("boiler");
		this.trunk = root.getChild("trunk");
		this.piston = root.getChild("piston");
		this.extension = root.getChild("extension");

		this.textures = new ResourceLocation[]{
				ForestryConstants.forestry(baseTexture + "base.png"),
				ForestryConstants.forestry(baseTexture + "piston.png"),
				ForestryConstants.forestry(baseTexture + "extension.png"),
				ForestryConstants.forestry(Constants.TEXTURE_PATH_BLOCK + "/engine_trunk_highest.png"),
				ForestryConstants.forestry(Constants.TEXTURE_PATH_BLOCK + "/engine_trunk_higher.png"),
				ForestryConstants.forestry(Constants.TEXTURE_PATH_BLOCK + "/engine_trunk_high.png"),
				ForestryConstants.forestry(Constants.TEXTURE_PATH_BLOCK + "/engine_trunk_medium.png"),
				ForestryConstants.forestry(Constants.TEXTURE_PATH_BLOCK + "/engine_trunk_low.png"),
		};
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("boiler", CubeListBuilder.create().texOffs(0, 0)
				.addBox(0, 0, 0, 16, 6, 16), PartPose.offset(0, 0, 0));
		root.addOrReplaceChild("trunk", CubeListBuilder.create().texOffs(0, 0)
				.addBox(0, 0, 0, 8, 12, 8), PartPose.offset(4, 4, 4));
		root.addOrReplaceChild("piston", CubeListBuilder.create().texOffs(0, 0)
				.addBox(0, 0, 0, 12, 4, 12), PartPose.offset(2, 6, 2));
		root.addOrReplaceChild("extension", CubeListBuilder.create().texOffs(0, 0)
				.addBox(0, 0, 0, 10, 2, 10), PartPose.offset(3, 5, 3));

		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void render(EngineBlockEntity engine, float partialTick, PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
		stack.pushPose();

		// apply direction rotation
		stack.translate(0.5, 0.5, 0.5);
		Direction orientation = engine.getBlockState().getValue(EngineBlock.VERTICAL_FACING);
		switch (orientation) {
			case EAST, WEST, DOWN -> stack.mulPose(Axis.ZP.rotation(ANGLE_MAP[orientation.ordinal()]));
			default -> stack.mulPose(Axis.XP.rotation(ANGLE_MAP[orientation.ordinal()]));
		}
		stack.translate(-0.5, -0.5, -0.5);

		// render base
		this.boiler.render(stack, buffers.getBuffer(RenderType.entityCutout(textures[Textures.BASE.ordinal()])), light, overlay);

		// render piston with smooth lerp
		float step = getPistonStep(engine, partialTick);
		float tfactor = step / 16;
		stack.translate(0, tfactor, 0);
		this.piston.render(stack, buffers.getBuffer(RenderType.entityCutout(textures[Textures.PISTON.ordinal()])), light, overlay);
		stack.translate(0, -tfactor, 0);

		// render trunk with color based on heat
		TemperatureState state = engine.hasLevel() ? engine.getTemperatureState() : TemperatureState.COOL;
		ResourceLocation texture = switch (state) {
			case OVERHEATING -> textures[Textures.TRUNK_HIGHEST.ordinal()];
			case RUNNING_HOT -> textures[Textures.TRUNK_HIGHER.ordinal()];
			case OPERATING_TEMPERATURE -> textures[Textures.TRUNK_HIGH.ordinal()];
			case WARMED_UP -> textures[Textures.TRUNK_MEDIUM.ordinal()];
			default -> textures[Textures.TRUNK_LOW.ordinal()];
		};
		this.trunk.render(stack, buffers.getBuffer(RenderType.entityCutout(texture)), light, overlay);

		// render piston sleeve
		float chamberf = 2F / 16F;
		if (step > 0) {
			VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(textures[Textures.EXTENSION.ordinal()]));

			for (int i = 0; i <= step + 2; i += 2) {
				this.extension.render(stack, buffer, light, overlay);
				stack.translate(0, chamberf, 0);
			}
		}

		stack.popPose();
	}

	private static float getPistonStep(EngineBlockEntity engine, float partialTick) {
		float progress;

		if (engine.hasLevel()) {
			progress = engine.progress;
			if (engine.stagePiston != 0) {
				float smoothing = engine.pistonSpeedServer * partialTick;
				progress = (progress + smoothing);
			}
		} else {
			progress = 0.25f;
		}

		if (progress > 0.5f) {
			return 6f - (progress - 0.5f) * 2f * 6F;
		} else {
			return progress * 2f * 6f;
		}
	}
}
