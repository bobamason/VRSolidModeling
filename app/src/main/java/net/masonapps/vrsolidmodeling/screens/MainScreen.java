package net.masonapps.vrsolidmodeling.screens;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.vrsolidmodeling.SolidModelingGame;
import net.masonapps.vrsolidmodeling.Style;
import net.masonapps.vrsolidmodeling.actions.AddAction;
import net.masonapps.vrsolidmodeling.actions.ColorAction;
import net.masonapps.vrsolidmodeling.actions.RemoveAction;
import net.masonapps.vrsolidmodeling.actions.TransformAction;
import net.masonapps.vrsolidmodeling.actions.UndoRedoCache;
import net.masonapps.vrsolidmodeling.environment.Grid;
import net.masonapps.vrsolidmodeling.math.Animator;
import net.masonapps.vrsolidmodeling.math.RotationUtil;
import net.masonapps.vrsolidmodeling.math.Side;
import net.masonapps.vrsolidmodeling.modeling.EditableNode;
import net.masonapps.vrsolidmodeling.modeling.ModelingProjectEntity;
import net.masonapps.vrsolidmodeling.modeling.primitives.Primitives;
import net.masonapps.vrsolidmodeling.modeling.transform.RotateWidget;
import net.masonapps.vrsolidmodeling.modeling.transform.ScaleWidget;
import net.masonapps.vrsolidmodeling.modeling.transform.SimpleGrabControls;
import net.masonapps.vrsolidmodeling.modeling.transform.TransformWidget3D;
import net.masonapps.vrsolidmodeling.modeling.transform.TranslateWidget;
import net.masonapps.vrsolidmodeling.modeling.ui.AddNodeInput;
import net.masonapps.vrsolidmodeling.modeling.ui.EditModeTable;
import net.masonapps.vrsolidmodeling.modeling.ui.InputProcessorChooser;
import net.masonapps.vrsolidmodeling.modeling.ui.MainInterface;
import net.masonapps.vrsolidmodeling.modeling.ui.SingleNodeSelector;
import net.masonapps.vrsolidmodeling.modeling.ui.ViewControls;
import net.masonapps.vrsolidmodeling.ui.GroupCompleteDialog;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.AABBTree;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.gfx.World;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 12/20/2017.
 */

public class MainScreen extends VrWorldScreen implements SolidModelingGame.OnControllerBackPressedListener {

    public static final float MIN_Z = 0.5f;
    public static final float MAX_Z = 10f;
    private static final String TAG = MainScreen.class.getSimpleName();
    private static final float UI_ALPHA = 0.25f;
    private final MainInterface mainInterface;
    private final UndoRedoCache undoRedoCache;
    private final ShapeRenderer shapeRenderer;
    private final Animator rotationAnimator;
    private final Animator positionAnimator;
    private final Entity gridEntity;
    private final TranslateWidget translateWidget;
    private final RotateWidget rotateWidget;
    private final ScaleWidget scaleWidget;
    private final GroupCompleteDialog groupDialog;
    private final Entity gradientBackground;
    // TODO: 3/23/2018 rename 
//    private final PlanarPointsInput pointInput;
    private TransformWidget3D transformUI;
    private boolean isTouchPadClicked = false;
    private Quaternion rotation = new Quaternion();
    private Quaternion lastRotation = new Quaternion();
    private Quaternion snappedRotation = new Quaternion();
    private Vector3 projectPosition = new Vector3(0, -0.5f, -2);
    private Vector3 position = new Vector3(projectPosition);
    private Vector3 snappedPosition = new Vector3(projectPosition);
    private Vector3 center = new Vector3();
    private float projectScale = 1f;
    private String projectName;
    // TODO: 3/28/2018 remove 
//    private ViewAction viewAction = ACTION_NONE;
//    private InputMode currentInputMode = InputMode.VIEW;
//    private State currentState = STATE_NONE;
    @Nullable
    private EditableNode focusedNode = null;
    @Nullable
    private EditableNode selectedNode = null;
    private List<EditableNode> multiSelectNodes = new ArrayList<>();
    private ModelingProjectEntity modelingProject;
    private Vector3 hitPoint = new Vector3();
    private AABBTree.IntersectionInfo intersectionInfo = new AABBTree.IntersectionInfo();
    private SimpleGrabControls grabControls = new SimpleGrabControls();
    private BoundingBox selectionBox = new BoundingBox();
    private Vector3 tmp = new Vector3();
    private Vector2 vec2 = new Vector2();
    private Grid gridFloor;
    private Vector3 cameraPosition = new Vector3();
    private boolean cameraUpdated = true;
    @Nullable
    private EditableNode nodeToAdd = null;
    private InputProcessorChooser inputProcessorChooser;
    private AddNodeInput addNodeInput;

