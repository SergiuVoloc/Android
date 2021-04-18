package com.example.weatherapp_v2;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.weatherapp_v2.adapter.ForecastAdapter;
import com.example.weatherapp_v2.common.Common;
import com.example.weatherapp_v2.model.ForecastResult;
import com.example.weatherapp_v2.retrofit.IOpenWeatherMap;
import com.example.weatherapp_v2.retrofit.RetrofitClient;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {

    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;

    TextView txtCityName, txtGeoCoord;
    RecyclerView recyclerForecast;


    static ForecastFragment instance;

    public static ForecastFragment getInstance() {
        if (instance == null)
            instance = new ForecastFragment();

        return instance;
    }

    public ForecastFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_forecast, container, false);

        txtCityName = (TextView)itemView.findViewById(R.id.txt_city_name);
        txtGeoCoord = (TextView)itemView.findViewById(R.id.geo_coord);

        recyclerForecast = (RecyclerView)itemView.findViewById(R.id.recycler_forecast);
        recyclerForecast.setHasFixedSize(true);
        recyclerForecast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false));

        getForecastInformation();


        return itemView;
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

    //Ctrl + O
    private void getForecastInformation() {
    compositeDisposable.add(mService.getForecastByLatLng(
            String.valueOf(Common.current_location.getLatitude()),
            String.valueOf(Common.current_location.getLongitude()),
            Common.APP_ID,
            "metric")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<ForecastResult>() {
                @Override
                public void accept(ForecastResult forecastResult) throws Exception {
                    displayForecast(forecastResult);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    Log.d("ERROR",""+throwable.getMessage());
                }
            })
    );
    }

    private void displayForecast(ForecastResult forecastResult) {
        txtCityName.setText(new StringBuilder(forecastResult.city.name));
        txtGeoCoord.setText(new StringBuilder(forecastResult.city.coord.toString()));

        ForecastAdapter adapter = new ForecastAdapter(getContext(),forecastResult);
        recyclerForecast.setAdapter(adapter);


    }
}
