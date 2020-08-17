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
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private QuickLocationBar mQuicLocationBar;

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
        mQuicLocationBar = findViewById(R.id.city_loactionbar);
        mQuicLocationBar.setOnTouchLitterChangedListener(new LetterListViewListener());
        TextView city_dialog = findViewById(R.id.city_dialog);
        mQuicLocationBar.setTextDialog(city_dialog);
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

    private class LetterListViewListener implements QuickLocationBar.OnTouchLetterChangedListener {

        @Override
        public void touchLetterChanged(String s) {
            Map<String, Integer> alphaIndexer = cityAdapter.getCityMap();
            if (alphaIndexer.get(s) != null) {
                int position = alphaIndexer.get(s);
                cityListView.setSelection(position);
            }
        }
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
        private Map<String, Integer> alphaIndexer;

        public CityAdapter(Context context) {
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
            mData = new ArrayList<>();
            alphaIndexer = new HashMap<String, Integer>();
        }

        public List<City> getData() {
            return mData;
        }

        public void setData(List<City> list) {
            mData = list;
            if (mData == null) {
                mData = new ArrayList<>();
                return;
            }
            for (int i = 0; i < list.size(); i++) {
                String currentStr = list.get(i).letter;
                String previewStr = (i - 1) >= 0 ? list.get(i - 1).letter : " ";
                if (!previewStr.equals(currentStr)) {//前一个首字母与当前首字母不同时加入HashMap中同时显示该字母
                    String name = list.get(i).letter;
                    alphaIndexer.put(name, i);
                }
            }
        }

        public Map<String, Integer> getCityMap() {
            return alphaIndexer;
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
        city.pinyin = jsonObject.optString("py");
        city.parseFirstLetter();
        return city;
    }
}
