package com.neatier.shell.exception;

import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action1;
import trikita.log.Log;

/**
 * Created by László Gálosi on 12/10/16
 */
public class RxLogger {

    private static boolean sCrashOnCall;

    public static void setCrashOnCall(final boolean crashOnCall) {
        sCrashOnCall = crashOnCall;
    }

    public static Action1<Throwable> logRxError() {
        final Throwable checkpoint = new Throwable();
        return throwable -> {
            StackTraceElement[] stackTrace = checkpoint.getStackTrace();
            StackTraceElement element = stackTrace[1]; // First element after `crashOnError()`
            String msg = String.format("onError() crash from subscribe() in %s.%s(%s:%s)",
                                       element.getClassName(),
                                       element.getMethodName(),
                                       element.getFileName(),
                                       element.getLineNumber());

            if (sCrashOnCall) {
                throw new OnErrorNotImplementedException(msg, throwable);
            } else {
                Log.e("logRxError", new OnErrorNotImplementedException(msg, throwable));
            }
        };
    }
}

