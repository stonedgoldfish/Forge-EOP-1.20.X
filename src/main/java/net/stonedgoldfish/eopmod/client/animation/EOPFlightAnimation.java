package net.stonedgoldfish.eopmod.client.animation;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.stonedgoldfish.eopmod.power.ability.CustomFlightAbility;
import net.threetag.palladium.client.model.animation.PalladiumAnimation;
import net.threetag.palladium.util.Easing;

public class EOPFlightAnimation extends PalladiumAnimation {

    private static final float ANIMATION_SPEED = 0.01F;
    private static final float SPRINT_ANIMATION_SPEED = 0.02F;
    private static final float MOVEMENT_SCALE = 8.0F;
    private static final float MOVEMENT_SMOOTHING = 0.10F;
    private static final float VERTICAL_SCALE = 1.1F;
    private static final float VERTICAL_PITCH_STRENGTH = 65F;

    private float previousAnimationProgress = 0.0F;
    private float animationProgress = 0.0F;

    private float previousSprintAnimationProgress = 0.0F;
    private float sprintAnimationProgress = 0.0F;

    private float smoothForward = 0.0F;
    private float smoothStrafe = 0.0F;
    private float smoothVertical = 0.0F;

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
        boolean sprintFlying = flying && player.isSprinting();

        previousAnimationProgress = animationProgress;
        animationProgress = Mth.clamp(
                animationProgress + (flying ? ANIMATION_SPEED : -ANIMATION_SPEED),
                0.0F,
                1.0F
        );

        previousSprintAnimationProgress = sprintAnimationProgress;
        sprintAnimationProgress = Mth.clamp(
                sprintAnimationProgress + (sprintFlying ? SPRINT_ANIMATION_SPEED : -SPRINT_ANIMATION_SPEED),
                0.0F,
                1.0F
        );

        float anim = Mth.lerp(partialTicks, previousAnimationProgress, animationProgress);
        float sprintAnim = Mth.lerp(partialTicks, previousSprintAnimationProgress, sprintAnimationProgress);

        if (anim <= 0.0F) {
            return;
        }

        Vec3 motion = player.getDeltaMovement();

        float yawRad = player.getYRot() * Mth.DEG_TO_RAD;

        double forwardX = -Mth.sin(yawRad);
        double forwardZ = Mth.cos(yawRad);

        double rightX = Mth.cos(yawRad);
        double rightZ = Mth.sin(yawRad);

        float forward = (float) (motion.x * forwardX + motion.z * forwardZ);
        float strafe = (float) (motion.x * rightX + motion.z * rightZ);
        float horizontalSpeed = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float speedScale = horizontalSpeed > 0.0F ? 1.0F / horizontalSpeed : 1.0F;

        float vertical = Mth.clamp((float) motion.y * VERTICAL_SCALE * speedScale, -1.0F, 1.0F);

        forward = Mth.clamp(forward * MOVEMENT_SCALE, -1.0F, 1.0F);
        strafe = Mth.clamp(strafe * MOVEMENT_SCALE, -1.0F, 1.0F);

        smoothForward = Mth.lerp(MOVEMENT_SMOOTHING, smoothForward, forward);
        smoothStrafe = Mth.lerp(MOVEMENT_SMOOTHING, smoothStrafe, strafe);
        smoothVertical = Mth.lerp(MOVEMENT_SMOOTHING, smoothVertical, vertical);

        forward = smoothForward;
        strafe = smoothStrafe;
        vertical = smoothVertical;

        float leftStrafe = Math.max(-strafe, 0.0F);
        float rightStrafe = Math.max(strafe, 0.0F);

        float rightArmStrafeAmount = rightStrafe * 0.35F + leftStrafe;
        float leftArmStrafeAmount = leftStrafe * 0.35F + rightStrafe;

        boolean rightHanded = player.getMainArm() == HumanoidArm.RIGHT;

        float attackX = 0F;
        float attackY = 0F;
        float attackZ = 0F;

        float attackAnim = player.getAttackAnim(partialTicks);

        if (attackAnim > 0.0F) {
            float swingOut = Mth.sin(attackAnim * Mth.PI);
            float swingIn = Mth.sin(Mth.clamp((attackAnim - 0.35F) / 0.65F, 0.0F, 1.0F) * Mth.PI);

            attackX = swingOut * -65F;
            attackY = swingOut * 35F - swingIn * 55F;
            attackZ = swingOut * -18F + swingIn * 25F;
        }

