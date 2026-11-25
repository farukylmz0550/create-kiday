package io.github.createkiday.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import io.github.createkiday.registry.ModItems;

/** Straight-line piercer shared by the reusable Salur Disc and the TNT Disc. */
public final class SalurDiskEntity extends Entity {
    public static final double RANGE = 24.0;
    public static final float DAMAGE = 5.0F;
    public static final float TNT_POWER = 4.0F;
    private static final double SPEED = 1.2;
    private static final double CATCH_DISTANCE = 1.4;

    private UUID ownerId;
    private Vec3 origin = Vec3.ZERO;
    private Vec3 direction = new Vec3(0, 0, 1);
    private final Set<UUID> hitThisFlight = new HashSet<>();
    private boolean returning;
    private boolean explosive;
    private boolean consumed;
    private int flightTicks;

    public SalurDiskEntity(EntityType<? extends SalurDiskEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public void launch(ServerPlayer owner, boolean explosive) {
        ownerId = owner.getUUID();
        this.explosive = explosive;
        origin = owner.getEyePosition();
        direction = owner.getLookAngle().normalize();
        setPos(origin.x, origin.y - 0.15, origin.z);
    }

    @Override public void tick() {
        super.tick();
        flightTicks++;
        if (!explosive && flightTicks > 200) { finishFlight(); return; }
        if (level().isClientSide) {
            moveTo(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);
            return;
        }
        ServerPlayer owner = ownerId == null || !(level() instanceof ServerLevel server)
                ? null : server.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null || owner.level() != level()) { discard(); return; }
        Vec3 current = position();
        Vec3 next;
        if (returning) {
            Vec3 toOwner = owner.getEyePosition().subtract(current);
            if (toOwner.lengthSqr() <= CATCH_DISTANCE * CATCH_DISTANCE) { finishFlight(); return; }
            next = current.add(toOwner.normalize().scale(SPEED * 1.4));
        } else {
            next = current.add(direction.scale(SPEED));
            var blockHit = level().clip(new ClipContext(current, next, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, this));
            if (blockHit.getType() != HitResult.Type.MISS) {
                if (explosive) explode();
                else { returning = true; next = current; }
            }
            if (!returning && !explosive && level() instanceof ServerLevel serverLevel) hitEntities(serverLevel, current, next, owner);
            if (!returning && explosive && level() instanceof ServerLevel serverLevel && hitsLiving(serverLevel, current, next, owner)) explode();
            if (!returning && !explosive && position().distanceTo(origin) + SPEED >= RANGE) returning = true;
            if (!returning && explosive && position().distanceTo(origin) + SPEED >= RANGE) returning = true;
        }
        Vec3 moved = next.subtract(current);
        setDeltaMovement(moved);
        setPos(next.x, next.y, next.z);
    }

    private void hitEntities(ServerLevel level, Vec3 from, Vec3 to, Player owner) {
        AABB area = new AABB(from, to).inflate(0.45);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive() && entity != owner && !hitThisFlight.contains(entity.getUUID()))) {
            if (target.getBoundingBox().clip(from, to).isEmpty()) continue;
            hitThisFlight.add(target.getUUID());
            DamageSource source = level.damageSources().playerAttack(owner);
            target.hurt(source, DAMAGE);
        }
    }

    private boolean hitsLiving(ServerLevel level, Vec3 from, Vec3 to, Player owner) {
        AABB area = new AABB(from, to).inflate(0.4);
        return !level.getEntitiesOfClass(LivingEntity.class, area, entity ->
                entity.isAlive() && entity != owner && entity.getBoundingBox().clip(from, to).isPresent()).isEmpty();
    }

    private void explode() {
        if (consumed) return;
        consumed = true;
        if (level() instanceof ServerLevel serverLevel) {
            Entity owner = ownerId == null ? null : serverLevel.getServer().getPlayerList().getPlayer(ownerId);
            serverLevel.explode(owner, getX(), getY(), getZ(), TNT_POWER, Level.ExplosionInteraction.TNT);
            if (owner instanceof Player player) consumeTntDisk(player);
        }
        finishFlight();
    }

    private static void consumeTntDisk(Player player) {
        if (player.getAbilities().instabuild) return;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var stack = player.getInventory().getItem(slot);
            if (!stack.is(ModItems.TNT_DISC.get())) continue;
            stack.shrink(1);
            return;
        }
        for (var hand : net.minecraft.world.InteractionHand.values()) {
            var stack = player.getItemInHand(hand);
            if (stack.is(ModItems.TNT_DISC.get())) { stack.shrink(1); return; }
        }
    }

    private void finishFlight() {
        if (!explosive && ownerId != null && level() instanceof ServerLevel serverLevel) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerId);
            if (owner != null) owner.getPersistentData().remove(io.github.createkiday.item.SalurDiskItem.ACTIVE_SALUR_DISK);
        }
        discard();
    }

    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) { }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) ownerId = tag.getUUID("Owner");
        origin = new Vec3(tag.getDouble("OriginX"), tag.getDouble("OriginY"), tag.getDouble("OriginZ"));
        direction = new Vec3(tag.getDouble("DirectionX"), tag.getDouble("DirectionY"), tag.getDouble("DirectionZ"));
        returning = tag.getBoolean("Returning");
        explosive = tag.getBoolean("Explosive");
        consumed = tag.getBoolean("Consumed");
        flightTicks = Math.max(0, tag.getInt("FlightTicks"));
        for (int i = 0; i < tag.getInt("HitCount"); i++) {
            String key = "Hit" + i;
            if (tag.hasUUID(key)) hitThisFlight.add(tag.getUUID(key));
        }
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) tag.putUUID("Owner", ownerId);
        tag.putDouble("OriginX", origin.x); tag.putDouble("OriginY", origin.y); tag.putDouble("OriginZ", origin.z);
        tag.putDouble("DirectionX", direction.x); tag.putDouble("DirectionY", direction.y); tag.putDouble("DirectionZ", direction.z);
        tag.putBoolean("Returning", returning); tag.putBoolean("Explosive", explosive); tag.putBoolean("Consumed", consumed);
        tag.putInt("FlightTicks", flightTicks); tag.putInt("HitCount", hitThisFlight.size());
        int index = 0; for (UUID id : hitThisFlight) tag.putUUID("Hit" + index++, id);
    }
    public boolean isReturning() { return returning; }
    public boolean isExplosive() { return explosive; }
}
