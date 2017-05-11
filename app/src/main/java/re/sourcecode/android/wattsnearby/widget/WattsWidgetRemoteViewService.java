package re.sourcecode.android.wattsnearby.widget;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import re.sourcecode.android.wattsnearby.MainMapActivity;
import re.sourcecode.android.wattsnearby.R;
import re.sourcecode.android.wattsnearby.data.ChargingStationContract;

/**
 * Created by olem on 5/11/17.
 */

public class WattsWidgetRemoteViewService extends RemoteViewsService {



    /* The data we need to get for each list item */
    public static final String[] STATION_LIST_PROJECTION = {
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
    public static final int INDEX_ID = 0;
    public static final int INDEX_OPERTOR_TITLE = 1;
    public static final int INDEX_ADDR_TITLE = 2;
    public static final int INDEX_ADDR_TOWN = 3;
    public static final int INDEX_FAVORITE = 4;
    /**
     * To be implemented by the derived service to generate appropriate factories for
     * the data.
     *
     * @param intent
     */
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor mCursor = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (mCursor != null) {
                    mCursor.close();
                }
                final long identityToken = Binder.clearCallingIdentity();

                ContentResolver wattContentResolver = getContentResolver();

                mCursor = wattContentResolver.query(
                        ChargingStationContract.StationEntry.CONTENT_URI,
                        STATION_LIST_PROJECTION,
                        ChargingStationContract.StationEntry.COLUMN_FAVORITE + "=?",
                        new String[] {"1"},
                        null);

                Binder.restoreCallingIdentity(identityToken);
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
                int stationId;

                stationId = mCursor.getInt(INDEX_ID);

                if (position == AdapterView.INVALID_POSITION ||
                        mCursor == null) {
                    return null;
                }

                if (mCursor.moveToPosition(position)) {
                    if (mCursor.getString(INDEX_OPERTOR_TITLE) != null) {
                        title = mCursor.getString(INDEX_OPERTOR_TITLE);
                    } else {
                        title = mCursor.getString(INDEX_ADDR_TITLE);
                    }
                    where = mCursor.getString(INDEX_ADDR_TOWN);
                }

                // fetch layout
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_station_item);

                views.setTextViewText(R.id.widget_item_title, title);
                views.setTextViewText(R.id.widget_item_where, where);


                // click, set extra data.
                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(MainMapActivity.ARG_WIDGET_INTENT_KEY, stationId);
                views.setOnClickFillInIntent(R.id.list_item, fillInIntent);

                return views;
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
                return position; //returns item position
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

}
