package com.neatier.shell.xboxgames;

import android.support.annotation.CallSuper;
import android.view.View;
import butterknife.ButterKnife;
import com.airbnb.epoxy.DataBindingEpoxyModel;
import com.fernandocejas.arrow.optional.Optional;

/**
 * Creating a base holder class allows us to leverage ButterKnife's view binding for all
 * subclasses.
 * This makes subclasses much cleaner, and is a highly recommended pattern.
 */
public abstract class BaseEpoxyHolder extends DataBindingEpoxyModel.DataBindingHolder {

    protected Optional<Object> mDataItem;

    @CallSuper
    @Override
    protected void bindView(View itemView) {
        super.bindView(itemView);
        ButterKnife.bind(this, itemView);
    }
}
