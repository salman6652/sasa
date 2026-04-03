package com.dimmer.app;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class DimmerTileReceiver extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        Tile tile = getQsTile();
        if (tile.getState() == Tile.STATE_ACTIVE) {
            stopService(new Intent(this, DimmerService.class));
            tile.setState(Tile.STATE_INACTIVE);
        } else {
            startService(new Intent(this, DimmerService.class));
            tile.setState(Tile.STATE_ACTIVE);
        }
    }
}
