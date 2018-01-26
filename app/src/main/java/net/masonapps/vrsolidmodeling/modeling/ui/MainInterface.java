package net.masonapps.vrsolidmodeling.modeling.ui;

import android.content.SharedPreferences;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Pools;

import net.masonapps.vrsolidmodeling.R;
import net.masonapps.vrsolidmodeling.Style;
import net.masonapps.vrsolidmodeling.modeling.primitives.Primitives;
import net.masonapps.vrsolidmodeling.ui.ColorPickerSimple;
import net.masonapps.vrsolidmodeling.ui.ConfirmDialog;
import net.masonapps.vrsolidmodeling.ui.ShapeSelector;
import net.masonapps.vrsolidmodeling.ui.VerticalImageTextButton;

import org.masonapps.libgdxgooglevr.math.CylindricalCoordinate;
import org.masonapps.libgdxgooglevr.ui.CylindricalWindowUiContainer;
import org.masonapps.libgdxgooglevr.ui.WindowTableVR;
import org.masonapps.libgdxgooglevr.ui.WindowVR;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Bob Mason on 12/20/2017.
 */

public class MainInterface extends CylindricalWindowUiContainer {

    public static final String WINDOW_BUTTON_BAR = "winBtnBar";
    public static final String WINDOW_COLOR_PICKER = "winColorPicker";
    public static final String WINDOW_BRUSH_SETTINGS = "winBrushSettings";
    public static final String WINDOW_VIEW_CONTROLS = "winViewControls";
    private static final float PADDING = 8f;
    private final Skin skin;
    private final UiEventListener eventListener;
    private final WindowTableVR buttonBar;
    private final ColorPickerSimple colorPicker;
    private final ConfirmDialog confirmDialog;
    private final ShapeSelector shapeSelector;
    private final ViewControls viewControls;

    public MainInterface(Batch spriteBatch, Skin skin, UiEventListener listener) {
        super(2f, 4f);
        this.skin = skin;
        this.eventListener = listener;
        final WindowVR.WindowVrStyle windowStyleWithClose = Style.createWindowVrStyle(skin);
        windowStyleWithClose.closeDrawable = skin.newDrawable(Style.Drawables.ic_close);
        buttonBar = new WindowTableVR(spriteBatch, skin, 560, 112, Style.createWindowVrStyle(skin));
        colorPicker = new ColorPickerSimple(spriteBatch, skin, 448, 448, Style.getStringResource(R.string.title_color_picker, "Color"), windowStyleWithClose);
        confirmDialog = new ConfirmDialog(spriteBatch, skin);
        shapeSelector = new ShapeSelector(spriteBatch, skin, createShapeItemList());
        viewControls = new ViewControls(spriteBatch, skin, windowStyleWithClose);
        initButtonBar();
        initColorTable();
        initConfirmDialog();
        initShapeSelector();
        initViewControls();
    }

    private List<ShapeSelector.ShapeItem> createShapeItemList() {
        return Arrays.asList(
                new ShapeSelector.ShapeItem(Primitives.KEY_CUBE, "Cube", Style.Drawables.ic_add),
                new ShapeSelector.ShapeItem(Primitives.KEY_SPHERE, "Sphere", Style.Drawables.circle));
    }

