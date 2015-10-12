package barqsoft.footballscores.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.ViewHolder;

/**
 * Created by PC on 04.10.2015.
 */
public class FootballscoresWidgetRemoteViewsService extends RemoteViewsService {
    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.SCORES_TABLE + "." + DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.MATCH_DAY,
    };

    // these indices must match the projection
    public static final int COL_DATE = 1;
    public static final int COL_MATCHTIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_LEAGUE = 5;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_ID = 8;
    public static final int COL_MATCHDAY = 9;

    public FootballscoresWidgetRemoteViewsService()
    {
        int h=0;
    }
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor cursor = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null) {
                    cursor.close();
                }

                //just matches today
                String[] curDate=new String[1];
                curDate[0]=new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                String where=DatabaseContract.scores_table.DATE_COL+"=?";

                final long identityToken = Binder.clearCallingIdentity();
                Uri dataUri=DatabaseContract.BASE_CONTENT_URI;
                cursor=getContentResolver().query(dataUri,
                        SCORES_COLUMNS,
                        where,
                        curDate,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount() {
                int count=cursor == null ? 0 : cursor.getCount();
                return count;
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        cursor == null || !cursor.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_footballscores_detail_list_item);

                views.setTextViewText(R.id.home_team,cursor.getString(COL_HOME));
                views.setTextViewText(R.id.away_team,cursor.getString(COL_AWAY));
                views.setTextViewText(R.id.time, cursor.getString(COL_MATCHTIME));
                views.setTextViewText(R.id.score, Utilies.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_footballscores_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (cursor.moveToPosition(position))
                    return cursor.getLong(COL_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
