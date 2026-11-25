package io.github.createkiday.entity;

import io.github.createkiday.registry.ModDamageTypes;
import io.github.createkiday.registry.ModItems;
import io.github.createkiday.registry.ModMobEffects;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class LeiGongEntity extends Monster {
    public static final int ACTIVE_ATTACK_TICKS = 40;
    public static final int ATTACK_CYCLE_TICKS = 80;
    public static final int SPECIAL_COOLDOWN_TICKS = 200;
    public static final double ATTACK_RANGE = 12.0;
    public static final double CHAIN_RANGE = 4.0;
    public static final int MAX_CHAIN_TARGETS = 3;
    public static final float MELEE_DAMAGE = 2.0F;
    public static final float PRIMARY_DAMAGE = 6.0F;
    public static final float SECONDARY_DAMAGE = 1.0F;
    public static final int LIGHTNING_STRIKES = 2;
    public static final int LIGHTNING_GAP_TICKS = 20;
    public static final int LIGHTNING_WARNING_TICKS = 5;
    public static final float STUN_CHANCE = 0.05F;
    public static final int STUN_TICKS = 10;

    private static final EntityDataAccessor<Boolean> ELECTRIFYING = SynchedEntityData.defineId(
            LeiGongEntity.class, EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.WHITE,
            BossEvent.BossBarOverlay.PROGRESS);

    private int attackCycleTick;
    private int specialCooldown;
    private Vec3 aimedStrikePosition;
    private int stormCooldown;
    private int stormTicks;
    private int postStormTicks;
    private final List<BlockPos> stormTargets = new ArrayList<>();

    public LeiGongEntity(EntityType<? extends LeiGongEntity> type, Level level) {
        super(type, level);
        xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 250.0)
                .add(Attributes.MOVEMENT_SPEED, 0.184)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.ATTACK_DAMAGE, MELEE_DAMAGE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 20.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ELECTRIFYING, false);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (specialCooldown > 0) specialCooldown--;
        updateBossBar();
        if (stormCooldown > 0) stormCooldown--;
        if (postStormTicks > 0) { postStormTicks--; return; }
        if (currentPhase() == LeiGongBalance.Phase.THREE && stormTicks == 0 && stormCooldown == 0
                && level() instanceof ServerLevel serverLevel) beginLightningStorm(serverLevel);
        if (stormTicks > 0 && level() instanceof ServerLevel serverLevel) {
            tickLightningStorm(serverLevel);
            return;
        }

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || target.distanceToSqr(this) > 20.0 * 20.0) {
            setElectrifying(false);
            attackCycleTick = 0;
            aimedStrikePosition = null;
            return;
        }

        getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (target.distanceToSqr(this) > ATTACK_RANGE * ATTACK_RANGE) {
            getNavigation().moveTo(target, LeiGongBalance.movementMultiplier(currentPhase()));
            setElectrifying(false);
            attackCycleTick = 0;
            aimedStrikePosition = null;
            return;
        }

        getNavigation().stop();
        boolean active = attackCycleTick < ACTIVE_ATTACK_TICKS;
        setElectrifying(active);
        if (active && level() instanceof ServerLevel serverLevel) {
            if (attackCycleTick == 0 || attackCycleTick == LIGHTNING_GAP_TICKS) {
                aimedStrikePosition = target.position();
                sendArc(serverLevel, getEyePosition(), aimedStrikePosition.add(0, 0.1, 0));
            }
            if (attackCycleTick == LIGHTNING_WARNING_TICKS
                    || attackCycleTick == LIGHTNING_GAP_TICKS + LIGHTNING_WARNING_TICKS) {
                if (aimedStrikePosition != null) strike(serverLevel, aimedStrikePosition);
            }
        }
        attackCycleTick = (attackCycleTick + 1) % LeiGongBalance.attackCycleTicks(currentPhase());
    }

    private LeiGongBalance.Phase currentPhase() {
        return LeiGongBalance.phase(getHealth(), getMaxHealth());
    }

    private void updateBossBar() {
        bossEvent.setProgress(getHealth() / getMaxHealth());
        LeiGongBalance.Phase phase = currentPhase();
        bossEvent.setColor(switch (phase) {
            case ONE -> BossEvent.BossBarColor.WHITE;
            case TWO -> BossEvent.BossBarColor.BLUE;
            case THREE -> BossEvent.BossBarColor.PURPLE;
        });
        var movement = getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement != null) movement.setBaseValue(0.184 * LeiGongBalance.movementMultiplier(phase));
    }

    private void beginLightningStorm(ServerLevel level) {
        stormCooldown = 300;
        stormTicks = 80;
        stormTargets.clear();
        for (int i = 0; i < 5; i++) {
            int x = getBlockX() + level.random.nextIntBetweenInclusive(-12, 12);
            int z = getBlockZ() + level.random.nextIntBetweenInclusive(-12, 12);
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
            if (level.isLoaded(surface)) stormTargets.add(surface);
        }
    }

    private void tickLightningStorm(ServerLevel level) {
        if (stormTicks > 60 || stormTicks % 4 == 0) {
            for (BlockPos target : stormTargets)
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX() + 0.5, target.getY() + 0.2,
                        target.getZ() + 0.5, 6, 0.25, 0.15, 0.25, 0.01);
        }
        if ((stormTicks == 60 || stormTicks == 40 || stormTicks == 20) && !stormTargets.isEmpty()) {
            int index = Math.floorMod(stormTicks / 20, stormTargets.size());
            strikeStormTarget(level, stormTargets.get(index));
        }
        stormTicks--;
        if (stormTicks <= 0) {
            stormTicks = 0;
            postStormTicks = 60;
            stormTargets.clear();
        }
    }

    private void strikeStormTarget(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) return;
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            level.addFreshEntity(bolt);
        }
    }

    @Override public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    private void strike(ServerLevel level, Vec3 targetPoint) {
        DamageSource electricity = level.damageSources().source(ModDamageTypes.LEI_GONG_ELECTRICITY, this);
        AABB strikeArea = new AABB(targetPoint.x - 0.8, targetPoint.y, targetPoint.z - 0.8,
                targetPoint.x + 0.8, targetPoint.y + 2.0, targetPoint.z + 0.8);
        List<Player> struckPlayers = level.getEntitiesOfClass(Player.class, strikeArea,
                player -> player.isAlive() && player.distanceToSqr(targetPoint) <= 2.25);
        if (struckPlayers.isEmpty()) {
            sendArc(level, getEyePosition(), targetPoint.add(0, 1.0, 0));
            return;
        }
        for (Player player : struckPlayers) {
            player.invulnerableTime = 0;
            boolean hit = player.hurt(electricity, PRIMARY_DAMAGE);
            sendArc(level, getEyePosition(), player.getBoundingBox().getCenter());
            if (hit && player.isAlive() && random.nextFloat() < STUN_CHANCE) {
                player.addEffect(new MobEffectInstance(ModMobEffects.STUN, STUN_TICKS, 0, false, true, true), this);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0,
                        player.getZ(), 18, 0.35, 0.55, 0.35, 0.08);
            }
        }

        if (specialCooldown > 0) return;
        specialCooldown = SPECIAL_COOLDOWN_TICKS;
        Set<UUID> struck = new HashSet<>();
        struck.add(struckPlayers.getFirst().getUUID());
        LivingEntity previous = struckPlayers.getFirst();
        for (int index = 1; index < MAX_CHAIN_TARGETS; index++) {
            LivingEntity next = nextChainTarget(level, previous, struck);
            if (next == null) break;
            struck.add(next.getUUID());
            next.invulnerableTime = 0;
            next.hurt(electricity, SECONDARY_DAMAGE);
            sendArc(level, previous.getBoundingBox().getCenter(), next.getBoundingBox().getCenter());
            previous = next;
        }
    }

    /** The staff reuses LeiGong's lightning arc and chaining, without its stun effect. */
    public static void staffLightning(ServerLevel level, LivingEntity attacker, Vec3 targetPoint) {
        DamageSource electricity = level.damageSources().source(ModDamageTypes.LEI_GONG_ELECTRICITY, attacker);
        AABB strikeArea = new AABB(targetPoint.x - 0.8, targetPoint.y - 0.4, targetPoint.z - 0.8,
                targetPoint.x + 0.8, targetPoint.y + 1.6, targetPoint.z + 0.8);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, strikeArea,
                living -> living.isAlive() && living != attacker && !living.isAlliedTo(attacker));
        sendArc(level, attacker.getEyePosition(), targetPoint);
        if (targets.isEmpty()) return;

        targets.sort(Comparator.comparingDouble(living -> living.distanceToSqr(targetPoint)));
        LivingEntity first = targets.getFirst();
        first.invulnerableTime = 0;
        first.hurt(electricity, PRIMARY_DAMAGE);
        LivingEntity previous = first;
        int chained = 1;
        Set<UUID> struck = new HashSet<>();
        struck.add(first.getUUID());
        while (chained < MAX_CHAIN_TARGETS) {
            LivingEntity from = previous;
            AABB search = from.getBoundingBox().inflate(CHAIN_RANGE);
            LivingEntity next = level.getEntitiesOfClass(LivingEntity.class, search, living ->
                            living.isAlive() && living != attacker && !living.isAlliedTo(attacker)
                                    && !struck.contains(living.getUUID())
                                    && living.distanceToSqr(from) <= CHAIN_RANGE * CHAIN_RANGE)
                    .stream().min(Comparator.comparingDouble(living -> living.distanceToSqr(from)))
                    .orElse(null);
            if (next == null) break;
            struck.add(next.getUUID());
            next.invulnerableTime = 0;
            next.hurt(electricity, SECONDARY_DAMAGE);
            sendArc(level, previous.getBoundingBox().getCenter(), next.getBoundingBox().getCenter());
            previous = next;
            chained++;
        }
    }

    private LivingEntity nextChainTarget(ServerLevel level, LivingEntity from, Set<UUID> struck) {
        AABB search = from.getBoundingBox().inflate(CHAIN_RANGE);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, search, living ->
                living.isAlive() && living != this && !(living instanceof LeiGongEntity)
                        && !struck.contains(living.getUUID())
                        && living.distanceToSqr(from) <= CHAIN_RANGE * CHAIN_RANGE);
        return candidates.stream()
                .min(Comparator.comparingDouble(living -> living.distanceToSqr(from)))
                .orElse(null);
    }

    private static void sendArc(ServerLevel level, Vec3 from, Vec3 to) {
        for (int step = 1; step <= 5; step++) {
            Vec3 point = from.lerp(to, step / 6.0);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z,
                    1, 0.03, 0.03, 0.03, 0.0);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        if (LeiGongBalance.coreDrops(random.nextFloat())) spawnAtLocation(ModItems.LEI_GONG_CORE.get());
    }

    private void setElectrifying(boolean value) {
        if (entityData.get(ELECTRIFYING) != value) entityData.set(ELECTRIFYING, value);
    }

    public boolean isElectrifying() {
        return entityData.get(ELECTRIFYING);
    }

    public int attackCycleTick() {
        return attackCycleTick;
    }

    public int specialCooldown() {
        return specialCooldown;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AttackCycle", attackCycleTick);
        tag.putInt("SpecialCooldown", specialCooldown);
        tag.putInt("StormCooldown", stormCooldown);
        tag.putInt("StormTicks", stormTicks);
        tag.putInt("PostStormTicks", postStormTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        attackCycleTick = Math.floorMod(tag.getInt("AttackCycle"), ATTACK_CYCLE_TICKS);
        specialCooldown = Math.max(0, tag.getInt("SpecialCooldown"));
        stormCooldown = Math.max(0, tag.getInt("StormCooldown"));
        stormTicks = Math.max(0, Math.min(80, tag.getInt("StormTicks")));
        postStormTicks = Math.max(0, tag.getInt("PostStormTicks"));
    }
}
