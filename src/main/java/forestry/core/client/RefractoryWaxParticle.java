package forestry.core.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class RefractoryWaxParticle implements ParticleProvider<SimpleParticleType> {
	private final SpriteSet sprite;

	public RefractoryWaxParticle(SpriteSet sprites) {
		this.sprite = sprites;
	}

	public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		GlowParticle particle = new GlowParticle(level, x, y, z, 0, 0, 0, this.sprite);
		particle.setColor(0.74509805f, 0.16470589f, 0.16470589f);
		particle.setParticleSpeed(xSpeed * 0.01 / 2, ySpeed * 0.01, zSpeed * 0.01 / 2);
		particle.setLifetime(level.random.nextInt(30) + 10);
		return particle;
	}
}
