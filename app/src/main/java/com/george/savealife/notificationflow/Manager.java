package com.george.savealife.notificationflow;

import android.content.Context;

public class Manager {
    private Context context = null;
    private int notificationID = 0;

    public int getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }

    public static Manager getInstance(){
        return new Manager();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
