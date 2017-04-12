/*
 * Copyright (C) 2017 Extremenet Ltd., All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  Proprietary and confidential.
 *  All information contained herein is, and remains the property of Extremenet Ltd.
 *  The intellectual and technical concepts contained herein are proprietary to Extremenet Ltd.
 *   and may be covered by U.S. and Foreign Patents, pending patents, and are protected
 *  by trade secret or copyright law. Dissemination of this information or reproduction of
 *  this material is strictly forbidden unless prior written permission is obtained from
 *   Extremenet Ltd.
 *
 */

package com.neatier.shell.appframework.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import com.fernandocejas.arrow.optional.Optional;
import com.google.common.collect.Lists;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.commons.helpers.RxUtils;
import com.neatier.commons.helpers.SharedKeyValueStore;
import com.neatier.shell.R;
import com.neatier.shell.appframework.SnackbarDisplayable;
import com.neatier.shell.eventbus.Event;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.exception.RxLogger;
import com.neatier.shell.factorysettings.AppSettings;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.functions.Action1;
import trikita.log.Log;

import static android.os.Build.VERSION_CODES.M;
import static com.neatier.shell.factorysettings.AppSettings.PREFKEY_PERMISION_DENIED_COUNT;
import static rx.Observable.from;

/**
 * Created by LargerLife on 02/02/17.
 */

public class PermissionInteractor implements PermissionInteraction {

    private final Activity activity;
    private final SharedKeyValueStore<String, Object> sharedKeyValueStore;
    private final DialogMaker dialogMaker;

    private SparseArray<Intent> mPermissionIntentMap = new SparseArray<>(5);
    private KeyValuePairs<String, Integer> mPermissionRequestCodes = new KeyValuePairs<>(5);

    @Inject public PermissionInteractor(
          final SharedKeyValueStore<String, Object> sharedKeyValueStore,
          final Activity activity) {
        this.activity = activity;
        this.sharedKeyValueStore = sharedKeyValueStore;
        this.dialogMaker = DialogMaker.getInstance();
    }

    @Override public void handlePermissionEvent(final EventBuilder event) {
        final Event eventType = event.getEventType();
        if (eventType.id == Event.EVT_REQUIRE_PERMISSION) {
            event.logReceived(getClass().getSimpleName(), "handlePermissionEvent").v(event);
            final String intentAction =
                  event.getCheckedParam(EventParam.PRM_ACTION, String.class).get();
            Optional<?> permissionOpt =
                  Optional.fromNullable(event.getBundle().get(EventParam.value().name));
            final Optional<Parcelable> extraOpt =
                  event.getParamAs(EventParam.PRM_INTENT_EXTRAS, Parcelable.class);
            final Boolean forceRequest =
                  event.getParamAs(EventParam.PRM_FORCE_REQUEST, Boolean.class, Boolean.FALSE)
                       .get();
            Intent intent = new Intent(activity, activity.getClass());
            intent.setAction(intentAction);
            final int requestCode =
                  event.getCheckedParam(EventParam.PRM_REQUEST_CODE, Integer.class).get();
            intent.putExtra(EventParam.requestCode().name, requestCode);
            if (extraOpt.isPresent()) {
                intent.putExtras(((BundleWrapper) extraOpt.get()).getBundle());
            }
            if (!permissionOpt.isPresent()) {
                permissionOpt = Optional.of(getPermissionsByRequestCode(requestCode));
            }
            if (Build.VERSION.SDK_INT >= 21) {
                Object permObject = permissionOpt.get();
                if (permObject instanceof String) {
                    if (forceRequest) {
                        clearDenialOf((String) permObject);
                    }
                    grantPermissionForIntent(intent, requestCode, new String[] {
                          (String) permObject });
                } else if (permObject instanceof String[]) {
                    if (forceRequest) {
                        clearDenialOf((String[]) permObject);
                    }
                    grantPermissionForIntent(intent, requestCode, (String[]) permObject);
                }
            } else {
                //activity.startActivityForResult(intent, requestCode);
                mPermissionIntentMap.put(requestCode, intent);
                onPermissionResult(requestCode, PackageManager.PERMISSION_GRANTED);
            }
        }
    }

