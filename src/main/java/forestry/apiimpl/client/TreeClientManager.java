package forestry.apiimpl.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;

import net.minecraft.resources.ResourceLocation;

import com.mojang.datafixers.util.Pair;

import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.client.arboriculture.ILeafSprite;
import forestry.api.client.arboriculture.ILeafTint;
import forestry.api.client.arboriculture.ITreeClientManager;

import org.jetbrains.annotations.Nullable;

public class TreeClientManager implements ITreeClientManager {
	private final IdentityHashMap<ITreeSpecies, ILeafSprite> sprites;
	private final IdentityHashMap<ITreeSpecies, ILeafTint> tints;
	private final IdentityHashMap<ITreeSpecies, Pair<ResourceLocation, ResourceLocation>> models;

	public TreeClientManager(IdentityHashMap<ITreeSpecies, ILeafSprite> sprites, IdentityHashMap<ITreeSpecies, ILeafTint> tints, IdentityHashMap<ITreeSpecies, Pair<ResourceLocation, ResourceLocation>> models) {
		this.sprites = sprites;
		this.tints = tints;
		this.models = models;
	}

	@Override
	public ILeafSprite getLeafSprite(@Nullable ITreeSpecies species) {
		return this.sprites.get(species);
	}

	// todo should these be sorted?
	@Override
	public Collection<ILeafSprite> getAllLeafSprites() {
		// remove duplicates
		return new HashSet<>(this.sprites.values());
	}

	@Override
	public ILeafTint getTint(@Nullable ITreeSpecies species) {
		return this.tints.getOrDefault(species, ILeafTint.DEFAULT);
	}

	@Override
	public Pair<ResourceLocation, ResourceLocation> getSaplingModels(ITreeSpecies species) {
		Pair<ResourceLocation, ResourceLocation> models = this.models.get(species);
		if (models == null) {
			throw new IllegalArgumentException("Species " + species.id() + " has no registered sapling models");
		}
		return models;
	}

	@Override
	public Collection<Pair<ResourceLocation, ResourceLocation>> getAllSaplingModels() {
		return Collections.unmodifiableCollection(this.models.values());
	}
}
