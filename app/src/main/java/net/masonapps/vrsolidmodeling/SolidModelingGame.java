package net.masonapps.vrsolidmodeling;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.vrsolidmodeling.io.ProjectFileIO;
import net.masonapps.vrsolidmodeling.modeling.BaseModelingProject;
import net.masonapps.vrsolidmodeling.modeling.primitives.AssetPrimitive;
import net.masonapps.vrsolidmodeling.modeling.primitives.Primitives;
import net.masonapps.vrsolidmodeling.screens.ExportScreen;
import net.masonapps.vrsolidmodeling.screens.LoadingScreen;
import net.masonapps.vrsolidmodeling.screens.ModelingScreen;
import net.masonapps.vrsolidmodeling.screens.OpenProjectListScreen;
import net.masonapps.vrsolidmodeling.screens.ProgressLoadingScreen;
import net.masonapps.vrsolidmodeling.screens.StartupScreen;
import net.masonapps.vrsolidmodeling.service.ExportService;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrScreen;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * Created by Bob Mason on 12/20/2017.
 */

public class SolidModelingGame extends VrGame {

    private boolean isAtlasLoaded = false;
    private LoadingScreen loadingScreen;
    private ProgressLoadingScreen progressLoadingScreen;
    @Nullable
    private ModelingScreen modelingScreen = null;
    private Skin skin;
    @Nullable
    private StartupScreen startupScreen = null;
    private int buttonSoundId = GvrAudioEngine.INVALID_ID;
    @Nullable
    private ModelInstance roomInstance = null;
    private boolean loadingFailed = false;
    private boolean appButtonDown = false;

    @SuppressLint("SimpleDateFormat")
    private static String generateNewProjectName() {
        return "model_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//        return "sculpt_" + MathUtils.random(5000);
    }

    @Override
    public void create() {
        super.create();
        getVrCamera().near = 0.1f;
        getVrCamera().far = 50f;
        loadingScreen = new LoadingScreen(this);
        setScreen(loadingScreen);
        skin = new Skin();
        loadAsset(Assets.ROOM_MODEL, Model.class);
        loadAsset(Style.ATLAS_FILE, TextureAtlas.class);
//        final TextureLoader.TextureParameter textureParameter = new TextureLoader.TextureParameter();
//        textureParameter.minFilter = Texture.TextureFilter.Linear;
//        textureParameter.magFilter = Texture.TextureFilter.Linear;

        GdxVr.graphics.setPostProcessingShader(new ShaderProgram(Gdx.files.internal("shaders/post.vertex.glsl"), Gdx.files.internal("shaders/post.fragment.glsl")));
    }

    @Override
    public void preloadSoundFiles(GvrAudioEngine gvrAudioEngine) {
        if (!gvrAudioEngine.preloadSoundFile(Assets.BUTTON_SOUND_FILE))
            Logger.e("load sound " + Assets.BUTTON_SOUND_FILE + " failed");
    }

    @Override
    protected void doneLoading(AssetManager assets) {
        if (!isAtlasLoaded) {
            roomInstance = new ModelInstance(assets.get(Assets.ROOM_MODEL, Model.class));
            roomInstance.transform.idt().translate(0, 0, -4).scale(0.05f, 0.05f, 0.05f);

            final TextureAtlas atlas = assets.get(Style.ATLAS_FILE, TextureAtlas.class);
            getSkin().addRegions(atlas);
            setupSkin();
            progressLoadingScreen = new ProgressLoadingScreen(this, getSkin());
            isAtlasLoaded = true;

            setScreen(progressLoadingScreen);
            setLoadingScreenMessage("initializing shapes");

            initShapes();
        }
    }

    private void initShapes() {

        CompletableFuture.runAsync(() ->
                Primitives.getMap().values()
                        .forEach(primitive -> {
                            if (primitive instanceof AssetPrimitive) {
                                final android.content.res.AssetManager assets = GdxVr.app.getActivityWeakReference().get().getAssets();
                                try {
                                    primitive.initialize(assets.open(((AssetPrimitive) primitive).getAsset()));
                                } catch (Exception e) {
                                    Logger.e("unable to load primitive " + primitive.getName(), e);
                                }
                            } else {
                                primitive.initialize(null);
                            }
                        }))
                .thenRun(() -> GdxVr.app.postRunnable(this::switchToStartupScreen));
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (loadingScreen != null)
            loadingScreen.dispose();
        if (progressLoadingScreen != null)
            progressLoadingScreen.dispose();
        if (modelingScreen != null)
            modelingScreen.dispose();
    }

    private void resetProgressLoadingScreen() {
        GdxVr.app.postRunnable(() -> {
            if (progressLoadingScreen != null) {
                progressLoadingScreen.setMessage(Style.getStringResource(R.string.loading, "loading..."));
                progressLoadingScreen.setAnimationEnabled(true);
            }
        });
    }

