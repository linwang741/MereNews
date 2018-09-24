package androidnews.kiloproject.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.SnackbarUtils;
import com.gyf.barlibrary.ImmersionBar;
import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.callback.SimpleCallBack;
import com.zhouyou.http.exception.ApiException;

import java.util.ArrayList;
import java.util.List;

import androidnews.kiloproject.R;
import androidnews.kiloproject.adapter.CommentAdapter;
import androidnews.kiloproject.bean.data.CommentLevel;
import androidnews.kiloproject.bean.net.CommonFullData;
import androidnews.kiloproject.system.base.BaseActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static androidnews.kiloproject.adapter.CommentAdapter.LEVEL_ONE;
import static androidnews.kiloproject.adapter.CommentAdapter.LEVEL_TWO;
import static androidnews.kiloproject.system.AppConfig.CONFIG_STATUSBAR;
import static androidnews.kiloproject.system.AppConfig.HOST163COMMENT;
import static androidnews.kiloproject.system.AppConfig.getNewsCommentA;
import static androidnews.kiloproject.system.AppConfig.getNewsCommentB;

public class CommentActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.progress)
    ProgressBar progress;

    CommentAdapter commentAdapter;
    @BindView(R.id.rv_content)
    RecyclerView rvContent;

    private List<CommentLevel> comments = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ButterKnife.bind(this);
        initToolbar(toolbar, true);
        getSupportActionBar().setTitle(R.string.action_comment);
        if (SPUtils.getInstance().getBoolean(CONFIG_STATUSBAR))
            ImmersionBar.with(mActivity).fitsSystemWindows(true).statusBarDarkFont(true).init();
        else
            initStateBar(android.R.color.white, true);
    }

    @Override
    protected void initSlowly() {
        String docid = getIntent().getStringExtra("docid");
        String board = getIntent().getStringExtra("board");
        EasyHttp.get(getNewsCommentA + board + "/" + docid + getNewsCommentB)
                .baseUrl(HOST163COMMENT)
                .readTimeOut(30 * 1000)//局部定义读超时
                .writeTimeOut(30 * 1000)
                .connectTimeout(30 * 1000)
                .timeStamp(true)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        progress.setVisibility(View.GONE);
                        SnackbarUtils.with(toolbar).setMessage(getString(R.string.load_fail) + e.getMessage()).showError();
                    }

                    @Override
                    public void onSuccess(String response) {
                        progress.setVisibility(View.GONE);
                        if (!TextUtils.isEmpty(response) || TextUtils.equals(response,"{}")) {
                            CommonFullData data = gson.fromJson(response, CommonFullData.class);
                            if (data.getNewPosts() != null && data.getNewPosts().size() > 0)
                                analysisData(data);
                        } else {
                            progress.setVisibility(View.GONE);
                            SnackbarUtils.with(toolbar).setMessage(getString(R.string.load_fail)).showError();
                        }
                    }
                });
}

    private void analysisData(CommonFullData data) {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                for (CommonFullData.NewPostsBean floor : data.getNewPosts()) {
                    CommonFullData.NewPostsBean._$1Bean layer = floor.get_$1();
                    if (layer != null) {
                        comments.add(new CommentLevel(layer.getTimg(), layer.getF(), layer.getB(), layer.getT(), LEVEL_ONE));
                    }

                    layer = floor.get_$2();
                    if (layer != null) {
                        comments.add(new CommentLevel(layer.getTimg(), layer.getF(), layer.getB(), layer.getT(), LEVEL_TWO));
                    } else {
                        continue;
                    }

                    layer = floor.get_$3();
                    if (layer != null) {
                        comments.add(new CommentLevel(layer.getTimg(), layer.getF(), layer.getB(), layer.getT(), LEVEL_TWO));
                    } else {
                        continue;
                    }

                    layer = floor.get_$4();
                    if (layer != null) {
                        comments.add(new CommentLevel(layer.getTimg(), layer.getF(), layer.getB(), layer.getT(), LEVEL_TWO));
                    } else {
                        continue;
                    }

                    layer = floor.get_$5();
                    if (layer != null) {
                        comments.add(new CommentLevel(layer.getTimg(), layer.getF(), layer.getB(), layer.getT(), LEVEL_TWO));
                    } else {
                        continue;
                    }

                    layer = floor.get_$6();
                    if (layer != null) {
                        comments.add(new CommentLevel(layer.getTimg(), layer.getF(), layer.getB(), layer.getT(), LEVEL_TWO));
                    } else {
                        continue;
                    }

                    layer = floor.get_$7();
                    if (layer != null) {
                        comments.add(new CommentLevel(layer.getTimg(), layer.getF(), layer.getB(), layer.getT(), LEVEL_TWO));
                    } else {
                        continue;
                    }

                    layer = floor.get_$8();
                    if (layer != null) {
                        comments.add(new CommentLevel(layer.getTimg(), layer.getF(), layer.getB(), layer.getT(), LEVEL_TWO));
                    } else {
                        continue;
                    }

                    layer = floor.get_$9();
                    if (layer != null) {
                        comments.add(new CommentLevel(layer.getTimg(), layer.getF(), layer.getB(), layer.getT(), LEVEL_TWO));
                    } else {
                        continue;
                    }

                    layer = floor.get_$10();
                    if (layer != null) {
                        comments.add(new CommentLevel(layer.getTimg(), layer.getF(), layer.getB(), layer.getT(), LEVEL_TWO));
                    }
                }
                e.onNext(true);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        commentAdapter = new CommentAdapter(mActivity, comments);
                        rvContent.setLayoutManager(new LinearLayoutManager(mActivity));
                        rvContent.setAdapter(commentAdapter);
                    }
                });
    }
}