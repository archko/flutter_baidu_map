package com.archko.map.baidu_map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author archko
 */
public class SelectCityActivity extends Activity {

    public static void start(Activity context) {
        context.startActivityForResult(new Intent(context, SelectCityActivity.class), REQUEST_CODE);
    }

    public static final int REQUEST_CODE = 1;
    public static final String EXTRA_KEY_IN = "city_in";
    public static final String EXTRA_KEY_OUT = "city_out";
    View back;
    private ListView cityListView;
    private CityAdapter cityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city_activity);
        intializeView();
        intializeData();
    }

    private void intializeView() {
        cityListView = findViewById(R.id.city_list);
        back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            finish();
        });
    }

    private void intializeData() {
        cityAdapter = new CityAdapter(this);
        cityListView.setAdapter(cityAdapter);
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city = (City) cityAdapter.getItem(position);
                setSelectedResult(city);
            }
        });
        loadCityList();
    }

    protected void setSelectedResult(City city) {
        if (city != null && !TextUtils.isEmpty(city.id)) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_KEY_OUT, city);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private static class CityAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater layoutInflater;
        private List<City> mData;

        public CityAdapter(Context context) {
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
            mData = new ArrayList<>();
        }

        public List<City> getData() {
            return mData;
        }

        public void setData(List<City> mData) {
            if (mData != null) {
                this.mData = mData;
            }
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.item_select_city, null);
                holder = new Holder();
                holder.nameTV = convertView.findViewById(R.id.name);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.nameTV.setText(mData.get(position).name);
            return convertView;
        }

        class Holder {
            TextView nameTV;
        }
    }

    private void loadCityList() {
        AsyncTask<Object, Object, List<City>> asyncTask = new AsyncTask<Object, Object, List<City>>() {

            @Override
            protected List<City> doInBackground(Object... objects) {
                return doLoadCityList();
            }

            @Override
            protected void onPostExecute(List<City> o) {
                cityAdapter.setData(o);
                cityAdapter.notifyDataSetChanged();
            }
        };
        asyncTask.execute();
    }

    private List<City> doLoadCityList() {
        try {
            InputStream inputStream = getAssets().open("city.json");
            String content = StreamUtils.readStringFromInputStream(inputStream);
            return parseCityList(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<City> parseCityList(String content) {
        List<City> cityList = new ArrayList<>();
        try {
            JSONArray ja = new JSONArray(content);
            for (int i = 0; i < ja.length(); i++) {
                cityList.add(parseCity(ja.optJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("citys:" + cityList);
        return cityList;
    }

    private City parseCity(JSONObject jsonObject) {
        City city = new City();
        city.id = jsonObject.optString("id");
        city.name = jsonObject.optString("name");
        return city;
    }
}