    private void setLoadingScreenAnimationEnabled(final boolean enabled) {
        GdxVr.app.postRunnable(() -> {
            if (progressLoadingScreen != null)
                progressLoadingScreen.setAnimationEnabled(enabled);
        });
    }

    private void setLoadingScreenMessage(final String msg) {
        GdxVr.app.postRunnable(() -> {
            if (progressLoadingScreen != null)
                progressLoadingScreen.setMessage(msg);
        });
    }

    public void switchToStartupScreen() {
        if (startupScreen == null) {
            startupScreen = new StartupScreen(this, new StartupScreen.StartupScreenListener() {
                @Override
                public void onCreateNewProjectClicked() {
                    createNewProject();
                }

                @Override
                public void onOpenProjectClicked() {
                    switchToOpenProjectScreen();
                }
            });
        }
        setScreen(startupScreen);
        resetProgressLoadingScreen();
    }

    private void switchToOpenProjectScreen() {
        setScreen(new OpenProjectListScreen(this, getProjectFileList(), this::openProject));
        resetProgressLoadingScreen();
    }

    private void createNewProject() {
        setScreen(progressLoadingScreen);
        loadingFailed = false;
        final String projectName = generateNewProjectName();
        Logger.d("new project created: " + projectName);
        GdxVr.app.postRunnable(() -> setScreen(new ModelingScreen(SolidModelingGame.this, projectName)));
    }

