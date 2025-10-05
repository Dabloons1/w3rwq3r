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
            
            // Try multiple approaches to get context and start floating window
            try {
                hookUnityActivity(lpparam);
            } catch (Exception e) {
                XposedBridge.log("MyFloatingModule: Error hooking Unity activity: " + e.getMessage());
            }
            
            // Fallback: Try to start floating window with system context
            FloatingWindowManager.startFloatingWindow(lpparam.classLoader);
            
            // Additional fallback: Try to start with a simple delay
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    XposedBridge.log("MyFloatingModule: Fallback attempt - trying to start floating window");
                    FloatingWindowManager.startFloatingWindow(lpparam.classLoader);
                }
            }, 5000); // 5 second delay as final fallback
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
        // Try multiple approaches to get the activity context
        try {
            // Method 1: Hook Activity.onCreate for any activity in the game
            Class<?> activityClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(activityClass, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    android.app.Activity activity = (android.app.Activity) param.thisObject;
                    String activityName = activity.getClass().getName();
                    XposedBridge.log("MyFloatingModule: Activity created: " + activityName);
                    
                    // Check if it's the main game activity
                    if (activityName.contains("Unity") || 
                        activityName.contains("Main") || 
                        activityName.contains("Game") ||
                        activityName.contains("SoulStrike")) {
                        XposedBridge.log("MyFloatingModule: Main game activity detected: " + activityName);
                        FloatingWindowManager.startFloatingWindowWithContext(activity);
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Activity hook failed: " + e.getMessage());
        }
        
        // Method 2: Try to hook Application.onCreate
        try {
            Class<?> applicationClass = XposedHelpers.findClass("android.app.Application", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(applicationClass, "onCreate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    android.app.Application app = (android.app.Application) param.thisObject;
                    XposedBridge.log("MyFloatingModule: Application onCreate detected");
                    // Use application context as fallback
                    FloatingWindowManager.startFloatingWindowWithApplicationContext(app);
                }
            });
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Application hook failed: " + e.getMessage());
        }
        
        // Method 3: Try to get context from ActivityThread
        try {
            Class<?> activityThreadClass = XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(activityThreadClass, "performLaunchActivity", 
                XposedHelpers.findClass("android.app.ActivityThread$ActivityClientRecord", lpparam.classLoader),
                android.content.Intent.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("MyFloatingModule: Activity launch detected");
                    // Try to get the activity from the record
                    Object record = param.args[0];
                    if (record != null) {
                        try {
                            Object activity = XposedHelpers.getObjectField(record, "activity");
                            if (activity instanceof android.app.Activity) {
                                XposedBridge.log("MyFloatingModule: Got activity from launch record");
                                FloatingWindowManager.startFloatingWindowWithContext((android.app.Activity) activity);
                            }
                        } catch (Exception e) {
                            XposedBridge.log("MyFloatingModule: Error getting activity from record: " + e.getMessage());
                        }
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: ActivityThread hook failed: " + e.getMessage());
        }
    }
}
