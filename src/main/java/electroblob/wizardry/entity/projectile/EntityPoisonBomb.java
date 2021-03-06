package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityPoisonBomb extends EntityBomb {

	public EntityPoisonBomb(World par1World){
		super(par1World);
	}

	public EntityPoisonBomb(World par1World, EntityLivingBase par2EntityLivingBase){
		super(par1World, par2EntityLivingBase);
	}

	public EntityPoisonBomb(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier,
			float blastMultiplier){
		super(par1World, par2EntityLivingBase, damageMultiplier, blastMultiplier);
	}

	public EntityPoisonBomb(World par1World, double par2, double par4, double par6){
		super(par1World, par2, par4, par6);
	}

	@Override
	protected void onImpact(RayTraceResult par1RayTraceResult){
		Entity entityHit = par1RayTraceResult.entityHit;

		if(entityHit != null){
			// This is if the poison bomb gets a direct hit
			float damage = 5 * damageMultiplier;

			entityHit.attackEntityFrom(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.POISON).setProjectile(),
					damage);

			if(entityHit instanceof EntityLivingBase && !MagicDamage.isEntityImmune(DamageType.POISON, entityHit))
				((EntityLivingBase)entityHit).addPotionEffect(new PotionEffect(MobEffects.POISON, 120, 1));
		}

		// Particle effect
		if(world.isRemote){
			for(int i = 0; i < 60 * blastMultiplier; i++){
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world,
						this.posX + (this.rand.nextDouble() * 4 - 2) * blastMultiplier,
						this.posY + (this.rand.nextDouble() * 4 - 2) * blastMultiplier,
						this.posZ + (this.rand.nextDouble() * 4 - 2) * blastMultiplier, 0.0d, 0.0d, 0.0d, 35,
						0.2f + rand.nextFloat() * 0.3f, 0.6f, 0.0f);
				Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world,
						this.posX + (this.rand.nextDouble() * 4 - 2) * blastMultiplier,
						this.posY + (this.rand.nextDouble() * 4 - 2) * blastMultiplier,
						this.posZ + (this.rand.nextDouble() * 4 - 2) * blastMultiplier, 0.0d, 0.0d, 0.0d, 0,
						0.2f + rand.nextFloat() * 0.2f, 0.8f, 0.0f);
			}
			// Spawning this after the other particles fixes the rendering colour bug. It's a bit of a cheat, but it
			// works pretty well.
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
		}

		if(!this.world.isRemote){

			this.playSound(SoundEvents.ENTITY_SPLASH_POTION_BREAK, 1.5F, rand.nextFloat() * 0.4F + 0.6F);
			this.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1.2F, 1.0f);

			double range = 3.0d * blastMultiplier;

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(range, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){
				if(target != entityHit && target != this.getThrower()
						&& !MagicDamage.isEntityImmune(DamageType.POISON, target)){
					target.attackEntityFrom(
							MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.POISON),
							4.0f * damageMultiplier);
					target.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1));
				}
			}

			this.setDead();
		}
	}
}
