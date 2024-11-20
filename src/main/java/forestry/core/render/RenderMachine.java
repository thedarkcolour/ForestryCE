/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.core.render;

import java.util.EnumMap;
import java.util.Locale;

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
import forestry.core.blocks.BlockBase;
import forestry.core.tiles.IRenderableTile;
import forestry.core.tiles.TileBase;
import forestry.core.utils.RenderUtil;

public class RenderMachine implements BlockEntityRenderer<TileBase> {
	private static final String BASE_FRONT = "basefront";
	private static final String BASE_BACK = "baseback";
	private static final String RESOURCE_TANK = "resourceTank";
	private static final String PRODUCT_TANK = "productTank";

	private final ModelPart basefront;
	private final ModelPart baseback;
	private final ModelPart resourceTank;
	private final ModelPart productTank;

	private final ResourceLocation textureBase;
	private final ResourceLocation textureResourceTank;
	private final ResourceLocation textureProductTank;

	private final EnumMap<EnumTankLevel, ResourceLocation> texturesTankLevels = new EnumMap<>(EnumTankLevel.class);

	public RenderMachine(BlockEntityRendererProvider.Context ctx, String baseTexture) {
		ModelPart root = ctx.bakeLayer(ForestryModelLayers.MACHINE_LAYER);

		basefront = root.getChild(BASE_FRONT);
		baseback = root.getChild(BASE_BACK);
		resourceTank = root.getChild(RESOURCE_TANK);
		productTank = root.getChild(PRODUCT_TANK);

		textureBase = ForestryConstants.forestry(baseTexture + "base.png");
		textureProductTank = ForestryConstants.forestry(baseTexture + "tank_product_empty.png");
		textureResourceTank = ForestryConstants.forestry(baseTexture + "tank_resource_empty.png");

		for (EnumTankLevel tankLevel : EnumTankLevel.values()) {
			if (tankLevel == EnumTankLevel.EMPTY) {
				continue;
			}
			String tankLevelString = tankLevel.toString().toLowerCase(Locale.ENGLISH);
			texturesTankLevels.put(tankLevel, ForestryConstants.forestry("textures/block/machine_tank_" + tankLevelString + ".png"));
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild(BASE_FRONT, CubeListBuilder.create().texOffs(0, 0)
				.addBox(0f, 0f, 0f, 16, 4, 16), PartPose.offset(0, 0, 0));
		root.addOrReplaceChild(BASE_BACK, CubeListBuilder.create().texOffs(0, 0)
				.addBox(0f, 0f, 0f, 16, 4, 16), PartPose.offset(0, 12, 0));
		root.addOrReplaceChild(RESOURCE_TANK, CubeListBuilder.create().texOffs(0, 0)
				.addBox(0f, 0f, 0f, 12, 16, 6), PartPose.offset(2, 0, 2));
		root.addOrReplaceChild(PRODUCT_TANK, CubeListBuilder.create().texOffs(0, 0)
				.addBox(0f, 0f, 0f, 12, 16, 6), PartPose.offset(2, 0, 8));

		return LayerDefinition.create(mesh, 64, 32);
	}

	private void renderTank(PoseStack stack, ModelPart tankModel, MultiBufferSource buffers, ResourceLocation textureBase, TankRenderInfo renderInfo, int light, int overlay) {
		tankModel.render(stack, buffers.getBuffer(RenderType.entityCutout(textureBase)), light, overlay);

		ResourceLocation textureResourceTankLevel = this.texturesTankLevels.get(renderInfo.getLevel());
		if (textureResourceTankLevel == null) {
			return;
		}

		int color = RenderUtil.getFluidColor(renderInfo.getFluidStack().getFluid());
		float r = (color >> 16 & 255) / 255f;
		float g = (color >> 8 & 255) / 255f;
		float b = (color & 255) / 255f;

		tankModel.render(stack, buffers.getBuffer(RenderType.entityCutout(textureResourceTankLevel)), light, overlay, r, g, b, 1.0f);
	}

	@Override
	public void render(TileBase machine, float partialTick, PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
		stack.pushPose();
		// apply direction rotation
		Direction orientation = machine.getBlockState().getValue(BlockBase.FACING);
		RenderUtil.rotateByHorizontalDirection(stack, orientation);
		// flip the machine on its side
		stack.translate(0.5, 0.5, 0.5);
		stack.mulPose(Axis.XP.rotation(-Mth.HALF_PI));
		stack.translate(-0.5, -0.5, -0.5);
		// render bases
		VertexConsumer base = buffers.getBuffer(RenderType.entityCutout(textureBase));
		this.basefront.render(stack, base, light, overlay);
		this.baseback.render(stack, base, light, overlay);

		// rotate for the tank "slices"
		stack.translate(0.5, 0.5, 0.5);
		stack.mulPose(Axis.YP.rotation(-Mth.HALF_PI));
		stack.translate(-0.5, -0.5, -0.5);
		// render the two tank "slices"
		IRenderableTile tile = ((IRenderableTile) machine);
		TankRenderInfo resourceTankInfo = tile.getResourceTankInfo();
		TankRenderInfo productTankInfo = tile.getProductTankInfo();
		renderTank(stack, resourceTank, buffers, textureResourceTank, resourceTankInfo, light, overlay);
		renderTank(stack, productTank, buffers, textureProductTank, productTankInfo, light, overlay);

		stack.popPose();
	}
}
