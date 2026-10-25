package com.android.settings.notification;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.android.settings.R;
import com.android.settings.RingtonePreference;

public class PetalSoundPreference extends RingtonePreference {
    public PetalSoundPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRingtoneType(RingtoneManager.TYPE_NOTIFICATION);
        setShowDefault(true);
        setShowSilent(true);
        setPersistent(false);
    }

    @Override
    public void onAttached() {
        super.onAttached();
        refreshSummary();
    }

    @Override
    protected void onSaveRingtone(Uri uri) {
        // picker spits the stock uri back when default is picked, so null it to get the old tone
        String value;
        if (uri == null) {
            value = "";
        } else if (RingtoneManager.isDefault(uri)) {
            value = null;
        } else {
            value = uri.toString();
        }
        Settings.System.putString(getContext().getContentResolver(), getKey(), value);
        refreshSummary();
    }

    @Override
    protected Uri onRestoreRingtone() {
        String sound = Settings.System.getString(getContext().getContentResolver(), getKey());
        // nothing saved means default, hand the picker a uri so it ticks that row
        if (sound == null) {
            return RingtoneManager.getDefaultUri(getRingtoneType());
        }
        return TextUtils.isEmpty(sound) ? null : Uri.parse(sound);
    }

    public void refreshSummary() {
        String sound = Settings.System.getString(getContext().getContentResolver(), getKey());
        if (sound == null) {
            setSummary(R.string.petal_sound_default);
        } else if (sound.isEmpty()) {
            setSummary(R.string.petal_sound_silent);
        } else {
            Ringtone ringtone = RingtoneManager.getRingtone(getContext(), Uri.parse(sound));
            setSummary(ringtone == null ? sound : ringtone.getTitle(getContext()));
        }
    }
}
