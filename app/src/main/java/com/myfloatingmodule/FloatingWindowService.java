package com.myfloatingmodule;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;

public class FloatingWindowService extends Service {
    
    private WindowManager windowManager;
    private View floatingView;
    private boolean isFloatingWindowVisible = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, createNotification());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "floating_window_channel",
                "Floating Window Service",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, "floating_window_channel")
            .setContentTitle("Floating Window Service")
            .setContentText("Floating window is active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build();
    }
    
    private void showFloatingWindow() {
        if (isFloatingWindowVisible) return;
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // Create floating view
        floatingView = createFloatingView();
        
        // Set window parameters optimized for Unity games
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 50;
        params.y = 200;
        
        try {
            windowManager.addView(floatingView, params);
            isFloatingWindowVisible = true;
            android.util.Log.d("FloatingWindow", "Floating window added successfully");
            
            // Make the window more visible by bringing it to front
            floatingView.bringToFront();
            floatingView.invalidate();
            
            // Add a small delay and try to make it more visible
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (floatingView != null) {
                        floatingView.setVisibility(View.VISIBLE);
                        floatingView.bringToFront();
                        floatingView.invalidate();
                    }
                }
            }, 500);
            
        } catch (Exception e) {
            // Handle permission or other errors
            android.util.Log.e("FloatingWindow", "Error adding floating window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private View createFloatingView() {
        // Create a more visible floating window with game-specific buttons
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xCC000000); // More opaque black background
        layout.setPadding(15, 15, 15, 15);
        
        // Add a border
        layout.setBackgroundColor(0xCC000000);
        
        // Title
        TextView title = new TextView(this);
        title.setText("Soul Strike Mod Menu");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(14);
        title.setPadding(0, 0, 0, 10);
        layout.addView(title);
        
        // Game-specific buttons
        Button godModeButton = new Button(this);
        godModeButton.setText("God Mode");
        godModeButton.setTextSize(12);
        godModeButton.setPadding(5, 5, 5, 5);
        godModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.util.Log.d("FloatingWindow", "God Mode activated");
                // Add god mode functionality here
            }
        });
        layout.addView(godModeButton);
        
        Button speedButton = new Button(this);
        speedButton.setText("Speed Hack");
        speedButton.setTextSize(12);
        speedButton.setPadding(5, 5, 5, 5);
        speedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.util.Log.d("FloatingWindow", "Speed hack activated");
                // Add speed hack functionality here
            }
        });
        layout.addView(speedButton);
        
        Button coinsButton = new Button(this);
        coinsButton.setText("Add Coins");
        coinsButton.setTextSize(12);
        coinsButton.setPadding(5, 5, 5, 5);
        coinsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.util.Log.d("FloatingWindow", "Coins added");
                // Add coin modification functionality here
            }
        });
        layout.addView(coinsButton);
        
        Button closeButton = new Button(this);
        closeButton.setText("Close");
        closeButton.setTextSize(12);
        closeButton.setPadding(5, 5, 5, 5);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFloatingWindow();
                stopSelf();
            }
        });
        layout.addView(closeButton);
        
        return layout;
    }
    
    private void hideFloatingWindow() {
        if (isFloatingWindowVisible && windowManager != null && floatingView != null) {
            try {
                windowManager.removeView(floatingView);
                isFloatingWindowVisible = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        hideFloatingWindow();
    }
}
