package fr.fusoft.frenchyponiescb.workers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import fr.fusoft.frenchyponiescb.FPUtils;

/**
 * Created by Florent on 31/03/2017.
 */

public class URLImageParser implements Html.ImageGetter {
    Context context;
    View container;

    /***
     * Construct the URLImageParser which will execute AsyncTask and refresh the container
     * @param t
     * @param c
     */
    public URLImageParser(View t, Context c) {
        this.context = c;
        this.container = t;
    }

    public Drawable getDrawable(String source) {
        URLDrawable urlDrawable = new URLDrawable();

        // get the actual source
        ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(urlDrawable);

        asyncTask.execute(source);

        // return reference to URLDrawable where I will change with actual image from
        // the src tag
        return urlDrawable;
    }

    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
        URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable d) {
            this.urlDrawable = d;
        }

        protected Drawable doInBackground(String... urls) {
            String urlDisplay = urls[0];
            String cacheFile = FPUtils.md5(urlDisplay);

            Drawable d = FPUtils.loadCachedDrawable(context,cacheFile);

            if(d == null){
                d = FPUtils.loadDrawableFromUrl(urlDisplay);
                FPUtils.cacheDrawable(context,d,cacheFile);
            }

            return d;
        }

        @Override
        protected void onPostExecute(Drawable result) {

            if(result == null)
            {
                Log.e("Html ImageGetter","Returned bitmap was empty");
            }


            // set the correct bound according to the result from HTTP call
            urlDrawable.setBounds(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());

            // change the reference of the current drawable to the result
            // from the HTTP call
            urlDrawable.drawable = result;

            // redraw the image by invalidating the container
            URLImageParser.this.container.postInvalidate();
        }


    }
}
