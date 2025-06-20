package sync2app.com.syncapplive.additionalSettings.cloudAppsync.schedules;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sync2app.com.syncapplive.MyApplication;
import sync2app.com.syncapplive.R;
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods;
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.api.RetrofitClientJava;
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.models.AppSettings;
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.models.Schedule;
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.responses.ServerTimeResponse;
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.util.Common;
import sync2app.com.syncapplive.additionalSettings.utils.Constants;
import sync2app.com.syncapplive.databinding.ActivityAddNewScheduleBinding;

public class AddNewSchedule extends AppCompatActivity {

    //binding
    private ActivityAddNewScheduleBinding activity;

    //dynamic values

    private boolean isActivityRunning = true;

    //dialog
    private android.app.AlertDialog theDialog;
    private boolean isDialogShowing = false;

    //values
    private String selectedType = "";
    private String selectedScope = "";
    private boolean isSundayChecked = false;
    private boolean isMondayChecked = false;
    private boolean isTuesdayChecked = false;
    private boolean isWednesdayChecked = false;
    private boolean isThursdayChecked = false;
    private boolean isFridayChecked = false;
    private boolean isSaturdayChecked = false;
    private String selectedStartTime = "";
    private String selectedStopTime = "";
    private String selectedStartDate = "";
    private String selectedEndDate = "";

    //time picker dialog
    private int hour = 0;
    private int min = 0;

    //calendar
    private Calendar myCalendar = Calendar.getInstance();

    //folder
    private File scheduleFile;

    private AppSettings currentSettings;


    private static final String ANNOUNCE_HTML = "announce.html";
    private static final String ANNOUNCE_PATH = "Path to announce.html";


    private static final String TRAINIG_HTML = "training.html";
    private static final String TRAINIG_PATH = "Path to training.html";


    private static final String CUSTOM_INPUT = "Path to custom folder";
    private static final String CUSTOM_PATH_URL = "Path to custom url";
    private static final String HTTPS = "https://";
    private static final String HTTPS_PATH = "Url path to https://";
    private static final String HTTP_P = "http://";
    private static final String HTTP_PATH = "Url path to http://";

    private SharedPreferences preferences;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set binding
        activity = DataBindingUtil.setContentView(this, R.layout.activity_add_new_schedule);
        currentSettings = Paper.book().read(Common.CURRENT_SETTING);



