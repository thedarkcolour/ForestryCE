package forestry.core.registration;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.Optional;

public class VillagerTrade {
	public record GiveItemForEmeralds(Item sellingItem, PriceInterval sellingAmounts, PriceInterval emeraldAmounts,
									  int maxUses, int xp) implements VillagerTrades.ItemListing {
		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldAmounts.getPrice(rand)), new ItemStack(this.sellingItem, this.sellingAmounts.getPrice(rand)), this.maxUses, this.xp, 0.05f);
		}
	}

	public record GiveEmeraldForItem(Item buyingItem, PriceInterval buyingAmounts, PriceInterval emeraldAmounts,
									 int maxUses, int xp) implements VillagerTrades.ItemListing {
		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			return new MerchantOffer(new ItemCost(this.buyingItem, this.buyingAmounts.getPrice(rand)), new ItemStack(Items.EMERALD, this.emeraldAmounts.getPrice(rand)), this.maxUses, this.xp, 0.05f);
		}
	}

	public record GiveItemForItemAndEmerald(Item buyingItem, PriceInterval buyAmounts, PriceInterval emeralsAmounts,
											ItemStack sellingItem, PriceInterval sellingAmounts, int maxUses,
											int xp) implements VillagerTrades.ItemListing {
		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			ItemCost buy1 = new ItemCost(this.buyingItem, this.buyAmounts.getPrice(rand));
			ItemCost buy2 = new ItemCost(Items.EMERALD, this.emeralsAmounts.getPrice(rand));
			ItemStack sell = this.sellingItem.copyWithCount(this.sellingAmounts.getPrice(rand));
			return new MerchantOffer(buy1, Optional.of(buy2), sell, this.maxUses, this.xp, 0.05f);
		}
	}

	public record GiveItemForLogAndEmerald(PriceInterval buyAmounts, PriceInterval emeraldAmounts, Item sellingItem,
										   PriceInterval sellingAmounts, int maxUses,
										   int xp) implements VillagerTrades.ItemListing {
		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			// should this give forestry logs too?
			Item[] logsBlock = new Item[]{
				Items.ACACIA_LOG,
				Items.BIRCH_LOG,
				Items.DARK_OAK_LOG,
				Items.JUNGLE_LOG,
				Items.OAK_LOG,
				Items.SPRUCE_LOG,
				Items.MANGROVE_LOG,
				Items.CHERRY_LOG,
			};

			return new MerchantOffer(new ItemCost(logsBlock[rand.nextInt(logsBlock.length)], this.buyAmounts.getPrice(rand)), Optional.of(new ItemCost(Items.EMERALD, this.emeraldAmounts.getPrice(rand))), new ItemStack(this.sellingItem, this.sellingAmounts.getPrice(rand)), this.maxUses, this.xp, 0.05f);
		}
	}

	public record GiveItemForTwoItems(Item buyingItem, PriceInterval buyAmounts, Item buyingItem2,
									  PriceInterval buyAmounts2, ItemStack sellingItem, PriceInterval sellingAmounts,
									  int maxUses, int xp) implements VillagerTrades.ItemListing {
		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			ItemCost buy1 = new ItemCost(this.buyingItem, this.buyAmounts.getPrice(rand));
			ItemCost buy2 = new ItemCost(this.buyingItem2, this.buyAmounts2.getPrice(rand));
			ItemStack sell = this.sellingItem.copyWithCount(this.sellingAmounts.getPrice(rand));
			return new MerchantOffer(buy1, Optional.of(buy2), sell, this.maxUses, this.xp, 0.05f);
		}
	}

	public record PriceInterval(int min, int max) {
		public int getPrice(RandomSource rand) {
			return this.min >= this.max ? this.min : this.min + rand.nextInt(this.max - this.min + 1);
		}
	}
}
