package forestry.arboriculture.items;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.entities.ForestryBoat;
import forestry.arboriculture.entities.ForestryChestBoat;
import forestry.core.items.ItemForestry;

// BoatItem
public class ItemForestryBoat extends ItemForestry {
	private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

	private final ForestryWoodType type;
	private final boolean hasChest;

	public ItemForestryBoat(ForestryWoodType type, boolean hasChest) {
		super(new Properties().stacksTo(1));

		this.type = type;
		this.hasChest = hasChest;
	}

	@Override
	public Component getName(ItemStack itemstack) {
		// todo use vanilla names and data generation instead of this
		return Component.translatable("for." + (this.hasChest ? "chest_boat" : "boat") + ".grammar", Component.translatable("for.trees.woodType." + this.type));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);

		if (hit.getType() == HitResult.Type.MISS) {
			return InteractionResultHolder.pass(stack);
		} else {
			Vec3 view = player.getViewVector(1f);
			List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(view.scale(5)).inflate(1), ENTITY_PREDICATE);

			if (!list.isEmpty()) {
				Vec3 vec31 = player.getEyePosition();

				for (Entity entity : list) {
					AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());

					if (aabb.contains(vec31)) {
						return InteractionResultHolder.pass(stack);
					}
				}
			}

			if (hit.getType() == HitResult.Type.BLOCK) {
				ForestryBoat boat = getBoat(level, hit);
				boat.setWoodType(this.type);
				boat.setYRot(player.getYRot());
				if (!level.noCollision(boat, boat.getBoundingBox())) {
					return InteractionResultHolder.fail(stack);
				} else {
					if (!level.isClientSide) {
						level.addFreshEntity(boat);
						level.gameEvent(player, GameEvent.ENTITY_PLACE, hit.getLocation());
						if (!player.getAbilities().instabuild) {
							stack.shrink(1);
						}
					}

					player.awardStat(Stats.ITEM_USED.get(this));
					return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
				}
			} else {
				return InteractionResultHolder.pass(stack);
			}
		}
	}

	private ForestryBoat getBoat(Level level, HitResult hit) {
		Vec3 location = hit.getLocation();
		return this.hasChest ? new ForestryChestBoat(level, location.x, location.y, location.z) : new ForestryBoat(level, location.x, location.y, location.z);
	}
}
