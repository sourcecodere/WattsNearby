package re.sourcecode.android.wattsnearby.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import re.sourcecode.android.wattsnearby.fragment.BottomSheetStationFragment;
import re.sourcecode.android.wattsnearby.R;
import re.sourcecode.android.wattsnearby.utilities.WattsAccessibilityUtils;
import re.sourcecode.android.wattsnearby.utilities.WattsImageUtils;


/**
 * Created by olem on 5/8/17.
 */

public class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ConnectionAdapterViewHolder> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    private Cursor mCursor;


    public ConnectionAdapter(Context context) {
       mContext = context;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (like ours does) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new ConnectionAdapterViewHolder that holds the View for each list item
     */
    @Override
    public ConnectionAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.bottom_sheet_station_adapter, viewGroup, false);

        itemView.setFocusable(true);
        return new ConnectionAdapterViewHolder(itemView);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the stations
     * connection details for this particular position, using the "position" argument that is
     * conveniently passed into us.
     *
     * @param holder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given station in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ConnectionAdapterViewHolder holder, int position) {

        mCursor.moveToPosition(position);

        int conType = mCursor.getInt(BottomSheetStationFragment.INDEX_CONN_TYPE_ID);
        holder.connIconView.setImageDrawable(
                WattsImageUtils.getConnectionIcon(mContext, conType)
        );
        holder.connIconView.setContentDescription(
                WattsAccessibilityUtils.getConnectionDescription(mContext, conType)
        );

        holder.connTitleView.setText(
                mCursor.getString(
                        BottomSheetStationFragment.INDEX_CONN_TITLE)
        );

        holder.connLevelTitleView.setText(
                mCursor.getString(BottomSheetStationFragment.INDEX_CONN_LEVEL_TITLE)
        );

        holder.connCurrentView.setText(
                mCursor.getString(BottomSheetStationFragment.INDEX_CONN_CURRENT_TYPE_TITLE)
        );
        holder.connPowerView.setText(
                String.format(mContext.getString(R.string.connection_power),
                        mCursor.getDouble(BottomSheetStationFragment.INDEX_CONN_VOLT),
                        mCursor.getDouble(BottomSheetStationFragment.INDEX_CONN_AMP),
                        mCursor.getDouble(BottomSheetStationFragment.INDEX_CONN_KW)
                )
        );
        if (mCursor.getInt(BottomSheetStationFragment.INDEX_CONN_LEVEL_FAST) == 1) {
            holder.connFastView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_fast));
            holder.connFastView.setContentDescription(mContext.getString(R.string.fast_charger));
        } else {
            holder.connFastView.setVisibility(View.INVISIBLE);
        }


    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }


    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a connection item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class ConnectionAdapterViewHolder extends RecyclerView.ViewHolder {

        final ImageView connIconView;
        final ImageView connFastView;
        final TextView connTitleView;
        final TextView connLevelTitleView;
        final TextView connCurrentView;
        final TextView connPowerView;


        ConnectionAdapterViewHolder(View view) {
            super(view);

            connIconView = (ImageView) view.findViewById(R.id.connection_icon);
            connFastView = (ImageView) view.findViewById(R.id.connection_fast);
            connTitleView = (TextView) view.findViewById(R.id.connection_title);
            connLevelTitleView = (TextView) view.findViewById(R.id.connection_level_title);
            connCurrentView = (TextView) view.findViewById(R.id.connection_current);
            connPowerView = (TextView) view.findViewById(R.id.connection_power);
        }
    }

    /**
     * Swaps the cursor used by the ForecastAdapter for its weather data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }



}
