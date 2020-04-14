package me.ichun.mods.tabula.old.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementTextWrapper;
import me.ichun.mods.tabula.old.common.Tabula;
import net.minecraft.util.text.translation.I18n;

public class WindowCredits extends Window
{
    public ElementTextWrapper textHolder;
    private String[] creds = new String[]{
            Tabula.VERSION, "iChun", "mr_hazard", "heldplayer, Vswe, bombmask, FraserKillip", "Kihira, Dizkonnekted, Dunkleosteus, Zorn_Taov, OndraSter, K-4U, Horfius, GlitchPulse"
    };

    public WindowCredits(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.about.title", true);

        textHolder = new ElementTextWrapper(this, 5 + 5, BORDER_SIZE + 1 + 12 + 5, width - 10 - 10, (height - BORDER_SIZE - 14) - (BORDER_SIZE + 1 + 12) - 2 - 10, 2, false, false);
        elements.add(textHolder);

        for(int i = 0; i <= 6; i++)
        {
            String text = I18n.translateToLocal("window.about.line" + i);
            if(i < creds.length)
            {
                if(text.endsWith(" "))
                {
                    text = text + creds[i];
                }
                else
                {
                    text = text + " " + creds[i];
                }
            }
            textHolder.text.add(text);
        }
        textHolder.text.add("");
        textHolder.text.add(I18n.translateToLocal("window.about.os1"));
        textHolder.text.add(I18n.translateToLocal("window.about.os2"));
        textHolder.text.add("https://github.com/iChun/Tabula");

        height = textHolder.getLineCount() * 15 + 30;
        minHeight = height;

        elements.add(new ElementButton(this, width / 2 - 30, height - 25, 60, 16, 3, false, 2, 1, "element.button.ok"));

    }

    @Override
    public void elementTriggered(Element element)
    {
        workspace.removeWindow(this, true);
    }
}
