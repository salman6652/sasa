package com.dimmer.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;

import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;

public class MainActivity extends Activity {

    private SwitchMaterial toggleSwitch;
    private Slider brightnessSlider;
    private MaterialTextView sliderValue;
    private static final int REQUEST_OVERLAY = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleSwitch = findViewById(R.id.toggleDimmer);
        brightnessSlider = findViewById(R.id.brightnessSlider);
        sliderValue = findViewById(R.id.sliderValue);

        // Prevent the activity itself from dimming while adjusting
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setDimAmount(0f);

        int currentAlpha = prefsGetInt(128);
        brightnessSlider.setValue(currentAlpha);
        updateAlphaText(currentAlpha);

        brightnessSlider.addOnChangeListener((slider, value, fromUser) -> {
            int alpha = (int) value;
            updateAlphaText(alpha);
            prefsPutInt(alpha);
            // If dimmer service is running, tell it to update
            DimmerService.updateAlpha(this, alpha);
        });

        toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Settings.canDrawOverlays(this)) {
                    startDimmer();
                } else {
                    toggleSwitch.setChecked(false);
                    requestOverlayPermission();
                }
            } else {
                stopDimmer();
            }
        });
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_OVERLAY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY && Settings.canDrawOverlays(this)) {
            toggleSwitch.setChecked(true);
            startDimmer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if the foreground service notification is visible
        // Simple heuristic: toggle stays as user last set it
    }

    private void startDimmer() {
        startForegroundService(new Intent(this, DimmerService.class));
    }

    private void stopDimmer() {
        stopService(new Intent(this, DimmerService.class));
    }

    private void prefsPutInt(int value) {
        getSharedPreferences("DimmerPrefs", MODE_PRIVATE).edit().putInt("alpha", value).apply();
    }

    private int prefsGetInt(int def) {
        return getSharedPreferences("DimmerPrefs", MODE_PRIVATE).getInt("alpha", def);
    }

    private void updateAlphaText(int value) {
        int pct = (int) (value / 2.55f);
        sliderValue.setText("Overlay: ~" + (100 - pct) + "% dim");
    }
}