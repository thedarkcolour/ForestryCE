package forestry.core.genetics;

import com.mojang.authlib.GameProfile;
import forestry.api.IForestryApi;
import forestry.api.genetics.*;
import forestry.core.features.CoreItems;
import forestry.core.genetics.mutations.EnumMutateChance;
import forestry.core.items.ItemForestry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemResearchNote extends ItemForestry {
	private static final String PARENT_0_KEY = "P0";
	private static final String PARENT_1_KEY = "P1";
	private static final String RESULT_KEY = "RS";
	private static final String TYPE_KEY = "ROT";

	private static final String RESEARCHER_KEY = "RES";
	private static final String NBT_INNER = "INN";

	public ItemResearchNote() {
		super(new Item.Properties());
	}

	@Override
	public Component getName(ItemStack itemstack) {
		ResearchNote note = new ResearchNote(itemstack.getTag());
		String researcherName;
		if (note.researcher == null) {
			researcherName = "Sengir";
		} else {
			researcherName = note.researcher.getName();
		}
		return Component.translatable(getDescriptionId(itemstack), researcherName);
	}

	@Override
	public void appendHoverText(ItemStack itemstack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, world, list, flag);
		ResearchNote note = new ResearchNote(itemstack.getTag());
		note.addTooltip(list);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack heldItem = playerIn.getItemInHand(handIn);
		if (worldIn.isClientSide) {
			return InteractionResultHolder.pass(heldItem);
		}

		ResearchNote note = new ResearchNote(heldItem.getTag());
		if (registerResults(worldIn, playerIn, note.inner)) {
			heldItem.shrink(1);
		}

		return InteractionResultHolder.success(heldItem);
	}

	@Nullable
	private static IMutation<?> getEncodedMutation(CompoundTag compound) {
		ISpeciesType<?, ?> type = IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypeSafe(new ResourceLocation(compound.getString(TYPE_KEY)));
		if (type == null) {
			return null;
		}

		ISpecies<?> parent0 = AlleleUtil.getSpecies(type, compound, PARENT_0_KEY);
		ISpecies<?> parent1 = AlleleUtil.getSpecies(type, compound, PARENT_1_KEY);
		if (parent0 == null || parent1 == null) {
			return null;
		}

		ISpecies<?> result = null;
		if (compound.contains(RESULT_KEY)) {
			result = AlleleUtil.getSpecies(type, compound, RESULT_KEY);
		}

		IMutation<?> encoded = null;
		IMutationManager mutations = type.getMutations();
		for (IMutation<?> mutation : (List<IMutation<?>>) mutations.getMutationsFrom(parent0)) {
			if (mutation.isPartner(parent1)) {
				if (result == null || mutation.getResult().id().equals(result.id())) {
					encoded = mutation;
					break;
				}
			}
		}

		return encoded;
	}

	public static List<Component> getTooltip(CompoundTag compound) {
		IMutation<?> mutation = getEncodedMutation(compound);
		if (mutation == null) {
			return List.of();
		}
		ArrayList<Component> tooltips = new ArrayList<>();

		Component species1 = Component.literal("'").append(mutation.getFirstParent().getDisplayName()).append("'").withStyle(ChatFormatting.YELLOW);
		Component species2 = Component.literal("'").append(mutation.getSecondParent().getDisplayName()).append("'").withStyle(ChatFormatting.YELLOW);
		String mutationChanceKey = EnumMutateChance.rateChance(mutation.getChance()).toString().toLowerCase(Locale.ENGLISH);
		Component mutationChance = Component.translatable("for.researchNote.chance." + mutationChanceKey).withStyle(ChatFormatting.BLUE);
		Component speciesResult = mutation.getResult().getDisplayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE);

		tooltips.add(Component.translatable("for.researchNote.discovery.0"));
		tooltips.add(Component.translatable("for.researchNote.discovery.1", species1, species2).withStyle(ChatFormatting.GRAY));
		tooltips.add(Component.translatable("for.researchNote.discovery.2", mutationChance).withStyle(ChatFormatting.GRAY));
		tooltips.add(Component.translatable("for.researchNote.discovery.3", speciesResult).withStyle(ChatFormatting.GRAY));

		if (!mutation.getSpecialConditions().isEmpty()) {
			for (Component line : mutation.getSpecialConditions()) {
				tooltips.add(line.copy().withStyle(ChatFormatting.GOLD));
			}
		}

		return tooltips;
	}

	public static boolean registerResults(Level level, Player player, CompoundTag compound) {
		IMutation<?> encoded = getEncodedMutation(compound);
		if (encoded == null) {
			return false;
		}

		IBreedingTracker tracker = encoded.getType().getBreedingTracker(level, player.getGameProfile());
		if (tracker.isResearched(encoded)) {
			player.sendSystemMessage(Component.translatable("for.chat.cannotmemorizeagain"));
			return false;
		}

		ISpecies<?> speciesFirst = encoded.getFirstParent();
		ISpecies<?> speciesSecond = encoded.getSecondParent();
		ISpecies<?> speciesResult = encoded.getResult();

		tracker.registerSpecies(speciesFirst);
		tracker.registerSpecies(speciesSecond);
		tracker.registerSpecies(speciesResult);

		tracker.researchMutation(encoded);
		player.sendSystemMessage(Component.translatable("for.chat.memorizednote"));

		player.sendSystemMessage(Component.translatable("for.chat.memorizednote2",
			speciesFirst.getDisplayName().withStyle(ChatFormatting.GRAY),
			speciesSecond.getDisplayName().withStyle(ChatFormatting.GRAY),
			speciesResult.getDisplayName().withStyle(ChatFormatting.GREEN)));

		return true;
	}

	public static ResearchNote createMutationNote(GameProfile researcher, IMutation<?> mutation) {
		CompoundTag compound = new CompoundTag();
		compound.putString(TYPE_KEY, mutation.getType().id().toString());
		compound.putString(PARENT_0_KEY, mutation.getFirstParent().id().toString());
		compound.putString(PARENT_1_KEY, mutation.getSecondParent().id().toString());
		compound.putString(RESULT_KEY, mutation.getResult().id().toString());
		return new ResearchNote(researcher, compound);
	}

	public static ItemStack createMutationNoteStack(GameProfile researcher, IMutation<?> mutation) {
		ResearchNote note = createMutationNote(researcher, mutation);
		CompoundTag compound = new CompoundTag();
		note.writeToNBT(compound);
		ItemStack created = new ItemStack(CoreItems.RESEARCH_NOTE.item());
		created.setTag(compound);
		return created;
	}

	public static class ResearchNote {
		@Nullable
		private final GameProfile researcher;
		private final CompoundTag inner;

		public ResearchNote(GameProfile researcher, CompoundTag inner) {
			this.researcher = researcher;
			this.inner = inner;
		}

		public ResearchNote(@Nullable CompoundTag nbt) {
			if (nbt != null) {
				if (nbt.contains(RESEARCHER_KEY)) {
					this.researcher = NbtUtils.readGameProfile(nbt.getCompound(RESEARCHER_KEY));
				} else {
					this.researcher = null;
				}
				this.inner = nbt.getCompound(NBT_INNER);
			} else {
				this.researcher = null;
				this.inner = new CompoundTag();
			}
		}

		public CompoundTag writeToNBT(CompoundTag compound) {
			if (this.researcher != null) {
				CompoundTag nbt = new CompoundTag();
				NbtUtils.writeGameProfile(nbt, this.researcher);
				compound.put(RESEARCHER_KEY, nbt);
			}
			compound.put(NBT_INNER, this.inner);
			return compound;
		}

		public void addTooltip(List<Component> list) {
			List<Component> tooltips = getTooltip(this.inner);
			if (tooltips.isEmpty()) {
				list.add(Component.translatable("for.researchNote.error.0").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
				list.add(Component.translatable("for.researchNote.error.1"));
				return;
			}

			list.addAll(tooltips);
		}
	}
}
