package me.ichun.mods.tabula.client.gui.window.popup;

import com.google.common.collect.Ordering;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.common.module.tabula.formats.ImportList;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.TreeSet;

public class WindowImportProject extends Window<WorkspaceTabula>
{
    public WindowImportProject(WorkspaceTabula parent)
    {
        super(parent);

        setView(new ViewImportProject(this));
        disableDockingEntirely();
    }

    public static class ViewImportProject extends View<WindowImportProject>
    {
        public ViewImportProject(@Nonnull WindowImportProject parent)
        {
            super(parent, "window.import.title");

            ElementScrollBar<?> sv = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            sv.setConstraint(new Constraint(sv).top(this, Constraint.Property.Type.TOP, 0)
                    .bottom(this, Constraint.Property.Type.BOTTOM, 40) // 10 + 20 + 10, bottom + button height + padding
                    .right(this, Constraint.Property.Type.RIGHT, 0)
            );
            elements.add(sv);

            ElementList<?> list = new ElementList<>(this).setScrollVertical(sv);
            list.setConstraint(new Constraint(list).bottom(this, Constraint.Property.Type.BOTTOM, 40)
                    .left(this, Constraint.Property.Type.LEFT, 0).right(sv, Constraint.Property.Type.LEFT, 0)
                    .top(this, Constraint.Property.Type.TOP, 0));
            elements.add(list);

            TreeSet<File> files = new TreeSet<>(Ordering.natural());
            File[] textures = ResourceHelper.getSavesDir().toFile().listFiles();
            if(textures != null)
            {
                for(File file : textures)
                {
                    if(!file.isDirectory() && ImportList.isFileSupported(file))
                    {
                        files.add(file);
                    }
                }
            }

            for(File file : files)
            {
                list.addItem(file).setDefaultAppearance().setDoubleClickHandler(item -> {
                    if(item.selected)
                    {
                        loadFile(item.getObject());
                    }
                });
            }

            ElementButtonTextured<?> openDir = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/info.png"), btn -> {
                Util.getOSType().openFile(ResourceHelper.getSavesDir().toFile());
            });
            openDir.setTooltip(I18n.format("topdock.openWorkingDir")).setSize(20, 20);
            openDir.setConstraint(new Constraint(openDir).bottom(this, Constraint.Property.Type.BOTTOM, 10).left(this, Constraint.Property.Type.LEFT, 10));
            elements.add(openDir);

            ElementToggle<?> toggle = new ElementToggle<>(this, "window.import.texture", btn -> {});
            toggle.setToggled(true).setTooltip(I18n.format("window.import.textureFull")).setSize(60, 20).setId("buttonTexture");
            toggle.setConstraint(new Constraint(toggle).bottom(this, Constraint.Property.Type.BOTTOM, 10).left(openDir, Constraint.Property.Type.RIGHT, 10));
            elements.add(toggle);

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), btn ->
            {
                getWorkspace().removeWindow(parent);
            });
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.ok"), btn ->
            {
                for(ElementList.Item<?> item : list.items)
                {
                    if(item.selected)
                    {
                        loadFile((File)item.getObject());
                        return;
                    }
                }
            });
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);
        }

        public void loadFile(File file)
        {
            parentFragment.parent.removeWindow(parentFragment);

            Project project = ImportList.createProjectFromFile(file);
            if(project == null)
            {
                WindowPopup.popup(parentFragment.parent, 0.4D, 0.3D, null, I18n.format("window.open.failed"));
            }
            else
            {
                parentFragment.parent.mainframe.importProject(project, ((ElementToggle)getById("buttonTexture")).toggleState, true);
            }
        }
    }
}
