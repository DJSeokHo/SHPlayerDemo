package com.swein.shplayerdemo.main.biglistitem;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;
import com.swein.shplayerdemo.main.biglistitem.adapter.ListAutoTinyWindowAdapter;
import com.swein.shplayerdemo.main.biglistitem.item.model.ListAutoTinyWindowItemModel;

import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;

public class ListAutoTinyWindowActivity extends Activity {

    private final static String TAG = "ListAutoTinyWindowActivity";

    private RecyclerView recyclerView;
    private ListAutoTinyWindowAdapter listAutoTinyWindowAdapter;

    private RecyclerView.LayoutManager layoutManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_auto_tiny_window);

        findView();
        initList();
    }

    private void findView() {
        recyclerView = findViewById(R.id.recyclerview);
    }

    private void initList() {

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        listAutoTinyWindowAdapter = new ListAutoTinyWindowAdapter(this);
        recyclerView.setAdapter(listAutoTinyWindowAdapter);
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {

            @Override
            public void onChildViewAttachedToWindow(View view) {
                // when visible
//                Jzvd.onChildViewAttachedToWindow(view, R.id.jzvdStd);

            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                // when invisible
//                Jzvd.onChildViewDetachedFromWindow(view);

                Jzvd.onChildViewDetachedFromWindowStopAutoPlay(view);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                switch (newState) {

                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        /*
                        drag
                         */
                        ILog.iLogDebug(TAG, "SCROLL_STATE_DRAGGING");
                        break;

                    case RecyclerView.SCROLL_STATE_IDLE:
                        /*
                        stop
                         */
                        ILog.iLogDebug(TAG, "SCROLL_STATE_IDLE");
//                        ILog.iLogDebug(TAG, ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition());
//                        ILog.iLogDebug(TAG, ((LinearLayoutManager)layoutManager).findFirstCompletelyVisibleItemPosition());


                         /*
                        auto play option
                         */

//                         if(JzvdMgr.getSecondFloor() != null) {
//                             if(JzvdMgr.getSecondFloor().currentScreen == Jzvd.SCREEN_WINDOW_TINY || JZMediaManager.isPlaying()) {
//                                 ILog.iLogDebug(TAG, "yes");
//                             }
//                             else {
//                                 RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForLayoutPosition(((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition());
//                                 if (viewHolder instanceof ListAutoTinyWindowItemViewHolder) {
//                                     ListAutoTinyWindowItemViewHolder listAutoTinyWindowItemViewHolder = (ListAutoTinyWindowItemViewHolder) viewHolder;
//                                     listAutoTinyWindowItemViewHolder.autoPlay();
//                                 }
//                             }
//                         }
//                         else {
//                             RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForLayoutPosition(((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition());
//                             if (viewHolder instanceof ListAutoTinyWindowItemViewHolder) {
//                                 ListAutoTinyWindowItemViewHolder listAutoTinyWindowItemViewHolder = (ListAutoTinyWindowItemViewHolder) viewHolder;
//                                 listAutoTinyWindowItemViewHolder.autoPlay();
//                             }
//                         }

//                        if(JzvdMgr.getSecondFloor() == null) {
//                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForLayoutPosition(((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition());
//                            if (viewHolder instanceof ListAutoTinyWindowItemViewHolder) {
//                                ListAutoTinyWindowItemViewHolder listAutoTinyWindowItemViewHolder = (ListAutoTinyWindowItemViewHolder) viewHolder;
//                                listAutoTinyWindowItemViewHolder.autoPlay();
//                            }
//                        }


                        break;

                    case RecyclerView.SCROLL_STATE_SETTLING:
                       /*
                       auto scrolling
                        */

                        ILog.iLogDebug(TAG, "SCROLL_STATE_SETTLING");
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        listAutoTinyWindowAdapter.loadMore(createTempData());
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }

    private List<ListAutoTinyWindowItemModel> createTempData() {
        List<ListAutoTinyWindowItemModel> list = new ArrayList<>();

        ListAutoTinyWindowItemModel listAutoTinyWindowItemModel;

        listAutoTinyWindowItemModel = new ListAutoTinyWindowItemModel();
        listAutoTinyWindowItemModel.url = "http://jzvd.nathen.cn/6ea7357bc3fa4658b29b7933ba575008/fbbba953374248eb913cb1408dc61d85-5287d2089db37e62345123a1be272f8b.mp4";
        listAutoTinyWindowItemModel.imageUrl = "http://jzvd-pic.nathen.cn/jzvd-pic/bd7ffc84-8407-4037-a078-7d922ce0fb0f.jpg";
        list.add(listAutoTinyWindowItemModel);

        listAutoTinyWindowItemModel = new ListAutoTinyWindowItemModel();
        listAutoTinyWindowItemModel.url = "http://jzvd.nathen.cn/df6096e7878541cbbea3f7298683fbed/ef76450342914427beafe9368a4e0397-5287d2089db37e62345123a1be272f8b.mp4";
        listAutoTinyWindowItemModel.imageUrl = "http://jzvd-pic.nathen.cn/jzvd-pic/ccd86ca1-66c7-4331-9450-a3b7f765424a.png";
        list.add(listAutoTinyWindowItemModel);

        listAutoTinyWindowItemModel = new ListAutoTinyWindowItemModel();
        listAutoTinyWindowItemModel.url = "http://jzvd.nathen.cn/384d341e000145fb82295bdc54ecef88/103eab5afca34baebc970378dd484942-5287d2089db37e62345123a1be272f8b.mp4";
        listAutoTinyWindowItemModel.imageUrl = "http://jzvd-pic.nathen.cn/jzvd-pic/2adde364-9be1-4864-b4b9-0b0bcc81ef2e.jpg";
        list.add(listAutoTinyWindowItemModel);

        listAutoTinyWindowItemModel = new ListAutoTinyWindowItemModel();
        listAutoTinyWindowItemModel.url = "http://jzvd.nathen.cn/f55530ba8a59403da0621cbf4faef15e/adae4f2e3ecf4ea780beb057e7bce84c-5287d2089db37e62345123a1be272f8b.mp4";
        listAutoTinyWindowItemModel.imageUrl = "http://jzvd-pic.nathen.cn/jzvd-pic/2a877211-4b68-4e3a-87be-6d2730faef27.png";
        list.add(listAutoTinyWindowItemModel);

        listAutoTinyWindowItemModel = new ListAutoTinyWindowItemModel();
        listAutoTinyWindowItemModel.url = "http://jzvd.nathen.cn/6340efd1962946ad80eeffd19b3be89c/65b499c0f16e4dd8900497e51ffa0949-5287d2089db37e62345123a1be272f8b.mp4";
        listAutoTinyWindowItemModel.imageUrl = "http://jzvd-pic.nathen.cn/jzvd-pic/aaeb5da9-ac50-4712-a28d-863fe40f1fc6.png";
        list.add(listAutoTinyWindowItemModel);

        listAutoTinyWindowItemModel = new ListAutoTinyWindowItemModel();
        listAutoTinyWindowItemModel.url = "http://jzvd.nathen.cn/f07fa9fddd1e45a6ae1570c7fe7967c1/c6db82685b894e25b523b1cb28d79f2e-5287d2089db37e62345123a1be272f8b.mp4";
        listAutoTinyWindowItemModel.imageUrl = "http://jzvd-pic.nathen.cn/jzvd-pic/e565f9cc-eedc-45f0-99f8-5b0fa3aed567%281%29.jpg";
        list.add(listAutoTinyWindowItemModel);

        listAutoTinyWindowItemModel = new ListAutoTinyWindowItemModel();
        listAutoTinyWindowItemModel.url = "http://jzvd.nathen.cn/d2e969f2ec734520b46ab0965d2b68bd/f124edfef6c24be8b1a7b7f996ccc5e0-5287d2089db37e62345123a1be272f8b.mp4";
        listAutoTinyWindowItemModel.imageUrl = "http://jzvd-pic.nathen.cn/jzvd-pic/3430ec64-e6a7-4d8e-b044-9d408e075b7c.jpg";
        list.add(listAutoTinyWindowItemModel);

        listAutoTinyWindowItemModel = new ListAutoTinyWindowItemModel();
        listAutoTinyWindowItemModel.url = "http://jzvd.nathen.cn/4f965ad507ef4194a60a943a34cfe147/32af151ea132471f92c9ced2cff785ea-5287d2089db37e62345123a1be272f8b.mp4";
        listAutoTinyWindowItemModel.imageUrl = "http://jzvd-pic.nathen.cn/jzvd-pic/2204a578-609b-440e-8af7-a0ee17ff3aee.jpg";
        list.add(listAutoTinyWindowItemModel);

        listAutoTinyWindowItemModel = new ListAutoTinyWindowItemModel();
        listAutoTinyWindowItemModel.url = "http://jzvd.nathen.cn/342a5f7ef6124a4a8faf00e738b8bee4/cf6d9db0bd4d41f59d09ea0a81e918fd-5287d2089db37e62345123a1be272f8b.mp4";
        listAutoTinyWindowItemModel.imageUrl = "http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png";
        list.add(listAutoTinyWindowItemModel);

        for(int i = 0; i < list.size(); i++) {
            list.get(i).index = i;
            list.get(i).title = "Title " + i;
            list.get(i).subTitle = list.get(i).imageUrl;
        }
        return list;
    }
}
