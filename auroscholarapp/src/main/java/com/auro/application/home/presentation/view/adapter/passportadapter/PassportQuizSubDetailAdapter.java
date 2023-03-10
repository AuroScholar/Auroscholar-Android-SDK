package com.auro.application.home.presentation.view.adapter.passportadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.auro.application.R;
import com.auro.application.core.common.CommonCallBackListner;
import com.auro.application.core.common.Status;
import com.auro.application.databinding.PassportQuizDetailLayoutBinding;
import com.auro.application.databinding.PassportQuizLayoutBinding;
import com.auro.application.home.data.model.passportmodels.PassportQuizDetailModel;
import com.auro.application.home.data.model.passportmodels.PassportQuizModel;
import com.auro.application.home.data.model.response.APIcertificate;
import com.auro.application.util.AppLogger;
import com.auro.application.util.AppUtil;

import java.util.ArrayList;
import java.util.List;


public class PassportQuizSubDetailAdapter extends RecyclerView.Adapter<PassportQuizSubDetailAdapter.ViewHolder> {

    List<PassportQuizDetailModel> mValues;
    Context mContext;
    PassportQuizDetailLayoutBinding binding;
    CommonCallBackListner listner;

    public PassportQuizSubDetailAdapter(Context context, List<PassportQuizDetailModel> values, CommonCallBackListner listner) {
        mValues = values;
        mContext = context;
        this.listner = listner;
    }

    public void updateList(ArrayList<PassportQuizDetailModel> values) {
        mValues = values;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        PassportQuizDetailLayoutBinding binding;

        public ViewHolder(PassportQuizDetailLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(APIcertificate resModel, int position) {

        }

    }


    @Override
    public PassportQuizSubDetailAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
                R.layout.passport_quiz_detail_layout, viewGroup, false);
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(ViewHolder Vholder, @SuppressLint("RecyclerView") int position) {

        setQuizAdapter(position);
        Vholder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listner != null) {
                    listner.commonEventListner(AppUtil.getCommonClickModel(1, Status.ITEM_CLICK, mValues.get(position)));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }




    public void setQuizAdapter(int position) {
        binding.gridRecycler.setLayoutManager(new GridLayoutManager(mContext,2));
        binding.gridRecycler.setHasFixedSize(true);
        binding.gridRecycler.setNestedScrollingEnabled(false);
        PassportQuizDetailAdapter passportSpinnerAdapter = new PassportQuizDetailAdapter(mContext,mValues.get(position).getPassportQuizGridModelList(),null);
        binding.gridRecycler.setAdapter(passportSpinnerAdapter);
    }
}
