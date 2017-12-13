package com.isuwang.dapeng.metadata.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class GsonBooleanAdapter implements JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        int code = jsonElement.getAsInt();
        return code == 0 ? false : (code == 1 ? true : null);
    }
}
