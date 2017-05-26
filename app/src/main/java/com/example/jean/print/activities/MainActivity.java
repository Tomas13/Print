package com.example.jean.print.activities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.jean.print.R;
import com.example.jean.print.utils.debug.LLog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import it.custom.printer.api.android.CustomAndroidAPI;
import it.custom.printer.api.android.CustomException;
import it.custom.printer.api.android.CustomPrinter;
import it.custom.printer.api.android.PrinterFont;


public class MainActivity extends AppCompatActivity {
    /*************************************
     * PRIVATE STATIC CONSTANTS
     *************************************/
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PORT = 8080;
    private static final String MIME_JSON = "application/json";
    private static final String SERVICE_NAME = "/parking";
    private static final long DELAY = 30000;

    /*************************************
     * PRIVATE FIELDS
     *************************************/
//    private ParkingServer mServer;
    private Handler mHandler = new Handler();

    private TextView mLogTextView;

//    private String mDate;
//    private String mTime;
    private String mNumber;
    private Timer clearStackTimer;

    private CustomPrinter prnDevice = null;
    private UsbDevice[] usbDeviceList = null;

    /*private final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    PendingIntent mPermissionIntent;
    UsbManager usbManager;
    UsbDevice device;
    UsbDevice printer = null;
    private static final int PRINTER_VENDOR_ID = 3540;
    private String mPrintString = "";*/

    /*************************************
     * LIFECYCLE METHODS
     *************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LLog.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mLogTextView = (TextView) findViewById(R.id.log_text);

        //Init everything
        init(savedInstanceState);

//        initSecondVersion();
    }

    @Override
    public void onResume() {
        LLog.e(TAG, "onResume");
        super.onResume();
       /* try {
            if (mServer == null) {
                mServer = new ParkingServer();
            } else {
                if (!mServer.isAlive()) {
                    mServer.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onPause() {
        LLog.e(TAG, "onPause");
        super.onPause();
//        if (mServer != null) {
            //mServer.stop();
//        }
    }

    /*************************************
     * PRIVATE METHODS
     *************************************/
    private void init(Bundle savedInstanceState) {
        LLog.e(TAG, "init");
        if (savedInstanceState == null) {
            try {
                //Get the list of devices
                usbDeviceList = CustomAndroidAPI.EnumUsbDevices(this);
                LLog.e(TAG, "init. usbDeviceList.length = " + usbDeviceList.length);
                if ((usbDeviceList == null) || (usbDeviceList.length == 0)) {
                    //Show Error
                    LLog.e(TAG, "init. Error: No Devices Connected...");
                    return;
                }
            } catch (CustomException e) {
                LLog.e(TAG, "init. Error: " + e.getMessage());
                return;
            } catch (Exception e) {
                //Show Error
                LLog.e(TAG, "init. Enum devices error...");
                return;
            }
        }

    }