        //init
        initialize();


    }


    private void setTextColor(TextView textView, int colorId) {
        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), colorId));
    }


    private void setDrawableColor(ImageView imageView, int drawableId, int colorId) {
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), drawableId);
        if (drawable != null) {
            drawable.setColorFilter(ContextCompat.getColor(getApplicationContext(), colorId), PorterDuff.Mode.SRC_IN);
            imageView.setImageDrawable(drawable);
        }
    }


    private void initialize() {

        //add exception
        Methods.addExceptionHandler(this);


        //set default
        setDefault();

        //back
        activity.backButton.setOnClickListener(v -> onBackPressed());

        //check file
        checkDirectory();

        //initialize day selects
        initializeDays();


        // orientation
        applyOritenation();

        activity.navMoveFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int scrollAmount = (int) (activity.horinZontalMove.getChildAt(0).getWidth() * 0.2);
                activity.horinZontalMove.post(new Runnable() {
                    @Override
                    public void run() {
                        activity.horinZontalMove.smoothScrollBy(scrollAmount, 0);
                    }
                });
            }
        });


        activity.navBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int scrollAmount = (int) (activity.horinZontalMove.getChildAt(0).getWidth() * 0.30);
                activity.horinZontalMove.post(new Runnable() {
                    @Override
                    public void run() {
                        activity.horinZontalMove.smoothScrollBy(-scrollAmount, 0);
                    }
                });
            }
        });


        //location
        activity.locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

                activity.setIsOnline(true);
                activity.redirectUrl.setSelection(activity.redirectUrl.getText().toString().length());

            } else {

                activity.setIsOnline(false);
                activity.redirectUrl.setSelection(activity.redirectUrl.getText().toString().length());


            }

        });

        //type
        activity.typeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {

                selectedType = Common.SCHEDULE_TYPE_SPECIFIC;
                activity.setIsNormal(false);

            } else {

                selectedType = Common.SCHEDULE_TYPE_NORMAL;
                activity.setIsNormal(true);

            }
        });

        //scope
        activity.scopeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedScope = Common.SCHEDULE_SCOPE_DAILY;
                activity.setIsWeekly(false);

                //set days
                checkEveryDay(false, true);
            } else {
                selectedScope = Common.SCHEDULE_SCOPE_WEEKLY;
                activity.setIsWeekly(true);

                //set days
                checkEveryDay(true, false);
            }
        });


        //start time
        activity.startTime.setOnClickListener(v -> {
            showStartTimeDialog();
        });

        //stop time
        activity.stopTime.setOnClickListener(v -> {
            showStopTimeDialog();
        });

        //start date
        activity.startDate.setOnClickListener(v -> {
            showStartDateDialog();
        });

        //end date
        activity.endDate.setOnClickListener(v -> {
            showEndDateDialog();
        });

        //add
        activity.addScheduleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getValue = activity.redirectUrl.getText().toString().trim();
                if (getValue.isEmpty()){
                    Toast.makeText(AddNewSchedule.this, "redirect url can not be empty", Toast.LENGTH_SHORT).show();
                }else {
                    validateParams();
                }
            }
        });

        // spanner
        activity.imagSpannerSavedDownload.setOnClickListener(v -> populateMyField());


        // set up background image
        SharedPreferences sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, Context.MODE_PRIVATE);
        String get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "");
        String get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "");
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(Constants.imageUseBranding)) {
            loadBackGroundImage();
        }


    }

    private void populateMyField() {

        if (activity.locationSwitch.isChecked()) {
            show_online_custom_pop_up();
        } else {
            show_Local_custom_pop_up();
        }

    }


    private void show_Local_custom_pop_up() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View bindingCm = inflater.inflate(R.layout.custom_select_redirect_url, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(bindingCm);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView textHTTP_Or_AnnounceText = bindingCm.findViewById(R.id.textHTTP_Or_AnnounceText);
        TextView textHTTPS_Or_Training_Text = bindingCm.findViewById(R.id.textHTTPS_Or_Training_Text);
        TextView textCustomSelection = bindingCm.findViewById(R.id.textCustomSelection);
        ImageView imageCrossClose = bindingCm.findViewById(R.id.imageCrossClose);
        ImageView close_bs = bindingCm.findViewById(R.id.close_bs);


        textHTTP_Or_AnnounceText.setText(ANNOUNCE_PATH);
        textHTTPS_Or_Training_Text.setText(TRAINIG_PATH);
        textHTTPS_Or_Training_Text.setText(TRAINIG_PATH);
        textCustomSelection.setText(CUSTOM_INPUT);

        textHTTP_Or_AnnounceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activity.redirectUrl.setText(ANNOUNCE_HTML);
                get_KeyBoardUi();
                alertDialog.dismiss();
            }
        });

        textHTTPS_Or_Training_Text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.redirectUrl.setText(TRAINIG_HTML);
                get_KeyBoardUi();
                alertDialog.dismiss();
            }
        });


        textCustomSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.redirectUrl.setText("");
                get_KeyBoardUi();
                alertDialog.dismiss();
            }
        });


        imageCrossClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        close_bs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        alertDialog.show();

    }


    private void show_online_custom_pop_up() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View bindingCm = inflater.inflate(R.layout.custom_select_redirect_url, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(bindingCm);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView textHTTP_Or_AnnounceText = bindingCm.findViewById(R.id.textHTTP_Or_AnnounceText);
        TextView textHTTP_Or_Training_Text = bindingCm.findViewById(R.id.textHTTPS_Or_Training_Text);
        TextView textCustomSelection = bindingCm.findViewById(R.id.textCustomSelection);
        ImageView imageCrossClose = bindingCm.findViewById(R.id.imageCrossClose);
        ImageView close_bs = bindingCm.findViewById(R.id.close_bs);


        textHTTP_Or_AnnounceText.setText(HTTPS_PATH);
        textHTTP_Or_Training_Text.setText(HTTP_PATH);
        textCustomSelection.setText(CUSTOM_PATH_URL);
        textHTTP_Or_AnnounceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activity.redirectUrl.setText(HTTPS);
                get_KeyBoardUi();
                alertDialog.dismiss();
            }
        });

        textHTTP_Or_Training_Text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.redirectUrl.setText(HTTP_P);
                get_KeyBoardUi();
                alertDialog.dismiss();
            }
        });


        textCustomSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.redirectUrl.setText("");
                get_KeyBoardUi();
                alertDialog.dismiss();
            }
        });


        imageCrossClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        close_bs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        alertDialog.show();

    }

    private void get_KeyBoardUi() {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(activity.redirectUrl.getWindowToken(), 0);
        }

        // Move cursor to the end
        activity.redirectUrl.setSelection(activity.redirectUrl.getText().length());
    }


    private void loadBackGroundImage() {
        String fileTypes = "app_background.png";
        SharedPreferences myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE);
        String getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "");
        String getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "");

        String pathFolder = "/" + getFolderClo + "/" + getFolderSubpath + "/" + Constants.App + "/" + "Config";
        String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/" + Constants.Syn2AppLive + "/" + pathFolder;
        File file = new File(folder, fileTypes);

        if (file.exists()) {
            Glide.with(this).load(file).centerCrop().into(activity.backgroundImage);
        }
        activity.backgroundImage.setVisibility(View.VISIBLE);
    }


    private void setDefault() {

        //set data
        //   activity.setSettings(currentSettings);

        //set defaults
        activity.setIsLoading(false);
        activity.setIsOnline(false);
        activity.setIsNormal(true);
        activity.setIsWeekly(true);

        //default
        selectedType = Common.SCHEDULE_TYPE_NORMAL;
        selectedScope = Common.SCHEDULE_SCOPE_WEEKLY;

        //set days
        checkEveryDay(true, false);

        //get current time
        getCurrentTimeForText();
    }


    private void getCurrentTimeForText() {
        SharedPreferences my_DownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE);
        String getCompany_id = my_DownloadClass.getString(Constants.getFolderClo, "");

        if (currentSettings.isUse_server_time()) {

            ///   Toast.makeText(getApplicationContext(), "Server time", Toast.LENGTH_SHORT).show();

            RetrofitClientJava
                    .getInstance(this)
                    .getApi()
                    .getServerTime(getCompany_id)
                    .enqueue(new Callback<ServerTimeResponse>() {
                        @Override
                        public void onResponse(Call<ServerTimeResponse> call, Response<ServerTimeResponse> response) {

                            if (response.code() == 200) {

                                //set text
                                activity.currentTime.setText(response.body().getTime());

                            } else {

                                //set text
                                activity.currentTime.setText("Api Error");

                            }

                        }

                        @Override
                        public void onFailure(Call<ServerTimeResponse> call, Throwable t) {

                            //set text
                            activity.currentTime.setText("Error Fetching Time");


                            Log.d("ScheduleStart", t.getMessage());

                        }
                    });




        } else {

            @SuppressLint("SimpleDateFormat") String currentTime = new SimpleDateFormat("HH:mm").format(System.currentTimeMillis());

            //set text
            activity.currentTime.setText(currentTime);

            //  Toast.makeText(getApplicationContext(), "Device time", Toast.LENGTH_SHORT).show();
        }

    }


    private void checkDirectory() {
        SharedPreferences my_DownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE);
        String company = my_DownloadClass.getString(Constants.getFolderClo, "");
        String license = my_DownloadClass.getString(Constants.getFolderSubpath, "");

        String USER_SCHEDULE_FOLDER = "Schedules";
        String LOCAL_SCHEDULE_FILE = "localSchedules.csv";


        String finalFolderPath = "/" + company + "/" + license;
        String Syn2AppLive = Constants.Syn2AppLive;

        File folder = new File(Environment.getExternalStorageDirectory().toString() + "/Download/" + Syn2AppLive + finalFolderPath);

        File scheduleFileFolder = new File(Environment.getExternalStorageDirectory().toString() + "/Download/" + Syn2AppLive + finalFolderPath + "/App/" + USER_SCHEDULE_FOLDER);

        scheduleFile = new File(scheduleFileFolder.getAbsolutePath(), LOCAL_SCHEDULE_FILE);

        if (folder.exists()) {

            if (!scheduleFile.exists()) {

                try (FileWriter writer = new FileWriter(scheduleFile, true)) {

                    StringBuilder sb = new StringBuilder();
                    sb.append("id");
                    sb.append(',');
                    sb.append("redirect_url");
                    sb.append(',');
                    sb.append("isDaily");
                    sb.append(',');
                    sb.append("isWeekly");
                    sb.append(',');
                    sb.append("isOneTime");
                    sb.append(',');
                    sb.append("day");
                    sb.append(',');
                    sb.append("startTime");
                    sb.append(',');
                    sb.append("stopTime");
                    sb.append(',');
                    sb.append("duration");
                    sb.append(',');
                    sb.append("date");
                    sb.append(',');
                    sb.append("priority");
                    sb.append('\n');

                    writer.write(sb.toString());

                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "File Not Available", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        } else {

            showInfoDialog("File Error", "Schedule Folder Missing, Please Contact Support");

        }


    }


    private void initializeDays() {

        //sunday
        activity.sunButton.setOnClickListener(v -> {

            //ui change
            if (isSundayChecked) {
                activity.sunIndicator.setBackgroundResource(R.drawable.unselected_day);
                activity.sunButton.setBackgroundResource(R.drawable.day_back);
                isSundayChecked = false;
            } else {
                activity.sunIndicator.setBackgroundResource(R.drawable.selected_day);
                activity.sunButton.setBackgroundResource(R.drawable.selected_day);
                isSundayChecked = true;
            }
        });

        //monday
        activity.monButton.setOnClickListener(v -> {

            //ui change
            if (isMondayChecked) {
                activity.monIndicator.setBackgroundResource(R.drawable.unselected_day);
                activity.monButton.setBackgroundResource(R.drawable.day_back);
                isMondayChecked = false;
            } else {
                activity.monIndicator.setBackgroundResource(R.drawable.selected_day);
                activity.monButton.setBackgroundResource(R.drawable.selected_day);
                isMondayChecked = true;
            }
        });

        //tuesday
        activity.tueButton.setOnClickListener(v -> {

            //ui change
            if (isTuesdayChecked) {
                activity.tueIndicator.setBackgroundResource(R.drawable.unselected_day);
                activity.tueButton.setBackgroundResource(R.drawable.day_back);
                isTuesdayChecked = false;
            } else {
                activity.tueIndicator.setBackgroundResource(R.drawable.selected_day);
                activity.tueButton.setBackgroundResource(R.drawable.selected_day);
                isTuesdayChecked = true;
            }
        });

        //wednesday
        activity.wedButton.setOnClickListener(v -> {

            //ui change
            if (isWednesdayChecked) {
                activity.wedIndicator.setBackgroundResource(R.drawable.unselected_day);
                activity.wedButton.setBackgroundResource(R.drawable.day_back);
                isWednesdayChecked = false;
            } else {
                activity.wedIndicator.setBackgroundResource(R.drawable.selected_day);
                activity.wedButton.setBackgroundResource(R.drawable.selected_day);
                isWednesdayChecked = true;
            }
        });

        //thursday
        activity.thuButton.setOnClickListener(v -> {

            //ui change
            if (isThursdayChecked) {
                activity.thuIndicator.setBackgroundResource(R.drawable.unselected_day);
                activity.thuButton.setBackgroundResource(R.drawable.day_back);
                isThursdayChecked = false;
            } else {
                activity.thuIndicator.setBackgroundResource(R.drawable.selected_day);
                activity.thuButton.setBackgroundResource(R.drawable.selected_day);
                isThursdayChecked = true;
            }
        });

        //friday
        activity.friButton.setOnClickListener(v -> {

            //ui change
            if (isFridayChecked) {
                activity.friIndicator.setBackgroundResource(R.drawable.unselected_day);
                activity.friButton.setBackgroundResource(R.drawable.day_back);
                isFridayChecked = false;
            } else {
                activity.friIndicator.setBackgroundResource(R.drawable.selected_day);
                activity.friButton.setBackgroundResource(R.drawable.selected_day);
                isFridayChecked = true;
            }
        });

        //saturday
        activity.satButton.setOnClickListener(v -> {

            //ui change
            if (isSaturdayChecked) {
                activity.satIndicator.setBackgroundResource(R.drawable.unselected_day);
                activity.satButton.setBackgroundResource(R.drawable.day_back);
                isSaturdayChecked = false;
            } else {
                activity.satIndicator.setBackgroundResource(R.drawable.selected_day);
                activity.satButton.setBackgroundResource(R.drawable.selected_day);
                isSaturdayChecked = true;
            }
        });

    }

    private void checkEveryDay(boolean isBtnActive, boolean isSelected) {

        //disable buttons
        activity.sunButton.setEnabled(isBtnActive);
        activity.monButton.setEnabled(isBtnActive);
        activity.tueButton.setEnabled(isBtnActive);
        activity.wedButton.setEnabled(isBtnActive);
        activity.thuButton.setEnabled(isBtnActive);
        activity.friButton.setEnabled(isBtnActive);
        activity.satButton.setEnabled(isBtnActive);

        //set select
        if (!isBtnActive) {

            activity.sunIndicator.setBackgroundResource(R.drawable.selected_day);
            activity.sunButton.setBackgroundResource(R.drawable.selected_day);

            activity.monIndicator.setBackgroundResource(R.drawable.selected_day);
            activity.monButton.setBackgroundResource(R.drawable.selected_day);

            activity.tueIndicator.setBackgroundResource(R.drawable.selected_day);
            activity.tueButton.setBackgroundResource(R.drawable.selected_day);

            activity.wedIndicator.setBackgroundResource(R.drawable.selected_day);
            activity.wedButton.setBackgroundResource(R.drawable.selected_day);

            activity.thuIndicator.setBackgroundResource(R.drawable.selected_day);
            activity.thuButton.setBackgroundResource(R.drawable.selected_day);

            activity.friIndicator.setBackgroundResource(R.drawable.selected_day);
            activity.friButton.setBackgroundResource(R.drawable.selected_day);

            activity.satIndicator.setBackgroundResource(R.drawable.selected_day);
            activity.satButton.setBackgroundResource(R.drawable.selected_day);

        } else {

            activity.sunIndicator.setBackgroundResource(R.drawable.unselected_day);
            activity.sunButton.setBackgroundResource(R.drawable.day_back);

            activity.monIndicator.setBackgroundResource(R.drawable.unselected_day);
            activity.monButton.setBackgroundResource(R.drawable.day_back);

            activity.tueIndicator.setBackgroundResource(R.drawable.unselected_day);
            activity.tueButton.setBackgroundResource(R.drawable.day_back);

            activity.wedIndicator.setBackgroundResource(R.drawable.unselected_day);
            activity.wedButton.setBackgroundResource(R.drawable.day_back);

            activity.thuIndicator.setBackgroundResource(R.drawable.unselected_day);
            activity.thuButton.setBackgroundResource(R.drawable.day_back);

            activity.friIndicator.setBackgroundResource(R.drawable.unselected_day);
            activity.friButton.setBackgroundResource(R.drawable.day_back);


            activity.satIndicator.setBackgroundResource(R.drawable.unselected_day);
            activity.satButton.setBackgroundResource(R.drawable.day_back);

        }

        //register check
        isSundayChecked = isSelected;
        isMondayChecked = isSelected;
        isTuesdayChecked = isSelected;
        isWednesdayChecked = isSelected;
        isThursdayChecked = isSelected;
        isFridayChecked = isSelected;
        isSaturdayChecked = isSelected;

    }

    private void showStartTimeDialog() {

        //create dialog
        android.app.AlertDialog startDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.time_picker_dialog, null);

        //widgets
        final TimePicker timePicker = viewOptions.findViewById(R.id.timePicker);
        final Button cancelBtn = viewOptions.findViewById(R.id.cancelBtn);
        final Button setBtn = viewOptions.findViewById(R.id.setBtn);

        //dialog props
        startDialog.setView(viewOptions);
        startDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        startDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //initialize time picker
        timePicker.setIs24HourView(true);
        Calendar calendar = Calendar.getInstance();
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {

            //get time
            hour = hourOfDay;
            min = minute;

        });

        //cancel
        cancelBtn.setOnClickListener(v -> {

            //reset
            hour = 0;
            min = 0;

            //dismiss
            startDialog.dismiss();

        });

        //grant access
        setBtn.setOnClickListener(v -> {

            //set time
            selectedStartTime = String.format("%02d:%02d", hour, min);
            activity.startTime.setText(String.format("%02d:%02d", hour, min));

            //reset
            hour = 0;
            min = 0;

            //close dialog
            startDialog.dismiss();
        });

        //show dialog
        startDialog.show();

    }

    private void showStopTimeDialog() {

        //create dialog
        android.app.AlertDialog stopDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.time_picker_dialog, null);

        //widgets
        final TimePicker timePicker = viewOptions.findViewById(R.id.timePicker);
        final Button cancelBtn = viewOptions.findViewById(R.id.cancelBtn);
        final Button setBtn = viewOptions.findViewById(R.id.setBtn);

        //dialog props
        stopDialog.setView(viewOptions);
        stopDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        stopDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //initialize time picker
        timePicker.setIs24HourView(true);
        Calendar calendar = Calendar.getInstance();
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {

            //get time
            hour = hourOfDay;
            min = minute;

        });

        //cancel
        cancelBtn.setOnClickListener(v -> {

            //reset
            hour = 0;
            min = 0;

            //dismiss
            stopDialog.dismiss();

        });

        //grant access
        setBtn.setOnClickListener(v -> {

            //set time
            selectedStopTime = String.format("%02d:%02d", hour, min);
            activity.stopTime.setText(String.format("%02d:%02d", hour, min));

            //reset
            hour = 0;
            min = 0;

            //close dialog
            stopDialog.dismiss();
        });

        //show dialog
        stopDialog.show();

    }

    private void showStartDateDialog() {

        //start date
        final DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {

            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //update birthday
            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            activity.startDate.setText(sdf.format(myCalendar.getTime()));
            selectedStartDate = sdf.format(myCalendar.getTime());

        };
        new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void showEndDateDialog() {

        //stop date
        final DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {

            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //update birthday
            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            activity.endDate.setText(sdf.format(myCalendar.getTime()));
            selectedEndDate = sdf.format(myCalendar.getTime());

        };
        new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void validateParams() {

        SharedPreferences my_DownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE);
        String company = my_DownloadClass.getString(Constants.getFolderClo, "");
        String license = my_DownloadClass.getString(Constants.getFolderSubpath, "");
        String Syn2AppLive = Constants.Syn2AppLive;

        String finalFolderPathDesired = "/" + company + "/" + license;

        //fetch strings
        String theUrl;
        if (!activity.locationSwitch.isChecked()) {

            //create path
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/" + Syn2AppLive + finalFolderPathDesired + "/App/";
            //create url
            theUrl = path + "/" + activity.redirectUrl.getText().toString().trim();


        } else {

            //create url
            theUrl = activity.redirectUrl.getText().toString().trim();

        }

        //validate
        if (TextUtils.isEmpty(theUrl)) {

            activity.redirectUrl.requestFocus();
            activity.redirectUrl.setError("Required");

        } else if (TextUtils.isEmpty(selectedStartTime)) {

            showInfoDialog("Start Time", "Please pick a start time for your schedule");

        } else if (TextUtils.isEmpty(selectedStopTime)) {

            showInfoDialog("End Time", "Please pick an end time for your schedule");

        } else if (selectedType.equals(Common.SCHEDULE_TYPE_SPECIFIC) && TextUtils.isEmpty(selectedStartDate)) {

            showInfoDialog("Start Date", "Please pick a start date for your schedule");

        } else if (selectedType.equals(Common.SCHEDULE_TYPE_SPECIFIC) && TextUtils.isEmpty(selectedEndDate)) {

            showInfoDialog("End Date", "Please pick an end date for your schedule");

        } else {

            if (isSundayChecked || isMondayChecked || isTuesdayChecked || isWednesdayChecked || isThursdayChecked || isFridayChecked || isSaturdayChecked) {

                setupScheduleList(theUrl);

            } else {

                showInfoDialog("Error", "At least, a day should be selected for schedule");

            }

        }

    }


    private void setupScheduleList(String theUrl) {

        //start loading
        activity.setIsLoading(true);


        Log.d("setupScheduleList", ": " + theUrl);

        //create list
        new Thread(() -> {


            if (selectedType.equals(Common.SCHEDULE_TYPE_NORMAL)) {

                if (selectedScope.equals(Common.SCHEDULE_SCOPE_WEEKLY)) {
                    if (isSundayChecked) {
                        setNormalWeeklySchedule(theUrl, Common.DAY_SUNDAY);
                    }

                    if (isMondayChecked) {
                        setNormalWeeklySchedule(theUrl, Common.DAY_MONDAY);
                    }

                    if (isTuesdayChecked) {
                        setNormalWeeklySchedule(theUrl, Common.DAY_TUESDAY);
                    }

                    if (isWednesdayChecked) {
                        setNormalWeeklySchedule(theUrl, Common.DAY_WEDNESDAY);
                    }

                    if (isThursdayChecked) {
                        setNormalWeeklySchedule(theUrl, Common.DAY_THURSDAY);
                    }

                    if (isFridayChecked) {
                        setNormalWeeklySchedule(theUrl, Common.DAY_FRIDAY);
                    }

                    if (isSaturdayChecked) {
                        setNormalWeeklySchedule(theUrl, Common.DAY_SATURDAY);
                    }

                    //cleanup after
                    onBackPressed();

                } else {

                    //write to csv
                    try (FileWriter writer = new FileWriter(scheduleFile, true)) {

                        //create
                        Schedule newRedirect = new Schedule(getRandomId(), theUrl, true, false, false, "", selectedStartTime, selectedStopTime, getTimeDifference(selectedStartTime, selectedStopTime), "", "Normal");

                        StringBuilder sb = new StringBuilder();
                        sb.append(newRedirect.getId());
                        sb.append(',');
                        sb.append(newRedirect.getRedirect_url());
                        sb.append(',');
                        sb.append(newRedirect.isDaily());
                        sb.append(',');
                        sb.append(newRedirect.isWeekly());
                        sb.append(',');
                        sb.append(newRedirect.isOneTime());
                        sb.append(',');
                        sb.append(newRedirect.getDay());
                        sb.append(',');
                        sb.append(newRedirect.getStartTime());
                        sb.append(',');
                        sb.append(newRedirect.getStopTime());
                        sb.append(',');
                        sb.append(newRedirect.getDuration());
                        sb.append(',');
                        sb.append(newRedirect.getDate());
                        sb.append(',');
                        sb.append(newRedirect.getPriority());
                        sb.append('\n');

                        writer.write(sb.toString());

                    } catch (FileNotFoundException e) {

                        //stop loading
                        activity.setIsLoading(false);

                        runOnUiThread(() -> {
                            //error
                            showInfoDialog("Error", "Schedule file not found, please restart this page or contact support");
                        });

                    } catch (IOException e) {
                        e.printStackTrace();

                        //stop loading
                        activity.setIsLoading(false);

                        runOnUiThread(() -> {
                            //error
                            showInfoDialog("Error", "Error : " + e.getMessage());
                        });

                    } catch (ParseException e) {
                        e.printStackTrace();
                    } finally {

                        onBackPressed();

                    }

                }

            } else {

                if (selectedScope.equals(Common.SCHEDULE_SCOPE_WEEKLY)) {
                    try {
                        //init date list
                        List<Date> dates = new ArrayList<Date>();

                        //init formatter
                        DateFormat formatter;

                        formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date startDateCalc = (Date) formatter.parse(selectedStartDate);
                        Date endDateCalc = (Date) formatter.parse(selectedEndDate);
                        long interval = 24 * 1000 * 60 * 60; // 1 day in millis
                        long endTime = endDateCalc.getTime(); // create your endtime here, possibly using Calendar or Date
                        long curTime = startDateCalc.getTime();
                        while (curTime <= endTime) {

                            if (isSundayChecked && today(new Date(curTime)).equals(Common.DAY_SUNDAY)) {

                                dates.add(new Date(curTime));

                            } else if (isMondayChecked && today(new Date(curTime)).equals(Common.DAY_MONDAY)) {

                                dates.add(new Date(curTime));

                            } else if (isTuesdayChecked && today(new Date(curTime)).equals(Common.DAY_TUESDAY)) {

                                dates.add(new Date(curTime));

                            } else if (isWednesdayChecked && today(new Date(curTime)).equals(Common.DAY_WEDNESDAY)) {

                                dates.add(new Date(curTime));

                            } else if (isThursdayChecked && today(new Date(curTime)).equals(Common.DAY_THURSDAY)) {

                                dates.add(new Date(curTime));

                            } else if (isFridayChecked && today(new Date(curTime)).equals(Common.DAY_FRIDAY)) {

                                dates.add(new Date(curTime));

                            } else if (isSaturdayChecked && today(new Date(curTime)).equals(Common.DAY_SATURDAY)) {

                                dates.add(new Date(curTime));

                            }

                            curTime += interval;
                        }

                        //loop between days to get
                        for (int i = 0; i < dates.size(); i++) {

                            Date lDate = (Date) dates.get(i);
                            String ds = formatter.format(lDate);

                            //write to csv
                            try (FileWriter writer = new FileWriter(scheduleFile, true)) {

                                //create
                                Schedule newRedirect = new Schedule(getRandomId(), theUrl, false, false, true, "", selectedStartTime, selectedStopTime, getTimeDifference(selectedStartTime, selectedStopTime), ds, "Normal");

                                StringBuilder sb = new StringBuilder();
                                sb.append(newRedirect.getId());
                                sb.append(',');
                                sb.append(newRedirect.getRedirect_url());
                                sb.append(',');
                                sb.append(newRedirect.isDaily());
                                sb.append(',');
                                sb.append(newRedirect.isWeekly());
                                sb.append(',');
                                sb.append(newRedirect.isOneTime());
                                sb.append(',');
                                sb.append(newRedirect.getDay());
                                sb.append(',');
                                sb.append(newRedirect.getStartTime());
                                sb.append(',');
                                sb.append(newRedirect.getStopTime());
                                sb.append(',');
                                sb.append(newRedirect.getDuration());
                                sb.append(',');
                                sb.append(newRedirect.getDate());
                                sb.append(',');
                                sb.append(newRedirect.getPriority());
                                sb.append('\n');

                                writer.write(sb.toString());

                            } catch (FileNotFoundException e) {
                                //stop loading
                                activity.setIsLoading(false);

                                runOnUiThread(() -> {
                                    //error
                                    showInfoDialog("Error", "Schedule file not found, please restart this page or contact support");
                                });

                            } catch (IOException e) {
                                e.printStackTrace();

                                //stop loading
                                activity.setIsLoading(false);

                                runOnUiThread(() -> {
                                    //error
                                    showInfoDialog("Error", "Error : " + e.getMessage());
                                });


                            } catch (ParseException e) {
                                e.printStackTrace();
                            } finally {

                                onBackPressed();

                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("DailySpecificSchedule", e.getMessage());
                    }

                } else {

                    try {
                        //init date list
                        List<Date> dates = new ArrayList<Date>();

                        //init formatter
                        DateFormat formatter;

                        formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date startDateCalc = (Date) formatter.parse(selectedStartDate);
                        Date endDateCalc = (Date) formatter.parse(selectedEndDate);
                        long interval = 24 * 1000 * 60 * 60; // 1 day in millis
                        long endTime = endDateCalc.getTime(); // create your endtime here, possibly using Calendar or Date
                        long curTime = startDateCalc.getTime();
                        while (curTime <= endTime) {
                            dates.add(new Date(curTime));
                            curTime += interval;
                        }

                        //loop between days to get
                        for (int i = 0; i < dates.size(); i++) {

                            Date lDate = (Date) dates.get(i);
                            String ds = formatter.format(lDate);

                            //write to csv
                            try (FileWriter writer = new FileWriter(scheduleFile, true)) {

                                //create
                                Schedule newRedirect = new Schedule(getRandomId(), theUrl, false, false, true, "", selectedStartTime, selectedStopTime, getTimeDifference(selectedStartTime, selectedStopTime), ds, "Normal");

                                StringBuilder sb = new StringBuilder();
                                sb.append(newRedirect.getId());
                                sb.append(',');
                                sb.append(newRedirect.getRedirect_url());
                                sb.append(',');
                                sb.append(newRedirect.isDaily());
                                sb.append(',');
                                sb.append(newRedirect.isWeekly());
                                sb.append(',');
                                sb.append(newRedirect.isOneTime());
                                sb.append(',');
                                sb.append(newRedirect.getDay());
                                sb.append(',');
                                sb.append(newRedirect.getStartTime());
                                sb.append(',');
                                sb.append(newRedirect.getStopTime());
                                sb.append(',');
                                sb.append(newRedirect.getDuration());
                                sb.append(',');
                                sb.append(newRedirect.getDate());
                                sb.append(',');
                                sb.append(newRedirect.getPriority());
                                sb.append('\n');

                                writer.write(sb.toString());

                            } catch (FileNotFoundException e) {
                                //stop loading
                                activity.setIsLoading(false);

                                runOnUiThread(() -> {
                                    //error
                                    showInfoDialog("Error", "Schedule file not found, please restart this page or contact support");
                                });

                            } catch (IOException e) {
                                e.printStackTrace();

                                //stop loading
                                activity.setIsLoading(false);

                                runOnUiThread(() -> {
                                    //error
                                    showInfoDialog("Error", "Error : " + e.getMessage());
                                });
                            } catch (ParseException e) {
                                e.printStackTrace();
                            } finally {

                                onBackPressed();

                            }


                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("DailySpecificSchedule", e.getMessage());
                    }


                }

            }

        }).start();

    }

    private void setNormalWeeklySchedule(String theUrl, String theDay) {

        //write to csv
        try (FileWriter writer = new FileWriter(scheduleFile, true)) {

            //create
            Schedule newRedirect = new Schedule(getRandomId(), theUrl, false, true, false, theDay, selectedStartTime, selectedStopTime, getTimeDifference(selectedStartTime, selectedStopTime), "", "Normal");

            StringBuilder sb = new StringBuilder();
            sb.append(newRedirect.getId());
            sb.append(',');
            sb.append(newRedirect.getRedirect_url());
            sb.append(',');
            sb.append(newRedirect.isDaily());
            sb.append(',');
            sb.append(newRedirect.isWeekly());
            sb.append(',');
            sb.append(newRedirect.isOneTime());
            sb.append(',');
            sb.append(newRedirect.getDay());
            sb.append(',');
            sb.append(newRedirect.getStartTime());
            sb.append(',');
            sb.append(newRedirect.getStopTime());
            sb.append(',');
            sb.append(newRedirect.getDuration());
            sb.append(',');
            sb.append(newRedirect.getDate());
            sb.append(',');
            sb.append(newRedirect.getPriority());
            sb.append('\n');

            writer.write(sb.toString());

        } catch (FileNotFoundException e) {
            //stop loading
            activity.setIsLoading(false);

            runOnUiThread(() -> {
                //error
                showInfoDialog("Error", "Schedule file not found, please restart this page or contact support");
            });

        } catch (IOException e) {
            e.printStackTrace();

            //stop loading
            activity.setIsLoading(false);

            runOnUiThread(() -> {
                //error
                showInfoDialog("Error", "Error : " + e.getMessage());
            });

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private String today(Date date) {

        //get today in millis
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

    }

    private String getTimeDifference(String startTime, String stopTime) throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        Date startDate = simpleDateFormat.parse(startTime);
        Date endDate = simpleDateFormat.parse(stopTime);

        long difference = endDate.getTime() - startDate.getTime();
        if (difference < 0) {
            Date dateMax = simpleDateFormat.parse("24:00");
            Date dateMin = simpleDateFormat.parse("00:00");
            difference = (dateMax.getTime() - startDate.getTime()) + (endDate.getTime() - dateMin.getTime());
        }
        int days = (int) (difference / (1000 * 60 * 60 * 24));
        int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
        int min = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
        int sec = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours) - (1000 * 60 * min)) / (1000);
        //Log.i("log_tag","Hours: "+hours+", Mins: "+min+", Secs: "+sec);

        return String.valueOf(difference);
    }

    public static String getRandomId() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 5;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();
        return generatedString;
    }

    private void showInfoDialog(String title, String message) {

        //change state
        isDialogShowing = true;

        //create dialog
        theDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.general_info_dialog, null);

        //widget
        TextView dialogTitle = viewOptions.findViewById(R.id.dialogTitle);
        TextView dialogText = viewOptions.findViewById(R.id.dialogText);
        TextView okayBtn = viewOptions.findViewById(R.id.okayBtn);

        //dialog props
        theDialog.setView(viewOptions);
        theDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        theDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //set dialog listener
        theDialog.setOnCancelListener(dialogInterface -> isDialogShowing = false);
        theDialog.setOnDismissListener(dialogInterface -> isDialogShowing = false);

        //lock dialog
        theDialog.setCancelable(true);
        theDialog.setCanceledOnTouchOutside(true);

        //set message
        dialogTitle.setText(title);
        dialogText.setText(message);

        //okay
        okayBtn.setOnClickListener(view -> theDialog.dismiss());

        //show dialog
        if (isActivityRunning) {
            theDialog.show();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //activity tag
        isActivityRunning = false;
    }



    @SuppressLint("SourceLockedOrientationActivity")
    private void applyOritenation() {

        SharedPreferences sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE);
        String getState = sharedBiometric.getString(Constants.IMG_TOGGLE_FOR_ORIENTATION, "").toString();


        if (getState == Constants.USE_POTRAIT){

            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        }else if (getState == Constants.USE_LANDSCAPE){
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        }else if (getState == Constants.USE_UNSEPECIFIED){
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        }

    }



    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ScheduleMediaActivity.class);
        startActivity(intent);
        finish();
    }

}