        animateFlying(
                builder,
                anim,
                sprintAnim,
                forward,
                strafe,
                vertical,
                rightArmStrafeAmount,
                leftArmStrafeAmount,
                rightHanded,
                attackX,
                attackY,
                attackZ
        );
    }

    private void animateFlying(
            Builder builder,
            float anim,
            float sprintAnim,
            float forward,
            float strafe,
            float vertical,
            float rightArmStrafeAmount,
            float leftArmStrafeAmount,
            boolean rightHanded,
            float attackX,
            float attackY,
            float attackZ
    ) {
        float bodyPitch = vertical * VERTICAL_PITCH_STRENGTH;
        float bodyPitchRad = bodyPitch * Mth.DEG_TO_RAD;
        float bodyY = Mth.lerp(
                sprintAnim,
                0F,
                24F * (1F - Mth.cos(bodyPitchRad))
        );
        float bodyZ = Mth.lerp(
                sprintAnim,
                0F,
                -24F * Mth.sin(bodyPitchRad)
        );

        float normalHeadX = 0F;
        float sprintHeadX = vertical * 3F;

        float headX = Mth.lerp(sprintAnim, normalHeadX, sprintHeadX);
        float headZ = Mth.lerp(sprintAnim, 0F, strafe * -8F);

        float chestX = Mth.lerp(sprintAnim, 0F, 75F);
        float chestZ = Mth.lerp(sprintAnim, 0F, strafe * -20F);
        float chestY = Mth.lerp(sprintAnim, 0F, 1F);

        float sprintArmTilt = strafe * -10F;
        float sprintArmYOffset = strafe * 2F;

        float sprintLegTilt = strafe * -10F;
        float sprintLegYOffset = strafe * 0.75F;

        float normalRightArmX = -10F + forward * -15F + (rightHanded ? attackX : 0F);
        float normalRightArmYRot = rightHanded ? attackY : 0F;
        float normalRightArmZRot = 12.5F + strafe * 18F * rightArmStrafeAmount + (rightHanded ? attackZ : 0F);

        float normalLeftArmX = -10F + forward * -15F + (!rightHanded ? attackX : 0F);
        float normalLeftArmYRot = !rightHanded ? -attackY : 0F;
        float normalLeftArmZRot = -12.5F + strafe * 18F * leftArmStrafeAmount + (!rightHanded ? -attackZ : 0F);

        float rightArmX = Mth.lerp(sprintAnim, normalRightArmX, 75F);
        float rightArmYRot = Mth.lerp(sprintAnim, normalRightArmYRot, 0F);
        float rightArmZRot = Mth.lerp(sprintAnim, normalRightArmZRot, 13F + sprintArmTilt);
        float rightArmY = Mth.lerp(sprintAnim, 2F, 1F + sprintArmYOffset);
        float rightArmZ = Mth.lerp(sprintAnim, 0F, 3F);

        float leftArmX = Mth.lerp(sprintAnim, normalLeftArmX, 75F);
        float leftArmYRot = Mth.lerp(sprintAnim, normalLeftArmYRot, 0F);
        float leftArmZRot = Mth.lerp(sprintAnim, normalLeftArmZRot, -13F + sprintArmTilt);
        float leftArmY = Mth.lerp(sprintAnim, 2F, 1F - sprintArmYOffset);
        float leftArmZ = Mth.lerp(sprintAnim, 0F, 3F);

        float normalRightLegX = forward * 10F;
        float normalRightLegZRot = 3F + strafe * 6F;

        float normalLeftLegX = forward * 10F;
        float normalLeftLegZRot = -3F + strafe * 6F;

        float rightLegX = Mth.lerp(sprintAnim, normalRightLegX, 75F);
        float rightLegZRot = Mth.lerp(sprintAnim, normalRightLegZRot, 13F + sprintLegTilt);
        float rightLegY = Mth.lerp(sprintAnim, 12F, 4F + sprintLegYOffset);
        float rightLegZ = Mth.lerp(sprintAnim, 0F, 11F);

        float leftLegX = Mth.lerp(sprintAnim, normalLeftLegX, 75F);
        float leftLegZRot = Mth.lerp(sprintAnim, normalLeftLegZRot, -13F + sprintLegTilt);
        float leftLegY = Mth.lerp(sprintAnim, 12F, 4F - sprintLegYOffset);
        float leftLegZ = Mth.lerp(sprintAnim, 0F, 11F);

        builder.get(PlayerModelPart.BODY)
                .setXRotShortestDegrees(Mth.lerp(sprintAnim, 0F, bodyPitch))
                .setYRotShortestDegrees(0F)
                .setZRotShortestDegrees(0F)
                .setY(bodyY)
                .setZ(bodyZ)
                .animate(Easing.INOUTCUBIC, anim);

        if (sprintAnim > 0.0F) {
            builder.get(PlayerModelPart.HEAD)
                    .setXRotShortestDegrees(vertical * 3F)
                    .setZRotShortestDegrees(headZ)
                    .animate(Easing.INOUTCUBIC, anim * sprintAnim);
        }

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(chestX)
                .setYRotShortestDegrees(0F)
                .setZRotShortestDegrees(chestZ)
                .setY(chestY)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(rightArmX)
                .setYRotShortestDegrees(rightArmYRot)
                .setZRotShortestDegrees(rightArmZRot)
                .setY(rightArmY)
                .setZ(rightArmZ)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(leftArmX)
                .setYRotShortestDegrees(leftArmYRot)
                .setZRotShortestDegrees(leftArmZRot)
                .setY(leftArmY)
                .setZ(leftArmZ)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_LEG)
                .setXRotShortestDegrees(rightLegX)
                .setYRotShortestDegrees(0F)
                .setZRotShortestDegrees(rightLegZRot)
                .setY(rightLegY)
                .setZ(rightLegZ)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_LEG)
                .setXRotShortestDegrees(leftLegX)
                .setYRotShortestDegrees(0F)
                .setZRotShortestDegrees(leftLegZRot)
                .setY(leftLegY)
                .setZ(leftLegZ)
                .animate(Easing.INOUTCUBIC, anim);
    }
}