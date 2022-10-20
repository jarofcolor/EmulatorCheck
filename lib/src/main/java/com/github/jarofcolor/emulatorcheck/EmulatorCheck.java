package com.github.jarofcolor.emulatorcheck;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmulatorCheck {

    private static final String TAG = "EmulatorCheck";
    private static final String KEY_CHECK_VERSION = "check_version";
    private static final String KEY_NAME = "name";
    private static final int VERSION = 1;

    private abstract static class CheckBean {
        abstract boolean check(Context context);
    }

    private static class ActivityCheckBean extends CheckBean {

        protected final String pkgName;
        protected final String className;

        public ActivityCheckBean(String pkgName, String className) {
            this.pkgName = pkgName;
            this.className = className;
        }

        @Override
        boolean check(Context context) {
            Intent intent = new Intent();
            intent.setClassName(pkgName, className);
            return context.getPackageManager().resolveActivity(intent, 0) != null;
        }
    }

    private static class ServiceCheckBean extends ActivityCheckBean {

        public ServiceCheckBean(String pkgName, String className) {
            super(pkgName, className);
        }

        @Override
        boolean check(Context context) {
            Intent intent = new Intent();
            intent.setClassName(pkgName, className);
            return context.getPackageManager().resolveService(intent, 0) != null;
        }
    }

    private abstract static class ResourceCheckBean extends CheckBean {
        private final String name;
        private final String defType;
        private final String defPackage;

        public ResourceCheckBean(String name, String defType, String defPackage) {
            this.name = name;
            this.defType = defType;
            this.defPackage = defPackage;
        }

        protected final int getId(Context context) {
            return context.getResources().getIdentifier(name, defType, defPackage);
        }
    }

    private static class StringResourceCheckBean extends ResourceCheckBean {
        private final String contains;

        public StringResourceCheckBean(String name, String defType, String defPackage, String contains) {
            super(name, defType, defPackage);
            this.contains = contains;
        }

        @Override
        boolean check(Context context) {
            int id = getId(context);
            if (id > 0) {
                String str = context.getResources().getString(id);
                return str != null && str.contains(contains);
            }
            return false;
        }
    }

    private static class ShellCheckBean extends CheckBean {
        private final String cmd;
        private final String[] contains;

        public ShellCheckBean(String cmd, String[] contains) {
            this.cmd = cmd;
            this.contains = contains;
        }

        @Override
        boolean check(Context context) {
            CmdHandler.Result result = CmdHandler.get().runtimeCommand(cmd);
//            LogUtil.i(TAG,result.toString());
            String msg = result.getMsg();
            if (!TextUtils.isEmpty(msg))
                for (String str : contains) {
                    if (msg.contains(str)) {
                        return true;
                    }
                }
            return false;
        }
    }

    private final static HashMap<String, List<CheckBean>> checkBeansMap = new HashMap<>();
    private static String sEmulatorName;

    static {
        ArrayList<CheckBean> checkBeans = new ArrayList<>();
        checkBeans.add(new ActivityCheckBean("com.mumu.store", "com.mumu.store.MainActivity"));
        checkBeans.add(new ActivityCheckBean("com.mumu.launcher", "com.mumu.launcher.Launcher"));
        checkBeansMap.put("网易MUMU", checkBeans);

        checkBeans = new ArrayList<>();
        checkBeans.add(new ActivityCheckBean("com.bignox.app.store.hd", "com.bignox.app.store.hd.ui.activity.MainActivity"));
        checkBeans.add(new ActivityCheckBean("com.vphone.launcher", "com.vphone.launcher.launcher3.Launcher"));
        checkBeansMap.put("夜神NOX", checkBeans);

        checkBeans = new ArrayList<>();
        checkBeans.add(new ActivityCheckBean("com.android.flysilkworm", "com.android.flysilkworm.app.activity.FrameworkActivity"));
        checkBeansMap.put("雷电", checkBeans);

        checkBeans = new ArrayList<>();
        checkBeans.add(new ActivityCheckBean("com.microvirt.market", "com.microvirt.market.activity.MainActivity"));
        checkBeans.add(new ActivityCheckBean("com.microvirt.launcher2", "com.microvirt.launcher.Launcher"));
        checkBeansMap.put("逍遥", checkBeans);

        checkBeans = new ArrayList<>();
        checkBeans.add(new ServiceCheckBean("com.tencent.tinput", "com.tencent.tinput.SoftKeyboard"));
        checkBeansMap.put("腾讯手游助手", checkBeans);

        checkBeans = new ArrayList<>();
        checkBeans.add(new ActivityCheckBean("com.tiantian.ime", "com.tiantian.ime.ImePreferences"));
        checkBeansMap.put("天天", checkBeans);

        checkBeans = new ArrayList<>();
        checkBeans.add(new StringResourceCheckBean("removing_account_restriction_message", "string", "android", "BlueStacks"));
        checkBeansMap.put("蓝叠", checkBeans);

        checkBeans = new ArrayList<>();
        checkBeans.add(new ShellCheckBean("cat /proc/" + Process.myPid() + "/maps", new String[]
                {"/data/dalvik-cache/x86/system@framework@boot",
                        "/system/framework/x86/boot"}
        ));
        checkBeansMap.put("未知", checkBeans);
    }

    static void init(Context context) {
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName() + "_" + TAG, Context.MODE_PRIVATE);
        if (sp.getInt(KEY_CHECK_VERSION, 0) == VERSION) {
            sEmulatorName = sp.getString(KEY_NAME, null);
            return;
        }

        sp.edit().putInt(KEY_CHECK_VERSION, VERSION).apply();

        for (Map.Entry<String, List<CheckBean>> entry : checkBeansMap.entrySet()) {
            List<CheckBean> checkBeans = entry.getValue();
            boolean isEmulator = true;
            for (CheckBean checkBean : checkBeans) {
                isEmulator &= checkBean.check(context);
            }
            if (isEmulator) {
                Log.i(TAG, "当前为模拟器，模拟器类型为" + entry.getKey());
                sEmulatorName = entry.getKey();
                sp.edit().putString(KEY_NAME, sEmulatorName).apply();
                return;
            }
        }
    }

    public static synchronized boolean isEmulator() {
        return sEmulatorName != null;
    }

    public static synchronized String getEmulatorName() {
        return sEmulatorName;
    }
}
