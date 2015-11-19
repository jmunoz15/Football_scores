package barqsoft.footballscores;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TodayWidgetService extends IntentService {

    private String[] dateValues = new String[1];

    public TodayWidgetService() {
        super(TodayWidgetService.class.getCanonicalName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat(getString(R.string.real_date_format));
        dateValues[0] = mformat.format(date);
        Cursor data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, null, dateValues, null);
        if (data == null)
            return;
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_today;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            views.setTextViewText(R.id.home_name, data.getString(scoresAdapter.COL_HOME));
            views.setTextViewText(R.id.away_name, data.getString(scoresAdapter.COL_AWAY));
            views.setTextViewText(R.id.score_textview,
                    Utilies.getScores(data.getInt(scoresAdapter.COL_HOME_GOALS),
                            data.getInt(scoresAdapter.COL_AWAY_GOALS)));

            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }


    }
}
