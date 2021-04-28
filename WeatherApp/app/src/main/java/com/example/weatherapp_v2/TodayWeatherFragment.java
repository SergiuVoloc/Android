package com.example.weatherapp_v2;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import com.squareup.picasso.Picasso;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodayWeatherFragment extends Fragment {

    ImageView imgWeather;
    TextView txtCityName, txtHumidity, txtSunrise, txtSunset, txtPressure,
            txtTemperature, txtDescription, txtDateTime, txtWind, txtGeoCoord;
    LinearLayout weatherPanel;
    ProgressBar loading;

    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;

    private CharSequence notificationTitle;
    private CharSequence notificationTemperature;


    public CharSequence getNotificationTitle() {
        return this.notificationTitle;
    }
    public CharSequence getNotificationTemperature() {
        return this.notificationTemperature;
    }


    static TodayWeatherFragment instance;

    public static TodayWeatherFragment getInstance(){
        if (instance ==null)
            instance=new TodayWeatherFragment();
            return instance;
    }

    public TodayWeatherFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_today_weather, container, false);

        imgWeather = (ImageView)itemView.findViewById(R.id.img_weather);
        txtHumidity = (TextView)itemView.findViewById(R.id.txt_humidity);
        txtSunrise = (TextView)itemView.findViewById(R.id.txt_sunrise);
        txtSunset = (TextView)itemView.findViewById(R.id.txt_sunset);
        txtPressure = (TextView)itemView.findViewById(R.id.txt_pressure);
        txtTemperature = (TextView)itemView.findViewById(R.id.txt_temperature);
        txtDescription = (TextView)itemView.findViewById(R.id.txt_description);
        txtDateTime = (TextView)itemView.findViewById(R.id.txt_date_time);
        txtWind = (TextView)itemView.findViewById(R.id.txt_wind);
        txtGeoCoord = (TextView)itemView.findViewById(R.id.txt_geo_coord);
        txtCityName = (TextView)itemView.findViewById(R.id.txt_city_name);

        weatherPanel = (LinearLayout)itemView.findViewById(R.id.weather_panel);
        loading = (ProgressBar)itemView.findViewById(R.id.loading);
        
        getWeatherInformation();

        return itemView;
    }

    public void getWeatherInformation() {
        compositeDisposable.add(mService.getWeatherByLatLng(String.valueOf(Common.current_location.getLatitude()),
                String.valueOf(Common.current_location.getLongitude()),
                Common.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {

                        //Load image
                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/").append(weatherResult.getWeather().get(0).getIcon())
                        .append(".png").toString()).into(imgWeather);

                        //Load Info
                        txtCityName.setText(weatherResult.getName() + "   " + weatherResult.getSys().getCountry());
                        txtDescription.setText(new StringBuilder("Current weather in ")
                        .append(weatherResult.getName()).toString());
                        txtTemperature.setText(new StringBuilder(
                                String.valueOf(weatherResult.getMain().getTemp())).append(" °C").toString());
                        txtDateTime.setText(Common.convertUnixToDate(weatherResult.getDt()));
                        txtPressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                        txtHumidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append("%").toString());
                        txtSunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
                        txtSunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                        txtGeoCoord.setText(new StringBuilder(weatherResult.getCoord().toString()).toString());
                        txtWind.setText(new StringBuilder(String.valueOf(weatherResult.getWind().getSpeed())).append(" Kph - ").append(weatherResult.getWind().getWindDirection()));

                        //Display panel
                        weatherPanel.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);

                        notificationTitle = (CharSequence) txtDescription.getText();
                        notificationTemperature = (CharSequence) txtTemperature.getText();

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
