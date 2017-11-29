package ht.bean;

/**
 * Created by dn on 2017/11/28.
 */

import android.os.Parcel;
import android.os.Parcelable;

public class ShopInfo implements Parcelable{
    private String mShopPhoto;
    private String mShopName;
    private String mShopCategory;
    private String mShopSale;
    private String mShopAddress;

    public String getShopPhoto() {
        return mShopPhoto;
    }

    public void setShopPhoto(String shopPhoto) {
        mShopPhoto = shopPhoto;
    }

    public String getShopName() {
        return mShopName;
    }

    public void setShopName(String shopName) {
        mShopName = shopName;
    }

    public String getShopCategory() {
        return mShopCategory;
    }

    public void setShopCategory(String shopCategory) {
        mShopCategory = shopCategory;
    }

    public String getShopSale() {
        return mShopSale;
    }

    public void setShopSale(String shopSale) {
        mShopSale = shopSale;
    }

    public String getShopAddress() {
        return mShopAddress;
    }

    public void setShopAddress(String shopAddress) {
        mShopAddress = shopAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mShopPhoto);
        dest.writeString(this.mShopName);
        dest.writeString(this.mShopCategory);
        dest.writeString(this.mShopSale);
        dest.writeString(this.mShopAddress);
    }

    public ShopInfo() {
    }

    public ShopInfo(String shopPhoto, String shopName, String shopCategory, String shopSale, String shopAddress) {
        mShopPhoto = shopPhoto;
        mShopName = shopName;
        mShopCategory = shopCategory;
        mShopSale = shopSale;
        mShopAddress = shopAddress;
    }

    protected ShopInfo(Parcel in) {
        this.mShopPhoto = in.readString();
        this.mShopName = in.readString();
        this.mShopCategory = in.readString();
        this.mShopSale = in.readString();
        this.mShopAddress = in.readString();
    }

    public static final Creator<ShopInfo> CREATOR = new Creator<ShopInfo>() {
        @Override
        public ShopInfo createFromParcel(Parcel source) {
            return new ShopInfo(source);
        }

        @Override
        public ShopInfo[] newArray(int size) {
            return new ShopInfo[size];
        }
    };
}

