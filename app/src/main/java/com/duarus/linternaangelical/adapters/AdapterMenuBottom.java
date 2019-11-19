package com.duarus.linternaangelical.adapters;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.duarus.linternaangelical.R;

public class AdapterMenuBottom extends PagerAdapter {

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        int resId = 0;
        switch (position) {
            case 0:
                resId = R.id.page_one;
                break;
            case 1:
                resId = R.id.page_two;
                break;
            case 2:
                resId = R.id.page_tree;
                break;
        }
        return container.findViewById(resId);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        NestedScrollView page_one=container.findViewById(R.id.page_one);
        NestedScrollView page_two=container.findViewById(R.id.page_two);
        NestedScrollView page_tree=container.findViewById(R.id.page_tree);
        NestedScrollView current = ((NestedScrollView)object);
        current.setNestedScrollingEnabled(true);
        if(position==0){
            page_one.setNestedScrollingEnabled(true);
            page_two.setNestedScrollingEnabled(false);
            page_tree.setNestedScrollingEnabled(false);
        }else if(position==1){
            page_one.setNestedScrollingEnabled(false);
            page_two.setNestedScrollingEnabled(true);
            page_tree.setNestedScrollingEnabled(false);
        }else if(position==2){
            page_two.setNestedScrollingEnabled(false);
            page_one.setNestedScrollingEnabled(false);
            page_tree.setNestedScrollingEnabled(true);
        }

        container.requestLayout();
        page_one.refreshDrawableState();
        page_two.refreshDrawableState();
        page_tree.refreshDrawableState();
    }
}
