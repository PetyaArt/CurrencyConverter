package com.example.petya.currencyconverter.interfaces;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Link {
    @GET("api/v6/convert")
    Call<JsonObject> getConvert(@Query("q") String currency, @Query("compact") String compact);
    @GET("api/v6/countries")
    Call<JsonObject> getCountries();
}
