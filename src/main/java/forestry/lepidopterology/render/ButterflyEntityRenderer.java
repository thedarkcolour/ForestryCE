package forestry.lepidopterology.render;

import com.mojang.blaze3d.vertex.PoseStack;
import forestry.core.render.ForestryModelLayers;
import forestry.lepidopterology.entities.EntityButterfly;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ButterflyEntityRenderer extends MobRenderer<EntityButterfly, ButterflyModel> {
	public ButterflyEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new ButterflyModel(context.bakeLayer(ForestryModelLayers.BUTTERFLY_LAYER)), 0.25f);
	}

	@Override
	public void render(EntityButterfly entity, float entityYaw, float partialTickTime, PoseStack transform, MultiBufferSource buffer, int packedLight) {
		if (!entity.isRenderable()) {
			return;
		}

		transform.pushPose();
		transform.translate(0, 0.2, 0);
        this.model.setScale(entity.getSize());
		super.render(entity, entityYaw, partialTickTime, transform, buffer, packedLight);
		transform.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(EntityButterfly entity) {
		return entity.getTexture();
	}

	@Override
	protected float getBob(EntityButterfly entity, float partialTickTime) {
		return entity.getWingFlap(partialTickTime);
	}
}
