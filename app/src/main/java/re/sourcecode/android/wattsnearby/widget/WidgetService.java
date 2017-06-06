package re.sourcecode.android.wattsnearby.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by SourcecodeRe on 5/14/17.
 *
 * WidgetService is the {@link RemoteViewsService} that will return our RemoteViewsFactory
 */
public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDataProvider(this, intent);
    }
}
