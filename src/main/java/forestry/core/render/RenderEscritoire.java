package forestry.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import forestry.api.ForestryConstants;
import forestry.core.blocks.BlockBase;
import forestry.core.config.Constants;
import forestry.core.tiles.TileEscritoire;
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
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RenderEscritoire implements BlockEntityRenderer<TileEscritoire> {
	private static final ResourceLocation TEXTURE = ForestryConstants.forestry(Constants.TEXTURE_PATH_BLOCK + "/escritoire.png");

	private final ItemRenderer itemRenderer;
	private final ModelPart root;

	public RenderEscritoire(BlockEntityRendererProvider.Context ctx) {
		this.itemRenderer = ctx.getItemRenderer();
		this.root = ctx.bakeLayer(ForestryModelLayers.ESCRITOIRE_LAYER);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("desk", CubeListBuilder.create().texOffs(0, 0)
			.addBox(0, 0, 0, 16, 2, 15).mirror(), PartPose.offsetAndRotation(0, 9.5f, 0.4f, 0.0872665f, 0, 0));
		partdefinition.addOrReplaceChild("standrb", CubeListBuilder.create().texOffs(38, 18)
			.addBox(0f, 0f, 0f, 2, 6, 2).mirror(), PartPose.offset(13, 4, 13));
		partdefinition.addOrReplaceChild("standrf", CubeListBuilder.create().texOffs(38, 18)
			.addBox(0f, 0f, 0f, 2, 6, 2).mirror(), PartPose.offset(13, 4, 1));
		partdefinition.addOrReplaceChild("standlb", CubeListBuilder.create().texOffs(38, 18)
			.addBox(0f, 0f, 0f, 2, 6, 2).mirror(), PartPose.offset(1, 4, 1));
		partdefinition.addOrReplaceChild("standlf", CubeListBuilder.create().texOffs(38, 18)
			.addBox(0f, 0f, 0f, 2, 6, 2).mirror(), PartPose.offset(1, 4, 13));
		partdefinition.addOrReplaceChild("drawers", CubeListBuilder.create().texOffs(0, 18)
			.addBox(0f, 0f, 0f, 15, 5, 3).mirror(), PartPose.offset(0.5f, 11, 0.5f));
		partdefinition.addOrReplaceChild("standlowrb", CubeListBuilder.create().texOffs(0, 26)
			.addBox(0f, 0f, 0f, 1, 4, 1).mirror(), PartPose.offset(13.5f, 0, 13.5f));
		partdefinition.addOrReplaceChild("standlowrf", CubeListBuilder.create().texOffs(0, 26)
			.addBox(0f, 0f, 0f, 1, 4, 1).mirror(), PartPose.offset(13.5f, 0, 1.5f));
		partdefinition.addOrReplaceChild("standlowlb", CubeListBuilder.create().texOffs(0, 26)
			.addBox(0f, 0f, 0f, 1, 4, 1).mirror(), PartPose.offset(1.5f, 0, 1.5f));
		partdefinition.addOrReplaceChild("standlowlf", CubeListBuilder.create().texOffs(0, 26)
			.addBox(0f, 0f, 0f, 1, 4, 1).mirror(), PartPose.offset(1.5f, 0, 13.5f));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void render(TileEscritoire escritoire, float partialTick, PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
		stack.pushPose();
		Direction facing = escritoire.getBlockState().getValue(BlockBase.FACING);
		RenderUtil.rotateByHorizontalDirection(stack, facing);
		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));

		this.root.render(stack, buffer, light, overlay);

		ItemStack displayStack = escritoire.getIndividualOnDisplay();
		if (!displayStack.isEmpty()) {
			stack.pushPose();
			stack.translate(0.5, 0.65, 0.5);
			stack.scale(0.75f, 0.75f, 0.75f);
			RenderUtil.renderDisplayStack(stack, this.itemRenderer, displayStack, escritoire.getLevel(), partialTick, buffers, light);
			stack.popPose();
		}
		stack.popPose();
	}
}
