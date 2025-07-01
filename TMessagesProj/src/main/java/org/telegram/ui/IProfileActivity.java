package org.telegram.ui;

import android.content.Context;

import org.telegram.ui.Components.RecyclerListView;

public interface IProfileActivity {

    // ProfileBirthdayEffect

    Context getContext();

    int getCurrentAccount();

    long getDialogId();

    RecyclerListView getListView();

    int getBirthdayRow();
}
