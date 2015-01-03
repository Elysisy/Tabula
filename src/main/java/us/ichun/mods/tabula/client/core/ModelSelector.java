package us.ichun.mods.tabula.client.core;

import ichun.client.render.RendererHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.Window;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.client.model.ModelBaseDummy;
import us.ichun.module.tabula.common.project.ProjectInfo;
import us.ichun.module.tabula.common.project.components.Animation;
import us.ichun.module.tabula.common.project.components.AnimationComponent;
import us.ichun.module.tabula.common.project.components.CubeInfo;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * A class which renders different parts of a model in different colors to figure out which part that was clicked on.
 *
 * Call onClick and the part of the model the user clicked on will get selected.
 *
 * @author Vswe
 * @version 1.1
 */
public class ModelSelector {

    private final GuiWorkspace workspace;
    private final int colors;
    private final float color_precision;

    /**
     * Create a new model selector
     * @param workspace The workspace interface this model selector will operator on
     * @param colors The amount of colors the model selector is allowed to use when figuring out what was clicked. The
     *               pro of having a high number is that the max number of parts a model can have is increased. The max
     *               amount of parts the model can have is (colors^3 - 1). However, if the number is too high floating
     *               point errors might cause problems.
     */
    public ModelSelector(GuiWorkspace workspace, int colors) {
        this.workspace = workspace;
        this.colors = colors - 1;
        this.color_precision = 1F / this.colors;
    }

    /**
     * Call this to selected the part of the model that is moused over.
     *
     * If the mouse is not in the model viewer when the click occurs, nothing is updated.
     * If the mouse is not on the model, the current selection is removed.
     * If the mouse is on the current selection, the selection is removed.
     *
     * @param mouseX The x coordinate as it is used in minecraft interfaces
     * @param mouseY The y coordinate as it is used in minecraft interfaces
     */
    public void onClick(int mouseX, int mouseY) {
        if(!isOnWindow(mouseX, mouseY) && workspace.projectManager.selectedProject != -1) {

            ArrayList<ElementListTree.Tree> trees = workspace.windowModelTree.modelList.trees;

            if(Tabula.config.getInt("renderModelControls") == 1)
            {
                for(ElementListTree.Tree tree : trees)
                {
                    if(tree.selected && tree.attachedObject instanceof CubeInfo)
                    {
                        CubeInfo cube = (CubeInfo)tree.attachedObject;

                        fakeRenderSelectedCube(cube);

                        break;
                    }
                }
            }

            int control = getSelectedId();

            if(control != -1)
            {
                workspace.controlDrag = GuiScreen.isShiftKeyDown() ? control + 3 : control;
                workspace.controlDragX = mouseX;
                workspace.controlDragY = mouseY;
            }
            else
            {
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                //render the model so the result can be analyzed
                fakeRender();

                //once the model is rendered we can fetch the id of the part that contributed to the pixel the mouse is hovering
                int id = getSelectedId();

                //update selection
                workspace.windowModelTree.modelList.selectedIdentifier = "";
                workspace.windowControls.selectedObject = null;
                int treeId = 0;
                for(ElementListTree.Tree tree : trees)
                {
                    if(tree.attachedObject instanceof CubeInfo)
                    {
                        tree.selected = !tree.selected && id == treeId;
                        if(tree.selected)
                        {
                            workspace.windowControls.selectedObject = tree.attachedObject;
                            workspace.windowModelTree.modelList.selectedIdentifier = ((CubeInfo)tree.attachedObject).identifier;
                        }
                        treeId++;
                    }
                    else
                    {
                        tree.selected = false;
                    }
                }
                workspace.windowControls.refresh = true;

                if(workspace.windowControls.selectedObject == null && id == -1)
                {
                    for(ElementListTree.Tree tree : workspace.windowAnimate.animList.trees)
                    {
                        tree.selected = false;
                    }
                    workspace.windowAnimate.animList.selectedIdentifier = "";
                    workspace.windowAnimate.timeline.selectedIdentifier = "";
                    workspace.windowAnimate.timeline.setCurrentPos(0);
                }
            }
        }
    }

    private boolean isOnWindow(int mouseX, int mouseY) {
        for(int i = 0; i < workspace.levels.size(); i++) {
            for(int j = 0; j < workspace.levels.get(i).size(); j++) {
                Window window = workspace.levels.get(i).get(j);
                if(mouseX >= window.posX && mouseX <= window.posX + window.getWidth() && mouseY >= window.posY && mouseY <= window.posY + window.getHeight()) {
                    return true;
                }
            }
        }

        return false;
    }

    private int getSelectedId() {
        //retrieve the color where the mouse is pointing
        FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
        GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
        int R = Math.round(buffer.get() * colors);
        int G = Math.round(buffer.get() * colors);
        int B = Math.round(buffer.get() * colors);

        //return the id based on the color, the color was generated from the id in the first place
        return R + G * colors + B * colors * colors - 1;
    }

    public void fakeRender() {
        GL11.glPushMatrix();

        //the background is black since that's teh color retrieved for sending in id = 0 in the color generator
        workspace.applyCamera();
        workspace.applyModelTranslation();

        ProjectInfo info = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_NORMALIZE);

