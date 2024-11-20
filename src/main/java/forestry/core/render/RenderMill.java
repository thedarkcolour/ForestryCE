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
import forestry.core.tiles.TileMill;
import forestry.core.utils.RenderUtil;

// This is called Mill because it used to be for the Forester and Treetap blocks in older versions of Forestry
public class RenderMill implements BlockEntityRenderer<TileMill> {
	private enum Textures {PEDESTAL, EXTENSION, BLADE, CHARGE}

	private final ResourceLocation[] textures;
	private final ModelPart pedestal;
	private final ModelPart column;
	private final ModelPart extension;
	private final ModelPart blade;

	public RenderMill(BlockEntityRendererProvider.Context ctx, String baseTexture) {
		ModelPart root = ctx.bakeLayer(ForestryModelLayers.MILL_LAYER);

		this.pedestal = root.getChild(Textures.PEDESTAL.name());
		this.column = root.getChild(Textures.CHARGE.name());
		this.extension = root.getChild(Textures.EXTENSION.name());
		this.blade = root.getChild(Textures.BLADE.name());

		this.textures = new ResourceLocation[11];

		this.textures[Textures.PEDESTAL.ordinal()] = ForestryConstants.forestry(baseTexture + "pedestal.png");
		this.textures[Textures.EXTENSION.ordinal()] = ForestryConstants.forestry(baseTexture + "extension.png");
		this.textures[Textures.BLADE.ordinal()] = ForestryConstants.forestry(baseTexture + "blade.png");

		for (int i = 0; i < 8; i++) {
			this.textures[Textures.CHARGE.ordinal() + i] = ForestryConstants.forestry(baseTexture + "column_" + i + ".png");
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild(Textures.PEDESTAL.name(), CubeListBuilder.create().texOffs(0, 0)
				.addBox(0f, 0f, 0f, 16, 1, 16), PartPose.offset(0, 0, 0));
		partdefinition.addOrReplaceChild(Textures.CHARGE.name(), CubeListBuilder.create().texOffs(0, 0)
				.addBox(0f, 0f, 0f, 4, 15, 4), PartPose.offset(6, 1, 6));
		partdefinition.addOrReplaceChild(Textures.EXTENSION.name(), CubeListBuilder.create().texOffs(0, 0)
				.addBox(0f, 0f, 0f, 14, 2, 2), PartPose.offset(1, 8, 7));
		partdefinition.addOrReplaceChild(Textures.BLADE.name(), CubeListBuilder.create().texOffs(0, 0)
				.addBox(0f, 0f, 0f, 1, 12, 8), PartPose.offset(10, 3, 4));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void render(TileMill mill, float partialTick, PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
		stack.pushPose();

		// todo implement and add rotated voxel shape
		Direction orientation = Direction.SOUTH;//mill.getBlockState().getValue(BlockBase.FACING);
		RenderUtil.rotateByHorizontalDirection(stack, orientation);

		this.pedestal.render(stack, buffers.getBuffer(RenderType.entityCutout(textures[Textures.PEDESTAL.ordinal()])), light, overlay);
		this.column.render(stack, buffers.getBuffer(RenderType.entityCutout(textures[Textures.CHARGE.ordinal() + mill.charge])), light, overlay);
		this.extension.render(stack, buffers.getBuffer(RenderType.entityCutout(textures[Textures.EXTENSION.ordinal()])), light, overlay);

		float step = getBladeStep(mill, partialTick) / 16f;
		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(textures[Textures.BLADE.ordinal()]));
		stack.pushPose();
		stack.translate(step, 0, 0);
		this.blade.render(stack, buffer, light, overlay);
		stack.popPose();
		stack.translate(1, 0, 1);
		stack.mulPose(Axis.YP.rotation(Mth.PI));
		stack.translate(step, 0, 0);
		this.blade.render(stack, buffer, light, overlay);

		stack.popPose();
	}

	private static float getBladeStep(TileMill mill, float partialTick) {
		float progress;

		if (mill.hasLevel()) {
			progress = mill.progress;
			if (mill.stage != 0) {
				float smoothing = mill.speed * partialTick;
				progress = (progress + smoothing);
			}
		} else {
			progress = 0.0f;
		}

		if (progress > 0.5f) {
			return 3.99f - (progress - 0.5f) * 2f * 3.99f;
		} else {
			return progress * 2f * 3.99f;
		}
	}
}
