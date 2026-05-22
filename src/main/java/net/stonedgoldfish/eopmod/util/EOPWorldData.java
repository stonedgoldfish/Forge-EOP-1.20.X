package net.stonedgoldfish.eopmod.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class EOPWorldData extends SavedData {

    private static final String NAME = "echoesofpower_data";

    private boolean destructionMode = true;

    public static EOPWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                EOPWorldData::load,
                EOPWorldData::new,
                NAME
        );
    }

    public static EOPWorldData load(CompoundTag tag) {
        EOPWorldData data = new EOPWorldData();

        if (tag.contains("DestructionMode")) {
            data.destructionMode = tag.getBoolean("DestructionMode");
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("DestructionMode", destructionMode);
        return tag;
    }

    public boolean isDestructionMode() {
        return destructionMode;
    }

    public void setDestructionMode(boolean value) {
        this.destructionMode = value;
        this.setDirty();
    }
}