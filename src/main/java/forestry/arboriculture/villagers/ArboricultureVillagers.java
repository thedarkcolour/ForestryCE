package forestry.arboriculture.villagers;

import com.google.common.collect.ImmutableSet;
import forestry.api.IForestryApi;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.genetics.ILifeStage;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.blocks.NaturalistChestBlockType;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.core.features.CoreBlocks;
import forestry.core.registration.VillagerTrade;
import forestry.core.utils.SpeciesUtil;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@FeatureProvider
public class ArboricultureVillagers {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.ARBORICULTURE);

	private static final DeferredRegister<PoiType> POINTS_OF_INTEREST = REGISTRY.getRegistry(Registries.POINT_OF_INTEREST_TYPE);
	private static final DeferredRegister<VillagerProfession> PROFESSIONS = REGISTRY.getRegistry(Registries.VILLAGER_PROFESSION);

	public static final Holder<PoiType> POI_TREE_CHEST = POINTS_OF_INTEREST.register("tree_chest", () -> new PoiType(Set.copyOf(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.ARBORIST_CHEST).block().getStateDefinition().getPossibleStates()), 1, 1));
	public static final Holder<VillagerProfession> ARBORIST = PROFESSIONS.register("arborist", () -> {
		ResourceKey<PoiType> key = Objects.requireNonNull(POI_TREE_CHEST.getKey());
		Predicate<Holder<PoiType>> jobSitePredicate = poi -> poi.is(key);
		return new VillagerProfession("arborist", jobSitePredicate, jobSitePredicate, ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_FISHERMAN);
	});

	public static void villagerTrades(VillagerTradesEvent event) {
		if (event.getType() == ARBORIST.value()) {
			Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
			trades.get(1).add(new GivePlanksForEmeralds(new VillagerTrade.PriceInterval(1, 4), new VillagerTrade.PriceInterval(10, 32), 8, 2, 0F));
			trades.get(1).add(new GivePollenForEmeralds(new VillagerTrade.PriceInterval(1, 1), new VillagerTrade.PriceInterval(1, 3), TreeLifeStage.SAPLING, 4, 8, 2, 0F));

			trades.get(2).add(new GivePlanksForEmeralds(new VillagerTrade.PriceInterval(1, 4), new VillagerTrade.PriceInterval(10, 32), 8, 6, 0F));
			trades.get(2).add(new GivePollenForEmeralds(new VillagerTrade.PriceInterval(2, 3), new VillagerTrade.PriceInterval(1, 1), TreeLifeStage.POLLEN, 6, 8, 6, 0F));
			trades.get(2).add(new VillagerTrade.GiveItemForEmeralds(ArboricultureItems.GRAFTER_PROVEN.item(), new VillagerTrade.PriceInterval(1, 1), new VillagerTrade.PriceInterval(1, 4), 8, 6));

			trades.get(3).add(new GiveLogsForEmeralds(new VillagerTrade.PriceInterval(2, 5), new VillagerTrade.PriceInterval(6, 18), 8, 2, 0F));

			trades.get(3).add(new GiveLogsForEmeralds(new VillagerTrade.PriceInterval(2, 5), new VillagerTrade.PriceInterval(6, 18), 8, 2, 0F));

			trades.get(4).add(new GivePollenForEmeralds(new VillagerTrade.PriceInterval(5, 20), new VillagerTrade.PriceInterval(1, 1), TreeLifeStage.POLLEN, 10, 8, 15, 0F));
			trades.get(4).add(new GivePollenForEmeralds(new VillagerTrade.PriceInterval(5, 20), new VillagerTrade.PriceInterval(1, 1), TreeLifeStage.SAPLING, 10, 8, 15, 0F));
		}
	}

	private record GivePlanksForEmeralds(VillagerTrade.PriceInterval emeraldsPriceInfo,
										 VillagerTrade.PriceInterval sellingPriceInfo, int maxUses, int xp,
										 float priceMult) implements VillagerTrades.ItemListing {
		@Override
		public MerchantOffer getOffer(@NotNull Entity trader, @NotNull RandomSource rand) {
			ForestryWoodType woodType = ForestryWoodType.getRandom(rand);
			ItemStack sellStack = IForestryApi.INSTANCE.getTreeManager().getWoodAccess().getStack(woodType, WoodBlockKind.PLANKS, false);
			sellStack.setCount(this.sellingPriceInfo.getPrice(rand));

			return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldsPriceInfo.getPrice(rand)), sellStack, this.maxUses, this.xp, this.priceMult);
		}
	}

	private record GiveLogsForEmeralds(VillagerTrade.PriceInterval emeraldsPriceInfo,
									   VillagerTrade.PriceInterval sellingPriceInfo, int maxUses, int xp,
									   float priceMult) implements VillagerTrades.ItemListing {
		@Override
		public MerchantOffer getOffer(@NotNull Entity trader, @NotNull RandomSource rand) {
			ForestryWoodType woodType = ForestryWoodType.getRandom(rand);
			ItemStack sellStack = IForestryApi.INSTANCE.getTreeManager().getWoodAccess().getStack(woodType, WoodBlockKind.LOG, false);
			sellStack.setCount(this.sellingPriceInfo.getPrice(rand));

			return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldsPriceInfo.getPrice(rand)), sellStack, this.maxUses, this.xp, this.priceMult);
		}
	}

	private record GivePollenForEmeralds(VillagerTrade.PriceInterval buyingPriceInfo,
										 VillagerTrade.PriceInterval sellingPriceInfo, ILifeStage stage,
										 int maxComplexity, int maxUses, int xp,
										 float priceMult) implements VillagerTrades.ItemListing {
		@Nullable
		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			// instead of copying the whole thing and then picking once
			List<ITreeSpecies> registeredSpecies = SpeciesUtil.getAllTreeSpecies();
			ArrayList<ITreeSpecies> potentialSpecies = new ArrayList<>();
			for (ITreeSpecies species : registeredSpecies) {
				if (species.getComplexity() <= this.maxComplexity) {
					potentialSpecies.add(species);
				}
			}

			if (potentialSpecies.isEmpty()) {
				return null;
			}

			ITreeSpecies chosenSpecies = potentialSpecies.get(rand.nextInt(potentialSpecies.size()));
			ItemStack sellStack = chosenSpecies.createStack(this.stage);
			sellStack.setCount(this.sellingPriceInfo.getPrice(rand));

			return new MerchantOffer(new ItemCost(Items.EMERALD, this.buyingPriceInfo.getPrice(rand)), sellStack, this.maxUses, this.xp, this.priceMult);
		}
	}
}
