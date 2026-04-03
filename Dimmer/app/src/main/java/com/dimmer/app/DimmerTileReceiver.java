package com.dimmer.app;

import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class DimmerTileReceiver extends TileService {

    @Override
    public void onTileRemoved() {}

    @Override
    public void onClick() {
        Tile tile = getQsTile();
        if (tile == null) return;
        if (tile.getState() == Tile.STATE_ACTIVE) {
            stopService(new Intent(this, DimmerService.class));
            tile.setState(Tile.STATE_INACTIVE);
        } else {
            startForegroundService(new Intent(this, DimmerService.class));
            tile.setState(Tile.STATE_ACTIVE);
        }
    }
}
