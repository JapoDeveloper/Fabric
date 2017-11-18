package co.japo.fabric.ui.util;

import android.content.Context;
import android.support.v7.app.AlertDialog;


/**
 * Created by japodeveloper on 11/12/17.
 */

public class ViewUtility {

    private ViewUtility(){}

    public static void displayTextPopup(String title, String message, Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.create().show();
    }

}