    private void clearDenialOf(final String... permissions) {
        final String permSetKey = getPermissionSetKey(permissions);
        final String denialPrefKey =
              String.format("%s_%s", PREFKEY_PERMISION_DENIED_COUNT, permSetKey);
        sharedKeyValueStore.put(denialPrefKey, 0).commit();
    }

    @Override
    public void grantPermissionForIntent(final Intent intentToStart, final int requestCode,
          final String... permissions) {
        Log.w("grantPermissionForIntent", requestCode, intentToStart, Arrays.toString(permissions));
        List<String> permissionList = Lists.newArrayList();
        String permSetKey = Observable
              .from(permissions)
              .switchIfEmpty(from(getPermissionsByRequestCode(requestCode)))
              .collect(() -> permissionList, (list, p) -> list.add(p))
              .flatMap(permList -> Observable.from(permList))
              .reduce("", (a, b) -> TextUtils.join(
                    TextUtils.isEmpty(a) ? "" : ",", new String[] { a, b }))
              .toBlocking().first();
        //.collect(() -> permissionList, (list, p) -> list.add(p))
        //.subscribe();
        //final String permSetKey = getPermissionSetKey(permissionList.toArray(new String[] {}));
        final String denialPrefKey =
              String.format("%s_%s", PREFKEY_PERMISION_DENIED_COUNT, permSetKey);
        final int deniedCount = sharedKeyValueStore.getAsOrDefault(denialPrefKey, Integer.class, 0);
        Log.v("grantPermissionForIntent", permSetKey, denialPrefKey, deniedCount);
        mPermissionRequestCodes.put(permSetKey, requestCode);
        //intentToStart.putExtra(EventParam.requestCode().name, requestCode);
        //save the intent for later execution.
        mPermissionIntentMap.put(requestCode, intentToStart);
        from(permissionList)
              .switchIfEmpty(from(getPermissionsByRequestCode(requestCode)))
              .filter(permission -> ActivityCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED)
              .toList()
              .subscribe(new RxUtils.SubscriberAdapter<List<String>>() {
                  @Override public void onError(final Throwable e) {
                      super.onError(e);
                      RxLogger.logRxError().call(e);
                  }

                  @Override public void onNext(final List<String> permList) {
                      super.onNext(permList);
                      Log.d("onNext", permSetKey);
                      if (permList.isEmpty()) {
                          //All permission has been granted execute the intent.
                          //activity.startActivityForResult(intentToStart, requestCode);
                          mPermissionRequestCodes.remove(permSetKey);
                          onPermissionResult(requestCode, PackageManager.PERMISSION_GRANTED);
                          return;
                      }
                      if (deniedCount == 0) {
                          //Request the missing permissions.
                          ActivityCompat.requestPermissions(
                                activity,
                                permList.toArray(new String[] {}),
                                requestCode
                          );
                      } else {
                          explainPermissionRequest(
                                permissions,
                                v -> onPermissionResult(requestCode,
                                                        PackageManager.PERMISSION_DENIED)
                          );
                      }
                  }
              });
    }

    @Override public void onRequestPermissionsResult(final int requestCode,
          final String[] permissions, final int[] grantResults) {
        //Checks whether how many times the user denied the permission so we should not bother
        //again with an alert dialog.
        final String permSetKey = getPermissionSetKey(getPermissionsByRequestCode(requestCode));
        final String denialKey = String.format("%s_%s", PREFKEY_PERMISION_DENIED_COUNT, permSetKey);
        final int deniedCount = sharedKeyValueStore.getAsOrDefault(denialKey, Integer.class, 0);

        Observable.range(0, permissions.length)
                  .flatMap(i -> Observable.just(new Pair<>(permissions[i], grantResults[i])))
                  .subscribe(new RxUtils.SubscriberAdapter<Pair<String, Integer>>() {
                      @Override public void onCompleted() {
                          super.onCompleted();
                          int deniedNum =
                                sharedKeyValueStore.getAsOrDefault(denialKey, Integer.class, 0);
                          if (deniedNum > 0) {
                              if (deniedNum >= AppSettings.MAX_PERMISSION_DENIED_LIMIT) {
                                  //send the negative result back.
                                  onPermissionResult(requestCode, PackageManager.PERMISSION_DENIED);
                                  return;
                              }
                              //else explain th why we need this permission.
                              explainPermissionRequest(
                                    permissions,
                                    v -> onPermissionResult(requestCode,
                                                            PackageManager.PERMISSION_DENIED)
                              );
                          } else {
                              //Send any pending intent for the requested permission.
                              onPermissionResult(requestCode, PackageManager.PERMISSION_GRANTED);
                          }
                      }

                      @Override public void onNext(final Pair<String, Integer> pair) {
                          Log.d("onRequestPermissionsResult", pair.first, pair.second);
                          if (pair.second != PackageManager.PERMISSION_GRANTED) {
                              //increase denial count and show explanation;
                              sharedKeyValueStore.putInt(
                                    denialKey,
                                    Math.min(AppSettings.MAX_PERMISSION_DENIED_LIMIT,
                                             deniedCount + 1)
                              ).commit();
                          } else {
                              //Remove denialCount from preferences to restart the permission
                              // request flow if the user revoke permissions later.
                              sharedKeyValueStore.remove(denialKey).commit();
                          }
                      }
                  });
    }

