package me.ichun.mods.tabula.old.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButtonTextured;
import me.ichun.mods.ichunutil.client.render.RendererHelper;
import me.ichun.mods.tabula.old.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.old.client.gui.window.element.ElementListTree;
import me.ichun.mods.tabula.old.common.Tabula;
import me.ichun.mods.tabula.old.common.packet.PacketGenericMethod;
import net.minecraft.util.ResourceLocation;

public class WindowModelTree extends Window
{
    public ElementListTree modelList;

    public WindowModelTree(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.modelTree.title", true);

        elements.add(new ElementButtonTextured(this, BORDER_SIZE + 00, height - 20 - BORDER_SIZE, 0, false, 0, 1, "window.modelTree.newCube", new ResourceLocation("tabula", "textures/icon/newcube.png")));
        elements.add(new ElementButtonTextured(this, BORDER_SIZE + 20, height - 20 - BORDER_SIZE, 1, false, 0, 1, "window.modelTree.newGroup", new ResourceLocation("tabula", "textures/icon/newgroup.png")));
        elements.add(new ElementButtonTextured(this, BORDER_SIZE + 40, height - 20 - BORDER_SIZE, 2, false, 0, 1, "window.modelTree.delete", new ResourceLocation("tabula", "textures/icon/delete.png")));
        elements.add(new ElementButtonTextured(this, BORDER_SIZE + 60, height - 20 - BORDER_SIZE, 3, false, 0, 1, "window.editMeta.title", new ResourceLocation("tabula", "textures/icon/editmeta.png")));
        modelList = new ElementListTree(this, BORDER_SIZE + 1, BORDER_SIZE + 1 + 10, width - (BORDER_SIZE * 2 + 2), height - BORDER_SIZE - 21 - 16, 3, false, true);
        elements.add(modelList);
    }

    @Override
    public void draw(int mouseX, int mouseY) //4 pixel border?
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            RendererHelper.drawColourOnScreen(workspace.currentTheme.elementButtonBorder[0], workspace.currentTheme.elementButtonBorder[1], workspace.currentTheme.elementButtonBorder[2], 255, posX + BORDER_SIZE, posY + height - 21 - BORDER_SIZE, width - (BORDER_SIZE * 2), 1, 0);
        }
    }

    @Override
    public boolean interactableWhileNoProjects()
    {
        return false;
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0) //newcube
        {
            if(!((GuiWorkspace)workspace).remoteSession)
            {
                Tabula.proxy.tickHandlerClient.mainframe.createNewCube(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier);
            }
            else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
            {
                Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)workspace).host, "createNewCube", ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier));
            }
        }
        else if(element.id == 1) //newgroup
        {
            if(!((GuiWorkspace)workspace).remoteSession)
            {
                Tabula.proxy.tickHandlerClient.mainframe.createNewGroup(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier);
            }
            else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
            {
                Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)workspace).host, "createNewGroup", ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier));
            }
        }
        else if(element.id == 2 && !modelList.selectedIdentifier.isEmpty())
        {
            if(!((GuiWorkspace)workspace).remoteSession)
            {
                Tabula.proxy.tickHandlerClient.mainframe.deleteObject(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, modelList.selectedIdentifier);
            }
            else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
            {
                Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)workspace).host, "deleteObject", ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, modelList.selectedIdentifier));
            }
        }
        else if(element.id == 3 && !modelList.selectedIdentifier.isEmpty())
        {
            workspace.addWindowOnTop(new WindowEditObjectMetadata((GuiWorkspace)workspace, 0, 0, 300, 200, 180, 80, modelList.selectedIdentifier).putInMiddleOfScreen());
        }
        else
        {
        }
    }
}
