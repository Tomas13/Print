package com.example.jean.print.printer;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.example.jean.print.adapters.USBAdapter;
import com.example.jean.print.utils.debug.LLog;


public class PrintOrder {
    private static final String TAG = PrintOrder.class.getSimpleName();

    public PrintOrder() {

    }

    public void Print(Context context, String msg, UsbDevice usbDevice) {
        LLog.e(TAG, "Print");
        USBAdapter usba = new USBAdapter();
        usba.createConn(context, usbDevice);
        try {
            usba.printMessage(context, msg);
            usba.closeConnection(context);
        } catch (Exception e) {
            LLog.e(TAG, "Print. Error: " + e.getMessage());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
