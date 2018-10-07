package androidnews.kiloproject.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.SnackbarUtils;
import com.blankj.utilcode.util.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.florent37.materialviewpager.header.MaterialViewPagerHeaderDecorator;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.callback.SimpleCallBack;
import com.zhouyou.http.exception.ApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidnews.kiloproject.R;
import androidnews.kiloproject.activity.GalleyActivity;
import androidnews.kiloproject.activity.NewsDetailActivity;
import androidnews.kiloproject.adapter.MainRvAdapter;
import androidnews.kiloproject.bean.net.GalleyData;
import androidnews.kiloproject.bean.net.NewMainListData;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static androidnews.kiloproject.system.AppConfig.CONFIG_AUTO_CLEAR;
import static androidnews.kiloproject.system.AppConfig.CONFIG_AUTO_LOADMORE;
import static androidnews.kiloproject.system.AppConfig.CONFIG_AUTO_REFRESH;
import static androidnews.kiloproject.system.AppConfig.getMainDataA;
import static androidnews.kiloproject.system.AppConfig.getMainDataB;

/**
 * Created by florentchampigny on 24/04/15.
 */
public class MainRvFragment extends BaseRvFragment {

    MainRvAdapter mainAdapter;
    //    MainListData contents;
    List<NewMainListData> contents;

    private static final boolean GRID_LAYOUT = false;

    private String CACHE_LIST_DATA;

    private int currentPage = 0;
    private int questPage = 20;

    String typeStr;

    public static MainRvFragment newInstance(int type) {
        MainRvFragment f = new MainRvFragment();
        Bundle b = new Bundle();
        b.putInt("type", type);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        int position = 999;
        if (args != null) {
            position = args.getInt("type");
        }
        typeStr = getResources().getStringArray(R.array.address)[position];

        this.CACHE_LIST_DATA = typeStr + "_data";
        mainAdapter = new MainRvAdapter(mActivity, contents);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        if (GRID_LAYOUT) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2));
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        }
        mRecyclerView.setHasFixedSize(true);

        //Use this now
        mRecyclerView.addItemDecoration(new MaterialViewPagerHeaderDecorator());

