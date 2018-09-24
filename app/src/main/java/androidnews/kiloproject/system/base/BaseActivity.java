package androidnews.kiloproject.system.base;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.gyf.barlibrary.ImmersionBar;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Setting;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;
import java.util.Locale;

import androidnews.kiloproject.R;
import androidnews.kiloproject.event.LanguageEvent;
import androidnews.kiloproject.permission.InstallRationale;
import androidnews.kiloproject.permission.OverlayRationale;
import androidnews.kiloproject.permission.RuntimeRationale;
import androidnews.kiloproject.permission.WriteApkTask;

import androidnews.kiloproject.system.MyApplication;

import static androidnews.kiloproject.system.AppConfig.CONFIG_LANGUAGE;

/**
 * Created by Administrator on 2017/12/9.
 */

public abstract class BaseActivity extends AppCompatActivity {
    protected BaseActivity mActivity;
    boolean isStart = false;
    private ImmersionBar mImmersionBar;
    protected Gson gson = new Gson();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ScreenUtils.adaptScreen4VerticalSlide(this, 360);
        mActivity = this;
        isStart = true;
        changeAppLanguage(SPUtils.getInstance().getInt(CONFIG_LANGUAGE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isStart) {
            //初始化逻辑代码
            initSlowly();
            isStart = false;
        }
    }

    //对只需要一次初始化的耗时操作或者界面绘制放在这里
    protected abstract void initSlowly();

    protected void initToolbar(Toolbar toolbar){
        initToolbar(toolbar,false);
    }

