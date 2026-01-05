package forestry.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import forestry.api.ForestryConstants;
import forestry.core.blocks.BlockBase;
import forestry.core.config.Constants;
import forestry.core.tiles.TileNaturalistChest;
import forestry.core.utils.RenderUtil;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RenderNaturalistChest implements BlockEntityRenderer<TileNaturalistChest> {
	private static final String LID = "lid";
	private static final String BASE = "base";
	private static final String LOCK = "lock";

	private final ModelPart lid;
	private final ModelPart base;
	private final ModelPart lock;
	private final ResourceLocation texture;

	public RenderNaturalistChest(BlockEntityRendererProvider.Context ctx, String textureName) {
		ModelPart root = ctx.bakeLayer(ForestryModelLayers.NATURALIST_CHEST_LAYER);

		this.lid = root.getChild(LID);
		this.base = root.getChild(BASE);
		this.lock = root.getChild(LOCK);
		this.texture = ForestryConstants.forestry(Constants.TEXTURE_PATH_BLOCK + "/" + textureName + ".png");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild(BASE, CubeListBuilder.create().texOffs(0, 19)
			.addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F), PartPose.offset(0, 0, 0));
		partdefinition.addOrReplaceChild(LID, CubeListBuilder.create().texOffs(0, 0)
			.addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0, 9.0F, 1.0F));
		partdefinition.addOrReplaceChild(LOCK, CubeListBuilder.create().texOffs(0, 0)
			.addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(0, 8.0F, 0));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void render(TileNaturalistChest chest, float partialTick, PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
		stack.pushPose();

		RenderUtil.rotateByHorizontalDirection(stack, chest.getBlockState().getValue(BlockBase.FACING));

		// calculate lid angle
		float prevLidAngle = chest.prevLidAngle;
		float lidAngle = chest.lidAngle;
		float angle = prevLidAngle + (lidAngle - prevLidAngle) * partialTick;
		angle = 1.0F - angle;
		angle = 1.0F - angle * angle * angle;
		float rotation = -(angle * Mth.HALF_PI);
		// set angles for lock and lid parts
		this.lid.xRot = rotation;
		this.lock.xRot = rotation;

		// render
		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(this.texture));
		this.lid.render(stack, buffer, light, overlay);
		this.lock.render(stack, buffer, light, overlay);
		this.base.render(stack, buffer, light, overlay);

		stack.popPose();
	}
}
