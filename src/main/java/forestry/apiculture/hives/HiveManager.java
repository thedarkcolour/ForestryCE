package forestry.apiculture.hives;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import forestry.api.ForestryDataMaps;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.apiculture.hives.IHive;
import forestry.api.apiculture.hives.IHiveDrop;
import forestry.api.apiculture.hives.IHiveManager;
import forestry.api.apiculture.hives.VillageHive;
import forestry.apiculture.bees.BeeHousingBeekeepingLogic;
import forestry.apiculture.bees.BeeHousingListener;
import forestry.apiculture.bees.BeeHousingModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;

public class HiveManager implements IHiveManager {
	private final ImmutableMap<ResourceLocation, IHive> registry;

	private final ImmutableList<VillageHive> commonVillageHives;
	private final ImmutableList<VillageHive> rareVillageHives;

	public HiveManager(ImmutableMap<ResourceLocation, IHive> registry, ImmutableList<VillageHive> commonVillageHives, ImmutableList<VillageHive> rareVillageHives) {
		this.registry = registry;
		this.commonVillageHives = commonVillageHives;
		this.rareVillageHives = rareVillageHives;
	}

	@Override
	public List<IHive> getHives() {
		return this.registry.values().asList();
	}

	@Override
	public ImmutableList<VillageHive> getCommonVillageHives() {
		return this.commonVillageHives;
	}

	@Override
	public ImmutableList<VillageHive> getRareVillageHives() {
		return this.rareVillageHives;
	}

	@Override
	public List<IHiveDrop> getDrops(ResourceLocation id) {
		IHive hive = this.registry.get(id);
		if (hive == null) {
			return List.of();
		} else {
			return hive.getDrops();
		}
	}

	@Override
	public float getSwarmingMaterialChance(Item swarmItem) {
		Float chance = swarmItem.builtInRegistryHolder().getData(ForestryDataMaps.SWARMER_FEED);
		return chance != null ? chance : 0;
	}

	@Override
	public IBeekeepingLogic createBeekeepingLogic(IBeeHousing housing) {
		return new BeeHousingBeekeepingLogic(housing);
	}

	@Override
	public IBeeModifier createBeeHousingModifier(IBeeHousing housing) {
		return new BeeHousingModifier(housing);
	}

	@Override
	public IBeeListener createBeeHousingListener(IBeeHousing housing) {
		return new BeeHousingListener(housing);
	}
}
