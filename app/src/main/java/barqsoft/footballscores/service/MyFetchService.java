package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;

public class MyFetchService extends IntentService {
    public static final String LOG_TAG = MyFetchService.class.getCanonicalName();

    public MyFetchService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getData(getString(R.string.next_param));
        getData(getString(R.string.past_param));
    }

    private void getData(String timeFrame) {
        Uri fetch_build = Uri.parse(getString(R.string.base_url)).buildUpon().
                appendQueryParameter(getString(R.string.time_frame), timeFrame).build();
        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        try {
            URL fetch = new URL(fetch_build.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod(getString(R.string.get_method));
            m_connection.addRequestProperty(getString(R.string.auth_token), getString(R.string.api_key));
            m_connection.connect();

            InputStream inputStream = m_connection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            if (buffer.length() == 0) {
                return;
            }
            JSON_data = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (m_connection != null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, getString(R.string.stream_error));
                }
            }
        }
        try {
            if (JSON_data != null) {
                JSONArray matches = new JSONObject(JSON_data).getJSONArray(getString(R.string.fixtures));
                if (matches.length() == 0) {
                    processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                    return;
                }
                processJSONdata(JSON_data, getApplicationContext(), true);
            } else {
                Log.d(LOG_TAG, getString(R.string.server_error));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void processJSONdata(String JSONdata, Context mContext, boolean isReal) {
        String League;
        String mDate;
        String mTime;
        String Home;
        String Away;
        String Home_goals;
        String Away_goals;
        String match_id;
        String match_day;


        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(getString(R.string.fixtures));
            Vector<ContentValues> values = new Vector<>(matches.length());
            for (int i = 0; i < matches.length(); i++) {
                JSONObject match_data = matches.getJSONObject(i);
                League = match_data.getJSONObject(getString(R.string.links)).getJSONObject(getString(R.string.soccer_season)).
                        getString(getString(R.string.href));
                League = League.replace(getString(R.string.season_link), "");
                if (League.equals(getString(R.string.premier)) ||
                        League.equals(getString(R.string.serie_a)) ||
                        League.equals(getString(R.string.bundesliga1)) ||
                        League.equals(getString(R.string.bundesliga2)) ||
                        League.equals(getString(R.string.primera_division))) {
                    match_id = match_data.getJSONObject(getString(R.string.links)).getJSONObject(getString(R.string.self)).
                            getString(getString(R.string.href));
                    match_id = match_id.replace(getString(R.string.match_link), "");
                    if (!isReal) {
                        match_id = match_id + Integer.toString(i);
                    }

                    mDate = match_data.getString(getString(R.string.match_date));
                    mTime = mDate.substring(mDate.indexOf(getString(R.string.t)) + 1, mDate.indexOf(getString(R.string.z)));
                    mDate = mDate.substring(0, mDate.indexOf(getString(R.string.t)));
                    SimpleDateFormat match_date = new SimpleDateFormat(getString(R.string.match_date_format), Locale.US);
                    match_date.setTimeZone(TimeZone.getTimeZone(getString(R.string.utc_timezone)));
                    try {
                        Date parseddate = match_date.parse(mDate + mTime);
                        SimpleDateFormat new_date = new SimpleDateFormat(getString(R.string.new_date_format), Locale.US);
                        new_date.setTimeZone(TimeZone.getDefault());
                        mDate = new_date.format(parseddate);
                        mTime = mDate.substring(mDate.indexOf(getString(R.string.colon)) + 1);
                        mDate = mDate.substring(0, mDate.indexOf(getString(R.string.colon)));

                        if (!isReal) {
                            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                            SimpleDateFormat mformat = new SimpleDateFormat(getString(R.string.real_date_format), Locale.US);
                            mDate = mformat.format(fragmentdate);
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    Home = match_data.getString(getString(R.string.home_team));
                    Away = match_data.getString(getString(R.string.away_team));
                    Home_goals = match_data.getJSONObject(getString(R.string.result)).getString(getString(R.string.home_goals));
                    Away_goals = match_data.getJSONObject(getString(R.string.result)).getString(getString(R.string.away_goals));
                    match_day = match_data.getString(getString(R.string.match_date));
                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID, match_id);
                    match_values.put(DatabaseContract.scores_table.DATE_COL, mDate);
                    match_values.put(DatabaseContract.scores_table.TIME_COL, mTime);
                    match_values.put(DatabaseContract.scores_table.HOME_COL, Home);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL, Away);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, Home_goals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, Away_goals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL, League);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY, match_day);
                    values.add(match_values);
                }
            }
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI, insert_data);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

    }
}

