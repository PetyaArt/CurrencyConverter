package com.example.petya.currencyconverter.data;

import com.example.petya.currencyconverter.interfaces.Link;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Controller {

    private static final String BASE_URL = "https://free.currencyconverterapi.com/";

    public static Link getApi() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Link mConvertLink  = retrofit.create(Link.class);
        return mConvertLink;

    }
}
