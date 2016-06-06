package com.neatier.shell.internal.di;

/**
 * Created by László Gálosi on 06/06/16
 */

import java.lang.annotation.Retention;
import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A scoping annotation to permit objects whose lifetime should
 * conform to the life of the fragment to be memorized in the
 * correct component.
 */
@Scope
@Retention(RUNTIME)
public @interface PerScreen {
}
