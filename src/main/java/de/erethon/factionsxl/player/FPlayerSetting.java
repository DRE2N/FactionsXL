package de.erethon.factionsxl.player;

public enum FPlayerSetting {

    ANTHEMS,
    CHAT_SPY,
    PUBLIC_CHAT,
    REGION_CHANGE_MESSAGE,
    SCOREBOARD;

    boolean playerValue;

    public boolean get() {
        return this.playerValue;
    }

    public void set(boolean value) {
        this.playerValue = value;
    }

}
