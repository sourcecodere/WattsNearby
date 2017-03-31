package re.sourcecode.wattsnearby;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainMapActivity extends AppCompatActivity {

    private static final String TAG = MainMapActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
    }
}
