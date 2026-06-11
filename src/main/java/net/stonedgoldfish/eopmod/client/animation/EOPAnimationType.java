package net.stonedgoldfish.eopmod.client.animation;

public enum EOPAnimationType {
    NONE(EOPAnimationPlaybackType.ONE_SHOT),

    DASH_FRONT(EOPAnimationPlaybackType.ONE_SHOT),
    DASH_LEFT(EOPAnimationPlaybackType.ONE_SHOT),
    DASH_RIGHT(EOPAnimationPlaybackType.ONE_SHOT),
    DASH_BACK(EOPAnimationPlaybackType.ONE_SHOT),

    AUTO_DODGE_1(EOPAnimationPlaybackType.ONE_SHOT),
    AUTO_DODGE_2(EOPAnimationPlaybackType.ONE_SHOT),
    AUTO_DODGE_3(EOPAnimationPlaybackType.ONE_SHOT),

    RIGHT_ARM_SWIPE(EOPAnimationPlaybackType.ONE_SHOT),
    SHOOT(EOPAnimationPlaybackType.ONE_SHOT),

    TRANSFORM(EOPAnimationPlaybackType.TWO_PHASE),
    CREATE(EOPAnimationPlaybackType.TWO_PHASE),

    THIRD_PERSON(EOPAnimationPlaybackType.HOLD),
    RIGHT_ARM_LIFT(EOPAnimationPlaybackType.HOLD);

    public final EOPAnimationPlaybackType playbackType;

    EOPAnimationType(EOPAnimationPlaybackType playbackType) {
        this.playbackType = playbackType;
    }
}