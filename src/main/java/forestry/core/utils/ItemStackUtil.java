/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/

package forestry.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import forestry.api.core.IProduct;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public abstract class ItemStackUtil {
	private static final int[] EMPTY_CONSUME = new int[0];

	/**
	 * Compares item id, damage and NBT. Accepts wildcard damage.
	 */
	public static boolean isIdenticalItem(ItemStack lhs, ItemStack rhs) {
		return ItemStack.isSameItemSameTags(lhs, rhs);
	}

	/**
	 * Merges the giving stack into the receiving stack as far as possible
	 */
	public static void mergeStacks(ItemStack giver, ItemStack receptor) {
		int maxInsert = receptor.getMaxStackSize() - receptor.getCount();
		int maxExtract = giver.getCount();
		int canTransfer = Math.min(maxInsert, maxExtract);

		giver.shrink(canTransfer);
		receptor.grow(canTransfer);
	}

	public static List<ItemStack> condenseStacks(List<ItemStack> stacks) {
		Object2IntOpenHashMap<ItemStack> map = new Object2IntOpenHashMap<>();

		for (ItemStack stack : stacks) {
			ItemStack copy = stack.copy();
			copy.setCount(1);

			map.put(copy, map.getInt(copy) + stack.getCount());
		}

		ArrayList<ItemStack> condensed = new ArrayList<>(map.size());

		for (Object2IntMap.Entry<ItemStack> entry : map.object2IntEntrySet()) {
			ItemStack stack = entry.getKey();
			int count = entry.getIntValue();

			while (count > 0) {
				int transfer = Math.min(count, stack.getMaxStackSize());
				count -= transfer;

				ItemStack copy = stack.copy();
				copy.setCount(transfer);
				condensed.add(copy);
			}
		}

		return condensed;
	}

	public static boolean containsItemStack(Iterable<ItemStack> list, ItemStack itemStack) {
		for (ItemStack listStack : list) {
			if (isIdenticalItem(listStack, itemStack)) {
				return true;
			}
		}
		return false;
	}

	public static int[] createConsume(List<Ingredient> set, Container inventory, boolean craftingTools) {
		return createConsume(set, inventory.getContainerSize(), inventory::getItem, craftingTools);
	}

	public static int[] createConsume(List<Ingredient> set, int stockCount, IntFunction<ItemStack> stock, boolean craftingTools) {
		//A array that contains the amount of items that is needed from this stack
		int[] reqAmounts = new int[stockCount];
		int found = 0;
		for (Ingredient ing : set) {
			if (ing.isEmpty()) {
				found++;
				continue;
			}
			for (int i = 0; i < reqAmounts.length; i++) {
				ItemStack offer = stock.apply(i);

				if (offer.getCount() > reqAmounts[i] && ing.test(offer)) {
					reqAmounts[i] = reqAmounts[i] + 1;
					found++;
					break;
				}
			}
		}
		if (found < set.size()) {
			return EMPTY_CONSUME;
		}

		return reqAmounts;
	}

	/**
	 * Counts how many full sets are contained in the passed stock
	 */
	public static int containsSets(List<ItemStack> set, List<ItemStack> stock) {
		return containsSets(set, stock, false);
	}

	/**
	 * Counts how many full sets are contained in the passed stock
	 */
	public static int containsSets(List<ItemStack> set, List<ItemStack> stock, boolean craftingTools) {
		int totalSets = 0;

		List<ItemStack> condensedRequired = ItemStackUtil.condenseStacks(set);
		List<ItemStack> condensedOffered = ItemStackUtil.condenseStacks(stock);

		for (ItemStack req : condensedRequired) {
			int reqCount = 0;

			for (ItemStack offer : condensedOffered) {
				if (isCraftingEquivalent(req, offer, craftingTools)) {
					int stackCount = offer.getCount() / req.getCount();
					reqCount = Math.max(reqCount, stackCount);
				}
			}

			if (reqCount == 0) {
				return 0;
			} else if (totalSets == 0) {
				totalSets = reqCount;
			} else if (totalSets > reqCount) {
				totalSets = reqCount;
			}
		}

		return totalSets;
	}

	public static boolean equalSets(NonNullList<ItemStack> set1, NonNullList<ItemStack> set2) {
		if (set1 == set2) {
			return true;
		}

		int count = set1.size();

		if (count != set2.size()) {
			return false;
		}

		for (int i = 0; i < count; i++) {
			if (!isIdenticalItem(set1.get(i), set2.get(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Compare two item stacks for crafting equivalency without oreDictionary or craftingTools
	 */
	public static boolean isCraftingEquivalent(ItemStack base, ItemStack comparison) {
		if (base.isEmpty() || comparison.isEmpty()) {
			return false;
		}

		if (base.getItem() != comparison.getItem()) {
			return false;
		}

		// When the base stackTagCompound is null or empty, treat it as a wildcard for crafting
		if (base.getTag() == null || base.getTag().isEmpty()) {
			return true;
		} else {
			return base.getTag().equals(comparison.getTag());
		}
	}

	/**
	 * Compare two item stacks for crafting equivalency.
	 */
	public static boolean isCraftingEquivalent(ItemStack base, ItemStack comparison, boolean craftingTools) {
		if (base.isEmpty() || comparison.isEmpty()) {
			return false;
		}

		if (craftingTools && isCraftingToolEquivalent(base, comparison)) {
			return true;
		}

		return isCraftingEquivalent(base, comparison);
	}

	public static boolean isCraftingToolEquivalent(ItemStack base, ItemStack comparison) {
		if (base.isEmpty() || comparison.isEmpty()) {
			return false;
		}

		Item baseItem = base.getItem();

		if (baseItem != comparison.getItem()) {
			return false;
		}

		// tool uses NBT for damage
		//base.getItemDamage() == comparison.getItemDamage();
		return base.getTag() == null || base.getTag().isEmpty();
	}

	public static void dropItemStackAsEntity(ItemStack items, Level world, double x, double y, double z) {
		dropItemStackAsEntity(items, world, x, y, z, 10);
	}

	public static void dropItemStackAsEntity(ItemStack items, Level world, BlockPos pos) {
		dropItemStackAsEntity(items, world, pos.getX(), pos.getY(), pos.getZ(), 10);
	}

	public static void dropItemStackAsEntity(ItemStack items, Level world, double x, double y, double z, int delayForPickup) {
		if (items.isEmpty() || world.isClientSide) {
			return;
		}

		float f1 = 0.4F;
		double d = (world.random.nextFloat() * f1 + (1.0F - f1)) * 0.5D;
		double d1 = (world.random.nextFloat() * f1 + (1.0F - f1)) * 0.5D;
		double d2 = (world.random.nextFloat() * f1 + (1.0F - f1)) * 0.5D;
		ItemEntity entityitem = new ItemEntity(world, x + d, y + d1, z + d2, items);
		entityitem.setPickUpDelay(delayForPickup);

		world.addFreshEntity(entityitem);
	}

	public static ItemStack copyWithRandomSize(IProduct template, int max, RandomSource rand) {
		int size = max <= 0 ? 1 : rand.nextInt(max);
		ItemStack copy = template.createRandomStack(rand);
		copy.setCount(Math.min(size, copy.getMaxStackSize()));
		return copy;
	}

	/**
	 * Checks if two items are exactly the same, ignoring counts
	 */
	public static boolean areItemStacksEqualIgnoreCount(ItemStack a, ItemStack b) {
		int countB = b.getCount();
		b.setCount(a.getCount());
		boolean equals = a.equals(b, false);
		b.setCount(countB);
		return equals;
	}
}
