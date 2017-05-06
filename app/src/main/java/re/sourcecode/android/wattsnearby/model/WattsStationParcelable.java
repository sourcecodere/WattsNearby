package re.sourcecode.android.wattsnearby.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import re.sourcecode.android.wattsnearby.data.ChargingStationContract;

/**
 * Created by olem on 5/6/17.
 * <p>
 * Parcelable for station elements in bottom sheet.
 */

public class WattsStationParcelable implements Parcelable {

    private long mId;
    private String mOpTitle;
    private String mOpWeb;
    private String mUsageTypeTitle;
    private int mAccessKey;
    private int mMembership;
    private int mPayOnSite;
    private Double mLat;
    private Double mLon;
    private String mAddrTitle;
    private String mAddr1;
    private String mAddr2;
    private String mPostCode;
    private String mState;
    private String mTown;
    private String mCountry;

    public WattsStationParcelable() {

    }

    public WattsStationParcelable(long id,
                                  String opTitle,
                                  String opWeb,
                                  String usageTypeTitle,
                                  int accessKey,
                                  int membership,
                                  int payOnSite,
                                  Double lat,
                                  Double lon,
                                  String addrTitle,
                                  String addr1,
                                  String addr2,
                                  String postCode,
                                  String state,
                                  String town,
                                  String country) {
        this.mId = id;
        this.mOpTitle = opTitle;
        this.mOpWeb = opWeb;
        this.mUsageTypeTitle = usageTypeTitle;
        this.mAccessKey = accessKey;
        this.mMembership = membership;
        this.mPayOnSite = payOnSite;
        this.mLat = lat;
        this.mLon = lon;
        this.mAddrTitle = addrTitle;
        this.mAddr1 = addr1;
        this.mAddr2 = addr2;
        this.mPostCode = postCode;
        this.mState = state;
        this.mTown = town;
        this.mCountry = country;
    }

