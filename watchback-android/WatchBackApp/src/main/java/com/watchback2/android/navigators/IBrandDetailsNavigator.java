package com.watchback2.android.navigators;

/**
 * Created by perk on 21/03/18.
 */

public interface IBrandDetailsNavigator {

    void onBackClick();

    void onButtonClick();

    void onSortClick();

    void onRequestMoreInfoClick();

    void onAddToFavoritesClick(boolean isAlreadyAdded);

    void onSettingsClick();
}
