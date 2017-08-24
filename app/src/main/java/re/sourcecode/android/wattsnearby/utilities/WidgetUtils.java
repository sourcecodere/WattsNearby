package re.sourcecode.android.wattsnearby.utilities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import re.sourcecode.android.wattsnearby.widget.CollectionWidget;

/**
 * Created by SourcecodeRe on 5/14/17.
 *
 * Util for WattsNearby widget
 */
public class WidgetUtils {

    /*
    * To refresh the list of favorites in the collection widget
    *
    */
    public static void sendRefreshBroadcast(Context context) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(context, CollectionWidget.class));
        context.sendBroadcast(intent);
    }
}
