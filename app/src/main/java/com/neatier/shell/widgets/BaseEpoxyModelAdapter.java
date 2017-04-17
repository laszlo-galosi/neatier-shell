/*
 * Copyright (C) 2016 Extremenet Ltd., All Rights Reserved
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

package com.neatier.shell.widgets;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import java.util.Collection;
import java.util.List;
import rx.Observable;
import trikita.log.Log;

/**
 * Creating a base holder class allows us to leverage ButterKnife's view binding for all
 * subclasses.
 * This makes subclasses much cleaner, and is a highly recommended pattern.
 */
public abstract class BaseEpoxyModelAdapter<M extends EpoxyModel> extends EpoxyAdapter {
    public BaseEpoxyModelAdapter() {
        super();
    }

    public M getModelAt(int position) {
        return (M) models.get(position);
    }

    public int getModelPos(M model) {
        return getModelPosition(model);
    }

    public void add(M model) {
        addModel(model);
    }

    public void addModelAt(int index, M model) {
        models.add(index, model);
        notifyItemInserted(index);
    }

    public void removeModelAt(int index) {
        models.remove(index);
        notifyItemRemoved(index);
    }

    public void addAll(Collection<M> collection) {
        int start = models.size();
        models.addAll((Collection<? extends EpoxyModel<?>>) collection);
        notifyItemRangeInserted(start, collection.size());
        //notifyModelsChanged();
    }

    public void clearModels() {
        int oldSize = models.size();
        models.clear();
        notifyItemRangeRemoved(0, oldSize);
    }

    public void setDataSet(final List<M> dataSet) {
        final int oldSize = models.size();
        models.clear();
        notifyItemRangeRemoved(0, oldSize);
        Observable.from(dataSet)
                  .subscribe(model -> {
                      Log.d("addModel").v(model);
                      addModel(model);
                  });
    }
}
