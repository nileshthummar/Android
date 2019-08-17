
package com.watchback2.android.utils;

import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableBoolean;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Same as ObservableBoolean, but allows setting value without notifying callbacks
 */
public class WatchBackObservableBoolean extends BaseObservable implements Parcelable, Serializable {
    static final long serialVersionUID = 1L;
    private boolean mValue;

    /**
     * Creates an ObservableBoolean with the given initial value.
     *
     * @param value the initial value for the ObservableBoolean
     */
    public WatchBackObservableBoolean(boolean value) {
        mValue = value;
    }

    /**
     * Creates an ObservableBoolean with the initial value of <code>false</code>.
     */
    public WatchBackObservableBoolean() {
    }

    /**
     * @return the stored value.
     */
    public boolean get() {
        return mValue;
    }

    /**
     * Set the stored value.
     * @param value The new value
     */
    public void set(boolean value) {
        if (value != mValue) {
            mValue = value;
            notifyChange();
        }
    }

    /**
     * Set the stored value without notifying callbacks
     * @param value The new value
     */
    public void setWithoutNotify(boolean value) {
        if (value != mValue) {
            mValue = value;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mValue ? 1 : 0);
    }

    public static final Parcelable.Creator<ObservableBoolean> CREATOR
            = new Parcelable.Creator<ObservableBoolean>() {

        @Override
        public ObservableBoolean createFromParcel(Parcel source) {
            return new ObservableBoolean(source.readInt() == 1);
        }

        @Override
        public ObservableBoolean[] newArray(int size) {
            return new ObservableBoolean[size];
        }
    };
}
