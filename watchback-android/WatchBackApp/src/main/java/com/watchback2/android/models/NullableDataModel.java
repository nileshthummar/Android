package com.watchback2.android.models;

import com.perk.request.annotation.NullableData;
import com.perk.request.model.Data;

/**
 * Created for use with APIs that return null for 'data' field in the JSON response
 */
@NullableData
public class NullableDataModel extends Data {
    private static final long serialVersionUID = 1L;
}