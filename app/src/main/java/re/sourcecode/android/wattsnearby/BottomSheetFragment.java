package re.sourcecode.android.wattsnearby;

import android.app.Dialog;
import android.os.Bundle;
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
        //super.setupDialog(dialog, style);
        View contentView;
        Long stationId = getArguments().getLong(MainMapActivity.ARG_DETAIL_SHEET_STATION_ID);

        if (stationId != null) { //user pushed a station
            contentView = View.inflate(getContext(), R.layout.fragment_bottom_sheet_station, null);
            TextView title = (TextView) contentView.findViewById(R.id.station_sheet_title);
            TextView text = (TextView) contentView.findViewById(R.id.station_sheet_text);
            title.setText("This is a station!");
            text.setText(stationId.toString());

        } else { // user pushed the car
            contentView = View.inflate(getContext(), R.layout.fragment_bottom_sheet_car, null);
            TextView title = (TextView) contentView.findViewById(R.id.car_sheet_title);
            title.setText("This is the car!");

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
