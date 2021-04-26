package com.example.weatherapp_v2;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherapp_v2.common.Common;
import com.example.weatherapp_v2.model.WeatherResult;
import com.example.weatherapp_v2.retrofit.IOpenWeatherMap;
import com.example.weatherapp_v2.retrofit.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.label305.asynctask.SimpleAsyncTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


public class CityFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private List<String> CityList;
    private MaterialSearchBar searchBar;


    ImageView img_weather;
    TextView txt_city_name, txt_humidity, txt_sunrise, txt_sunset, txt_pressure,
            txt_temperature, txt_description, txt_date_time, txt_wind, txt_geo_coord;
    LinearLayout weather_panel;
    ProgressBar loading;

    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;

    static CityFragment instance;

    public static CityFragment getInstance(){
        if (instance ==null)
            instance=new CityFragment();
        return instance;
    }


    public CityFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }


    // TODO: Rename and change types and number of parameters
    public static CityFragment newInstance(String param1, String param2) {
        CityFragment fragment = new CityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_city, container, false);

        img_weather = (ImageView)itemView.findViewById(R.id.img_weather);
        txt_humidity= (TextView)itemView.findViewById(R.id.txt_humidity);
        txt_sunrise= (TextView)itemView.findViewById(R.id.txt_sunrise);
        txt_sunset= (TextView)itemView.findViewById(R.id.txt_sunset);
        txt_pressure= (TextView)itemView.findViewById(R.id.txt_pressure);
        txt_temperature= (TextView)itemView.findViewById(R.id.txt_temperature);
        txt_description= (TextView)itemView.findViewById(R.id.txt_description);
        txt_date_time= (TextView)itemView.findViewById(R.id.txt_date_time);
        txt_wind= (TextView)itemView.findViewById(R.id.txt_wind);
        txt_geo_coord= (TextView)itemView.findViewById(R.id.txt_geo_coord);
        txt_city_name= (TextView)itemView.findViewById(R.id.txt_city_name);

        weather_panel = (LinearLayout)itemView.findViewById(R.id.weather_panel);
        loading = (ProgressBar)itemView.findViewById(R.id.loading);

        searchBar = (MaterialSearchBar)itemView.findViewById(R.id.searchBar);
        searchBar.setEnabled(false);

        // AsyncTask class to load Cities List
        new LoadCities().execute();



        return itemView;
    }



      private class LoadCities extends SimpleAsyncTask<List<String>> {

        @Override
        protected List<String> doInBackgroundSimple() {
            CityList = new ArrayList<>();
            try {
                StringBuilder stringBuilder = new StringBuilder();
                InputStream is = getResources().openRawResource(R.raw.city_list);
                GZIPInputStream gzipInputStream = new GZIPInputStream(is);

                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String readed;
                while((readed = bufferedReader.readLine()) !=null){
                    stringBuilder.append(readed);
                }
                CityList = new Gson().fromJson(stringBuilder.toString(), new TypeToken<List<String>>(){}.getType());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CityList;
        }

        @Override
        protected void onSuccess(final List<String> listCity){
            super.onSuccess(listCity);

            searchBar.setEnabled(true);
            searchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    List<String> suggest = new ArrayList<>();
                    for (String search : listCity){
                        if (search.toLowerCase().contains(searchBar.getText().toLowerCase()))
                            suggest.add(search);
                    }
                    searchBar.setLastSuggestions(suggest);

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {

                }

                @Override
                public void onSearchConfirmed(CharSequence text) {
                    getWeatherInformation(text.toString());

                    searchBar.setLastSuggestions(listCity);

                }

                @Override
                public void onButtonClicked(int buttonCode) {

                }
            });
            searchBar.setLastSuggestions(listCity);

            loading.setVisibility(View.GONE);
            weather_panel.setVisibility(View.VISIBLE);
        }



      }

    private void getWeatherInformation(String cityName) {
        compositeDisposable.add(mService.getWeatherByCityName(cityName,
                Common.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {

                        //Load image
                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/").append(weatherResult.getWeather().get(0).getIcon())
                                .append(".png").toString()).into(img_weather);

                        //Load Info
                        txt_city_name.setText(weatherResult.getName() + "   " + weatherResult.getSys().getCountry());
                        txt_description.setText(new StringBuilder("Current weather in ")
                                .append(weatherResult.getName()).toString());
                        txt_temperature.setText(new StringBuilder(
                                String.valueOf(weatherResult.getMain().getTemp())).append(" °C").toString());
                        txt_date_time.setText(Common.convertUnixToDate(weatherResult.getDt()));
                        txt_pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                        txt_humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append("%").toString());
                        txt_sunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
                        txt_sunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                        txt_geo_coord.setText(new StringBuilder(weatherResult.getCoord().toString()).toString());
                        txt_wind.setText(new StringBuilder(String.valueOf(weatherResult.getWind().getSpeed())).append(" Kph - ").append(weatherResult.getWind().getWindDirection()));

                        //Display panel
                        weather_panel.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);


                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getActivity() , ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    @Override
    public void  onDestroy(){
        compositeDisposable.clear();
        super.onDestroy();
    }
    @Override
    public void onStop(){
        compositeDisposable.clear();
        super.onStop();
    }


}