    protected void initToolbar(Toolbar toolbar, boolean isBack) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        if (isBack){
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    //状态栏沉浸(透明版)
    protected void initStateBarTrans(boolean isBlackFront) {
//        ScreenUtils.cancelAdaptScreen(this);
        mImmersionBar = ImmersionBar.with(this);
        if (isBlackFront) {
            mImmersionBar.statusBarDarkFont(true, 0.2f)
                    //原理：如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色，
                    // 如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
                    .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                    .transparentStatusBar()  //透明状态栏，不写默认透明色
                    .transparentNavigationBar()
                    .init();
        } else {
            mImmersionBar.keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                    .transparentStatusBar()  //透明状态栏，不写默认透明色
                    .transparentNavigationBar()
                    .init();   //所有子类都将继承这些相同的属性
        }
//        ScreenUtils.restoreAdaptScreen();
    }

    //状态栏沉浸(颜色资源)
    protected void initStateBar(int colorRes, boolean isBlackFront) {
//        ScreenUtils.cancelAdaptScreen(this);
        mImmersionBar = ImmersionBar.with(this);
        if (isBlackFront) {
            mImmersionBar.statusBarDarkFont(true, 0.2f)
                    //原理：如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色，
                    // 如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
                    .statusBarColor(colorRes)
                    .fitsSystemWindows(true)
                    .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                    .init();
        } else {
            mImmersionBar.keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                    .statusBarColor(colorRes)
                    .navigationBarColor(colorRes)
                    .fitsSystemWindows(true)
                    .init();   //所有子类都将继承这些相同的属性
        }
//        ScreenUtils.restoreAdaptScreen();
    }

    //状态栏沉浸(解析前的的字符串色值)
    protected void initStateBar(String color, boolean isBlackFront) {
//        ScreenUtils.cancelAdaptScreen(this);
        mImmersionBar = ImmersionBar.with(this);
        if (isBlackFront) {
            mImmersionBar.statusBarDarkFont(true, 0.2f)
                    //原理：如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色，
                    // 如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
                    .statusBarColor(color)
                    .fitsSystemWindows(true)
                    .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                    .init();
        } else {
            mImmersionBar.keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                    .statusBarColor(color)
                    .navigationBarColor(color)
                    .fitsSystemWindows(true)
                    .init();   //所有子类都将继承这些相同的属性
        }
//        ScreenUtils.restoreAdaptScreen();
    }

    //状态栏沉浸(解析后的整型色值)
    protected void initStateBarInt(int colorInt, boolean isBlackFront) {
//        ScreenUtils.cancelAdaptScreen(this);
        mImmersionBar = ImmersionBar.with(this);
        if (isBlackFront) {
            try {
                mImmersionBar.statusBarDarkFont(true, 0.2f)
                        //原理：如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色，
                        // 如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
                        .statusBarColorInt(colorInt)
                        .fitsSystemWindows(true)
                        .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                        .init();
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            mImmersionBar.keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                    .statusBarColorInt(colorInt)
                    .navigationBarColorInt(colorInt)
                    .fitsSystemWindows(true)
                    .init();   //所有子类都将继承这些相同的属性
        }
//        ScreenUtils.restoreAdaptScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //必须调用该方法，防止内存泄漏，不调用该方法，如果界面bar发生改变，
        // 在不关闭app的情况下，退出此界面再进入将记忆最后一次bar改变的状态
        ImmersionBar.with(mActivity).destroy();
    }

    /**
     * 权限相关
     * @param grantedAction
     * @param permissions
     */
    protected void requestPermission(Action grantedAction,String... permissions) {
        AndPermission.with(mActivity)
                .runtime()
                .permission(permissions)
                .rationale(new RuntimeRationale())
                .onGranted(grantedAction)
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        if (AndPermission.hasAlwaysDeniedPermission(mActivity, permissions)) {
                            showSettingDialog(mActivity, permissions);
                        }
                    }
                })
                .start();
    }

    public void showSettingDialog(Context context, final List<String> permissions) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = context.getString(R.string.message_permission_always_failed, TextUtils.join("\n", permissionNames));

        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(R.string.title_dialog)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPermission();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    protected void setPermission() {
        AndPermission.with(this)
                .runtime()
                .setting()
                .onComeback(new Setting.Action() {
                    @Override
                    public void onAction() {
                        Toast.makeText(mActivity, R.string.message_setting_comeback, Toast.LENGTH_SHORT).show();
                    }
                })
                .start();
    }

    /**
     * Request to read and write external storage permissions.
     */
    protected void requestPermissionForInstallPackage() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE)
                .rationale(new RuntimeRationale())
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        new WriteApkTask(mActivity, new Runnable() {
                            @Override
                            public void run() {
                                installPackage();
                            }
                        }).execute();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        ToastUtils.showShort(R.string.message_install_failed);
                    }
                })
                .start();
    }

    /**
     * Install package.
     */
    protected void installPackage() {
        AndPermission.with(this)
                .install()
                .file(new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.apk_name)))
                .rationale(new InstallRationale())
                .onGranted(new Action<File>() {
                    @Override
                    public void onAction(File data) {
                        // Installing.
                    }
                })
                .onDenied(new Action<File>() {
                    @Override
                    public void onAction(File data) {
                        // The user refused to install.
                    }
                })
                .start();
    }

    protected void requestPermissionForAlertWindow() {
        AndPermission.with(this)
                .overlay()
                .rationale(new OverlayRationale())
                .onGranted(new Action<Void>() {
                    @Override
                    public void onAction(Void data) {
                        showAlertWindow();
                    }
                })
                .onDenied(new Action<Void>() {
                    @Override
                    public void onAction(Void data) {
                        ToastUtils.showShort(R.string.message_overlay_failed);
                    }
                })
                .start();
    }

    protected void showAlertWindow() {
        MyApplication.getInstance().showLauncherView();

        Intent backHome = new Intent(Intent.ACTION_MAIN);
        backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        backHome.addCategory(Intent.CATEGORY_HOME);
        startActivity(backHome);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LanguageEvent event) {
        changeAppLanguage(event.getLaguageType());
    }

    private void changeAppLanguage(int type){
        Locale myLocale = null;
        switch (type) {
            case 0:
                myLocale = Locale.getDefault();
                break;
            case 1:
                myLocale = new Locale("en");
                break;
            case 2:
                myLocale = new Locale("zh");
                break;
        }
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

}