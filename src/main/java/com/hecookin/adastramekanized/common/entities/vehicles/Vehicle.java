package com.hecookin.adastramekanized.common.entities.vehicles;

import com.hecookin.adastramekanized.common.container.VehicleContainer;
import com.hecookin.adastramekanized.common.entities.multipart.MultipartEntity;
import com.hecookin.adastramekanized.common.entities.multipart.MultipartPartEntity;
import com.hecookin.adastramekanized.common.network.ModNetworking;
import com.hecookin.adastramekanized.common.network.VehicleControlPacket;
import com.hecookin.adastramekanized.mixins.LivingEntityAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Base class for all vehicles (rockets, rovers, landers).
 * Adapted from Ad Astra with Mekanized changes:
 * - Uses our gravity system
 * - Simplified inventory (fuel only for rockets)
 * - Integrated with our networking
 */
public abstract class Vehicle extends Entity implements PlayerRideable, MenuProvider, MultipartEntity, HasCustomInventoryScreen {

    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;

    private float xxa;
    private float zza;

    protected final VehicleContainer inventory = new VehicleContainer(getInventorySize());

    protected final List<VehiclePart> parts = new ArrayList<>();
    private final List<MultipartPartEntity<?>> multipartParts = new ArrayList<>();

    public Vehicle(EntityType<?> type, Level level) {
        super(type, level);
    }

    protected void addPart(float width, float height, Vector3f offset,
                          BiFunction<Player, InteractionHand, InteractionResult> handler) {
        VehiclePart part = new VehiclePart(this, width, height, offset, handler);
        this.parts.add(part);
        this.multipartParts.add(part);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        inventory.fromTag(registryAccess(), compound.getList("Inventory", Tag.TAG_COMPOUND));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("Inventory", inventory.createTag(registryAccess()));
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return Boat.canVehicleCollide(this, entity);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int lerpSteps) {
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYRot = yRot;
        lerpXRot = xRot;
        this.lerpSteps = lerpSteps;
    }

    @Override
    public void tick() {
        super.tick();
        this.tickLerp();
        if (!isNoGravity()) tickGravity();
        setPos(getX(), getY(), getZ());
        if (isControlledByLocalInstance()) {
            if (level().isClientSide() && isVehicle() && getControllingPassenger() instanceof Player player) {
                ModNetworking.sendToServer(new VehicleControlPacket(player.xxa, player.zza));
            }
            move(MoverType.SELF, getDeltaMovement());
            tickFriction();
        }

        for (VehiclePart part : this.parts) {
            part.tickPart();
        }
    }

    public void tickLerp() {
        if (isControlledByLocalInstance()) {
            lerpSteps = 0;
            syncPacketPositionCodec(getX(), getY(), getZ());
        }

        if (lerpSteps > 0) {
            double x = getX() + (lerpX - getX()) / lerpSteps;
            double y = getY() + (lerpY - getY()) / lerpSteps;
            double z = getZ() + (lerpZ - getZ()) / lerpSteps;
            double g = Mth.wrapDegrees(lerpYRot - getYRot());
            setYRot(getYRot() + (float) g / lerpSteps);
            setXRot(getXRot() + (float) (lerpXRot - getXRot()) / lerpSteps);
            lerpSteps--;
            setPos(x, y, z);
            setRot(getYRot(), getXRot());
        }
    }

    public void tickGravity() {
        Vec3 velocity = getDeltaMovement();
        // Use standard Minecraft gravity - our planet gravity system handles entities separately
        double gravity = 0.04;
        setDeltaMovement(velocity.x, velocity.y - gravity, velocity.z);
    }

    public void tickFriction() {
        float friction = level().getBlockState(getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction();
        float speed = this.onGround() ? friction * 0.91f : 0.91f;

        var deltaMovement = getDeltaMovement();
        setDeltaMovement(deltaMovement.x * speed, deltaMovement.y, deltaMovement.z * speed);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof LivingEntity e ? e : null;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        if (source.is(DamageTypeTags.IS_PROJECTILE)) return false;
        if (amount >= 0
            && source.getEntity() instanceof Player player
            && (player.getVehicle() == null || !player.getVehicle().equals(this))) {
            playSound(SoundEvents.NETHERITE_BLOCK_BREAK);
            if (!player.getAbilities().instabuild) {
                drop();
            }
            discard();
            return true;
        }
        return false;
    }

    public void drop() {
        Containers.dropItemStack(level(), getX(), getY(), getZ(), getDropStack());
        if (inventory.getContainerSize() > 0) {
            Containers.dropContents(level(), blockPosition(), inventory);
        }
    }

    public abstract ItemStack getDropStack();

    @Override
    public @NotNull InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide()) {
            if (player.isSecondaryUseActive()) {
                this.openCustomInventoryScreen(player);
                return InteractionResult.SUCCESS;
            }
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public abstract AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player);

    public void updateInput(float xxa, float zza) {
        this.xxa = xxa;
        this.zza = zza;
    }

    public abstract int getInventorySize();

    /**
     * Checks if the rider should be sitting in the vehicle.
     */
    public boolean shouldSit() {
        return true;
    }

    /**
     * Prevents the rider from rendering.
     */
    public boolean hideRider() {
        return false;
    }

    public boolean zoomOutCameraInThirdPerson() {
        return false;
    }

    /**
     * Checks if it's safe to dismount the vehicle. If not, the passenger has to hold shift for 2 seconds to dismount.
     */
    public boolean isSafeToDismount(Player player) {
        return true;
    }

    /**
     * Gets the left/right passenger input.
     */
    public float xxa() {
        var controllingPassenger = getControllingPassenger();
        if (controllingPassenger == null) return 0;
        return level().isClientSide() ? controllingPassenger.xxa : xxa;
    }

    /**
     * Gets the forward/backward passenger input.
     */
    public float zza() {
        var controllingPassenger = getControllingPassenger();
        if (controllingPassenger == null) return 0;
        return level().isClientSide() ? controllingPassenger.zza : zza;
    }

    public boolean passengerHasSpaceDown() {
        var controllingPassenger = getControllingPassenger();
        if (!(controllingPassenger instanceof LivingEntityAccessor entity)) return false;
        return entity.isJumping();
    }

    public VehicleContainer inventory() {
        return inventory;
    }

    @Override
    public VehiclePart[] getParts() {
        return parts.toArray(new VehiclePart[0]);
    }

    // For MultipartEntity compatibility
    public List<MultipartPartEntity<?>> getMultipartParts() {
        return multipartParts;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);

        for (int i = 0; i < this.parts.size(); ++i) {
            this.parts.get(i).setId(i + 1 + packet.getId());
        }
    }
}
