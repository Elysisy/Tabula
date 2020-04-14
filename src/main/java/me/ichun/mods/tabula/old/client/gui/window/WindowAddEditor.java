package me.ichun.mods.tabula.old.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.tabula.old.client.gui.window.element.ElementListTree;
import me.ichun.mods.tabula.old.common.Tabula;
import net.minecraft.client.Minecraft;

public class WindowAddEditor extends Window
{
    public ElementListTree modelList;

    public WindowAddEditor(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "topdock.addEditor", true);

        elements.add(new ElementButton(this, width - 140, height - 22, 60, 16, 1, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 22, 60, 16, 0, false, 1, 1, "element.button.cancel"));
        modelList = new ElementListTree(this, BORDER_SIZE + 1, BORDER_SIZE + 1 + 10, width - (BORDER_SIZE * 2 + 2), height - BORDER_SIZE - 22 - 16, 3, false, false);
        elements.add(modelList);

        for(String model : Tabula.proxy.tickHandlerClient.mainframe.listeners.keySet())
        {
            if(!model.equals(Minecraft.getMinecraft().getSession().getUsername()))
            {
                modelList.createTree(null, model, 13, 0, false, false);
            }
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id == 1 || element.id == 3)
        {
            boolean found = false;

            if(workspace.windowDragged == this)
            {
                workspace.windowDragged = null;
            }
            for(int i = 0; i < modelList.trees.size(); i++)
            {
                ElementListTree.Tree tree = modelList.trees.get(i);
                if(tree.selected)
                {
                    Tabula.proxy.tickHandlerClient.mainframe.addEditor((String)tree.attachedObject);
                    found = true;
                    break;
                }
            }

            if(found)
            {
                workspace.removeWindow(this, true);
            }
        }
    }
}