    @Override public boolean shouldRequestPermission(final String... permissions) {
        final String denialKey = String.format("%s_%s", PREFKEY_PERMISION_DENIED_COUNT,
                                               getPermissionSetKey(permissions));
        final int deniedCount = sharedKeyValueStore.getAsOrDefault(denialKey, Integer.class, 0);
        if (deniedCount >= AppSettings.MAX_PERMISSION_DENIED_LIMIT) {
            return false;
        }
        return from(permissions)
              .filter(perm -> ActivityCompat.checkSelfPermission(activity, perm)
                    != PackageManager.PERMISSION_GRANTED)
              .toList()
              .map(permList -> !permList.isEmpty()).toBlocking().first();
    }

    public static String[] getPermissionsByRequestCode(final Integer requestCode) {
        switch (requestCode) {
            case AppSettings.PERM_REQUEST_CODE_LOCATION:
            case AppSettings.PERM_REQUEST_CODE_READ_EXT_STORAGE:
                return new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
            case AppSettings.PERM_REQUEST_CODE_IMAGE_CHOOSER:
            case AppSettings.PERM_REQUEST_CODE_IMAGE_CAPTURE:
                return new String[] {
                      Manifest.permission.CAMERA,
                      Manifest.permission.READ_EXTERNAL_STORAGE,
                      Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
            case AppSettings.PERM_REQUEST_CODE_VIDEO_CAPTURE:
                return new String[] {
                      Manifest.permission.CAMERA,
                      Manifest.permission.RECORD_AUDIO,
                      Manifest.permission.READ_EXTERNAL_STORAGE,
                      Manifest.permission.WRITE_EXTERNAL_STORAGE,
                };
            case AppSettings.PERM_REQUEST_CODE_CALL:
            default:
                return new String[] { Manifest.permission.CALL_PHONE };
        }
    }

    private static String getPermissionSetKey(final String[] permissions) {
        return TextUtils.join(",", permissions);
    }

    private void onPermissionResult(final int requestCode, final int grantResult) {
        sendIntentForPermissionRequest(requestCode, grantResult,
                                       mPermissionIntentMap.get(requestCode));
        //Intent was served so remove from the map.
        mPermissionIntentMap.remove(requestCode);
    }

    @Override public void sendIntentForPermissionRequest(int requestCode, int grantResult,
          Intent intentForResult) {
        Log.d("sendIntentForPermissionRequest", requestCode, "result", grantResult);
        if (intentForResult != null) {
            Log.d(intentForResult).v("intent extras", intentForResult.getExtras());
            intentForResult.putExtra(EXTRA_RESULT_CODE, grantResult);
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intentForResult);
        }
    }