    public MainScreen(SolidModelingGame game, String projectName) {
        this(game, projectName, new ArrayList<>());
    }

    public MainScreen(SolidModelingGame game, String projectName, List<EditableNode> nodeList) {
        super(game);
        final Skin skin = game.getSkin();
        this.projectName = projectName;
        gradientBackground = Style.newGradientBackground(getVrCamera().far - 1f);
        getWorld().add(gradientBackground);
        gradientBackground.invalidate();
        gridFloor = new Grid(2, skin.getRegion(Style.Drawables.grid), Color.LIGHT_GRAY);

        setBackgroundColor(Color.SKY);
        modelingProject = new ModelingProjectEntity();
        undoRedoCache = new UndoRedoCache();

        final ModelBuilder modelBuilder = new ModelBuilder();

        translateWidget = new TranslateWidget(modelBuilder);
        translateWidget.setVisible(false);

        rotateWidget = new RotateWidget(modelBuilder);
        rotateWidget.setVisible(false);

        scaleWidget = new ScaleWidget(modelBuilder);
        scaleWidget.setVisible(false);

        final TransformWidget3D.OnTransformActionListener transformActionListener = new TransformWidget3D.OnTransformActionListener() {

            TransformAction.Transform oldTransform;

            @Override
            public void onTransformStarted(@NonNull EditableNode entity) {
                oldTransform = entity.getTransform(new TransformAction.Transform());
            }

            @Override
            public void onTransformFinished(@NonNull EditableNode entity) {
                undoRedoCache.save(new TransformAction(entity, oldTransform, entity.getTransform(new TransformAction.Transform())));
                final AABBTree.Node leafNode = entity.getNode();
                if (leafNode != null)
                    leafNode.refit();
                setSelectedNode(entity);
            }
        };
        translateWidget.setListener(transformActionListener);
        rotateWidget.setListener(transformActionListener);
        scaleWidget.setListener(transformActionListener);
        grabControls.setListener(transformActionListener);

        transformUI = translateWidget;

        rotationAnimator = new Animator(new Animator.AnimationListener() {
            @Override
            public void apply(float value) {
                final Quaternion rot = modelingProject.getRotation();
                rot.set(rotation).slerp(snappedRotation, value);
                lastRotation.set(rot);
                modelingProject.invalidate();
                transformUI.setTransform(modelingProject.getTransform());
            }

            @Override
            public void finished() {
                rotation.set(snappedRotation);
                lastRotation.set(rotation);
            }
        });
        rotationAnimator.setInterpolation(Interpolation.linear);

        positionAnimator = new Animator(new Animator.AnimationListener() {
            @Override
            public void apply(float value) {
                modelingProject.getPosition().set(position).slerp(snappedPosition, value);
                modelingProject.invalidate();
                transformUI.setTransform(modelingProject.getTransform());
            }

            @Override
            public void finished() {
                position.set(snappedPosition);
            }
        });
        positionAnimator.setInterpolation(Interpolation.linear);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        final SpriteBatch spriteBatch = new SpriteBatch();
        manageDisposable(shapeRenderer, spriteBatch);

        modelingProject.setPosition(projectPosition);

        final MainInterface.UiEventListener uiEventListener = new MainInterface.UiEventListener() {

            @Override
            public void onAddClicked(String key) {
                // TODO: 3/28/2018 remove 
//                if (currentState == STATE_GROUPING) return;
                nodeToAdd = new EditableNode(key);
                addNode(nodeToAdd);
            }

            @Override
            public void onDeleteClicked() {
                // TODO: 3/28/2018 remove 
//                if (currentState == STATE_GROUPING) return;
                if (selectedNode != null) {
                    modelingProject.remove(selectedNode);
                    undoRedoCache.save(new RemoveAction(selectedNode, modelingProject));
                    setSelectedNode(null);
                }
            }

            @Override
            public void onDuplicateClicked() {
                // TODO: 3/28/2018 remove 
//                if (currentState == STATE_GROUPING) return;
                if (selectedNode != null) {
                    nodeToAdd = selectedNode.copy();
                }
            }

            @Override
            public void onColorChanged(Color color) {
                // TODO: 3/28/2018 remove 
//                if (currentState == STATE_GROUPING) return;
                if (selectedNode != null) {
                    final ColorAction colorAction = new ColorAction(selectedNode, selectedNode.getDiffuseColor().cpy(), color.cpy(), c -> mainInterface.getColorPicker().setColor(c));
                    undoRedoCache.save(colorAction);
                    selectedNode.setAmbientColor(color);
                    selectedNode.setDiffuseColor(color);
                }
            }

            @Override
            public void onEditModeChanged(EditModeTable.EditMode mode) {
                // TODO: 3/28/2018 remove 
//                if (currentState == STATE_GROUPING) return;
                setEditMode(mode);
            }

            @Override
            public void onUndoClicked() {
                // TODO: 3/28/2018 remove 
//                if (currentState == STATE_GROUPING) return;
                undoRedoCache.undo();
            }

            @Override
            public void onRedoClicked() {
                // TODO: 3/28/2018 remove 
//                if (currentState == STATE_GROUPING) return;
                undoRedoCache.redo();
            }

            @Override
            public void onExportClicked() {
                // TODO: 3/28/2018 remove 
//                if (currentState == STATE_GROUPING) return;
                getSolidModelingGame().switchToExportScreen();
            }

            @Override
            public void onGroupClicked() {
                // TODO: 3/28/2018 remove 
//                currentState = STATE_GROUPING;
                groupDialog.show();
            }

            @Override
            public void onUnGroupClicked() {
                // TODO: 3/28/2018 remove 
//                if (currentState == STATE_GROUPING) return;
                if (selectedNode != null && selectedNode.isGroup()) {
                    modelingProject.remove(selectedNode);
                    final int n = selectedNode.getChildCount();
                    for (int i = 0; i < n; i++) {
                        final Node child = selectedNode.getChild(i);
                        if (child instanceof EditableNode) {
                            final EditableNode editableNode = (EditableNode) child;
                            editableNode.translation.mul(selectedNode.localTransform);
                            editableNode.rotation.mulLeft(selectedNode.rotation);
                            editableNode.scale.scl(selectedNode.scale);
                            editableNode.invalidate();
                            modelingProject.add(editableNode);
                        }
                    }
                }
            }
        };
        mainInterface = new MainInterface(spriteBatch, skin, uiEventListener);
        mainInterface.loadWindowPositions(PreferenceManager.getDefaultSharedPreferences(GdxVr.app.getContext()));


        final float sliderVal = 1f - (float) Math.sqrt((-projectPosition.z - MIN_Z) / (MAX_Z - MIN_Z));
        mainInterface.getViewControls().getZoomSlider().setValue(sliderVal);
        mainInterface.getViewControls().setListener(new ViewControls.ViewControlListener() {
            @Override
            public void onViewSelected(Side side) {
                RotationUtil.rotateToViewSide(snappedRotation, side);
                final Quaternion rotDiff = Pools.obtain(Quaternion.class);
                rotDiff.set(rotation).conjugate().mulLeft(snappedRotation);
                final float angleRad = rotDiff.getAngleRad();
                final float duration = Math.abs(angleRad < MathUtils.PI ? angleRad : MathUtils.PI2 - angleRad) / MathUtils.PI;
                Pools.free(rotDiff);
                rotationAnimator.setDuration(duration);
                rotationAnimator.start();
            }

            @Override
            public void onZoomChanged(float value) {
                final float z = -MathUtils.lerp(MIN_Z, MAX_Z, (1f - value) * (1f - value));
                projectPosition.z = z;
                if (selectedNode != null) {
                    center.set(selectedNode.getPosition());
                } else {
                    center.set(0, 0, 0);
                }
                snappedPosition.set(center).scl(-1).mul(rotation).add(projectPosition);
                position.set(snappedPosition);
                modelingProject.setPosition(position);
            }
        });

        groupDialog = new GroupCompleteDialog(spriteBatch, skin, new GroupCompleteDialog.GroupDialogListener() {
            @Override
            public void onCancelClicked() {
                multiSelectNodes.clear();
                // TODO: 3/28/2018 remove 
//                currentState = STATE_NONE;
            }

            @Override
            public void onDoneClicked() {
                final EditableNode group = new EditableNode();
                for (EditableNode node : multiSelectNodes) {
                    modelingProject.remove(node);
                    group.addChild(node);
                }
                modelingProject.add(group);
                multiSelectNodes.clear();
                // TODO: 3/28/2018 remove 
//                currentState = STATE_NONE;
            }
        });
        groupDialog.setPosition(0, -1f, 0);
        mainInterface.addProcessor(groupDialog);
        inputProcessorChooser = new InputProcessorChooser();
        mainInterface.addProcessor(inputProcessorChooser);

        gridEntity = new Entity(new ModelInstance(createGrid(modelBuilder, skin, 3f)));
        gridEntity.setLightingEnabled(false);
        getWorld().add(gridEntity).setTransform(modelingProject.getTransform());
        gridEntity.setVisible(false);

        getWorld().add(modelingProject);
        modelingProject.setScale(projectScale);

        for (EditableNode node : nodeList) {
            modelingProject.add(node);
        }

        // TODO: 3/23/2018 remove test 
//        pointInput = new PlanarPointsInput(modelingProject, point -> Logger.d("point added " + point));
//        pointInput.getPlane().set(Vector3.Zero, Vector3.Z);

        // TODO: 3/28/2018 remove tests 
        addNodeInput = new AddNodeInput(modelingProject, node -> Logger.d("node added"));
        final EditableNode previewNode = new EditableNode(Primitives.KEY_CONE);
        previewNode.initMesh();
        addNodeInput.setPreviewNode(previewNode);
        addNodeInput.setVisible(false);
//        inputProcessorChooser.setActiveProcessor(addNodeInput);
        inputProcessorChooser.setActiveProcessor(new SingleNodeSelector(modelingProject, this::setSelectedNode));
    }

