package com.swein.shplayerdemo.main.biglistitem.item.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;
import com.swein.shplayerdemo.framework.util.picasso.SHPicasso;
import com.swein.shplayerdemo.main.biglistitem.item.model.ListAutoTinyWindowItemModel;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class ListAutoTinyWindowItemViewHolder extends RecyclerView.ViewHolder {

    private final static String TAG = "ListAutoTinyWindowItemViewHolder";

    private JzvdStd autoTinyPlayer;

    public ListAutoTinyWindowItemViewHolder(@NonNull View itemView) {
        super(itemView);
        findView();
    }

    public void setPlayer(ListAutoTinyWindowItemModel listAutoTinyWindowItemMode) {
        autoTinyPlayer.setUp(listAutoTinyWindowItemMode.url, "title", Jzvd.SCREEN_WINDOW_LIST);
        SHPicasso.getInstance().loadImage(autoTinyPlayer.getContext(), listAutoTinyWindowItemMode.imageUrl, autoTinyPlayer.thumbImageView);
    }

    private void findView() {
        autoTinyPlayer = itemView.findViewById(R.id.autoTinyPlayer);
    }

    @Override
    protected void finalize() throws Throwable {
        ILog.iLogDebug(TAG, "finalize");
        super.finalize();
    }
}
