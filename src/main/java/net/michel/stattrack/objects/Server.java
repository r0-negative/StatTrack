package net.michel.stattrack.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
public class Server {
    private final ArrayList<GraphData> players = new ArrayList<>();
    private String name;
    private String fullName;
    private boolean online;
    private int maxPlayers;
    private int ping;
    private long lastUpdate;

    public void updateServer(boolean online, int maxPlayers, int ping) {
        this.online = online;
        this.maxPlayers = maxPlayers;
        this.ping = ping;
        this.lastUpdate = System.currentTimeMillis();
    }

    public void updatePlayers(int players) {
        this.players.add(new GraphData(System.currentTimeMillis(), players));

        if (this.players.size() > 10) {
            this.players.subList(0, this.players.size() - 10).clear();
        }
    }
}
