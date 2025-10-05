package com.myfloatingmodule;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedBridge;

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
            
            // Start floating window service immediately when game loads
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
}
