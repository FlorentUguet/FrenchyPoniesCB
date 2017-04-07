package fr.fusoft.frenchyponiescb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Florent on 26/03/2017.
 */

public class FPUtils {

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Drawable loadDrawableFromUrl(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            return Drawable.createFromStream(is, "url");
        } catch (Exception e) {
            Log.w("PictureDL","Couldn't download " + url + " " + e.toString());
            return null;
        }
    }

    public static String cacheDrawable(Context c, Drawable d, String name){
        File cache = c.getCacheDir();
        File file = new File(cache,name);

        if(!file.exists()){
            saveDrawableToFile(c.getCacheDir(),name,d, Bitmap.CompressFormat.PNG,100);
        }

        return file.getAbsolutePath();
    }

    public static Drawable loadCachedDrawable(String path){
        return Drawable.createFromPath(path);
    }

    public static Drawable loadCachedDrawable(Context c, String name){
        File cache = c.getCacheDir();
        File file = new File(cache,name);
        if(file.exists()){
            return Drawable.createFromPath(file.getAbsolutePath());
        }else{
            return null;
        }
    }

    public static Bitmap drawableToBitmap(Drawable d) {
        Bitmap bitmap;

        if (d instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }


        if (d == null || d.getIntrinsicWidth() <= 0 || d.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        if (d != null) {
            d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            d.draw(canvas);
        }

        return bitmap;
    }

    public static boolean saveDrawableToFile(File dir, String fileName, Drawable d, Bitmap.CompressFormat format, int quality) {
        return saveBitmapToFile(dir, fileName, drawableToBitmap(d), format, quality);
    }

    public static boolean saveBitmapToFile(File dir, String fileName, Bitmap bm, Bitmap.CompressFormat format, int quality) {

        /*
        * Bitmap.CompressFormat can be PNG,JPEG or WEBP.
        *
        * quality goes from 1 to 100. (Percentage).
        *
        * dir you can get from many places like Environment.getExternalStorageDirectory() or mContext.getFilesDir()
        * depending on where you want to save the image.
        */

        File imageFile = new File(dir,fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bm.compress(format,quality,fos);
            fos.close();
            return true;
        }
        catch (IOException e) {
            Log.e("FileSaver",e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }
    public static boolean fileExists(String path){
        File file = new File(path);
        return file.exists();
    }

    public static boolean fileExists(File file){
        return file.exists();
    }

}
