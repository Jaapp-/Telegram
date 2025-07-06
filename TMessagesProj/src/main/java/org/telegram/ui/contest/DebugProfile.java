package org.telegram.ui.contest;

import static org.telegram.messenger.AndroidUtilities.displaySize;
import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.AndroidUtilities.lerp;
import static org.telegram.messenger.AndroidUtilities.statusBarHeight;
import static org.telegram.messenger.Utilities.clamp01;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.AvatarImageView;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SharedMediaLayout;
import org.telegram.ui.Components.VectorAvatarThumbDrawable;
import org.telegram.ui.ProfileBirthdayEffect;

import java.util.Arrays;

public class DebugProfile extends BaseFragment {
    private static final String TAG = "Contest";

    private final static float HEADER_BUTTON_HEIGHT_DP = 54;
    private final static float HEADER_BUTTON_MARGIN_DP = 12;
    private final static float AVATAR_SIZE_DP = 90;
    private final static float AVATAR_EXPAND_THRESHOLD = 0.4f;

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
    private int topScroll;
    private TopView topView;
    private int actionBarHeight;
    private TextView debugText;
    private int topBarsHeight;
    private int minimizedOffset;
    private int expandedOffset;
    private int maximizedOffset;
    private float expandProgress;
    private float maximizeProgress;
    private long userId;
    private long chatId;
    private long topicId;

    private MessagesController.PeerColor peerColor;

    private AvatarImageView avatarImage;
    private AvatarDrawable avatarDrawable;

    private ProfileGalleryView avatarsViewPager;
    private ImageLocation prevLoadedImageLocation;
    private FrameLayout avatarContainer;
    private RadialProgressView avatarProgressView;
    private PagerIndicatorView avatarsViewPagerIndicatorView;
    private LinearLayout headerButtonLayout;
    private float buttonHideProgress;
    private boolean isPulledDown;
    private ValueAnimator avatarMaximizeAnimator;
    private float avatarOffsetY;
    private float avatarScale;

    public DebugProfile(Bundle args, SharedMediaLayout.SharedMediaPreloader preloader) {
        super(args);
        sharedMediaPreloader = preloader;
    }

    @Override
    public boolean onFragmentCreate() {
        userId = arguments.getLong("user_id", 0);
        chatId = arguments.getLong("chat_id", 0);
        topicId = arguments.getLong("topic_id", 0);
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
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
        listView.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                onScrollStopped();
            }
            return false;
        });

        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        topScroll = expandedOffset;

        topView = new TopView(context);
        frameLayout.addView(topView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));


        HeaderButtonView button1 = new HeaderButtonView(context);
        button1.setTextAndIcon(LocaleController.getString(R.string.Message), R.drawable.message);
        button1.setOnClickListener(v -> {
            Log.i(TAG, "button1 click");
        });
        HeaderButtonView button2 = new HeaderButtonView(context);
        button2.setTextAndIcon(LocaleController.getString(R.string.Mute), R.drawable.mute);
        HeaderButtonView button3 = new HeaderButtonView(context);
        button3.setTextAndIcon(LocaleController.getString(R.string.Call), R.drawable.call);
        HeaderButtonView button4 = new HeaderButtonView(context);
        button4.setTextAndIcon(LocaleController.getString(R.string.Video), R.drawable.video);


        avatarDrawable = new AvatarDrawable();
        avatarDrawable.setProfile(true);

        avatarContainer = new FrameLayout(context);
        avatarContainer.setPivotX(0);
        avatarContainer.setPivotY(0);
        frameLayout.addView(avatarContainer, LayoutHelper.createFrame(AVATAR_SIZE_DP, AVATAR_SIZE_DP, Gravity.TOP | Gravity.LEFT));

        avatarImage = new AvatarImageView(context);
        avatarImage.setRoundRadius(dp(AVATAR_SIZE_DP / 2));
        avatarImage.getImageReceiver().setAllowDecodeSingleFrame(true);
        avatarContainer.addView(avatarImage, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        avatarProgressView = new RadialProgressView(context) {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            {
                paint.setColor(0x55000000);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                if (avatarImage != null && avatarImage.getImageReceiver().hasNotThumb()) {
                    paint.setAlpha((int) (0x55 * avatarImage.getImageReceiver().getCurrentAlpha()));
                    canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, getMeasuredWidth() / 2.0f, paint);
                }
                super.onDraw(canvas);
            }
        };
        avatarProgressView.setSize(AndroidUtilities.dp(26));
        avatarProgressView.setProgressColor(0xffffffff);
        avatarProgressView.setNoProgress(false);
        avatarContainer.addView(avatarProgressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));


        OverlaysView overlaysView = new OverlaysView(context);
        avatarsViewPager = new ProfileGalleryView(context, userId != 0 ? userId : -chatId, actionBar, listView, avatarImage, getClassGuid(), overlaysView);


        frameLayout.addView(avatarsViewPager);
        frameLayout.addView(overlaysView);
        avatarImage.setAvatarsViewPager(avatarsViewPager);

        avatarsViewPagerIndicatorView = new PagerIndicatorView(context);
        frameLayout.addView(avatarsViewPagerIndicatorView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        headerButtonLayout = new LinearLayout(getContext());
        headerButtonLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutHelper.WRAP_CONTENT, 1f);
        params.setMargins(AndroidUtilities.dp(10f / 3), 0, AndroidUtilities.dp(10f / 3), 0);
        headerButtonLayout.addView(button1, params);
        headerButtonLayout.addView(button2, params);
        headerButtonLayout.addView(button3, params);
        headerButtonLayout.addView(button4, params);
        frameLayout.addView(headerButtonLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 26 / 3f, 0f, 26 / 3f, 0f));

        frameLayout.addView(actionBar);

        debugText = new TextView(context);
        debugText.setTextColor(getThemedColor(Theme.key_actionBarDefaultTitle));
        debugText.setTextSize(10);
        frameLayout.addView(debugText, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.RIGHT, 0, 24, 4, 0));


        avatarMaximizeAnimator = ValueAnimator.ofFloat(0f, 1f);
        avatarMaximizeAnimator.addUpdateListener(anim -> {
            setAvatarMaximizeAnimationProgress(anim.getAnimatedFraction());
        });
        avatarMaximizeAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        avatarMaximizeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (!isPulledDown) {
                    avatarsViewPager.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                actionBar.setItemsBackgroundColor(isPulledDown ? Theme.ACTION_BAR_WHITE_SELECTOR_COLOR : peerColor != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), false);
                avatarImage.clearForeground();
                avatarsViewPager.setVisibility(isPulledDown ? View.VISIBLE : View.GONE);
