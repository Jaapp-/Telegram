package org.telegram.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SharedMediaLayout;

import java.util.ArrayList;

public class ContestProfileActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, DialogsActivity.DialogsActivityDelegate, SharedMediaLayout.SharedMediaPreloaderDelegate, ImageUpdater.ImageUpdaterDelegate, SharedMediaLayout.Delegate {
    public static String TAG = "ContestProfile";

    private final SharedMediaLayout.SharedMediaPreloader sharedMediaPreloader;

    public ContestProfileActivity(Bundle args) {
        this(args, null);
    }

    public ContestProfileActivity(Bundle args, SharedMediaLayout.SharedMediaPreloader preloader) {
        super(args);
        sharedMediaPreloader = preloader;
    }

    public static ContestProfileActivity of(long dialogId) {
        Bundle bundle = new Bundle();
        if (dialogId >= 0) {
            bundle.putLong("user_id", dialogId);
        } else {
            bundle.putLong("chat_id", -dialogId);
        }
        return new ContestProfileActivity(bundle);
    }


    @Override
    public View createView(Context context) {
        TextView textView = new TextView(context);
        textView.setText("Hello from BaseFragment!");
        textView.setTextSize(18);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    public void setPlayProfileAnimation(int type) {
        Log.i(TAG, "setPlayProfileAnimation: ");
//        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
//        if (!AndroidUtilities.isTablet()) {
//            needTimerImage = type != 0;
//            needStarImage = type != 0;
//            if (preferences.getBoolean("view_animations", true)) {
//                playProfileAnimation = type;
//            } else if (type == 2) {
//                expandPhoto = true;
//            }
//        }
    }


    public void setUserInfo(TLRPC.UserFull value, ProfileChannelCell.ChannelMessageFetcher channelMessageFetcher, ProfileBirthdayEffect.BirthdayEffectFetcher birthdayAssetsFetcher) {
        Log.i(TAG, "setUserInfo: ");
//        userInfo = value;
//        if (storyView != null) {
//            storyView.setStories(userInfo.stories);
//        }
//        if (giftsView != null) {
//            giftsView.update();
//        }
//        if (avatarImage != null) {
//            avatarImage.setHasStories(needInsetForStories());
//        }
//        if (sharedMediaLayout != null) {
//            sharedMediaLayout.setUserInfo(userInfo);
//        }
//        if (profileChannelMessageFetcher == null) {
//            profileChannelMessageFetcher = channelMessageFetcher;
//        }
//        if (profileChannelMessageFetcher == null) {
//            profileChannelMessageFetcher = new ProfileChannelCell.ChannelMessageFetcher(currentAccount);
//        }
//        profileChannelMessageFetcher.subscribe(() -> updateListAnimated(false));
//        profileChannelMessageFetcher.fetch(userInfo);
//        if (birthdayFetcher == null) {
//            birthdayFetcher = birthdayAssetsFetcher;
//        }
//        if (birthdayFetcher == null) {
//            birthdayFetcher = ProfileBirthdayEffect.BirthdayEffectFetcher.of(currentAccount, userInfo, birthdayFetcher);
//            createdBirthdayFetcher = birthdayFetcher != null;
//        }
//        if (birthdayFetcher != null) {
//            birthdayFetcher.subscribe(this::createBirthdayEffect);
//        }
//        if (otherItem != null) {
//            otherItem.setSubItemShown(start_secret_chat, DialogObject.isEmpty(getMessagesController().isUserContactBlocked(userId)));
//            if (hasPrivacyCommand()) {
//                otherItem.showSubItem(bot_privacy);
//            } else {
//                otherItem.hideSubItem(bot_privacy);
//            }
//        }
    }


    public void setChatInfo(TLRPC.ChatFull value) {
        Log.i(TAG, "setChatInfo: ");
//        chatInfo = value;
//        if (chatInfo != null && chatInfo.migrated_from_chat_id != 0 && mergeDialogId == 0) {
//            mergeDialogId = -chatInfo.migrated_from_chat_id;
//            getMediaDataController().getMediaCounts(mergeDialogId, topicId, classGuid);
//        }
//        if (sharedMediaLayout != null) {
//            sharedMediaLayout.setChatInfo(chatInfo);
//        }
//        if (avatarsViewPager != null && !isTopic) {
//            avatarsViewPager.setChatInfo(chatInfo);
//        }
//        if (storyView != null && chatInfo != null) {
//            storyView.setStories(chatInfo.stories);
//        }
//        if (giftsView != null) {
//            giftsView.update();
//        }
//        if (avatarImage != null) {
//            avatarImage.setHasStories(needInsetForStories());
//        }
//        fetchUsersFromChannelInfo();
//        if (chatId != 0) {
//            otherItem.setSubItemShown(gift_premium, !BuildVars.IS_BILLING_UNAVAILABLE && !getMessagesController().premiumPurchaseBlocked() && chatInfo != null && chatInfo.stargifts_available);
//        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

    }

    @Override
    public void didUploadPhoto(TLRPC.InputFile photo, TLRPC.InputFile video, double videoStartTimestamp, String videoPath, TLRPC.PhotoSize bigSize, TLRPC.PhotoSize smallSize, boolean isVideo, TLRPC.VideoSize emojiMarkup) {

    }

    @Override
    public void scrollToSharedMedia() {

    }

    @Override
    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean b, boolean resultOnly, View view) {
        return false;
    }

    @Override
    public TLRPC.Chat getCurrentChat() {
        return null;
    }

    @Override
    public boolean isFragmentOpened() {
        return false;
    }

    @Override
    public RecyclerListView getListView() {
        return null;
    }

    @Override
    public boolean canSearchMembers() {
        return false;
    }

    @Override
    public void updateSelectedMediaTabText() {

    }

    @Override
    public void mediaCountUpdated() {

    }

    @Override
    public boolean didSelectDialogs(DialogsActivity fragment, ArrayList<MessagesStorage.TopicKey> dids, CharSequence message, boolean param, boolean notify, int scheduleDate, TopicsFragment topicsFragment) {
        return false;
    }
}