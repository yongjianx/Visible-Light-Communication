package ht.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.skyworthclub.visible_light_communication.R;
import ht.bean.ShopInfo;

import java.util.List;

/**
 * Created by dn on 2017/11/24.
 */

public class OnSaleRvAdapter extends RecyclerView.Adapter<OnSaleRvAdapter.OnSaleViewHolder>{

    private List<ShopInfo> mShopInfoList;
    private Context mContext;

    public OnSaleRvAdapter(Context context, List<ShopInfo> shopInfoList) {
        mContext = context;
        mShopInfoList = shopInfoList;
    }


    @Override
    public OnSaleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ht_item_shop_on_sale, parent, false);
        return new OnSaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OnSaleViewHolder holder, int position) {
        ShopInfo shopInfo = mShopInfoList.get(position);
        Glide.with(mContext)
                .load(shopInfo.getShopPhoto())
                .into(holder.imvShopPhoto);
        holder.tvShopName.setText(shopInfo.getShopName());
        holder.tvShopCategory.setText(shopInfo.getShopCategory());
        holder.tvShopSale.setText(shopInfo.getShopSale());
        holder.tvShopAddress.setText(shopInfo.getShopAddress());
    }

    @Override
    public int getItemCount() {
        return mShopInfoList.size();
    }

    class OnSaleViewHolder extends RecyclerView.ViewHolder {
        ImageView imvShopPhoto;
        TextView tvShopName;
        TextView tvShopCategory;
        TextView tvShopSale;
        TextView tvShopAddress;

        public OnSaleViewHolder(View itemView) {
            super(itemView);
            imvShopPhoto = itemView.findViewById(R.id.img_shop);
            tvShopName = itemView.findViewById(R.id.tv_shop_name);
            tvShopCategory = itemView.findViewById(R.id.tv_shop_category);
            tvShopSale = itemView.findViewById(R.id.tv_sale);
            tvShopAddress = itemView.findViewById(R.id.tv_shop_address);
        }
    }

}
