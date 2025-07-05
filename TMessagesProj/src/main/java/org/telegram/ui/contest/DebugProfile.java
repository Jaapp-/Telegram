package org.telegram.ui.contest;

import static org.telegram.messenger.AndroidUtilities.displaySize;
import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.statusBarHeight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SharedMediaLayout;
import org.telegram.ui.ProfileBirthdayEffect;

public class DebugProfile extends BaseFragment {
    private static final String TAG = "Contest";
    private final SharedMediaLayout.SharedMediaPreloader sharedMediaPreloader;
    private TLRPC.UserFull userInfo;

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;

    private int rowCount;
    private int bioRow;
    private int usernameRow;
    private int mediaSectionRow;
    private Theme.ResourcesProvider resourcesProvider;
    private int mediaRow;

    private int topPadding;
    private int topScroll;
    private TopView topView;
    private int actionBarHeight;

    public DebugProfile(Bundle args, SharedMediaLayout.SharedMediaPreloader preloader) {
        super(args);
        sharedMediaPreloader = preloader;
    }

    @Override
    public View createView(Context context) {
        Theme.createProfileResources(context);
        Theme.createChatResources(context, false);
        updateRowIds();

        FrameLayout frameLayout = new FrameLayout(context);

        layoutManager = new LinearLayoutManager(context);
        listView = new RecyclerListView(context);
        listView.setLayoutManager(layoutManager);
        ListAdapter adapter = new ListAdapter(context);
        listView.setAdapter(adapter);
        listView.setClipToPadding(false);
        listView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                onScroll();
            }
        });
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        topScroll = 0;

        topView = new TopView(context);
        frameLayout.addView(topView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        frameLayout.addView(actionBar);

        checkLayout();

        return frameLayout;
    }

    private void onScroll() {
        int newOffset = 0;
        if (listView.getChildCount() > 0) {
            View child = listView.getChildAt(0);
            int adapterPosition = listView.getChildAdapterPosition(child);

            if (adapterPosition == 0) {
                int top = child.getTop();
                if (top >= 0) {
                    newOffset = top;
                }
            }
        }

        topScroll = newOffset;
        Log.i(TAG, "onScroll: " + newOffset + " " + ((float) newOffset / topPadding));
        topView.invalidate();
    }

    private void checkLayout() {
        actionBarHeight = ActionBar.getCurrentActionBarHeight();
        topPadding = displaySize.x + dp(10);
        Log.i(TAG, "checkLayout: " + topPadding);

        listView.setPadding(0, topPadding, 0, 0);
    }

    @Override
    public ActionBar createActionBar(Context context) {
        ActionBar ab = new ActionBar(context, resourcesProvider);
        ab.setBackgroundColor(Color.TRANSPARENT);
        ab.setBackButtonDrawable(new BackDrawable(false));
        ab.setOccupyStatusBar(true);
        ab.setClipContent(true);
        ab.setAddToContainer(false);
        ab.setTitle("HELLO");
        ab.setItemsColor(getThemedColor(Theme.key_actionBarDefaultIcon), false);
        ab.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        return ab;
    }

    public void setUserInfo(TLRPC.UserFull value, ProfileChannelCell.ChannelMessageFetcher channelMessageFetcher, ProfileBirthdayEffect.BirthdayEffectFetcher birthdayAssetsFetcher) {
        userInfo = value;
    }

    void updateRowIds() {
        rowCount = 0;
        bioRow = rowCount++;
        mediaSectionRow = rowCount++;
        usernameRow = rowCount++;
        for (int i = 0; i < 5; i++) {
            rowCount++;
        }
        mediaRow = rowCount++;
    }

    public int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }


    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final static int VIEW_TYPE_TEXT_DETAIL = 1, VIEW_TYPE_SHADOW = 2, VIEW_TYPE_MEDIA = 3;
        private final Context context;

        ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == bioRow || position == usernameRow) {
                return VIEW_TYPE_TEXT_DETAIL;
            } else if (position == mediaSectionRow) {
                return VIEW_TYPE_SHADOW;
            } else if (position == mediaRow) {
                return VIEW_TYPE_MEDIA;
            }
            return VIEW_TYPE_TEXT_DETAIL;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_TEXT_DETAIL:
                    view = new TextDetailCell(context, resourcesProvider, false);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_SHADOW:
                    view = new ShadowSectionCell(context, resourcesProvider);
                    break;
                case VIEW_TYPE_MEDIA:
                    view = new View(context);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    view.setPadding(10, 10, 10, 10);
                    view.setMinimumHeight(1000);
                    break;
                default:
                    view = new TextDetailCell(context, resourcesProvider, false);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_TEXT_DETAIL:
                    TextDetailCell detailCell = (TextDetailCell) holder.itemView;
                    if (position == bioRow) {
                        detailCell.setTextAndValue("25 y.o, CS streamer, San Francisco", "Bio", false);
                    } else if (position == usernameRow) {
                        detailCell.setTextAndValue("@ronald_copper", "Username", false);
                    } else {
                        detailCell.setTextAndValue("WOW", "HEY", false);
                    }
                    break;
                case VIEW_TYPE_SHADOW:
                    View sectionCell = holder.itemView;
                    sectionCell.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                    break;
                case VIEW_TYPE_MEDIA:
                    break;
            }
        }
    }

    class TopView extends FrameLayout {
        private final Paint paint;

        public TopView(@NonNull Context context) {
            super(context);
            setWillNotDraw(false);
            this.paint = new Paint();
            paint.setColor(getThemedColor(Theme.key_actionBarDefault));
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRect(0, 0, getMeasuredWidth(), Math.max(topScroll, statusBarHeight + actionBarHeight), paint);
        }
    }
}
