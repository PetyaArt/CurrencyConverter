package com.example.petya.currencyconverter.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ModelLab {

    private static ModelLab sModelLab;

    private Context mContext;
    private List<Model> mModelList;

    private ModelLab(Context context) {
        mContext = context.getApplicationContext();
        mModelList = new ArrayList<>();
    }

    public void addModel(Model m) {
        mModelList.add(m);
    }

    public List<Model> getCrimes() {
        return mModelList;
    }

    public Model getModel(UUID id) {
        for (Model model: mModelList) {
            if (model.getId().equals(id)){
                return model;
            }
        }
        return null;
    }

    public static ModelLab get(Context context) {
        if (sModelLab == null) {
            sModelLab = new ModelLab(context);
        }
        return sModelLab;
    }
}
