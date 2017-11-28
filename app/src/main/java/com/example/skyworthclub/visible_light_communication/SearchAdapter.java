package com.example.skyworthclub.visible_light_communication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by skyworthclub on 2017/11/27.
 */

public class SearchAdapter extends BaseAdapter {
    private List<HashMap<String, String>> addressDatas;
    private LayoutInflater layoutInflater;

    public SearchAdapter(Context context, List<HashMap<String, String>> datas){
        layoutInflater = LayoutInflater.from(context);
        addressDatas = new ArrayList<HashMap<String, String>>();
        addressDatas = datas;
    }

    @Override
    public int getCount() {
        return addressDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return addressDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.xyj_search_list_item, null);
            viewHolder.title = (TextView)convertView.findViewById(R.id.xyj_title);
            viewHolder.text = (TextView)convertView.findViewById(R.id.xyj_text);
            convertView.setTag(viewHolder);

        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        viewHolder.title.setText(addressDatas.get(position).get("name"));
        viewHolder.text.setText(addressDatas.get(position).get("address"));

        return convertView;
    }

    private static class ViewHolder{
        private TextView title;
        private TextView text;
    }
}
