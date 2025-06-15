package forestry.apiimpl.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import forestry.api.arboriculture.ICharcoalPileWall;
import forestry.api.arboriculture.genetics.ITreeSpecies;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITreeEffect;
import forestry.api.genetics.ISpeciesType;
import forestry.api.plugin.IArboricultureRegistration;
import forestry.api.plugin.ITreeSpeciesBuilder;
import forestry.arboriculture.TreeManager;
import forestry.arboriculture.charcoal.CharcoalPileWall;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ArboricultureRegistration extends SpeciesRegistration<ITreeSpeciesBuilder, ITreeSpecies, TreeSpeciesBuilder> implements IArboricultureRegistration {
	private final Registrar<ResourceLocation, IFruit, IFruit> fruits = new Registrar<>(IFruit.class);
	private final Registrar<ResourceLocation, ITreeEffect, ITreeEffect> effects = new Registrar<>(ITreeEffect.class);
	private final ImmutableMap.Builder<Block, Block> refractoryWaxables = ImmutableMap.builder();
	private final ImmutableList.Builder<ICharcoalPileWall> charcoalPitWalls = ImmutableList.builder();

	public ArboricultureRegistration(ISpeciesType<ITreeSpecies, ?> type) {
		super(type);
	}

	@Override
	protected TreeSpeciesBuilder createSpeciesBuilder(ResourceLocation id, String genus, String species, MutationsRegistration mutations) {
		return new TreeSpeciesBuilder(id, genus, species, mutations);
	}

	@Override
	public ITreeSpeciesBuilder registerSpecies(ResourceLocation id, String genus, String species, boolean dominant, TextColor escritoireColor, IWoodType woodType) {
		return register(id, genus, species)
			.setDominant(dominant)
			.setEscritoireColor(escritoireColor)
			.setWoodType(woodType);
	}

	@Override
	public void registerFruit(ResourceLocation id, IFruit fruit) {
		this.fruits.create(id, fruit);
	}

	@Override
	public void registerTreeEffect(ResourceLocation id, ITreeEffect effect) {
		this.effects.create(id, effect);
	}

	@Override
	public void registerRefractoryWaxable(Block block, Block waxedForm) {
		this.refractoryWaxables.put(block, waxedForm);
	}

	@Override
	public void registerCharcoalPitWall(BlockState state, int charcoal) {
		this.charcoalPitWalls.add(new CharcoalPileWall(state, charcoal));
	}

	@Override
	public void registerCharcoalPitWall(Block block, int charcoal) {
		this.charcoalPitWalls.add(new CharcoalPileWall(block, charcoal));
	}

	@Override
	public void registerCharcoalPitWall(ICharcoalPileWall wall) {
		this.charcoalPitWalls.add(wall);
	}

	public ImmutableMap<ResourceLocation, IFruit> getFruits() {
		return this.fruits.build();
	}

	public ImmutableMap<ResourceLocation, ITreeEffect> getEffects() {
		return this.effects.build();
	}

	public TreeManager buildTreeManager() {
		return new TreeManager(this.refractoryWaxables.build(), this.charcoalPitWalls.build());
	}
}
