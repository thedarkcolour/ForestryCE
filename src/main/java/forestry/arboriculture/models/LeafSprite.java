package forestry.arboriculture.models;

import forestry.api.client.arboriculture.ILeafSprite;
import net.minecraft.resources.ResourceLocation;

public class LeafSprite implements ILeafSprite {
	private final ResourceLocation fast;
	private final ResourceLocation fancy;
	private final ResourceLocation pollinatedFast;
	private final ResourceLocation pollinatedFancy;

	public LeafSprite(ResourceLocation fast, ResourceLocation fancy, ResourceLocation pollinatedFast, ResourceLocation pollinatedFancy) {
		this.fast = fast;
		this.fancy = fancy;
		this.pollinatedFast = pollinatedFast;
		this.pollinatedFancy = pollinatedFancy;
	}

	public static LeafSprite create(ResourceLocation id) {
		String namespace = id.getNamespace();
		String path = "block/leaves/" + id.getPath();

		return new LeafSprite(
			ResourceLocation.fromNamespaceAndPath(namespace, path + "_fast"),
			ResourceLocation.fromNamespaceAndPath(namespace, path),
			ResourceLocation.fromNamespaceAndPath(namespace, path + "_pollinated_fast"),
			ResourceLocation.fromNamespaceAndPath(namespace, path + "_pollinated")
		);
	}

	@Override
	public ResourceLocation get(boolean pollinated, boolean fancy) {
		if (pollinated) {
			return fancy ? this.pollinatedFancy : this.pollinatedFast;
		} else {
			return fancy ? this.fancy : this.fast;
		}
	}

	@Override
	public ResourceLocation getParticle() {
		return this.fancy;
	}
}

