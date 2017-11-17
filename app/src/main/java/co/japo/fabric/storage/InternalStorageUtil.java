package co.japo.fabric.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by japodeveloper on 11/16/17.
 */

public class InternalStorageUtil {

    public static File getAppMediaStorageDir(Context context){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + context.getApplicationContext().getPackageName()
                + "/Files");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        return mediaStorageDir;
    }

    public static File saveImage(Bitmap finalBitmap, boolean scaled, Context context) {

        File mediaStorageDir = getAppMediaStorageDir(context);
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String mImageName="Image_"+ timeStamp +".jpg";

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);

        try {
            FileOutputStream out = new FileOutputStream(mediaFile);
            if(scaled) {
                Bitmap scaledBmp = Bitmap.createScaledBitmap(finalBitmap, 1920, 1080, false);
                scaledBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }else{
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaFile;
    }


}
