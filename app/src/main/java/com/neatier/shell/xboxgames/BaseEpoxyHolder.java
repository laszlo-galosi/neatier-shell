package com.neatier.shell.xboxgames;

import android.support.annotation.CallSuper;
import android.view.View;
import butterknife.ButterKnife;
import com.airbnb.epoxy.EpoxyHolder;
import com.fernandocejas.arrow.optional.Optional;

/**
 * Creating a base holder class allows us to leverage ButterKnife's view binding for all
 * subclasses.
 * This makes subclasses much cleaner, and is a highly recommended pattern.
 */
public abstract class BaseEpoxyHolder extends EpoxyHolder {

    protected Optional<Object> mDataItem;

    @CallSuper
    @Override
    protected void bindView(View itemView) {
        ButterKnife.bind(this, itemView);
    }
}
