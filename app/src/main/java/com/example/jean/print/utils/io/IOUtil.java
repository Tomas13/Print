package com.example.jean.print.utils.io;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;

import com.example.jean.print.utils.debug.LLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;


public class IOUtil {

    private static final String TAG = IOUtil.class.getSimpleName();
    public static final int BITMAP_SAMPLE_COEFFICIENT = 2;

    // ---

    public static void saveStringToFile(String content, String fileName) {
        File file = new File(fileName);
        InputStream stream = new ByteArrayInputStream(content.getBytes());
        try {
            saveFile(stream, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendStringToFile(String content, String fileName) {

        if (!isFileExist(fileName)) {
            return;
        }

        try {
            FileWriter fw = new FileWriter(fileName, true);
            fw.write(content + "\n");
            fw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    public static void saveFile(InputStream in, File file) throws IOException {
        final FileOutputStream out = new FileOutputStream(file);
        try {
            final byte[] bytes = new byte[32000];
            for (int a = in.read(bytes); a >= 0; a = in.read(bytes)) {
                out.write(bytes, 0, a);
            }
        } finally {
            try {
                out.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveFile(InputStream in, String fileName) throws IOException {
        saveFile(in, new File(fileName));
    }

    public static void saveFile(byte[] bytes, File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(bytes);
        } finally {
            try {
                out.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveBitmapToFilePng(Bitmap bitmap, File file) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveBitmapToFileJpg(Bitmap bitmap, File file, int quality) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveBitmapToFilePng(Bitmap bitmap, String fileName) throws IOException {
        saveBitmapToFilePng(bitmap, new File(fileName));
    }

    public static void saveBitmapToFileJpg(Bitmap bitmap, String fileName, int quality) throws IOException {
        saveBitmapToFileJpg(bitmap, new File(fileName), quality);
    }

    public static void savePicDefault(Context c, Bitmap bitmap, String fileName) {
        try {
            saveBitmapToFileJpg(bitmap, new File(getDataStoragePath(c) + File.separator + fileName), 100);
        } catch (IOException e) {
            LLog.e(TAG, "Exception - " + e);
        }
    }

    public static byte[] consume(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] bytes = new byte[64000];
        for (int a = in.read(bytes); a >= 0; a = in.read(bytes)) {
            baos.write(bytes, 0, a);
        }
        return baos.toByteArray();
    }

    public static String getFileName(String urlOrPath) {
        int ind = urlOrPath.lastIndexOf('/');
        return ind >= 0 ? urlOrPath.substring(ind + 1) : urlOrPath;
    }

    public static byte[] readFile(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream(10000);
        final byte[] bytes = new byte[32000];
        for (int a = in.read(bytes); a >= 0; a = in.read(bytes)) {
            out.write(bytes, 0, a);
        }
        in.close();
        return out.toByteArray();
    }

    public static boolean isFileExist(String fileName) {
        return new File(fileName).exists();
    }

    public static JSONObject loadJSONFromFile(String fileName) {

        try {

            FileInputStream is = new FileInputStream(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new JSONObject(new String(buffer, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String loadStringFromFile(String fileName) {
        LLog.e(TAG, "loadStringFromFile");

        try {

            FileInputStream is = new FileInputStream(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");

        } catch (IOException e) {
            LLog.e(TAG, "IOException - " + e);
            e.printStackTrace();
            return null;

        } catch (OutOfMemoryError e) {
            LLog.e(TAG, "OutOfMemoryError - " + e);
            return null;

        }

    }

    public static void createFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    public static void createFile(String filePath) {
        File file = new File(filePath);
        try {
            file.createNewFile();
        } catch (IOException e) {
            LLog.e(TAG, "IOException - " + e);
            e.printStackTrace();
        }
    }

    public static Bitmap loadBitmapFromFileWithSampleCoeff(String fileName) {
        File file = new File(fileName);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = BITMAP_SAMPLE_COEFFICIENT;

        if (file.exists()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        }
        return null;

    }

    public static String getDataStoragePath(Context context) {
        return context.getExternalCacheDir().getPath();
    }

    public static String getDataStoragePathOld(Context context) {
        /**
         * that method works very slow
         * here  we checking if external storage is available.
         * I am not sure about situations how it can be unavailable
         * so for that time I will just always use external storage
         * and will fix it if there will be buggy.
         */
        LLog.e(TAG, "getDataStoragePath");

        Boolean externalStorageMounted = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        Boolean externalStorageNotRemovable = !Environment.isExternalStorageRemovable();

        File cacheDir;

        if ((externalStorageMounted || externalStorageNotRemovable) && context.getApplicationContext().getExternalCacheDir() != null) {
            LLog.e(TAG, "getDataStoragePath - external");
            cacheDir = context.getExternalCacheDir();
        } else {
            LLog.e(TAG, "getDataStoragePath - usual");
            cacheDir = context.getCacheDir();
        }

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        LLog.e(TAG, "getDataStoragePath - " + cacheDir.getPath());
        return cacheDir.getPath();

    }

    public static File createTemporaryFile(String folderPath, String name, String extension) throws Exception {

        File tempDir = new File(folderPath);
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        return File.createTempFile(name, extension, tempDir);
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        boolean deleted = file.delete();
    }


    public static BitmapDrawable getBitmapDrawableFromAsset(Context context, String filePath) {
        Bitmap bitmap = getBitmapFromAsset(context, filePath);
        if (bitmap == null) {
            return null;
        }
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            return null;
        }

        return bitmap;
    }

}
