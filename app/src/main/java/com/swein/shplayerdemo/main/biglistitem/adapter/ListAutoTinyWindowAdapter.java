package com.swein.shplayerdemo.main.biglistitem.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.main.biglistitem.item.model.ListAutoTinyWindowItemModel;
import com.swein.shplayerdemo.main.biglistitem.item.viewholder.ListAutoTinyWindowItemViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ListAutoTinyWindowAdapter extends RecyclerView.Adapter {

    private final static String TAG = "ListAutoTinyWindowAdapter";

    private Context context;

    List<ListAutoTinyWindowItemModel> listAutoTinyWindowItemModelList = new ArrayList<>();



    public ListAutoTinyWindowAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_holder_list_auto_tiny_window_item, viewGroup, false);
        return new ListAutoTinyWindowItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        ListAutoTinyWindowItemViewHolder listAutoTinyWindowItemViewHolder = (ListAutoTinyWindowItemViewHolder) viewHolder;
        listAutoTinyWindowItemViewHolder.setPlayer(listAutoTinyWindowItemModelList.get(i));
    }

    public void loadMore(List<ListAutoTinyWindowItemModel> listAutoTinyWindowItemModelList) {
        this.listAutoTinyWindowItemModelList.addAll(listAutoTinyWindowItemModelList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listAutoTinyWindowItemModelList.size();
    }
}
