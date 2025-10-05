package com.hecookin.adastramekanized.mixins;

import com.hecookin.adastramekanized.common.entities.vehicles.Vehicle;
import com.hecookin.adastramekanized.common.tags.ModItemTags;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {

    @SuppressWarnings("unchecked")
    @Inject(method = "setupAnim*", at = @At("HEAD"))
    private void adastramekanized$setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        var model = ((HumanoidModel<LivingEntity>) (Object) this);

        if (entity.getVehicle() instanceof Vehicle vehicle) {
            model.riding = vehicle.shouldSit(); // disable the sitting pose while standing in a rocket
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "setupAnim*", at = @At("TAIL"))
    private void adastramekanized$setupAnimTail(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        var model = ((HumanoidModel<LivingEntity>) (Object) this);

        if (entity.getMainHandItem().is(ModItemTags.HELD_OVER_HEAD)) {
            model.rightArm.xRot = -2.8f;
            model.leftArm.xRot = model.rightArm.xRot;
        } else if (entity.getOffhandItem().is(ModItemTags.HELD_OVER_HEAD)) {
            model.leftArm.xRot = -2.8f;
            model.rightArm.xRot = model.leftArm.xRot;
        }
    }
}