    public WattsStationParcelable(Cursor cursor) {

        int idx_col_id = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_ID);
        int idx_col_title = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_OPERATOR_TITLE);
        int idx_col_web = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_OPERATOR_WEBSITE);
        int idx_col_usage_type_title = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_UT_TITLE);
        int idx_col_access_key = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_UT_ACCESSKEY);
        int idx_col_membership = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_UT_MEMBERSHIP);
        int idx_col_pay = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_UT_PAY_ON_SITE);
        int idx_col_lat = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_LAT);
        int idx_col_lon = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_LON);
        int idx_addr_title = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_ADDR_TITLE);
        int idx_addr_1 = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_ADDR_LINE1);
        int idx_addr_2 = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_ADDR_LINE2);
        int idx_post_code = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_ADDR_POSTCODE);
        int idx_state = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_ADDR_STATE);
        int idx_town = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_ADDR_TOWN);
        int idx_country = cursor.getColumnIndex(ChargingStationContract.StationEntry.COLUMN_ADDR_COUNTRY_TITLE);


        this.mId = cursor.getLong(idx_col_id);
        this.mOpTitle = cursor.getString(idx_col_title);
        this.mOpWeb = cursor.getString(idx_col_web);
        this.mUsageTypeTitle = cursor.getString(idx_col_usage_type_title);
        this.mAccessKey = cursor.getInt(idx_col_access_key);
        this.mMembership = cursor.getInt(idx_col_membership);
        this.mPayOnSite = cursor.getInt(idx_col_pay);
        this.mLat = cursor.getDouble(idx_col_lat);
        this.mLon = cursor.getDouble(idx_col_lon);
        this.mAddrTitle = cursor.getString(idx_addr_title);
        this.mAddr1 = cursor.getString(idx_addr_1);
        this.mAddr2 = cursor.getString(idx_addr_2);
        this.mPostCode = cursor.getString(idx_post_code);
        this.mState = cursor.getString(idx_state);
        this.mTown = cursor.getString(idx_town);
        this.mCountry = cursor.getString(idx_country);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeLong(mId);
        parcel.writeString(mOpTitle);
        parcel.writeString(mOpWeb);
        parcel.writeString(mUsageTypeTitle);
        parcel.writeInt(mAccessKey);
        parcel.writeInt(mMembership);
        parcel.writeInt(mPayOnSite);
        parcel.writeDouble(mLat);
        parcel.writeDouble(mLon);
        parcel.writeString(mAddrTitle);
        parcel.writeString(mAddr1);
        parcel.writeString(mAddr2);
        parcel.writeString(mPostCode);
        parcel.writeString(mState);
        parcel.writeString(mTown);
        parcel.writeString(mCountry);
    }

    public static final Parcelable.Creator<WattsStationParcelable> CREATOR
            = new Parcelable.Creator<WattsStationParcelable>() {

        public WattsStationParcelable createFromParcel(Parcel in) {
            return new WattsStationParcelable(in);
        }

        public WattsStationParcelable[] newArray(int size) {
            return new WattsStationParcelable[size];
        }

    };

    // Parcelling part
    private WattsStationParcelable(Parcel in) {
        mId = in.readLong();
        mOpTitle = in.readString();
        mOpWeb = in.readString();
        mUsageTypeTitle = in.readString();
        mAccessKey = in.readInt();
        mMembership = in.readInt();
        mPayOnSite = in.readInt();
        mLat = in.readDouble();
        mLon = in.readDouble();
        mAddrTitle = in.readString();
        mAddr1 = in.readString();
        mAddr2 = in.readString();
        mPostCode = in.readString();
        mState = in.readString();
        mTown = in.readString();
        mCountry = in.readString();

    }

    public long getmId() {
        return mId;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public String getmOpTitle() {
        return mOpTitle;
    }

    public void setmOpTitle(String mOpTitle) {
        this.mOpTitle = mOpTitle;
    }

    public String getmOpWeb() {
        return mOpWeb;
    }

    public void setmOpWeb(String mOpWeb) {
        this.mOpWeb = mOpWeb;
    }

    public String getmUsageTypeTitle() {
        return mUsageTypeTitle;
    }

    public void setmUsageTypeTitle(String mUsageTypeTitle) {
        this.mUsageTypeTitle = mUsageTypeTitle;
    }

    public int getmAccessKey() {
        return mAccessKey;
    }

    public void setmAccessKey(int mAccessKey) {
        this.mAccessKey = mAccessKey;
    }

    public int getmMembership() {
        return mMembership;
    }

    public void setmMembership(int mMembership) {
        this.mMembership = mMembership;
    }

    public int getmPayOnSite() {
        return mPayOnSite;
    }

    public void setmPayOnSite(int mPayOnSite) {
        this.mPayOnSite = mPayOnSite;
    }

    public Double getmLat() {
        return mLat;
    }

    public void setmLat(Double mLat) {
        this.mLat = mLat;
    }

    public Double getmLon() {
        return mLon;
    }

    public void setmLon(Double mLon) {
        this.mLon = mLon;
    }

    public String getmAddrTitle() {
        return mAddrTitle;
    }

    public void setmAddrTitle(String mAddrTitle) {
        this.mAddrTitle = mAddrTitle;
    }

    public String getmAddr1() {
        return mAddr1;
    }

    public void setmAddr1(String mAddr1) {
        this.mAddr1 = mAddr1;
    }

    public String getmAddr2() {
        return mAddr2;
    }

    public void setmAddr2(String mAddr2) {
        this.mAddr2 = mAddr2;
    }

    public String getmPostCode() {
        return mPostCode;
    }

    public void setmPostCode(String mPostCode) {
        this.mPostCode = mPostCode;
    }

    public String getmState() {
        return mState;
    }

    public void setmState(String mState) {
        this.mState = mState;
    }

    public String getmTown() {
        return mTown;
    }

    public void setmTown(String mTown) {
        this.mTown = mTown;
    }

    public String getmCountry() {
        return mCountry;
    }

    public void setmCountry(String mCountry) {
        this.mCountry = mCountry;
    }
}