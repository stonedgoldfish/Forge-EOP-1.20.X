package net.stonedgoldfish.eopmod.client.animation;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.threetag.palladium.client.model.animation.PalladiumAnimation;
import net.threetag.palladium.util.Easing;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.HumanoidArm;
import net.stonedgoldfish.eopmod.power.ability.CustomFlightAbility;

public class EOPFlightAnimation extends PalladiumAnimation {

    private float animationProgress = 0.0F;

    private float smoothForward = 0.0F;
    private float smoothStrafe = 0.0F;


    public EOPFlightAnimation(int priority) {
        super(priority);
    }

    @Override
    public void animate(
            Builder builder,
            AbstractClientPlayer player,
            HumanoidModel<?> model,
            FirstPersonContext firstPersonContext,
            float partialTicks
    ) {
        if (firstPersonContext.firstPerson()) {
            return;
        }

        if (!CustomFlightAbility.hasCustomFlight(player)) {
            return;
        }

        boolean flying = player.getAbilities().flying;

        float previousAnimationProgress = animationProgress;

        if (flying) {
            animationProgress = Math.min(animationProgress + 0.01F, 1.0F);
        } else {
            animationProgress = Math.max(animationProgress - 0.01F, 0.0F);
        }

        float anim = Mth.lerp(partialTicks, previousAnimationProgress, animationProgress);

        Vec3 motion = player.getDeltaMovement();

        float yawRad = (float) Math.toRadians(player.getYRot());

        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);

        double rightX = Math.cos(yawRad);
        double rightZ = Math.sin(yawRad);

        float forward = (float) (motion.x * forwardX + motion.z * forwardZ);
        float strafe = (float) (motion.x * rightX + motion.z * rightZ);

        forward = Mth.clamp(forward * 8.0F, -1.0F, 1.0F);
        strafe = Mth.clamp(strafe * 8.0F, -1.0F, 1.0F);

        smoothForward = Mth.lerp(0.15F, smoothForward, forward);
        smoothStrafe = Mth.lerp(0.15F, smoothStrafe, strafe);

        forward = smoothForward;
        strafe = smoothStrafe;

        float leftStrafe = Math.max(-strafe, 0.0F);
        float rightStrafe = Math.max(strafe, 0.0F);

        float rightArmStrafeAmount = rightStrafe * 0.35F + leftStrafe;
        float leftArmStrafeAmount = leftStrafe * 0.35F + rightStrafe;
        float attackAnim = player.getAttackAnim(partialTicks);
        float attackSin = Mth.sin(attackAnim * (float) Math.PI);

        boolean rightHanded = player.getMainArm() == HumanoidArm.RIGHT;

        float attackX = attackSin * -65F;
        float swingOut = Mth.sin(attackAnim * (float) Math.PI);
        float swingIn = Mth.sin(Mth.clamp((attackAnim - 0.35F) / 0.65F, 0.0F, 1.0F) * (float) Math.PI);

        float attackY = swingOut * 35F - swingIn * 55F;
        float attackZ = swingOut * -18F + swingIn * 25F;

        if (anim <= 0.0F) {
            return;
        }


        // Upright hovering body
        builder.get(PlayerModelPart.BODY)
                .resetX()
                .resetZ()
                .animate(Easing.LINEAR, 1.0F);

        builder.get(PlayerModelPart.CHEST)
                .animate(Easing.LINEAR, 1.0F);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-10F + forward * -15F + (rightHanded ? attackX : 0F))
                .setYRotShortestDegrees(rightHanded ? attackY : 0F)
                .setZRotShortestDegrees(12.5F + strafe * 18F * rightArmStrafeAmount + (rightHanded ? attackZ : 0F))
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-10F + forward * -15F + (!rightHanded ? attackX : 0F))
                .setYRotShortestDegrees(!rightHanded ? -attackY : 0F)
                .setZRotShortestDegrees(-12.5F + strafe * 18F * leftArmStrafeAmount + (!rightHanded ? -attackZ : 0F))
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_LEG)
                .setXRotShortestDegrees(forward * 10F)
                .setYRotShortestDegrees(strafe * -5F)
                .setZRotShortestDegrees(3F + strafe * 10F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_LEG)
                .setXRotShortestDegrees(forward * 10F)
                .setYRotShortestDegrees(strafe * -5F)
                .setZRotShortestDegrees(-3F + strafe * 10F)
                .animate(Easing.INOUTCUBIC, anim);
    }
}