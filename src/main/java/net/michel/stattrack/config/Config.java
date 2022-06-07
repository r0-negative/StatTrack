package net.michel.stattrack.config;

import lombok.Getter;
import lombok.Setter;
import net.michel.stattrack.utils.StringUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

@Getter
@Setter
public class Config {
    private JSONObject json;
    private File configFile;
    private String secretKey;
    private int port;

    public void init() {
        try {
            configFile = new File("StatTrack/config.json");
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/config.json")), configFile.toPath());
            }

            InputStream inputStream = new FileInputStream(configFile);
            String jsonTxt = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            json = new JSONObject(jsonTxt);


            this.secretKey = json.getString("secretKey");
            if (this.secretKey.isEmpty()) {
                this.secretKey = StringUtils.generateRandomString(32);
                saveField("secretKey", this.secretKey);
            }

            this.port = json.getInt("port");
            if (this.port == 0) {
                this.port = 8080;
                saveField("port", this.port);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveField(String key, Object value) throws IOException {
        json.put(key, value);
        Files.write(configFile.toPath(), json.toString(4).getBytes());
    }
}
