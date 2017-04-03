package re.sourcecode.android.wattsnearby;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import re.sourcecode.android.wattsnearby.sync.WattsOCMSyncTask;

public class MainMapActivity extends AppCompatActivity {

    private static final String TAG = MainMapActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);


        WattsOCMSyncTask wattsOCMSyncTask = new WattsOCMSyncTask(this, 60.029265, 11.0952163, 2.0);
        wattsOCMSyncTask.execute();
    }
}
