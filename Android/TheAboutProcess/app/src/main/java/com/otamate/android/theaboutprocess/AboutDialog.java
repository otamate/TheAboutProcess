/*
 * Copyright (c) 2015 OTAMate Technology Ltd. All Rights Reserved.
 * http://www.otamate.com
 */
package com.otamate.android.theaboutprocess;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AboutDialog extends android.app.AlertDialog.Builder {
    private static final String TAG = AboutDialog.class.getSimpleName();


    public AboutDialog(Context context) {
        super(context);

        String versionStr = "Unknown";
        int versionCode = 0;

        try {
            PackageInfo pInfo =  context.getApplicationContext().getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionStr = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (Exception e) {
            Log.e(TAG, "PackageInfo error: " + e.getMessage());
        }

        Date buildDate;

        try {
            buildDate = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.ENGLISH).parse(BuildConfig.BUILD_DATE);
        } catch(ParseException pe) {
            throw new IllegalArgumentException();
        }

        long buildDateLong = buildDate.getTime();
        long expireDateLong = buildDateLong + (Constants.DAYS_AFTER_BUILD_TO_EXPIRE_MS);
        String aboutMessageStr1 = context.getString(R.string.about_message1, versionStr
                + (BuildConfig.BUILD_TYPE.equals("release") ? "" :
                "\n" + versionCode
                        + " " + BuildConfig.FLAVOR
                        + " " + BuildConfig.BUILD_TYPE
                        + "\nBLT: " + BuildConfig.BUILD_DATE
                        + (BuildConfig.BUILD_EXPIRES ? "\nEXP: " + new SimpleDateFormat(Constants.DATE_FORMAT, Locale.ENGLISH).format(new Date(expireDateLong)) : "")
                        + "\nGEN:"
                        + " " + BuildConfig.USERNAME
        ));

        setTitle(context.getString(R.string.pref_summary_about, context.getString(R.string.app_name)));

        ScrollView aboutContent = (ScrollView) ((LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.about_content, null, false);
        TextView aboutMessage1 =  aboutContent.findViewById(R.id.aboutMessage1);
        TextView aboutMessage2 = aboutContent.findViewById(R.id.aboutMessage2);
        TextView aboutLinks = aboutContent.findViewById(R.id.aboutLinks);
        ImageView OTAMateIcon =  aboutContent.findViewById(R.id.aboutOTAMateIcon);

        RotateAnimation rotate = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);

        rotate.setInterpolator(new LinearInterpolator());
        rotate.setDuration(10000);
        rotate.setRepeatCount(Animation.INFINITE);
        OTAMateIcon.setAnimation(rotate);

        aboutMessage1.setText(aboutMessageStr1);
        aboutLinks.setMovementMethod(LinkMovementMethod.getInstance());
        aboutMessage2.setMovementMethod(LinkMovementMethod.getInstance());

        setView(aboutContent);

        setPositiveButton("Rate us!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                rateApp();
            }
        });

        setNeutralButton(getContext().getString(R.string.pref_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            aboutContent.findViewById(R.id.separator1).setVisibility(View.GONE);
            aboutContent.findViewById(R.id.separator2).setVisibility(View.GONE);
        }

        setCancelable(true);

    }

    public void rateApp() {
        try {
            Intent rateIntent = rateIntentForUrl("market://details");
            getContext().startActivity(rateIntent);
        } catch (ActivityNotFoundException e) {
            Intent rateIntent = rateIntentForUrl("http://play.google.com/store/apps/details");
            getContext().startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getContext().getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        } else {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);

        return intent;
    }
}