    private void openProject(final File file) {
        setScreen(progressLoadingScreen);
        final String fileName = file.getName();
        loadingFailed = false;
        CompletableFuture.supplyAsync(() -> {
            try {
                setLoadingScreenMessage("loading project...");
                return ProjectFileIO.loadFile(file, Primitives.getMap());
            } catch (Exception e) {
                e.printStackTrace();
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            Logger.e("unable to open project " + fileName, e);
            setLoadingScreenMessage(e.getLocalizedMessage());
            return null;
        }).thenAccept(modelingObjects -> {
            if (modelingObjects != null) {
                final int endIndex = fileName.lastIndexOf('.');
                final String projectName = endIndex == -1 ? fileName : fileName.substring(0, endIndex);
                // FIXME: 2/12/2018 
//                GdxVr.app.postRunnable(() -> setScreen(new ModelingScreen(SolidModelingGame.this, projectName, modelingObjects)));
            } else {
                loadingFailed = true;
                showError("unable to open project");
            }
        });
    }

    private void showError(String message) {
        setLoadingScreenMessage(message);
        setLoadingScreenAnimationEnabled(false);
        Logger.e(message);
    }

    public void saveCurrentProject() {
        // FIXME: 2/12/2018 
//        if (modelingScreen != null)
//            saveCurrentProject(modelingScreen.getModelingProject(), modelingScreen.getProjectName());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveCurrentProject(final BaseModelingProject modelingProject, final String projectName) {
        Log.d(Constants.APP_NAME, "saving project " + projectName + "...");
        Activity activity = GdxVr.app.getActivityWeakReference().get();
        if (activity != null) {
            ((SolidModelingApplication) activity.getApplication()).setModelingProject(new ArrayList<>(modelingProject.getModelingObjectList()), new Matrix4());
            final File file = new File(activity.getFilesDir(), projectName + "." + Constants.FILE_TYPE_PROJECT);
            final Intent intent = new Intent(activity, ExportService.class);
            intent.putExtra(Constants.KEY_FILE_PATH, file.getAbsolutePath());
            intent.putExtra(Constants.KEY_FILE_TYPE, Constants.FILE_TYPE_PROJECT);
            intent.putExtra(Constants.KEY_EXTERNAL, false);
            activity.startService(intent);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    private void exportFile(final BaseModelingProject modelingProject, final String projectName, final String fileType, Matrix4 transform) {
        Activity activity = GdxVr.app.getActivityWeakReference().get();
        if (activity == null || modelingScreen == null) return;
        if (!((MainActivity) activity).areStoragePermissionsGranted()) {
            ((MainActivity) activity).requestStoragePermissions((readGranted, writeGranted) -> {
                if (writeGranted) {
                    exportFile(modelingProject, projectName, fileType, transform);
                }
            });
        } else {
            ((SolidModelingApplication) activity.getApplication()).setModelingProject(new ArrayList<>(modelingProject.getModelingObjectList()), transform);
            final File dir = new File(Environment.getExternalStorageDirectory(), Constants.EXTERNAL_DIRECTORY);
            dir.mkdirs();
            String extension;
            if (fileType.equals(Constants.FILE_TYPE_OBJ))
                extension = "";
            else
                extension = "." + fileType;

            File file = new File(dir, "sculpt" + projectName + extension);
            int i = 2;
            while (file.exists()) {
                file = new File(dir, "sculpt" + projectName + (i++) + extension);
            }
            final Intent intent = new Intent(activity, ExportService.class);
            intent.putExtra(Constants.KEY_FILE_PATH, file.getAbsolutePath());
            intent.putExtra(Constants.KEY_FILE_TYPE, fileType);
            intent.putExtra(Constants.KEY_EXTERNAL, true);
            activity.startService(intent);
        }
    }

    @Override
    public void setScreen(VrScreen screen) {
        super.setScreen(screen);
        if (screen instanceof ModelingScreen) {
            modelingScreen = (ModelingScreen) screen;
        } else if (screen instanceof ProgressLoadingScreen) {
            resetProgressLoadingScreen();
        }
    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {
        super.onControllerButtonEvent(controller, event);
        if (event.button == DaydreamButtonEvent.BUTTON_APP) {
            if (!appButtonDown && event.action == DaydreamButtonEvent.ACTION_DOWN) {
                appButtonDown = true;
                onControllerBackButtonClicked();
            }
            if (event.action == DaydreamButtonEvent.ACTION_UP) {
                appButtonDown = false;
            }
        } else if (event.button == DaydreamButtonEvent.BUTTON_TOUCHPAD) {
            if (event.action == DaydreamButtonEvent.ACTION_DOWN)
                playButtonSound();
        }
    }

    protected void playButtonSound() {
        final VrInputProcessor inputProcessor = GdxVr.input.getVrInputProcessor();
        if (inputProcessor != null && inputProcessor.isCursorOver() && inputProcessor.getHitPoint3D() != null) {
            final Vector3 hitPoint3D = inputProcessor.getHitPoint3D();
            final GvrAudioEngine gvrAudioEngine = getGvrAudioEngine();
            buttonSoundId = gvrAudioEngine.createSoundObject(Assets.BUTTON_SOUND_FILE);
            if (buttonSoundId != -1) {
                gvrAudioEngine.setSoundObjectPosition(buttonSoundId, hitPoint3D.x, hitPoint3D.y, hitPoint3D.z);
                gvrAudioEngine.playSound(buttonSoundId, false);
            } else {
                Logger.e("sound " + Assets.BUTTON_SOUND_FILE + " failed to play");
            }
        }
    }

    private void onControllerBackButtonClicked() {
        if (getScreen() instanceof OnControllerBackPressedListener)
            ((OnControllerBackPressedListener) getScreen()).onControllerBackButtonClicked();
    }

    public void closeModelingScreen() {
        if (modelingScreen == null) return;
        modelingScreen.hide();
        // FIXME: 2/12/2018 
//        saveCurrentProject(modelingScreen.getModelingProject(), modelingScreen.getProjectName());
        modelingScreen.dispose();
        modelingScreen = null;
    }

    public boolean isLoadingFailed() {
        return loadingFailed;
    }

    @Nullable
    public ModelingScreen getModelingScreen() {
        return modelingScreen;
    }

    private void setupSkin() {
        addFont();
        addSliderStyle();
        addProgressBarStyle();
        addButtonStyle();
        addCheckBoxStyle();
        addLabelStyle();
    }

    private void addFont() {
        getSkin().add(Style.DEFAULT_FONT, new BitmapFont(Gdx.files.internal(Style.FONT_FILE), getSkin().getRegion(Style.FONT_REGION)), BitmapFont.class);
    }

    private void addProgressBarStyle() {
        final ProgressBar.ProgressBarStyle progressBarStyle = new ProgressBar.ProgressBarStyle();
        progressBarStyle.background = getSkin().newDrawable(Style.Drawables.slider, Style.COLOR_UP_2);
        progressBarStyle.knobBefore = getSkin().newDrawable(Style.Drawables.slider, Style.COLOR_ACCENT);
        getSkin().add(Style.DEFAULT_HORIZONTAL, progressBarStyle, ProgressBar.ProgressBarStyle.class);
    }

    private void addSliderStyle() {
        final Slider.SliderStyle sliderStyle = new Slider.SliderStyle(getSkin().newDrawable(Style.Drawables.slider, Style.SLIDER_BG_COLOR), getSkin().newDrawable(Style.Drawables.slider_knob));
        sliderStyle.knobBefore = getSkin().newDrawable(Style.Drawables.slider, Style.COLOR_ACCENT);
        getSkin().add(Style.DEFAULT_HORIZONTAL, sliderStyle, Slider.SliderStyle.class);
    }

    private void addButtonStyle() {
        final ImageButton.ImageButtonStyle imageButtonStyle = new ImageButton.ImageButtonStyle();
        imageButtonStyle.up = skin.newDrawable(Style.Drawables.round_button, Style.COLOR_UP);
        imageButtonStyle.over = skin.newDrawable(Style.Drawables.round_button, Style.COLOR_OVER);
        imageButtonStyle.down = skin.newDrawable(Style.Drawables.round_button, Style.COLOR_DOWN);
        imageButtonStyle.disabled = skin.newDrawable(Style.Drawables.round_button, Style.COLOR_DISABLED);
        imageButtonStyle.checked = null;
        getSkin().add(Style.DEFAULT, imageButtonStyle, ImageButton.ImageButtonStyle.class);

        final ImageTextButton.ImageTextButtonStyle imageTextButtonStyle = new ImageTextButton.ImageTextButtonStyle();
        imageTextButtonStyle.font = skin.getFont(Style.DEFAULT_FONT);
        imageTextButtonStyle.up = skin.newDrawable(Style.Drawables.button, Style.COLOR_UP);
        imageTextButtonStyle.over = skin.newDrawable(Style.Drawables.button, Style.COLOR_OVER);
        imageTextButtonStyle.down = skin.newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        imageTextButtonStyle.checked = null;
        imageTextButtonStyle.fontColor = Style.FONT_COLOR;
        getSkin().add(Style.DEFAULT, imageTextButtonStyle, ImageTextButton.ImageTextButtonStyle.class);


        final TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = getSkin().getFont(Style.DEFAULT_FONT);
        textButtonStyle.up = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_UP);
        textButtonStyle.over = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_OVER);
        textButtonStyle.down = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        textButtonStyle.checked = null;
        textButtonStyle.fontColor = Style.FONT_COLOR;
        getSkin().add(Style.DEFAULT, textButtonStyle, TextButton.TextButtonStyle.class);

        final TextButton.TextButtonStyle toggleStyle = new TextButton.TextButtonStyle();
        toggleStyle.font = getSkin().getFont(Style.DEFAULT_FONT);
        toggleStyle.up = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_UP);
        toggleStyle.over = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_OVER);
        toggleStyle.down = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        toggleStyle.checked = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        toggleStyle.fontColor = Style.FONT_COLOR;
        getSkin().add(Style.TOGGLE, toggleStyle, TextButton.TextButtonStyle.class);

        final TextButton.TextButtonStyle listBtnStyle = new TextButton.TextButtonStyle();
        listBtnStyle.font = getSkin().getFont(Style.DEFAULT_FONT);
        listBtnStyle.up = getSkin().newDrawable(Style.Drawables.button, new Color(0, 0, 0, 0.84706f));
        listBtnStyle.over = getSkin().newDrawable(Style.Drawables.button, new Color(0.15f, 0.15f, 0.15f, 0.84706f));
        listBtnStyle.down = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        listBtnStyle.checked = null;
        listBtnStyle.fontColor = Style.FONT_COLOR;
        getSkin().add(Style.LIST_ITEM, listBtnStyle, TextButton.TextButtonStyle.class);
    }

