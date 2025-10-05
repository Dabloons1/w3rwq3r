package com.myfloatingmodule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import de.robv.android.xposed.XposedBridge;

public class FloatingWindowManager {
    
    public static void startFloatingWindow(ClassLoader classLoader) {
        try {
            // Get the current context
            Context context = getSystemContext();
            if (context != null) {
                XposedBridge.log("MyFloatingModule: Starting floating window service");
                
                // Add a small delay to ensure the game has fully loaded
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Always start the floating window service when called from Xposed
                            Intent serviceIntent = new Intent(context, FloatingWindowService.class);
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent);
                            } else {
                                context.startService(serviceIntent);
                            }
                            XposedBridge.log("MyFloatingModule: Floating window service started successfully");
                        } catch (Exception e) {
                            XposedBridge.log("MyFloatingModule: Error in delayed start: " + e.getMessage());
                        }
                    }
                }, 3000); // 3 second delay
                
            } else {
                XposedBridge.log("MyFloatingModule: Could not get system context");
            }
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Error starting floating window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void startFloatingWindowWithContext(android.app.Activity activity) {
        try {
            XposedBridge.log("MyFloatingModule: Starting floating window with activity context");
            
            // Add a small delay to ensure the activity is fully initialized
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent serviceIntent = new Intent(activity, FloatingWindowService.class);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            activity.startForegroundService(serviceIntent);
                        } else {
                            activity.startService(serviceIntent);
                        }
                        XposedBridge.log("MyFloatingModule: Floating window service started with activity context");
                    } catch (Exception e) {
                        XposedBridge.log("MyFloatingModule: Error starting with activity context: " + e.getMessage());
                    }
                }
            }, 2000); // 2 second delay for activity initialization
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Error starting floating window with context: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void startFloatingWindowWithApplicationContext(android.app.Application app) {
        try {
            XposedBridge.log("MyFloatingModule: Starting floating window with application context");
            
            // Add a small delay to ensure the application is fully initialized
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent serviceIntent = new Intent(app, FloatingWindowService.class);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            app.startForegroundService(serviceIntent);
                        } else {
                            app.startService(serviceIntent);
                        }
                        XposedBridge.log("MyFloatingModule: Floating window service started with application context");
                    } catch (Exception e) {
                        XposedBridge.log("MyFloatingModule: Error starting with application context: " + e.getMessage());
                    }
                }
            }, 3000); // 3 second delay for application initialization
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Error starting floating window with application context: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Context getSystemContext() {
        try {
            // Try to get system context through reflection
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            if (activityThread != null) {
                return (Context) activityThreadClass.getMethod("getSystemContext").invoke(activityThread);
            }
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Could not get system context: " + e.getMessage());
        }
        return null;
    }
}
