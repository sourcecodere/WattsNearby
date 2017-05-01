package re.sourcecode.android.wattsnearby;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.TextView;


/**
 * Created by olem on 4/23/17.
 */

public class BottomSheetFragment extends BottomSheetDialogFragment {



    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };


    @Override
    public void setupDialog(Dialog dialog, int style) {


        View contentView;
        Bundle args = getArguments();

        if ((args != null) && (getArguments().containsKey(MainMapActivity.ARG_DETAIL_SHEET_STATION_ID))) { //user pushed a station
            Long stationId = getArguments().getLong(MainMapActivity.ARG_DETAIL_SHEET_STATION_ID);
            contentView = View.inflate(getContext(), R.layout.bottom_sheet_marker_station, null);
            TextView title = (TextView) contentView.findViewById(R.id.sheet_title);
            TextView sub_title = (TextView) contentView.findViewById(R.id.sheet_sub_title);
            title.setText("This is a station!"); // Todo get data from content provider
            sub_title.setText(stationId.toString());

        } else if ((args != null) && (getArguments().containsKey(MainMapActivity.ARG_DETAIL_SHEET_ABOUT))) {
          contentView = View.inflate(getContext(),R.layout.bottom_sheet_about, null);

        } else { // user pushed the car marker
            contentView = View.inflate(getContext(), R.layout.bottom_sheet_marker_car, null);
            TextView title = (TextView) contentView.findViewById(R.id.car_sheet_title);
            title.setText(getResources().getString(R.string.marker_current));
        }

        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);

        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);

    }
}
