package org.galaxy.beyond.api.pack;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public class IdentifierTypeAdapter extends TypeAdapter<ResourceLocation> {

    @Override
    public void write(JsonWriter out, ResourceLocation value) throws IOException {
        out.value(value.toString());
    }

    @Override
    public ResourceLocation read(JsonReader in) throws IOException {
        return ResourceLocation.parse(in.nextString());
    }
}