    private static Model createGrid(ModelBuilder builder, Skin skin, float radius) {
//        final Material material = new Material(TextureAttribute.createDiffuse(skin.getRegion(Style.Drawables.grid)), ColorAttribute.createDiffuse(Color.WHITE), FloatAttribute.createAlphaTest(0.25f), IntAttribute.createCullFace(0), new BlendingAttribute(true, 0.5f));
        final Material material = new Material(ColorAttribute.createDiffuse(Color.GOLDENROD), IntAttribute.createCullFace(0), new BlendingAttribute(true, 0.25f));
        return builder.createRect(
                -radius, -radius, 0f,
                radius, -radius, 0f,
                radius, radius, 0f,
                -radius, radius, 0f,
                0f, 1f, 0f,
                material,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates
        );
    }

    private static Model createGridBox(ModelBuilder builder, Skin skin, float radius) {
        final Material material = new Material(TextureAttribute.createDiffuse(skin.getRegion(Style.Drawables.grid)), ColorAttribute.createDiffuse(Color.GRAY), FloatAttribute.createAlphaTest(0.5f), IntAttribute.createCullFace(GL20.GL_FRONT), new BlendingAttribute(true, 1f));
        return builder.createBox(radius * 2f, radius * 2f, radius * 2f, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates
        );
    }

