package com.example.jean.print.adapters;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.example.jean.print.utils.debug.LLog;

import it.custom.printer.api.android.CustomAndroidAPI;
import it.custom.printer.api.android.CustomException;
import it.custom.printer.api.android.CustomPrinter;


public class USBAdapter {
    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private PendingIntent mPermissionIntent;
    UsbDeviceConnection connection;
    String TAG = "USB";

    byte[] cut_paper = {0x1B, 0x69};
    byte[] print_command = {0x1B, 0x4A};
    private CustomPrinter prnDevice;
    private Context mContext;

    public USBAdapter() {
    }

    public void createConn(Context context, UsbDevice usbDevice) {
        LLog.e(TAG, "createConn");
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        final String ACTION_USB_PERMISSION = "kz.otgroup.parking.activities.USB_PERMISSION";
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mDevice = usbDevice;
//        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
//        context.registerReceiver(mUsbReceiver, filter);
    }

    private static final String ACTION_USB_PERMISSION =
            "kz.otgroup.parking.activities.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    mDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (mDevice != null) {
                            //call method to set up device communication
                        }
                    } else {
                        LLog.e(TAG, "permission denied for device " + mDevice);
                    }
                }
            }
        }
    };

    public byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    @SuppressLint("NewApi")
    public void printMessage(Context context, String msg) {
        mContext = context;
        LLog.e(TAG, "printMessage");
        // TODO Auto-generated method stub
        final String printdata = msg;
        final UsbEndpoint mEndpointBulkOut;
        LLog.e(TAG, "mUsbManager: " + mUsbManager);
        if (mUsbManager.hasPermission(mDevice)) {
            LLog.e(TAG, "Device have permission");
            UsbInterface intf = mDevice.getInterface(0);
            for (int i = 0; i < intf.getEndpointCount(); i++) {
                UsbEndpoint ep = intf.getEndpoint(i);
                if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                        mEndpointBulkOut = ep;
                        connection = mUsbManager.openDevice(mDevice);
                        if (connection != null) {
                            LLog.e(TAG, "Device connected");
                        }
                        /*PrinterFont fntPrinterNormal = new PrinterFont();
                        //open device
                        if (openDevice() == false)
                            LLog.e(TAG, "prnDevice not connected");

                        try {
                            //Fill class: NORMAL
                            fntPrinterNormal.setCharHeight(PrinterFont.FONT_SIZE_X1);                    //Height x1
                            fntPrinterNormal.setCharWidth(PrinterFont.FONT_SIZE_X1);                    //Width x1
                            fntPrinterNormal.setEmphasized(false);                                        //No Bold
                            fntPrinterNormal.setItalic(false);                                            //No Italic
                            fntPrinterNormal.setUnderline(false);                                        //No Underline
                            fntPrinterNormal.setJustification(PrinterFont.FONT_JUSTIFICATION_CENTER);    //Center
                            fntPrinterNormal.setInternationalCharSet(PrinterFont.FONT_CS_DEFAULT);        //Default International Chars
                        } catch (CustomException e) {
                            //Show Error
                            LLog.e(TAG, "Error: " + e.getMessage());
                        } catch (Exception e) {
                            LLog.e(TAG, "Set font properties error...");
                        }

                        try {
                            //Print Text (NORMAL)
                            prnDevice.printText(printdata, fntPrinterNormal);
//                            prnDevice.printTextLF(printdata, fntPrinterNormal);
                        } catch (CustomException e) {
                            LLog.e(TAG, "Error print: " + e.getMessage());
                            //Show Error
                        } catch (Exception e) {
                            LLog.e(TAG, "Print Text Error...");
                        }*/
                           boolean forceClaim = true;
				           connection.claimInterface(intf, forceClaim );
				           //Integer res = connection.bulkTransfer(mEndpointBulkOut, printdata.getBytes(), printdata.getBytes().length, 10000);
				           new Thread(new Runnable() 
				           { 
					           @Override 
					           public void run() 
					           { 
					               // TODO Auto-generated method stub
								   LLog.e(TAG, "in run thread");
					               byte[] bytes = printdata.getBytes();
                                   byte[] print_bytes = concat(print_command, bytes);

					               int b = connection.bulkTransfer(mEndpointBulkOut, print_bytes, print_bytes.length, 10000);
								   LLog.e(TAG, "Return Status. b-->" + b);
								   b = connection.bulkTransfer(mEndpointBulkOut, cut_paper, cut_paper.length, 0);
								   LLog.e(TAG, "Return Status. b-->" + b);
							   }
				            }).start(); 

				               connection.releaseInterface(intf);
				           break;
                    }
                }
            }
        } else {
            mUsbManager.requestPermission(mDevice, mPermissionIntent);
            LLog.e(TAG, "Device have no permission");
        }
    }

    @SuppressLint("NewApi")
    public void closeConnection(Context context) {
        LLog.e(TAG, "closeConnection");
        BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        LLog.e(TAG, "Device closed");
                        connection.close();
                    }
                }
            }
        };
    }

    public boolean openDevice() {
        LLog.e(TAG, "openDevice");
        //If i never open it
        if (prnDevice == null) {
            try {
                //Open and connect it
                /*UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                if (mUsbManager.hasPermission(usbDeviceList[0])) {

                }*/
                prnDevice = new CustomAndroidAPI().getPrinterDriverUSB(mDevice, mContext);
                return true;
            } catch (CustomException e) {

                //Show Error
                LLog.e(TAG, "openDevice. Error: " + e.getMessage());
                return false;
            } catch (Exception e) {
                LLog.e(TAG, "openDevice. Open Print Error...");
                //open error
                return false;
            }
        }
        //Already opened
        return true;

    }
}
