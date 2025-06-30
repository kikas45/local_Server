package sync2app.com.syncapplive.additionalSettings.cloudAppsync.schedules;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import io.paperdb.Paper;
import sync2app.com.syncapplive.R;
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods;
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.adapters.ScheduleAdapter;
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.models.Schedule;
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.util.Common;
import sync2app.com.syncapplive.additionalSettings.utils.Constants;
import sync2app.com.syncapplive.databinding.ActivityScheduleListBinding;

public class ScheduleList extends AppCompatActivity {
    // ViewBinding instance
    private ActivityScheduleListBinding binding;
    // dynamic values
    private boolean isActivityRunning = true;
    private boolean isClearAll = false;
    private boolean isDeleteOne = false;

    // dialog
    private android.app.AlertDialog theDialog;
    private android.app.AlertDialog theDialogSchedule;
    private boolean isDialogShowing = false;

    private List<Schedule> tempList = new ArrayList<>();
    private List<Schedule> scheduleList = new ArrayList<>();
    private ScheduleAdapter adapter;
    // data
    private File scheduleFile;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the binding
        binding = ActivityScheduleListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        applyOritenation();


        // Initialize the activity
        initialize();
    }

    private void initialize() {
        // Add exception handler
        Methods.addExceptionHandler(this);

        // Set up the back button click listener
        binding.backButton.setOnClickListener(v -> onBackPressed());

        // Check the directory and initialize the file
        checkDirectory();
        initializeFile();

        // Load schedules
        loadSchedules();

        // Clear schedules button click listener
        binding.clearSchedules.setOnClickListener(v -> {
            isClearAll = true;
            isDeleteOne = false;
            showChoiceDialog("Clear Schedules", "Please note that this will only clear the schedules locally set on your device. Are you sure you want to proceed?", "Yes", "No");
        });

        // Set up background image if needed
        SharedPreferences sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, Context.MODE_PRIVATE);
        String get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "");
        String get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "");
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(Constants.imageUseBranding)) {
            loadBackGroundImage();
        }
    }

    private void loadBackGroundImage() {
        SharedPreferences sharedP = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE);
        String getFolderClo = sharedP.getString(Constants.getFolderClo, "");
        String getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "");

        File baseDir = getExternalFilesDir(null); // App-private external storage
        String relativePath = "Syn2AppLive/" + getFolderClo + "/" + getFolderSubpath + "/" + Constants.App + "/Config";
        File folder = new File(baseDir, relativePath);
        String fileTypes = "app_background.png";
        File file = new File(folder, fileTypes);

        if (file.exists()) {
            Glide.with(this).load(file).centerCrop().into(binding.backgroundImage);
        }
    }

    private void checkDirectory() {

        SharedPreferences my_DownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE);
        String company = my_DownloadClass.getString(Constants.getFolderClo, "");
        String license = my_DownloadClass.getString(Constants.getFolderSubpath, "");
        String Syn2AppLive = Constants.Syn2AppLive;
        String USER_SCHEDULE_FOLDER = "Schedules";
        String LOCAL_SCHEDULE_FILE = "localSchedules.csv";

        // ✅ Safe app-private external directory
        File baseFolder = new File(getExternalFilesDir(null), Syn2AppLive + "/" + company + "/" + license);

        File scheduleFileFolder = new File(baseFolder, "App/" + USER_SCHEDULE_FOLDER);
        if (!scheduleFileFolder.exists()) {
            scheduleFileFolder.mkdirs(); // Make sure the schedule folder exists
        }

        scheduleFile = new File(scheduleFileFolder, LOCAL_SCHEDULE_FILE);

        if (baseFolder.exists()) {
            if (!scheduleFile.exists()) {
                try (FileWriter writer = new FileWriter(scheduleFile, true)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("id").append(',');
                    sb.append("redirect_url").append(',');
                    sb.append("isDaily").append(',');
                    sb.append("isWeekly").append(',');
                    sb.append("isOneTime").append(',');
                    sb.append("day").append(',');
                    sb.append("startTime").append(',');
                    sb.append("stopTime").append(',');
                    sb.append("duration").append(',');
                    sb.append("date").append(',');
                    sb.append("priority").append('\n');

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


    private void initializeFile() {

        SharedPreferences my_DownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE);
        String company = my_DownloadClass.getString(Constants.getFolderClo, "");
        String license = my_DownloadClass.getString(Constants.getFolderSubpath, "");
        String Syn2AppLive = Constants.Syn2AppLive;
        String LOCAL_SCHEDULE_FILE = "localSchedules.csv";
        String ONLINE_SCHEDULE_FILE = "onlineSchedules.csv"; // ⚠️ Don't prefix with '/' here
        String USER_SCHEDULE_FOLDER = "Schedules";

        // ✅ Use app-private external storage path
        File basePath = new File(getExternalFilesDir(null), Syn2AppLive + "/" + company + "/" + license + "/App/" + USER_SCHEDULE_FOLDER);

        if (basePath.exists()) {
            // Switch file based on saved preference
            String savedState = Paper.book().read(Common.set_schedule_key, Common.schedule_online);

            if (Common.schedule_online.equals(savedState)) {
                scheduleFile = new File(basePath, ONLINE_SCHEDULE_FILE);
            } else {
                scheduleFile = new File(basePath, LOCAL_SCHEDULE_FILE);
            }

        } else {
            showInfoDialogScheduleNotFound("Error", "Schedule folder is missing, please re-sync or contact support");
        }
    }


    private void loadSchedules() {

        binding.scheduleRecycler.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        binding.scheduleRecycler.setLayoutManager(layoutManager);

        adapter = new ScheduleAdapter(scheduleList, this, this);
        binding.scheduleRecycler.setAdapter(adapter);

        //fetch schedules
        fetchSchedules();

    }

    private void fetchSchedules() {

        //read csv
        try {
            CSVReader reader = new CSVReader(new FileReader(scheduleFile));
            String[] nextLine;
            int count = 0;

            //skip first line
            reader.readNext();

            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                count++;


                try {
                    scheduleList.add(new Schedule(nextLine[0], nextLine[1], Boolean.parseBoolean(nextLine[2].toLowerCase()), Boolean.parseBoolean(nextLine[3].toLowerCase()), Boolean.parseBoolean(nextLine[4].toLowerCase()), nextLine[5], nextLine[6], nextLine[7], nextLine[8], nextLine[9], nextLine[10]));

                } catch (Exception e) {
                }

            }

            //notify adapter
            adapter.notifyDataSetChanged();

        } catch (IOException e) {

            showInfoDialog("CSV Error", "Error: " + e.getMessage());

        }

    }

    public void deleteSchedule(Schedule schedule, int position) {

        //situation
        isClearAll = false;
        isDeleteOne = true;

        //show choice
        showChoiceDialog("Clear Schedules", "Please note that this will only clear the schedules locally set on your device. Are you sure you want to proceed?", "Yes", "No", schedule, position);


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


    private void showInfoDialogScheduleNotFound(String title, String message) {


        //create dialog
        theDialogSchedule = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.general_info_dialog, null);

        //widget
        TextView dialogTitle = viewOptions.findViewById(R.id.dialogTitle);
        TextView dialogText = viewOptions.findViewById(R.id.dialogText);
        TextView okayBtn = viewOptions.findViewById(R.id.okayBtn);

        //dialog props
        theDialogSchedule.setView(viewOptions);
        theDialogSchedule.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        theDialogSchedule.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        //lock dialog
        theDialogSchedule.setCancelable(true);
        theDialogSchedule.setCanceledOnTouchOutside(true);

        //set message
        dialogTitle.setText(title);
        dialogText.setText(message);

        //okay
        okayBtn.setOnClickListener(view -> theDialogSchedule.dismiss());

        //show dialog
        if (isActivityRunning) {
            theDialogSchedule.show();
        }

    }

    private void showChoiceDialog(String title, String message, String positive, String negative) {

        //change state
        isDialogShowing = true;

        //create dialog
        theDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.general_choice_dialog, null);

        //widget
        TextView dialogTitle = viewOptions.findViewById(R.id.dialogTitle);
        TextView dialogText = viewOptions.findViewById(R.id.dialogText);
        TextView negativeBtn = viewOptions.findViewById(R.id.negativeBtn);
        TextView positiveBtn = viewOptions.findViewById(R.id.positiveBtn);

        //dialog props
        theDialog.setView(viewOptions);
        theDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        theDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //set dialog listener
        theDialog.setOnCancelListener(dialogInterface -> isDialogShowing = false);
        theDialog.setOnDismissListener(dialogInterface -> isDialogShowing = false);

        //lock dialog
        theDialog.setCancelable(false);
        theDialog.setCanceledOnTouchOutside(false);

        //set message
        dialogTitle.setText(title);
        dialogText.setText(message);
        negativeBtn.setText(negative);
        positiveBtn.setText(positive);

        //okay
        negativeBtn.setOnClickListener(view -> theDialog.dismiss());
        positiveBtn.setOnClickListener(view -> {

            //dismiss
            theDialog.dismiss();

            //switch location based on user pref
            String savedState = Paper.book().read(Common.set_schedule_key, Common.schedule_online);

            if (!Common.schedule_online.equals(savedState)) {

                //clear schedule
                scheduleFile.delete();

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

                //finish
                onBackPressed();

            } else {

                showInfoDialog("Error", "Sorry, you cannot clear schedules while you are in online mode.");

            }

        });

        //show dialog
        if (isActivityRunning) {
            theDialog.show();
        }

    }

    private void showChoiceDialog(String title, String message, String positive, String negative, Schedule schedule, int position) {

        //change state
        isDialogShowing = true;

        //create dialog
        theDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.general_choice_dialog, null);

        //widget
        TextView dialogTitle = viewOptions.findViewById(R.id.dialogTitle);
        TextView dialogText = viewOptions.findViewById(R.id.dialogText);
        TextView negativeBtn = viewOptions.findViewById(R.id.negativeBtn);
        TextView positiveBtn = viewOptions.findViewById(R.id.positiveBtn);

        //dialog props
        theDialog.setView(viewOptions);
        theDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        theDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //set dialog listener
        theDialog.setOnCancelListener(dialogInterface -> isDialogShowing = false);
        theDialog.setOnDismissListener(dialogInterface -> isDialogShowing = false);

        //lock dialog
        theDialog.setCancelable(false);
        theDialog.setCanceledOnTouchOutside(false);

        //set message
        dialogTitle.setText(title);
        dialogText.setText(message);
        negativeBtn.setText(negative);
        positiveBtn.setText(positive);

        //okay
        negativeBtn.setOnClickListener(view -> theDialog.dismiss());
        positiveBtn.setOnClickListener(view -> {

            //dismiss
            theDialog.dismiss();

            //switch location based on user pref
            String savedState = Paper.book().read(Common.set_schedule_key, Common.schedule_online);

            if (!Common.schedule_online.equals(savedState)) {

                //get correct id to delet
                int positionToRemove = position + 1;

                //remove schedule
                try {
                    CSVReader reader2 = new CSVReader(new FileReader(scheduleFile));
                    List<String[]> allElements = reader2.readAll();
                    allElements.remove(positionToRemove);
                    FileWriter sw = new FileWriter(scheduleFile);
                    CSVWriter writer = new CSVWriter(sw);
                    writer.writeAll(allElements);
                    writer.close();

                    //remove from file
                    scheduleList.remove(schedule);
                    adapter.notifyDataSetChanged();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {

                showInfoDialog("Error", "Sorry, you cannot delete an schedule.");

            }

        });

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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

        //dialog
        if (isDialogShowing)
            theDialog.dismiss();

        //start main
        funcloseActivity();


    }

    private void funcloseActivity() {
        Intent intent = new Intent(getApplicationContext(), ScheduleMediaActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void applyOritenation() {

        SharedPreferences sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE);
        String getState = sharedBiometric.getString(Constants.IMG_TOGGLE_FOR_ORIENTATION, "").toString();


        if (getState == Constants.USE_POTRAIT) {

            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        } else if (getState == Constants.USE_LANDSCAPE) {
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else if (getState == Constants.USE_UNSEPECIFIED) {
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        }

    }


}