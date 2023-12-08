package com.mcjty.lostworlds;

import net.minecraftforge.fml.common.Mod;

@Mod(LostWorlds.MODID)
public class LostWorlds {

    public static final String MODID = "lostworlds";

    public static LostWorlds instance;

    public LostWorlds() {
        instance = this;
    }
}
