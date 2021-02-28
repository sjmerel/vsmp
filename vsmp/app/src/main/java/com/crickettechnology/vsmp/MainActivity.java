package com.crickettechnology.vsmp;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity 
{

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_imageView = (ImageView) findViewById(R.id.imageView);
        m_frameLabel = (TextView) findViewById(R.id.frameLabel);
        m_messageLabel = (TextView) findViewById(R.id.messageLabel);

        // don't sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        restoreState();
        setError(null);

        // toggle label on tap
        m_frameLabel.setVisibility(View.INVISIBLE);
        m_imageView.setOnClickListener(
                new View.OnClickListener() 
                {
                    @Override
                    public void onClick(View view) 
                    {
                        m_frameLabel.setVisibility(m_frameLabel.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                    }
                }
        );

        // forward/back one frame
        Button forwardButton = (Button) findViewById(R.id.forwardButton);
        forwardButton.setOnClickListener(
                new View.OnClickListener() 
                {
                    @Override
                    public void onClick(View view) 
                    {
                        updateFrame(1);
                    }
                }
        );
        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(
                new View.OnClickListener() 
                {
                    @Override
                    public void onClick(View view) 
                    {
                        updateFrame(-1);
                    }
                }
        );

        //System.out.printf("media: %b\n", Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
        m_ejectReceiver = new MediaEjectReceiver();

        m_runnable = new Runnable()
                {
                    public void run()
                    {
                        updateFrame(1);
                    }
                };

/*
        showRawImage("black");
        m_handler.postDelayed(new Runnable() { public void run() { showRawImage("white"); } }, 1000);
        //m_handler.postDelayed(new Runnable() { public void run() { showRawImage("black"); } }, 2000);
        m_handler.postDelayed(new Runnable() { public void run() { showRawImage("gradient"); } }, 2000);


        ImageView testImageView = (ImageView) findViewById(R.id.testImageView);
        testImageView.setImageResource(getResources().getIdentifier("gradient_4bit", "raw", getPackageName()));
        */

    }

    private void showRawImage(String name)
    {
        m_imageView.setImageResource(getResources().getIdentifier(name, "raw", getPackageName()));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        m_ejectReceiver.unregister();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // dismiss lockscreen (e.g. if launched from boot)
        KeyguardManager kmgr = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = kmgr.newKeyguardLock("vsmp");
        lock.disableKeyguard();

        m_ejectReceiver.register();
        scanFiles();
    }

    ////////////////////////////////////////

    private ImageView m_imageView;
    private TextView m_frameLabel;
    private TextView m_messageLabel;

    private Runnable m_runnable;
    private Handler m_handler = new Handler();

    private int m_frame = 1; // current frame index
    private String m_dirPath = null; // full path of current dir
    private ArrayList<String> m_dirList = new ArrayList<>();

    ////////////////////////////////////////

    private String getFilename(int frame)
    {
        return String.format("img%05d.jpg", frame);
    }

    private void setError(String message)
    {
        if (message == null)
        {
            m_messageLabel.setVisibility(View.INVISIBLE);
        }
        else
        {
            m_handler.removeCallbacks(m_runnable);
            m_imageView.setImageBitmap(null);
            m_messageLabel.setVisibility(View.VISIBLE);
            m_messageLabel.setText(message);
        }
    }

    private void scanFiles()
    {
        System.out.println("scanning");
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            // no SD card
            setError("No SD card");
        }
        else
        {
            // get list of dirs containing image files with the expected filenames
            m_dirList.clear();
            File topDir = new File("/sdcard");
            for (File dir : topDir.listFiles()) 
            {
                if (dir.isDirectory())
                {
                    System.out.println("checking dir " + dir.getAbsolutePath());
                    String imagePath = String.format("%s/%s", dir.getAbsolutePath(), getFilename(1));
                    File imageFile = new File(imagePath);
                    if (imageFile.exists())
                    {
                        m_dirList.add(dir.getAbsolutePath());
                    }
                }
            }

            if (m_dirList.isEmpty())
            {
                setError("No image files");
            }
            else
            {
                if (m_dirPath == null || !m_dirList.contains(m_dirPath))
                {
                    // no current dir, or no longer present; choose first
                    m_dirPath = m_dirList.get(0);
                    m_frame = 1;
                }
                setError(null);
                updateFrame(0);
            }
        }
    }

    private void updateFrame(int delta)
    {
        m_frame += delta;
        if (m_frame < 1) m_frame = 1;

        String imagePath = String.format("%s/%s", m_dirPath, getFilename(m_frame));
        System.out.println("loading " + imagePath);
        File file = new File(imagePath);
        if (file.exists())
        {
            final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            // show black, then white, then new frame to prevent ghosting from previous image
            final int delayMs = 500;
            showRawImage("black");
            m_handler.postDelayed(new Runnable() { public void run() { showRawImage("white"); } }, delayMs);
            m_handler.postDelayed(new Runnable() { public void run() { m_imageView.setImageBitmap(bitmap); } }, delayMs*2);

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            String dateStr = formatter.format(date);
            m_frameLabel.setText(String.format("%s: %s", dateStr, imagePath));

            saveState();
        }
        else
        {
            // assume can't find file because we've finished the movie
            int dirIndex = m_dirList.indexOf(m_dirPath);
            ++dirIndex;
            if (dirIndex >= m_dirList.size())
            {
                dirIndex = 0;
            }
            m_dirPath = m_dirList.get(dirIndex);
            m_frame = 1;
            updateFrame(0);
        }

        m_handler.removeCallbacks(m_runnable);
        m_handler.postDelayed(m_runnable, 1000*frameSeconds);
    }

    // if movie is dumped at 24 fps, 60 seconds -> 1/60*24 = 1/1440 speed
    // if movie is dumped at 1 fps, 60 seconds -> 1/60 speed
    // if movie is dumped at 1 fps, 300 seconds -> 1/300 speed
    // 1 hour at 1/300 speed = 300 hours = 12 days
    private final int frameSeconds = 300;


    ////////////////////////////////////////
    // persistence

    private SharedPreferences getPrefs()
    {
        return getSharedPreferences("settings", 0);
    }

    private void saveState()
    {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putInt("frame", m_frame);
        editor.putString("dir", m_dirPath);
        editor.commit();
    }

    private void restoreState()
    {
        SharedPreferences prefs = getPrefs();
        m_frame = prefs.getInt("frame", 1);
        m_dirPath = prefs.getString("dir", null);
        //System.out.printf("saved state: %s %d\n", m_dirPath, m_frame);
    }


    ////////////////////////////////////////
    // detect SD card removed/inserted

    private class MediaEjectReceiver extends BroadcastReceiver
    {
        public void register()
        {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addDataScheme("file");
            MainActivity.this.registerReceiver(this, filter);
        }

        public void unregister()
        {
            MainActivity.this.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) 
        {
            System.out.println(intent.getAction());
            scanFiles();
        }
    }

    private MediaEjectReceiver m_ejectReceiver;
}
