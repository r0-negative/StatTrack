package net.michel.stattrack.main;

import net.michel.stattrack.StatTrack;

public class Bootstrap {

    public static void main(String[] args) {
        StatTrack statTrack = new StatTrack();
        statTrack.init();
        statTrack.start();
    }

}