    /**
     * Displays a snackbar or an alert message based on the user previous denial count for the
     * specified
     * permission, and requestPermissions if the denialCount <= {@link
     * AppSettings#MAX_PERMISSION_DENIED_LIMIT}
     *
     * @param permissions permissions to request.
     */
    private void explainPermissionRequest(final String[] permissions,
          Action1<View>... negativeActions) {
        final String permSetKey = getPermissionSetKey(permissions);
        final int requestCode = mPermissionRequestCodes.getOrDefault(permSetKey, -1);
        final String denialKey =
              String.format("%s_%s", PREFKEY_PERMISION_DENIED_COUNT, permSetKey);
        final int deniedCount =
              sharedKeyValueStore.getAsOrDefault(denialKey, Integer.class, 0);
        boolean showPermissionRationale = shouldExplainPermissionRequest(permSetKey, deniedCount);
        String message = getPermissionExplanation(permSetKey, deniedCount);
        dialogMaker.title(R.string.alert_title_request_permission)
                   .setMessage(message)
                   .positiveAction(R.string.snackbar_action_next);
        if (showPermissionRationale) {
            dialogMaker.negativeAction(R.string.snackbar_action_defer)
                       .positiveActionClicked(v -> ActivityCompat.requestPermissions(
                             activity, permissions, requestCode))
                       .negativeActionClicked(v -> {
                           //If the user denies increment and store the denial count.
                           sharedKeyValueStore.putInt(denialKey, deniedCount + 1)
                                              .commit();
                           if (negativeActions.length > 0) {
                               from(negativeActions)
                                     .subscribe(action -> action.call(v));
                           }
                       });
            dialogMaker.makeAlert(activity);
        } else {
            //If the denial count is reached the limit, simply show a snackbar warning, on positive
            //action navigates to the application settings screen.
            final ComponentName activityCompName = new ComponentName(activity, activity.getClass());
            final String packageName = activityCompName.getPackageName();
            dialogMaker.positiveAction(R.string.snackbar_action_show_settings)
                       .positiveActionClicked(v -> {
                           try {
                               //Open the specific App Info page:
                               Intent intent = new Intent(
                                     android.provider.Settings
                                           .ACTION_APPLICATION_DETAILS_SETTINGS);
                               intent.setData(Uri.parse("package:" + packageName));
                               activity.startActivity(intent);
                           } catch (ActivityNotFoundException e) {
                               Log.w("Cannot open application settings", packageName,
                                     activityCompName);
                               //Open the generic Apps page:
                               Intent intent = new Intent(
                                     android.provider.Settings
                                           .ACTION_MANAGE_APPLICATIONS_SETTINGS);
                               activity.startActivity(intent);
                           }
                       })
                       .make(activity, ((SnackbarDisplayable) activity).getSnackbarAnchorView());
            from(negativeActions)
                  .subscribe(action -> action.call(null));
        }
    }

    @NonNull
    private String getPermissionExplanation(final String permSetKey, final int deniedCount) {
        String message = "";
        boolean showRationale = shouldExplainPermissionRequest(permSetKey, deniedCount);
        final int requestCode = mPermissionRequestCodes.getOrDefault(permSetKey, -1);
        switch (requestCode) {

            case AppSettings.PERM_REQUEST_CODE_LOCATION:
                message = activity.getString(showRationale
                                             ? R.string.message_location_permission_required
                                             : R.string.message_location_permission_not_granted);
                break;
            case AppSettings.PERM_REQUEST_CODE_CALL:
                message = activity.getString(showRationale
                                             ? R.string.message_call_permission_required
                                             : R.string.message_call_permission_not_granted);
                break;
            case AppSettings.PERM_REQUEST_CODE_IMAGE_CAPTURE:
                message = activity.getString(showRationale
                                             ? R.string.message_capture_image_permission_required
                                             : R.string
                                                   .message_camera_permission_not_granted);
                break;
            case AppSettings.PERM_REQUEST_CODE_VIDEO_CAPTURE:
                message = activity.getString(showRationale
                                             ? R.string.message_capture_video_permission_required
                                             : R.string
                                                   .message_camera_permission_not_granted);
                break;
        }
        return message;
    }

    private boolean shouldExplainPermissionRequest(final String permSetKey, int denialCount) {
        if (Build.VERSION.SDK_INT >= M && denialCount < AppSettings.MAX_PERMISSION_DENIED_LIMIT) {
            List<String> permExplains = Lists.newArrayList();
            from(TextUtils.split(permSetKey, ";"))
                  .subscribe(perm -> permExplains.add(perm));
            return !permExplains.isEmpty();
        }
        return denialCount < AppSettings.MAX_PERMISSION_DENIED_LIMIT;
    }
}
