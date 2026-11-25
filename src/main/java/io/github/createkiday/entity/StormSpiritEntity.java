package io.github.createkiday.entity;

import io.github.createkiday.advancement.KidayAdvancements;
import io.github.createkiday.registry.ModDamageTypes;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** A neutral, flying spirit that retaliates when attacked. */
public final class StormSpiritEntity extends Monster {
    private static final int ATTACK_PERIOD_TICKS = 60;
    private static final int ATTACK_WARNING_TICKS = 10;
    private static final double ATTACK_RANGE = 16.0;
    private static final float ATTACK_DAMAGE = 0.5F;

    private int attackTicks = ATTACK_PERIOD_TICKS;
    private int encounterCheckTicks;

    public StormSpiritEntity(EntityType<? extends StormSpiritEntity> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 20, true);
        setNoGravity(true);
        xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FLYING_SPEED, 0.24)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 0.65));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        // Neutral: no player target is selected until a player attacks this mob.
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel serverLevel)) return;
        awardEncounterAdvancement(serverLevel);

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            attackTicks = Math.min(attackTicks, ATTACK_PERIOD_TICKS);
            return;
        }

        double distance = distanceToSqr(target);
        getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (distance > 9.0) getNavigation().moveTo(target, 1.0);
        else getNavigation().stop();

        if (distance > ATTACK_RANGE * ATTACK_RANGE) return;
        if (attackTicks > 0) attackTicks--;
        if (attackTicks > 0 && attackTicks <= ATTACK_WARNING_TICKS) {
            Vec3 mark = target.getBoundingBox().getCenter();
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    mark.x, mark.y, mark.z, 3, 0.22, 0.28, 0.22, 0.01);
        } else if (attackTicks == 0) {
            strike(serverLevel, target);
            attackTicks = ATTACK_PERIOD_TICKS;
        }
    }

    private void awardEncounterAdvancement(ServerLevel level) {
        if (--encounterCheckTicks > 0) return;
        encounterCheckTicks = 20;
        List<ServerPlayer> nearby = level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(24.0));
        for (ServerPlayer player : nearby) KidayAdvancements.award(player, "encounter_storm_spirit", "meet");
    }

    private void strike(ServerLevel level, LivingEntity target) {
        if (!target.isAlive() || distanceToSqr(target) > ATTACK_RANGE * ATTACK_RANGE) return;
        DamageSource damage = level.damageSources().source(ModDamageTypes.LEI_GONG_ELECTRICITY, this);
        target.hurt(damage, ATTACK_DAMAGE);
        Vec3 from = getEyePosition();
        Vec3 to = target.getBoundingBox().getCenter();
        for (int i = 1; i <= 6; i++) {
            Vec3 point = from.lerp(to, i / 7.0);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z,
                    2, 0.04, 0.04, 0.04, 0.01);
        }
    }

    @Override
    public boolean isSensitiveToWater() {
        return false;
    }

}