    private void doPrint(String date, String time, String number) {
        LLog.e(TAG, "doPrint");
        new PrintTask(this).execute(date, time, number);
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
                prnDevice = new CustomAndroidAPI().getPrinterDriverUSB(usbDeviceList[0], this);
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

    /*private void initSecondVersion() {
        LLog.e(TAG, "initSecondVersion");
        try {
            usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            if (deviceList.size() <= 0) {
                LLog.e(TAG, "No device found");
            } else {
                LLog.e(TAG, "Number of device : " + deviceList.size());
            }
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            int count = 0;
            mPermissionIntent = PendingIntent.getBroadcast(getBaseContext(), 0,
                    new Intent(ACTION_USB_PERMISSION), 0);
            while (deviceIterator.hasNext()) {
                count++;
                device = deviceIterator.next();
                LLog.e(TAG, "Device No " + count + "........");
                LLog.e(TAG, "Vendor id : " + device.getVendorId());
                LLog.e(TAG, "Product id : " + device.getProductId());
                LLog.e(TAG, "Device  name : " + device.getDeviceName());
                LLog.e(TAG, "Device class : " + device.getClass().getName());
                LLog.e(TAG, "Device protocol: " + device.getDeviceProtocol());
                LLog.e(TAG, "Device subclass : " + device.getDeviceSubclass());
                if (device.getVendorId() == PRINTER_VENDOR_ID) {
                    printer = device;
                    break;
                }
            }
        } catch (Exception e) {
            LLog.e(TAG, "initSecondVersion. Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printCommand(String printString) {
        mPrintString = printString;
        LLog.e(TAG, "Print command given");
        IntentFilter filter = new IntentFilter(
                ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        if (printer != null) {
            usbManager.requestPermission(printer,
                    mPermissionIntent);
        } else {
            LLog.e(TAG, "Printer not found");
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        final UsbDevice printerDevice = (UsbDevice) intent
                                .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(
                                UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (printerDevice != null) {
                                LLog.e(TAG, "Device permission granted");
                                startPrinting(printerDevice);
                            }
                        } else {
                            LLog.e(TAG, "permission denied for device "
                                    + printerDevice);
                        }
                    }
                }
            } catch (Exception e) {
                LLog.e(TAG, "Exception in onRecieve " + e.getMessage());
                e.printStackTrace();
            }
        }

    };

    public void startPrinting(final UsbDevice printerDevice) {
        new Handler().post(new Runnable() {
            UsbDeviceConnection conn;
            UsbInterface usbInterface;

            @Override
            public void run() {
                try {
                    LLog.e(TAG, "Bulk transfer started");
                    usbInterface = printerDevice.getInterface(0);

                    for(int i = 0; i < device.getInterfaceCount(); i++){
                        usbInterface = device.getInterface(i);
                        if(usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER){
                            printer = device;
                        }
                    }

                    UsbEndpoint endPoint = usbInterface.getEndpoint(0);
                    conn = usbManager.openDevice(printer);
                    conn.claimInterface(usbInterface, true);
//                    String myStringData = "\nThis \nis \nmy \nsample \ntext";
                    LLog.e(TAG, "printing string: " + mPrintString);
                    byte[] array = mPrintString.getBytes();
                    ByteBuffer output_buffer = ByteBuffer
                            .allocate(array.length);
                    UsbRequest request = new UsbRequest();
                    request.initialize(conn, endPoint);
                    request.queue(output_buffer, array.length);
                    if (conn.requestWait() == request) {
                        LLog.e(TAG, "output_buffer: " + output_buffer.getChar(0) + "");
                        Message m = new Message();
                        m.obj = output_buffer.array();
                        // handler.sendMessage(m);
                        output_buffer.clear();
                    } else {
                        LLog.e(TAG, "No request recieved");
                    }

                    try{
                        int b = conn.bulkTransfer(endPoint, array, array.length, 10000);
                        LLog.e(TAG, "Return Status. b-->" + b);
                    }catch (Exception e){
                        LLog.e(TAG, "bulkTransfer. Error: " + e.getMessage());
                    }
                    // int transfered = conn.bulkTransfer(endPoint,
                    // myStringData.getBytes(),
                    // myStringData.getBytes().length, 5000);
                    // Log.i("Info", "Amount of data transferred : " +
                    // transfered);

                } catch (Exception e) {
                    LLog.e(TAG, "Unable to transfer bulk data");
                    e.printStackTrace();
                } finally {
                    try {
                        conn.releaseInterface(usbInterface);
                        LLog.e(TAG, "Interface released");
                        conn.close();
                        LLog.e(TAG, "Usb connection closed");
                        unregisterReceiver(mUsbReceiver);
                        LLog.e(TAG, "Brodcast reciever unregistered");
                    } catch (Exception e) {
                        LLog.e(TAG,
                                "Unable to release resources because : "
                                        + e.getMessage());
                        e.printStackTrace();
                    }
                }

            }
        });
    }*/
    /*************************************
     * PRIVATE CLASSES
     *************************************/
//    private class ParkingServer extends NanoHTTPD {
//
//        /*************************************
//         * PUBLIC METHODS
//         *************************************/
//        public ParkingServer() throws IOException {
//            super(PORT);
//            start();
//            System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
//        }
//
//        @Override
//        public Response serve(IHTTPSession session) {
//            try {
//                Method method = session.getMethod();
//                String uri = session.getUri();
//                Map<String, String> params = session.getParms();
//                String responseString = "Not Found";
//                if (uri.equalsIgnoreCase(SERVICE_NAME)) {
//                    responseString = serveParking(session, uri, method, params);
//                }
//                return new NanoHTTPD.Response(Response.Status.OK, MIME_JSON, responseString);
//
//            } catch (IOException ioe) {
//                return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
//            } catch (ResponseException re) {
//                return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
//            } catch (NotFoundException nfe) {
//                return new NanoHTTPD.Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
//            } catch (Exception ex) {
//                return new Response(Response.Status.INTERNAL_ERROR, MIME_HTML, "<html><body><h1>Error</h1>" + ex.toString() + "</body></html>");
//            }
//        }
//
//        private String serveParking(IHTTPSession session, String uri, Method method, Map<String, String> params) throws IOException, ResponseException {
//            LLog.e(TAG, "serveParking");
//            LLog.e(TAG, "uri: " + uri);
//            String responseString = "";
//            do {
//                if (Method.GET.equals(method)) {
//                    responseString = handleGet(session, params);
//                    break;
//                }
//
//                if (Method.POST.equals(method)) {
//                    responseString = handlePost(session);
//                    break;
//                }
//
//                throw new Resources.NotFoundException();
//
//            } while (false);
//
//            return responseString;
//        }
//
//        private String handleGet(IHTTPSession session, Map<String, String> params) {
//            LLog.e(TAG, "handleGet");
//            String date = params.get("date");
//            String time = params.get("time");
//            String number = params.get("number");
//
//            if (!(number.equalsIgnoreCase(mNumber))) {
//                StringBuilder buf = new StringBuilder();
//                buf.append("Number" + " : " + number + "\n");
//                buf.append("Time" + " : " + time + "\n");
//                buf.append("Date" + " : " + date + "\r\n");
//
////                mDate = date;
////                mTime = time;
//                mNumber = number;
//
//                try {
//                    if (clearStackTimer != null) {
//                        clearStackTimer.cancel();
//                        clearStackTimer.purge();
//                        clearStackTimer = null;
//                    }
//                    clearStackTimer = new Timer();
//                    clearStackTimer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            mNumber = "";
//                        }
//                    }, DELAY);
//                }catch (Exception e){
//                    LLog.e(TAG, "Timer error: " + e.getMessage());
//                }
//
//                printToLog(buf.toString());
//                doPrint(date, time, number);
////                printCommand(buf.toString());
//            }
//
//            return "GET";
//            //return clock.handleRequest("{'name':'status', 'value':''}");
//        }
//
//        private String handlePost(IHTTPSession session) throws IOException, ResponseException {
//            LLog.e(TAG, "handlePost");
//            Map<String, String> files = new HashMap<String, String>();
//            session.parseBody(files);
//
//            return "POST";
//            //return clock.handleRequest(files.get("postData"));
//        }
//
//        private void printToLog(final String log) {
//            LLog.e(TAG, "");
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    mLogTextView.setText(log);
//                }
//            });
//        }
//
//        private class NotFoundException extends RuntimeException {
//        }
//    }

    public class PrintTask extends AsyncTask<String, Integer, Void> {
        private Context mContext;
        private boolean isPrinterConnected;

        public PrintTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            mLogTextView.setText(mLogTextView.getText() + "\n" + "Start printing...");
        }

        @Override
        protected Void doInBackground(String... params) {

//            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.sergek);

            String dateString = params[0];
            String timeString = params[1];
            String numberString = params[2];
            /*// Get a PrintManager instance
            PrintManager printManager = (PrintManager) mContext
                    .getSystemService(Context.PRINT_SERVICE);

            // Set job name, which will be displayed in the print queue
            String jobName = getString(R.string.app_name) + " Document";

            // Start a print job, passing in a PrintDocumentAdapter implementation
            // to handle the generation of a print document
            printManager.print(jobName, new MyPrintDocumentAdapter(mContext),
                    null); //*/

            /*try {
                PrintOrder printer = new PrintOrder();
                printer.Print(mContext, printString, usbDeviceList[0]);
            }catch (Exception e){
                LLog.e(TAG, "doInBackground. Error: " + e.getMessage());
            }*/

            PrinterFont fntPrinterNormal = new PrinterFont();
            //open device
            isPrinterConnected = openDevice();
            if (openDevice() == false)
                return null;

            try {
                //Fill class: NORMAL
                fntPrinterNormal.setCharHeight(PrinterFont.FONT_SIZE_X1);                    //Height x1
                fntPrinterNormal.setCharWidth(PrinterFont.FONT_SIZE_X1);                    //Width x1
                fntPrinterNormal.setEmphasized(false);                                        //No Bold
                fntPrinterNormal.setItalic(false);                                            //No Italic
                fntPrinterNormal.setUnderline(false);                                        //No Underline
                fntPrinterNormal.setJustification(PrinterFont.FONT_JUSTIFICATION_CENTER);    //Center
                fntPrinterNormal.setInternationalCharSet(PrinterFont.FONT_CS_RUSSIAN);        //Default International Chars
            } catch (CustomException e) {
                //Show Error
                LLog.e(TAG, "doInBackground. Error: " + e.getMessage());
            } catch (Exception e) {
                LLog.e(TAG, "doInBackground. Set font properties error...");
            }

            try {
                //Print logo
//                prnDevice.printImage(image, CustomPrinter.IMAGE_ALIGN_TO_RIGHT, CustomPrinter.IMAGE_SCALE_NONE, 0);
                prnDevice.feed(1);
                //Print Text (NORMAL)
                prnDevice.printText("TItle", fntPrinterNormal);
                prnDevice.feed(1);
                prnDevice.printText(String.format("%s %s", "car number", numberString), fntPrinterNormal);
                prnDevice.feed(1);
                prnDevice.printText(String.format("%s %s %s", "time", dateString, timeString), fntPrinterNormal);
                prnDevice.feed(1);
                prnDevice.printText("test name", fntPrinterNormal);
                try
                {
                    //Feeds (3)
                    prnDevice.feed(5);
                    //Cut (Total)
                    prnDevice.cut(CustomPrinter.CUT_TOTAL);
                }
                catch(CustomException e )
                {
                    //Only if isn't unsupported
                    if (e.GetErrorCode() != CustomException.ERR_UNSUPPORTEDFUNCTION)
                    {
                        //Show Error
                        LLog.e(TAG, "doInBackground. Error cut feed: " + e.getMessage());
                    }
                }
                try
                {
                    //Present (40mm)
                    prnDevice.present(40);
                }
                catch(CustomException e )
                {
                    //Only if isn't unsupported
                    if (e.GetErrorCode() != CustomException.ERR_UNSUPPORTEDFUNCTION)
                    {
                        //Show Error
                        LLog.e(TAG, "doInBackground. Error present: " + e.getMessage());
                    }
                }
            } catch (CustomException e) {
                LLog.e(TAG, "doInBackground. Error print: " + e.getMessage());
                //Show Error
            } catch (Exception e) {
                LLog.e(TAG, "doInBackground. Print Text Error...");
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!isPrinterConnected){
                mLogTextView.setText(mLogTextView.getText() + "\n" + "Printer not connected!");
            }
            mLogTextView.setText(mLogTextView.getText() + "\n" + "Stop printing...");
        }
    }
}