//                doNotSetForeground = false;
//                updateStoriesViewBounds(false);
            }
        });

        checkLayout();
        showAvatarProgress(false, false);
        updateProfileData(true);

        layoutManager.scrollToPositionWithOffset(0, expandedOffset - maximizedOffset);

        return frameLayout;
    }


    private void onScrollStopped() {
        if (topScroll < minimizedOffset) return;
        final View view = layoutManager.findViewByPosition(0);
        int targetOffset;
        if (topScroll < expandedOffset) {
            if (expandProgress > 0.33) {
                targetOffset = expandedOffset;
            } else {
                targetOffset = minimizedOffset;
            }
        } else {
            if (maximizeProgress > AVATAR_EXPAND_THRESHOLD) {
                targetOffset = maximizedOffset;
            } else {
                targetOffset = expandedOffset;
            }
        }
        listView.post(() -> {
            listView.smoothScrollBy(0, (view != null ? view.getTop() : 0) - targetOffset);
        });
    }

    void updateAvatar() {
        if (topScroll < minimizedOffset) {
            avatarContainer.setVisibility(View.GONE);
            return;
        }
        avatarContainer.setVisibility(View.VISIBLE);

        float offsetX;
        float alpha = 1;

        if (topScroll < expandedOffset) {
            if (expandProgress < 0.16) {
                avatarScale = 0.1f;
            } else if (expandProgress < 0.5) {
                avatarScale = lerp(0.1f, 0.8f, (expandProgress - 0.16f) / (0.5f - 0.16f));
            } else if (expandProgress < 0.7) {
                avatarScale = 0.8f;
            } else {
                avatarScale = lerp(0.8f, 1f, (expandProgress - 0.7f) / (1f - 0.7f));
            }
            avatarOffsetY = lerp(-0.3f * dp(AVATAR_SIZE_DP), dp(38), expandProgress);
            offsetX = (displaySize.x - dp(AVATAR_SIZE_DP) * avatarScale) / 2f;
            alpha = clamp01((expandProgress - 0.3f) / (0.5f - 0.3f));
        } else {
            float pulldownProgress = clamp01(maximizeProgress / AVATAR_EXPAND_THRESHOLD);
            avatarScale = lerp(1f, 1.1f, pulldownProgress);
            avatarOffsetY = lerp(dp(38), dp(74), pulldownProgress);
            offsetX = (displaySize.x - dp(AVATAR_SIZE_DP) * avatarScale) / 2f;
        }
        if (maximizeProgress > AVATAR_EXPAND_THRESHOLD) {
            // Handle by maximize animation
            return;
        }
        avatarContainer.setTranslationX(offsetX);
        avatarContainer.setTranslationY(avatarOffsetY);
        avatarContainer.setScaleX(avatarScale);
        avatarContainer.setScaleY(avatarScale);
        avatarContainer.setAlpha(alpha);
    }

    private void setAvatarMaximizeAnimationProgress(float animatedFraction) {
        float progress = isPulledDown ? animatedFraction : 1f - animatedFraction;
        updateAvatar();

        float scaleX = lerp(avatarScale, (float) displaySize.x / dp(AVATAR_SIZE_DP), progress);
        float scaleY = lerp(avatarScale, (float) maximizedOffset / dp(AVATAR_SIZE_DP), progress);
        float offsetY = lerp(avatarOffsetY, 0, progress);
        float offsetX = (displaySize.x - dp(AVATAR_SIZE_DP) * scaleX) / 2f;
        int roundRadius = lerp(dp(AVATAR_SIZE_DP / 2), 0, progress);
        avatarContainer.setTranslationX(offsetX);
        avatarContainer.setTranslationY(offsetY);
        avatarContainer.setScaleX(scaleX);
        avatarContainer.setScaleY(scaleY);
        avatarImage.setRoundRadius(roundRadius, roundRadius, roundRadius, roundRadius);


        if (isPulledDown) {
            ViewGroup.LayoutParams params = avatarsViewPager.getLayoutParams();
            params.width = listView.getMeasuredWidth();
            params.height = (int) (progress * maximizedOffset);
            avatarsViewPager.requestLayout();
        }
    }

    void startMaximizeAnimator() {
        listView.smoothScrollBy(0, 0);
        if (avatarMaximizeAnimator.isRunning()) {
            avatarMaximizeAnimator.cancel();
        }
        avatarMaximizeAnimator.start();
    }

    @SuppressLint("SetTextI18n")
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
        topView.invalidate();

        String debug = "scroll: " + topScroll + "\n";
        if (topScroll < minimizedOffset) {
            debug += "minimized";
            expandProgress = 0;
        } else if (topScroll <= expandedOffset) {
            expandProgress = clamp01((topScroll - minimizedOffset) / (float) (expandedOffset - minimizedOffset));
            maximizeProgress = 0;
            debug += "expanding " + expandProgress;
        } else if (topScroll <= maximizedOffset) {
            maximizeProgress = clamp01((topScroll - expandedOffset) / (float) (maximizedOffset - expandedOffset));
            expandProgress = 1f;
            debug += "maximizing " + maximizeProgress;

            if (maximizeProgress > AVATAR_EXPAND_THRESHOLD) {
                if (!isPulledDown) {
                    isPulledDown = true;
                    Log.i(TAG, "onScroll: DOWN");
                    startMaximizeAnimator();
                }
            } else {
                if (isPulledDown) {
                    isPulledDown = false;
                    Log.i(TAG, "onScroll: UP");
                    startMaximizeAnimator();
                }
            }
        } else {
            debugText.setText("maximized");
            debug += "maximized";
        }
        debugText.setText(debug);
        updateAvatar();
        updateHeaderButtons();
    }

    private void updateHeaderButtons() {
        buttonHideProgress = 1f - clamp01((float) (topScroll - topBarsHeight) / dp(HEADER_BUTTON_HEIGHT_DP));

        if (headerButtonLayout != null) {
            for (int i = 0; i < headerButtonLayout.getChildCount(); i++) {
                HeaderButtonView button = (HeaderButtonView) headerButtonLayout.getChildAt(i);
                button.setHideProgress(buttonHideProgress);
            }
        }

        if (headerButtonLayout != null) {
            headerButtonLayout.setTranslationY(topScroll - dp(HEADER_BUTTON_HEIGHT_DP + HEADER_BUTTON_MARGIN_DP) + buttonHideProgress * dp(HEADER_BUTTON_HEIGHT_DP));
        }
    }

    private void checkLayout() {
        actionBarHeight = ActionBar.getCurrentActionBarHeight();
        topBarsHeight = actionBarHeight + statusBarHeight;

        minimizedOffset = topBarsHeight;
        expandedOffset = dp(270);
        maximizedOffset = displaySize.x + dp(62);

        listView.setPadding(0, maximizedOffset, 0, 0);
    }

    @Override
    public ActionBar createActionBar(Context context) {
        ActionBar ab = new ActionBar(context, resourcesProvider);
        ab.setBackgroundColor(Color.TRANSPARENT);
        ab.setBackButtonDrawable(new BackDrawable(false));
        ab.setOccupyStatusBar(true);
        ab.setClipContent(true);
        ab.setAddToContainer(false);
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


    private void updateProfileData(boolean reload) {
        if (userId != 0) {
            TLRPC.User user = getMessagesController().getUser(userId);
            if (user == null) {
                return;
            }

            avatarDrawable.setInfo(currentAccount, user);
            peerColor = MessagesController.PeerColor.fromCollectible(user.emoji_status);
            if (peerColor == null) {
                final int colorId = UserObject.getProfileColorId(user);
                final MessagesController.PeerColors peerColors = MessagesController.getInstance(currentAccount).profilePeerColors;
                peerColor = peerColors == null ? null : peerColors.getColor(colorId);
            }

            final ImageLocation imageLocation = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_BIG);
            final ImageLocation thumbLocation = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL);
            final ImageLocation videoThumbLocation = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_VIDEO_BIG);
            VectorAvatarThumbDrawable vectorAvatarThumbDrawable = null;
            TLRPC.VideoSize vectorAvatar = null;
            if (userInfo != null) {
                vectorAvatar = FileLoader.getVectorMarkupVideoSize(user.photo != null && user.photo.personal ? userInfo.personal_photo : userInfo.profile_photo);
                if (vectorAvatar != null) {
                    vectorAvatarThumbDrawable = new VectorAvatarThumbDrawable(vectorAvatar, user.premium, VectorAvatarThumbDrawable.TYPE_PROFILE);
                }
            }
            final ImageLocation videoLocation = avatarsViewPager.getCurrentVideoLocation(thumbLocation, imageLocation);
            avatarsViewPager.initIfEmpty(vectorAvatarThumbDrawable, imageLocation, thumbLocation, reload);
            if (vectorAvatar != null) {
                avatarImage.setImageDrawable(vectorAvatarThumbDrawable);
            } else if (videoThumbLocation != null && !user.photo.personal) {
                avatarImage.getImageReceiver().setVideoThumbIsSame(true);
                avatarImage.setImage(videoThumbLocation, "avatar", thumbLocation, "50_50", avatarDrawable, user);
            } else {
                avatarImage.setImage(videoLocation, ImageLoader.AUTOPLAY_FILTER, thumbLocation, "50_50", avatarDrawable, user);
            }

            if (imageLocation != null && (prevLoadedImageLocation == null || imageLocation.photoId != prevLoadedImageLocation.photoId)) {
                prevLoadedImageLocation = imageLocation;
                getFileLoader().loadFile(imageLocation, user, null, FileLoader.PRIORITY_LOW, 1);
            }
        }
    }

    private void showAvatarProgress(boolean show, boolean animated) {
        if (avatarProgressView == null) {
            return;
        }
//        if (avatarAnimation != null) {
//            avatarAnimation.cancel();
//            avatarAnimation = null;
//        }
//        if (animated) {
//            avatarAnimation = new AnimatorSet();
//            if (show) {
//                avatarProgressView.setVisibility(View.VISIBLE);
//                avatarAnimation.playTogether(ObjectAnimator.ofFloat(avatarProgressView, View.ALPHA, 1.0f));
//            } else {
//                avatarAnimation.playTogether(ObjectAnimator.ofFloat(avatarProgressView, View.ALPHA, 0.0f));
//            }
//            avatarAnimation.setDuration(180);
//            avatarAnimation.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    if (avatarAnimation == null || avatarProgressView == null) {
//                        return;
//                    }
//                    if (!show) {
//                        avatarProgressView.setVisibility(View.INVISIBLE);
//                    }
//                    avatarAnimation = null;
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//                    avatarAnimation = null;
//                }
//            });
//            avatarAnimation.start();
//        } else {
        if (show) {
            avatarProgressView.setAlpha(1.0f);
            avatarProgressView.setVisibility(View.VISIBLE);
        } else {
            avatarProgressView.setAlpha(0.0f);
            avatarProgressView.setVisibility(View.INVISIBLE);
        }
    }

    static class HeaderButtonView extends FrameLayout {

        private final ImageView imageView;
        private final TextView textView;
        private float hideProgress;

        public HeaderButtonView(@NonNull Context context) {
            super(context);
            setWillNotDraw(false);
            imageView = new ImageView(context);
            textView = new TextView(context);
            textView.setTextColor(Color.WHITE);
            textView.setTypeface(AndroidUtilities.bold());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
            addView(imageView, LayoutHelper.createFrame(24, 24, Gravity.CENTER_HORIZONTAL, 0, 6, 0, 0));
            addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0, 0, 6));
            setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(10), ColorUtils.setAlphaComponent(Color.BLACK, 20), ColorUtils.setAlphaComponent(Color.BLACK, 80)));
        }

        public void setTextAndIcon(CharSequence text, int icon) {
            textView.setText(text);
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), icon));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int targetHeight = (int) ((1f - this.hideProgress) * AndroidUtilities.dpf2(HEADER_BUTTON_HEIGHT_DP));
            int heightSpec = MeasureSpec.makeMeasureSpec(targetHeight, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightSpec);
        }

        public void setHideProgress(float hideProgress) {
            this.hideProgress = hideProgress;

            float imageScale = Math.max(0f, 1f - hideProgress * 2);
            imageView.setPivotX(imageView.getWidth() / 2f);
            imageView.setPivotY(0);
            imageView.setScaleX(imageScale);
            imageView.setScaleY(imageScale);

            float textScale = 1f - hideProgress * 0.5f;
            textView.setPivotX(textView.getWidth() / 2f);
            textView.setPivotY(textView.getHeight());
            textView.setScaleX(textScale);
            textView.setScaleY(textScale);

            setAlpha(Math.max(0f, 1f - (hideProgress - 0.25f) * 4));

            requestLayout();
        }
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
        private static final float DROPLET_WIDTH_DP = 33f;
        private static final float DROPLET_HEIGHT_DP = 13f;
        private final Paint paint;
        private final Path connection = new Path();
        private final Paint black = new Paint();

        public TopView(@NonNull Context context) {
            super(context);
            setWillNotDraw(false);
            this.paint = new Paint();
            paint.setColor(getThemedColor(Theme.key_actionBarDefault));
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRect(0, 0, getMeasuredWidth(), Math.max(topScroll, topBarsHeight), paint);

            if (topScroll >= minimizedOffset && topScroll <= expandedOffset && !avatarMaximizeAnimator.isRunning()) {
                float avatarSize = avatarContainer.getScaleY() * dp(AVATAR_SIZE_DP) / 2f;
                canvas.drawCircle((float) getWidth() / 2, avatarContainer.getTranslationY() + avatarSize, avatarSize, black);
                drawConnectionSide(canvas, 1f);
                drawConnectionSide(canvas, -1f);
            }
        }


        private void drawConnectionSide(Canvas canvas, float direction) {
            connection.reset();
            float avatarTop = avatarContainer.getTranslationY();
            float avatarSize = avatarContainer.getScaleY() * dp(AVATAR_SIZE_DP) / 2f;
            float progress = (1f - expandProgress);

            float centerX = getWidth() / 2f;
            float dropletHeight = dpf2(DROPLET_HEIGHT_DP) * progress;

            float flipProgress = 0.45f;
            float circleTouchProgress = 0.6f;
            float widenProgress = Math.max(0f, Math.min(1f, (progress - flipProgress) / (circleTouchProgress - flipProgress)));

            float dropletWidth = lerp(dpf2(DROPLET_WIDTH_DP), avatarSize * 2, widenProgress);

            float p0XOffset = (dropletWidth / 2);
            float circleOffsetX = lerp(avatarSize / 2, avatarSize, widenProgress);

            // Position of midpoint of cubic
            float p0X = centerX - p0XOffset * direction;
            float p0Y = 0;
            connection.moveTo(p0X, p0Y);

            // Angle at midpoint of cubic
            float d = dropletWidth / 4;
            float p1X = (1f - widenProgress) * d;
            float p1Y = 0;

            float p2Y;
            float p2X;
            float p3Y;
            float p3X;

            if (progress < flipProgress) {
                p3X = dropletWidth / 2;
                p3Y = dropletHeight;
                p2X = dropletWidth / 4;
                p2Y = dropletHeight;
            } else {
                // Position of top-left point of circle
                float circleTopLeftY = (float) Math.sqrt(Math.pow(avatarSize, 2) - Math.pow(circleOffsetX, 2));
                p3Y = avatarTop + avatarSize - p0Y - circleTopLeftY;
                p3X = p0XOffset - circleOffsetX;

                // Angle at top-left point of circle
                float dCircle = 50f;
                float length = (float) Math.hypot(circleOffsetX, circleTopLeftY);
                float normalizedX = circleOffsetX / length;
                float normalizedY = circleTopLeftY / length;
                p2X = normalizedY * dCircle + p3X;
                p2Y = -normalizedX * dCircle + p3Y;
            }


            connection.rCubicTo(direction * p1X, p1Y, direction * p2X, p2Y, direction * p3X, p3Y);
            connection.rLineTo((p0XOffset - p3X) * direction, 0);
            connection.lineTo(centerX, p0Y);
            connection.close();

            canvas.drawPath(connection, black);
        }
    }

    private class OverlaysView extends View implements ProfileGalleryView.Callback {

        private final int statusBarHeight = actionBar.getOccupyStatusBar() && !inBubbleMode ? AndroidUtilities.statusBarHeight : 0;

        private final Rect topOverlayRect = new Rect();
        private final Rect bottomOverlayRect = new Rect();
        private final RectF rect = new RectF();

        private final GradientDrawable topOverlayGradient;
        private final GradientDrawable bottomOverlayGradient;
        private final ValueAnimator animator;
        private final float[] animatorValues = new float[]{0f, 1f};
        private final Paint backgroundPaint;
        private final Paint barPaint;
        private final Paint selectedBarPaint;

        private final GradientDrawable[] pressedOverlayGradient = new GradientDrawable[2];
        private final boolean[] pressedOverlayVisible = new boolean[2];
        private final float[] pressedOverlayAlpha = new float[2];

        private boolean isOverlaysVisible;
        private float currentAnimationValue;
        private float alpha = 0f;
        private float[] alphas = null;
        private long lastTime;
        private float previousSelectedProgress;
        private int previousSelectedPotision = -1;
        private float currentProgress;
        private int selectedPosition;

        private float currentLoadingAnimationProgress;
        private int currentLoadingAnimationDirection = 1;

        private int overlayCountVisible;

        public OverlaysView(Context context) {
            super(context);
            setVisibility(GONE);

            barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            barPaint.setColor(0x55ffffff);
            selectedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            selectedBarPaint.setColor(0xffffffff);

            topOverlayGradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0x42000000, 0});
            topOverlayGradient.setShape(GradientDrawable.RECTANGLE);

            bottomOverlayGradient = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0x42000000, 0});
            bottomOverlayGradient.setShape(GradientDrawable.RECTANGLE);

            for (int i = 0; i < 2; i++) {
                final GradientDrawable.Orientation orientation = i == 0 ? GradientDrawable.Orientation.LEFT_RIGHT : GradientDrawable.Orientation.RIGHT_LEFT;
                pressedOverlayGradient[i] = new GradientDrawable(orientation, new int[]{0x32000000, 0});
                pressedOverlayGradient[i].setShape(GradientDrawable.RECTANGLE);
            }

            backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            backgroundPaint.setColor(Color.BLACK);
            backgroundPaint.setAlpha(66);
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(250);
            animator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
            animator.addUpdateListener(anim -> {
                float value = lerp(animatorValues, currentAnimationValue = anim.getAnimatedFraction());
                setAlphaValue(value, true);
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isOverlaysVisible) {
                        setVisibility(GONE);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    setVisibility(VISIBLE);
                }
            });
        }

        public void saveCurrentPageProgress() {
            previousSelectedProgress = currentProgress;
            previousSelectedPotision = selectedPosition;
            currentLoadingAnimationProgress = 0.0f;
            currentLoadingAnimationDirection = 1;
        }

        public void setAlphaValue(float value, boolean self) {
            if (Build.VERSION.SDK_INT > 18) {
                int alpha = (int) (255 * value);
                topOverlayGradient.setAlpha(alpha);
                bottomOverlayGradient.setAlpha(alpha);
                backgroundPaint.setAlpha((int) (66 * value));
                barPaint.setAlpha((int) (0x55 * value));
                selectedBarPaint.setAlpha(alpha);
                this.alpha = value;
            } else {
                setAlpha(value);
            }
            if (!self) {
                currentAnimationValue = value;
            }
            invalidate();
        }

        public boolean isOverlaysVisible() {
            return isOverlaysVisible;
        }

        public void setOverlaysVisible() {
            isOverlaysVisible = true;
            setVisibility(VISIBLE);
        }

        public void setOverlaysVisible(boolean overlaysVisible, float durationFactor) {
            if (overlaysVisible != isOverlaysVisible) {
                isOverlaysVisible = overlaysVisible;
                animator.cancel();
                final float value = lerp(animatorValues, currentAnimationValue);
                if (overlaysVisible) {
                    animator.setDuration((long) ((1f - value) * 250f / durationFactor));
                } else {
                    animator.setDuration((long) (value * 250f / durationFactor));
                }
                animatorValues[0] = value;
                animatorValues[1] = overlaysVisible ? 1f : 0f;
                animator.start();
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            final int actionBarHeight = statusBarHeight + ActionBar.getCurrentActionBarHeight();
            final float k = 0.5f;
            topOverlayRect.set(0, 0, w, (int) (actionBarHeight * k));
            bottomOverlayRect.set(0, (int) (h - AndroidUtilities.dp(72f) * k), w, h);
            topOverlayGradient.setBounds(0, topOverlayRect.bottom, w, actionBarHeight + AndroidUtilities.dp(16f));
            bottomOverlayGradient.setBounds(0, h - AndroidUtilities.dp(72f) - AndroidUtilities.dp(24f), w, bottomOverlayRect.top);
            pressedOverlayGradient[0].setBounds(0, 0, w / 5, h);
            pressedOverlayGradient[1].setBounds(w - (w / 5), 0, w, h);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            for (int i = 0; i < 2; i++) {
                if (pressedOverlayAlpha[i] > 0f) {
                    pressedOverlayGradient[i].setAlpha((int) (pressedOverlayAlpha[i] * 255));
                    pressedOverlayGradient[i].draw(canvas);
                }
            }

            topOverlayGradient.draw(canvas);
            bottomOverlayGradient.draw(canvas);
            canvas.drawRect(topOverlayRect, backgroundPaint);
            canvas.drawRect(bottomOverlayRect, backgroundPaint);

            int count = avatarsViewPager.getRealCount();
            selectedPosition = avatarsViewPager.getRealPosition();

            if (alphas == null || alphas.length != count) {
                alphas = new float[count];
                Arrays.fill(alphas, 0.0f);
            }

            boolean invalidate = false;

            long newTime = SystemClock.elapsedRealtime();
            long dt = (newTime - lastTime);
            if (dt < 0 || dt > 20) {
                dt = 17;
            }
            lastTime = newTime;

            if (count > 1 && count <= 20) {
                if (overlayCountVisible == 0) {
                    alpha = 0.0f;
                    overlayCountVisible = 3;
                } else if (overlayCountVisible == 1) {
                    alpha = 0.0f;
                    overlayCountVisible = 2;
                }
                if (overlayCountVisible == 2) {
                    barPaint.setAlpha((int) (0x55 * alpha));
                    selectedBarPaint.setAlpha((int) (0xff * alpha));
                }
                int width = (getMeasuredWidth() - AndroidUtilities.dp(5 * 2) - AndroidUtilities.dp(2 * (count - 1))) / count;
                int y = AndroidUtilities.dp(4) + (Build.VERSION.SDK_INT >= 21 && !inBubbleMode ? AndroidUtilities.statusBarHeight : 0);
                for (int a = 0; a < count; a++) {
                    int x = AndroidUtilities.dp(5 + a * 2) + width * a;
                    float progress;
                    int baseAlpha = 0x55;
                    if (a == previousSelectedPotision && Math.abs(previousSelectedProgress - 1.0f) > 0.0001f) {
                        progress = previousSelectedProgress;
                        canvas.save();
                        canvas.clipRect(x + width * progress, y, x + width, y + AndroidUtilities.dp(2));
                        rect.set(x, y, x + width, y + AndroidUtilities.dp(2));
                        barPaint.setAlpha((int) (0x55 * alpha));
                        canvas.drawRoundRect(rect, AndroidUtilities.dp(1), AndroidUtilities.dp(1), barPaint);
                        baseAlpha = 0x50;
                        canvas.restore();
                        invalidate = true;
                    } else if (a == selectedPosition) {
                        if (avatarsViewPager.isCurrentItemVideo()) {
                            progress = currentProgress = avatarsViewPager.getCurrentItemProgress();
                            if (progress <= 0 && avatarsViewPager.isLoadingCurrentVideo() || currentLoadingAnimationProgress > 0.0f) {
                                currentLoadingAnimationProgress += currentLoadingAnimationDirection * dt / 500.0f;
                                if (currentLoadingAnimationProgress > 1.0f) {
                                    currentLoadingAnimationProgress = 1.0f;
                                    currentLoadingAnimationDirection *= -1;
                                } else if (currentLoadingAnimationProgress <= 0) {
                                    currentLoadingAnimationProgress = 0.0f;
                                    currentLoadingAnimationDirection *= -1;
                                }
                            }
                            rect.set(x, y, x + width, y + AndroidUtilities.dp(2));
                            barPaint.setAlpha((int) ((0x55 + 0x30 * currentLoadingAnimationProgress) * alpha));
                            canvas.drawRoundRect(rect, AndroidUtilities.dp(1), AndroidUtilities.dp(1), barPaint);
                            invalidate = true;
                            baseAlpha = 0x50;
                        } else {
                            progress = currentProgress = 1.0f;
                        }
                    } else {
                        progress = 1.0f;
                    }
                    rect.set(x, y, x + width * progress, y + AndroidUtilities.dp(2));

                    if (a != selectedPosition) {
                        if (overlayCountVisible == 3) {
                            barPaint.setAlpha((int) (lerp(baseAlpha, 0xff, CubicBezierInterpolator.EASE_BOTH.getInterpolation(alphas[a])) * alpha));
                        }
                    } else {
                        alphas[a] = 0.75f;
                    }

                    canvas.drawRoundRect(rect, AndroidUtilities.dp(1), AndroidUtilities.dp(1), a == selectedPosition ? selectedBarPaint : barPaint);
                }

                if (overlayCountVisible == 2) {
                    if (alpha < 1.0f) {
                        alpha += dt / 180.0f;
                        if (alpha > 1.0f) {
                            alpha = 1.0f;
                        }
                        invalidate = true;
                    } else {
                        overlayCountVisible = 3;
                    }
                } else if (overlayCountVisible == 3) {
                    for (int i = 0; i < alphas.length; i++) {
                        if (i != selectedPosition && alphas[i] > 0.0f) {
                            alphas[i] -= dt / 500.0f;
                            if (alphas[i] <= 0.0f) {
                                alphas[i] = 0.0f;
                                if (i == previousSelectedPotision) {
                                    previousSelectedPotision = -1;
                                }
                            }
                            invalidate = true;
                        } else if (i == previousSelectedPotision) {
                            previousSelectedPotision = -1;
                        }
                    }
                }
            }

            for (int i = 0; i < 2; i++) {
                if (pressedOverlayVisible[i]) {
                    if (pressedOverlayAlpha[i] < 1f) {
                        pressedOverlayAlpha[i] += dt / 180.0f;
                        if (pressedOverlayAlpha[i] > 1f) {
                            pressedOverlayAlpha[i] = 1f;
                        }
                        invalidate = true;
                    }
                } else {
                    if (pressedOverlayAlpha[i] > 0f) {
                        pressedOverlayAlpha[i] -= dt / 180.0f;
                        if (pressedOverlayAlpha[i] < 0f) {
                            pressedOverlayAlpha[i] = 0f;
                        }
                        invalidate = true;
                    }
                }
            }

            if (invalidate) {
                postInvalidateOnAnimation();
            }
        }

        @Override
        public void onDown(boolean left) {
            pressedOverlayVisible[left ? 0 : 1] = true;
            postInvalidateOnAnimation();
        }

        @Override
        public void onRelease() {
            Arrays.fill(pressedOverlayVisible, false);
            postInvalidateOnAnimation();
        }

        @Override
        public void onPhotosLoaded() {
            updateProfileData(false);
        }

        @Override
        public void onVideoSet() {
            invalidate();
        }
    }

    private class PagerIndicatorView extends View {

        private final RectF indicatorRect = new RectF();

        private final TextPaint textPaint;
        private final Paint backgroundPaint;

        private final ValueAnimator animator;
        private final float[] animatorValues = new float[]{0f, 1f};

        private final PagerAdapter adapter = avatarsViewPager.getAdapter();

        private boolean isIndicatorVisible;

        public PagerIndicatorView(Context context) {
            super(context);
            setVisibility(GONE);

            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(Typeface.SANS_SERIF);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(dpf2(15f));
            backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            backgroundPaint.setColor(0x26000000);
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
            animator.addUpdateListener(a -> {
                final float value = lerp(animatorValues, a.getAnimatedFraction());
//                if (searchItem != null && !isPulledDown) {
//                    searchItem.setScaleX(1f - value);
//                    searchItem.setScaleY(1f - value);
//                    searchItem.setAlpha(1f - value);
//                }
//                if (editItemVisible) {
//                    editItem.setScaleX(1f - value);
//                    editItem.setScaleY(1f - value);
//                    editItem.setAlpha(1f - value);
//                }
                setScaleX(value);
                setScaleY(value);
                setAlpha(value);
            });
//            boolean expanded = expandPhoto;
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
//                    if (isIndicatorVisible) {
//                        if (searchItem != null) {
//                            searchItem.setClickable(false);
//                        }
//                        if (editItemVisible) {
//                            editItem.setVisibility(GONE);
//                        }
//                    } else {
//                        setVisibility(GONE);
//                    }
//                    updateStoriesViewBounds(false);
                }

                @Override
                public void onAnimationStart(Animator animation) {
//                    if (searchItem != null && !expanded) {
//                        searchItem.setClickable(true);
//                    }
//                    if (editItemVisible) {
//                        editItem.setVisibility(VISIBLE);
//                    }
//                    setVisibility(VISIBLE);
//                    updateStoriesViewBounds(false);
                }
            });
            avatarsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                private int prevPage;

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    int realPosition = avatarsViewPager.getRealPosition(position);
                    invalidateIndicatorRect(prevPage != realPosition);
                    prevPage = realPosition;
                    updateAvatarItems();
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
//            adapter.registerDataSetObserver(new DataSetObserver() {
//                @Override
//                public void onChanged() {
//                    int count = avatarsViewPager.getRealCount();
//                    if (overlayCountVisible == 0 && count > 1 && count <= 20 && overlaysView.isOverlaysVisible()) {
//                        overlayCountVisible = 1;
//                    }
//                    invalidateIndicatorRect(false);
//                    refreshVisibility(1f);
//                    updateAvatarItems();
//                }
//            });
        }

        private void updateAvatarItemsInternal() {
//            if (otherItem == null || avatarsViewPager == null) {
//                return;
//            }
//            if (isPulledDown) {
//                int position = avatarsViewPager.getRealPosition();
//                if (position == 0) {
//                    otherItem.hideSubItem(set_as_main);
//                    otherItem.showSubItem(add_photo);
//                } else {
//                    otherItem.showSubItem(set_as_main);
//                    otherItem.hideSubItem(add_photo);
//                }
//            }
        }

        private void updateAvatarItems() {
//            if (imageUpdater == null) {
//                return;
//            }
//            if (otherItem.isSubMenuShowing()) {
//                AndroidUtilities.runOnUIThread(this::updateAvatarItemsInternal, 500);
//            } else {
//                updateAvatarItemsInternal();
//            }
        }

        public boolean isIndicatorVisible() {
            return isIndicatorVisible;
        }

        public boolean isIndicatorFullyVisible() {
            return isIndicatorVisible && !animator.isRunning();
        }

        public void setIndicatorVisible(boolean indicatorVisible, float durationFactor) {
            if (indicatorVisible != isIndicatorVisible) {
                isIndicatorVisible = indicatorVisible;
                animator.cancel();
                final float value = lerp(animatorValues, animator.getAnimatedFraction());
                if (durationFactor <= 0f) {
                    animator.setDuration(0);
                } else if (indicatorVisible) {
                    animator.setDuration((long) ((1f - value) * 250f / durationFactor));
                } else {
                    animator.setDuration((long) (value * 250f / durationFactor));
                }
                animatorValues[0] = value;
                animatorValues[1] = indicatorVisible ? 1f : 0f;
                animator.start();
            }
        }

        public void refreshVisibility(float durationFactor) {
//            setIndicatorVisible(isPulledDown && avatarsViewPager.getRealCount() > 20, durationFactor);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            invalidateIndicatorRect(false);
        }

        private void invalidateIndicatorRect(boolean pageChanged) {
//            if (pageChanged) {
//                overlaysView.saveCurrentPageProgress();
//            }
//            overlaysView.invalidate();
//            final float textWidth = textPaint.measureText(getCurrentTitle());
//            indicatorRect.right = getMeasuredWidth() - AndroidUtilities.dp(54f) - (qrItem != null ? AndroidUtilities.dp(48) : 0);
//            indicatorRect.left = indicatorRect.right - (textWidth + AndroidUtilities.dpf2(16f));
//            indicatorRect.top = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.dp(15f);
//            indicatorRect.bottom = indicatorRect.top + AndroidUtilities.dp(26);
//            setPivotX(indicatorRect.centerX());
//            setPivotY(indicatorRect.centerY());
//            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            final float radius = dpf2(12);
            canvas.drawRoundRect(indicatorRect, radius, radius, backgroundPaint);
            canvas.drawText(getCurrentTitle(), indicatorRect.centerX(), indicatorRect.top + dpf2(18.5f), textPaint);
        }

        private String getCurrentTitle() {
            return adapter.getPageTitle(avatarsViewPager.getCurrentItem()).toString();
        }

        private ActionBarMenuItem getSecondaryMenuItem() {
//            if (editItemVisible) {
//                return editItem;
//            } else if (searchItem != null) {
//                return searchItem;
//            } else {
            return null;
//            }
        }
    }
}