    private void addCheckBoxStyle() {
        final CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.checkboxOn = getSkin().newDrawable(Style.Drawables.checkbox_on, Style.COLOR_ACCENT);
        checkBoxStyle.checkboxOff = getSkin().newDrawable(Style.Drawables.checkbox_off, Style.COLOR_ACCENT);
        checkBoxStyle.font = getSkin().getFont(Style.DEFAULT_FONT);
        checkBoxStyle.fontColor = Style.FONT_COLOR;
        getSkin().add(Style.DEFAULT, checkBoxStyle, CheckBox.CheckBoxStyle.class);
    }

    private void addLabelStyle() {
        getSkin().add(Style.DEFAULT, new Label.LabelStyle(getSkin().getFont(Style.DEFAULT_FONT), Style.FONT_COLOR), Label.LabelStyle.class);
    }

    public Skin getSkin() {
        return skin;
    }

    public void switchToExportScreen() {
        if (modelingScreen != null) {
//            setScreen(new ExportScreen(this, csg, modelingScreen.getProjectName(), (projectName, fileType, transform) -> exportFile(csg, projectName, fileType, transform)));
        }
    }

    public List<File> getProjectFileList() {
        final Context context = GdxVr.app.getActivityWeakReference().get();
        if (context != null)
            return Arrays.stream(context.getFilesDir().listFiles())
                    .filter(file -> file.getName().endsWith(Constants.FILE_TYPE_PROJECT))
                    .sorted(((o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified())))
                    .collect(Collectors.toList());
        else
            return new ArrayList<>();
    }

    @Nullable
    public ModelInstance getRoomInstance() {
        return roomInstance;
    }

    public void onExportComplete() {
        final VrScreen screen = getScreen();
        if (screen instanceof ExportScreen)
            ((ExportScreen) screen).onExportComplete();
    }

    public interface OnControllerBackPressedListener {
        void onControllerBackButtonClicked();
    }
}
