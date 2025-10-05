package com.myfloatingmodule;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class XposedInit implements IXposedHookLoadPackage {
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("MyFloatingModule: Package loaded: " + lpparam.packageName);
        XposedBridge.log("MyFloatingModule: Process name: " + lpparam.processName);
        
        // Hook into the specific game package
        if (lpparam.packageName.equals("com.com2usholdings.soulstrike.android.google.global.normal")) {
            
            XposedBridge.log("MyFloatingModule: *** TARGET GAME DETECTED ***");
            XposedBridge.log("MyFloatingModule: Hooking into Soul Strike game: " + lpparam.packageName);
            XposedBridge.log("MyFloatingModule: ClassLoader: " + lpparam.classLoader);
            
            // Hook into Unity's main activity to ensure proper context
            try {
                hookUnityActivity(lpparam);
            } catch (Exception e) {
                XposedBridge.log("MyFloatingModule: Error hooking Unity activity: " + e.getMessage());
            }
            
            // Also try the floating window approach
            FloatingWindowManager.startFloatingWindow(lpparam.classLoader);
        }
        // Also hook into system processes for broader compatibility
        else if (lpparam.packageName.equals("com.android.systemui") || 
                 lpparam.packageName.equals("com.android.launcher3") ||
                 lpparam.packageName.equals("android")) {
            
            XposedBridge.log("MyFloatingModule: Hooking into system process: " + lpparam.packageName);
            
            // Start floating window service
            FloatingWindowManager.startFloatingWindow(lpparam.classLoader);
        }
        else {
            XposedBridge.log("MyFloatingModule: Ignoring package: " + lpparam.packageName);
        }
    }
    
    private void hookUnityActivity(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook Unity's main activity onCreate method
            Class<?> unityPlayerClass = XposedHelpers.findClass("com.unity3d.player.UnityPlayer", lpparam.classLoader);
            if (unityPlayerClass != null) {
                XposedBridge.log("MyFloatingModule: Found UnityPlayer class");
                
                // Hook UnityPlayer's currentActivity
                XposedHelpers.findAndHookMethod(unityPlayerClass, "currentActivity", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.getResult() != null) {
                            XposedBridge.log("MyFloatingModule: Unity activity found, starting floating window");
                            // Start floating window with Unity activity context
                            FloatingWindowManager.startFloatingWindowWithContext((android.app.Activity) param.getResult());
                        }
                    }
                });
            }
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Unity hook failed: " + e.getMessage());
        }
        
        // Also try hooking Android Activity lifecycle
        try {
            Class<?> activityClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(activityClass, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    android.app.Activity activity = (android.app.Activity) param.thisObject;
                    if (activity.getClass().getName().contains("Unity")) {
                        XposedBridge.log("MyFloatingModule: Unity Activity onCreate detected");
                        FloatingWindowManager.startFloatingWindowWithContext(activity);
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Activity hook failed: " + e.getMessage());
        }
    }
}
