package net.masonapps.vrsolidmodeling.io;

import net.masonapps.vrsolidmodeling.modeling.ModelingObject;
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

    public static JSONArray toJSONArray(List<ModelingObject> objects) throws JSONException {
        final JSONArray jsonArray = new JSONArray();
        for (ModelingObject object : objects) {
            jsonArray.put(object.toJSONObject());
        }
        return jsonArray;
    }

    public static List<ModelingObject> fromJSONArray(JSONArray jsonArray, HashMap<String, Primitive> primitiveMap) throws JSONException {
        final ArrayList<ModelingObject> objects = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            objects.add(ModelingObject.fromJSONObject(primitiveMap, jsonArray.getJSONObject(i)));
        }
        return objects;
    }

    public static void saveFile(File file, List<ModelingObject> objects) throws IOException, JSONException {
        BufferedWriter writer = null;
        try {
            final JSONArray jsonArray = toJSONArray(objects);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(jsonArray.toString());
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    public static List<ModelingObject> loadFile(File file, HashMap<String, Primitive> primitiveMap) throws IOException, JSONException {
        BufferedReader reader = null;
        final ArrayList<ModelingObject> objects = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            final StringBuilder sb = new StringBuilder();
            final char[] buffer = new char[1024];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, count);
            }
            objects.addAll(fromJSONArray(new JSONArray(sb.toString()), primitiveMap));
        } finally {
            if (reader != null)
                reader.close();
        }
        return objects;
    }
}
