package net.michel.stattrack.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Server {
    private String name;
    private String fullName;
    private boolean online;
    private int players;
    private int maxPlayers;
    private int ping;
    private long lastUpdate;

    public void updateServer(boolean online, int players, int maxPlayers, int ping) {
        this.online = online;
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.ping = ping;
        this.lastUpdate = System.currentTimeMillis();
    }
}
