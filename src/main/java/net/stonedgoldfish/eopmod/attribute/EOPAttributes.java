package net.stonedgoldfish.eopmod.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stonedgoldfish.eopmod.EOPMod;

public class EOPAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, EOPMod.MOD_ID);

    public static final RegistryObject<Attribute> FLIGHT =
            ATTRIBUTES.register("flight", () ->
                    new RangedAttribute(
                            "attribute.name.echoesofpower.flight",
                            0.0D,
                            0.0D,
                            1024.0D
                    ).setSyncable(true)
            );
}

