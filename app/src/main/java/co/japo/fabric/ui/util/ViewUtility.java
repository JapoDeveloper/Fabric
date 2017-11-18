package co.japo.fabric.ui.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.LightingColorFilter;
import android.support.v7.app.AlertDialog;

import co.japo.fabric.R;


/**
 * Created by japodeveloper on 11/12/17.
 */

public class ViewUtility {

    private ViewUtility(){}

    public static void displayTextPopup(String title, String message, Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        Dialog myDialog = builder.create();
        myDialog.getWindow().getDecorView().getBackground().setColorFilter(new LightingColorFilter(0xFF000000,
                context.getResources().getColor(R.color.colorAccent)));
        myDialog.show();
    }

}
