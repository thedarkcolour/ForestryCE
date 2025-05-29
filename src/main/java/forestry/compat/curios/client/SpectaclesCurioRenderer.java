package forestry.compat.curios.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import forestry.api.ForestryConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class SpectaclesCurioRenderer implements ICurioRenderer {
	private static final ResourceLocation TEXTURE = ForestryConstants.forestry("textures/item/naturalist_armor_1.png");

	private final HumanoidArmorModel<AbstractClientPlayer> armorModel;

	public SpectaclesCurioRenderer() {
		EntityModelSet models = Minecraft.getInstance().getEntityModels();

		// we don't care about slim model since we're just rendered on the head
		this.armorModel = new HumanoidArmorModel<>(models.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
		this.armorModel.setAllVisible(false);
		this.armorModel.head.visible = true;
		this.armorModel.hat.visible = true;
	}

	@Override
	public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext context, PoseStack poseStack, RenderLayerParent<T, M> parent, MultiBufferSource buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (parent instanceof PlayerRenderer renderer) {
			// HumanoidArmorLayer.renderArmorPiece
			renderer.getModel().copyPropertiesTo(this.armorModel);
			renderModel(poseStack, buffers, light);

			if (stack.hasFoil()) {
				renderGlint(poseStack, buffers, light);
			}
		}
	}

	// HumanoidArmorLayer.renderModel (with known args inlined)
	private void renderModel(PoseStack poseStack, MultiBufferSource buffers, int light) {
		VertexConsumer buffer = buffers.getBuffer(RenderType.armorCutoutNoCull(TEXTURE));
		this.armorModel.renderToBuffer(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
	}

	// HumanoidArmorLayer.renderGlint (with known args inlined)
	private void renderGlint(PoseStack poseStack, MultiBufferSource buffers, int light) {
		this.armorModel.renderToBuffer(poseStack, buffers.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY);
	}
}
