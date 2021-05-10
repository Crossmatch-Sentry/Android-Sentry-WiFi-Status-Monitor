package com.crossmatch.wifistatuslogger;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class
FirstFragment extends Fragment {
    private static String LOG_TAG = "WiFiConnectionMonitor";
    private static String LOG_FILE = "WiFi_Monitor.log";
    private static String LOG_FILE_BACKUP = "WiFi_Monitor-1.log";
    private static int LOG_FILE_MAX_SIZE = 1 * 1024 * 1024;     // 1 MB max

    TextView tvConsole;
    Context myContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myContext = view.getContext();

        tvConsole = view.findViewById(R.id.textview_console);
        tvConsole.setText("Wi-Fi Monitor\n");

        NetworkUtil.checkNetworkInfo(myContext, new NetworkUtil.OnConnectionStatusChange() {
            @Override
            public void onChange(boolean type) {
                String toDisplay;
                if(type){
                    toDisplay = "Wi-Fi Available";
                } else {
                    toDisplay = "Wi-Fi Lost";
                }
                Log.i(LOG_TAG, toDisplay);

                //we can't modify UI on this thread, so do on UI runnable thread
                setText(tvConsole, toDisplay);

            }
        });

    }

    // to modify UI from fragment we need the Activity that its running on (handler)
    private void setText(final TextView text, final String status) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                text.setText(status);
                logToFile(myContext, LOG_TAG, status);

                if ( MainActivity.getPopUpStatus() ) {
                    Toast.makeText(myContext, status, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    /**
     * Gets a stamp containing the current date and time to write to the log.
     * @return The stamp for the current date and time.
     */
    private static String getDateTimeStamp()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        return (dateFormat.format(new Date()));

/*
        Date dateNow = Calendar.getInstance().getTime();
        // My locale, so all the log files have the same date and time format
        return (DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US).format(dateNow));
*/
    }

    private static File getAbsoluteFile(String relativePath, Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return new File(context.getExternalFilesDir(null), relativePath);
        } else {
            return new File(context.getFilesDir(), relativePath);
        }
    }

    private static void logToFile(Context context, String logMessageTag, String logMessage) {
        try
        {
            boolean appendFile = true;

            // Gets the log file from the root of the primary storage. If it does
            // not exist or is > allowed max size, the file is created anew and the
            // current file is renamed to
            //File logFile = new File(Environment.getExternalStorageDirectory(), LOG_FILE);
            File logFile = getAbsoluteFile(LOG_FILE, context);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            int file_size = Integer.parseInt(String.valueOf(logFile.length()));
            if (file_size > LOG_FILE_MAX_SIZE) {
                // for log rotation rename existing log file and then create new file
                File logFileBackup = getAbsoluteFile(LOG_FILE_BACKUP, context);
                logFile.renameTo(logFileBackup);
                appendFile = false;
            }
            // Write the message to the log with a timestamp
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, appendFile));
            writer.write(String.format("%1s [%2s]:%3s\r\n", getDateTimeStamp(), logMessageTag, logMessage));
            writer.close();
            // Refresh the data so it can seen when the device is plugged in a
            // computer. You may have to unplug and replug to see the latest
            // changes
            MediaScannerConnection.scanFile(context,
                    new String[] { logFile.toString() },
                    null,
                    null);

        }
        catch (IOException e)
        {
            Log.i(LOG_TAG, "Unable to log to external file."+e.getMessage());
        }
    }

}
