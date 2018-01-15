package net.masonapps.vrsolidmodeling.io;

import com.badlogic.gdx.graphics.g3d.model.data.ModelData;

import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;
import net.masonapps.vrsolidmodeling.modeling.primitives.Primitive;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bob Mason on 1/15/2018.
 */

public class ProjectFileIO {

    public static final String EXTENSION = "proj";

    public static JSONArray toJSONArray(List<ModelingEntity> entities) throws JSONException {
        final JSONArray jsonArray = new JSONArray();
        for (ModelingEntity entity : entities) {
            jsonArray.put(entity.toJSONObject());
        }
        return jsonArray;
    }

    public static List<ModelingEntity> fromJSONArray(JSONArray jsonArray, HashMap<String, Primitive> primitiveMap) throws JSONException {
        final ArrayList<ModelingEntity> entities = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            entities.add(ModelingEntity.fromJSONObject(primitiveMap, jsonArray.getJSONObject(i)));
        }
        return entities;
    }

    public static void saveFile(File file, List<ModelingEntity> entities) throws IOException, JSONException {
        BufferedWriter writer = null;
        try {
            final JSONArray jsonArray = toJSONArray(entities);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(jsonArray.toString());
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    public static List<ModelingEntity> loadFile(File file, HashMap<String, Primitive> primitiveMap) throws IOException, JSONException {
        BufferedReader reader = null;
        final ArrayList<ModelingEntity> entities = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            final StringBuilder sb = new StringBuilder();
            final char[] buffer = new char[1024];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, count);
            }
            entities.addAll(fromJSONArray(new JSONArray(sb.toString()), primitiveMap));
        } finally {
            if (reader != null)
                reader.close();
        }
        return entities;
    }

    public static ModelData toModelData(File file, HashMap<String, Primitive> primitiveMap) {
        return null;
    }
}
