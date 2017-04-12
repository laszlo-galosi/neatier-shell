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

package com.neatier.shell.camera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import com.neatier.commons.helpers.PhotoUtils;
import com.neatier.commons.interactors.CameraInteraction;
import com.neatier.shell.BuildConfig;
import com.neatier.shell.R;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.factorysettings.AppSettings;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;

/**
 * Created by LargerLife on 02/02/17.
 */

public class CameraIntentInteractor implements CameraInteraction {

    private Context context;

    public CameraIntentInteractor(final Context context) {
        this.context = context;
    }

    @Override public Observable<Intent> stillImageChooserIntent(final int requestCode, @Nullable
          Bundle callbackBundle) {
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                                       MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        final Intent chooserIntent = Intent.createChooser(pickIntent, context.getString(
              R.string.select_picture));

        //Intent captureIntent = new Intent(context, CameraActivity.class);
        //captureIntent.setAction(AppSettings.ACTION_CAMERA_CAPTURE);
        PackageManager pm = context.getPackageManager();
        List<Intent> pickIntents = new ArrayList<>();
        return stillImageCaptureIntent(requestCode)
              .flatMap(captureIntent -> {
                  chooserIntent.putExtra(EventParam.requestCode().name, requestCode);
                  if (callbackBundle != null) {
                      callbackBundle.putAll(captureIntent.getExtras());
                  }
                  pickIntents.add(captureIntent);
                  List<ResolveInfo> resolveInfos =
                        pm.queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY);
                  return Observable.from(resolveInfos)
                                   .flatMap(resolveInfo -> Observable.just(
                                         pm.getLaunchIntentForPackage(
                                               resolveInfo.activityInfo.packageName))
                                   )
                                   .filter(pi -> pi != null)
                                   .collect(() -> pickIntents, (list, pi) -> {
                                       pi.setAction(Intent.ACTION_PICK);
                                       list.add(pi);
                                   })
                                   .flatMap(intentList -> Observable.just(
                                         intentList.toArray(new Parcelable[] {}))
                                   );
              }).flatMap(intentArray -> {
                  chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                  return Observable.just(chooserIntent);
              });
    }

    @Override public Observable<Intent> stillImageCaptureIntent(final int requestCode) {

/*        final Intent captureIntent = new Intent(context, CameraActivity.class);
        captureIntent.setAction(getStillImageCaptureAction());
        captureIntent.addCategory(Intent.CATEGORY_DEFAULT);
        captureIntent.putExtra(EventParam.requestCode().name, requestCode);
        return captureIntent;*/
        final Intent takePictureIntent = new Intent(getStillImageCaptureAction());
        List<ResolveInfo> resInfoList =
              context.getPackageManager()
                     .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
        //Only need granting FileProvider uri permissions to API < 21.
        final Observable<String> uriPermissionObservable =
              Observable.from(resInfoList)
                        .filter(rinfo -> BuildConfig.VERSION_CODE < Build.VERSION_CODES.LOLLIPOP)
                        .flatMap(rinfo -> Observable.just(rinfo.activityInfo.packageName));
        return PhotoUtils.createImageFile(context)
                         .flatMap(imageFile -> {
                             takePictureIntent.putExtra(EventParam.requestCode().name, requestCode);
                             takePictureIntent.putExtra(PhotoUtils.EXTRA_OUTPUT_PATH,
                                                        imageFile.getAbsolutePath());
                             return Observable.just(
                                   PhotoUtils.getUriForFile(imageFile,
                                                            AppSettings.IMAGE_PROVIDER_NAME,
                                                            context));
                         }).flatMap(photoUri -> {
                  //Grant FileProvider uri permission if needed.
                  uriPermissionObservable.subscribe(packageName -> {
                      context.grantUriPermission(packageName, photoUri,
                                                 Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                                       | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                  });
                  takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                  setCameraFace(requestCode, takePictureIntent);
                  return Observable.just(takePictureIntent);
              });
    }

    @Override public String getStillImageCaptureAction() {
        return MediaStore.ACTION_IMAGE_CAPTURE;
    }

    @Override public Intent videoImageCaptureIntent(final int requestCode) {
        //Todo this
        return null;
    }

    @Override public String getVideoImageCaptureAction() {
        return MediaStore.ACTION_VIDEO_CAPTURE;
    }

    @Override public Observable<Intent> videoCaptureChooserIntent(int requestCode) {
        final Intent takeVideoIntent = new Intent(getVideoImageCaptureAction());
        List<ResolveInfo> resInfoList =
              context.getPackageManager()
                     .queryIntentActivities(takeVideoIntent, PackageManager.MATCH_DEFAULT_ONLY);
        //Only need granting FileProvider uri permissions to API < 21.
        final Observable<String> uriPermissionObservable =
              Observable.from(resInfoList)
                        .filter(rinfo -> BuildConfig.VERSION_CODE < Build.VERSION_CODES.LOLLIPOP)
                        .flatMap(rinfo -> Observable.just(rinfo.activityInfo.packageName));
        return PhotoUtils.createImageFile(context)
                         .flatMap(imageFile -> {
                             takeVideoIntent.putExtra(EventParam.requestCode().name, requestCode);
                             takeVideoIntent.putExtra(PhotoUtils.EXTRA_OUTPUT_PATH,
                                                      imageFile.getAbsolutePath());
                             return Observable.just(
                                   PhotoUtils.getUriForFile(imageFile,
                                                            AppSettings.IMAGE_PROVIDER_NAME,
                                                            context));
                         }).flatMap(photoUri -> {
                  //Grant FileProvider uri permission if needed.
                  uriPermissionObservable.subscribe(packageName -> {
                      context.grantUriPermission(packageName, photoUri,
                                                 Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                                       | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                  });
                  takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                  setCameraFace(requestCode, takeVideoIntent);
                  return Observable.just(takeVideoIntent);
              });
    }

    @Override public void clearLeakables() {

    }

    private void setCameraFace(final int requestCode, final Intent intent) {
        switch (requestCode) {
            default:
                intent.putExtra("android.intent.extras.CAMERA_FACING",
                                Camera.CameraInfo.CAMERA_FACING_BACK);
        }
    }
}
