package re.sourcecode.android.wattsnearby.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import re.sourcecode.android.wattsnearby.data.ChargingStationContract;

/**
 * Created by olem on 5/6/17.
 *
 * Parcelable for station elements in bottom sheet.
 *
 */

public class WattsConnectionParcelable implements Parcelable {

    private long mConnectionId;
    private int mConnectionTypeId;
    private int mConnectionFast;
    private String mConnectionTitle;
    private String mConnectionLevelTitle;
    private String mConnectionCurrent;
    private Double mConnectionAmp;
    private Double mConnectionVolt;
    private Double mConnectionKw;

    // Constructors
    public  WattsConnectionParcelable() {
    }

    public WattsConnectionParcelable(long connectionId,
                                     int connectionTypeId,
                                     int connectionFast,
                                     String connectionTitle,
                                     String connectionLevelTitle,
                                     String connectionCurrent,
                                     Double connectionAmp,
                                     Double connectionVolt,
                                     Double connectionKw) {
        this.mConnectionId = connectionId;
        this.mConnectionTypeId = connectionTypeId;
        this.mConnectionFast = connectionFast;
        this.mConnectionTitle = connectionTitle;
        this.mConnectionLevelTitle = connectionLevelTitle;
        this.mConnectionCurrent = connectionCurrent;
        this.mConnectionAmp = connectionAmp;
        this.mConnectionVolt = connectionVolt;
        this.mConnectionKw = connectionKw;

    }

    public WattsConnectionParcelable(Cursor cursor) {

        int idx_col_con_id = cursor.getColumnIndex(ChargingStationContract.ConnectionEntry.COLUMN_CONN_STATION_ID);
        int idx_col_con_type_id = cursor.getColumnIndex(ChargingStationContract.ConnectionEntry.COLUMN_CONN_TYPE_ID);
        int idx_col_con_level = cursor.getColumnIndex(ChargingStationContract.ConnectionEntry.COLUMN_CONN_LEVEL_FAST);
        int idx_col_con_title = cursor.getColumnIndex(ChargingStationContract.ConnectionEntry.COLUMN_CONN_TITLE);
        int idx_col_con_level_title = cursor.getColumnIndex(ChargingStationContract.ConnectionEntry.COLUMN_CONN_LEVEL_TITLE);
        int idx_col_con_current = cursor.getColumnIndex(ChargingStationContract.ConnectionEntry.COLUMN_CONN_CURRENT_TYPE_DESC);
        int idx_col_con_amp = cursor.getColumnIndex(ChargingStationContract.ConnectionEntry.COLUMN_CONN_AMP);
        int idx_col_con_volt = cursor.getColumnIndex(ChargingStationContract.ConnectionEntry.COLUMN_CONN_VOLT);
        int idx_col_con_kw = cursor.getColumnIndex(ChargingStationContract.ConnectionEntry.COLUMN_CONN_KW);

        this.mConnectionId = cursor.getLong(idx_col_con_id);
        this.mConnectionTypeId = cursor.getInt(idx_col_con_type_id);
        this.mConnectionFast = cursor.getInt(idx_col_con_level);
        this.mConnectionTitle = cursor.getString(idx_col_con_title);
        this.mConnectionLevelTitle = cursor.getString(idx_col_con_level_title);
        this.mConnectionCurrent = cursor.getString(idx_col_con_current);
        this.mConnectionAmp = cursor.getDouble(idx_col_con_amp);
        this.mConnectionVolt = cursor.getDouble(idx_col_con_volt);
        this.mConnectionKw = cursor.getDouble(idx_col_con_kw);
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {


        parcel.writeLong(mConnectionId);
        parcel.writeInt(mConnectionTypeId);
        parcel.writeInt(mConnectionFast);
        parcel.writeString(mConnectionTitle);
        parcel.writeString(mConnectionLevelTitle);
        parcel.writeString(mConnectionCurrent);
        parcel.writeDouble(mConnectionAmp);
        parcel.writeDouble(mConnectionVolt);
        parcel.writeDouble(mConnectionKw);
    }

    public static final Parcelable.Creator<WattsConnectionParcelable> CREATOR
            = new Parcelable.Creator<WattsConnectionParcelable>() {

        public WattsConnectionParcelable createFromParcel(Parcel in) {
            return new WattsConnectionParcelable(in);
        }

        public WattsConnectionParcelable[] newArray(int size) {
            return new WattsConnectionParcelable[size];
        }

    };

    // Parcelling part
    private WattsConnectionParcelable(Parcel in) {
        mConnectionId = in.readLong();
        mConnectionTypeId = in.readInt();
        mConnectionFast = in.readInt();
        mConnectionTitle = in.readString();
        mConnectionLevelTitle = in.readString();
        mConnectionCurrent = in.readString();
        mConnectionAmp = in.readDouble();
        mConnectionVolt = in.readDouble();
        mConnectionKw = in.readDouble();

    }

    // Getters and setters


    public long getmConnectionId() {
        return mConnectionId;
    }

    public void setmConnectionId(long mConnectionId) {
        this.mConnectionId = mConnectionId;
    }

    public int getmConnectionTypeId() {
        return mConnectionTypeId;
    }

    public void setmConnectionTypeId(int mConnectionTypeId) {
        this.mConnectionTypeId = mConnectionTypeId;
    }

    public int getmConnectionFast() {
        return mConnectionFast;
    }

    public void setmConnectionFast(int mConnectionFast) {
        this.mConnectionFast = mConnectionFast;
    }

    public String getmConnectionTitle() {
        return mConnectionTitle;
    }

    public void setmConnectionTitle(String mConnectionTitle) {
        this.mConnectionTitle = mConnectionTitle;
    }

    public String getmConnectionLevelTitle() {
        return mConnectionLevelTitle;
    }

    public void setmConnectionLevelTitle(String mConnectionLevelTitle) {
        this.mConnectionLevelTitle = mConnectionLevelTitle;
    }

    public String getmConnectionCurrent() {
        return mConnectionCurrent;
    }

    public void setmConnectionCurrent(String mConnectionCurrent) {
        this.mConnectionCurrent = mConnectionCurrent;
    }

    public Double getmConnectionAmp() {
        return mConnectionAmp;
    }

    public void setmConnectionAmp(Double mConnectionAmp) {
        this.mConnectionAmp = mConnectionAmp;
    }

    public Double getmConnectionVolt() {
        return mConnectionVolt;
    }

    public void setmConnectionVolt(Double mConnectionVolt) {
        this.mConnectionVolt = mConnectionVolt;
    }

    public Double getmConnectionKw() {
        return mConnectionKw;
    }

    public void setmConnectionKw(Double mConnectionKw) {
        this.mConnectionKw = mConnectionKw;
    }
}
