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

package com.neatier.shell.data.network.retrofit;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import com.neatier.commons.exception.InternalErrorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import trikita.log.Log;

/**
 * Created by László Gálosi on 21/02/17
 */
public class UploadProgressRequestBody extends RequestBody {
    private static final String TAG = "UploadProgressRequestBody";
    private final File mFile;

    public interface ProgressCallback {
        public void onProgress(long progress, long total);
    }

    public static class UploadInfo {
        //Content uri for the file
        public Uri contentUri;

        // File size in bytes
        public long contentLength;
    }

    private WeakReference<Context> mContextRef;
    //private UploadInfo mUploadInfo;
    private ProgressCallback mListener;

    private static final int UPLOAD_PROGRESS_BUFFER_SIZE = 8192;

    public UploadProgressRequestBody(Context context, File file,
          ProgressCallback listener) {
        mContextRef = new WeakReference<>(context);
        //mUploadInfo = uploadInfo;
        mFile = file;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        // NOTE: We are posting the upload as binary data so we don't need the true mimeType
        return MediaType.parse("application/octet-stream");
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = contentLength();
        byte[] buffer = new byte[UPLOAD_PROGRESS_BUFFER_SIZE];
        InputStream in = in();
        long uploaded = 0;

        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                mListener.onProgress(uploaded, fileLength);

                uploaded += read;

                sink.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }

    /**
     * WARNING: You must override this function and return the file size or you will get errors
     */
    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    private InputStream in() throws IOException {
        InputStream stream = null;
        try {
            //stream = getContentResolver().openInputStream(mUploadInfo.contentUri);
            stream = new FileInputStream(mFile);
        } catch (Exception ex) {
            Log.e(new InternalErrorException("Error getting input stream for upload", ex));
        }
        return stream;
    }

    private ContentResolver getContentResolver() {
        if (mContextRef.get() != null) {
            return mContextRef.get().getContentResolver();
        }
        return null;
    }
}