    private void initButtonBar() {
        final Table buttonBarTable = buttonBar.getTable();

        final VerticalImageTextButton undoBtn = new VerticalImageTextButton(Style.getStringResource(R.string.undo, "undo"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_undo));
        undoBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                eventListener.onUndoClicked();
            }
        });
        buttonBarTable.add(undoBtn).padTop(PADDING).padBottom(PADDING).padLeft(PADDING).padRight(PADDING);

        final VerticalImageTextButton redoBtn = new VerticalImageTextButton(Style.getStringResource(R.string.redo, "redo"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_redo));
        redoBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                eventListener.onRedoClicked();
            }
        });
        buttonBarTable.add(redoBtn).padTop(PADDING).padBottom(PADDING).padRight(PADDING);

        final VerticalImageTextButton addBtn = new VerticalImageTextButton("add", Style.createImageTextButtonStyle(skin, Style.Drawables.ic_add));
        addBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showShapeSelector((item) -> eventListener.onAddClicked(item.primitiveKey));
            }
        });
        buttonBarTable.add(addBtn).padTop(PADDING).padBottom(PADDING).padRight(PADDING);

        final VerticalImageTextButton colorBtn = new VerticalImageTextButton(Style.getStringResource(R.string.title_color_picker, "color"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_color));
        colorBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                colorPicker.setVisible(!colorPicker.isVisible());
            }
        });
        buttonBarTable.add(colorBtn).padTop(PADDING).padBottom(PADDING).padRight(PADDING);

        final VerticalImageTextButton viewBtn = new VerticalImageTextButton(Style.getStringResource(R.string.view, "view"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_rotate));
        viewBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                viewControls.setVisible(!viewControls.isVisible());
            }
        });
        buttonBarTable.add(viewBtn).padTop(PADDING).padBottom(PADDING).padRight(PADDING);

        final VerticalImageTextButton exportBtn = new VerticalImageTextButton(Style.getStringResource(R.string.export, "export"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_export));
        exportBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                eventListener.onExportClicked();
            }
        });
        buttonBarTable.add(exportBtn).padTop(PADDING).padBottom(PADDING).padRight(PADDING);

        addProcessor(buttonBar);
        buttonBar.resizeToFitTable();
        buttonBar.setPosition(new CylindricalCoordinate(getRadius(), 90f, -0.75f, CylindricalCoordinate.AngleMode.degrees).toCartesian());
        buttonBar.lookAt(new Vector3(0, buttonBar.getPosition().y, 0), Vector3.Y);
    }

    private void initConfirmDialog() {
        confirmDialog.setVisible(false);
        confirmDialog.setBackground(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));
        confirmDialog.setPosition(new CylindricalCoordinate(getRadius(), 90f, 0f, CylindricalCoordinate.AngleMode.degrees).toCartesian());
        addProcessor(confirmDialog);
    }

    private void showConfirmDialog(String msg, Consumer<Boolean> consumer) {
        confirmDialog.setMessage(msg);
        confirmDialog.setListener(consumer);
        confirmDialog.show();
    }

    private void initColorTable() {
        final CylindricalCoordinate coordinate = new CylindricalCoordinate(getRadius(), 50f, 0.35f, CylindricalCoordinate.AngleMode.degrees);
        colorPicker.setPosition(coordinate.toCartesian());
        colorPicker.lookAt(new Vector3(0, coordinate.vertical, 0), Vector3.Y);
        colorPicker.setVisible(false);
        colorPicker.setColorListener(eventListener::onColorChanged);
        addProcessor(colorPicker);
    }

    private void initShapeSelector() {
        shapeSelector.setVisible(false);
        shapeSelector.setBackground(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));
        shapeSelector.setPosition(new CylindricalCoordinate(getRadius(), 90f, 0f, CylindricalCoordinate.AngleMode.degrees).toCartesian());
        addProcessor(shapeSelector);
    }

    private void showShapeSelector(ShapeSelector.OnShapeItemClickedListener listener) {
        shapeSelector.setListener(listener);
        shapeSelector.show();
    }

    private void initViewControls() {
        final CylindricalCoordinate coordinate = new CylindricalCoordinate(getRadius(), 40f, -0.35f, CylindricalCoordinate.AngleMode.degrees);
        viewControls.setPosition(coordinate.toCartesian());
        viewControls.lookAt(new Vector3(0, coordinate.vertical, 0), Vector3.Y);
        viewControls.setVisible(false);
        addProcessor(viewControls);
    }

    @Override
    public void act() {
        super.act();
    }

    public void loadWindowPositions(SharedPreferences sharedPreferences) {
        final Vector3 tmp = Pools.obtain(Vector3.class);

        tmp.fromString(sharedPreferences.getString(WINDOW_BUTTON_BAR, buttonBar.getPosition().toString()));
        buttonBar.setPosition(tmp);
        snapDragTableToCylinder(buttonBar);

        tmp.fromString(sharedPreferences.getString(WINDOW_COLOR_PICKER, colorPicker.getPosition().toString()));
        colorPicker.setPosition(tmp);
        snapDragTableToCylinder(colorPicker);

        tmp.fromString(sharedPreferences.getString(WINDOW_VIEW_CONTROLS, viewControls.getPosition().toString()));
        viewControls.setPosition(tmp);
        snapDragTableToCylinder(viewControls);

        Pools.free(tmp);
    }

    public WindowTableVR getButtonBar() {
        return buttonBar;
    }

    public ColorPickerSimple getColorPicker() {
        return colorPicker;
    }

    public void saveWindowPositions(SharedPreferences.Editor editor) {
        editor.putString(WINDOW_BUTTON_BAR, buttonBar.getPosition().toString());
        editor.putString(WINDOW_COLOR_PICKER, colorPicker.getPosition().toString());
        editor.putString(WINDOW_VIEW_CONTROLS, viewControls.getPosition().toString());
    }

    public boolean onControllerBackButtonClicked() {
        if (confirmDialog.isVisible()) {
            confirmDialog.dismiss();
            return true;
        }
        return false;
    }

    public void setViewControlsListener(ViewControls.ViewControlListener listener) {
        viewControls.setListener(listener);
    }

    public interface UiEventListener {
        // TODO: 1/4/2018 remove test add method
        void onAddClicked(String key);

        void onColorChanged(Color color);
        
        void onUndoClicked();

        void onRedoClicked();

        void onExportClicked();
    }
}
