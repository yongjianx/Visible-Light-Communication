package ht.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.skyworthclub.visible_light_communication.R;
import ht.adapter.OnSaleRvAdapter;
import ht.bean.ShopInfo;

import java.util.List;

/**
 * Created by dn on 2017/11/24.
 */

public class OnSaleFragment extends Fragment {

    private RecyclerView mRvOnSale;
    private List<ShopInfo> mShopInfoList;
    private OnSaleRvAdapter mOnSaleRvAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ht_fragment_sale, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        mShopInfoList = getArguments().getParcelableArrayList("shop info");
        mRvOnSale = view.findViewById(R.id.rv_on_sale);
        mOnSaleRvAdapter = new OnSaleRvAdapter(getContext(), mShopInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRvOnSale.setLayoutManager(linearLayoutManager);
        mRvOnSale.setAdapter(mOnSaleRvAdapter);
    }
}
