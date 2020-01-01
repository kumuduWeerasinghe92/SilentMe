package com.planit.planit.adapter;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.planit.planit.models.PlaceApi;

import java.util.ArrayList;

public class PlaceAutoSuggestAdapter extends ArrayAdapter implements Filterable {

    private ArrayList<String> results;

    private int resource;
    public Context context;
    private boolean isEnter;

    private PlaceApi placeApi=new PlaceApi();

    public PlaceAutoSuggestAdapter(Context context,int resId){
        super(context,resId);
        this.context=context;
        this.resource=resId;
        this.isEnter = false;

    }

    public void setIsEnter(boolean isEnter)
    {
        this.isEnter =isEnter;
    }

    @Override
    public int getCount(){
        if(results !=null) {
            return results.size();
        }else
            {
                return 0;
            }
    }

    @Override
    public String getItem(int pos){
        if(results.size()>0) {
            return results.get(pos);
        }else
            {
                return "";
            }
    }

    @Override
    public Filter getFilter(){
        Filter filter=new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults=new FilterResults();
                if(constraint!=null && isEnter){

                    Log.e("@@@@@@@@@@@@",constraint.toString());
                    results=placeApi.autoComplete(constraint.toString());

                    filterResults.values=results;
                    filterResults.count=results.size();
                }


                isEnter =false;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results!=null && results.count>0){
                    notifyDataSetChanged();
                }
                else{
                    notifyDataSetInvalidated();
                }

            }
        };


        return filter;
    }

}