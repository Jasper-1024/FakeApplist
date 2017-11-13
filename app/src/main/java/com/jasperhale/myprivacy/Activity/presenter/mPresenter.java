package com.jasperhale.myprivacy.Activity.presenter;

import android.content.pm.PackageInfo;

import com.jasperhale.myprivacy.Activity.ViewModel.ViewModel;
import com.jasperhale.myprivacy.Activity.adapter.BindingAdapterItem;
import com.jasperhale.myprivacy.Activity.model.Model;
import com.jasperhale.myprivacy.Activity.model.mModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;



/**
 * Created by ZHANG on 2017/11/1.
 */

public class mPresenter implements Presenter {
    private final Model model;
    private final ViewModel viewModel;

    public mPresenter(ViewModel viewModel) {
        this.viewModel = viewModel;
        model = new mModel();
    }

    @Override
    public void RefreshView() {
        Observable
                .create((ObservableOnSubscribe<List<PackageInfo>>) emitter -> emitter.onNext(model.getPackages())
                )
                //新线程
                .subscribeOn(Schedulers.newThread())
                //cpu密集
                .observeOn(Schedulers.computation())
                .map(packages -> {
                    if (!model.ShowSystemApp()) {
                        //剔除系统应用
                        List<PackageInfo> items = new ArrayList<>();
                        for (PackageInfo pac : packages) {
                            if (!model.isSystemApp(pac)) {
                                items.add(pac);
                            }
                        }
                        return items;
                    } else {
                        return packages;
                    }
                })
                //io密集
                .observeOn(Schedulers.io())
                .map(t -> {
                    List<BindingAdapterItem> items = new ArrayList<>();
                    for (PackageInfo pac : t) {
                        items.add(model.creatApplistItem(pac));
                    }
                    return items;
                })
                //主线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    viewModel.setItems(s);
                    viewModel.RefreshRecycleView();
                });


    }
}