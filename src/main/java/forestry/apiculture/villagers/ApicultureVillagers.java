package forestry.apiculture.villagers;

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;

import forestry.api.ForestryTags;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.blocks.BlockTypeApiculture;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.items.EnumPropolis;
import forestry.core.blocks.BlockTypeCoreTesr;
import forestry.core.features.CoreBlocks;
import forestry.core.registration.VillagerTrade;
import forestry.core.registration.VillagerTrade.GiveEmeraldForItem;
import forestry.core.registration.VillagerTrade.GiveItemForEmeralds;
import forestry.core.registration.VillagerTrade.GiveItemForItemAndEmerald;
import forestry.core.registration.VillagerTrade.GiveItemForLogAndEmerald;
import forestry.core.registration.VillagerTrade.GiveItemForTwoItems;
import forestry.core.utils.SpeciesUtil;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

@FeatureProvider
public class ApicultureVillagers {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	private static final DeferredRegister<PoiType> POINTS_OF_INTEREST = REGISTRY.getRegistry(Registries.POINT_OF_INTEREST_TYPE);
	private static final DeferredRegister<VillagerProfession> PROFESSIONS = REGISTRY.getRegistry(Registries.VILLAGER_PROFESSION);

	public static final RegistryObject<PoiType> POI_ESCRITOIRE = POINTS_OF_INTEREST.register("escritoire", () -> new PoiType(Set.copyOf(CoreBlocks.BASE.get(BlockTypeCoreTesr.ESCRITOIRE).block().getStateDefinition().getPossibleStates()), 1, 1));
	public static final RegistryObject<VillagerProfession> PROF_BEEKEEPER = PROFESSIONS.register("beekeeper", () -> {
		ResourceKey<PoiType> key = Objects.requireNonNull(POI_ESCRITOIRE.getKey());
		Predicate<Holder<PoiType>> jobSitePredicate = e -> e.is(key);
		return new VillagerProfession("beekeeper", jobSitePredicate, jobSitePredicate, ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LIBRARIAN);
	});

	public static void villagerTrades(VillagerTradesEvent event) {
		if (event.getType().equals(PROF_BEEKEEPER.get())) {
			Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
			List<Item> combs = ServerLifecycleHooks.getCurrentServer().registryAccess().registryOrThrow(Registries.ITEM).getTag(ForestryTags.Items.VILLAGE_COMBS).get().stream()
					.map(Holder::get)
					.toList();

			trades.get(1).add(new GiveHoneyCombForItem(combs, Items.WHEAT, new VillagerTrade.PriceInterval(2, 4), new VillagerTrade.PriceInterval(8, 12), 8, 2, 0F));
			trades.get(1).add(new GiveHoneyCombForItem(combs, Items.CARROT, new VillagerTrade.PriceInterval(2, 4), new VillagerTrade.PriceInterval(8, 12), 8, 2, 0F));
			trades.get(1).add(new GiveHoneyCombForItem(combs, Items.POTATO, new VillagerTrade.PriceInterval(2, 4), new VillagerTrade.PriceInterval(8, 12), 8, 2, 0F));

			trades.get(2).add(new GiveItemForEmeralds(ApicultureItems.SMOKER.item(), new VillagerTrade.PriceInterval(1, 1), new VillagerTrade.PriceInterval(1, 4), 8, 6));
			trades.get(2).add(new GiveDroneForItems(ApicultureItems.PROPOLIS.stack(EnumPropolis.NORMAL).getItem(), new VillagerTrade.PriceInterval(2, 4), new VillagerTrade.PriceInterval(1, 1), 8, 6, 0F));

			trades.get(3).add(new GiveEmeraldForItem(ApicultureItems.BEE_PRINCESS.item(), new VillagerTrade.PriceInterval(1, 1), new VillagerTrade.PriceInterval(1, 1), 8, 10));
			trades.get(3).add(new GiveItemForEmeralds(ApicultureItems.FRAME_PROVEN.item(), new VillagerTrade.PriceInterval(1, 2), new VillagerTrade.PriceInterval(1, 6), 8, 10));
			trades.get(3).add(new GiveItemForLogAndEmerald(new VillagerTrade.PriceInterval(32, 64), new VillagerTrade.PriceInterval(16, 32), ApicultureBlocks.BASE.get(BlockTypeApiculture.APIARY).stack().getItem(), new VillagerTrade.PriceInterval(1, 1), 8, 10));

			trades.get(4).add(new GiveItemForItemAndEmerald(ApicultureItems.BEE_PRINCESS.item(), new VillagerTrade.PriceInterval(1, 1), new VillagerTrade.PriceInterval(10, 64), SpeciesUtil.getBeeSpecies(ForestryBeeSpecies.MONASTIC).createStack(BeeLifeStage.DRONE), new VillagerTrade.PriceInterval(1, 1), 8, 15));
			trades.get(4).add(new GiveItemForTwoItems(ApicultureItems.BEE_DRONE.item(), new VillagerTrade.PriceInterval(1, 1), Items.ENDER_EYE, new VillagerTrade.PriceInterval(12, 16), SpeciesUtil.getBeeSpecies(ForestryBeeSpecies.ENDED).createStack(BeeLifeStage.DRONE), new VillagerTrade.PriceInterval(1, 1), 8, 15));
		}
	}

	public record GiveHoneyCombForItem(List<Item> itemHoneyCombs,
									   Item buying,
									   VillagerTrade.PriceInterval sellingPriceInfo,
									   VillagerTrade.PriceInterval buyingPriceInfo,
									   int maxUses,
									   int xp,
									   float priceMult) implements VillagerTrades.ItemListing {

		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			ItemStack buy = new ItemStack(buying, buyingPriceInfo.getPrice(rand));
			ItemStack sell = new ItemStack(itemHoneyCombs.get(rand.nextInt(itemHoneyCombs.size())), sellingPriceInfo.getPrice(rand));
			return new MerchantOffer(buy, sell, maxUses, xp, priceMult);
		}
	}

	public record GiveDroneForItems(Item buying, VillagerTrade.PriceInterval buyingPriceInfo,
									VillagerTrade.PriceInterval sellingPriceInfo, int maxUses, int xp,
									float priceMult) implements VillagerTrades.ItemListing {

		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			ResourceLocation[] forestryMundane = new ResourceLocation[]{ForestryBeeSpecies.FOREST, ForestryBeeSpecies.MEADOWS, ForestryBeeSpecies.MODEST, ForestryBeeSpecies.WINTRY, ForestryBeeSpecies.TROPICAL, ForestryBeeSpecies.MARSHY};
			ItemStack randomHiveDrone = SpeciesUtil.getBeeSpecies(forestryMundane[rand.nextInt(forestryMundane.length)]).createStack(BeeLifeStage.DRONE);
			randomHiveDrone.setCount(sellingPriceInfo.getPrice(rand));

			return new MerchantOffer(new ItemStack(buying, buyingPriceInfo.getPrice(rand)), randomHiveDrone, maxUses, xp, priceMult);
		}
	}
}
