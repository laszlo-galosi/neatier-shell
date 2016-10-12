package com.neatier.shell.notification;

import android.app.IntentService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.neatier.shell.eventbus.Event;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.eventbus.Item;
import trikita.log.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class InstanceIDListenerService extends /*IntentService*/FirebaseInstanceIdService {

    public static final String TAG = "InstanceIDListenerService";
    public static final String ACTION_INSTANCE_ID = "com.google.firebase.INSTANCE_ID_EVENT";

    public InstanceIDListenerService() {
        super();
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Sends an event with the changed token.
     */
    private void sendRegistrationToServer(String token) {
        EventBuilder.withItemAndType(
              Item.PUSH_NOTIFICATION, Event.EVT_SEND)
                    .addParam(EventParam.PRM_VALUE, token)
                    .send();
    }
}
