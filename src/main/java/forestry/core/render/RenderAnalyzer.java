package forestry.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import forestry.api.ForestryConstants;
import forestry.core.blocks.BlockBase;
import forestry.core.config.Constants;
import forestry.core.tiles.TileAnalyzer;
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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

// todo replace with static block model and just render the item
public class RenderAnalyzer implements BlockEntityRenderer<TileAnalyzer> {
	private static final String TOWER2 = "tower2";
	private static final String TOWER1 = "tower1";
	private static final String COVER = "cover";
	private static final String PEDESTAL = "pedestal";

	private static final ResourceLocation TEXTURE0 = ForestryConstants.forestry(Constants.TEXTURE_PATH_BLOCK + "/analyzer_pedestal.png");
	private static final ResourceLocation TEXTURE1 = ForestryConstants.forestry(Constants.TEXTURE_PATH_BLOCK + "/analyzer_tower1.png");
	private static final ResourceLocation TEXTURE2 = ForestryConstants.forestry(Constants.TEXTURE_PATH_BLOCK + "/analyzer_tower2.png");

	private final ItemRenderer itemRenderer;
	private final ModelPart pedestal;
	private final ModelPart cover;
	private final ModelPart tower1;
	private final ModelPart tower2;

	public RenderAnalyzer(BlockEntityRendererProvider.Context ctx) {
		this.itemRenderer = ctx.getItemRenderer();

		ModelPart root = ctx.bakeLayer(ForestryModelLayers.ANALYZER_LAYER);
		this.pedestal = root.getChild(PEDESTAL);
		this.cover = root.getChild(COVER);
		this.tower1 = root.getChild(TOWER1);
		this.tower2 = root.getChild(TOWER2);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild(PEDESTAL, CubeListBuilder.create().texOffs(0, 0)
			.addBox(0, 0, 0, 16, 1, 16), PartPose.offset(0, 0, 0));
		partdefinition.addOrReplaceChild(COVER, CubeListBuilder.create().texOffs(0, 0)
			.addBox(0, 0, 0, 16, 1, 16), PartPose.offsetAndRotation(16, 16, 0, 0, 0, Mth.PI));
		partdefinition.addOrReplaceChild(TOWER1, CubeListBuilder.create().texOffs(0, 0)
			.addBox(0, 0, 0, 2, 14, 14), PartPose.offset(0, 1, 1));
		partdefinition.addOrReplaceChild(TOWER2, CubeListBuilder.create().texOffs(0, 0)
			.addBox(0, 0, 0, 2, 14, 14), PartPose.offset(14, 1, 1));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void render(TileAnalyzer analyzer, float partialTick, PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
		stack.pushPose();
		Direction facing = analyzer.getBlockState().getValue(BlockBase.FACING);
		RenderUtil.rotateByHorizontalDirection(stack, facing);

		VertexConsumer buffer0 = buffers.getBuffer(RenderType.entityCutout(TEXTURE0));
		this.pedestal.render(stack, buffer0, light, overlay);
		this.cover.render(stack, buffer0, light, overlay);
		this.tower1.render(stack, buffers.getBuffer(RenderType.entityCutout(TEXTURE1)), light, overlay);
		this.tower2.render(stack, buffers.getBuffer(RenderType.entityCutout(TEXTURE2)), light, overlay);

		stack.popPose();

		ItemStack displayStack = analyzer.getIndividualOnDisplay();
		if (!displayStack.isEmpty()) {
			stack.pushPose();
			stack.translate(0.5f, 0.2f, 0.5f);

			RenderUtil.renderDisplayStack(stack, this.itemRenderer, displayStack, analyzer.getLevel(), partialTick, buffers, light);

			stack.popPose();
		}
	}
}
