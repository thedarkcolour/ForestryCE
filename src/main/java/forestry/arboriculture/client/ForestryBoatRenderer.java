package forestry.arboriculture.client;

import java.util.EnumMap;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

import com.mojang.datafixers.util.Pair;

import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.entities.ForestryBoat;

import static forestry.api.ForestryConstants.forestry;

public class ForestryBoatRenderer extends BoatRenderer {
	private final EnumMap<ForestryWoodType, Pair<ResourceLocation, ListModel<Boat>>> boatResources;

	public ForestryBoatRenderer(EntityRendererProvider.Context ctx, boolean hasChest) {
		super(ctx, hasChest);

		this.boatResources = new EnumMap<>(ForestryWoodType.class);
		for (ForestryWoodType type : ForestryWoodType.VALUES) {
			var pair = new Pair<>(forestry(getTextureLocation(type, hasChest)), createBoatModel(ctx, type, hasChest));
			this.boatResources.put(type, pair);
		}
	}

	private static String getTextureLocation(ForestryWoodType type, boolean hasChest) {
		return (hasChest ? "textures/entity/chest_boat/" : "textures/entity/boat/") + type.getSerializedName() + ".png";
	}

	private static ListModel<Boat> createBoatModel(EntityRendererProvider.Context ctx, ForestryWoodType type, boolean hasChest) {
		ModelLayerLocation modelLoc = createBoatModelLocation(type, hasChest);
		ModelPart root = ctx.bakeLayer(modelLoc);

		return hasChest ? new ChestBoatModel(root) : new BoatModel(root);
	}

	public static ModelLayerLocation createBoatModelLocation(ForestryWoodType type, boolean hasChest) {
		return new ModelLayerLocation(forestry((hasChest ? "chest_boat/" : "boat/") + type.getSerializedName()), "main");
	}

	@Override
	public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) {
		return boat instanceof ForestryBoat forestryBoat ? this.boatResources.get(forestryBoat.getWoodType()) : super.getModelWithLocation(boat);
	}
}