    private static Model createBoxModel(ModelBuilder builder, Color color, BoundingBox bounds) {
        builder.begin();
        final MeshPartBuilder part = builder.part("s", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(color), new BlendingAttribute(true, 0.5f)));
        BoxShapeBuilder.build(part, bounds);
        return builder.end();
    }

    private static void drawBounds(ShapeRenderer shapeRenderer, BoundingBox bounds) {
        shapeRenderer.box(bounds.min.x, bounds.min.y, bounds.max.z,
                bounds.getWidth(), bounds.getHeight(), bounds.getDepth());
    }

    private void addNode(EditableNode node) {
        modelingProject.add(node);
        setSelectedNode(node);
        undoRedoCache.save(new AddAction(node, modelingProject));
    }

    protected void drawEntityBounds(ShapeRenderer shapeRenderer, EditableNode entity, Color color) {
        shapeRenderer.setColor(color);
        shapeRenderer.setTransformMatrix(modelingProject.getTransform());
        final BoundingBox bounds = entity.getAABB();
        drawBounds(shapeRenderer, bounds);
    }

    protected void drawSelectionBox(ShapeRenderer shapeRenderer, Color color) {
        if (!selectionBox.isValid()) return;
        shapeRenderer.setColor(color);
        shapeRenderer.setTransformMatrix(modelingProject.getTransform());
        drawBounds(shapeRenderer, selectionBox);
    }

    protected void setEditMode(EditModeTable.EditMode mode) {
        switch (mode) {
            case TRANSLATE:
                transformUI = translateWidget;
                rotateWidget.setVisible(false);
                scaleWidget.setVisible(false);
                transformUI.setVisible(selectedNode != null);
                break;
            case ROTATE:
                transformUI = rotateWidget;
                translateWidget.setVisible(false);
                scaleWidget.setVisible(false);
                transformUI.setVisible(selectedNode != null);
                break;
            case SCALE:
                transformUI = scaleWidget;
                translateWidget.setVisible(false);
                rotateWidget.setVisible(false);
                transformUI.setVisible(selectedNode != null);
                break;
            default:
                transformUI.setVisible(false);
                break;
        }
        transformUI.setEntity(selectedNode, modelingProject);
        mainInterface.setEditMode(mode);
    }

    private SolidModelingGame getSolidModelingGame() {
        return (SolidModelingGame) game;
    }

    @Override
    protected World createWorld() {
        return new World() {

            @Override
            public void update() {
                super.update();
                modelingProject.update();
                transformUI.update();
            }

            @Override
            public void render(ModelBatch batch, Environment environment) {
                super.render(batch, environment);
                gridFloor.render(batch);
                transformUI.render(batch);
                addNodeInput.render(batch);
            }
        };
    }

    @Override
    protected void addLights(Array<BaseLight> lights) {
        final DirectionalLight light = new DirectionalLight();
        light.setColor(Color.WHITE);
        light.setDirection(new Vector3(1, -1, -1).nor());
        lights.add(light);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(GdxVr.app.getContext()).edit();
        mainInterface.saveWindowPositions(editor);
        editor.apply();
    }

    @Override
    public void show() {
        super.show();
        // TODO: 3/23/2018 remove test 
//        GdxVr.input.setInputProcessor(pointInput);
        GdxVr.input.setInputProcessor(mainInterface);
        getVrCamera().position.set(0, 0f, 0);
        getVrCamera().lookAt(0, 0, -1);
//        buttonControls.attachListener();
    }

    @Override
    public void hide() {
        super.hide();
        GdxVr.input.setInputProcessor(null);
//        buttonControls.detachListener();
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(GdxVr.app.getContext()).edit();
        mainInterface.saveWindowPositions(editor);
        editor.apply();
    }

    @Override
    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
        updateCamera();
        super.onDrawFrame(headTransform, leftEye, rightEye);
    }

    private void updateCamera() {
        if (cameraUpdated) {
            getVrCamera().position.set(cameraPosition);
            gradientBackground.setPosition(getVrCamera().position);
            mainInterface.setTransformable(true);
            mainInterface.setPosition(getVrCamera().position);
            mainInterface.lookAt(tmp.set(getVrCamera().direction).scl(-1).add(getVrCamera().position), getVrCamera().up);
            cameraUpdated = false;
        }
    }

    @Override
    public void update() {
        super.update();
        grabControls.update(hitPoint, modelingProject);
        // TODO: 3/28/2018 remove 
//        if (currentInputMode == InputMode.SELECT || currentInputMode == InputMode.MULTI_SELECT || currentInputMode == InputMode.EDIT)
//            getSolidModelingGame().getCursor().position.set(hitPoint);
        mainInterface.act();
        rotationAnimator.update(GdxVr.graphics.getDeltaTime());
        positionAnimator.update(GdxVr.graphics.getDeltaTime());
//        Logger.d(GdxVr.graphics.getFramesPerSecond() + "fps");
    }

    @Nullable
    public EditableNode getSelectedNode() {
        return selectedNode;
    }

    private void setSelectedNode(@Nullable EditableNode entity) {
        selectedNode = entity;
        mainInterface.setEntity(selectedNode);

        if (selectedNode != null) {
//            center.set(selectedNode.getPosition());

            final Color diffuseColor = selectedNode.getDiffuseColor();
            if (diffuseColor != null)
                mainInterface.getColorPicker().setColor(diffuseColor);
        } else {
            transformUI.setVisible(false);
        }
//        snappedPosition.set(center).scl(-1).mul(rotation).add(projectPosition);
//        positionAnimator.setDuration(0.5f);
//        positionAnimator.start();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        GdxVr.gl.glEnable(GL20.GL_DEPTH_TEST);
        shapeRenderer.begin();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setTransformMatrix(modelingProject.getTransform());
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(0, 0, 0, 1, 0, 0);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.line(0, 0, 0, 0, 0, 1);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.line(0, 0, 0, 0, 1, 0);
        shapeRenderer.end();
        GdxVr.gl.glDisable(GL20.GL_DEPTH_TEST);

        super.render(camera, whichEye);

        shapeRenderer.begin();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setTransformMatrix(modelingProject.getTransform());
//        AABBTree.debugAABBTree(shapeRenderer, modelingProject.getAABBTree(), Color.YELLOW);
        transformUI.drawShapes(shapeRenderer);
        if (focusedNode != null) {
            drawEntityBounds(shapeRenderer, focusedNode, Color.BLACK);
        }
        if (selectedNode != null) {
            drawEntityBounds(shapeRenderer, selectedNode, Color.WHITE);
//            debugBVH(shapeRenderer, modelingProject, Color.YELLOW);
        }
        // TODO: 3/28/2018 remove 
//        if (currentState == STATE_GROUPING)
//            drawSelectionBox(shapeRenderer, Color.WHITE);

        // TODO: 3/23/2018 improve draw 
//        pointInput.draw(shapeRenderer);
        
        shapeRenderer.end();

        mainInterface.draw(camera);
    }

    private void rotate() {
        final Quaternion rotDiff = Pools.obtain(Quaternion.class);
        rotDiff.set(lastRotation).conjugate().mulLeft(GdxVr.input.getControllerOrientation());
//        RotationUtil.snapAxisAngle(rotDiff);
//        Logger.d("rotDiff " + rotDiff);

        rotation.mulLeft(rotDiff);
        modelingProject.setRotation(rotation);
        position.set(center).scl(-1).mul(rotation).add(projectPosition);
        modelingProject.setPosition(position);
//        gridEntity.setPosition(modelingProject.getPosition());
//        gridEntity.setRotation(modelingProject.getRotation());
        lastRotation.set(GdxVr.input.getControllerOrientation());
        Pools.free(rotDiff);
        transformUI.setTransform(modelingProject.getTransform());
    }

    @Override
    public void onControllerBackButtonClicked() {
        if (!mainInterface.onControllerBackButtonClicked()) {
            if (selectedNode != null) {
                setSelectedNode(null);
            } else {
                getSolidModelingGame().closeModelingScreen();
                getSolidModelingGame().switchToStartupScreen();
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        undoRedoCache.clear();
    }

    public String getProjectName() {
        return projectName;
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        // TODO: 3/28/2018 remove 
//        updateCurrentInputMode();
//        if (currentState == STATE_VIEW_TRANSFORM) {
//            getSolidModelingGame().setCursorVisible(false);
//            mainInterface.setVisible(false);
//            if (viewAction == ROTATE)
//                rotate();
////            else if (transformAction == PAN)
////                pan();
////            else if (transformAction == ZOOM)
////                zoom();
//        } else {
//            getSolidModelingGame().setCursorVisible(true);
//        }
//
//        if (controller.clickButtonState) {
//            if (!isTouchPadClicked) {
//                onTouchPadButtonDown();
//                isTouchPadClicked = true;
//            }
//        } else {
//            if (isTouchPadClicked) {
//                onTouchPadButtonUp();
//                isTouchPadClicked = false;
//            }
//        }
    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {
    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
        switch (event.action) {
            case DaydreamTouchEvent.ACTION_DOWN:
                vec2.set(event.x - 0.5f, event.y - 0.5f);
                break;
            case DaydreamTouchEvent.ACTION_MOVE:
                final float dx = event.x - 0.5f - vec2.x;
                final float dy = event.y - 0.5f - vec2.y;
                final float min = 0.25f * 0.25f;
                final float dx2 = dx * dx;
                final float dy2 = dy * dy;
                // TODO: 3/28/2018 remove 
//                if (currentState == STATE_NONE && dx2 + dy2 > min) {
                if (dx2 + dy2 > min) {
                    cameraPosition.set(getVrCamera().position);
                    cameraUpdated = true;
                    if (dx2 > dy2) {
//                        getVrCamera().direction.rotate(getVrCamera().up, -dx * 45f * GdxVr.graphics.getDeltaTime()).nor();
                        tmp.set(getRightVector());
                        tmp.y = 0;
                        tmp.nor();
                        tmp.scl(dx * 2f * GdxVr.graphics.getDeltaTime());
                        cameraPosition.add(tmp);
                    } else {
                        tmp.set(getForwardVector());
                        tmp.y = 0;
                        tmp.nor();
                        tmp.scl(-dy * 2f * GdxVr.graphics.getDeltaTime());
                        cameraPosition.add(tmp);
                    }
                }
                break;
            case DaydreamTouchEvent.ACTION_UP:
                break;
        }
    }

    // TODO: 3/28/2018 remove 
//    private void onTouchPadButtonDown() {
//        switch (currentInputMode) {
//            case UI:
////                currentState = STATE_NONE;
//                break;
//            case MULTI_SELECT:
//                if (focusedNode != null && !multiSelectNodes.contains(focusedNode)) {
//                    multiSelectNodes.add(focusedNode);
//                    selectionBox.ext(focusedNode.getAABB());
//                }
//                break;
//            case EDIT:
//                currentState = STATE_EDITING;
//                transformUI.touchDown();
//                break;
//            case SELECT:
//                if (focusedNode != null && !grabControls.isTransforming()) {
//                    final Vector3 tmp = Pools.obtain(Vector3.class);
//                    setSelectedNode(focusedNode);
//                    grabControls.begin(focusedNode, hitPoint, modelingProject);
//                    final Vector3 position = focusedNode.getPosition();
//                    gridEntity.setPosition(position);
//                    final Vector3 normal = grabControls.getPlane().getNormal();
//                    gridEntity.lookAt(tmp.set(position).add(normal), Math.abs(normal.dot(Vector3.Y)) > 0.99f ? Vector3.Z : Vector3.Y);
//                    gridEntity.getPosition().mul(modelingProject.getTransform());
//                    gridEntity.getRotation().mulLeft(modelingProject.getRotation());
//                    gridEntity.invalidate();
//                    gridEntity.setVisible(true);
//                }
//                break;
//            case VIEW:
//                lastRotation.set(GdxVr.input.getControllerOrientation());
//                viewAction = ROTATE;
//                currentState = STATE_VIEW_TRANSFORM;
//                break;
//            default:
//                break;
//        }
//    }

    // TODO: 3/28/2018 remove 
//    private void onTouchPadButtonUp() {
//        switch (currentState) {
//            case STATE_NONE:
//                if (grabControls.isTransforming()) {
//                    grabControls.end();
//                    gridEntity.setVisible(false);
//                }
//                break;
//            case STATE_EDITING:
//                transformUI.touchUp();
//                break;
//            case STATE_VIEW_TRANSFORM:
//                if (RotationUtil.snap(rotation, snappedRotation, 0.1f)) {
//                    final Quaternion rotDiff = Pools.obtain(Quaternion.class);
//                    rotDiff.set(rotation).conjugate().mulLeft(snappedRotation);
//                    final float angleRad = rotDiff.getAngleRad();
//                    final float duration = Math.abs(angleRad < MathUtils.PI ? angleRad : MathUtils.PI2 - angleRad) / MathUtils.PI;
//                    Pools.free(rotDiff);
//                    rotationAnimator.setDuration(duration);
//                    rotationAnimator.start();
//
//                    snappedPosition.set(center).scl(-1).mul(snappedRotation).add(projectPosition);
//                    positionAnimator.setDuration(duration);
//                    positionAnimator.start();
//                }
////                final float len = getVrCamera().position.len();
////                RotationUtil.setToClosestUnitVector(getVrCamera().position).scl(len);
////                RotationUtil.setToClosestUnitVector(getVrCamera().up);
////                getVrCamera().lookAt(Vector3.Zero);
//                viewAction = ACTION_NONE;
//                break;
//            default:
//                break;
//        }
//        currentState = STATE_NONE;
//        mainInterface.setAlpha(1f);
//        mainInterface.setVisible(true);
//    }

    // TODO: 3/28/2018 remove 
//    private void updateCurrentInputMode() {
//        focusedNode = null;
//        switch (currentState) {
//            case STATE_EDITING:
//                transformUI.performRayTest(getControllerRay());
//                hitPoint.set(transformUI.getHitPoint3D());
//                currentInputMode = InputMode.EDIT;
//                break;
//            case STATE_NONE:
//                if (mainInterface.isCursorOver())
//                    currentInputMode = InputMode.UI;
//                else if (transformUI.performRayTest(getControllerRay())) {
//                    hitPoint.set(transformUI.getHitPoint3D());
//                    currentInputMode = InputMode.EDIT;
//                } else if (modelingProject.rayTest(getControllerRay(), intersectionInfo)) {
//                    hitPoint.set(intersectionInfo.hitPoint);
//                    if (!grabControls.isTransforming()) {
//                        focusedNode = (EditableNode) intersectionInfo.object;
//                        currentInputMode = InputMode.SELECT;
//                    }
//                } else
//                    currentInputMode = InputMode.VIEW;
//                break;
//            case STATE_VIEW_TRANSFORM:
//                currentInputMode = InputMode.VIEW;
//                break;
//            case STATE_GROUPING:
//                if (mainInterface.isCursorOver()) {
//                    currentInputMode = InputMode.UI;
//                } else if (modelingProject.rayTest(getControllerRay(), intersectionInfo)) {
//                    hitPoint.set(intersectionInfo.hitPoint);
//                    focusedNode = (EditableNode) intersectionInfo.object;
//                    currentInputMode = InputMode.MULTI_SELECT;
//                } else
//                    currentInputMode = InputMode.VIEW;
//                break;
//        }
//    }

    public ModelingProjectEntity getModelingProject() {
        return modelingProject;
    }

    // TODO: 3/28/2018 remove 
//    enum ViewAction {
//        ACTION_NONE, ROTATE, PAN, ZOOM
//    }
//
//    enum InputMode {
//        UI, VIEW
//    }
//
//    enum State {
//        STATE_VIEW_TRANSFORM, STATE_EDITING, STATE_NONE
//    }
}
