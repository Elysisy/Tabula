package me.ichun.mods.tabula.client.core;

import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import me.ichun.mods.tabula.common.Tabula;

import javax.annotation.Nonnull;

public class ConfigClient extends ConfigBase
{
    //TODO localize these

    @CategoryDivider(name = "clientOnly")
    @Prop(min = -1, max = 8)
    public int forceGuiScale = 2;

    @Prop
    public boolean animateImports = true;

    @Nonnull
    @Override
    public String getModId()
    {
        return Tabula.MOD_ID;
    }

    @Nonnull
    @Override
    public String getConfigName()
    {
        return Tabula.MOD_NAME;
    }
}