        GL11.glScaled(1D / info.scale[0], 1D / info.scale[1], 1D / info.scale[2]);
        fakeRenderModel(info, 0.0625F);

        GL11.glEnable(GL11.GL_NORMALIZE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    public void fakeRenderSelectedCube(CubeInfo info)
    {
        List<CubeInfo> hidden = workspace.getHiddenElements();

        if(!(info.hidden || hidden.contains(info)))
        {
            GL11.glPushMatrix();

            //the background is black since that's teh color retrieved for sending in id = 0 in the color generator
            workspace.applyCamera();
            workspace.applyModelTranslation();

            ProjectInfo proj = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_NORMALIZE);

            GL11.glScaled(1D / proj.scale[0], 1D / proj.scale[1], 1D / proj.scale[2]);

            float f5 = 0.0625F;
            workspace.applyModelAnimations();

            if (!info.getChildren().isEmpty()) {
                info.modelCube.childModels.clear();
            }

            GL11.glPushMatrix();
            applyScale(info, f5);
            applyParentTransformations(proj.model.getParents(info), f5);
            int id = 0;
            applyColorAndFakeRender(info.modelCube, id++, f5);

            GL11.glPushMatrix();

            GL11.glTranslatef(info.modelCube.offsetX, info.modelCube.offsetY, info.modelCube.offsetZ);
            GL11.glTranslatef(info.modelCube.rotationPointX * f5, info.modelCube.rotationPointY * f5, info.modelCube.rotationPointZ * f5);

            if(info.modelCube.rotateAngleZ != 0.0F)
            {
                GL11.glRotatef(info.modelCube.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
            }

            if(info.modelCube.rotateAngleY != 0.0F)
            {
                GL11.glRotatef(info.modelCube.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
            }

            if(info.modelCube.rotateAngleX != 0.0F)
            {
                GL11.glRotatef(info.modelCube.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
            }

            if(!GuiScreen.isShiftKeyDown())
            {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glPushMatrix();
                float scale = 0.75F;
                GL11.glScalef(scale, scale, scale);
                applyColor(id++);
                GL11.glRotatef(90F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(90F, 0.0F, 0.0F, 1.0F);
                proj.model.rotationControls.render(f5);
                GL11.glRotatef(90F, 0.0F, 0.0F, -1.0F);
                applyColor(id++);
                proj.model.rotationControls.render(f5);
                GL11.glRotatef(90F, -1.0F, 0.0F, 0.0F);
                applyColor(id++);
                proj.model.rotationControls.render(f5);
                GL11.glPopMatrix();
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
            else
            {
                GL11.glPushMatrix();
                float scale1 = 0.5F;
                GL11.glScalef(scale1, scale1, scale1);

                GL11.glPushMatrix();
                GL11.glTranslated((0.125F * (info.dimensions[0] / 2D + info.offset[0])), (0.125D * (1D + (info.dimensions[1] + info.offset[1]) + info.mcScale)), (0.125F * (info.dimensions[2] / 2D + info.offset[2])));
                applyColor(id++);
                proj.model.sizeControls.render(f5);
                GL11.glPopMatrix();

                GL11.glPushMatrix();
                GL11.glTranslated((0.125F * (info.dimensions[0] / 2D + info.offset[0])), -(0.125D * (1D - (info.offset[1]) + info.mcScale)), (0.125F * (info.dimensions[2] / 2D + info.offset[2])));
                GL11.glRotatef(180F, 0F, 0F, -1F);
                applyColor(id++);
                proj.model.sizeControls.render(f5);
                GL11.glPopMatrix();

                GL11.glPushMatrix();
                GL11.glTranslated((0.125F * (1D + info.dimensions[0] + info.offset[0] + info.mcScale)), (0.125D * ((info.dimensions[1] / 2D + info.offset[1]))), (0.125F * (info.dimensions[2] / 2D + info.offset[2])));
                GL11.glRotatef(90F, 0F, 0F, -1F);
                applyColor(id++);
                proj.model.sizeControls.render(f5);
                GL11.glPopMatrix();

                GL11.glPushMatrix();
                GL11.glTranslated(-(0.125F * (1D - info.offset[0] + info.mcScale)), (0.125D * ((info.dimensions[1] / 2D + info.offset[1]))), (0.125F * (info.dimensions[2] / 2D + info.offset[2])));
                GL11.glRotatef(90F, 0F, 0F, 1F);
                applyColor(id++);
                proj.model.sizeControls.render(f5);
                GL11.glPopMatrix();

                GL11.glPushMatrix();
                GL11.glTranslated((0.125F * (info.dimensions[0] / 2D + info.offset[0])), (0.125D * ((info.dimensions[1] / 2D + info.offset[1]))), (0.125F * (1D + info.dimensions[2] + info.offset[2] + info.mcScale)));
                GL11.glRotatef(90F, 1F, 0F, 0F);
                applyColor(id++);
                proj.model.sizeControls.render(f5);
                GL11.glPopMatrix();

                GL11.glPushMatrix();
                GL11.glTranslated((0.125F * (info.dimensions[0] / 2D + info.offset[0])), (0.125D * ((info.dimensions[1] / 2D + info.offset[1]))), -(0.125F * (1D - info.offset[2] + info.mcScale)));
                GL11.glRotatef(90F, -1F, 0F, 0F);
                applyColor(id++);
                proj.model.sizeControls.render(f5);
                GL11.glPopMatrix();

                GL11.glPopMatrix();
            }

            GL11.glPopMatrix();

            GL11.glPopMatrix();

            workspace.resetModelAnimations();

            GL11.glEnable(GL11.GL_NORMALIZE);
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();

            return;
        }
    }

    private void fakeRenderModel(ProjectInfo info, float f5) {
        List<CubeInfo> hidden = workspace.getHiddenElements();

        workspace.applyModelAnimations();

        int id = 1; //leave one id for the background
        for (int i = 0; i < info.model.cubes.size(); i++) {
            id = fakeRenderModelPart(info.model, info.model.cubes.get(i), hidden, id, f5, true);
        }

        workspace.resetModelAnimations();
    }

    private int fakeRenderModelPart(ModelBaseDummy model, CubeInfo info, List<CubeInfo> hidden, int id, float f5, boolean top) {
        if (info.hidden || hidden.contains(info)) {
            id = consumeIds(info, id);
        }else{

            //to be able to render child models in their own color, temporarily remove them
            if (!info.getChildren().isEmpty()) {
                info.modelCube.childModels.clear();
            }

            GL11.glPushMatrix();
            if (top) {
                applyScale(info, f5);
            }else{
                applyParentTransformations(model.getParents(info), f5);
            }
            applyColorAndFakeRender(info.modelCube, id++, f5);
            GL11.glPopMatrix();

            for (CubeInfo cubeInfo : info.getChildren()) {
                //noinspection unchecked
                info.modelCube.childModels.add(cubeInfo.modelCube);
                id = fakeRenderModelPart(model, cubeInfo, hidden, id, f5, false);
            }
        }

        return id;
    }

    private int consumeIds(CubeInfo info, int id) {
        id++;
        for (CubeInfo cubeInfo : info.getChildren()) {
            id = consumeIds(cubeInfo, id);
        }
        return id;
    }

    private void applyColor(int id) {
        //calculate the color based on the id, the color can then be used to retrieve the id
        int R = id % colors;
        id /= colors;
        int G = id % colors;
        id /= colors;
        int B = id % colors;

        GL11.glColor4f(R * color_precision, G * color_precision, B * color_precision, 1.0F);
    }

    private void applyColorAndFakeRender(ModelRenderer model, int id, float f5) {
        applyColor(id);
        model.render(f5);
    }

    private void applyScale(CubeInfo info, float f5) {
        GL11.glTranslatef(info.modelCube.offsetX, info.modelCube.offsetY, info.modelCube.offsetZ);
        GL11.glTranslatef(info.modelCube.rotationPointX * f5, info.modelCube.rotationPointY * f5, info.modelCube.rotationPointZ * f5);
        GL11.glScaled(info.scale[0], info.scale[1], info.scale[2]);
        GL11.glTranslatef(-info.modelCube.offsetX, -info.modelCube.offsetY, -info.modelCube.offsetZ);
        GL11.glTranslatef(-info.modelCube.rotationPointX * f5, -info.modelCube.rotationPointY * f5, -info.modelCube.rotationPointZ * f5);
    }

    //Extracted from the ModelBaseDummy class
    private void applyParentTransformations(List<CubeInfo> parents, float f5) {
        for(int i = parents.size() - 1; i >= 0; i--) {
            CubeInfo parent = parents.get(i);

            if(i == parents.size() - 1) {
                GL11.glTranslatef(parent.modelCube.offsetX, parent.modelCube.offsetY, parent.modelCube.offsetZ);
                GL11.glTranslatef(parent.modelCube.rotationPointX * f5, parent.modelCube.rotationPointY * f5, parent.modelCube.rotationPointZ * f5);
                GL11.glScaled(parent.scale[0], parent.scale[1], parent.scale[2]);
                GL11.glTranslatef(-parent.modelCube.offsetX, -parent.modelCube.offsetY, -parent.modelCube.offsetZ);
                GL11.glTranslatef(-parent.modelCube.rotationPointX * f5, -parent.modelCube.rotationPointY * f5, -parent.modelCube.rotationPointZ * f5);
            }

            GL11.glTranslatef(parent.modelCube.offsetX, parent.modelCube.offsetY, parent.modelCube.offsetZ);
            GL11.glTranslatef(parent.modelCube.rotationPointX * f5, parent.modelCube.rotationPointY * f5, parent.modelCube.rotationPointZ * f5);

            if(parent.modelCube.rotateAngleZ != 0.0F) {
                GL11.glRotatef(parent.modelCube.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
            }

            if(parent.modelCube.rotateAngleY != 0.0F) {
                GL11.glRotatef(parent.modelCube.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
            }

            if(parent.modelCube.rotateAngleX != 0.0F){
                GL11.glRotatef(parent.modelCube.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
            }

        }
    }
}
