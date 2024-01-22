package it.silleellie.dndsync;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class DNDSyncListenerService extends WearableListenerService {
    private static final String TAG = "DNDSyncListenerService";
    private static final String DND_SYNC_MESSAGE_PATH = "/wear-dnd-sync";

    @Override
    public void onMessageReceived (@NonNull MessageEvent messageEvent) {

        if (messageEvent.getPath().equalsIgnoreCase(DND_SYNC_MESSAGE_PATH)) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Log.d(TAG, "received path: " + DND_SYNC_MESSAGE_PATH);

            byte[] data = messageEvent.getData();
            // data[0] contains dnd mode of phone
            // 0 = INTERRUPTION_FILTER_UNKNOWN
            // 1 = INTERRUPTION_FILTER_ALL (all notifications pass)
            // 2 = INTERRUPTION_FILTER_PRIORITY
            // 3 = INTERRUPTION_FILTER_NONE (no notification passes)
            // 4 = INTERRUPTION_FILTER_ALARMS
            // Custom
            // 5 = BedTime Mode On
            // 6 = BedTime Mode Off
            byte dndStatePhone = data[0];
            Log.d(TAG, "dndStatePhone: " + dndStatePhone);

            // get dnd state
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            int filterState = mNotificationManager.getCurrentInterruptionFilter();
            if (filterState < 0 || filterState > 4) {
                Log.d(TAG, "DNDSync weird current dnd state: " + filterState);
            }
            byte currentDndState = (byte) filterState;
            Log.d(TAG, "currentDndState: " + currentDndState);

            if ((0 <= dndStatePhone && dndStatePhone <= 4) && dndStatePhone != currentDndState) {

                Log.d(TAG, "dndStatePhone != currentDndState: " + dndStatePhone + " != " + currentDndState);

                if (mNotificationManager.isNotificationPolicyAccessGranted()) {
                    mNotificationManager.setInterruptionFilter(dndStatePhone);
                    Log.d(TAG, "DND set to " + dndStatePhone);
                } else {
                    Log.d(TAG, "attempting to set DND but access not granted");
                }

                boolean vibrate = prefs.getBoolean("vibrate_key", false);
                Log.d(TAG, "vibrate: " + vibrate);
                if (vibrate) {
                    vibrate();
                }

            } else if (dndStatePhone == 5 || dndStatePhone == 6) {

                boolean useBedtimeMode = prefs.getBoolean("bedtime_key", false);
                Log.d(TAG, "useBedtimeMode: " + useBedtimeMode);
                if (useBedtimeMode) {

                    int newSetting = (dndStatePhone == 5) ? 1 : 0;

                    boolean bedtimeModeSuccess = changeBedtimeSetting(newSetting);
                    if (bedtimeModeSuccess) {
                        Log.d(TAG, "Bedtime mode value toggled");
                    } else {
                        Log.d(TAG, "Bedtime mode toggle failed");
                    }

                    boolean usePowerSaverMode = prefs.getBoolean("power_saver_key", false);
                    if(usePowerSaverMode) {

                        boolean powerModeSuccess = changePowerModeSetting(newSetting);
                        if(powerModeSuccess) {
                            Log.d(TAG, "Power Saver mode toggled");
                        } else {
                            Log.d(TAG, "Power Saver mode toggle failed");
                        }
                    }
                }

                boolean vibrate = prefs.getBoolean("vibrate_key", false);
                Log.d(TAG, "vibrate: " + vibrate);
                if (vibrate) {
                    vibrate();
                }
            }

        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private boolean changeBedtimeSetting(int newSetting) {

        boolean bedtimeModeSuccess = Settings.Global.putInt(
                getApplicationContext().getContentResolver(), "setting_bedtime_mode_running_state", newSetting);
        boolean zenModeSuccess = Settings.Global.putInt(
                getApplicationContext().getContentResolver(), "zen_mode", newSetting);

        return bedtimeModeSuccess && zenModeSuccess;
    }

    private boolean changePowerModeSetting(int newSetting) {

        boolean lowPower = Settings.Global.putInt(
                getApplicationContext().getContentResolver(), "low_power", newSetting);
        boolean restrictedDevicePerformance = Settings.Global.putInt(
                getApplicationContext().getContentResolver(), "restricted_device_performance", newSetting);

        boolean lowPowerBackDataOff = Settings.Global.putInt(
                getApplicationContext().getContentResolver(), "low_power_back_data_off", newSetting);
        boolean smConnectivityDisable = Settings.Secure.putInt(
                getApplicationContext().getContentResolver(), "sm_connectivity_disable", newSetting);

        // screen timeout should be set to 10000 also, and ambient_tilt_to_wake should be set to 0
        // but previous variables in those 2 cases must be stored and they do not seem to stick
        // and they are not so much important tbh (ambient tilt to wake is disabled anyways)

        return lowPower && restrictedDevicePerformance
                && lowPowerBackDataOff && smConnectivityDisable;
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
    }

}
