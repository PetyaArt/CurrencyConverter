package com.example.petya.currencyconverter.activity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petya.currencyconverter.data.Controller;
import com.example.petya.currencyconverter.interfaces.Filterable;
import com.example.petya.currencyconverter.interfaces.Link;
import com.example.petya.currencyconverter.R;
import com.example.petya.currencyconverter.data.Model;
import com.example.petya.currencyconverter.data.ModelLab;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petya.currencyconverter.activity.MainActivity.mFlag;

public class ListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    public static final String SELECTED_MODEL =
            "com.example.petya.currencyconverter.model";
    private static final String FLAG_KEY = "FLAG";

    private TextView mTextViewData;
    private RecyclerView mModelRecyclerView;
    private ModelAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_list);


        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mModelRecyclerView = findViewById(R.id.model_recycler_view);
        mModelRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mTextViewData = findViewById(R.id.textViewData);


        downloads();
    }

    private void downloads() {
        Link link = Controller.getApi();

        if (isOnline()) {
            link.getCountries().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    JsonObject jo = response.body();
                    JsonObject joDeep = jo.getAsJsonObject("results");

                    for (String key : joDeep.keySet()) {
                        if (mFlag) {
                            break;
                        }


                        Model model = new Model();
                        model.setCurrencyId(joDeep.getAsJsonObject(key).get("currencyId").toString().replace("\"", ""));
                        model.setCurrencyName(joDeep.getAsJsonObject(key).get("currencyName").toString().replace("\"", ""));
                        model.setCurrencySymbol(joDeep.getAsJsonObject(key).get("currencySymbol").toString().replace("\"", ""));
                        model.setName(joDeep.getAsJsonObject(key).get("name").toString().replace("\"", ""));

                        ModelLab modelLab = ModelLab.get(getApplicationContext());
                        modelLab.addModel(model);
                    }

                    mFlag = true;
                    stopSwipe();
                    updateUI();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("myLogs", t.toString());

                }
            });
        } else {
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
            stopSwipe();
        }
    }

    private void stopSwipe() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void updateUI() {
        ModelLab modelLab = ModelLab.get(getApplicationContext());
        List<Model> models = modelLab.getCrimes();

        if (models.isEmpty()){
            mTextViewData.setVisibility(View.VISIBLE);
        } else{
            mTextViewData.setVisibility(View.INVISIBLE);
        }

        if (mAdapter == null) {
            mAdapter = new ModelAdapter(models);
            mModelRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setModels(models);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class ModelHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mCurrencySymbol;
        TextView mNameCurrency;

        private Model mModel;

        public ModelHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_model, parent, false));
            itemView.setOnClickListener(this);
            mCurrencySymbol = itemView.findViewById(R.id.currencySymbol);
            mNameCurrency = itemView.findViewById(R.id.NameCurrency);
        }

        public void bind(Model model) {
            mModel = model;
            mCurrencySymbol.setText(model.getCurrencySymbol());
            mNameCurrency.setText(model.getCurrencyName());
        }


        @Override
        public void onClick(View v) {
            Intent data = new Intent();
            data.putExtra(SELECTED_MODEL, mModel.getId());
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private class ModelAdapter extends RecyclerView.Adapter<ModelHolder> implements Filterable {

        private List<Model> mModels;
        private List<Model> mArrayList;

        public ModelAdapter(List<Model> models) {
            mModels = models;
            mArrayList = models;
        }

        @NonNull
        @Override
        public ModelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            return new ModelHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ModelHolder holder, int position) {
            Model model = mModels.get(position);
            holder.bind(model);
        }

        @Override
        public int getItemCount() {
            return mModels.size();
        }

        public void setModels(List<Model> models) {
            mModels = models;
        }


        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {

                    String charString = charSequence.toString().toLowerCase();

                    if (charString.isEmpty()) {
                        mModels = mArrayList;
                    } else {

                        ArrayList<Model> filteredList = new ArrayList<>();

                        for (Model model : mArrayList) {

                            if (model.getCurrencyName().toLowerCase().contains(charString) || model.getName().toLowerCase().contains(charString) || model.getCurrencyId().toLowerCase().contains(charString)) {
                                filteredList.add(model);
                            }
                        }
                        mModels = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = mModels;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    mModels = (List<Model>) filterResults.values;
                    mAdapter.notifyDataSetChanged();
                }
            };
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (null != networkInfo) {
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                return true;
            }
            if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        search(searchView);
        return true;
    }

    private void search(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (mAdapter != null) mAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @Override
    public void onRefresh() {
        downloads();
        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();

    }
}


