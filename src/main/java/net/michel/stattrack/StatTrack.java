package net.michel.stattrack;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.http.util.RateLimiter;
import lombok.Getter;
import lombok.Setter;
import net.michel.stattrack.api.v1.ServerApi;
import net.michel.stattrack.config.Config;
import net.michel.stattrack.objects.Server;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

@Getter
@Setter
public class StatTrack {
    public static StatTrack instance;
    private Config config;
    private Javalin javalin;
    private final ArrayList<Server> servers = new ArrayList<>();

    public void init() {
        instance = this;
        this.config = new Config();
        this.javalin = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH));
    }

    public void start() {
        System.out.println("Loading config...");
        config.init();

        System.out.println("Starting server...");

        var rateLimit = new RateLimiter(TimeUnit.MINUTES);

        javalin.before(ctx -> {
            if (ctx.path().startsWith("/api")) rateLimit.incrementCounter(ctx, 100);
        });

        javalin.routes(() -> path("/api/v1", () -> {
            get("serverlist", ServerApi::getServerList);
            get("addserver", ServerApi::addServerToList);
            get("updateserver", ServerApi::serverUpdate);
            get("serverinfo", ServerApi::serverInfo);
            get("servergraph", ServerApi::serverGraph);
        }));

        javalin.start(config.getPort());

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> servers.stream()
                .filter(server -> System.currentTimeMillis() - server.getLastUpdate() > TimeUnit.MINUTES.toMillis(10))
                .forEach(server -> {
                    server.setOnline(false);
                    //todo: send a webhook to discord
                }), 5, 15, TimeUnit.MINUTES);
    }


    public boolean serverExists(String name) {
        return servers.stream().anyMatch(server -> server.getName().equals(name));
    }

    public Server getServerByName(String name) {
        return servers.stream()
                .filter(server -> server.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
