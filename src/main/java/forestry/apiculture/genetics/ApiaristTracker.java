package forestry.apiculture.genetics;

import forestry.api.IForestryApi;
import forestry.api.apiculture.IApiaristTracker;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.IMutationManager;
import forestry.api.genetics.ISpecies;
import forestry.core.genetics.BreedingTracker;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.nbt.CompoundTag;

public class ApiaristTracker extends BreedingTracker implements IApiaristTracker {
	private int queensTotal = 0;
	private int dronesTotal = 0;
	private int princessesTotal = 0;

	public ApiaristTracker() {
		super(ForestrySpeciesTypes.BEE);
	}

	@Override
	public void writeToNbt(CompoundTag nbt) {
		super.writeToNbt(nbt);

		writeUpdateData(nbt);
	}

	@Override
	protected void writeUpdateData(CompoundTag nbt) {
		nbt.putInt("QueensTotal", this.queensTotal);
		nbt.putInt("PrincessesTotal", this.princessesTotal);
		nbt.putInt("DronesTotal", this.dronesTotal);
	}

	@Override
	public void readFromNbt(CompoundTag nbt) {
		super.readFromNbt(nbt);

		this.queensTotal = nbt.getInt("QueensTotal");
		this.princessesTotal = nbt.getInt("PrincessesTotal");
		this.dronesTotal = nbt.getInt("DronesTotal");
	}

	@Override
	public void registerPickup(ISpecies<?> species) {
		IMutationManager<ISpecies<?>> manager = IForestryApi.INSTANCE.getGeneticManager().getMutations(SpeciesUtil.BEE_TYPE.get());

		if (manager.getMutationsFrom(species).isEmpty()) {
			registerSpecies(species);
		}
	}

	@Override
	public void registerQueen(IBee bee) {
		this.queensTotal++;
		registerBirth(bee.getSpecies());
	}

	@Override
	public int getQueenCount() {
		return this.queensTotal;
	}

	@Override
	public void registerPrincess(IBee bee) {
		this.princessesTotal++;
		registerBirth(bee.getSpecies());
	}

	@Override
	public int getPrincessCount() {
		return this.princessesTotal;
	}

	@Override
	public void registerDrone(IBee bee) {
		this.dronesTotal++;
		registerBirth(bee.getSpecies());
	}

	@Override
	public int getDroneCount() {
		return this.dronesTotal;
	}
}
