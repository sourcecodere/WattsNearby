package re.sourcecode.android.wattsnearby.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import re.sourcecode.android.wattsnearby.MainMapActivity;
import re.sourcecode.android.wattsnearby.R;
import re.sourcecode.android.wattsnearby.SettingsActivity;


/**
 * Created by SourcecodeRe on 4/23/17.
 *
 * Generic bottom sheet fragment used for car details and about data
 *
 */
public class BottomSheetGenericFragment extends BottomSheetDialogFragment {

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

        if ((getArguments()!=null) && getArguments().containsKey(MainMapActivity.ARG_DETAIL_SHEET_ABOUT)) {
            // about from appbar
            contentView = View.inflate(getContext(), R.layout.fragment_about, null);

        } else { // user pushed the car marker
            contentView = View.inflate(getContext(), R.layout.fragment_car, null);
            TextView title = contentView.findViewById(R.id.car_sheet_title);
            title.setText(getResources().getString(R.string.marker_current));
            ImageButton settingsBtn = contentView.findViewById(R.id.btn_settings);
            settingsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // launch the settings activity
                    Intent intent = new Intent(getContext(), SettingsActivity.class);
                    startActivity(intent);
                }
            });

        }

        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);

        }
    }

}
