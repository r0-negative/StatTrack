package net.michel.stattrack.api.v1;

import io.javalin.http.Context;
import net.michel.stattrack.StatTrack;
import net.michel.stattrack.objects.GraphData;
import net.michel.stattrack.objects.Server;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ServerApi {

    /**
     * Get a list of all servers
     *
     * @param ctx The context of the request.
     */
    public static void getServerList(Context ctx) {
        var servers = StatTrack.instance.getServers();

        if (servers.size() == 0) {
            responseError(ctx, "server list is empty");
            return;
        }

        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        StatTrack.instance.getServers().forEach(server -> array.put(server.getName()));
        json.put("servers", array);

        ctx.json(json.toString());
    }

    /**
     * Adds a server to the list of servers.
     *
     * @param ctx The context of the request.
     */
    public static void addServerToList(Context ctx) {
        if (checkKey(ctx)) {
            ctx.status(401);
            return;
        }

        String name = ctx.queryParamAsClass("name", String.class).getOrDefault(null);
        if (name == null || name.isEmpty()) {
            responseError(ctx, "name is required");
            return;
        }

        String fullName = ctx.queryParamAsClass("fullName", String.class).getOrDefault(null);
        if (fullName == null || fullName.isEmpty()) fullName = name;

        if (StatTrack.instance.serverExists(name)) {
            responseError(ctx, "server already exists");
            return;
        }

        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("name", name);

        var server = new Server(name, fullName, true, 0, 0, System.currentTimeMillis());


        Instant time = Instant.now().minus(300, ChronoUnit.MINUTES);
        for (int i = 10; i > 0; i--) {
            time = time.plus(30, ChronoUnit.MINUTES);
            server.getPlayers().add(new GraphData(time.toEpochMilli(), 0));
        }

        StatTrack.instance.getServers().add(server);
        ctx.result(json.toString());
    }

    /**
     * Updates the server status
     *
     * @param ctx The context of the request.
     */
    public static void serverUpdate(Context ctx) {
        if (checkKey(ctx)) {
            ctx.status(401);
            return;
        }

        String name = ctx.queryParamAsClass("name", String.class).getOrDefault(null);
        if (name == null || name.isEmpty()) {
            responseError(ctx, "name is required");
            return;
        }

        Boolean online = ctx.queryParamAsClass("online", Boolean.class).getOrDefault(false);
        Integer players = ctx.queryParamAsClass("players", Integer.class).getOrDefault(0);
        Integer maxPlayers = ctx.queryParamAsClass("maxPlayers", Integer.class).getOrDefault(0);
        Integer ping = ctx.queryParamAsClass("ping", Integer.class).getOrDefault(0);

        var server = StatTrack.instance.getServerByName(name);
        if (server == null) {
            responseError(ctx, "server does not exist");
            return;
        }

        server.updateServer(online, maxPlayers, ping);
        server.updatePlayers(players);

        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("name", name);
        ctx.result(json.toString());
    }

    /**
     * Returns information about a server.
     *
     * @param context The context of the request.
     */
    public static void serverInfo(Context context) {
        String name = context.queryParamAsClass("name", String.class).getOrDefault(null);
        if (name == null || name.isEmpty()) {
            responseError(context, "name is required");
            return;
        }

        var server = StatTrack.instance.getServerByName(name);
        if (server == null) {
            responseError(context, "server does not exist");
            return;
        }

        JSONObject json = new JSONObject();
        json.put("name", server.getName());
        json.put("fullName", server.getFullName());
        json.put("online", server.isOnline());
        json.put("players", server.getPlayers());
        json.put("maxPlayers", server.getMaxPlayers());
        json.put("ping", server.getPing());
        context.result(json.toString());
    }


    /**
     * Returns the server graph data as a json.
     * @param context The context of the request.
     */
    public static void serverGraph(Context context) {
        String name = context.queryParamAsClass("name", String.class).getOrDefault(null);
        if (name == null || name.isEmpty()) {
            responseError(context, "name is required");
            return;
        }

        var server = StatTrack.instance.getServerByName(name);
        if (server == null) {
            responseError(context, "server does not exist");
            return;
        }

        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("name", server.getName());

        JSONArray array = new JSONArray();
        server.getPlayers().forEach(player -> {
            JSONObject obj = new JSONObject();

            var time = new SimpleDateFormat("HH:mm").format(player.getKey());

            obj.put("time", time);
            obj.put("players", player.getValue());
            array.put(obj);
        });
        json.put("players", array);
        context.result(json.toString());
    }

    /**
     * Checks if the key is valid.
     *
     * @param ctx The context of the request.
     * @return True if the key is valid, false otherwise.
     */
    private static boolean checkKey(Context ctx) {
        String key = ctx.queryParamAsClass("key", String.class).getOrDefault(null);
        return key == null || !key.equals(StatTrack.instance.getConfig().getSecretKey());
    }

    /**
     * Sends an error response.
     *
     * @param ctx   The context of the request.`
     * @param error The error message.
     */
    private static void responseError(Context ctx, String error) {
        JSONObject json = new JSONObject();
        json.put("error", error);
        ctx.result(json.toString());
    }
}
