package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class ScaleAbility extends Ability {

    public static final PalladiumProperty<Float> HEIGHT = new FloatProperty("height").configurable("Visual height scale");
    public static final PalladiumProperty<Float> WIDTH = new FloatProperty("width").configurable("Visual width scale");
    public static final PalladiumProperty<Float> HITBOX_HEIGHT = new FloatProperty("hitbox_height").configurable("Hitbox height scale");
    public static final PalladiumProperty<Float> HITBOX_WIDTH = new FloatProperty("hitbox_width").configurable("Hitbox width scale");
    public static final PalladiumProperty<Float> EYE_HEIGHT = new FloatProperty("eye_height").configurable("Eye height scale");

    public ScaleAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ARMOR_STAND));
        this.withProperty(HEIGHT, 1.0F);
        this.withProperty(WIDTH, 1.0F);
        this.withProperty(HITBOX_HEIGHT, 1.0F);
        this.withProperty(HITBOX_WIDTH, 1.0F);
        this.withProperty(EYE_HEIGHT, 1.0F);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled) {
            return;
        }

        applyScale(entity, entry);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled) {
            return;
        }

        applyScale(entity, entry);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        resetScale(entity);
    }

    private static void applyScale(LivingEntity entity, AbilityInstance entry) {
        setScale(ScaleTypes.HEIGHT.getScaleData(entity), entry.getProperty(HEIGHT));
        setScale(ScaleTypes.WIDTH.getScaleData(entity), entry.getProperty(WIDTH));
        setScale(ScaleTypes.HITBOX_HEIGHT.getScaleData(entity), entry.getProperty(HITBOX_HEIGHT));
        setScale(ScaleTypes.HITBOX_WIDTH.getScaleData(entity), entry.getProperty(HITBOX_WIDTH));
        setScale(ScaleTypes.EYE_HEIGHT.getScaleData(entity), entry.getProperty(EYE_HEIGHT));
    }

    private static void resetScale(LivingEntity entity) {
        setScale(ScaleTypes.HEIGHT.getScaleData(entity), 1.0F);
        setScale(ScaleTypes.WIDTH.getScaleData(entity), 1.0F);
        setScale(ScaleTypes.HITBOX_HEIGHT.getScaleData(entity), 1.0F);
        setScale(ScaleTypes.HITBOX_WIDTH.getScaleData(entity), 1.0F);
        setScale(ScaleTypes.EYE_HEIGHT.getScaleData(entity), 1.0F);
    }

    private static void setScale(ScaleData scaleData, float value) {
        float safeValue = Math.max(0.01F, value);

        scaleData.setScaleTickDelay(0);
        scaleData.setTargetScale(safeValue);
    }

    @Override
    public String getDocumentationDescription() {
        return "Changes Pehkui height, width, hitbox height, hitbox width and eye height.";
    }
}