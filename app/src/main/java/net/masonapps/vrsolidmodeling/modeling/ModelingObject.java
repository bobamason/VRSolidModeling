package net.masonapps.vrsolidmodeling.modeling;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.utils.Array;

import net.masonapps.vrsolidmodeling.io.JsonUtils;
import net.masonapps.vrsolidmodeling.modeling.primitives.Primitive;

import org.json.JSONException;
import org.json.JSONObject;
import org.masonapps.libgdxgooglevr.gfx.Transformable;

import java.util.HashMap;

/**
 * Created by Bob Mason on 1/16/2018.
 */

public class ModelingObject extends Transformable {
    public static final String KEY_PRIMITIVE = "primitive";
    public static final String KEY_POSITION = "position";
    public static final String KEY_ROTATION = "rotation";
    public static final String KEY_SCALE = "scale";
    public static final String KEY_AMBIENT = "ambient";
    public static final String KEY_DIFFUSE = "diffuse";
    public static final String KEY_SPECULAR = "specular";
    public static final String KEY_SHININESS = "shininess";
    private final Primitive primitive;
    public Color ambientColor = new Color(Color.BLACK);
    public Color diffuseColor = new Color(Color.GRAY);
    public Color specularColor = new Color(Color.DARK_GRAY);
    public float shininess = 16f;

    public ModelingObject(Primitive primitive) {
        this.primitive = primitive;
    }

    public static ModelingObject fromJSONObject(HashMap<String, Primitive> primitiveMap, JSONObject jsonObject) throws JSONException {
        Primitive primitive = primitiveMap.get(jsonObject.getString(KEY_PRIMITIVE));
        final Color ambient = Color.valueOf(jsonObject.getString(KEY_AMBIENT));
        final Color diffuse = Color.valueOf(jsonObject.getString(KEY_DIFFUSE));
        final Color specular = Color.valueOf(jsonObject.getString(KEY_SPECULAR));
        final float shininess = (float) jsonObject.getDouble(KEY_SHININESS);
        final ModelingObject modelingObject = new ModelingObject(primitive);
        modelingObject.ambientColor.set(ambient);
        modelingObject.diffuseColor.set(diffuse);
        modelingObject.specularColor.set(specular);
        modelingObject.shininess = shininess;
        modelingObject.position.fromString(jsonObject.getString(KEY_POSITION));
        modelingObject.rotation.set(JsonUtils.quaternionFromString(jsonObject.getString(KEY_ROTATION)));
        modelingObject.scale.fromString(jsonObject.getString(KEY_SCALE));
        modelingObject.recalculateTransform();
        return modelingObject;
    }

    public Primitive getPrimitive() {
        return primitive;
    }

    public JSONObject toJSONObject() throws JSONException {
        if (!isUpdated()) recalculateTransform();
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_PRIMITIVE, primitive.getName());
        jsonObject.put(KEY_AMBIENT, ambientColor.toString());
        jsonObject.put(KEY_DIFFUSE, diffuseColor.toString());
        jsonObject.put(KEY_SPECULAR, specularColor.toString());
        jsonObject.put(KEY_SHININESS, shininess);
        jsonObject.put(KEY_POSITION, position.toString());
        jsonObject.put(KEY_ROTATION, JsonUtils.quaternionToString(rotation));
        jsonObject.put(KEY_SCALE, position.toString());
        return jsonObject;
    }

    public Material createMaterial() {
        return new Material(ColorAttribute.createAmbient(ambientColor),
                ColorAttribute.createDiffuse(diffuseColor),
                ColorAttribute.createSpecular(specularColor),
                FloatAttribute.createShininess(shininess));
    }

    public ModelInstance createModelInstance(HashMap<String, Model> modelMap) {
        if (!updated)
            recalculateTransform();
        final ModelInstance modelInstance = new ModelInstance(modelMap.get(primitive.getName()), transform.cpy());
        final Material material = createMaterial();
        modelInstance.materials.get(0).set(material.get(new Array<>(), material.getMask()));
        return modelInstance;
    }
}
