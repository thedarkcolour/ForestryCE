package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.bee.BeeLifeStage;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.IIndividualLiving;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class AgingBeeEffect extends NonStackingBeeEffect {
	protected boolean aging;

	public AgingBeeEffect(boolean dominant, boolean aging) {
		super(dominant);
		this.aging = aging;
	}

	@Override
	protected void doEffectForHive(Level level, IBeeHousing housing) {
		if (!housing.getErrorLogic().hasErrors()) {
			IIndividualHandlerItem handler = IIndividualHandlerItem.get(housing.getBeeInventory().getQueen());

			if (handler != null && handler.getStage() == BeeLifeStage.QUEEN) {
				IIndividual individual = handler.getIndividual();

				if (individual instanceof IIndividualLiving queen) {
					RandomSource rand = level.getRandom();
					int life = queen.getMaxHealth() / ForestryAlleles.LIFESPAN_NORMAL.value();

					if (rand.nextInt(ForestryAlleles.LIFESPAN_NORMAL.value()) < queen.getMaxHealth() % ForestryAlleles.LIFESPAN_NORMAL.value()) {
						// Ensure below normal lifespans are still affected, though to a lesser degree
						life++;
					}
					if (this.aging) {
						queen.setHealth(Math.max(1, queen.getHealth() - life));
					} else {
						queen.setHealth((int) Math.min(queen.getMaxHealth(), Math.min(Integer.MAX_VALUE, queen.getHealth() + (long) life)));
					}
					queen.saveToStack(housing.getBeeInventory().getQueen());
				}
			}
		}
	}
}
