package earth.terrarium.adastra.common.utils.radio;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.resourcefullib.common.utils.GsonHelpers;
import com.teamresourceful.resourcefullib.common.utils.WebUtils;
import earth.terrarium.adastra.AdAstra;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class StationLoader {

    private static final Codec<List<StationInfo>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        StationInfo.CODEC.listOf().fieldOf("stations").forGetter(Function.identity())
    ).apply(instance, Function.identity()));

    private static final List<StationInfo> STATIONS = new ArrayList<>();

    public static void init() {
        JsonObject object = readLocalStations();
        if (object == null) {
            object = WebUtils.getJson("https://rococo-piroshki-246107.netlify.app/stations.json");
        }

        if (object == null) return;
        STATIONS.clear();
        CODEC.parse(JsonOps.INSTANCE, object)
            .result()
            .ifPresent(STATIONS::addAll);
    }

    public static List<StationInfo> stations() {
        return STATIONS;
    }

    public static boolean hasStation(String url) {
        return STATIONS.stream().anyMatch(station -> station.url().equals(url));
    }

    @Nullable
    private static JsonObject readLocalStations() {
        String stationsFile = System.getProperty("adastra.stations");
        if (stationsFile != null) {
            String fileName = stationsFile.indexOf('/') == -1 ? stationsFile : stationsFile.substring(stationsFile.lastIndexOf('/') + 1);
            try {
                AdAstra.LOGGER.info("Loading stations from {}", fileName);
                return GsonHelpers.parseJson(Files.readString(Path.of(stationsFile))).orElse(null);
            } catch (Exception exception) {
                AdAstra.LOGGER.error("Failed loading stations from {}", fileName, exception);
                return null;
            }
        }
        return null;
    }
}
