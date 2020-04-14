package me.ichun.mods.tabula.old.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import me.ichun.mods.tabula.old.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.old.common.Tabula;
import me.ichun.mods.tabula.old.common.packet.PacketSetProjectMetadata;

import java.util.ArrayList;
import java.util.Collections;

public class WindowEditProjectMetadata extends Window
{
    public WindowEditProjectMetadata(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.metadata.title", true);

        elements.add(new ElementButton(this, width / 2 - 30, height - 25, 60, 16, -20, false, 2, 1, "gui.done"));

        GuiWorkspace workspace = ((GuiWorkspace)parent);

        if(workspace.hasOpenProject())
        {
            for(int i = 0; i <= workspace.getOpenProject().metadata.size(); i++)
            {
                elements.add(new ElementTextInput(this, 10, i * 18 + 20, width - 20, 12, i, null, 500, i == workspace.getOpenProject().metadata.size() ? "" : workspace.getOpenProject().metadata.get(i)));
            }
        }
    }

    @Override
    public void update()
    {
        int i = -1;
        ElementTextInput text = null;
        boolean lastIsEmpty = false;
        for(Element e : elements)
        {
            if(e instanceof ElementTextInput && e.id > i)
            {
                if(text != null)
                {
                    lastIsEmpty = text.textField.getText().isEmpty();
                }
                i = e.id;
                text = ((ElementTextInput)e);
            }
        }
        if(i == -1 || !text.textField.getText().isEmpty())
        {
            elements.add(new ElementTextInput(this, 10, (i + 1) * 18 + 20, width - 20, 12, (i + 1), null, 500, ""));
        }
        else if(i > 0 && lastIsEmpty)
        {
            elements.remove(text);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == -20)//done
        {
            if(((GuiWorkspace)workspace).hasOpenProject())
            {
                ArrayList<String> meta = new ArrayList<>();

                for(Element e : elements)
                {
                    if(e instanceof ElementTextInput)
                    {
                        String text = ((ElementTextInput)e).textField.getText();
                        if(!text.isEmpty())
                        {
                            meta.add(text);
                        }
                    }
                }

                Collections.sort(meta);

                if(!((GuiWorkspace)workspace).remoteSession)
                {
                    Tabula.proxy.tickHandlerClient.mainframe.setProjectMetadata(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, "", meta, true);
                }
                else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
                {
                    Tabula.channel.sendToServer(new PacketSetProjectMetadata(((GuiWorkspace)workspace).host, ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, "", meta, true));
                }
            }

            workspace.removeWindow(this, true);
        }
    }
}