//        refreshLayout.setRefreshHeader(new MaterialHeader(mActivity));
//        refreshLayout.setRefreshFooter(new ClassicsFooter(mActivity));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                requestData(TYPE_REFRESH, true);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                requestData(TYPE_LOADMORE, true);
            }
        });

        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                String json = SPUtils.getInstance().getString(CACHE_LIST_DATA, "");
                if (!TextUtils.isEmpty(json)) {
                    contents = gson.fromJson(json, new TypeToken<List<NewMainListData>>() {
                    }.getType());
                    if (contents != null && contents.size() > 0) {
                        contents.get(0)
                                .setItemType(HEADER);
                        e.onNext(true);
                    } else
                        e.onNext(false);
                } else e.onNext(false);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean s) throws Exception {
                        if (s)
                            createAdapter();
                        MainRvFragment.super.onViewCreated(view, savedInstanceState);
                    }
                });
    }

    protected void onFragmentVisibleChange(boolean isVisible) {
        if (isVisible) {
            if (contents == null || SPUtils.getInstance().getBoolean(CONFIG_AUTO_REFRESH)) {
                refreshLayout.autoRefresh();
            }
        }
    }

    private void requestData(int type, boolean isNotify) {
        String dataUrl = "";
        switch (type) {
            case TYPE_REFRESH:
                currentPage = 0;
            case TYPE_LOADMORE:
                dataUrl = getMainDataA + typeStr + "/" + currentPage + getMainDataB;
                break;
        }
        EasyHttp.get(dataUrl)
                .readTimeOut(30 * 1000)//局部定义读超时
                .writeTimeOut(30 * 1000)
                .connectTimeout(30 * 1000)
                .timeStamp(true)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        if (refreshLayout != null) {
                            switch (type) {
                                case TYPE_REFRESH:
                                    refreshLayout.finishRefresh(false);
                                    break;
                                case TYPE_LOADMORE:
                                    if (SPUtils.getInstance().getBoolean(CONFIG_AUTO_LOADMORE))
                                        mainAdapter.loadMoreFail();
                                    else
                                        refreshLayout.finishLoadMore(false);
                                    break;
                            }
                            SnackbarUtils.with(refreshLayout).
                                    setMessage(getString(R.string.load_fail) + e.getMessage()).
                                    showError();
                        }
                    }

                    @Override
                    public void onSuccess(String response) {
                        if (!TextUtils.isEmpty(response) || TextUtils.equals(response, "{}")) {
                            Observable.create(new ObservableOnSubscribe<Boolean>() {
                                @Override
                                public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                                    HashMap<String, List<NewMainListData>> retMap = null;
                                    try {
                                        retMap = gson.fromJson(response,
                                                new TypeToken<HashMap<String, List<NewMainListData>>>() {
                                                }.getType());
                                        //设置头部轮播
                                        if (type == TYPE_REFRESH) {
                                            NewMainListData first = retMap.get(typeStr).get(0);
                                            first.setItemType(HEADER);
                                            if (first.getAds() != null) {
                                                for (int i = 0; i < first.getAds().size(); i++) {
                                                    NewMainListData.AdsBean bean =
                                                            first.getAds().get(i);
                                                    if (!bean.getSkipID().contains("|")) {
                                                        first.getAds().remove(i);
                                                        continue;
                                                    }
                                                    if (bean.getImgsrc().equals("bigimg")) {
                                                        requestRealPic(i, bean.getSkipID());
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                        loadFailed(type);
                                    }
                                    List<NewMainListData> newList = new ArrayList<>();
                                    switch (type) {
                                        case TYPE_REFRESH:
                                            currentPage = 0;
                                            contents = new ArrayList<>();
                                            newList = retMap.get(typeStr);
                                            for (NewMainListData content : newList) {
                                                if (TextUtils.isEmpty(content.getTAG()))
//                                                if (!TextUtils.isEmpty(content.getSource()) && TextUtils.isEmpty(content.getTAG()))
                                                    contents.add(content);
                                            }
                                            SPUtils.getInstance().put(CACHE_LIST_DATA, gson.toJson(contents));
                                            break;
                                        case TYPE_LOADMORE:
                                            currentPage += questPage;
                                            newList.addAll(contents);
                                            boolean isAllSame = true;
                                            for (NewMainListData newBean : retMap.get(typeStr)) {
                                                boolean isSame = false;
//                                                if (TextUtils.isEmpty(newBean.getSource()) && !TextUtils.isEmpty(newBean.getTAG())){
                                                if (!TextUtils.isEmpty(newBean.getTAG())){
                                                    continue;
                                                }
                                                for (NewMainListData myBean : contents) {
                                                    if (TextUtils.equals(myBean.getDocid(), newBean.getDocid())) {
                                                        isSame = true;
                                                        break;
                                                    }
                                                }
                                                if (!isSame) {
                                                    newList.add(newBean);
                                                    isAllSame = false;
                                                }
                                            }
                                            if (!isAllSame) {
                                                contents.clear();
                                                contents.addAll(newList);
                                            }
                                            break;
                                    }
                                    e.onNext(true);
                                    e.onComplete();
                                }
                            }).subscribeOn(Schedulers.computation())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<Boolean>() {
                                        @Override
                                        public void accept(Boolean o) throws Exception {
                                            if (mainAdapter == null || type == TYPE_REFRESH) {
                                                createAdapter();
                                                refreshLayout.finishRefresh(true);
                                            } else if (type == TYPE_LOADMORE) {
                                                mainAdapter.notifyDataSetChanged();
                                                if (SPUtils.getInstance().getBoolean(CONFIG_AUTO_LOADMORE))
                                                    mainAdapter.loadMoreComplete();
                                                else
                                                    refreshLayout.finishLoadMore(true);
                                            }
                                            if (isNotify)
                                                SnackbarUtils.with(refreshLayout)
                                                        .setMessage(getString(R.string.load_success))
                                                        .showSuccess();
                                        }
                                    });
                        } else {
                            loadFailed(type);
                        }
                    }
                });
    }

    private void loadFailed(int type) {
        switch (type) {
            case TYPE_REFRESH:
                refreshLayout.finishRefresh(false);
                SnackbarUtils.with(refreshLayout).setMessage(getString(R.string.server_fail)).showError();
                break;
            case TYPE_LOADMORE:
                if (SPUtils.getInstance().getBoolean(CONFIG_AUTO_LOADMORE))
                    mainAdapter.loadMoreFail();
                else
                    refreshLayout.finishLoadMore(false);
                SnackbarUtils.with(refreshLayout).setMessage(getString(R.string.server_fail)).showError();
                break;
        }
    }

    private void createAdapter() {
        mainAdapter = new MainRvAdapter(getActivity(), contents);
        mainAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                NewMainListData bean = contents.get(position);
                Intent intent = null;
                switch (bean.getItemType()) {
                    case CELL:
                        if (!TextUtils.isEmpty(bean.getSkipID()) && TextUtils.equals(bean.getSkipType(), "photoset")) {
                            String skipID = "";
                            String rawId;
                            rawId = bean.getSkipID();
                            if (!TextUtils.isEmpty(rawId)) {
                                int index = rawId.lastIndexOf("|");
                                if (index != -1) {
                                    skipID = rawId.substring(index - 4, rawId.length());
                                    intent = new Intent(mActivity, GalleyActivity.class);
                                    intent.putExtra("skipID", skipID.replace("|", "/") + ".json");
                                    startActivity(intent);
                                } else {
                                    SnackbarUtils.with(refreshLayout).setMessage(getString(R.string.server_fail)).showError();
                                    return;
                                }
                            }
                            break;
                        }else {
                            intent = new Intent(getActivity(), NewsDetailActivity.class);
                            intent.putExtra("docid", bean.getDocid().replace("_special", "").trim());
                            startActivity(intent);
                        }
                }
            }
        });
        mainAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                ClipboardManager cm = (ClipboardManager) Utils.getApp().getSystemService(Context.CLIPBOARD_SERVICE);
                //noinspection ConstantConditions
                cm.setPrimaryClip(ClipData.newPlainText("link", contents.get(position).getUrl()));
                SnackbarUtils.with(refreshLayout).setMessage(getString(R.string.action_link)
                        + " " + getString(R.string.successfully)).showSuccess();
                return true;
            }
        });
        mRecyclerView.setAdapter(mainAdapter);
        if (SPUtils.getInstance().getBoolean(CONFIG_AUTO_LOADMORE)) {
            mainAdapter.setPreLoadNumber(5);
            mainAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
                @Override
                public void onLoadMoreRequested() {
                    requestData(TYPE_LOADMORE, false);
                }
            }, mRecyclerView);
            mainAdapter.disableLoadMoreIfNotFullPage();
            refreshLayout.setEnableLoadMore(false);
        }
    }

    private void requestRealPic(final int position, String rawId) {
        String skipID = rawId.split("000")[1];
        EasyHttp.get("/photo/api/set/" + "000" + skipID.replace("|", "/") + ".json")
                .readTimeOut(30 * 1000)//局部定义读超时
                .writeTimeOut(30 * 1000)
                .connectTimeout(30 * 1000)
                .timeStamp(true)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        SnackbarUtils.with(refreshLayout).setMessage(getString(R.string.load_fail) + e.getMessage()).showError();
                    }

                    @Override
                    public void onSuccess(String response) {
                        if (!TextUtils.isEmpty(response) || TextUtils.equals(response, "{}")) {
                            GalleyData galleyContent = gson.fromJson(response, GalleyData.class);
                            if (contents != null) {
                                NewMainListData bean = contents.get(0);
                                bean.getAds().get(position).setImgsrc(galleyContent.getPhotos().get(0).getSquareimgurl());
                                mainAdapter.notifyItemChanged(0);

                                String json = gson.toJson(contents);
                                SPUtils.getInstance().put(CACHE_LIST_DATA, json);
                            }
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (SPUtils.getInstance().getBoolean(CONFIG_AUTO_CLEAR)) {
            SPUtils.getInstance().put(CACHE_LIST_DATA, "");
        }
        super.onDestroy();
    }
}
