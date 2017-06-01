package re.sourcecode.android.wattsnearby.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import re.sourcecode.android.wattsnearby.MainMapActivity;
import re.sourcecode.android.wattsnearby.R;
import re.sourcecode.android.wattsnearby.data.ChargingStationContract;

/**
 * WidgetDataProvider acts as the adapter for the collection view widget,
 * providing RemoteViews to the widget in the getViewAt method.
 */
public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "WidgetDataProvider";

    private List<String> mCollection = new ArrayList<>();
    private Context mContext = null;
    private Cursor mCursor = null;

    /* The data we need to get for each list item */
    private static final String[] STATION_LIST_PROJECTION = {
            ChargingStationContract.StationEntry.COLUMN_ID,
            ChargingStationContract.StationEntry.COLUMN_OPERATOR_TITLE,
            ChargingStationContract.StationEntry.COLUMN_ADDR_TITLE,
            ChargingStationContract.StationEntry.COLUMN_ADDR_TOWN,
            ChargingStationContract.StationEntry.COLUMN_FAVORITE
    };
    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    private static final int INDEX_ID = 0;
    private static final int INDEX_OPERTOR_TITLE = 1;
    private static final int INDEX_ADDR_TITLE = 2;
    private static final int INDEX_ADDR_TOWN = 3;
    private static final int INDEX_FAVORITE = 4;


    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        if (mCursor == null) {
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {

        String title = "";
        String where = "";
        Long stationId = null;


        if (position == AdapterView.INVALID_POSITION ||
                mCursor == null) {
            return null;
        }

        if (mCursor.moveToPosition(position)) {
            stationId = mCursor.getLong(INDEX_ID);
            if (mCursor.getString(INDEX_ADDR_TITLE) != null) {
                title = mCursor.getString(INDEX_ADDR_TITLE);
            } else {
                title = mCursor.getString(INDEX_OPERTOR_TITLE);
            }
            where = mCursor.getString(INDEX_ADDR_TOWN);
        }

        // fetch layout
        RemoteViews view = new RemoteViews(
                mContext.getPackageName(),
                R.layout.widget_station_item);

        view.setTextViewText(R.id.widget_item_title, title);
        view.setTextViewText(R.id.widget_item_where, where);


        // click, set extra data.
        if (stationId != null) {
            final Intent fillInIntent = new Intent();
            fillInIntent.putExtra(MainMapActivity.ARG_WIDGET_INTENT_KEY, stationId);
            view.setOnClickFillInIntent(R.id.list_item, fillInIntent);
        }

        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    private void initData() {
        mCollection.clear();
        for (int i = 1; i <= 10; i++) {
            mCollection.add("ListView item " + i);
        }
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = mContext.getContentResolver().query(
                ChargingStationContract.StationEntry.CONTENT_URI,
                STATION_LIST_PROJECTION,
                ChargingStationContract.StationEntry.COLUMN_FAVORITE + "=?",
                new String[]{"1"},
                null);
    }

}
