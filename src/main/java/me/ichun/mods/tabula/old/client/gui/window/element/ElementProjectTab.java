package me.ichun.mods.tabula.old.client.gui.window.element;

import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.render.RendererHelper;
import me.ichun.mods.ichunutil.common.module.tabula.project.ProjectInfo;
import me.ichun.mods.tabula.old.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.old.client.gui.Theme;
import me.ichun.mods.tabula.old.client.gui.window.WindowProjectSelection;

public class ElementProjectTab extends Element
{
    public ProjectInfo info;
    public boolean changed;

    public ElementProjectTab(Window window, int x, int y, int w, int h, int ID, ProjectInfo inf)
    {
        super(window, x, y, w, h, ID, true);
        info = inf;
        changed = false;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        WindowProjectSelection proj = (WindowProjectSelection)parent;
        if(id != proj.projects.size() - 1)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.tabSideInactive[0], parent.workspace.currentTheme.tabSideInactive[1], parent.workspace.currentTheme.tabSideInactive[2], 255, getPosX() + width - 1, getPosY() + 1, 1, height, 0);
        }
        if(proj.selectedProject == id)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementProjectTabActive[0], parent.workspace.currentTheme.elementProjectTabActive[1], parent.workspace.currentTheme.elementProjectTabActive[2], 255, getPosX(), getPosY(), width - 1, height, 0);
        }
        else if(hover)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementProjectTabHover[0], parent.workspace.currentTheme.elementProjectTabHover[1], parent.workspace.currentTheme.elementProjectTabHover[2], 255, getPosX(), getPosY(), width - 1, height, 0);
        }

        String titleToRender = info.modelName;
        while(titleToRender.length() > 1 && parent.workspace.getFontRenderer().getStringWidth(titleToRender) > width - (((GuiWorkspace)parent.workspace).remoteSession ? 2 : 11) )
        {
            if(titleToRender.startsWith("... "))
            {
                break;
            }
            if(titleToRender.endsWith("... "))
            {
                titleToRender = titleToRender.substring(0, titleToRender.length() - 5) + "... ";
            }
            else
            {
                titleToRender = titleToRender.substring(0, titleToRender.length() - 1) + "... ";
            }
        }
        parent.workspace.getFontRenderer().drawString(titleToRender, parent.posX + posX + 4, parent.posY + posY + 3, Theme.getAsHex(changed? parent.workspace.currentTheme.elementProjectTabFontChanges : parent.workspace.currentTheme.elementProjectTabFont), false);
        if(!((GuiWorkspace)parent.workspace).remoteSession)
        {
            parent.workspace.getFontRenderer().drawString("X", parent.posX + posX + width - 8, parent.posY + posY + 3, Theme.getAsHex(parent.workspace.currentTheme.elementProjectTabFont), false);
        }
    }

    @Override
    public void resized()
    {
        WindowProjectSelection tab = (WindowProjectSelection)parent;
        int space = tab.getWidth();
        int totalSpace = 0;
        for(ProjectInfo tab1 : tab.projects)
        {
            totalSpace += tab.workspace.getFontRenderer().getStringWidth(" " + tab1.modelName + (((GuiWorkspace)parent.workspace).remoteSession ? " " : " X "));
        }
        if(totalSpace > space)
        {
            posX = (id * (space / tab.projects.size()));
            posY = 0;
            width = space / tab.projects.size();
            height = 12;
        }
        else
        {
            posX = 0;
            for(int i = 0; i < id; i++)
            {
                posX += tab.workspace.getFontRenderer().getStringWidth(" " + tab.projects.get(i).modelName + (((GuiWorkspace)parent.workspace).remoteSession ? " " : " X "));
            }
            posY = 0;
            width = tab.workspace.getFontRenderer().getStringWidth(" " + info.modelName + (((GuiWorkspace)parent.workspace).remoteSession ? " " : " X "));
            height = 12;
        }
    }

    @Override
    public String tooltip()
    {
        //        String titleToRender = I18n.translateToLocal(mountedWindow.titleLocale);
        //        if(parent.workspace.getFontRenderer().getStringWidth(titleToRender) > width)
        //        {
        //            return mountedWindow.titleLocale;
        //        }
        String tooltip = info.modelName + " - " + info.authorName;
        if(info.saveFile != null)
        {
            tooltip = tooltip + " - " + info.saveFile.getName();
        }
        return tooltip; //return null for no tooltip. This is localized.
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        if(id == 0 || id == 2)
        {
            ((WindowProjectSelection)parent).changeProject(this.id);
            if(!((GuiWorkspace)parent.workspace).remoteSession && (mouseX + parent.posX > getPosX() + width - 9 || id == 2))
            {
                ((GuiWorkspace)parent.workspace).closeProject(((WindowProjectSelection)parent).projects.get(this.id));
            }
        }
        return false;
    }
}
