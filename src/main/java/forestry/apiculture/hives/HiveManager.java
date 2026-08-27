package forestry.apiculture.hives;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.apiculture.hives.IHive;
import forestry.api.apiculture.hives.IHiveDrop;
import forestry.api.apiculture.hives.IHiveManager;
import forestry.apiculture.bees.BeeHousingListener;
import forestry.apiculture.bees.BeeHousingModifier;
import forestry.apiculture.bees.BeeHousingBeekeepingLogic;
import forestry.api.apiculture.hives.VillageHive;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;
import forestry.api.ForestryDataMaps;

public class HiveManager implements IHiveManager {
	private final ImmutableMap<ResourceLocation, IHive> registry;

	private final ImmutableList<VillageHive> commonVillageHives;
	private final ImmutableList<VillageHive> rareVillageHives;
	private final Object2FloatOpenHashMap<Item> swarmerMaterials;

	public HiveManager(ImmutableMap<ResourceLocation, IHive> registry, ImmutableList<VillageHive> commonVillageHives, ImmutableList<VillageHive> rareVillageHives, Object2FloatOpenHashMap<Item> swarmerMaterials) {
		this.registry = registry;
		this.commonVillageHives = commonVillageHives;
		this.rareVillageHives = rareVillageHives;
		this.swarmerMaterials = swarmerMaterials;
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
		if (chance != null) {
			return chance;
		}
		// Falls back to the deprecated plugin registration, so a mod that has not moved to the data
		// map keeps working. A pack overrides either one by writing the data map entry itself
		return this.swarmerMaterials.getFloat(swarmItem);
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
