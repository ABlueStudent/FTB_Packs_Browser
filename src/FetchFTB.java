import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

public class FetchFTB {
    // HTTP Get request mod pack list.
    private final OkHttpClient client = new OkHttpClient();

    private String fetchList() throws IOException {
        Request req = new Request.Builder()
                .url("https://api.modpacks.ch/public/modpack/popular/installs/FTB/all")
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:84.0) Gecko/20100101 Firefox/84.0") //加User-Agent免得被Ban
                .build();

        try (Response res = client.newCall(req).execute()) {
            return Objects.requireNonNull(res.body()).string();
        }
    }

    public JsonArray packList() throws IOException {
        String resBody = fetchList().replaceAll("\n", "").replace(" ", "");
        JsonObject res = JsonParser.parseString(resBody).getAsJsonObject();
        return res.get("packs").getAsJsonArray();
    }

    private String fetchPackInfo(int id) throws IOException {
        Request req = new Request.Builder()
                .url("https://api.modpacks.ch/public/modpack/" + id)
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:84.0) Gecko/20100101 Firefox/84.0")
                .build();

        try (Response res = client.newCall(req).execute()) {
            return Objects.requireNonNull(res.body()).string();
        }
    }

    public JsonObject packInfo(int id) throws IOException {
        String resBody = fetchPackInfo(id).replaceAll("\n", "");
        return JsonParser.parseString(resBody).getAsJsonObject();
    }
}
