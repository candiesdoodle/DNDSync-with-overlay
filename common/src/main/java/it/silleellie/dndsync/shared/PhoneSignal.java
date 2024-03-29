package it.silleellie.dndsync.shared;

import android.content.SharedPreferences;

import java.io.Serializable;

public class PhoneSignal implements Serializable {

    public boolean AANotif = false;
    // dndState and bedtimeState will be null if the signal to sent is not related
    // to those two states
    public Integer dndState = null;
    public Integer bedtimeState = null;
    public boolean powersavePref = false;
    public boolean vibratePref = false;
    public boolean powersaveAAPref = false;
    public Integer AAPowerState = null;
    public boolean AASyncPref = false;
    public PhoneSignal(Integer dndState, SharedPreferences prefs) {

        boolean dndAsBedtime = prefs.getBoolean("dnd_as_bedtime_key", false);
        this.AASyncPref = prefs.getBoolean("android_auto_sync_key", true);
        this.powersavePref = prefs.getBoolean("power_save_key", false);
        this.powersaveAAPref = prefs.getBoolean("power_save_with_AA_key", true);
        this.vibratePref = prefs.getBoolean("watch_vibrate_key", false);
        this.AANotif=prefs.getBoolean("android_auto_notif",false);

        // DnD disabled:
        // 0 = INTERRUPTION_FILTER_UNKNOWN
        // 1 = INTERRUPTION_FILTER_ALL

        // DnD enabled:
        // 2 = INTERRUPTION_FILTER_PRIORITY
        // 3 = INTERRUPTION_FILTER_NONE (no notification passes)
        // 4 = INTERRUPTION_FILTER_ALARMS

        // Custom
        // 5 = BEDTIME ON
        // 6 = BEDTIME OFF
        if (0 <= dndState && dndState <= 4) {

            this.dndState = dndState;
            this.powersavePref=false;
            if (dndAsBedtime && dndState > 1) {
                // dndState > 1 means that it's enabled
                this.bedtimeState = 1;
                this.powersavePref=true;
            } else if (dndAsBedtime) {
                // in this branch dndState < = 1, so it's disabled
                this.bedtimeState = 0;
                this.powersavePref=true;
            }

            if (this.AANotif && this.AASyncPref && this.powersaveAAPref && dndState==1)  {
                this.AAPowerState = 0;
            } else if (this.AANotif && this.AASyncPref && this.powersaveAAPref && dndState==2) {
                this.AAPowerState = 1; //enable powersave on dnd state > 1
            }

        } else if (dndState == 5 || dndState == 6) {

            // dndState == 5 means bedtime on, dndState == 6 means bedtime off
            this.bedtimeState = dndState == 5 ? 1 : 0;
        }
    }
}
