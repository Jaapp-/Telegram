package org.telegram.ui.contest;

import static androidx.core.view.ViewCompat.TYPE_TOUCH;
import static org.telegram.messenger.AndroidUtilities.displaySize;
import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.AndroidUtilities.lerp;
import static org.telegram.messenger.AndroidUtilities.statusBarHeight;
import static org.telegram.messenger.ContactsController.PRIVACY_RULES_TYPE_ADDED_BY_PHONE;
import static org.telegram.messenger.LocaleController.formatPluralString;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.messenger.Utilities.clamp01;
import static org.telegram.ui.Stars.StarsIntroActivity.formatStarsAmountShort;
import static org.telegram.ui.bots.AffiliateProgramFragment.percents;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Property;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
import androidx.core.math.MathUtils;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScrollerCustom;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BillingController;
import org.telegram.messenger.BirthdayController;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.DocumentObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.FlagSecureReason;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.SvgHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.tgnet.tl.TL_bots;
import org.telegram.tgnet.tl.TL_fragment;
import org.telegram.tgnet.tl.TL_stars;
import org.telegram.tgnet.tl.TL_stories;
import org.telegram.ui.AccountFrozenAlert;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.OKLCH;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.AutoDeleteMessagesActivity;
import org.telegram.ui.AvatarImageView;
import org.telegram.ui.BasePermissionsActivity;
import org.telegram.ui.Business.OpeningHoursActivity;
import org.telegram.ui.Business.ProfileHoursCell;
import org.telegram.ui.Business.ProfileLocationCell;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.SettingsSuggestionCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChangeUsernameActivity;
import org.telegram.ui.ChannelMonetizationLayout;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.ChatEditActivity;
import org.telegram.ui.ChatRightsEditActivity;
import org.telegram.ui.ChatUsersActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AnimatedColor;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedEmojiSpan;
import org.telegram.ui.Components.AnimatedFileDrawable;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.AnimationProperties;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.AutoDeletePopupWrapper;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ButtonBounce;
import org.telegram.ui.Components.ChatActivityInterface;
import org.telegram.ui.Components.ChatNotificationsPopupWrapper;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CrossfadeDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.DotDividerSpan;
import org.telegram.ui.Components.EmptyStubSpan;
import org.telegram.ui.Components.Forum.ForumUtilities;
import org.telegram.ui.Components.IdenticonDrawable;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.JoinGroupAlert;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.MediaActivity;
import org.telegram.ui.Components.MessagePrivateSeenView;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.Premium.PremiumGradient;
import org.telegram.ui.Components.Premium.PremiumPreviewBottomSheet;
import org.telegram.ui.Components.Premium.ProfilePremiumCell;
import org.telegram.ui.Components.Premium.boosts.UserSelectorBottomSheet;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.Reactions.ReactionsLayoutInBubble;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ScamDrawable;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.SharedMediaLayout;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.TimerDrawable;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.Components.VectorAvatarThumbDrawable;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.ContactAddActivity;
import org.telegram.ui.DataSettingsActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.FiltersSetupActivity;
import org.telegram.ui.FragmentUsernameBottomSheet;
import org.telegram.ui.Gifts.GiftSheet;
import org.telegram.ui.GroupCreateActivity;
import org.telegram.ui.IProfileActivity;
import org.telegram.ui.IdenticonActivity;
import org.telegram.ui.LanguageSelectActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LiteModeSettingsActivity;
import org.telegram.ui.LocationActivity;
import org.telegram.ui.LogoutActivity;
import org.telegram.ui.MemberRequestsActivity;
import org.telegram.ui.NotificationsSettingsActivity;
import org.telegram.ui.PeerColorActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.PinchToZoomHelper;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.PrivacyControlActivity;
import org.telegram.ui.PrivacySettingsActivity;
import org.telegram.ui.ProfileBirthdayEffect;
import org.telegram.ui.ProfileNotificationsActivity;
import org.telegram.ui.QrActivity;
import org.telegram.ui.ReportBottomSheet;
import org.telegram.ui.RestrictedLanguagesSelectActivity;
import org.telegram.ui.SelectAnimatedEmojiDialog;
import org.telegram.ui.SessionsActivity;
import org.telegram.ui.Stars.BotStarsActivity;
import org.telegram.ui.Stars.BotStarsController;
import org.telegram.ui.Stars.ProfileGiftsView;
import org.telegram.ui.Stars.StarGiftPatterns;
import org.telegram.ui.Stars.StarGiftSheet;
import org.telegram.ui.Stars.StarsController;
import org.telegram.ui.Stars.StarsIntroActivity;
import org.telegram.ui.StatisticActivity;
import org.telegram.ui.StickersActivity;
import org.telegram.ui.Stories.ProfileStoriesView;
import org.telegram.ui.Stories.StoriesController;
import org.telegram.ui.Stories.StoryViewer;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;
import org.telegram.ui.Stories.recorder.HintView2;
import org.telegram.ui.Stories.recorder.StoryRecorder;
import org.telegram.ui.ThemeActivity;
import org.telegram.ui.TopicCreateFragment;
import org.telegram.ui.TopicsFragment;
import org.telegram.ui.TopicsNotifySettingsFragments;
import org.telegram.ui.TwoStepVerificationSetupActivity;
import org.telegram.ui.UserInfoActivity;
import org.telegram.ui.bots.AffiliateProgramFragment;
import org.telegram.ui.bots.BotBiometry;
import org.telegram.ui.bots.BotLocation;
import org.telegram.ui.bots.BotWebViewAttachedSheet;
import org.telegram.ui.bots.ChannelAffiliateProgramsFragment;
import org.telegram.ui.bots.SetupEmojiStatusSheet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DebugProfile extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, ImageUpdater.ImageUpdaterDelegate, SharedMediaLayout.SharedMediaPreloaderDelegate, SharedMediaLayout.Delegate, DialogsActivity.DialogsActivityDelegate, IProfileActivity {
    private static final String TAG = "Contest";

    private final static int PHONE_OPTION_CALL = 0, PHONE_OPTION_COPY = 1, PHONE_OPTION_TELEGRAM_CALL = 2, PHONE_OPTION_TELEGRAM_VIDEO_CALL = 3;

    private final static float HEADER_BUTTON_HEIGHT_DP = 54;
    private final static float HEADER_BUTTON_MARGIN_DP = 12;
    private final static float AVATAR_SIZE_DP = 90;
    private final static float AVATAR_EXPAND_THRESHOLD = 0.4f;
    private final static int add_contact = 1;
    private final static int block_contact = 2;
    private final static int share_contact = 3;
    private final static int edit_contact = 4;
    private final static int delete_contact = 5;
    private final static int leave_group = 7;
    private final static int invite_to_group = 9;
    private final static int share = 10;
    private final static int edit_channel = 12;
    private final static int add_shortcut = 14;
    private final static int call_item = 15;
    private final static int video_call_item = 16;
    private final static int search_members = 17;
    private final static int add_member = 18;
    private final static int statistics = 19;
    private final static int start_secret_chat = 20;
    private final static int gallery_menu_save = 21;
    private final static int view_discussion = 22;
    private final static int delete_topic = 23;
    private final static int report = 24;
    private final static int edit_info = 30;
    private final static int logout = 31;
    private final static int search_button = 32;
    private final static int set_as_main = 33;
    private final static int edit_avatar = 34;
    private final static int delete_avatar = 35;
    private final static int add_photo = 36;
    private final static int qr_button = 37;
    private final static int gift_premium = 38;
    private final static int channel_stories = 39;
    private final static int edit_color = 40;
    private final static int edit_profile = 41;
    private final static int copy_link_profile = 42;
    private final static int set_username = 43;
    private final static int bot_privacy = 44;
    private final ArrayList<TLRPC.ChatParticipant> visibleChatParticipants = new ArrayList<>();
    private final ArrayList<Integer> visibleSortedUsers = new ArrayList<>();
    private final SparseIntArray adaptedColors = new SparseIntArray();
    private final boolean fragmentOpened = true; // TODO animation
    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[] emojiStatusDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[2];
    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[] botVerificationDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[2];
    private final CrossfadeDrawable[] verifiedCrossfadeDrawable = new CrossfadeDrawable[2];
    private final CrossfadeDrawable[] premiumCrossfadeDrawable = new CrossfadeDrawable[2];
    private final Drawable[] verifiedDrawable = new Drawable[2];
    private final Drawable[] premiumStarDrawable = new Drawable[2];
    public SharedMediaLayout sharedMediaLayout;
    public int birthdayRow;
    public boolean saved;
    public boolean isTopic;
    public boolean myProfile;
    public ProfileGiftsView giftsView;
    protected float headerShadowAlpha = 1.0f;
    HashSet<Integer> notificationsExceptionTopics = new HashSet<>();
    float photoDescriptionProgress = -1;
    private SharedMediaLayout.SharedMediaPreloader sharedMediaPreloader;
    private TLRPC.UserFull userInfo;
    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private Theme.ResourcesProvider resourcesProvider;
    private int topScroll;
    private TopView topView;
    private final Property<DebugProfile, Float> HEADER_SHADOW = new AnimationProperties.FloatProperty<DebugProfile>("headerShadow") {

        @Override
        public void setValue(DebugProfile object, float value) {
            headerShadowAlpha = value;
            topView.invalidate();
        }

        @Override
        public Float get(DebugProfile object) {
            return headerShadowAlpha;
        }
    };
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
    private boolean openSimilar;
    private long banFromGroup;
    private int reportReactionMessageId;
    private long reportReactionFromDialogId;
    private boolean showAddToContacts;
    private String vcardPhone;
    private String vcardFirstName;
    private String vcardLastName;
    private boolean reportSpam;
    private boolean openGifts;
    private boolean openCommonChats;
    private long dialogId;
    private TLRPC.EncryptedChat currentEncryptedChat;
    private FlagSecureReason flagSecure;
    private boolean userBlocked;
    private boolean isBot;
    private LongSparseArray<TLRPC.ChatParticipant> participantsMap = new LongSparseArray<>();
    private ImageUpdater imageUpdater;
    private int actionBarAnimationColorFrom;
    private TLRPC.Chat currentChat;
    private ArrayList<Integer> sortedUsers;
    private int onlineCount;
    private TLRPC.ChatFull chatInfo;
    private ListAdapter listAdapter;
    private TL_account.TL_password currentPassword;
    private FrameLayout bottomButtonsContainer;
    private FrameLayout[] bottomButtonContainer;
    private ButtonWithCounterView[] bottomButton;
    private SpannableStringBuilder bottomButtonPostText;
    private boolean openedGifts;
    private int rowCount;
    private int setAvatarRow;
    private int setAvatarSectionRow;
    private int channelRow;
    private int channelDividerRow;
    private int numberSectionRow;
    private int numberRow;
    private int setUsernameRow;
    private int bioRow;
    private int phoneSuggestionSectionRow;
    private int graceSuggestionRow;
    private int graceSuggestionSectionRow;
    private int phoneSuggestionRow;
    private int passwordSuggestionSectionRow;
    private int passwordSuggestionRow;
    private int settingsSectionRow;
    private int settingsSectionRow2;
    private int notificationRow;
    private int languageRow;
    private int privacyRow;
    private int dataRow;
    private int chatRow;
    private int filtersRow;
    private int liteModeRow;
    private int stickersRow;
    private int devicesRow;
    private int devicesSectionRow;
    private int helpHeaderRow;
    private int questionRow;
    private int faqRow;
    private int policyRow;
    private int helpSectionCell;
    private int debugHeaderRow;
    private int sendLogsRow;
    private int sendLastLogsRow;
    private int clearLogsRow;
    private int switchBackendRow;
    private int versionRow;
    private int emptyRow;
    private int bottomPaddingRow;
    private int infoHeaderRow;
    private int phoneRow;
    private int locationRow;
    private int userInfoRow;
    private int channelInfoRow;
    private int usernameRow;
    private int notificationsDividerRow;
    private int notificationsRow;
    private int bizHoursRow;
    private int bizLocationRow;
    private int notificationsSimpleRow;
    private int infoStartRow, infoEndRow;
    private int infoSectionRow;
    private int affiliateRow;
    private int infoAffiliateRow;
    private int sendMessageRow;
    private int reportRow;
    private int reportReactionRow;
    private int reportDividerRow;
    private int addToContactsRow;
    private int addToGroupButtonRow;
    private int addToGroupInfoRow;
    private int premiumRow;
    private int starsRow;
    private int businessRow;
    private int premiumGiftingRow;
    private int premiumSectionsRow;
    private int botAppRow;
    private int botPermissionsHeader;
    @Keep
    private int botPermissionLocation;
    @Keep
    private int botPermissionEmojiStatus;
    private int botPermissionEmojiStatusReqId;
    @Keep
    private int botPermissionBiometry;
    private int botPermissionsDivider;
    private int settingsTimerRow;
    private int settingsKeyRow;
    private int secretSettingsSectionRow;
    private int membersHeaderRow;
    private int membersStartRow;
    private int membersEndRow;
    private int addMemberRow;
    private int subscribersRow;
    private int subscribersRequestsRow;
    private int administratorsRow;
    private int settingsRow;
    private int botStarsBalanceRow;
    private int botTonBalanceRow;
    private int channelBalanceRow;
    private int channelBalanceSectionRow;
    private int balanceDividerRow;
    private int blockedUsersRow;
    private int membersSectionRow;
    private int sharedMediaRow;
    private int unblockRow;
    private int joinRow;
    private int lastSectionRow;
    private int usersForceShowingIn = 0;
    private Runnable applyBulletin;
    private UndoView undoView;
    private TLRPC.FileLocation avatar;
    private TLRPC.FileLocation avatarBig;
    private ImageLocation uploadingImageLocation;
    private int avatarUploadingRequest;
    private boolean waitCanSendStoryRequest;
    private boolean showBoostsAlert;
    private ProfileChannelCell.ChannelMessageFetcher profileChannelMessageFetcher;
    private BotLocation botLocation;
    private BotBiometry botBiometry;
    private boolean editItemVisible;
    private boolean callItemVisible;
    private boolean videoCallItemVisible;
    private boolean canSearchMembers;
    private boolean searchMode;
    private ActionBarMenuItem editItem;
    private ActionBarMenuItem otherItem;
    private ImageView ttlIconView;
    private boolean isInLandscapeMode;
    private int playProfileAnimation;
    private boolean disableProfileAnimation;
    private TL_bots.BotInfo botInfo;
    private DialogsActivity previousTransitionMainFragment;
    private boolean hasFallbackPhoto;
    private boolean hasCustomPhoto;
    private boolean doNotSetForeground;
    private ImageReceiver fallbackImage;
    private ProfileStoriesView storyView;
    private ActionBarMenuSubItem editColorItem;
    private boolean hasVoiceChatItem;
    private long mergeDialogId;
    private boolean loadingUsers;
    private AutoDeletePopupWrapper autoDeletePopupWrapper;
    private TimerDrawable autoDeleteItemDrawable;
    private ActionBarMenuSubItem autoDeleteItem;
    private AboutLinkCell aboutLinkCell;
    private boolean sharedMediaLayoutAttached;
    private CharacterStyle loadingSpan;
    private boolean isFragmentPhoneNumber;
    private String currentBio;
    private RLottieDrawable cellCameraDrawable;
    private TextCell setAvatarCell;
    private boolean hoursExpanded;
    private boolean hoursShownMine;
    private ProfileBirthdayEffect.BirthdayEffectFetcher birthdayFetcher;
    private boolean createdBirthdayFetcher;
    private ProfileBirthdayEffect birthdayEffect;
    private boolean usersEndReached;
    private boolean firstLayout;
    private ActionBarMenuSubItem setUsernameItem;
    private ActionBarMenuSubItem linkItem;
    private AnimatorSet headerAnimatorSet;
    private AnimatorSet headerShadowAnimatorSet;
    private SimpleTextView[] nameTextView = new SimpleTextView[2];
    private SimpleTextView[] onlineTextView = new SimpleTextView[4];
    private String nameTextViewRightDrawableContentDescription = null;
    private String nameTextViewRightDrawable2ContentDescription = null;
    private HintView2 collectibleHint;
    private int collectibleHintBackgroundColor;
    private Boolean collectibleHintVisible;
    private TLRPC.TL_emojiStatusCollectible collectibleStatus;
    private boolean[] isOnline = new boolean[1];
    private float mediaHeaderAnimationProgress;
    private boolean mediaHeaderVisible;
    private boolean expandPhoto;
    private boolean needTimerImage;
    private boolean needStarImage;
    private int overlayCountVisible;
    private OverlaysView overlaysView;
    private int actionBarBackgroundColor;
    private SizeNotifierFrameLayout contentView;
    private boolean openAnimationInProgress;
    private float customAvatarProgress;
    private float currentExpandAnimatorValue;
    private ShowDrawable showStatusButton;
    private float customPhotoOffset;
    private float onlineX;
    private Drawable lockIconDrawable;
    private ScamDrawable scamDrawable;
    private Drawable[] verifiedCheckDrawable = new Drawable[2];
    private boolean fragmentViewAttached;
    private float lastEmojiStatusProgress;
    private DrawerProfileCell.AnimatedStatusView animatedStatusView;
    private final Property<ActionBar, Float> ACTIONBAR_HEADER_PROGRESS = new AnimationProperties.FloatProperty<ActionBar>("avatarAnimationProgress") {
        @Override
        public void setValue(ActionBar object, float value) {
            mediaHeaderAnimationProgress = value;
            if (storyView != null) {
                storyView.setActionBarActionMode(value);
            }
            if (giftsView != null) {
                giftsView.setActionBarActionMode(value);
            }
            topView.invalidate();

            int color1 = getThemedColor(Theme.key_profile_title);
            int color2 = getThemedColor(Theme.key_player_actionBarTitle);
            int c = AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f);
            nameTextView[1].setTextColor(c);
            if (lockIconDrawable != null) {
                lockIconDrawable.setColorFilter(c, PorterDuff.Mode.MULTIPLY);
            }
            if (scamDrawable != null) {
                color1 = getThemedColor(Theme.key_avatar_subtitleInProfileBlue);
                scamDrawable.setColor(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f));
            }

            color1 = peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon);
            color2 = getThemedColor(Theme.key_actionBarActionModeDefaultIcon);
            actionBar.setItemsColor(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), false);

            color1 = peerColor != null ? Theme.ACTION_BAR_WHITE_SELECTOR_COLOR : peerColor != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue);
            color2 = getThemedColor(Theme.key_actionBarActionModeDefaultSelector);
            actionBar.setItemsBackgroundColor(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), false);

            topView.invalidate();
            otherItem.setIconColor(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon));
//            callItem.setIconColor(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon));
//            videoCallItem.setIconColor(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon));
            editItem.setIconColor(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon));

            if (verifiedDrawable[0] != null) {
                color1 = getThemedColor(Theme.key_profile_verifiedBackground);
                color2 = getThemedColor(Theme.key_player_actionBarTitle);
                verifiedDrawable[0].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }
            if (verifiedDrawable[1] != null) {
                color1 = peerColor != null ? Theme.adaptHSV(ColorUtils.blendARGB(peerColor.getColor2(), peerColor.hasColor6(Theme.isCurrentThemeDark()) ? peerColor.getColor5() : peerColor.getColor3(), .4f), +.1f, Theme.isCurrentThemeDark() ? -.1f : -.08f) : getThemedColor(Theme.key_profile_verifiedBackground);
                color2 = getThemedColor(Theme.key_player_actionBarTitle);
                verifiedDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }

            if (verifiedCheckDrawable[0] != null) {
                color1 = getThemedColor(Theme.key_profile_verifiedCheck);
                color2 = getThemedColor(Theme.key_windowBackgroundWhite);
                verifiedCheckDrawable[0].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }
            if (verifiedCheckDrawable[1] != null) {
                color1 = peerColor != null ? Color.WHITE : dontApplyPeerColor(getThemedColor(Theme.key_profile_verifiedCheck));
                color2 = getThemedColor(Theme.key_windowBackgroundWhite);
                verifiedCheckDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }


            if (premiumStarDrawable[0] != null) {
                color1 = getThemedColor(Theme.key_profile_verifiedBackground);
                color2 = getThemedColor(Theme.key_player_actionBarTitle);
                premiumStarDrawable[0].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }
            if (premiumStarDrawable[1] != null) {
                color1 = dontApplyPeerColor(getThemedColor(Theme.key_profile_verifiedBackground));
                color2 = dontApplyPeerColor(getThemedColor(Theme.key_player_actionBarTitle));
                premiumStarDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }

            updateEmojiStatusDrawableColor();
        }

        @Override
        public Float get(ActionBar object) {
            return mediaHeaderAnimationProgress;
        }
    };
    private SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow selectAnimatedEmojiDialog;
    private Long emojiStatusGiftId;
    private boolean preloadedChannelEmojiStatuses;
    private AudioPlayerAlert.ClippingTextViewSwitcher mediaCounterTextView;
    private PinchToZoomHelper pinchToZoomHelper;

    private View scrimView = null;
    private Paint scrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        @Override
        public void setAlpha(int a) {
            super.setAlpha(a);
            fragmentView.invalidate();
        }
    };
    private Paint actionBarBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private View blurredView;
    private Paint whitePaint = new Paint();
    private boolean allowPullingDown;


    public DebugProfile(Bundle args) {
        this(args, null);
    }

    public DebugProfile(Bundle args, SharedMediaLayout.SharedMediaPreloader preloader) {
        super(args);
        sharedMediaPreloader = preloader;
    }

    public static DebugProfile of(long dialogId) {
        Bundle bundle = new Bundle();
        if (dialogId >= 0) {
            bundle.putLong("user_id", dialogId);
        } else {
            bundle.putLong("chat_id", -dialogId);
        }
        return new DebugProfile(bundle);
    }

    public static void sendLogs(Activity activity, boolean last) {
        if (activity == null) {
            return;
        }
        AlertDialog progressDialog = new AlertDialog(activity, AlertDialog.ALERT_TYPE_SPINNER);
        progressDialog.setCanCancel(false);
        progressDialog.show();
        Utilities.globalQueue.postRunnable(() -> {
            try {
                File dir = AndroidUtilities.getLogsDir();
                if (dir == null) {
                    AndroidUtilities.runOnUIThread(progressDialog::dismiss);
                    return;
                }

                File zipFile = new File(dir, "logs.zip");
                if (zipFile.exists()) {
                    zipFile.delete();
                }

                ArrayList<File> files = new ArrayList<>();

                File[] logFiles = dir.listFiles();
                Collections.addAll(files, logFiles);

                File filesDir = ApplicationLoader.getFilesDirFixed();
                filesDir = new File(filesDir, "malformed_database/");
                if (filesDir.exists() && filesDir.isDirectory()) {
                    File[] malformedDatabaseFiles = filesDir.listFiles();
                    Collections.addAll(files, malformedDatabaseFiles);
                }

                boolean[] finished = new boolean[1];
                long currentDate = System.currentTimeMillis();

                BufferedInputStream origin = null;
                ZipOutputStream out = null;
                try {
                    FileOutputStream dest = new FileOutputStream(zipFile);
                    out = new ZipOutputStream(new BufferedOutputStream(dest));
                    byte[] data = new byte[1024 * 64];

                    for (int i = 0; i < files.size(); i++) {
                        File file = files.get(i);
                        if (!file.getName().contains("cache4") && (last || file.getName().contains("_mtproto")) && (currentDate - file.lastModified()) > 24 * 60 * 60 * 1000) {
                            continue;
                        }
                        if (!file.exists()) {
                            continue;
                        }
                        FileInputStream fi = new FileInputStream(file);
                        origin = new BufferedInputStream(fi, data.length);

                        ZipEntry entry = new ZipEntry(file.getName());
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, data.length)) != -1) {
                            out.write(data, 0, count);
                        }
                        origin.close();
                        origin = null;
                    }
                    finished[0] = true;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (origin != null) {
                        origin.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }

                AndroidUtilities.runOnUIThread(() -> {
                    try {
                        progressDialog.dismiss();
                    } catch (Exception ignore) {

                    }
                    if (finished[0]) {
                        Uri uri;
                        if (Build.VERSION.SDK_INT >= 24) {
                            uri = FileProvider.getUriForFile(activity, ApplicationLoader.getApplicationId() + ".provider", zipFile);
                        } else {
                            uri = Uri.fromFile(zipFile);
                        }

                        Intent i = new Intent(Intent.ACTION_SEND);
                        if (Build.VERSION.SDK_INT >= 24) {
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, "");
                        i.putExtra(Intent.EXTRA_SUBJECT, "Logs from " + LocaleController.getInstance().getFormatterStats().format(System.currentTimeMillis()));
                        i.putExtra(Intent.EXTRA_STREAM, uri);
                        if (activity != null) {
                            try {
                                activity.startActivityForResult(Intent.createChooser(i, "Select email application."), 500);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                        }
                    } else {
                        if (activity != null) {
                            Toast.makeText(activity, LocaleController.getString(R.string.ErrorOccurred), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void onItemClick(View view, int position, float x, float y) {
        if (getParentActivity() == null) {
            return;
        }
        BaseFragment lastFragment = parentLayout.getLastFragment();
        long did = getDialogId();
        Context context = getContext();
        listView.stopScroll();
        if (position == affiliateRow) {
            TLRPC.User user = getMessagesController().getUser(userId);
            if (userInfo != null && userInfo.starref_program != null) {
                final long selfId = getUserConfig().getClientUserId();
                BotStarsController.getInstance(currentAccount).getConnectedBot(getContext(), selfId, userId, connectedBot -> {
                    if (connectedBot == null) {
                        ChannelAffiliateProgramsFragment.showConnectAffiliateAlert(context, currentAccount, userInfo.starref_program, getUserConfig().getClientUserId(), resourcesProvider, false);
                    } else {
                        ChannelAffiliateProgramsFragment.showShareAffiliateAlert(context, currentAccount, connectedBot, selfId, resourcesProvider);
                    }
                });
            } else if (user != null && user.bot_can_edit) {
                presentFragment(new AffiliateProgramFragment(userId));
            }
        } else if (position == notificationsSimpleRow) {
            boolean muted = getMessagesController().isDialogMuted(did, topicId);
            getNotificationsController().muteDialog(did, topicId, !muted);
            BulletinFactory.createMuteBulletin(DebugProfile.this, !muted, null).show();
            updateExceptions();
            if (notificationsSimpleRow >= 0 && listAdapter != null) {
                listAdapter.notifyItemChanged(notificationsSimpleRow);
            }
        } else if (position == addToContactsRow) {
            TLRPC.User user = getMessagesController().getUser(userId);
            Bundle args = new Bundle();
            args.putLong("user_id", user.id);
            args.putBoolean("addContact", true);
            args.putString("phone", vcardPhone);
            args.putString("first_name_card", vcardFirstName);
            args.putString("last_name_card", vcardLastName);
            openAddToContact(user, args);
        } else if (position == reportReactionRow) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
            builder.setTitle(LocaleController.getString(R.string.ReportReaction));
            builder.setMessage(LocaleController.getString(R.string.ReportAlertReaction));

            TLRPC.Chat chat = getMessagesController().getChat(-reportReactionFromDialogId);
            CheckBoxCell[] cells = new CheckBoxCell[1];
            if (chat != null && ChatObject.canBlockUsers(chat)) {
                LinearLayout linearLayout = new LinearLayout(getParentActivity());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                cells[0] = new CheckBoxCell(getParentActivity(), 1, resourcesProvider);
                cells[0].setBackgroundDrawable(Theme.getSelectorDrawable(false));
                cells[0].setText(LocaleController.getString(R.string.BanUser), "", true, false);
                cells[0].setPadding(LocaleController.isRTL ? AndroidUtilities.dp(16) : AndroidUtilities.dp(8), 0, LocaleController.isRTL ? AndroidUtilities.dp(8) : AndroidUtilities.dp(16), 0);
                linearLayout.addView(cells[0], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
                cells[0].setOnClickListener(v -> {
                    cells[0].setChecked(!cells[0].isChecked(), true);
                });
                builder.setView(linearLayout);
            }

            builder.setPositiveButton(LocaleController.getString(R.string.ReportChat), (dialog, which) -> {
                TLRPC.TL_messages_reportReaction req = new TLRPC.TL_messages_reportReaction();
                req.user_id = getMessagesController().getInputUser(userId);
                req.peer = getMessagesController().getInputPeer(reportReactionFromDialogId);
                req.id = reportReactionMessageId;
                ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {

                });

                if (cells[0] != null && cells[0].isChecked()) {
                    TLRPC.User user = getMessagesController().getUser(userId);
                    getMessagesController().deleteParticipantFromChat(-reportReactionFromDialogId, user);
                }

                reportReactionMessageId = 0;
                updateListAnimated(false);
                BulletinFactory.of(DebugProfile.this).createReportSent(resourcesProvider).show();
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog dialog = builder.show();
            TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
            }
        } else if (position == settingsKeyRow) {
            Bundle args = new Bundle();
            args.putInt("chat_id", DialogObject.getEncryptedChatId(dialogId));
            presentFragment(new IdenticonActivity(args));
        } else if (position == settingsTimerRow) {
            showDialog(AlertsCreator.createTTLAlert(getParentActivity(), currentEncryptedChat, resourcesProvider).create());
        } else if (position == notificationsRow) {
            if (LocaleController.isRTL && x <= AndroidUtilities.dp(76) || !LocaleController.isRTL && x >= view.getMeasuredWidth() - AndroidUtilities.dp(76)) {
                NotificationsCheckCell checkCell = (NotificationsCheckCell) view;
                boolean checked = !checkCell.isChecked();

                boolean defaultEnabled = getNotificationsController().isGlobalNotificationsEnabled(did, false, false);

                String key = NotificationsController.getSharedPrefKey(did, topicId);
                if (checked) {
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                    SharedPreferences.Editor editor = preferences.edit();
                    if (defaultEnabled) {
                        editor.remove("notify2_" + key);
                    } else {
                        editor.putInt("notify2_" + key, 0);
                    }
                    if (topicId == 0) {
                        getMessagesStorage().setDialogFlags(did, 0);
                        TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(did);
                        if (dialog != null) {
                            dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
                        }
                    }
                    editor.apply();
                } else {
                    int untilTime = Integer.MAX_VALUE;
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                    SharedPreferences.Editor editor = preferences.edit();
                    long flags;
                    if (!defaultEnabled) {
                        editor.remove("notify2_" + key);
                        flags = 0;
                    } else {
                        editor.putInt("notify2_" + key, 2);
                        flags = 1;
                    }
                    getNotificationsController().removeNotificationsForDialog(did);
                    if (topicId == 0) {
                        getMessagesStorage().setDialogFlags(did, flags);
                        TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(did);
                        if (dialog != null) {
                            dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
                            if (defaultEnabled) {
                                dialog.notify_settings.mute_until = untilTime;
                            }
                        }
                    }
                    editor.apply();
                }
                updateExceptions();
                getNotificationsController().updateServerNotificationsSettings(did, topicId);
                checkCell.setChecked(checked);
                RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findViewHolderForPosition(notificationsRow);
                if (holder != null) {
                    listAdapter.onBindViewHolder(holder, notificationsRow);
                }
                return;
            }
            ChatNotificationsPopupWrapper chatNotificationsPopupWrapper = new ChatNotificationsPopupWrapper(context, currentAccount, null, true, true, new ChatNotificationsPopupWrapper.Callback() {
                @Override
                public void toggleSound() {
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                    boolean enabled = !preferences.getBoolean("sound_enabled_" + NotificationsController.getSharedPrefKey(did, topicId), true);
                    preferences.edit().putBoolean("sound_enabled_" + NotificationsController.getSharedPrefKey(did, topicId), enabled).apply();
                    if (BulletinFactory.canShowBulletin(DebugProfile.this)) {
                        BulletinFactory.createSoundEnabledBulletin(DebugProfile.this, enabled ? NotificationsController.SETTING_SOUND_ON : NotificationsController.SETTING_SOUND_OFF, getResourceProvider()).show();
                    }
                }

                @Override
                public void muteFor(int timeInSeconds) {
                    if (timeInSeconds == 0) {
                        if (getMessagesController().isDialogMuted(did, topicId)) {
                            toggleMute();
                        }
                        if (BulletinFactory.canShowBulletin(DebugProfile.this)) {
                            BulletinFactory.createMuteBulletin(DebugProfile.this, NotificationsController.SETTING_MUTE_UNMUTE, timeInSeconds, getResourceProvider()).show();
                        }
                    } else {
                        getNotificationsController().muteUntil(did, topicId, timeInSeconds);
                        if (BulletinFactory.canShowBulletin(DebugProfile.this)) {
                            BulletinFactory.createMuteBulletin(DebugProfile.this, NotificationsController.SETTING_MUTE_CUSTOM, timeInSeconds, getResourceProvider()).show();
                        }
                        updateExceptions();
                        if (notificationsRow >= 0 && listAdapter != null) {
                            listAdapter.notifyItemChanged(notificationsRow);
                        }
                    }
                }

                @Override
                public void showCustomize() {
                    if (did != 0) {
                        Bundle args = new Bundle();
                        args.putLong("dialog_id", did);
                        args.putLong("topic_id", topicId);
                        presentFragment(new ProfileNotificationsActivity(args, resourcesProvider));
                    }
                }

                @Override
                public void toggleMute() {
                    boolean muted = getMessagesController().isDialogMuted(did, topicId);
                    getNotificationsController().muteDialog(did, topicId, !muted);
                    if (DebugProfile.this.fragmentView != null) {
                        BulletinFactory.createMuteBulletin(DebugProfile.this, !muted, null).show();
                    }
                    updateExceptions();
                    if (notificationsRow >= 0 && listAdapter != null) {
                        listAdapter.notifyItemChanged(notificationsRow);
                    }
                }

                @Override
                public void openExceptions() {
                    Bundle bundle = new Bundle();
                    bundle.putLong("dialog_id", did);
                    TopicsNotifySettingsFragments notifySettings = new TopicsNotifySettingsFragments(bundle);
                    notifySettings.setExceptions(notificationsExceptionTopics);
                    presentFragment(notifySettings);
                }
            }, getResourceProvider());
            chatNotificationsPopupWrapper.update(did, topicId, notificationsExceptionTopics);
            if (AndroidUtilities.isTablet()) {
                View v = parentLayout.getView();
                x += v.getX() + v.getPaddingLeft();
                y += v.getY() + v.getPaddingTop();
            }
            chatNotificationsPopupWrapper.showAsOptions(DebugProfile.this, view, x, y);
        } else if (position == unblockRow) {
            getMessagesController().unblockPeer(userId);
            if (BulletinFactory.canShowBulletin(DebugProfile.this)) {
                BulletinFactory.createBanBulletin(DebugProfile.this, false).show();
            }
        } else if (position == addToGroupButtonRow) {
            try {
                actionBar.getActionBarMenuOnItemClick().onItemClick(invite_to_group);
            } catch (Exception e) {
                FileLog.e(e);
            }
        } else if (position == sendMessageRow) {
            onWriteButtonClick();
        } else if (position == reportRow) {
            ReportBottomSheet.openChat(DebugProfile.this, getDialogId());
        } else if (position >= membersStartRow && position < membersEndRow) {
            TLRPC.ChatParticipant participant;
            if (!sortedUsers.isEmpty()) {
                participant = chatInfo.participants.participants.get(sortedUsers.get(position - membersStartRow));
            } else {
                participant = chatInfo.participants.participants.get(position - membersStartRow);
            }
            onMemberClick(participant, false, view);
        } else if (position == addMemberRow) {
            openAddMember();
        } else if (position == usernameRow) {
            processOnClickOrPress(position, view, x, y);
        } else if (position == locationRow) {
            if (chatInfo.location instanceof TLRPC.TL_channelLocation) {
                LocationActivity fragment = new LocationActivity(LocationActivity.LOCATION_TYPE_GROUP_VIEW);
                fragment.setChatLocation(chatId, (TLRPC.TL_channelLocation) chatInfo.location);
                presentFragment(fragment);
            }
        } else if (position == joinRow) {
            getMessagesController().addUserToChat(currentChat.id, getUserConfig().getCurrentUser(), 0, null, DebugProfile.this, true, () -> {
                updateRowsIds();
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
            }, err -> {
                if (err != null && "INVITE_REQUEST_SENT".equals(err.text)) {
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                    preferences.edit().putLong("dialog_join_requested_time_" + dialogId, System.currentTimeMillis()).commit();
                    JoinGroupAlert.showBulletin(context, DebugProfile.this, ChatObject.isChannel(currentChat) && !currentChat.megagroup);
                    updateRowsIds();
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    if (lastFragment instanceof ChatActivity) {
                        ((ChatActivity) lastFragment).showBottomOverlayProgress(false, true);
                    }
                    return false;
                }
                return true;
            });
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeSearchByActiveAction);
        } else if (position == subscribersRow) {
            Bundle args = new Bundle();
            args.putLong("chat_id", chatId);
            args.putInt("type", ChatUsersActivity.TYPE_USERS);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(chatInfo);
            presentFragment(fragment);
        } else if (position == subscribersRequestsRow) {
            MemberRequestsActivity activity = new MemberRequestsActivity(chatId);
            presentFragment(activity);
        } else if (position == administratorsRow) {
            Bundle args = new Bundle();
            args.putLong("chat_id", chatId);
            args.putInt("type", ChatUsersActivity.TYPE_ADMIN);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(chatInfo);
            presentFragment(fragment);
        } else if (position == settingsRow) {
            editItem.performClick();
        } else if (position == botStarsBalanceRow) {
            presentFragment(new BotStarsActivity(BotStarsActivity.TYPE_STARS, userId));
        } else if (position == botTonBalanceRow) {
            presentFragment(new BotStarsActivity(BotStarsActivity.TYPE_TON, userId));
        } else if (position == channelBalanceRow) {
            Bundle args = new Bundle();
            args.putLong("chat_id", chatId);
            args.putBoolean("start_from_monetization", true);
            presentFragment(new StatisticActivity(args));
        } else if (position == blockedUsersRow) {
            Bundle args = new Bundle();
            args.putLong("chat_id", chatId);
            args.putInt("type", ChatUsersActivity.TYPE_BANNED);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(chatInfo);
            presentFragment(fragment);
        } else if (position == notificationRow) {
            presentFragment(new NotificationsSettingsActivity());
        } else if (position == privacyRow) {
            presentFragment(new PrivacySettingsActivity().setCurrentPassword(currentPassword));
        } else if (position == dataRow) {
            presentFragment(new DataSettingsActivity());
        } else if (position == chatRow) {
            presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC));
        } else if (position == filtersRow) {
            presentFragment(new FiltersSetupActivity());
        } else if (position == stickersRow) {
            presentFragment(new StickersActivity(MediaDataController.TYPE_IMAGE, null));
        } else if (position == liteModeRow) {
            presentFragment(new LiteModeSettingsActivity());
        } else if (position == devicesRow) {
            presentFragment(new SessionsActivity(0));
        } else if (position == questionRow) {
            showDialog(AlertsCreator.createSupportAlert(DebugProfile.this, resourcesProvider));
        } else if (position == faqRow) {
            Browser.openUrl(getParentActivity(), LocaleController.getString(R.string.TelegramFaqUrl));
        } else if (position == policyRow) {
            Browser.openUrl(getParentActivity(), LocaleController.getString(R.string.PrivacyPolicyUrl));
        } else if (position == sendLogsRow) {
            sendLogs(getParentActivity(), false);
        } else if (position == sendLastLogsRow) {
            sendLogs(getParentActivity(), true);
        } else if (position == clearLogsRow) {
            FileLog.cleanupLogs();
        } else if (position == switchBackendRow) {
            if (getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
            builder1.setMessage(LocaleController.getString(R.string.AreYouSure));
            builder1.setTitle(LocaleController.getString(R.string.AppName));
            builder1.setPositiveButton(LocaleController.getString(R.string.OK), (dialogInterface, i) -> {
                SharedConfig.pushAuthKey = null;
                SharedConfig.pushAuthKeyId = null;
                SharedConfig.saveConfig();
                getConnectionsManager().switchBackend(true);
            });
            builder1.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            showDialog(builder1.create());
        } else if (position == languageRow) {
            presentFragment(new LanguageSelectActivity());
        } else if (position == setUsernameRow) {
            presentFragment(new ChangeUsernameActivity());
        } else if (position == bioRow) {
            presentFragment(new UserInfoActivity());
        } else if (position == numberRow) {
            presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
        } else if (position == setAvatarRow) {
            onWriteButtonClick();
        } else if (position == premiumRow) {
            presentFragment(new PremiumPreviewFragment("settings"));
        } else if (position == starsRow) {
            presentFragment(new StarsIntroActivity());
        } else if (position == businessRow) {
            presentFragment(new PremiumPreviewFragment(PremiumPreviewFragment.FEATURES_BUSINESS, "settings"));
        } else if (position == premiumGiftingRow) {
            UserSelectorBottomSheet.open(0, BirthdayController.getInstance(currentAccount).getState());
        } else if (position == botPermissionLocation) {
            if (botLocation != null) {
                botLocation.setGranted(!botLocation.granted(), () -> {
                    ((TextCell) view).setChecked(botLocation.granted());
                });
            }
        } else if (position == botPermissionBiometry) {
            if (botBiometry != null) {
                botBiometry.setGranted(!botBiometry.granted());
                ((TextCell) view).setChecked(botBiometry.granted());
            }
        } else if (position == botPermissionEmojiStatus) {
            ((TextCell) view).setChecked(!((TextCell) view).isChecked());
            if (botPermissionEmojiStatusReqId > 0) {
                getConnectionsManager().cancelRequest(botPermissionEmojiStatusReqId, true);
            }
            TL_bots.toggleUserEmojiStatusPermission req = new TL_bots.toggleUserEmojiStatusPermission();
            req.bot = getMessagesController().getInputUser(userId);
            req.enabled = ((TextCell) view).isChecked();
            if (userInfo != null) {
                userInfo.bot_can_manage_emoji_status = req.enabled;
            }
            final int[] reqId = new int[1];
            reqId[0] = botPermissionEmojiStatusReqId = getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                if (!(res instanceof TLRPC.TL_boolTrue)) {
                    BulletinFactory.of(DebugProfile.this).showForError(err);
                }
                if (botPermissionEmojiStatusReqId == reqId[0]) {
                    botPermissionEmojiStatusReqId = 0;
                }
            }));
        } else if (position == bizHoursRow) {
            hoursExpanded = !hoursExpanded;
            saveScrollPosition();
            view.requestLayout();
            listAdapter.notifyItemChanged(bizHoursRow);
            // TODO
//            if (savedScrollPosition >= 0) {
//                layoutManager.scrollToPositionWithOffset(savedScrollPosition, savedScrollOffset - listView.getPaddingTop());
//            }
        } else if (position == bizLocationRow) {
            openLocation(false);
        } else if (position == channelRow) {
            if (userInfo == null) return;
            Bundle args = new Bundle();
            args.putLong("chat_id", userInfo.personal_channel_id);
            presentFragment(new ChatActivity(args));
        } else if (position == birthdayRow) {
            if (birthdayEffect != null && birthdayEffect.start()) {
                return;
            }
            if (editRow(view, position)) {
                return;
            }
            TextDetailCell cell = (TextDetailCell) view;
            if (cell.hasImage()) {
                onTextDetailCellImageClicked(cell.getImageView());
            }
        } else {
            processOnClickOrPress(position, view, x, y);
        }
    }

    private boolean processOnClickOrPress(final int position, final View view, final float x, final float y) {
        if (position == usernameRow || position == setUsernameRow) {
            final String username;
            final TLRPC.TL_username usernameObj;
            if (userId != 0) {
                final TLRPC.User user = getMessagesController().getUser(userId);
                String username1 = UserObject.getPublicUsername(user);
                if (user == null || username1 == null) {
                    return false;
                }
                username = username1;
                usernameObj = DialogObject.findUsername(username, user);
            } else if (chatId != 0) {
                final TLRPC.Chat chat = getMessagesController().getChat(chatId);
                if (chat == null || topicId == 0 && !ChatObject.isPublic(chat)) {
                    return false;
                }
                username = ChatObject.getPublicUsername(chat);
                usernameObj = DialogObject.findUsername(username, chat);
            } else {
                return false;
            }
            if (userId == 0) {
                TLRPC.Chat chat = getMessagesController().getChat(chatId);
                String link;
                if (ChatObject.isPublic(chat)) {
                    link = "https://" + getMessagesController().linkPrefix + "/" + ChatObject.getPublicUsername(chat) + (topicId != 0 ? "/" + topicId : "");
                } else {
                    link = "https://" + getMessagesController().linkPrefix + "/c/" + chat.id + (topicId != 0 ? "/" + topicId : "");
                }
                ShareAlert shareAlert = new ShareAlert(getParentActivity(), null, link, false, link, false) {
                    @Override
                    protected void onSend(LongSparseArray<TLRPC.Dialog> dids, int count, TLRPC.TL_forumTopic topic, boolean showToast) {
                        if (!showToast) return;
                        AndroidUtilities.runOnUIThread(() -> {
                            BulletinFactory.createInviteSentBulletin(getParentActivity(), (FrameLayout) fragmentView, dids.size(), dids.size() == 1 ? dids.valueAt(0).id : 0, count, getThemedColor(Theme.key_undo_background), getThemedColor(Theme.key_undo_infoColor)).show();
                        }, 250);
                    }
                };
                showDialog(shareAlert);
                if (usernameObj != null && !usernameObj.editable) {
                    TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                    TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                    input.username = usernameObj.username;
                    req.collectible = input;
                    int reqId = getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (res instanceof TL_fragment.TL_collectibleInfo) {
                            TL_fragment.TL_collectibleInfo info = (TL_fragment.TL_collectibleInfo) res;
                            TLObject obj;
                            if (userId != 0) {
                                obj = getMessagesController().getUser(userId);
                            } else {
                                obj = getMessagesController().getChat(chatId);
                            }
                            final String usernameStr = "@" + usernameObj.username;
                            final String date = LocaleController.getInstance().getFormatterBoostExpired().format(new Date(info.purchase_date * 1000L));
                            final String cryptoAmount = BillingController.getInstance().formatCurrency(info.crypto_amount, info.crypto_currency);
                            final String amount = BillingController.getInstance().formatCurrency(info.amount, info.currency);
                            BulletinFactory.of(shareAlert.bulletinContainer2, resourcesProvider).createImageBulletin(R.drawable.filled_username, AndroidUtilities.withLearnMore(AndroidUtilities.replaceTags(formatString(R.string.FragmentChannelUsername, usernameStr, date, cryptoAmount, TextUtils.isEmpty(amount) ? "" : "(" + amount + ")")), () -> {
                                Bulletin.hideVisible();
                                Browser.openUrl(getContext(), info.url);
                            })).setOnClickListener(v -> {
                                Bulletin.hideVisible();
                                Browser.openUrl(getContext(), info.url);
                            }).show(false);
                        } else {
                            BulletinFactory.showError(err);
                        }
                    }));
                    getConnectionsManager().bindRequestToGuid(reqId, getClassGuid());
                }
            } else {
                if (editRow(view, position)) return true;

                if (usernameObj != null && !usernameObj.editable) {
                    TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                    TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                    input.username = usernameObj.username;
                    req.collectible = input;
                    int reqId = getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (res instanceof TL_fragment.TL_collectibleInfo) {
                            TLObject obj;
                            if (userId != 0) {
                                obj = getMessagesController().getUser(userId);
                            } else {
                                obj = getMessagesController().getChat(chatId);
                            }
                            FragmentUsernameBottomSheet.open(getContext(), FragmentUsernameBottomSheet.TYPE_USERNAME, usernameObj.username, obj, (TL_fragment.TL_collectibleInfo) res, getResourceProvider());
                        } else {
                            BulletinFactory.showError(err);
                        }
                    }));
                    getConnectionsManager().bindRequestToGuid(reqId, getClassGuid());
                    return true;
                }

                try {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    String text = "@" + username;
                    BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.UsernameCopied), resourcesProvider).show();
                    android.content.ClipData clip = android.content.ClipData.newPlainText("label", text);
                    clipboard.setPrimaryClip(clip);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            return true;
        } else if (position == phoneRow || position == numberRow) {
            if (editRow(view, position)) return true;

            final TLRPC.User user = getMessagesController().getUser(userId);
            if (user == null || user.phone == null || user.phone.length() == 0 || getParentActivity() == null) {
                return false;
            }

            if (position == phoneRow && user.phone.startsWith("888")) {
                TL_fragment.TL_inputCollectiblePhone input = new TL_fragment.TL_inputCollectiblePhone();
                final String phone = input.phone = user.phone;
                TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                req.collectible = input;
                int reqId = getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                    if (res instanceof TL_fragment.TL_collectibleInfo) {
                        FragmentUsernameBottomSheet.open(getContext(), FragmentUsernameBottomSheet.TYPE_PHONE, phone, user, (TL_fragment.TL_collectibleInfo) res, getResourceProvider());
                    } else {
                        BulletinFactory.showError(err);
                    }
                }));
                getConnectionsManager().bindRequestToGuid(reqId, getClassGuid());
                return true;
            }

            ArrayList<CharSequence> items = new ArrayList<>();
            ArrayList<Integer> actions = new ArrayList<>();
            List<Integer> icons = new ArrayList<>();
            if (position == phoneRow) {
                if (userInfo != null && userInfo.phone_calls_available) {
                    icons.add(R.drawable.msg_calls);
                    items.add(LocaleController.getString(R.string.CallViaTelegram));
                    actions.add(PHONE_OPTION_TELEGRAM_CALL);
                    if (Build.VERSION.SDK_INT >= 18 && userInfo.video_calls_available) {
                        icons.add(R.drawable.msg_videocall);
                        items.add(LocaleController.getString(R.string.VideoCallViaTelegram));
                        actions.add(PHONE_OPTION_TELEGRAM_VIDEO_CALL);
                    }
                }
                if (!isFragmentPhoneNumber) {
                    icons.add(R.drawable.msg_calls_regular);
                    items.add(LocaleController.getString(R.string.Call));
                    actions.add(PHONE_OPTION_CALL);
                }
            }
            icons.add(R.drawable.msg_copy);
            items.add(LocaleController.getString(R.string.Copy));
            actions.add(PHONE_OPTION_COPY);

            AtomicReference<ActionBarPopupWindow> popupWindowRef = new AtomicReference<>();
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext(), R.drawable.popup_fixed_alert, resourcesProvider) {
                final Path path = new Path();

                @Override
                protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                    canvas.save();
                    path.rewind();
                    AndroidUtilities.rectTmp.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                    path.addRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.dp(6), AndroidUtilities.dp(6), Path.Direction.CW);
                    canvas.clipPath(path);
                    boolean draw = super.drawChild(canvas, child, drawingTime);
                    canvas.restore();
                    return draw;
                }
            };
            popupLayout.setFitItems(true);

            for (int i = 0; i < icons.size(); i++) {
                int action = actions.get(i);
                ActionBarMenuItem.addItem(popupLayout, icons.get(i), items.get(i), false, resourcesProvider).setOnClickListener(v -> {
                    popupWindowRef.get().dismiss();
                    switch (action) {
                        case PHONE_OPTION_CALL:
                            try {
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + user.phone));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getParentActivity().startActivityForResult(intent, 500);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            break;
                        case PHONE_OPTION_COPY:
                            try {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("label", "+" + user.phone);
                                clipboard.setPrimaryClip(clip);
                                if (AndroidUtilities.shouldShowClipboardToast()) {
                                    BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.PhoneCopied)).show();
                                }
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            break;
                        case PHONE_OPTION_TELEGRAM_CALL:
                        case PHONE_OPTION_TELEGRAM_VIDEO_CALL:
                            if (getParentActivity() == null) {
                                return;
                            }
                            VoIPHelper.startCall(user, action == PHONE_OPTION_TELEGRAM_VIDEO_CALL, userInfo != null && userInfo.video_calls_available, getParentActivity(), userInfo, getAccountInstance());
                            break;
                    }
                });
            }
            if (isFragmentPhoneNumber) {
                FrameLayout gap = new FrameLayout(getContext());
                gap.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuSeparator, resourcesProvider));
                popupLayout.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));

                TextView fragmentInfoView = new TextView(getContext());
                fragmentInfoView.setPadding(AndroidUtilities.dp(13), AndroidUtilities.dp(8), AndroidUtilities.dp(13), AndroidUtilities.dp(8));
                fragmentInfoView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                fragmentInfoView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem, resourcesProvider));
                fragmentInfoView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText, resourcesProvider));
                fragmentInfoView.setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector, resourcesProvider), 0, 6));

                SpannableStringBuilder spanned = new SpannableStringBuilder(AndroidUtilities.replaceTags(LocaleController.getString(R.string.AnonymousNumberNotice)));

                int startIndex = TextUtils.indexOf(spanned, '*');
                int lastIndex = TextUtils.lastIndexOf(spanned, '*');
                if (startIndex != -1 && lastIndex != -1 && startIndex != lastIndex) {
                    spanned.replace(lastIndex, lastIndex + 1, "");
                    spanned.replace(startIndex, startIndex + 1, "");
                    spanned.setSpan(new TypefaceSpan(AndroidUtilities.bold()), startIndex, lastIndex - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spanned.setSpan(new ForegroundColorSpan(fragmentInfoView.getLinkTextColors().getDefaultColor()), startIndex, lastIndex - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                fragmentInfoView.setText(spanned);
                fragmentInfoView.setOnClickListener(v -> {
                    try {
                        v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://fragment.com")));
                    } catch (ActivityNotFoundException e) {
                        FileLog.e(e);
                    }
                });

                gap.setTag(R.id.fit_width_tag, 1);
                fragmentInfoView.setTag(R.id.fit_width_tag, 1);
                popupLayout.addView(fragmentInfoView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
            }

            ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
            popupWindow.setPauseNotifications(true);
            popupWindow.setDismissAnimationDuration(220);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setClippingEnabled(true);
            popupWindow.setAnimationStyle(R.style.PopupContextAnimation);
            popupWindow.setFocusable(true);
            popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
            popupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
            popupWindow.getContentView().setFocusableInTouchMode(true);
            popupWindowRef.set(popupWindow);

            float px = x, py = y;
            View v = view;
            while (v != getFragmentView() && v != null) {
                px += v.getX();
                py += v.getY();
                v = (View) v.getParent();
            }
            if (AndroidUtilities.isTablet()) {
                View pv = parentLayout.getView();
                if (pv != null) {
                    px += pv.getX() + pv.getPaddingLeft();
                    py += pv.getY() + pv.getPaddingTop();
                }
            }
            px -= popupLayout.getMeasuredWidth() / 2f;
            popupWindow.showAtLocation(getFragmentView(), 0, (int) px, (int) py);
            popupWindow.dimBehind();
            return true;
        } else if (position == channelInfoRow || position == userInfoRow || position == locationRow || position == bioRow) {
            if (position == bioRow && (userInfo == null || TextUtils.isEmpty(userInfo.about))) {
                return false;
            }
            if (editRow(view, position)) return true;
            if (view instanceof AboutLinkCell && ((AboutLinkCell) view).onClick()) {
                return false;
            }
            String text;
            if (position == locationRow) {
                text = chatInfo != null && chatInfo.location instanceof TLRPC.TL_channelLocation ? ((TLRPC.TL_channelLocation) chatInfo.location).address : null;
            } else if (position == channelInfoRow) {
                text = chatInfo != null ? chatInfo.about : null;
            } else {
                text = userInfo != null ? userInfo.about : null;
            }
            final String finalText = text;
            if (TextUtils.isEmpty(finalText)) {
                return false;
            }
            final String[] fromLanguage = new String[1];
            fromLanguage[0] = "und";
            final boolean translateButtonEnabled = MessagesController.getInstance(currentAccount).getTranslateController().isContextTranslateEnabled();
            final boolean[] withTranslate = new boolean[1];
            withTranslate[0] = position == bioRow || position == channelInfoRow || position == userInfoRow;
            final String toLang = LocaleController.getInstance().getCurrentLocale().getLanguage();
            Runnable showMenu = () -> {
                if (getParentActivity() == null) {
                    return;
                }
                CharSequence[] items = withTranslate[0] ? new CharSequence[]{LocaleController.getString(R.string.Copy), LocaleController.getString(R.string.TranslateMessage)} : new CharSequence[]{LocaleController.getString(R.string.Copy)};
                int[] icons = withTranslate[0] ? new int[]{R.drawable.msg_copy, R.drawable.msg_translate} : new int[]{R.drawable.msg_copy};

                AtomicReference<ActionBarPopupWindow> popupWindowRef = new AtomicReference<>();
                ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext(), R.drawable.popup_fixed_alert, resourcesProvider) {
                    final Path path = new Path();

                    @Override
                    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                        canvas.save();
                        path.rewind();
                        AndroidUtilities.rectTmp.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                        path.addRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.dp(6), AndroidUtilities.dp(6), Path.Direction.CW);
                        canvas.clipPath(path);
                        boolean draw = super.drawChild(canvas, child, drawingTime);
                        canvas.restore();
                        return draw;
                    }
                };
                popupLayout.setFitItems(true);

                for (int i = 0; i < icons.length; i++) {
                    int j = i;
                    ActionBarMenuItem.addItem(popupLayout, icons[i], items[i], false, resourcesProvider).setOnClickListener(v -> {
                        popupWindowRef.get().dismiss();
                        try {
                            if (j == 0) {
                                AndroidUtilities.addToClipboard(finalText);
                                if (position == bioRow) {
                                    BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.BioCopied)).show();
                                } else {
                                    BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.TextCopied)).show();
                                }
                            } else if (j == 1) {
                                TranslateAlert2.showAlert(fragmentView.getContext(), this, currentAccount, fromLanguage[0], toLang, finalText, null, false, span -> {
                                    if (span != null) {
                                        openUrl(span.getURL(), null);
                                        return true;
                                    }
                                    return false;
                                }, null);
                            }
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    });
                }

                ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
                popupWindow.setPauseNotifications(true);
                popupWindow.setDismissAnimationDuration(220);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setClippingEnabled(true);
                popupWindow.setAnimationStyle(R.style.PopupContextAnimation);
                popupWindow.setFocusable(true);
                popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
                popupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
                popupWindow.getContentView().setFocusableInTouchMode(true);
                popupWindowRef.set(popupWindow);

                float px = x, py = y;
                View v = view;
                while (v != null && v != getFragmentView()) {
                    px += v.getX();
                    py += v.getY();
                    v = (View) v.getParent();
                }
                if (AndroidUtilities.isTablet()) {
                    View pv = parentLayout.getView();
                    if (pv != null) {
                        px += pv.getX() + pv.getPaddingLeft();
                        py += pv.getY() + pv.getPaddingTop();
                    }
                }
                px -= popupLayout.getMeasuredWidth() / 2f;
                popupWindow.showAtLocation(getFragmentView(), 0, (int) px, (int) py);
                popupWindow.dimBehind();
            };
            if (withTranslate[0]) {
                if (LanguageDetector.hasSupport()) {
                    LanguageDetector.detectLanguage(finalText, (fromLang) -> {
                        fromLanguage[0] = fromLang;
                        withTranslate[0] = fromLang != null && (!fromLang.equals(toLang) || fromLang.equals("und")) && (translateButtonEnabled && !RestrictedLanguagesSelectActivity.getRestrictedLanguages().contains(fromLang) || (currentChat != null && (currentChat.has_link || ChatObject.isPublic(currentChat))) && ("uk".equals(fromLang) || "ru".equals(fromLang)));
                        showMenu.run();
                    }, (error) -> {
                        FileLog.e("mlkit: failed to detect language in selection", error);
                        showMenu.run();
                    });
                } else {
                    showMenu.run();
                }
            } else {
                showMenu.run();
            }
            return true;
        } else if (position == bizHoursRow || position == bizLocationRow) {
            if (getParentActivity() == null || userInfo == null) {
                return false;
            }
            final String finalText;
            if (position == bizHoursRow) {
                if (userInfo.business_work_hours == null) return false;
                finalText = OpeningHoursActivity.toString(currentAccount, userInfo.user, userInfo.business_work_hours);
            } else if (position == bizLocationRow) {
                if (editRow(view, position)) return true;
                if (userInfo.business_location == null) return false;
                finalText = userInfo.business_location.address;
            } else return true;

            AtomicReference<ActionBarPopupWindow> popupWindowRef = new AtomicReference<>();
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext(), R.drawable.popup_fixed_alert, resourcesProvider) {
                final Path path = new Path();

                @Override
                protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                    canvas.save();
                    path.rewind();
                    AndroidUtilities.rectTmp.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                    path.addRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.dp(6), AndroidUtilities.dp(6), Path.Direction.CW);
                    canvas.clipPath(path);
                    boolean draw = super.drawChild(canvas, child, drawingTime);
                    canvas.restore();
                    return draw;
                }
            };
            popupLayout.setFitItems(true);

            ActionBarMenuItem.addItem(popupLayout, R.drawable.msg_copy, LocaleController.getString(R.string.Copy), false, resourcesProvider).setOnClickListener(v -> {
                popupWindowRef.get().dismiss();
                try {
                    AndroidUtilities.addToClipboard(finalText);
                    if (position == bizHoursRow) {
                        BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.BusinessHoursCopied)).show();
                    } else {
                        BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.BusinessLocationCopied)).show();
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            });

            ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
            popupWindow.setPauseNotifications(true);
            popupWindow.setDismissAnimationDuration(220);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setClippingEnabled(true);
            popupWindow.setAnimationStyle(R.style.PopupContextAnimation);
            popupWindow.setFocusable(true);
            popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
            popupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
            popupWindow.getContentView().setFocusableInTouchMode(true);
            popupWindowRef.set(popupWindow);

            float px = x, py = y;
            View v = view;
            while (v != null && v != getFragmentView()) {
                px += v.getX();
                py += v.getY();
                v = (View) v.getParent();
            }
            if (AndroidUtilities.isTablet()) {
                View pv = parentLayout.getView();
                if (pv != null) {
                    px += pv.getX() + pv.getPaddingLeft();
                    py += pv.getY() + pv.getPaddingTop();
                }
            }
            px -= popupLayout.getMeasuredWidth() / 2f;
            popupWindow.showAtLocation(getFragmentView(), 0, (int) px, (int) py);
            popupWindow.dimBehind();
            return true;
        }
        return false;
    }


    private boolean editRow(View view, int position) {
        if (!myProfile) return false;

        if (view instanceof ProfileChannelCell) {
            view = ((ProfileChannelCell) view).dialogCell;
        }

        TLRPC.User user = getUserConfig().getCurrentUser();
        if (user == null) return false;
        TLRPC.UserFull userFull = userInfo == null ? getMessagesController().getUserFull(user.id) : userInfo;
        if (userFull == null) return false;

        String copyButton = getString(R.string.Copy);
        String textToCopy = null;
        if (position == channelInfoRow || position == userInfoRow || position == bioRow) {
            textToCopy = userFull.about;
        } else if (position == bizHoursRow) {
            textToCopy = OpeningHoursActivity.toString(currentAccount, user, userFull.business_work_hours);
            copyButton = getString(R.string.ProfileHoursCopy);
        } else if (position == bizLocationRow) {
            textToCopy = userFull.business_location.address;
            copyButton = getString(R.string.ProfileLocationCopy);
        } else if (position == usernameRow) {
            textToCopy = UserObject.getPublicUsername(user);
            if (textToCopy != null) textToCopy = "@" + textToCopy;
            copyButton = getString(R.string.ProfileCopyUsername);
        } else if (position == phoneRow) {
            textToCopy = user.phone;
        } else if (position == birthdayRow) {
            textToCopy = UserInfoActivity.birthdayString(userInfo.birthday);
        }

        ItemOptions itemOptions = ItemOptions.makeOptions((FrameLayout) fragmentView, resourcesProvider, view);
        itemOptions.setGravity(Gravity.LEFT);

        if (position == bizLocationRow && userFull.business_location != null) {
            if (userFull.business_location.geo_point != null) {
                itemOptions.add(R.drawable.msg_view_file, getString(R.string.ProfileLocationView), () -> {
                    openLocation(false);
                });
            }
            itemOptions.add(R.drawable.msg_map, getString(R.string.ProfileLocationMaps), () -> {
                openLocation(true);
            });
        }

        if (textToCopy != null) {
            final String text = textToCopy;
            itemOptions.add(R.drawable.msg_copy, copyButton, () -> {
                AndroidUtilities.addToClipboard(text);
            });
        }

        if (position == bizHoursRow) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileHoursEdit), () -> {
                presentFragment(new OpeningHoursActivity());
            });
            itemOptions.add(R.drawable.msg_delete, getString(R.string.ProfileHoursRemove), true, () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.BusinessHoursClearTitle));
                builder.setMessage(LocaleController.getString(R.string.BusinessHoursClearMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Remove), (di, w) -> {
                    TL_account.updateBusinessWorkHours req = new TL_account.updateBusinessWorkHours();
                    if (userFull != null) {
                        userFull.business_work_hours = null;
                        userFull.flags2 &= ~1;
                    }
                    getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (err != null) {
                            BulletinFactory.showError(err);
                        } else if (res instanceof TLRPC.TL_boolFalse) {
                            BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                        }
                    }));
                    updateRowsIds();
                    listAdapter.notifyItemRemoved(position);
                    getMessagesStorage().updateUserInfo(userFull, false);
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                showDialog(builder.create());
            });
        } else if (position == bizLocationRow) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileLocationEdit), () -> {
                presentFragment(new org.telegram.ui.Business.LocationActivity());
            });
            itemOptions.add(R.drawable.msg_delete, getString(R.string.ProfileLocationRemove), true, () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.BusinessLocationClearTitle));
                builder.setMessage(LocaleController.getString(R.string.BusinessLocationClearMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Remove), (di, w) -> {
                    TL_account.updateBusinessLocation req = new TL_account.updateBusinessLocation();
                    if (userFull != null) {
                        userFull.business_location = null;
                        userFull.flags2 &= ~2;
                    }
                    getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (err != null) {
                            BulletinFactory.showError(err);
                        } else if (res instanceof TLRPC.TL_boolFalse) {
                            BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                        }
                    }));
                    updateRowsIds();
                    listAdapter.notifyItemRemoved(position);
                    getMessagesStorage().updateUserInfo(userFull, false);
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                showDialog(builder.create());
            });
        } else if (position == usernameRow) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileUsernameEdit), () -> {
                presentFragment(new ChangeUsernameActivity());
            });
        } else if (position == channelInfoRow || position == userInfoRow || position == bioRow) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileEditBio), () -> {
                presentFragment(new UserInfoActivity());
            });
        } else if (position == phoneRow) {
            itemOptions.add(R.drawable.menu_storage_path, getString(R.string.ProfilePhoneEdit), () -> {
                presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
            });
        } else if (position == birthdayRow) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileBirthdayChange), () -> {
                showDialog(AlertsCreator.createBirthdayPickerDialog(getContext(), getString(R.string.EditProfileBirthdayTitle), getString(R.string.EditProfileBirthdayButton), userFull.birthday, birthday -> {
                    TL_account.updateBirthday req = new TL_account.updateBirthday();
                    req.flags |= 1;
                    req.birthday = birthday;
                    TL_account.TL_birthday oldBirthday = userFull != null ? userFull.birthday : null;
                    if (userFull != null) {
                        userFull.flags2 |= 32;
                        userFull.birthday = birthday;
                    }
                    getMessagesController().invalidateContentSettings();
                    getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (res instanceof TLRPC.TL_boolTrue) {
                            BulletinFactory.of(DebugProfile.this).createSimpleBulletin(R.raw.contact_check, LocaleController.getString(R.string.PrivacyBirthdaySetDone)).setDuration(Bulletin.DURATION_PROLONG).show();
                        } else {
                            if (userFull != null) {
                                if (oldBirthday == null) {
                                    userFull.flags2 &= ~32;
                                } else {
                                    userFull.flags2 |= 32;
                                }
                                userFull.birthday = oldBirthday;
                                getMessagesStorage().updateUserInfo(userFull, false);
                            }
                            if (err != null && err.text != null && err.text.startsWith("FLOOD_WAIT_")) {
                                if (getContext() != null) {
                                    showDialog(new AlertDialog.Builder(getContext(), resourceProvider).setTitle(getString(R.string.PrivacyBirthdayTooOftenTitle)).setMessage(getString(R.string.PrivacyBirthdayTooOftenMessage)).setPositiveButton(getString(R.string.OK), null).create());
                                }
                            } else {
                                BulletinFactory.of(DebugProfile.this).createSimpleBulletin(R.raw.error, LocaleController.getString(R.string.UnknownError)).show();
                            }
                        }
                    }), ConnectionsManager.RequestFlagDoNotWaitFloodWait);
                }, () -> {
                    BaseFragment.BottomSheetParams params = new BaseFragment.BottomSheetParams();
                    params.transitionFromLeft = true;
                    params.allowNestedScroll = false;
                    showAsSheet(new PrivacyControlActivity(PrivacyControlActivity.PRIVACY_RULES_TYPE_BIRTHDAY), params);
                }, getResourceProvider()).create());
            });
            itemOptions.add(R.drawable.msg_delete, getString(R.string.Remove), true, () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.BirthdayClearTitle));
                builder.setMessage(LocaleController.getString(R.string.BirthdayClearMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Remove), (di, w) -> {
                    TL_account.updateBirthday req = new TL_account.updateBirthday();
                    if (userFull != null) {
                        userFull.birthday = null;
                        userFull.flags2 &= ~32;
                    }
                    getMessagesController().invalidateContentSettings();
                    getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (err != null) {
                            BulletinFactory.showError(err);
                        } else if (res instanceof TLRPC.TL_boolFalse) {
                            BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                        }
                    }));
                    updateListAnimated(false);
                    getMessagesStorage().updateUserInfo(userFull, false);
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                showDialog(builder.create());
            });
        } else if (position == channelRow) {
            TLRPC.Chat channel = getMessagesController().getChat(userFull.personal_channel_id);
            if (channel != null && ChatObject.getPublicUsername(channel) != null) {
                itemOptions.add(R.drawable.msg_copy, getString(R.string.ProfileChannelCopy), () -> {
                    AndroidUtilities.addToClipboard("https://" + getMessagesController().linkPrefix + "/" + ChatObject.getPublicUsername(channel));
                });
            }
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileChannelChange), () -> {
                presentFragment(new UserInfoActivity());
            });
            itemOptions.add(R.drawable.msg_delete, getString(R.string.Remove), true, () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.ProfileChannelClearTitle));
                builder.setMessage(LocaleController.getString(R.string.ProfileChannelClearMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Remove), (di, w) -> {
                    TL_account.updatePersonalChannel req = new TL_account.updatePersonalChannel();
                    req.channel = new TLRPC.TL_inputChannelEmpty();
                    if (userFull != null) {
                        userFull.personal_channel_id = 0;
                        userFull.personal_channel_message = 0;
                        userFull.flags2 &= ~64;
                    }
                    getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (err != null) {
                            BulletinFactory.showError(err);
                        } else if (res instanceof TLRPC.TL_boolFalse) {
                            BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                        }
                    }));
                    updateListAnimated(false);
                    getMessagesStorage().updateUserInfo(userFull, false);
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                showDialog(builder.create());
            });
        }

        if (itemOptions.getItemsCount() <= 0) {
            return false;
        }

        itemOptions.show();

        return true;
    }


    private void openLocation(boolean inMapsApp) {
        if (userInfo == null || userInfo.business_location == null) return;
        if (userInfo.business_location.geo_point != null && !inMapsApp) {
            LocationActivity fragment = new LocationActivity(3) {
                @Override
                protected boolean disablePermissionCheck() {
                    return true;
                }
            };
            fragment.setResourceProvider(resourcesProvider);
            TLRPC.TL_message message = new TLRPC.TL_message();
            message.local_id = -1;
            message.peer_id = getMessagesController().getPeer(getDialogId());
            TLRPC.TL_messageMediaGeo media = new TLRPC.TL_messageMediaGeo();
            media.geo = userInfo.business_location.geo_point;
            media.address = userInfo.business_location.address;
            message.media = media;
            fragment.setSharingAllowed(false);
            fragment.setMessageObject(new MessageObject(UserConfig.selectedAccount, message, false, false));
            presentFragment(fragment);
        } else {
            String domain;
            if (BuildVars.isHuaweiStoreApp()) {
                domain = "mapapp://navigation";
            } else {
                domain = "http://maps.google.com/maps";
            }
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, domain + "?q=" + userInfo.business_location.address)));
                getParentActivity().startActivity(intent);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    private void onWriteButtonClick() {
        if (userId != 0) {
            if (imageUpdater != null) {
                // TODO Update image
//                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
//                if (user == null) {
//                    user = UserConfig.getInstance(currentAccount).getCurrentUser();
//                }
//                if (user == null) {
//                    return;
//                }
//                imageUpdater.openMenu(user.photo != null && user.photo.photo_big != null && !(user.photo instanceof TLRPC.TL_userProfilePhotoEmpty), () -> {
//                    MessagesController.getInstance(currentAccount).deleteUserPhoto(null);
//                    cameraDrawable.setCurrentFrame(0);
//                    cellCameraDrawable.setCurrentFrame(0);
//                }, dialog -> {
//                    if (!imageUpdater.isUploadingImage()) {
//                        cameraDrawable.setCustomEndFrame(86);
//                        cellCameraDrawable.setCustomEndFrame(86);
//                        writeButton.playAnimation();
//                        if (setAvatarCell != null) {
//                            setAvatarCell.getImageView().playAnimation();
//                        }
//                    } else {
//                        cameraDrawable.setCurrentFrame(0, false);
//                        cellCameraDrawable.setCurrentFrame(0, false);
//                    }
//                }, 0);
//                cameraDrawable.setCurrentFrame(0);
//                cameraDrawable.setCustomEndFrame(43);
//                cellCameraDrawable.setCurrentFrame(0);
//                cellCameraDrawable.setCustomEndFrame(43);
//                writeButton.playAnimation();
//                if (setAvatarCell != null) {
//                    setAvatarCell.getImageView().playAnimation();
//                }
            } else {
                if (playProfileAnimation != 0 && parentLayout != null && parentLayout.getFragmentStack() != null && parentLayout.getFragmentStack().size() >= 2 && parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2) instanceof ChatActivity) {
                    finishFragment();
                } else {
                    TLRPC.User user = getMessagesController().getUser(userId);
                    if (user == null || user instanceof TLRPC.TL_userEmpty) {
                        return;
                    }
                    Bundle args = new Bundle();
                    args.putLong("user_id", userId);
                    if (!getMessagesController().checkCanOpenChat(args, DebugProfile.this)) {
                        return;
                    }
                    boolean removeFragment = arguments.getBoolean("removeFragmentOnChatOpen", true);
                    if (!AndroidUtilities.isTablet() && removeFragment) {
                        getNotificationCenter().removeObserver(DebugProfile.this, NotificationCenter.closeChats);
                        getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                    }
                    int distance = getArguments().getInt("nearby_distance", -1);
                    if (distance >= 0) {
                        args.putInt("nearby_distance", distance);
                    }
                    ChatActivity chatActivity = new ChatActivity(args);
                    chatActivity.setPreloadedSticker(getMediaDataController().getGreetingsSticker(), false);
                    presentFragment(chatActivity, removeFragment);
                    if (AndroidUtilities.isTablet()) {
                        finishFragment();
                    }
                }
            }
        } else {
            openDiscussion();
        }
    }

    @Override
    public boolean onFragmentCreate() {
        userId = arguments.getLong("user_id", 0);
        chatId = arguments.getLong("chat_id", 0);
        topicId = arguments.getLong("topic_id", 0);
        saved = arguments.getBoolean("saved", false);
        openSimilar = arguments.getBoolean("similar", false);
        isTopic = topicId != 0;
        banFromGroup = arguments.getLong("ban_chat_id", 0);
        reportReactionMessageId = arguments.getInt("report_reaction_message_id", 0);
        reportReactionFromDialogId = arguments.getLong("report_reaction_from_dialog_id", 0);
        showAddToContacts = arguments.getBoolean("show_add_to_contacts", true);
        vcardPhone = PhoneFormat.stripExceptNumbers(arguments.getString("vcard_phone"));
        vcardFirstName = arguments.getString("vcard_first_name");
        vcardLastName = arguments.getString("vcard_last_name");
        reportSpam = arguments.getBoolean("reportSpam", false);
        myProfile = arguments.getBoolean("my_profile", false);
        openGifts = arguments.getBoolean("open_gifts", false);
        openCommonChats = arguments.getBoolean("open_common", false);
//        if (!expandPhoto) { // TODO
//            expandPhoto = arguments.getBoolean("expandPhoto", false);
//            if (expandPhoto) {
//                currentExpandAnimatorValue = 1f;
//                needSendMessage = true;
//            }
//        }
        if (userId != 0) {
            dialogId = arguments.getLong("dialog_id", 0);
            if (dialogId != 0) {
                currentEncryptedChat = getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(dialogId));
            }
            if (flagSecure != null) {
                flagSecure.invalidate();
            }
            TLRPC.User user = getMessagesController().getUser(userId);
            if (user == null) {
                return false;
            }

            getNotificationCenter().addObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.newSuggestionsAvailable);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatCreated);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.blockedUsersDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.botInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.userInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.privacyRulesUpdated);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.reloadInterface);

            userBlocked = getMessagesController().blockePeers.indexOfKey(userId) >= 0;
            if (user.bot) {
                isBot = true;
                getMediaDataController().loadBotInfo(user.id, user.id, true, classGuid);
            }
            userInfo = getMessagesController().getUserFull(userId);
            getMessagesController().loadFullUser(getMessagesController().getUser(userId), classGuid, true);
            participantsMap = null;

            if (UserObject.isUserSelf(user)) {
                imageUpdater = new ImageUpdater(true, ImageUpdater.FOR_TYPE_USER, true);
                imageUpdater.setOpenWithFrontfaceCamera(true);
                imageUpdater.parentFragment = this;
                imageUpdater.setDelegate(this);
                getMediaDataController().checkFeaturedStickers();
                getMessagesController().loadSuggestedFilters();
                getMessagesController().loadUserInfo(getUserConfig().getCurrentUser(), true, classGuid);
            }
            actionBarAnimationColorFrom = arguments.getInt("actionBarColor", 0);
        } else if (chatId != 0) {
            currentChat = getMessagesController().getChat(chatId);
            if (currentChat == null) {
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                getMessagesStorage().getStorageQueue().postRunnable(() -> {
                    currentChat = getMessagesStorage().getChat(chatId);
                    countDownLatch.countDown();
                });
                try {
                    countDownLatch.await();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                if (currentChat != null) {
                    getMessagesController().putChat(currentChat, true);
                } else {
                    return false;
                }
            }
            if (flagSecure != null) {
                flagSecure.invalidate();
            }

            if (currentChat.megagroup) {
                getChannelParticipants(true);
            } else {
                participantsMap = null;
            }
            getNotificationCenter().addObserver(this, NotificationCenter.chatInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.chatOnlineCountDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.groupCallUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.channelRightsUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.chatWasBoostedByUser);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.uploadStoryEnd);
            sortedUsers = new ArrayList<>();
            updateOnlineCount(true);
            if (chatInfo == null) {
                chatInfo = getMessagesController().getChatFull(chatId);
            }
            if (ChatObject.isChannel(currentChat)) {
                getMessagesController().loadFullChat(chatId, classGuid, true);
            } else if (chatInfo == null) {
                chatInfo = getMessagesStorage().loadChatInfo(chatId, false, null, false, false);
            }

            updateExceptions();
        } else {
            return false;
        }
        if (sharedMediaPreloader == null) {
            sharedMediaPreloader = new SharedMediaLayout.SharedMediaPreloader(this);
        }
        sharedMediaPreloader.addDelegate(this);

        getNotificationCenter().addObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().addObserver(this, NotificationCenter.didReceiveNewMessages);
        getNotificationCenter().addObserver(this, NotificationCenter.closeChats);
        getNotificationCenter().addObserver(this, NotificationCenter.topicsDidLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.updateSearchSettings);
        getNotificationCenter().addObserver(this, NotificationCenter.reloadDialogPhotos);
        getNotificationCenter().addObserver(this, NotificationCenter.storiesUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.storiesReadUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.userIsPremiumBlockedUpadted);
        getNotificationCenter().addObserver(this, NotificationCenter.currentUserPremiumStatusChanged);
        getNotificationCenter().addObserver(this, NotificationCenter.starBalanceUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.botStarsUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.botStarsTransactionsLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.dialogDeleted);
        getNotificationCenter().addObserver(this, NotificationCenter.channelRecommendationsLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.starUserGiftsLoaded);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
        updateRowsIds();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }

        if (arguments.containsKey("preload_messages")) {
            getMessagesController().ensureMessagesLoaded(userId, 0, null);
        }

        if (userId != 0) {
            TLRPC.User user = getMessagesController().getUser(userId);

            if (UserObject.isUserSelf(user)) {
                TL_account.getPassword req = new TL_account.getPassword();
                getConnectionsManager().sendRequest(req, (response, error) -> {
                    if (response instanceof TL_account.TL_password) {
                        currentPassword = (TL_account.TL_password) response;
                    }
                });
            }
        }

        Bulletin.addDelegate(this, new Bulletin.Delegate() {
            @Override
            public int getTopOffset(int tag) {
                return AndroidUtilities.statusBarHeight;
            }

            @Override
            public int getBottomOffset(int tag) {
                if (bottomButtonsContainer == null) {
                    return 0;
                }
                final float gifts = clamp01(1f - Math.abs(sharedMediaLayout.getTabProgress() - SharedMediaLayout.TAB_GIFTS));
                final float stories = clamp01(1f - Math.abs(sharedMediaLayout.getTabProgress() - SharedMediaLayout.TAB_STORIES));
                final float archivedStories = clamp01(1f - Math.abs(sharedMediaLayout.getTabProgress() - SharedMediaLayout.TAB_ARCHIVED_STORIES));
                return lerp((int) (dp(72) - bottomButtonsContainer.getTranslationY() - archivedStories * bottomButtonContainer[1].getTranslationY() - stories * bottomButtonContainer[0].getTranslationY()), 0, gifts);
            }

            @Override
            public boolean bottomOffsetAnimated() {
                return bottomButtonsContainer == null;
            }
        });

        if (userId != 0 && UserObject.isUserSelf(getMessagesController().getUser(userId)) && !myProfile) {
            getMessagesController().getContentSettings(null);
        }

        return true;
    }

    private void updateExceptions() {
        if (!isTopic && ChatObject.isForum(currentChat)) {
            getNotificationsController().loadTopicsNotificationsExceptions(-chatId, (topics) -> {
                ArrayList<Integer> arrayList = new ArrayList<>(topics);
                for (int i = 0; i < arrayList.size(); i++) {
                    if (getMessagesController().getTopicsController().findTopic(chatId, arrayList.get(i)) == null) {
                        arrayList.remove(i);
                        i--;
                    }
                }
                notificationsExceptionTopics.clear();
                notificationsExceptionTopics.addAll(arrayList);

                if (notificationsRow >= 0 && listAdapter != null) {
                    listAdapter.notifyItemChanged(notificationsRow);
                }
            });
        }
    }

    private void updateOnlineCount(boolean notify) {
        onlineCount = 0;
        int currentTime = getConnectionsManager().getCurrentTime();
        sortedUsers.clear();
        if (chatInfo instanceof TLRPC.TL_chatFull || chatInfo instanceof TLRPC.TL_channelFull && chatInfo.participants_count <= 200 && chatInfo.participants != null) {
            final ArrayList<Integer> sortNum = new ArrayList<>();
            for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                TLRPC.ChatParticipant participant = chatInfo.participants.participants.get(a);
                TLRPC.User user = getMessagesController().getUser(participant.user_id);
                if (user != null && user.status != null && (user.status.expires > currentTime || user.id == getUserConfig().getClientUserId()) && user.status.expires > 10000) {
                    onlineCount++;
                }
                sortedUsers.add(a);
                int sort = Integer.MIN_VALUE;
                if (user != null) {
                    if (user.bot) {
                        sort = -110;
                    } else if (user.self) {
                        sort = currentTime + 50000;
                    } else if (user.status != null) {
                        sort = user.status.expires;
                    }
                }
                sortNum.add(sort);
            }

            try {
                Collections.sort(sortedUsers, Comparator.comparingInt(hs -> sortNum.get((int) hs)).reversed());
            } catch (Exception e) {
                FileLog.e(e);
            }

            if (notify && listAdapter != null && membersStartRow > 0) {
                AndroidUtilities.updateVisibleRows(listView);
            }
            if (sharedMediaLayout != null && sharedMediaRow != -1 && (sortedUsers.size() > 5 || usersForceShowingIn == 2) && usersForceShowingIn != 1) {
                sharedMediaLayout.setChatUsers(sortedUsers, chatInfo);
            }
        } else if (chatInfo instanceof TLRPC.TL_channelFull && chatInfo.participants_count > 200) {
            onlineCount = chatInfo.online_count;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View createView(Context context) {
        Theme.createProfileResources(context);
        Theme.createChatResources(context, false);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(final int id) {
                if (getParentActivity() == null) {
                    return;
                }
                if (id == -1) {
                    finishFragment();
                } else if (id == block_contact) {
                    TLRPC.User user = getMessagesController().getUser(userId);
                    if (user == null) {
                        return;
                    }
                    if (!isBot || MessagesController.isSupportUser(user)) {
                        if (userBlocked) {
                            getMessagesController().unblockPeer(userId);
                            if (BulletinFactory.canShowBulletin(DebugProfile.this)) {
                                BulletinFactory.createBanBulletin(DebugProfile.this, false).show();
                            }
                        } else {
                            if (reportSpam) {
                                AlertsCreator.showBlockReportSpamAlert(DebugProfile.this, userId, user, null, currentEncryptedChat, false, null, param -> {
                                    if (param == 1) {
                                        getNotificationCenter().removeObserver(DebugProfile.this, NotificationCenter.closeChats);
                                        getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                                        playProfileAnimation = 0;
                                        finishFragment();
                                    } else {
                                        getNotificationCenter().postNotificationName(NotificationCenter.peerSettingsDidLoad, userId);
                                    }
                                }, resourcesProvider);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                                builder.setTitle(LocaleController.getString(R.string.BlockUser));
                                builder.setMessage(AndroidUtilities.replaceTags(formatString("AreYouSureBlockContact2", R.string.AreYouSureBlockContact2, ContactsController.formatName(user.first_name, user.last_name))));
                                builder.setPositiveButton(LocaleController.getString(R.string.BlockContact), (dialogInterface, i) -> {
                                    getMessagesController().blockPeer(userId);
                                    if (BulletinFactory.canShowBulletin(DebugProfile.this)) {
                                        BulletinFactory.createBanBulletin(DebugProfile.this, true).show();
                                    }
                                });
                                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                                AlertDialog dialog = builder.create();
                                showDialog(dialog);
                                TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                if (button != null) {
                                    button.setTextColor(getThemedColor(Theme.key_text_RedBold));
                                }
                            }
                        }
                    } else {
                        if (!userBlocked) {
                            AlertsCreator.createClearOrDeleteDialogAlert(DebugProfile.this, false, currentChat, user, currentEncryptedChat != null, true, true, (param) -> {
                                if (getParentLayout() != null) {
                                    List<BaseFragment> fragmentStack = getParentLayout().getFragmentStack();
                                    BaseFragment prevFragment = fragmentStack == null || fragmentStack.size() < 2 ? null : fragmentStack.get(fragmentStack.size() - 2);
                                    if (prevFragment instanceof ChatActivity) {
                                        getParentLayout().removeFragmentFromStack(fragmentStack.size() - 2);
                                    }
                                }
                                finishFragment();
                                getNotificationCenter().postNotificationName(NotificationCenter.needDeleteDialog, dialogId, user, currentChat, param);
                            }, getResourceProvider());
                        } else {
                            getMessagesController().unblockPeer(userId, () -> getSendMessagesHelper().sendMessage(SendMessagesHelper.SendMessageParams.of("/start", userId, null, null, null, false, null, null, null, true, 0, null, false)));
                            finishFragment();
                        }
                    }
                } else if (id == add_contact) {
                    TLRPC.User user = getMessagesController().getUser(userId);
                    Bundle args = new Bundle();
                    args.putLong("user_id", user.id);
                    args.putBoolean("addContact", true);
                    openAddToContact(user, args);
                } else if (id == share_contact) {
                    Bundle args = new Bundle();
                    args.putBoolean("onlySelect", true);
                    args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_FORWARD);
                    args.putString("selectAlertString", LocaleController.getString(R.string.SendContactToText));
                    args.putString("selectAlertStringGroup", LocaleController.getString(R.string.SendContactToGroupText));
                    DialogsActivity fragment = new DialogsActivity(args);
                    fragment.setDelegate(DebugProfile.this);
                    presentFragment(fragment);
                } else if (id == edit_contact) {
                    Bundle args = new Bundle();
                    args.putLong("user_id", userId);
                    presentFragment(new ContactAddActivity(args, resourcesProvider));
                } else if (id == delete_contact) {
                    final TLRPC.User user = getMessagesController().getUser(userId);
                    if (user == null || getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                    builder.setTitle(LocaleController.getString(R.string.DeleteContact));
                    builder.setMessage(LocaleController.getString(R.string.AreYouSureDeleteContact));
                    builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialogInterface, i) -> {
                        ArrayList<TLRPC.User> arrayList = new ArrayList<>();
                        arrayList.add(user);
                        getContactsController().deleteContact(arrayList, true);
                        if (user != null) {
                            user.contact = false;
                            updateListAnimated(false);
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                    AlertDialog dialog = builder.create();
                    showDialog(dialog);
                    TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(getThemedColor(Theme.key_text_RedBold));
                    }
                } else if (id == leave_group) {
                    leaveChatPressed();
                } else if (id == delete_topic) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(LocaleController.getPluralString("DeleteTopics", 1));
                    TLRPC.TL_forumTopic topic = MessagesController.getInstance(currentAccount).getTopicsController().findTopic(chatId, topicId);
                    builder.setMessage(formatString("DeleteSelectedTopic", R.string.DeleteSelectedTopic, topic == null ? "topic" : topic.title));
                    builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialog, which) -> {
                        ArrayList<Integer> topicIds = new ArrayList<>();
                        topicIds.add((int) topicId);
                        getMessagesController().getTopicsController().deleteTopics(chatId, topicIds);
                        playProfileAnimation = 0;
                        if (parentLayout != null && parentLayout.getFragmentStack() != null) {
                            for (int i = 0; i < parentLayout.getFragmentStack().size(); ++i) {
                                BaseFragment fragment = parentLayout.getFragmentStack().get(i);
                                if (fragment instanceof ChatActivity && ((ChatActivity) fragment).getTopicId() == topicId) {
                                    fragment.removeSelfFromStack();
                                }
                            }
                        }
                        finishFragment();

                        Context context = getContext();
                        if (context != null) {
                            BulletinFactory.of(Bulletin.BulletinWindow.make(context), resourcesProvider).createSimpleBulletin(R.raw.ic_delete, LocaleController.getPluralString("TopicsDeleted", 1)).show();
                        }
                        dialog.dismiss();
                    });
                    builder.setNegativeButton(LocaleController.getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
                    }
                } else if (id == report) {
                    ReportBottomSheet.openChat(DebugProfile.this, getDialogId());
                } else if (id == edit_channel) {
                    if (isTopic) {
                        Bundle args = new Bundle();
                        args.putLong("chat_id", chatId);
                        TopicCreateFragment fragment = TopicCreateFragment.create(chatId, topicId);
                        presentFragment(fragment);
                    } else {
                        Bundle args = new Bundle();
                        if (chatId != 0) {
                            args.putLong("chat_id", chatId);
                        } else if (isBot) {
                            args.putLong("user_id", userId);
                        }
                        ChatEditActivity fragment = new ChatEditActivity(args);
                        if (chatInfo != null) {
                            fragment.setInfo(chatInfo);
                        } else {
                            fragment.setInfo(userInfo);
                        }
                        presentFragment(fragment);
                    }
                } else if (id == edit_profile) {
                    presentFragment(new UserInfoActivity());
                } else if (id == invite_to_group) {
                    final TLRPC.User user = getMessagesController().getUser(userId);
                    if (user == null) {
                        return;
                    }
                    Bundle args = new Bundle();
                    args.putBoolean("onlySelect", true);
                    args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_ADD_USERS_TO);
                    args.putBoolean("resetDelegate", false);
                    args.putBoolean("closeFragment", false);
                    DialogsActivity fragment = new DialogsActivity(args);
                    fragment.setDelegate((fragment1, dids, message, param, notify, scheduleDate, topicsFragment) -> {
                        long did = dids.get(0).dialogId;

                        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-did);
                        if (chat != null && (chat.creator || chat.admin_rights != null && chat.admin_rights.add_admins)) {
                            getMessagesController().checkIsInChat(false, chat, user, (isInChatAlready, rightsAdmin, currentRank) -> AndroidUtilities.runOnUIThread(() -> {
                                ChatRightsEditActivity editRightsActivity = new ChatRightsEditActivity(userId, -did, rightsAdmin, null, null, currentRank, ChatRightsEditActivity.TYPE_ADD_BOT, true, !isInChatAlready, null);
                                editRightsActivity.setDelegate(new ChatRightsEditActivity.ChatRightsEditActivityDelegate() {
                                    @Override
                                    public void didSetRights(int rights, TLRPC.TL_chatAdminRights rightsAdmin, TLRPC.TL_chatBannedRights rightsBanned, String rank) {
                                        disableProfileAnimation = true;
                                        fragment.removeSelfFromStack();
                                        getNotificationCenter().removeObserver(DebugProfile.this, NotificationCenter.closeChats);
                                        getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                                    }

                                    @Override
                                    public void didChangeOwner(TLRPC.User user) {
                                    }
                                });
                                presentFragment(editRightsActivity);
                            }));
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                            builder.setTitle(LocaleController.getString(R.string.AddBot));
                            String chatName = chat == null ? "" : chat.title;
                            builder.setMessage(AndroidUtilities.replaceTags(formatString("AddMembersAlertNamesText", R.string.AddMembersAlertNamesText, UserObject.getUserName(user), chatName)));
                            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                            builder.setPositiveButton(LocaleController.getString(R.string.AddBot), (di, i) -> {
                                disableProfileAnimation = true;

                                Bundle args1 = new Bundle();
                                args1.putBoolean("scrollToTopOnResume", true);
                                args1.putLong("chat_id", -did);
                                if (!getMessagesController().checkCanOpenChat(args1, fragment1)) {
                                    return;
                                }
                                ChatActivity chatActivity = new ChatActivity(args1);
                                getNotificationCenter().removeObserver(DebugProfile.this, NotificationCenter.closeChats);
                                getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                                getMessagesController().addUserToChat(-did, user, 0, null, chatActivity, true, null, null);
                                presentFragment(chatActivity, true);
                            });
                            showDialog(builder.create());
                        }
                        return true;
                    });
                    presentFragment(fragment);
                } else if (id == share) {
                    try {
                        String text = null;
                        if (userId != 0) {
                            TLRPC.User user = getMessagesController().getUser(userId);
                            if (user == null) {
                                return;
                            }
                            if (botInfo != null && userInfo != null && !TextUtils.isEmpty(userInfo.about)) {
                                text = String.format("%s https://" + getMessagesController().linkPrefix + "/%s", userInfo.about, UserObject.getPublicUsername(user));
                            } else {
                                text = String.format("https://" + getMessagesController().linkPrefix + "/%s", UserObject.getPublicUsername(user));
                            }
                        } else if (chatId != 0) {
                            TLRPC.Chat chat = getMessagesController().getChat(chatId);
                            if (chat == null) {
                                return;
                            }
                            if (chatInfo != null && !TextUtils.isEmpty(chatInfo.about)) {
                                text = String.format("%s\nhttps://" + getMessagesController().linkPrefix + "/%s", chatInfo.about, ChatObject.getPublicUsername(chat));
                            } else {
                                text = String.format("https://" + getMessagesController().linkPrefix + "/%s", ChatObject.getPublicUsername(chat));
                            }
                        }
                        if (TextUtils.isEmpty(text)) {
                            return;
                        }
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, text);
                        startActivityForResult(Intent.createChooser(intent, LocaleController.getString(R.string.BotShare)), 500);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                } else if (id == add_shortcut) {
                    try {
                        long did;
                        if (currentEncryptedChat != null) {
                            did = DialogObject.makeEncryptedDialogId(currentEncryptedChat.id);
                        } else if (userId != 0) {
                            did = userId;
                        } else if (chatId != 0) {
                            did = -chatId;
                        } else {
                            return;
                        }
                        getMediaDataController().installShortcut(did, MediaDataController.SHORTCUT_TYPE_USER_OR_CHAT);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                } else if (id == call_item || id == video_call_item) {
                    startCall(id == video_call_item);
                } else if (id == search_members) {
                    Bundle args = new Bundle();
                    args.putLong("chat_id", chatId);
                    args.putInt("type", ChatUsersActivity.TYPE_USERS);
                    args.putBoolean("open_search", true);
                    ChatUsersActivity fragment = new ChatUsersActivity(args);
                    fragment.setInfo(chatInfo);
                    presentFragment(fragment);
                } else if (id == add_member) {
                    openAddMember();
                } else if (id == statistics) {
                    TLRPC.Chat chat = getMessagesController().getChat(chatId);
                    presentFragment(StatisticActivity.create(chat, false));
                } else if (id == view_discussion) {
                    openDiscussion();
                } else if (id == gift_premium) {
                    if (UserObject.areGiftsDisabled(userInfo)) {
                        BaseFragment lastFragment = LaunchActivity.getSafeLastFragment();
                        if (lastFragment != null) {
                            BulletinFactory.of(lastFragment).createSimpleBulletin(R.raw.error, AndroidUtilities.replaceTags(LocaleController.formatString(R.string.UserDisallowedGifts, DialogObject.getShortName(getDialogId())))).show();
                        }
                        return;
                    }
                    if (currentChat != null) {
                        MessagesController.getGlobalMainSettings().edit().putInt("channelgifthint", 3).apply();
                    }
                    showDialog(new GiftSheet(getContext(), currentAccount, getDialogId(), null, null));
                } else if (id == channel_stories) {
                    Bundle args = new Bundle();
                    args.putInt("type", MediaActivity.TYPE_ARCHIVED_CHANNEL_STORIES);
                    args.putLong("dialog_id", -chatId);
                    MediaActivity fragment = new MediaActivity(args, null);
                    fragment.setChatInfo(chatInfo);
                    presentFragment(fragment);
                } else if (id == start_secret_chat) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                    builder.setTitle(LocaleController.getString(R.string.AreYouSureSecretChatTitle));
                    builder.setMessage(LocaleController.getString(R.string.AreYouSureSecretChat));
                    builder.setPositiveButton(LocaleController.getString(R.string.Start), (dialogInterface, i) -> {
                        if (MessagesController.getInstance(currentAccount).isFrozen()) {
                            AccountFrozenAlert.show(currentAccount);
                            return;
                        }
                        getSecretChatHelper().startSecretChat(getParentActivity(), getMessagesController().getUser(userId));
                    });
                    builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (id == bot_privacy) {
                    BotWebViewAttachedSheet.openPrivacy(currentAccount, userId);
                } else if (id == gallery_menu_save) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= 23 && (Build.VERSION.SDK_INT <= 28 || BuildVars.NO_SCOPED_STORAGE) && getParentActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        getParentActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE);
                        return;
                    }
                    ImageLocation location = avatarsViewPager.getImageLocation(avatarsViewPager.getRealPosition());
                    if (location == null) {
                        return;
                    }
                    final boolean isVideo = location.imageType == FileLoader.IMAGE_TYPE_ANIMATION;
                    File f = FileLoader.getInstance(currentAccount).getPathToAttach(location.location, isVideo ? "mp4" : null, true);
                    if (isVideo && !f.exists()) {
                        f = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_IMAGE), FileLoader.getAttachFileName(location.location, "mp4"));
                    }
                    if (f.exists()) {
                        MediaController.saveFile(f.toString(), getParentActivity(), 0, null, null, uri -> {
                            if (getParentActivity() == null) {
                                return;
                            }
                            BulletinFactory.createSaveToGalleryBulletin(DebugProfile.this, isVideo, null).show();
                        });
                    }
                } else if (id == edit_info) {
                    presentFragment(new UserInfoActivity());
                } else if (id == edit_color) {
                    if (!getUserConfig().isPremium()) {
                        showDialog(new PremiumFeatureBottomSheet(DebugProfile.this, PremiumPreviewFragment.PREMIUM_FEATURE_NAME_COLOR, true));
                        return;
                    }
                    presentFragment(new PeerColorActivity(0).startOnProfile().setOnApplied(DebugProfile.this));
                } else if (id == copy_link_profile) {
                    TLRPC.User user = getMessagesController().getUser(userId);
                    AndroidUtilities.addToClipboard(getMessagesController().linkPrefix + "/" + UserObject.getPublicUsername(user));
                } else if (id == set_username) {
                    presentFragment(new ChangeUsernameActivity());
                } else if (id == logout) {
                    presentFragment(new LogoutActivity());
                } else if (id == set_as_main) {
                    int position = avatarsViewPager.getRealPosition();
                    TLRPC.Photo photo = avatarsViewPager.getPhoto(position);
                    if (photo == null) {
                        return;
                    }
                    avatarsViewPager.startMovePhotoToBegin(position);

                    TLRPC.TL_photos_updateProfilePhoto req = new TLRPC.TL_photos_updateProfilePhoto();
                    req.id = new TLRPC.TL_inputPhoto();
                    req.id.id = photo.id;
                    req.id.access_hash = photo.access_hash;
                    req.id.file_reference = photo.file_reference;
                    UserConfig userConfig = getUserConfig();
                    getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                        avatarsViewPager.finishSettingMainPhoto();
                        if (response instanceof TLRPC.TL_photos_photo) {
                            TLRPC.TL_photos_photo photos_photo = (TLRPC.TL_photos_photo) response;
                            getMessagesController().putUsers(photos_photo.users, false);
                            TLRPC.User user = getMessagesController().getUser(userConfig.clientUserId);
                            if (photos_photo.photo instanceof TLRPC.TL_photo) {
                                avatarsViewPager.replaceFirstPhoto(photo, photos_photo.photo);
                                if (user != null) {
                                    user.photo.photo_id = photos_photo.photo.id;
                                    userConfig.setCurrentUser(user);
                                    userConfig.saveConfig(true);
                                }
                            }
                        }
                    }));
                    undoView.showWithAction(userId, UndoView.ACTION_PROFILE_PHOTO_CHANGED, photo.video_sizes.isEmpty() ? null : 1);
                    TLRPC.User user = getMessagesController().getUser(userConfig.clientUserId);

                    TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 800);
                    if (user != null) {
                        TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 90);
                        user.photo.photo_id = photo.id;
                        user.photo.photo_small = smallSize.location;
                        user.photo.photo_big = bigSize.location;
                        userConfig.setCurrentUser(user);
                        userConfig.saveConfig(true);
                        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
                        updateProfileData(true);
                    }
                    avatarsViewPager.commitMoveToBegin();
                } else if (id == edit_avatar) {
                    if (MessagesController.getInstance(currentAccount).isFrozen()) {
                        AccountFrozenAlert.show(currentAccount);
                        return;
                    }
                    int position = avatarsViewPager.getRealPosition();
                    ImageLocation location = avatarsViewPager.getImageLocation(position);
                    if (location == null) {
                        return;
                    }

                    File f = FileLoader.getInstance(currentAccount).getPathToAttach(PhotoViewer.getFileLocation(location), PhotoViewer.getFileLocationExt(location), true);
                    boolean isVideo = location.imageType == FileLoader.IMAGE_TYPE_ANIMATION;
                    String thumb;
                    if (isVideo) {
                        ImageLocation imageLocation = avatarsViewPager.getRealImageLocation(position);
                        thumb = FileLoader.getInstance(currentAccount).getPathToAttach(PhotoViewer.getFileLocation(imageLocation), PhotoViewer.getFileLocationExt(imageLocation), true).getAbsolutePath();
                    } else {
                        thumb = null;
                    }
                    imageUpdater.openPhotoForEdit(f.getAbsolutePath(), thumb, 0, isVideo);
                } else if (id == delete_avatar) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                    ImageLocation location = avatarsViewPager.getImageLocation(avatarsViewPager.getRealPosition());
                    if (location == null) {
                        return;
                    }
                    if (location.imageType == FileLoader.IMAGE_TYPE_ANIMATION) {
                        builder.setTitle(LocaleController.getString(R.string.AreYouSureDeleteVideoTitle));
                        builder.setMessage(getString(R.string.AreYouSureDeleteVideo));
                    } else {
                        builder.setTitle(LocaleController.getString(R.string.AreYouSureDeletePhotoTitle));
                        builder.setMessage(getString(R.string.AreYouSureDeletePhoto));
                    }
                    builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialogInterface, i) -> {
                        int position = avatarsViewPager.getRealPosition();
                        TLRPC.Photo photo = avatarsViewPager.getPhoto(position);
                        TLRPC.UserFull userFull = userInfo;
                        if (avatar != null && position == 0) {
                            imageUpdater.cancel();
                            if (avatarUploadingRequest != 0) {
                                getConnectionsManager().cancelRequest(avatarUploadingRequest, true);
                            }
                            // TODO
//                            allowPullingDown = !AndroidUtilities.isTablet() && !isInLandscapeMode && avatarImage.getImageReceiver().hasNotThumb() && !AndroidUtilities.isAccessibilityScreenReaderEnabled();
                            avatar = null;
                            avatarBig = null;
                            avatarsViewPager.scrolledByUser = true;
                            avatarsViewPager.removeUploadingImage(uploadingImageLocation);
                            avatarsViewPager.setCreateThumbFromParent(false);
                            updateProfileData(true);
                            showAvatarProgress(false, true);
                            getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                            getUserConfig().saveConfig(true);
                            return;
                        }
                        if (hasFallbackPhoto && photo != null && userFull != null && userFull.fallback_photo != null && userFull.fallback_photo.id == photo.id) {
                            userFull.fallback_photo = null;
                            userFull.flags &= ~4194304;
                            getMessagesStorage().updateUserInfo(userFull, true);
                            updateProfileData(false);
                        }
                        if (avatarsViewPager.getRealCount() == 1) {
                            setForegroundImage(true);
                        }
                        if (photo == null || avatarsViewPager.getRealPosition() == 0) {
                            TLRPC.Photo nextPhoto = avatarsViewPager.getPhoto(1);
                            if (nextPhoto != null) {
                                getUserConfig().getCurrentUser().photo = new TLRPC.TL_userProfilePhoto();
                                TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(nextPhoto.sizes, 90);
                                TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(nextPhoto.sizes, 1000);
                                if (smallSize != null && bigSize != null) {
                                    getUserConfig().getCurrentUser().photo.photo_small = smallSize.location;
                                    getUserConfig().getCurrentUser().photo.photo_big = bigSize.location;
                                }
                            } else {
                                getUserConfig().getCurrentUser().photo = new TLRPC.TL_userProfilePhotoEmpty();
                            }
                            getMessagesController().deleteUserPhoto(null);
                        } else {
                            TLRPC.TL_inputPhoto inputPhoto = new TLRPC.TL_inputPhoto();
                            inputPhoto.id = photo.id;
                            inputPhoto.access_hash = photo.access_hash;
                            inputPhoto.file_reference = photo.file_reference;
                            if (inputPhoto.file_reference == null) {
                                inputPhoto.file_reference = new byte[0];
                            }
                            getMessagesController().deleteUserPhoto(inputPhoto);
                            getMessagesStorage().clearUserPhoto(userId, photo.id);
                        }
                        if (avatarsViewPager.removePhotoAtIndex(position) || avatarsViewPager.getRealCount() <= 0) {
                            avatarsViewPager.setVisibility(View.GONE);
                            avatarImage.setForegroundAlpha(1f);
                            avatarContainer.setVisibility(View.VISIBLE);
                            doNotSetForeground = true;
                            final View view = layoutManager.findViewByPosition(0);
                            if (view != null) {
                                listView.smoothScrollBy(0, view.getTop() - AndroidUtilities.dp(88), CubicBezierInterpolator.EASE_OUT_QUINT);
                            }
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                    AlertDialog alertDialog = builder.create();
                    showDialog(alertDialog);
                    TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(getThemedColor(Theme.key_text_RedBold));
                    }
                } else if (id == add_photo) {
//                    onWriteButtonClick(); // TODO
                } else if (id == qr_button) {
//                    if (qrItem != null && qrItem.getAlpha() > 0) {
//                        Bundle args = new Bundle();
//                        args.putLong("chat_id", chatId);
//                        args.putLong("user_id", userId);
//                        presentFragment(new QrActivity(args));
//                    }
                }
            }
        });

        updateRowsIds();

        contentView = new NestedFrameLayout(context) {

            private final ArrayList<View> sortedChildren = new ArrayList<>();
            private final Comparator<View> viewComparator = (view, view2) -> (int) (view.getY() - view2.getY());
            private boolean ignoreLayout;
            private Paint grayPaint = new Paint();
            private boolean wasPortrait;

//            @Override
//            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//                super.onLayout(changed, left, top, right, bottom);
//                savedScrollPosition = -1;
//                firstLayout = false;
//                invalidateScroll = false;
//                checkListViewScroll();
//            }

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (pinchToZoomHelper.isInOverlayMode()) {
                    return pinchToZoomHelper.onTouchEvent(ev);
                }
                if (sharedMediaLayout != null && sharedMediaLayout.isInFastScroll() && sharedMediaLayout.isPinnedToTop()) {
                    return sharedMediaLayout.dispatchFastScrollEvent(ev);
                }
                if (sharedMediaLayout != null && sharedMediaLayout.checkPinchToZoom(ev)) {
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }

            @Override
            public boolean hasOverlappingRendering() {
                return false;
            }

            @Override
            public void requestLayout() {
                if (ignoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                whitePaint.setColor(getThemedColor(Theme.key_windowBackgroundWhite));
                if (listView.getVisibility() == VISIBLE) {
                    grayPaint.setColor(getThemedColor(Theme.key_windowBackgroundGray));
                    // TODO
//                    if (transitionAnimationInProress) {
//                        whitePaint.setAlpha((int) (255 * listView.getAlpha()));
//                    }
//                    if (transitionAnimationInProress) {
//                        grayPaint.setAlpha((int) (255 * listView.getAlpha()));
//                    }

                    int count = listView.getChildCount();
                    sortedChildren.clear();
                    boolean hasRemovingItems = false;
                    for (int i = 0; i < count; i++) {
                        View child = listView.getChildAt(i);
                        if (listView.getChildAdapterPosition(child) != RecyclerView.NO_POSITION) {
                            sortedChildren.add(listView.getChildAt(i));
                        } else {
                            hasRemovingItems = true;
                        }
                    }
                    Collections.sort(sortedChildren, viewComparator);
                    boolean hasBackground = false;
                    float lastY = listView.getY();
                    count = sortedChildren.size();
                    if (!openAnimationInProgress && count > 0 && !hasRemovingItems) {
                        lastY += sortedChildren.get(0).getY();
                    }
                    float alpha = 1f;
                    for (int i = 0; i < count; i++) {
                        View child = sortedChildren.get(i);
                        boolean currentHasBackground = child.getBackground() != null;
                        int currentY = (int) (listView.getY() + child.getY());
                        if (hasBackground == currentHasBackground) {
                            if (child.getAlpha() == 1f) {
                                alpha = 1f;
                            }
                            continue;
                        }
                        if (hasBackground) {
                            canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), currentY, grayPaint);
                        } else {
                            if (alpha != 1f) {
                                canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), currentY, grayPaint);
                                whitePaint.setAlpha((int) (255 * alpha));
                                canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), currentY, whitePaint);
                                whitePaint.setAlpha(255);
                            } else {
                                canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), currentY, whitePaint);
                            }
                        }
                        hasBackground = currentHasBackground;
                        lastY = currentY;
                        alpha = child.getAlpha();
                    }

                    if (hasBackground) {
                        canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), listView.getBottom(), grayPaint);
                    } else {
                        if (alpha != 1f) {
                            canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), listView.getBottom(), grayPaint);
                            whitePaint.setAlpha((int) (255 * alpha));
                            canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), listView.getBottom(), whitePaint);
                            whitePaint.setAlpha(255);
                        } else {
                            canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), listView.getBottom(), whitePaint);
                        }
                    }
                } else {
                    // TODO
//                    int top = searchListView.getTop();
//                    canvas.drawRect(0, top + extraHeight + searchTransitionOffset, getMeasuredWidth(), top + getMeasuredHeight(), whitePaint);
                }
                super.dispatchDraw(canvas);
                // TODO
//                if (profileTransitionInProgress && parentLayout.getFragmentStack().size() > 1) {
//                    BaseFragment fragment = parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2);
//                    if (fragment instanceof ChatActivity) {
//                        ChatActivity chatActivity = (ChatActivity) fragment;
//                        FragmentContextView fragmentContextView = chatActivity.getFragmentContextView();
//
//                        if (fragmentContextView != null && fragmentContextView.isCallStyle()) {
//                            float progress = extraHeight / AndroidUtilities.dpf2(fragmentContextView.getStyleHeight());
//                            if (progress > 1f) {
//                                progress = 1f;
//                            }
//                            canvas.save();
//                            canvas.translate(fragmentContextView.getX(), fragmentContextView.getY());
//                            fragmentContextView.setDrawOverlay(true);
//                            fragmentContextView.setCollapseTransition(true, extraHeight, progress);
//                            fragmentContextView.draw(canvas);
//                            fragmentContextView.setCollapseTransition(false, extraHeight, progress);
//                            fragmentContextView.setDrawOverlay(false);
//                            canvas.restore();
//                        }
//                    }
//                }

                if (scrimPaint.getAlpha() > 0) {
                    canvas.drawRect(0, 0, getWidth(), getHeight(), scrimPaint);
                }
                if (scrimView != null) {
                    int c = canvas.save();
                    canvas.translate(scrimView.getLeft(), scrimView.getTop());
                    if (scrimView == actionBar.getBackButton()) {
                        int r = Math.max(scrimView.getMeasuredWidth(), scrimView.getMeasuredHeight()) / 2;
                        int wasAlpha = actionBarBackgroundPaint.getAlpha();
                        actionBarBackgroundPaint.setAlpha((int) (wasAlpha * (scrimPaint.getAlpha() / 255f) / 0.3f));
                        canvas.drawCircle(r, r, r * 0.7f, actionBarBackgroundPaint);
                        actionBarBackgroundPaint.setAlpha(wasAlpha);
                    }
                    scrimView.draw(canvas);
                    canvas.restoreToCount(c);
                }
                if (blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
                    if (blurredView.getAlpha() != 1f) {
                        if (blurredView.getAlpha() != 0) {
                            canvas.saveLayerAlpha(blurredView.getLeft(), blurredView.getTop(), blurredView.getRight(), blurredView.getBottom(), (int) (255 * blurredView.getAlpha()), Canvas.ALL_SAVE_FLAG);
                            canvas.translate(blurredView.getLeft(), blurredView.getTop());
                            blurredView.draw(canvas);
                            canvas.restore();
                        }
                    } else {
                        blurredView.draw(canvas);
                    }
                }
            }

            @Override
            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                if (pinchToZoomHelper.isInOverlayMode() && (child == actionBar)) {
                    return true;
                }
                if (child == blurredView) {
                    return true;
                }
                return super.drawChild(canvas, child, drawingTime);
            }

            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                fragmentViewAttached = true;
                for (int i = 0; i < emojiStatusDrawable.length; i++) {
                    if (emojiStatusDrawable[i] != null) {
                        emojiStatusDrawable[i].attach();
                    }
                }
                for (int i = 0; i < botVerificationDrawable.length; ++i) {
                    if (botVerificationDrawable[i] != null) {
                        botVerificationDrawable[i].attach();
                    }
                }
            }

            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                fragmentViewAttached = false;
                for (int i = 0; i < emojiStatusDrawable.length; i++) {
                    if (emojiStatusDrawable[i] != null) {
                        emojiStatusDrawable[i].detach();
                    }
                }
                for (int i = 0; i < botVerificationDrawable.length; ++i) {
                    if (botVerificationDrawable[i] != null) {
                        botVerificationDrawable[i].detach();
                    }
                }
            }
        };
        fragmentView = contentView;
        fragmentView.setWillNotDraw(false);
        contentView.needBlur = true;

        layoutManager = new LinearLayoutManager(context) {

//            @Override
//            public boolean supportsPredictiveItemAnimations() {
//                return imageUpdater != null;
//            }

            @Override
            public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
                final View view = layoutManager.findViewByPosition(0);
                // TODO
//                if (view != null && !openingAvatar) {
                if (view != null) {
                    final int canScroll = view.getTop() - expandedOffset;
                    if (!allowPullingDown && canScroll > dy) {
                        dy = canScroll;
                        if (avatarsViewPager.hasImages() && avatarImage.getImageReceiver().hasNotThumb() && !AndroidUtilities.isAccessibilityScreenReaderEnabled() && !isInLandscapeMode && !AndroidUtilities.isTablet()) {
                            allowPullingDown = avatarBig == null;
                        }
                    } else if (allowPullingDown) {
                        if (dy >= canScroll) {
                            dy = canScroll;
                            allowPullingDown = false;
                        } else if (listView.getScrollState() == RecyclerListView.SCROLL_STATE_DRAGGING) {
                            if (!isPulledDown) {
                                dy /= 2;
                            }
                        }
                    }
                }
                return super.scrollVerticallyBy(dy, recycler, state);
            }
        };
        listView = new RecyclerListView(context);
        listView.setLayoutManager(layoutManager);
        listAdapter = new ListAdapter(context);
        listView.setAdapter(listAdapter);
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
        listView.setOnItemClickListener(this::onItemClick);


        contentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        topScroll = expandedOffset;

        topView = new TopView(context);
        topView.setBackgroundColorId(peerColor, false);
        topView.setBackgroundColor(getThemedColor(Theme.key_avatar_backgroundActionBarBlue));
        contentView.addView(topView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));


        avatarDrawable = new AvatarDrawable();
        avatarDrawable.setProfile(true);

        avatarContainer = new FrameLayout(context);
        avatarContainer.setPivotX(0);
        avatarContainer.setPivotY(0);
        contentView.addView(avatarContainer, LayoutHelper.createFrame(AVATAR_SIZE_DP, AVATAR_SIZE_DP, Gravity.TOP | Gravity.LEFT));

        avatarImage = new AvatarImageView(context);
        avatarImage.setBlurAllowed(true);
        avatarImage.setHasBlur(true);
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


        overlaysView = new OverlaysView(context);
        avatarsViewPager = new ProfileGalleryView(context, userId != 0 ? userId : -chatId, actionBar, listView, avatarImage, getClassGuid(), overlaysView) {
            @Override
            protected void setCustomAvatarProgress(float progress) {
                customAvatarProgress = progress;
                checkPhotoDescriptionAlpha();
            }

            @Override
            public void setVisibility(int visibility) {
                super.setVisibility(visibility);
            }
        };
        checkLayout();
        avatarsViewPager.setBottomBlurPadding(maximizedOffset - displaySize.x);


        contentView.addView(avatarsViewPager);
        contentView.addView(overlaysView);
        avatarImage.setAvatarsViewPager(avatarsViewPager);

        avatarsViewPagerIndicatorView = new PagerIndicatorView(context);
        contentView.addView(avatarsViewPagerIndicatorView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));


        initHeaderButtons(context, contentView);

        contentView.addView(actionBar);


        // TODO
        fallbackImage = new ImageReceiver(contentView);
        fallbackImage.setRoundRadius(AndroidUtilities.dp(11));

        undoView = new UndoView(context, null, false, resourcesProvider);
        contentView.addView(undoView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));

//        createFloatingActionButton(getContext());

        debugText = new TextView(context);
        debugText.setTextColor(getThemedColor(Theme.key_actionBarDefaultTitle));
        debugText.setTextSize(10);
        contentView.addView(debugText, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.RIGHT, 0, 24, 4, 0));


        avatarMaximizeAnimator = ValueAnimator.ofFloat(0f, 1f);
        avatarMaximizeAnimator.addUpdateListener(anim -> {
            setAvatarMaximizeAnimationProgress(anim.getAnimatedFraction());
        });
        avatarMaximizeAnimator.setDuration(500);
        avatarMaximizeAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        avatarMaximizeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                avatarImage.setBlurRoundRadiusEnabled(false);
                if (!isPulledDown) {
                    avatarsViewPager.setVisibility(View.GONE);
                    updateAvatar();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setAvatarMaximizeAnimationProgress(1.0f);
                actionBar.setItemsBackgroundColor(isPulledDown ? Theme.ACTION_BAR_WHITE_SELECTOR_COLOR : peerColor != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), false);
                avatarImage.clearForeground();
                if (!isPulledDown) {
                    avatarImage.setBlurRoundRadiusEnabled(true);
                }
                avatarsViewPager.setVisibility(isPulledDown ? View.VISIBLE : View.GONE);
                doNotSetForeground = false;
//                updateStoriesViewBounds(false);
            }
        });

        initText(context);


        if (myProfile) {
            bottomButtonsContainer = new FrameLayout(context);

            bottomButtonContainer = new FrameLayout[2];
            bottomButton = new ButtonWithCounterView[2];
            for (int a = 0; a < 2; ++a) {
                bottomButtonContainer[a] = new FrameLayout(context);
                bottomButtonContainer[a].setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));

                View shadow = new View(context);
                shadow.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
                bottomButtonContainer[a].addView(shadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1 / AndroidUtilities.density, Gravity.TOP | Gravity.FILL_HORIZONTAL));

                bottomButton[a] = new ButtonWithCounterView(context, resourcesProvider);
                if (a == 0) {
                    bottomButtonPostText = new SpannableStringBuilder("c");
                    bottomButtonPostText.setSpan(new ColoredImageSpan(R.drawable.filled_premium_camera), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    bottomButtonPostText.append("  ").append(getString(R.string.StoriesAddPost));
                    bottomButton[a].setText(bottomButtonPostText, false);
                } else {
                    bottomButton[a].setText(getString(R.string.StorySave), false);
                }
                final int finalA = a;
                bottomButton[a].setOnClickListener(v -> {
                    if (finalA == 0 && !sharedMediaLayout.isActionModeShown()) {
                        if (!getMessagesController().storiesEnabled()) {
                            showDialog(new PremiumFeatureBottomSheet(this, PremiumPreviewFragment.PREMIUM_FEATURE_STORIES, true));
                            return;
                        }
                        getMessagesController().getMainSettings().edit().putBoolean("story_keep", true).apply();
                        StoryRecorder.getInstance(getParentActivity(), getCurrentAccount()).closeToWhenSent(new StoryRecorder.ClosingViewProvider() {
                            @Override
                            public void preLayout(long dialogId, Runnable runnable) {
                                avatarImage.setHasStories(needInsetForStories());
                                if (dialogId == getDialogId()) {
//                                            collapseAvatarInstant(); // TODO
                                }
                                AndroidUtilities.runOnUIThread(runnable, 30);
                            }

                            @Override
                            public StoryRecorder.SourceView getView(long dialogId) {
                                if (dialogId != getDialogId()) {
                                    return null;
                                }
                                updateAvatarRoundRadius();
                                return StoryRecorder.SourceView.fromAvatarImage(avatarImage, ChatObject.isForum(currentChat));
                            }
                        }).open(null);
                    } else {
                        final long dialogId = getUserConfig().getClientUserId();
                        if (applyBulletin != null) {
                            applyBulletin.run();
                            applyBulletin = null;
                        }
                        Bulletin.hideVisible();
                        boolean pin = sharedMediaLayout.getClosestTab() == SharedMediaLayout.TAB_ARCHIVED_STORIES;
                        int count = 0;
                        ArrayList<TL_stories.StoryItem> storyItems = new ArrayList<>();
                        SparseArray<MessageObject> actionModeMessageObjects = sharedMediaLayout.getActionModeSelected();
                        if (actionModeMessageObjects != null) {
                            for (int i = 0; i < actionModeMessageObjects.size(); ++i) {
                                MessageObject messageObject = actionModeMessageObjects.valueAt(i);
                                if (messageObject.storyItem != null) {
                                    storyItems.add(messageObject.storyItem);
                                    count++;
                                }
                            }
                        }
                        sharedMediaLayout.closeActionMode(false);
                        if (pin) {
                            sharedMediaLayout.scrollToPage(SharedMediaLayout.TAB_STORIES);
                        }
                        if (storyItems.isEmpty()) {
                            return;
                        }
                        boolean[] pastValues = new boolean[storyItems.size()];
                        for (int i = 0; i < storyItems.size(); ++i) {
                            TL_stories.StoryItem storyItem = storyItems.get(i);
                            pastValues[i] = storyItem.pinned;
                            storyItem.pinned = pin;
                        }
                        getMessagesController().getStoriesController().updateStoriesInLists(dialogId, storyItems);
                        final boolean[] undone = new boolean[]{false};
                        applyBulletin = () -> {
                            getMessagesController().getStoriesController().updateStoriesPinned(dialogId, storyItems, pin, null);
                        };
                        final Runnable undo = () -> {
                            undone[0] = true;
                            AndroidUtilities.cancelRunOnUIThread(applyBulletin);
                            for (int i = 0; i < storyItems.size(); ++i) {
                                TL_stories.StoryItem storyItem = storyItems.get(i);
                                storyItem.pinned = pastValues[i];
                            }
                            getMessagesController().getStoriesController().updateStoriesInLists(dialogId, storyItems);
                        };
                        Bulletin bulletin;
                        if (pin) {
                            bulletin = BulletinFactory.of(this).createSimpleBulletin(R.raw.contact_check, LocaleController.formatPluralString("StorySavedTitle", count), LocaleController.getString(R.string.StorySavedSubtitle), LocaleController.getString(R.string.Undo), undo).show();
                        } else {
                            bulletin = BulletinFactory.of(this).createSimpleBulletin(R.raw.chats_archived, LocaleController.formatPluralString("StoryArchived", count), LocaleController.getString(R.string.Undo), Bulletin.DURATION_PROLONG, undo).show();
                        }
                        bulletin.setOnHideListener(() -> {
                            if (!undone[0] && applyBulletin != null) {
                                applyBulletin.run();
                            }
                            applyBulletin = null;
                        });
                    }
                });
                bottomButtonContainer[a].addView(bottomButton[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 12, 12, 12, 12));

                bottomButtonsContainer.addView(bottomButtonContainer[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
                if (a == 1 || !getMessagesController().storiesEnabled()) {
                    bottomButtonContainer[a].setTranslationY(dp(72));
                }
            }
        }


        final ArrayList<Integer> users = chatInfo != null && chatInfo.participants != null && chatInfo.participants.participants.size() > 5 ? sortedUsers : null;
        int initialTab = -1;
        if (openCommonChats) {
            initialTab = SharedMediaLayout.TAB_COMMON_GROUPS;
        } else if (openGifts && (userInfo != null && userInfo.stargifts_count > 0 || chatInfo != null && chatInfo.stargifts_count > 0)) {
            initialTab = SharedMediaLayout.TAB_GIFTS;
            openedGifts = true;
        } else if (openSimilar) {
            initialTab = SharedMediaLayout.TAB_RECOMMENDED_CHANNELS;
        } else if (users != null) {
            initialTab = SharedMediaLayout.TAB_GROUPUSERS;
        }
        sharedMediaLayout = new SharedMediaLayout(context, getDialogId(), sharedMediaPreloader, userInfo != null ? userInfo.common_chats_count : 0, sortedUsers, chatInfo, userInfo, initialTab, this, this, SharedMediaLayout.VIEW_TYPE_PROFILE_ACTIVITY, resourcesProvider) {
            @Override
            protected int processColor(int color) {
                return dontApplyPeerColor(color, false);
            }

            @Override
            protected void onSelectedTabChanged() {
                updateSelectedMediaTabText();
            }

            @Override
            protected boolean includeSavedDialogs() {
                return dialogId == getUserConfig().getClientUserId() && !saved;
            }

            @Override
            protected boolean isSelf() {
                return myProfile;
            }

            @Override
            protected boolean isStoriesView() {
                return myProfile;
            }

            @Override
            protected void onSearchStateChanged(boolean expanded) {
                // TODO
//                AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);
//
//                listView.stopScroll();
//                avatarContainer2.setPivotY(avatarContainer.getPivotY() + avatarContainer.getMeasuredHeight() / 2f);
//                avatarContainer2.setPivotX(avatarContainer2.getMeasuredWidth() / 2f);
//                AndroidUtilities.updateViewVisibilityAnimated(avatarContainer2, !expanded, 0.95f, true);
//
//                callItem.setVisibility(expanded || !callItemVisible ? GONE : INVISIBLE);
//                videoCallItem.setVisibility(expanded || !videoCallItemVisible ? GONE : INVISIBLE);
//                editItem.setVisibility(expanded || !editItemVisible ? GONE : INVISIBLE);
//                otherItem.setVisibility(expanded ? GONE : INVISIBLE);
//                if (qrItem != null) {
//                    qrItem.setVisibility(expanded ? GONE : INVISIBLE);
//                }
//                updateStoriesViewBounds(false);
            }

            @Override
            protected boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, View view) {
                return DebugProfile.this.onMemberClick(participant, isLong, view);
            }

            @Override
            protected void drawBackgroundWithBlur(Canvas canvas, float y, Rect rectTmp2, Paint backgroundPaint) {
                // TODO
                contentView.drawBlurRect(canvas, listView.getY() + getY() + y, rectTmp2, backgroundPaint, true);
            }

            @Override
            protected void invalidateBlur() {
                if (contentView != null) {
                    contentView.invalidateBlur();
                }
            }

            @Override
            protected int getInitialTab() {
                return TAB_STORIES;
            }

            @Override
            protected void showActionMode(boolean show) {
                super.showActionMode(show);
                if (myProfile) {
                    disableScroll(show);

                    int a = getSelectedTab() - SharedMediaLayout.TAB_STORIES;
                    if (a < 0 || a > 1) return;
                    bottomButtonContainer[a].animate().translationY(show || a == 0 && MessagesController.getInstance(currentAccount).storiesEnabled() ? 0 : dp(72)).setDuration(320).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).setUpdateListener(anm -> updateBottomButtonY()).start();
                }
            }

            @Override
            protected void onTabProgress(float progress) {
                super.onTabProgress(progress);
                if (myProfile) {
                    int width = sharedMediaLayout == null ? AndroidUtilities.displaySize.x : sharedMediaLayout.getMeasuredWidth();
                    if (bottomButtonContainer[0] != null)
                        bottomButtonContainer[0].setTranslationX((SharedMediaLayout.TAB_STORIES - progress) * width);
                    if (bottomButtonContainer[1] != null)
                        bottomButtonContainer[1].setTranslationX((SharedMediaLayout.TAB_ARCHIVED_STORIES - progress) * width);
                    updateBottomButtonY();
                }
            }

            @Override
            protected void onActionModeSelectedUpdate(SparseArray<MessageObject> messageObjects) {
                super.onActionModeSelectedUpdate(messageObjects);
                if (myProfile) {
                    final int count = messageObjects.size();
                    int a = getSelectedTab() - SharedMediaLayout.TAB_STORIES;
                    if (a < 0 || a > 1) return;
                    if (a == 0) {
                        bottomButton[a].setText(count > 0 || !MessagesController.getInstance(currentAccount).storiesEnabled() ? formatPluralString("ArchiveStories", count) : bottomButtonPostText, true);
                    }
                    bottomButton[a].setCount(count, true);
                }
            }

            @Override
            public void openStoryRecorder() {
                StoryRecorder.getInstance(getParentActivity(), currentAccount).selectedPeerId(getDialogId()).canChangePeer(false).closeToWhenSent(new StoryRecorder.ClosingViewProvider() {
                    @Override
                    public void preLayout(long dialogId, Runnable runnable) {
                        avatarImage.setHasStories(needInsetForStories());
                        if (dialogId == getDialogId()) {
                            collapseAvatarInstant();
                        }
                        AndroidUtilities.runOnUIThread(runnable, 30);
                    }

                    @Override
                    public StoryRecorder.SourceView getView(long dialogId) {
                        if (dialogId != getDialogId()) {
                            return null;
                        }
                        updateAvatarRoundRadius();
                        return StoryRecorder.SourceView.fromAvatarImage(avatarImage, ChatObject.isForum(currentChat));
                    }
                });
//                        .open(StoryRecorder.SourceView.fromFloatingButton(floatingButtonContainer), true); // TODO
            }

            @Override
            public void updateTabs(boolean animated) {
                super.updateTabs(animated);
                if (openGifts && !openedGifts && scrollSlidingTextTabStrip.hasTab(TAB_GIFTS)) {
                    openedGifts = true;
                    scrollToPage(TAB_GIFTS);
                }
            }
        };
        sharedMediaLayout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));

        ActionBarMenu menu = actionBar.createMenu();

        if (userId == getUserConfig().clientUserId && !myProfile) {
            // TODO: for settings
//            qrItem = menu.addItem(qr_button, R.drawable.msg_qr_mini, getResourceProvider());
//            qrItem.setContentDescription(LocaleController.getString(R.string.GetQRCode));
//            updateQrItemVisibility(false);
            if (ContactsController.getInstance(currentAccount).getPrivacyRules(PRIVACY_RULES_TYPE_ADDED_BY_PHONE) == null) {
                ContactsController.getInstance(currentAccount).loadPrivacySettings();
            }
        }
        // TODO: for own settings
//        if (imageUpdater != null && !myProfile) {
//            searchItem = menu.addItem(search_button, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
//
//                @Override
//                public Animator getCustomToggleTransition() {
//                    searchMode = !searchMode;
//                    if (!searchMode) {
//                        searchItem.clearFocusOnSearchView();
//                    }
//                    if (searchMode) {
//                        searchItem.getSearchField().setText("");
//                    }
//                    return searchExpandTransition(searchMode);
//                }
//
//                @Override
//                public void onTextChanged(EditText editText) {
//                    searchAdapter.search(editText.getText().toString().toLowerCase());
//                }
//            });
//            searchItem.setContentDescription(LocaleController.getString(R.string.SearchInSettings));
//            searchItem.setSearchFieldHint(LocaleController.getString(R.string.SearchInSettings));
//            sharedMediaLayout.getSearchItem().setVisibility(View.GONE);
//            if (sharedMediaLayout.getSearchOptionsItem() != null) {
//                sharedMediaLayout.getSearchOptionsItem().setVisibility(View.GONE);
//            }
//            if (sharedMediaLayout.getSaveItem() != null) {
//                sharedMediaLayout.getSaveItem().setVisibility(View.GONE);
//            }
//            if (expandPhoto) {
//                searchItem.setVisibility(View.GONE);
//            }
//        }

        if (myProfile) {
            editItem = menu.addItem(edit_profile, R.drawable.group_edit_profile);
            editItem.setContentDescription(LocaleController.getString(R.string.Edit));
        } else {
            editItem = menu.addItem(edit_channel, R.drawable.group_edit_profile);
            editItem.setContentDescription(LocaleController.getString(R.string.Edit));
        }
        otherItem = menu.addItem(10, R.drawable.ic_ab_other, resourcesProvider);
        ttlIconView = new ImageView(context);
        ttlIconView.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_actionBarDefaultIcon), PorterDuff.Mode.MULTIPLY));
        AndroidUtilities.updateViewVisibilityAnimated(ttlIconView, false, 0.8f, false);
        ttlIconView.setImageResource(R.drawable.msg_mini_autodelete_timer);
        otherItem.addView(ttlIconView, LayoutHelper.createFrame(12, 12, Gravity.CENTER_VERTICAL | Gravity.LEFT, 8, 2, 0, 0));
        otherItem.setContentDescription(LocaleController.getString(R.string.AccDescrMoreOptions));


        storyView = new ProfileStoriesView(context, currentAccount, getDialogId(), isTopic, avatarContainer, avatarImage, resourcesProvider) {
            @Override
            protected void onTap(StoryViewer.PlaceProvider provider) {
                long did = getDialogId();
                StoriesController storiesController = getMessagesController().getStoriesController();
                if (storiesController.hasStories(did) || storiesController.hasUploadingStories(did) || storiesController.isLastUploadingFailed(did)) {
                    getOrCreateStoryViewer().open(context, did, provider);
                } else if (userInfo != null && userInfo.stories != null && !userInfo.stories.stories.isEmpty() && userId != getUserConfig().clientUserId) {
                    getOrCreateStoryViewer().open(context, userInfo.stories, provider);
                } else if (chatInfo != null && chatInfo.stories != null && !chatInfo.stories.stories.isEmpty()) {
                    getOrCreateStoryViewer().open(context, chatInfo.stories, provider);
                } else {
                    // TODO
//                    expandAvatar();
                }
            }

            @Override
            protected void onLongPress() {
                // TODO
//                openAvatar();
            }
        };
        updateStoriesViewBounds(false);
        if (userInfo != null) {
            storyView.setStories(userInfo.stories);
        } else if (chatInfo != null) {
            storyView.setStories(chatInfo.stories);
        }
        if (avatarImage != null) {
            avatarImage.setHasStories(needInsetForStories());
        }
        contentView.addView(storyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        giftsView = new ProfileGiftsView(context, currentAccount, getDialogId(), avatarContainer, avatarImage, resourcesProvider);
        contentView.addView(giftsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));


        ViewGroup decorView;
        if (Build.VERSION.SDK_INT >= 21) {
            decorView = (ViewGroup) getParentActivity().getWindow().getDecorView();
        } else {
            decorView = contentView;
        }
        pinchToZoomHelper = new PinchToZoomHelper(decorView, contentView) {

            Paint statusBarPaint;

            @Override
            protected void invalidateViews() {
                super.invalidateViews();
                fragmentView.invalidate();
                for (int i = 0; i < avatarsViewPager.getChildCount(); i++) {
                    avatarsViewPager.getChildAt(i).invalidate();
                }
            }

            @Override
            protected void drawOverlays(Canvas canvas, float alpha, float parentOffsetX, float parentOffsetY, float clipTop, float clipBottom) {
                if (alpha > 0) {
                    AndroidUtilities.rectTmp.set(0, 0, avatarsViewPager.getMeasuredWidth(), avatarsViewPager.getMeasuredHeight() + AndroidUtilities.dp(30));
                    canvas.saveLayerAlpha(AndroidUtilities.rectTmp, (int) (255 * alpha), Canvas.ALL_SAVE_FLAG);

                    contentView.draw(canvas);

                    if (actionBar.getOccupyStatusBar() && !SharedConfig.noStatusBar) {
                        if (statusBarPaint == null) {
                            statusBarPaint = new Paint();
                            statusBarPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, (int) (255 * 0.2f)));
                        }
                        canvas.drawRect(actionBar.getX(), actionBar.getY(), actionBar.getX() + actionBar.getMeasuredWidth(), actionBar.getY() + AndroidUtilities.statusBarHeight, statusBarPaint);
                    }
                    canvas.save();
                    canvas.translate(actionBar.getX(), actionBar.getY());
                    actionBar.draw(canvas);
                    canvas.restore();

                    canvas.restore();
                }
            }

            @Override
            protected boolean zoomEnabled(View child, ImageReceiver receiver) {
                if (!super.zoomEnabled(child, receiver)) {
                    return false;
                }
                return listView.getScrollState() != RecyclerView.SCROLL_STATE_DRAGGING;
            }
        };
        pinchToZoomHelper.setCallback(new PinchToZoomHelper.Callback() {
            @Override
            public void onZoomStarted(MessageObject messageObject) {
                listView.cancelClickRunnables(true);
                if (sharedMediaLayout != null && sharedMediaLayout.getCurrentListView() != null) {
                    sharedMediaLayout.getCurrentListView().cancelClickRunnables(true);
                }
                topView.setBackgroundColor(ColorUtils.blendARGB(getAverageColor(pinchToZoomHelper.getPhotoImage()), getThemedColor(Theme.key_windowBackgroundWhite), 0.1f));
            }
        });
        avatarsViewPager.setPinchToZoomHelper(pinchToZoomHelper);
        scrimPaint.setAlpha(0);
        actionBarBackgroundPaint.setColor(getThemedColor(Theme.key_listSelector));
        contentView.blurBehindViews.add(sharedMediaLayout);
        updateTtlIcon();

        blurredView = new View(context) {
            @Override
            public void setAlpha(float alpha) {
                super.setAlpha(alpha);
                if (fragmentView != null) {
                    fragmentView.invalidate();
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            blurredView.setForeground(new ColorDrawable(ColorUtils.setAlphaComponent(getThemedColor(Theme.key_windowBackgroundWhite), 100)));
        }
        blurredView.setFocusable(false);
        blurredView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        blurredView.setOnClickListener(e -> {
            finishPreviewFragment();
        });
        blurredView.setVisibility(View.GONE);
        blurredView.setFitsSystemWindows(true);
        contentView.addView(blurredView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        showAvatarProgress(false, false);
        updateProfileData(true);

        layoutManager.scrollToPositionWithOffset(0, expandedOffset - maximizedOffset);

        return contentView;
    }

    private int getAverageColor(ImageReceiver imageReceiver) {
        if (imageReceiver.getDrawable() instanceof VectorAvatarThumbDrawable) {
            return ((VectorAvatarThumbDrawable) imageReceiver.getDrawable()).gradientTools.getAverageColor();
        }
        return AndroidUtilities.calcBitmapColor(avatarImage.getImageReceiver().getBitmap());
    }

    private void initText(Context context) {
        for (int a = 0; a < nameTextView.length; a++) {
            if (playProfileAnimation == 0 && a == 0) {
                continue;
            }
            nameTextView[a] = new SimpleTextView(context) {
                @Override
                public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(info);
                    if (isFocusable() && (nameTextViewRightDrawableContentDescription != null || nameTextViewRightDrawable2ContentDescription != null)) {
                        StringBuilder s = new StringBuilder(getText());
                        if (nameTextViewRightDrawable2ContentDescription != null) {
                            if (s.length() > 0) s.append(", ");
                            s.append(nameTextViewRightDrawable2ContentDescription);
                        }
                        if (nameTextViewRightDrawableContentDescription != null) {
                            if (s.length() > 0) s.append(", ");
                            s.append(nameTextViewRightDrawableContentDescription);
                        }
                        info.setText(s);
                    }
                }

                @Override
                protected void onDraw(Canvas canvas) {
                    final int wasRightDrawableX = getRightDrawableX();
                    super.onDraw(canvas);
                    if (wasRightDrawableX != getRightDrawableX()) {
                        updateCollectibleHint();
                    }
                }
            };
            if (a == 1) {
                nameTextView[a].setTextColor(getThemedColor(Theme.key_profile_title));
            } else {
                nameTextView[a].setTextColor(getThemedColor(Theme.key_actionBarDefaultTitle));
            }
            nameTextView[a].setWidthWrapContent(true);
            nameTextView[a].setTextSize(18);
            nameTextView[a].setGravity(Gravity.LEFT);
            nameTextView[a].setTypeface(AndroidUtilities.bold());
            nameTextView[a].setLeftDrawableTopPadding(-AndroidUtilities.dp(1.3f));
            nameTextView[a].setPivotX(0);
            nameTextView[a].setPivotY(0);
            nameTextView[a].setAlpha(a == 0 ? 0.0f : 1.0f);
            if (a == 1) {
                nameTextView[a].setScrollNonFitText(true);
                nameTextView[a].setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
            nameTextView[a].setFocusable(a == 0);
            nameTextView[a].setEllipsizeByGradient(true);
            nameTextView[a].setRightDrawableOutside(a == 0);
            contentView.addView(nameTextView[a], LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
        }
        for (int a = 0; a < onlineTextView.length; a++) {
            if (a == 1) {
                onlineTextView[a] = new LinkSpanDrawable.ClickableSmallTextView(context) {
                    @Override
                    public void setAlpha(float alpha) {
                        super.setAlpha(alpha);
                        checkPhotoDescriptionAlpha();
                    }

                    @Override
                    public void setTranslationY(float translationY) {
                        super.setTranslationY(translationY);
                        onlineTextView[2].setTranslationY(translationY);
                        onlineTextView[3].setTranslationY(translationY);
                    }

                    @Override
                    public void setTranslationX(float translationX) {
                        super.setTranslationX(translationX);
                        onlineTextView[2].setTranslationX(translationX);
                        onlineTextView[3].setTranslationX(translationX);
                    }

                    @Override
                    public void setTextColor(int color) {
                        super.setTextColor(color);
                        if (onlineTextView[2] != null) {
                            onlineTextView[2].setTextColor(color);
                            onlineTextView[3].setTextColor(color);
                        }
                        if (showStatusButton != null) {
                            showStatusButton.setTextColor(Theme.multAlpha(Theme.adaptHSV(color, -.02f, +.15f), 1.4f));
                        }
                    }
                };
            } else {
                onlineTextView[a] = new LinkSpanDrawable.ClickableSmallTextView(context);
            }

            onlineTextView[a].setEllipsizeByGradient(true);
            onlineTextView[a].setTextColor(applyPeerColor(getThemedColor(Theme.key_avatar_subtitleInProfileBlue), true, null));
            onlineTextView[a].setTextSize(14);
            onlineTextView[a].setGravity(Gravity.LEFT);
            onlineTextView[a].setAlpha(a == 0 ? 0.0f : 1.0f);
            if (a > 0) {
                onlineTextView[a].setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
            onlineTextView[a].setFocusable(a == 0);
            onlineTextView[a].setWidthWrapContent(true);
            contentView.addView(onlineTextView[a], LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));

            animatedStatusView = new DrawerProfileCell.AnimatedStatusView(context, 20, 60);
            animatedStatusView.setPivotX(AndroidUtilities.dp(30));
            animatedStatusView.setPivotY(AndroidUtilities.dp(30));
            contentView.addView(animatedStatusView);
        }

        mediaCounterTextView = new AudioPlayerAlert.ClippingTextViewSwitcher(context) {
            @Override
            protected TextView createTextView() {
                TextView textView = new TextView(context);
                textView.setTextColor(getThemedColor(Theme.key_player_actionBarSubtitle));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, AndroidUtilities.dp(14));
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setGravity(Gravity.LEFT);
                return textView;
            }
        };
        mediaCounterTextView.setAlpha(0.0f);
        contentView.addView(mediaCounterTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
    }

    private void checkPhotoDescriptionAlpha() {
        float p = photoDescriptionProgress;
        if (playProfileAnimation == 1 && (!fragmentOpened || openAnimationInProgress)) {
            photoDescriptionProgress = 0;
        } else if (playProfileAnimation == 2 && (!fragmentOpened || openAnimationInProgress)) {
            photoDescriptionProgress = onlineTextView[1] == null ? 0 : onlineTextView[1].getAlpha();
        } else {
            if (userId == UserConfig.getInstance(currentAccount).clientUserId) {
                photoDescriptionProgress = currentExpandAnimatorValue * (1f - customAvatarProgress);
            } else {
                photoDescriptionProgress = currentExpandAnimatorValue * customAvatarProgress;
            }
        }
        if (userId == UserConfig.getInstance(currentAccount).clientUserId) {
            if (hasFallbackPhoto) {
                customPhotoOffset = AndroidUtilities.dp(28) * photoDescriptionProgress;
                if (onlineTextView[2] != null) {
                    onlineTextView[2].setAlpha(currentExpandAnimatorValue);
                    onlineTextView[3].setAlpha(1f - currentExpandAnimatorValue);
                    //  onlineTextView[1].setAlpha(1f - expandProgress);
                    onlineTextView[1].setTranslationX(onlineX + customPhotoOffset);
                    contentView.invalidate();
                    if (showStatusButton != null) {
                        showStatusButton.setAlpha2(1f - currentExpandAnimatorValue);
                    }
                }
            } else {
                if (onlineTextView[2] != null) {
                    onlineTextView[2].setAlpha(0);
                    onlineTextView[3].setAlpha(0);
                }
                if (showStatusButton != null) {
                    showStatusButton.setAlpha2(1f);
                }
            }

        } else {
            if (hasCustomPhoto) {
                if (onlineTextView[2] != null) {
                    onlineTextView[2].setAlpha(photoDescriptionProgress);
                }
                if (showStatusButton != null) {
                    showStatusButton.setAlpha2(1f - photoDescriptionProgress);
                }
            } else {
                if (onlineTextView[2] != null) {
                    onlineTextView[2].setAlpha(0);
                }
                if (showStatusButton != null) {
                    showStatusButton.setAlpha2(1f);
                }
            }
        }
    }

    public void updateCollectibleHint() {
        if (collectibleHint == null) return;
        collectibleHint.setJointPx(0, -collectibleHint.getPaddingLeft() + nameTextView[1].getX() + (nameTextView[1].getRightDrawableX() - nameTextView[1].getRightDrawableWidth() * lerp(0.45f, 0.25f, currentExpandAnimatorValue)) * nameTextView[1].getScaleX());
        final float expanded = expandProgress;
        collectibleHint.setTranslationY(-collectibleHint.getPaddingBottom() + nameTextView[1].getY() - dp(24) + lerp(dp(6), -dp(12), expanded));
        collectibleHint.setBgColor(ColorUtils.blendARGB(collectibleHintBackgroundColor, 0x50000000, expanded));
        final boolean visible = topScroll > minimizedOffset;
        if (collectibleHintVisible == null || collectibleHintVisible != visible) {
            collectibleHint.animate().alpha((collectibleHintVisible = visible) ? 1.0f : 0.0f).setInterpolator(CubicBezierInterpolator.EASE_OUT).setDuration(200).start();
        }
    }

    public void setCollectibleGiftStatus(TLRPC.TL_emojiStatusCollectible status) {
        if (collectibleStatus == status) return;
        if (collectibleStatus != null && status != null && collectibleStatus.collectible_id == status.collectible_id)
            return;
        collectibleStatus = status;
        if (collectibleHint != null) {
            collectibleHint.hide();
        }
        if (status != null && !TextUtils.isEmpty(status.slug)) {
            collectibleHintVisible = null;
            collectibleHint = new HintView2(getContext(), HintView2.DIRECTION_BOTTOM);
            collectibleHintBackgroundColor = Theme.blendOver(status.center_color | 0xFF000000, Theme.multAlpha(status.pattern_color | 0xFF000000, .5f));
            collectibleHint.setPadding(dp(4), 0, dp(4), dp(2));
            collectibleHint.setFlicker(.66f, Theme.multAlpha(status.text_color | 0xFF000000, 0.5f));
            contentView.addView(collectibleHint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 24));
            collectibleHint.setTextSize(9.33f);
            collectibleHint.setTextTypeface(AndroidUtilities.bold());
            collectibleHint.setText(status.title);
            collectibleHint.setDuration(-1);
            collectibleHint.setInnerPadding(4.66f + 1, 2.66f, 4.66f + 1, 2.66f);
            collectibleHint.setArrowSize(4, 2.66f);
            collectibleHint.setRoundingWithCornerEffect(false);
            collectibleHint.setRounding(16);
            collectibleHint.show();
            final String slug = status.slug;
            collectibleHint.setOnClickListener(v -> {
                Browser.openUrl(getContext(), "https://" + getMessagesController().linkPrefix + "/nft/" + slug);
            });
            if (topScroll < minimizedOffset) {
                collectibleHintVisible = false;
                collectibleHint.setAlpha(0.0f);
            }
            updateCollectibleHint();
            AndroidUtilities.runOnUIThread(collectibleHint::hide, 6 * 1000);
        }
    }

    private void initHeaderButtons(Context context, FrameLayout frameLayout) {
        HeaderButtonView messageButton = new HeaderButtonView(context);
        messageButton.setTextAndIcon(LocaleController.getString(R.string.Message), R.drawable.message);
        messageButton.setOnClickListener(v -> {
            onWriteButtonClick();
        });

        HeaderButtonView muteButton = new HeaderButtonView(context);
        muteButton.setTextAndIcon(LocaleController.getString(R.string.Mute), R.drawable.mute);
        muteButton.setOnClickListener(v -> {
            Log.i(TAG, "initHeaderButtons: MUTE");
        });


        HeaderButtonView button3 = new HeaderButtonView(context);
        button3.setTextAndIcon(LocaleController.getString(R.string.Call), R.drawable.call);
        button3.setOnClickListener(v -> {
            startCall(false);
        });

        HeaderButtonView button4 = new HeaderButtonView(context);
        button4.setTextAndIcon(LocaleController.getString(R.string.Video), R.drawable.video);
        button4.setOnClickListener(v -> {
            startCall(true);
        });

        headerButtonLayout = new LinearLayout(getContext());
        headerButtonLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutHelper.WRAP_CONTENT, 1f);
        params.setMargins(AndroidUtilities.dp(10f / 3), 0, AndroidUtilities.dp(10f / 3), 0);
        headerButtonLayout.addView(messageButton, params);
        headerButtonLayout.addView(muteButton, params);
        headerButtonLayout.addView(button3, params);
        headerButtonLayout.addView(button4, params);
        frameLayout.addView(headerButtonLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 26 / 3f, 0f, 26 / 3f, 0f));
    }

    private void startCall(boolean isVideo) {
        if (userId != 0) {
            TLRPC.User user = getMessagesController().getUser(userId);
            if (user != null) {
                VoIPHelper.startCall(user, isVideo, userInfo != null && userInfo.video_calls_available, getParentActivity(), userInfo, getAccountInstance());
            }
        } else if (chatId != 0) {
            ChatObject.Call call = getMessagesController().getGroupCall(chatId, false);
            if (call == null) {
                VoIPHelper.showGroupCallAlert(DebugProfile.this, currentChat, null, false, getAccountInstance());
            } else {
                VoIPHelper.startCall(currentChat, null, null, false, getParentActivity(), DebugProfile.this, getAccountInstance());
            }
        }
    }

    private void updateStoriesViewBounds(boolean animated) {
        if (storyView == null && giftsView == null || actionBar == null) {
            return;
        }
        float atop = actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0;
        float aleft = 0;
        float aright = actionBar.getWidth();

        if (actionBar.getBackButton() != null) {
            aleft = Math.max(aleft, actionBar.getBackButton().getRight());
        }
        if (actionBar.menu != null) {
            for (int i = 0; i < actionBar.menu.getChildCount(); ++i) {
                View child = actionBar.menu.getChildAt(i);
                if (child.getAlpha() <= 0 || child.getVisibility() != View.VISIBLE) {
                    continue;
                }
                int left = actionBar.menu.getLeft() + (int) child.getX();
                if (left < aright) {
                    aright = AndroidUtilities.lerp(aright, left, child.getAlpha());
                }
            }
        }
        if (storyView != null) {
            storyView.setBounds(aleft, aright, atop + (actionBar.getHeight() - atop) / 2f, !animated);
        }
        if (giftsView != null) {
            giftsView.setBounds(aleft, aright, atop + (actionBar.getHeight() - atop) / 2f, !animated);
        }
    }

    private void setForegroundImage(boolean secondParent) {
        Drawable drawable = avatarImage.getImageReceiver().getDrawable();
        if (drawable instanceof VectorAvatarThumbDrawable) {
            avatarImage.setForegroundImage(null, null, drawable);
        } else if (drawable instanceof AnimatedFileDrawable) {
            AnimatedFileDrawable fileDrawable = (AnimatedFileDrawable) drawable;
            avatarImage.setForegroundImage(null, null, fileDrawable);
            if (secondParent) {
                fileDrawable.addSecondParentView(avatarImage);
            }
        } else {
            ImageLocation location = avatarsViewPager.getImageLocation(0);
            String filter;
            if (location != null && location.imageType == FileLoader.IMAGE_TYPE_ANIMATION) {
                filter = "avatar";
            } else {
                filter = null;
            }
            avatarImage.setForegroundImage(location, filter, drawable);
        }
    }

    private void openDiscussion() {
        if (chatInfo == null || chatInfo.linked_chat_id == 0) {
            return;
        }
        Bundle args = new Bundle();
        args.putLong("chat_id", chatInfo.linked_chat_id);
        if (!getMessagesController().checkCanOpenChat(args, DebugProfile.this)) {
            return;
        }
        presentFragment(new ChatActivity(args));
    }

    private void openAddMember() {
        Bundle args = new Bundle();
        args.putBoolean("addToGroup", true);
        args.putLong("chatId", currentChat.id);
        GroupCreateActivity fragment = new GroupCreateActivity(args);
        fragment.setInfo(chatInfo);
        if (chatInfo != null && chatInfo.participants != null) {
            LongSparseArray<TLObject> users = new LongSparseArray<>();
            for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                users.put(chatInfo.participants.participants.get(a).user_id, null);
            }
            fragment.setIgnoreUsers(users);
        }
        fragment.setDelegate2((users, fwdCount) -> {
            HashSet<Long> currentParticipants = new HashSet<>();
            ArrayList<TLRPC.User> addedUsers = new ArrayList<>();
            if (chatInfo != null && chatInfo.participants != null && chatInfo.participants.participants != null) {
                for (int i = 0; i < chatInfo.participants.participants.size(); i++) {
                    currentParticipants.add(chatInfo.participants.participants.get(i).user_id);
                }
            }
            getMessagesController().addUsersToChat(currentChat, DebugProfile.this, users, fwdCount, user -> {
                addedUsers.add(user);
            }, restrictedUser -> {
                for (int i = 0; i < chatInfo.participants.participants.size(); i++) {
                    if (chatInfo.participants.participants.get(i).user_id == restrictedUser.id) {
                        chatInfo.participants.participants.remove(i);
                        updateListAnimated(true);
                        break;
                    }
                }
            }, () -> {
                int N = addedUsers.size();
                int[] finished = new int[1];
                for (int a = 0; a < N; a++) {
                    TLRPC.User user = addedUsers.get(a);
                    if (!currentParticipants.contains(user.id)) {
                        if (chatInfo.participants == null) {
                            chatInfo.participants = new TLRPC.TL_chatParticipants();
                        }
                        if (ChatObject.isChannel(currentChat)) {
                            TLRPC.TL_chatChannelParticipant channelParticipant1 = new TLRPC.TL_chatChannelParticipant();
                            channelParticipant1.channelParticipant = new TLRPC.TL_channelParticipant();
                            channelParticipant1.channelParticipant.inviter_id = getUserConfig().getClientUserId();
                            channelParticipant1.channelParticipant.peer = new TLRPC.TL_peerUser();
                            channelParticipant1.channelParticipant.peer.user_id = user.id;
                            channelParticipant1.channelParticipant.date = getConnectionsManager().getCurrentTime();
                            channelParticipant1.user_id = user.id;
                            chatInfo.participants.participants.add(channelParticipant1);
                        } else {
                            TLRPC.ChatParticipant participant = new TLRPC.TL_chatParticipant();
                            participant.user_id = user.id;
                            participant.inviter_id = getAccountInstance().getUserConfig().clientUserId;
                            chatInfo.participants.participants.add(participant);
                        }
                        chatInfo.participants_count++;
                        getMessagesController().putUser(user, false);
                    }
                }
                updateListAnimated(true);
            });

        });
        presentFragment(fragment);
    }

    private void leaveChatPressed() {
        boolean isForum = ChatObject.isForum(currentChat);
        AlertsCreator.createClearOrDeleteDialogAlert(DebugProfile.this, false, currentChat, null, false, isForum, !isForum, (param) -> {
            playProfileAnimation = 0;
            getNotificationCenter().removeObserver(DebugProfile.this, NotificationCenter.closeChats);
            getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
            finishFragment();
            getNotificationCenter().postNotificationName(NotificationCenter.needDeleteDialog, -currentChat.id, null, currentChat, param);
        }, resourcesProvider);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openAddToContact(TLRPC.User user, Bundle args) {
        ContactAddActivity contactAddActivity = new ContactAddActivity(args, resourcesProvider);
        contactAddActivity.setDelegate(() -> {
            int currentAddToContactsRow = addToContactsRow;
            if (currentAddToContactsRow >= 0) {
                if (sharedMediaRow == -1) {
                    updateRowsIds();
                    listAdapter.notifyDataSetChanged();
                } else {
                    updateListAnimated(false);
                }
            }

            if (sharedMediaRow == -1) {
                if (isInLandscapeMode || AndroidUtilities.isTablet()) {
                    // TODO
//                    listView.setPadding(0, AndroidUtilities.dp(88), 0, 0);
//                    expandAnimator.cancel();
//                    expandAnimatorValues[0] = 1f;
//                    expandAnimatorValues[1] = 0f;
//                    setAvatarExpandProgress(1f);
//                    extraHeight = AndroidUtilities.dp(88);
                } else {
                    final int actionBarHeight = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
                    int ws = View.MeasureSpec.makeMeasureSpec(listView.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
                    int hs = View.MeasureSpec.makeMeasureSpec(listView.getMeasuredHeight(), View.MeasureSpec.UNSPECIFIED);
                    int contentHeight = 0;
                    for (int i = 0; i < listAdapter.getItemCount(); i++) {
                        RecyclerView.ViewHolder holder = listAdapter.createViewHolder(null, listAdapter.getItemViewType(i));
                        listAdapter.onBindViewHolder(holder, i);
                        holder.itemView.measure(ws, hs);
                        contentHeight += holder.itemView.getMeasuredHeight();
                    }
                    int paddingBottom = Math.max(0, fragmentView.getMeasuredHeight() - (contentHeight + AndroidUtilities.dp(88) + actionBarHeight));
                    listView.setPadding(0, listView.getPaddingTop(), 0, paddingBottom);
                }
            }
            undoView.showWithAction(dialogId, UndoView.ACTION_CONTACT_ADDED, user);
        });
        presentFragment(contactAddActivity);
    }

//    private void createFloatingActionButton(Context context) {
//        if (!getMessagesController().storiesEnabled()) {
//            return;
//        }
//        if (getDialogId() > 0L) {
//            return;
//        }
//        TLRPC.Chat currentChat = MessagesController.getInstance(currentAccount).getChat(chatId);
//        if (!ChatObject.isBoostSupported(currentChat)) {
//            return;
//        }
//        StoriesController storiesController = getMessagesController().getStoriesController();
//        if (!storiesController.canPostStories(getDialogId())) {
//            return;
//        } else {
//            checkCanSendStoryForPosting();
//        }
//        long dialogId = getDialogId();
//        floatingButtonContainer = new FrameLayout(context);
//        floatingButtonContainer.setVisibility(View.VISIBLE);
//        contentView.addView(floatingButtonContainer, LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60), (Build.VERSION.SDK_INT >= 21 ? 56 : 60), (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));
//        floatingButtonContainer.setOnClickListener(v -> {
//            if (showBoostsAlert) {
//                if (loadingBoostsStats) {
//                    return;
//                }
//                MessagesController messagesController = MessagesController.getInstance(currentAccount);
//                loadingBoostsStats = true;
//                messagesController.getBoostsController().getBoostsStats(dialogId, boostsStatus -> {
//                    loadingBoostsStats = false;
//                    if (boostsStatus == null) {
//                        return;
//                    }
//                    messagesController.getBoostsController().userCanBoostChannel(dialogId, boostsStatus, canApplyBoost -> {
//                        if (canApplyBoost == null) {
//                            return;
//                        }
//                        BaseFragment lastFragment = LaunchActivity.getLastFragment();
//                        LimitReachedBottomSheet.openBoostsForPostingStories(lastFragment, dialogId, canApplyBoost, boostsStatus, () -> {
//                            TLRPC.Chat chat = getMessagesController().getChat(chatId);
//                            presentFragment(StatisticActivity.create(chat));
//                        });
//                    });
//                });
//                return;
//            }
//            StoryRecorder recorder = StoryRecorder.getInstance(getParentActivity(), currentAccount)
//                    .selectedPeerId(getDialogId())
//                    .canChangePeer(false)
//                    .closeToWhenSent(new StoryRecorder.ClosingViewProvider() {
//                        @Override
//                        public void preLayout(long dialogId, Runnable runnable) {
//                            avatarImage.setHasStories(needInsetForStories());
//                            if (dialogId == getDialogId()) {
//                                collapseAvatarInstant();
//                            }
//                            AndroidUtilities.runOnUIThread(runnable, 30);
//                        }
//
//                        @Override
//                        public StoryRecorder.SourceView getView(long dialogId) {
//                            if (dialogId != getDialogId()) {
//                                return null;
//                            }
//                            updateAvatarRoundRadius();
//                            return StoryRecorder.SourceView.fromAvatarImage(avatarImage, ChatObject.isForum(currentChat));
//                        }
//                    });
//            recorder.open(StoryRecorder.SourceView.fromFloatingButton(floatingButtonContainer), true);
//        });
//
//        floatingButton = new RLottieImageView(context);
//        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
//        floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.MULTIPLY));
//        if (Build.VERSION.SDK_INT >= 21) {
//            StateListAnimator animator = new StateListAnimator();
//            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButtonContainer, View.TRANSLATION_Z, AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
//            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButtonContainer, View.TRANSLATION_Z, AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
//            floatingButtonContainer.setStateListAnimator(animator);
//            floatingButtonContainer.setOutlineProvider(new ViewOutlineProvider() {
//                @SuppressLint("NewApi")
//                @Override
//                public void getOutline(View view, Outline outline) {
//                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
//                }
//            });
//        }
//        floatingButtonContainer.addView(floatingButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//        floatingButton.setAnimation(R.raw.write_contacts_fab_icon_camera, 56, 56);
//        floatingButtonContainer.setContentDescription(LocaleController.getString(R.string.AccDescrCaptureStory));
//        updateFloatingButtonColor();
//
//        floatingHidden = true;
//        floatingButtonHideProgress = 1.0f;
//        updateFloatingButtonOffset();
//    }

    private void updateAvatarRoundRadius() {
        // TODO
//        avatarImage.setRoundRadius((int) AndroidUtilities.lerp(getSmallAvatarRoundRadius(), 0f, currentExpandAnimatorValue));
    }

    private void collapseAvatarInstant() {
        // TODO
//        if (allowPullingDown && currentExpandAnimatorValue > 0) {
//            layoutManager.scrollToPositionWithOffset(0, AndroidUtilities.dp(88) - listView.getPaddingTop());
//            listView.post(() -> {
//                needLayout(true);
//                if (expandAnimator.isRunning()) {
//                    expandAnimator.cancel();
//                }
//                setAvatarExpandProgress(1f);
//            });
//        }
    }

    private void updateBottomButtonY() {
        if (bottomButtonsContainer == null) {
            return;
        }
        bottomButtonsContainer.setTranslationY(sharedMediaLayout != null && sharedMediaLayout.isAttachedToWindow() ? Math.max(0, dp(72 + 64 + 48) - (listView.getMeasuredHeight() - sharedMediaLayout.getY())) : dp(72));
        final Bulletin bulletin = Bulletin.getVisibleBulletin();
        if (bulletin != null) {
            bulletin.updatePosition();
        }
    }

    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, View view) {
        return onMemberClick(participant, isLong, false, view);
    }

    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, boolean resultOnly) {
        return onMemberClick(participant, isLong, resultOnly, null);
    }

    @Override
    public void scrollToSharedMedia() {
        scrollToSharedMedia(false);
    }

    public void scrollToSharedMedia(boolean animated) {
        if (sharedMediaRow >= 0) {
            if (animated) {
                LinearSmoothScrollerCustom linearSmoothScroller = new LinearSmoothScrollerCustom(getContext(), LinearSmoothScrollerCustom.POSITION_TOP, .6f);
                linearSmoothScroller.setTargetPosition(sharedMediaRow);
                linearSmoothScroller.setOffset(-listView.getPaddingTop());
                layoutManager.startSmoothScroll(linearSmoothScroller);
            } else {
                layoutManager.scrollToPositionWithOffset(sharedMediaRow, -listView.getPaddingTop());
            }
        }
    }

    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, boolean resultOnly, View view) {
        if (getParentActivity() == null) {
            return false;
        }
        if (isLong) {
            TLRPC.User user = getMessagesController().getUser(participant.user_id);
            if (user == null || participant.user_id == getUserConfig().getClientUserId()) {
                return false;
            }
            long selectedUser = participant.user_id;
            boolean allowKick;
            boolean canEditAdmin;
            boolean canRestrict;
            boolean editingAdmin;
            final TLRPC.ChannelParticipant channelParticipant;

            if (ChatObject.isChannel(currentChat)) {
                channelParticipant = ((TLRPC.TL_chatChannelParticipant) participant).channelParticipant;
                TLRPC.User u = getMessagesController().getUser(participant.user_id);
                canEditAdmin = ChatObject.canAddAdmins(currentChat);
                if (canEditAdmin && (channelParticipant instanceof TLRPC.TL_channelParticipantCreator || channelParticipant instanceof TLRPC.TL_channelParticipantAdmin && !channelParticipant.can_edit)) {
                    canEditAdmin = false;
                }
                allowKick = canRestrict = ChatObject.canBlockUsers(currentChat) && (!(channelParticipant instanceof TLRPC.TL_channelParticipantAdmin || channelParticipant instanceof TLRPC.TL_channelParticipantCreator) || channelParticipant.can_edit);
                if (currentChat.gigagroup) {
                    canRestrict = false;
                }
                editingAdmin = channelParticipant instanceof TLRPC.TL_channelParticipantAdmin;
            } else {
                channelParticipant = null;
                allowKick = currentChat.creator || participant instanceof TLRPC.TL_chatParticipant && (ChatObject.canBlockUsers(currentChat) || participant.inviter_id == getUserConfig().getClientUserId());
                canEditAdmin = currentChat.creator;
                canRestrict = currentChat.creator;
                editingAdmin = participant instanceof TLRPC.TL_chatParticipantAdmin;
            }

            boolean result = (canEditAdmin || canRestrict || allowKick);
            if (resultOnly || !result) {
                return result;
            }

            Utilities.Callback<Integer> openRightsEdit = action -> {
                if (channelParticipant != null) {
                    openRightsEdit(action, user, participant, channelParticipant.admin_rights, channelParticipant.banned_rights, channelParticipant.rank, editingAdmin);
                } else {
                    openRightsEdit(action, user, participant, null, null, "", editingAdmin);
                }
            };

            ItemOptions.makeOptions(this, view).setScrimViewBackground(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundWhite))).addIf(canEditAdmin, R.drawable.msg_admins, editingAdmin ? LocaleController.getString(R.string.EditAdminRights) : LocaleController.getString(R.string.SetAsAdmin), () -> openRightsEdit.run(0)).addIf(canRestrict, R.drawable.msg_permissions, LocaleController.getString(R.string.ChangePermissions), () -> {
                if (channelParticipant instanceof TLRPC.TL_channelParticipantAdmin || participant instanceof TLRPC.TL_chatParticipantAdmin) {
                    showDialog(new AlertDialog.Builder(getParentActivity(), resourcesProvider).setTitle(LocaleController.getString(R.string.AppName)).setMessage(formatString("AdminWillBeRemoved", R.string.AdminWillBeRemoved, ContactsController.formatName(user.first_name, user.last_name))).setPositiveButton(LocaleController.getString(R.string.OK), (dialog, which) -> openRightsEdit.run(1)).setNegativeButton(LocaleController.getString(R.string.Cancel), null).create());
                } else {
                    openRightsEdit.run(1);
                }
            }).addIf(allowKick, R.drawable.msg_remove, LocaleController.getString(R.string.KickFromGroup), true, () -> {
                kickUser(selectedUser, participant);
            }).setMinWidth(190).show();
        } else {
            if (participant.user_id == getUserConfig().getClientUserId()) {
                return false;
            }
            Bundle args = new Bundle();
            args.putLong("user_id", participant.user_id);
            args.putBoolean("preload_messages", true);
            presentFragment(new DebugProfile(args));
        }
        return true;
    }

    private void kickUser(long uid, TLRPC.ChatParticipant participant) {
        if (uid != 0) {
            TLRPC.User user = getMessagesController().getUser(uid);
            getMessagesController().deleteParticipantFromChat(chatId, user);
            if (currentChat != null && user != null && BulletinFactory.canShowBulletin(this)) {
                BulletinFactory.createRemoveFromChatBulletin(this, user, currentChat.title).show();
            }
            if (chatInfo.participants.participants.remove(participant)) {
//                updateListAnimated(true);
                // TODO
            }
        } else {
            getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
            if (AndroidUtilities.isTablet()) {
                getNotificationCenter().postNotificationName(NotificationCenter.closeChats, -chatId);
            } else {
                getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
            }
            getMessagesController().deleteParticipantFromChat(chatId, getMessagesController().getUser(getUserConfig().getClientUserId()));
            playProfileAnimation = 0;
            finishFragment();
        }
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
    public int getBirthdayRow() {
        return birthdayRow;
    }

    @Override
    public boolean canSearchMembers() {
        return false;
    }

    private void openRightsEdit(int action, TLRPC.User user, TLRPC.ChatParticipant participant, TLRPC.TL_chatAdminRights adminRights, TLRPC.TL_chatBannedRights bannedRights, String rank, boolean editingAdmin) {
        boolean[] needShowBulletin = new boolean[1];
        ChatRightsEditActivity fragment = new ChatRightsEditActivity(user.id, chatId, adminRights, currentChat.default_banned_rights, bannedRights, rank, action, true, false, null) {
            @Override
            public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
                if (!isOpen && backward && needShowBulletin[0] && BulletinFactory.canShowBulletin(this)) {
                    BulletinFactory.createPromoteToAdminBulletin(this, user.first_name).show();
                }
            }
        };
        fragment.setDelegate(new ChatRightsEditActivity.ChatRightsEditActivityDelegate() {
            @Override
            public void didSetRights(int rights, TLRPC.TL_chatAdminRights rightsAdmin, TLRPC.TL_chatBannedRights rightsBanned, String rank) {
                if (action == 0) {
                    if (participant instanceof TLRPC.TL_chatChannelParticipant) {
                        TLRPC.TL_chatChannelParticipant channelParticipant1 = ((TLRPC.TL_chatChannelParticipant) participant);
                        if (rights == 1) {
                            channelParticipant1.channelParticipant = new TLRPC.TL_channelParticipantAdmin();
                            channelParticipant1.channelParticipant.flags |= 4;
                        } else {
                            channelParticipant1.channelParticipant = new TLRPC.TL_channelParticipant();
                        }
                        channelParticipant1.channelParticipant.inviter_id = getUserConfig().getClientUserId();
                        channelParticipant1.channelParticipant.peer = new TLRPC.TL_peerUser();
                        channelParticipant1.channelParticipant.peer.user_id = participant.user_id;
                        channelParticipant1.channelParticipant.date = participant.date;
                        channelParticipant1.channelParticipant.banned_rights = rightsBanned;
                        channelParticipant1.channelParticipant.admin_rights = rightsAdmin;
                        channelParticipant1.channelParticipant.rank = rank;
                    } else if (participant != null) {
                        TLRPC.ChatParticipant newParticipant;
                        if (rights == 1) {
                            newParticipant = new TLRPC.TL_chatParticipantAdmin();
                        } else {
                            newParticipant = new TLRPC.TL_chatParticipant();
                        }
                        newParticipant.user_id = participant.user_id;
                        newParticipant.date = participant.date;
                        newParticipant.inviter_id = participant.inviter_id;
                        int index = chatInfo.participants.participants.indexOf(participant);
                        if (index >= 0) {
                            chatInfo.participants.participants.set(index, newParticipant);
                        }
                    }
                    if (rights == 1 && !editingAdmin) {
                        needShowBulletin[0] = true;
                    }
                } else if (action == 1) {
                    if (rights == 0) {
                        if (currentChat.megagroup && chatInfo != null && chatInfo.participants != null) {
                            boolean changed = false;
                            for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                                TLRPC.ChannelParticipant p = ((TLRPC.TL_chatChannelParticipant) chatInfo.participants.participants.get(a)).channelParticipant;
                                if (MessageObject.getPeerId(p.peer) == participant.user_id) {
                                    chatInfo.participants_count--;
                                    chatInfo.participants.participants.remove(a);
                                    changed = true;
                                    break;
                                }
                            }
                            if (chatInfo != null && chatInfo.participants != null) {
                                for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                                    TLRPC.ChatParticipant p = chatInfo.participants.participants.get(a);
                                    if (p.user_id == participant.user_id) {
                                        chatInfo.participants.participants.remove(a);
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                            if (changed) {
                                updateOnlineCount(true);
                                updateRowsIds();
                                listAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }

            @Override
            public void didChangeOwner(TLRPC.User user) {
                undoView.showWithAction(-chatId, currentChat.megagroup ? UndoView.ACTION_OWNER_TRANSFERED_GROUP : UndoView.ACTION_OWNER_TRANSFERED_CHANNEL, user);
            }
        });
        presentFragment(fragment);
    }

    public void updateSelectedMediaTabText() {
        if (sharedMediaLayout == null || mediaCounterTextView == null) {
            return;
        }
        int id = sharedMediaLayout.getClosestTab();
        int[] mediaCount = sharedMediaPreloader.getLastMediaCount();
        if (id == SharedMediaLayout.TAB_PHOTOVIDEO) {
            if (mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY] <= 0 && mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY] <= 0) {
                if (mediaCount[MediaDataController.MEDIA_PHOTOVIDEO] <= 0) {
                    mediaCounterTextView.setText(LocaleController.getString(R.string.SharedMedia));
                } else {
                    mediaCounterTextView.setText(LocaleController.formatPluralString("Media", mediaCount[MediaDataController.MEDIA_PHOTOVIDEO]));
                }
            } else if (sharedMediaLayout.getPhotosVideosTypeFilter() == SharedMediaLayout.FILTER_PHOTOS_ONLY || mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY] <= 0) {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Photos", mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY]));
            } else if (sharedMediaLayout.getPhotosVideosTypeFilter() == SharedMediaLayout.FILTER_VIDEOS_ONLY || mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY] <= 0) {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Videos", mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY]));
            } else {
                String str = String.format("%s, %s", LocaleController.formatPluralString("Photos", mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY]), LocaleController.formatPluralString("Videos", mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY]));
                mediaCounterTextView.setText(str);
            }
        } else if (id == SharedMediaLayout.TAB_FILES) {
            if (mediaCount[MediaDataController.MEDIA_FILE] <= 0) {
                mediaCounterTextView.setText(LocaleController.getString(R.string.Files));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Files", mediaCount[MediaDataController.MEDIA_FILE]));
            }
        } else if (id == SharedMediaLayout.TAB_VOICE) {
            if (mediaCount[MediaDataController.MEDIA_AUDIO] <= 0) {
                mediaCounterTextView.setText(LocaleController.getString(R.string.Voice));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Voice", mediaCount[MediaDataController.MEDIA_AUDIO]));
            }
        } else if (id == SharedMediaLayout.TAB_LINKS) {
            if (mediaCount[MediaDataController.MEDIA_URL] <= 0) {
                mediaCounterTextView.setText(LocaleController.getString(R.string.SharedLinks));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Links", mediaCount[MediaDataController.MEDIA_URL]));
            }
        } else if (id == SharedMediaLayout.TAB_AUDIO) {
            if (mediaCount[MediaDataController.MEDIA_MUSIC] <= 0) {
                mediaCounterTextView.setText(LocaleController.getString(R.string.Music));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("MusicFiles", mediaCount[MediaDataController.MEDIA_MUSIC]));
            }
        } else if (id == SharedMediaLayout.TAB_GIF) {
            if (mediaCount[MediaDataController.MEDIA_GIF] <= 0) {
                mediaCounterTextView.setText(LocaleController.getString(R.string.AccDescrGIFs));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("GIFs", mediaCount[MediaDataController.MEDIA_GIF]));
            }
        } else if (id == SharedMediaLayout.TAB_COMMON_GROUPS) {
            mediaCounterTextView.setText(LocaleController.formatPluralString("CommonGroups", userInfo.common_chats_count));
        } else if (id == SharedMediaLayout.TAB_GROUPUSERS) {
            mediaCounterTextView.setText(onlineTextView[1].getText());
        } else if (id == SharedMediaLayout.TAB_STORIES) {
            if (isBot) {
                mediaCounterTextView.setText(sharedMediaLayout.getBotPreviewsSubtitle(false));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("ProfileStoriesCount", sharedMediaLayout.getStoriesCount(id)));
            }
        } else if (id == SharedMediaLayout.TAB_BOT_PREVIEWS) {
            mediaCounterTextView.setText(sharedMediaLayout.getBotPreviewsSubtitle(true));
        } else if (id == SharedMediaLayout.TAB_ARCHIVED_STORIES) {
            mediaCounterTextView.setText(LocaleController.formatPluralString("ProfileStoriesArchiveCount", sharedMediaLayout.getStoriesCount(id)));
        } else if (id == SharedMediaLayout.TAB_RECOMMENDED_CHANNELS) {
            final MessagesController.ChannelRecommendations rec = MessagesController.getInstance(currentAccount).getChannelRecommendations(getDialogId());
            mediaCounterTextView.setText(LocaleController.formatPluralString(isBot ? "Bots" : "Channels", rec == null ? 0 : rec.chats.size() + rec.more));
        } else if (id == SharedMediaLayout.TAB_SAVED_MESSAGES) {
            int messagesCount = getMessagesController().getSavedMessagesController().getMessagesCount(getDialogId());
            mediaCounterTextView.setText(LocaleController.formatPluralString("SavedMessagesCount", Math.max(1, messagesCount)));
        } else if (id == SharedMediaLayout.TAB_GIFTS) {
            mediaCounterTextView.setText(LocaleController.formatPluralStringComma("ProfileGiftsCount", sharedMediaLayout.giftsContainer == null ? 0 : sharedMediaLayout.giftsContainer.getGiftsCount()));
        }
    }

    private int dontApplyPeerColor(int color) {
        return dontApplyPeerColor(color, true, null);
    }

    private int dontApplyPeerColor(int color, boolean actionBar) {
        return dontApplyPeerColor(color, actionBar, null);
    }

    private int dontApplyPeerColor(int color, boolean actionBar, Boolean online) {
        return color;
    }

    private boolean needInsetForStories() {
        return getMessagesController().getStoriesController().hasStories(getDialogId()) && !isTopic;
    }

    public long getDialogId() {
        if (dialogId != 0) {
            return dialogId;
        } else if (userId != 0) {
            return userId;
        } else {
            return -chatId;
        }
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

        float breakpoint1 = 0.2f;
        float breakpoint2 = 0.55f;
        float breakpoint3 = 0.7f;

        float scale1 = 0.3f;
        float scale2 = 0.8f;

        if (topScroll < expandedOffset) {
            if (expandProgress < breakpoint1) {
                avatarScale = scale1;
            } else if (expandProgress < breakpoint2) {
                avatarScale = lerp(scale1, scale2, (expandProgress - breakpoint1) / (breakpoint2 - breakpoint1));
            } else if (expandProgress < breakpoint3) {
                avatarScale = scale2;
            } else {
                avatarScale = lerp(scale2, 1f, (expandProgress - breakpoint3) / (1f - breakpoint3));
            }
            avatarOffsetY = lerp(-scale1 * dp(AVATAR_SIZE_DP), dp(38), expandProgress);
            offsetX = (displaySize.x - dp(AVATAR_SIZE_DP) * avatarScale) / 2f;
            alpha = clamp01((expandProgress - 0.3f) / (0.5f - 0.3f));
        } else {
            float pulldownProgress = clamp01(maximizeProgress / AVATAR_EXPAND_THRESHOLD);
            avatarScale = lerp(1f, 1.1f, pulldownProgress);
            avatarOffsetY = lerp(dp(38), dp(74), pulldownProgress);
            offsetX = (displaySize.x - dp(AVATAR_SIZE_DP) * avatarScale) / 2f;
        }
        if (maximizeProgress > AVATAR_EXPAND_THRESHOLD) {
            avatarScale = 1.1f;
            avatarContainer.setScaleX(avatarScale);
            avatarContainer.setScaleY(avatarScale);


            final ViewGroup.LayoutParams overlaysLp = overlaysView.getLayoutParams();
            overlaysLp.width = listView.getMeasuredWidth();
            overlaysLp.height = topScroll;
            overlaysView.requestLayout();

            if (!avatarMaximizeAnimator.isRunning()) {
                ViewGroup.LayoutParams params = avatarsViewPager.getLayoutParams();
                params.width = listView.getMeasuredWidth();
                params.height = topScroll;
                avatarsViewPager.requestLayout();

                final FrameLayout.LayoutParams avatarParams = (FrameLayout.LayoutParams) avatarContainer.getLayoutParams();
                avatarParams.width = listView.getMeasuredWidth();
                avatarParams.height = (int) (topScroll / 1.1f);
                avatarContainer.requestLayout();
            }
            return;
        }
        avatarContainer.setTranslationX(offsetX);
        avatarContainer.setTranslationY(avatarOffsetY);
        avatarContainer.setScaleX(avatarScale);
        avatarContainer.setScaleY(avatarScale);
        avatarContainer.setAlpha(alpha);
        avatarImage.setBlurAlpha(clamp01((1f - alpha) * 2));
        avatarImage.invalidate();
    }

    private void setAvatarMaximizeAnimationProgress(float animatedFraction) {
        float progress = isPulledDown ? animatedFraction : 1f - animatedFraction;
        currentExpandAnimatorValue = progress;
        updateAvatar();

        float scaleX = lerp(avatarScale, (float) displaySize.x / dp(AVATAR_SIZE_DP), progress);
        float scaleY = lerp(avatarScale, (float) maximizedOffset / dp(AVATAR_SIZE_DP), progress);
        float offsetY = lerp(avatarOffsetY, 0, progress);
        float offsetX = (displaySize.x - dp(AVATAR_SIZE_DP) * scaleX) / 2f;
        int roundRadius = lerp(dp(AVATAR_SIZE_DP / 2), 0, progress);
        avatarContainer.setTranslationX(offsetX);
        avatarContainer.setTranslationY(offsetY);
        avatarImage.setRoundRadius(roundRadius);
        avatarImage.setBottomBlurPadding(lerp(0, maximizedOffset - listView.getMeasuredWidth(), progress));


        final FrameLayout.LayoutParams avatarParams = (FrameLayout.LayoutParams) avatarContainer.getLayoutParams();
        avatarParams.width = (int) lerp(dpf2(AVATAR_SIZE_DP), (float) displaySize.x / 1.1f, progress);
        avatarParams.height = (int) lerp(dpf2(AVATAR_SIZE_DP), (float) topScroll / 1.1f, progress);
        avatarContainer.requestLayout();


        if (isPulledDown) {
            ViewGroup.LayoutParams params = avatarsViewPager.getLayoutParams();
            params.width = listView.getMeasuredWidth();
            params.height = (int) (progress * topScroll);
            avatarsViewPager.requestLayout();
        }
    }

    void startMaximizeAnimator() {
        listView.smoothScrollBy(0, 0);
//        if (avatarMaximizeAnimator.isRunning()) {
//            avatarMaximizeAnimator.cancel();
//        }
        avatarMaximizeAnimator.start();
    }

    @SuppressLint("SetTextI18n")
    private void onScroll() {

        if (sharedMediaLayoutAttached) {
            sharedMediaLayout.setVisibleHeight(listView.getMeasuredHeight() - sharedMediaLayout.getTop());
            ViewGroup.LayoutParams params
                    = sharedMediaLayout.getLayoutParams();
            params.height = listView.getMeasuredHeight() - topBarsHeight;
        }

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
                    if (avatarMaximizeAnimator.isRunning()) {
                        avatarMaximizeAnimator.cancel();
                    }
                    isPulledDown = true;
                    startMaximizeAnimator();
                    // TODO duration
                    avatarsViewPagerIndicatorView.refreshVisibility(300);
                    overlaysView.setOverlaysVisible(true, 300);
                }
            } else {
                if (isPulledDown) {
                    if (avatarMaximizeAnimator.isRunning()) {
                        avatarMaximizeAnimator.cancel();
                    }
                    isPulledDown = false;
                    startMaximizeAnimator();
                    avatarsViewPagerIndicatorView.refreshVisibility(300);
                    overlaysView.setOverlaysVisible(false, 300);
                }
            }
        } else {
            debugText.setText("maximized");
            debug += "maximized";
        }
        debugText.setText(debug);
        updateAvatar();
        updateHeaderButtons();
        updateMediaHeaderVisible();
        updateText();
    }

    private void updateText() {
        final float titleX = 72;
        final float titleY = 32;
        final float subtitleY = 55;

        if (topScroll <= minimizedOffset) {
            nameTextView[1].setTranslationX(dp(titleX));
            nameTextView[1].setTranslationY(dp(titleY));
            onlineTextView[1].setTranslationX(dp(titleX));
            onlineTextView[1].setTranslationY(dp(subtitleY));
            mediaCounterTextView.setTranslationX(dp(titleX));
            mediaCounterTextView.setTranslationY(dp(subtitleY));
        } else if (topScroll <= expandedOffset) {
            nameTextView[1].setTranslationX(lerp(dp(titleX), displaySize.x / 2f - nameTextView[1].getWidth() / 2f, expandProgress));
            nameTextView[1].setTranslationY(lerp(dp(titleY), dp(140), expandProgress));
            onlineTextView[1].setTranslationX(lerp(dp(titleX), displaySize.x / 2f - onlineTextView[1].getWidth() / 2f, expandProgress));
            onlineTextView[1].setTranslationY(lerp(dp(subtitleY), dp(158), expandProgress));
        } else {
            nameTextView[1].setTranslationX(lerp(displaySize.x / 2f - nameTextView[1].getWidth() / 2f, dp(titleX), maximizeProgress));
            nameTextView[1].setTranslationY(lerp(dp(140), dp(300), maximizeProgress));
            onlineTextView[1].setTranslationX(lerp(displaySize.x / 2f - onlineTextView[1].getWidth() / 2f, dp(titleX), maximizeProgress));
            onlineTextView[1].setTranslationY(lerp(dp(158), dp(318), maximizeProgress));
        }
    }

    void updateMediaHeaderVisible() {
        boolean mediaHeaderVisible;
        boolean searchVisible = imageUpdater == null && actionBar.isSearchFieldVisible();
        if (sharedMediaRow != -1 && !searchVisible) {
            RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findViewHolderForAdapterPosition(sharedMediaRow);
            mediaHeaderVisible = holder != null && holder.itemView.getTop() <= topBarsHeight;
        } else {
            mediaHeaderVisible = searchVisible;
        }
        setMediaHeaderVisible(mediaHeaderVisible);
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

    private void updateRowsIds() {
        int prevRowsCount = rowCount;
        rowCount = 0;

        setAvatarRow = -1;
        setAvatarSectionRow = -1;
        numberSectionRow = -1;
        numberRow = -1;
        birthdayRow = -1;
        setUsernameRow = -1;
        bioRow = -1;
        channelRow = -1;
        channelDividerRow = -1;
        phoneSuggestionSectionRow = -1;
        phoneSuggestionRow = -1;
        passwordSuggestionSectionRow = -1;
        graceSuggestionRow = -1;
        graceSuggestionSectionRow = -1;
        passwordSuggestionRow = -1;
        settingsSectionRow = -1;
        settingsSectionRow2 = -1;
        notificationRow = -1;
        languageRow = -1;
        premiumRow = -1;
        starsRow = -1;
        businessRow = -1;
        premiumGiftingRow = -1;
        premiumSectionsRow = -1;
        privacyRow = -1;
        dataRow = -1;
        chatRow = -1;
        filtersRow = -1;
        liteModeRow = -1;
        stickersRow = -1;
        devicesRow = -1;
        devicesSectionRow = -1;
        helpHeaderRow = -1;
        questionRow = -1;
        faqRow = -1;
        policyRow = -1;
        helpSectionCell = -1;
        debugHeaderRow = -1;
        sendLogsRow = -1;
        sendLastLogsRow = -1;
        clearLogsRow = -1;
        switchBackendRow = -1;
        versionRow = -1;
        botAppRow = -1;
        botPermissionsHeader = -1;
        botPermissionBiometry = -1;
        botPermissionEmojiStatus = -1;
        botPermissionLocation = -1;
        botPermissionsDivider = -1;

        sendMessageRow = -1;
        reportRow = -1;
        reportReactionRow = -1;
        addToContactsRow = -1;
        emptyRow = -1;
        infoHeaderRow = -1;
        phoneRow = -1;
        userInfoRow = -1;
        locationRow = -1;
        channelInfoRow = -1;
        usernameRow = -1;
        settingsTimerRow = -1;
        settingsKeyRow = -1;
        notificationsDividerRow = -1;
        reportDividerRow = -1;
        notificationsRow = -1;
        bizLocationRow = -1;
        bizHoursRow = -1;
        infoSectionRow = -1;
        affiliateRow = -1;
        infoAffiliateRow = -1;
        secretSettingsSectionRow = -1;
        bottomPaddingRow = -1;
        addToGroupButtonRow = -1;
        addToGroupInfoRow = -1;
        infoStartRow = -1;
        infoEndRow = -1;

        membersHeaderRow = -1;
        membersStartRow = -1;
        membersEndRow = -1;
        addMemberRow = -1;
        subscribersRow = -1;
        subscribersRequestsRow = -1;
        administratorsRow = -1;
        blockedUsersRow = -1;
        membersSectionRow = -1;
        channelBalanceSectionRow = -1;
        sharedMediaRow = -1;
        notificationsSimpleRow = -1;
        settingsRow = -1;
        botStarsBalanceRow = -1;
        botTonBalanceRow = -1;
        channelBalanceRow = -1;
        balanceDividerRow = -1;

        unblockRow = -1;
        joinRow = -1;
        lastSectionRow = -1;
        visibleChatParticipants.clear();
        visibleSortedUsers.clear();

        boolean hasMedia = false;
        if (sharedMediaPreloader != null) {
            int[] lastMediaCount = sharedMediaPreloader.getLastMediaCount();
            for (int a = 0; a < lastMediaCount.length; a++) {
                if (lastMediaCount[a] > 0) {
                    hasMedia = true;
                    break;
                }
            }
            if (!hasMedia) {
                hasMedia = sharedMediaPreloader.hasSavedMessages;
            }
            if (!hasMedia) {
                hasMedia = sharedMediaPreloader.hasPreviews;
            }
        }
        if (!hasMedia && userInfo != null) {
            hasMedia = userInfo.stories_pinned_available;
        }
        if (!hasMedia && userInfo != null && userInfo.bot_info != null) {
            hasMedia = userInfo.bot_info.has_preview_medias;
        }
        if (!hasMedia && (userInfo != null && userInfo.stargifts_count > 0 || chatInfo != null && chatInfo.stargifts_count > 0)) {
            hasMedia = true;
        }
        if (!hasMedia && chatInfo != null) {
            hasMedia = chatInfo.stories_pinned_available;
        }
        if (!hasMedia) {
            if (chatId != 0 && MessagesController.ChannelRecommendations.hasRecommendations(currentAccount, -chatId)) {
                hasMedia = true;
            } else if (isBot && userId != 0 && MessagesController.ChannelRecommendations.hasRecommendations(currentAccount, userId)) {
                hasMedia = true;
            }
        }

        if (userId != 0) {
            if (LocaleController.isRTL) {
                emptyRow = rowCount++;
            }
            TLRPC.User user = getMessagesController().getUser(userId);

            if (UserObject.isUserSelf(user) && !myProfile) {
                if (avatarBig == null && (user.photo == null || !(user.photo.photo_big instanceof TLRPC.TL_fileLocation_layer97) && !(user.photo.photo_big instanceof TLRPC.TL_fileLocationToBeDeprecated)) && (avatarsViewPager == null || avatarsViewPager.getRealCount() == 0)) {
                    setAvatarRow = rowCount++;
                    setAvatarSectionRow = rowCount++;
                }
                numberSectionRow = rowCount++;
                numberRow = rowCount++;
                setUsernameRow = rowCount++;
                bioRow = rowCount++;

                settingsSectionRow = rowCount++;

                Set<String> suggestions = getMessagesController().pendingSuggestions;
                if (suggestions.contains("PREMIUM_GRACE")) {
                    graceSuggestionRow = rowCount++;
                    graceSuggestionSectionRow = rowCount++;
                } else if (suggestions.contains("VALIDATE_PHONE_NUMBER")) {
                    phoneSuggestionRow = rowCount++;
                    phoneSuggestionSectionRow = rowCount++;
                } else if (suggestions.contains("VALIDATE_PASSWORD")) {
                    passwordSuggestionRow = rowCount++;
                    passwordSuggestionSectionRow = rowCount++;
                }

                settingsSectionRow2 = rowCount++;
                chatRow = rowCount++;
                privacyRow = rowCount++;
                notificationRow = rowCount++;
                dataRow = rowCount++;
                liteModeRow = rowCount++;
//                stickersRow = rowCount++;
                if (getMessagesController().filtersEnabled || !getMessagesController().dialogFilters.isEmpty()) {
                    filtersRow = rowCount++;
                }
                devicesRow = rowCount++;
                languageRow = rowCount++;
                devicesSectionRow = rowCount++;
                if (!getMessagesController().premiumFeaturesBlocked()) {
                    premiumRow = rowCount++;
                }
                if (getMessagesController().starsPurchaseAvailable()) {
                    starsRow = rowCount++;
                }
                if (!getMessagesController().premiumFeaturesBlocked()) {
                    businessRow = rowCount++;
                }
                if (!getMessagesController().premiumPurchaseBlocked()) {
                    premiumGiftingRow = rowCount++;
                }
                if (premiumRow >= 0 || starsRow >= 0 || businessRow >= 0 || premiumGiftingRow >= 0) {
                    premiumSectionsRow = rowCount++;
                }
                helpHeaderRow = rowCount++;
                questionRow = rowCount++;
                faqRow = rowCount++;
                policyRow = rowCount++;
                if (BuildVars.LOGS_ENABLED || BuildVars.DEBUG_PRIVATE_VERSION) {
                    helpSectionCell = rowCount++;
                    debugHeaderRow = rowCount++;
                }
                if (BuildVars.LOGS_ENABLED) {
                    sendLogsRow = rowCount++;
                    sendLastLogsRow = rowCount++;
                    clearLogsRow = rowCount++;
                }
                if (BuildVars.DEBUG_VERSION) {
                    switchBackendRow = rowCount++;
                }
                versionRow = rowCount++;
            } else {
                String username = UserObject.getPublicUsername(user);
                boolean hasInfo = userInfo != null && !TextUtils.isEmpty(userInfo.about) || user != null && !TextUtils.isEmpty(username);
                boolean hasPhone = user != null && (!TextUtils.isEmpty(user.phone) || !TextUtils.isEmpty(vcardPhone));

                if (userInfo != null && (userInfo.flags2 & 64) != 0 && (profileChannelMessageFetcher == null || !profileChannelMessageFetcher.loaded || profileChannelMessageFetcher.messageObject != null)) {
                    final TLRPC.Chat channel = getMessagesController().getChat(userInfo.personal_channel_id);
                    if (channel != null && (ChatObject.isPublic(channel) || !ChatObject.isNotInChat(channel))) {
                        channelRow = rowCount++;
                        channelDividerRow = rowCount++;
                    }
                }
                infoStartRow = rowCount;
                infoHeaderRow = rowCount++;
                if (!isBot && (hasPhone || !hasInfo)) {
                    phoneRow = rowCount++;
                }
                if (userInfo != null && !TextUtils.isEmpty(userInfo.about)) {
                    userInfoRow = rowCount++;
                }
                if (user != null && username != null) {
                    usernameRow = rowCount++;
                }
                if (userInfo != null) {
                    if (userInfo.birthday != null) {
                        birthdayRow = rowCount++;
                    }
                    if (userInfo.business_work_hours != null) {
                        bizHoursRow = rowCount++;
                    }
                    if (userInfo.business_location != null) {
                        bizLocationRow = rowCount++;
                    }
                }
//                if (phoneRow != -1 || userInfoRow != -1 || usernameRow != -1 || bizHoursRow != -1 || bizLocationRow != -1) {
//                    notificationsDividerRow = rowCount++;
//                }
                if (userId != getUserConfig().getClientUserId()) {
                    notificationsRow = rowCount++;
                }
                if (isBot && user != null && user.bot_has_main_app) {
                    botAppRow = rowCount++;
                }
                infoEndRow = rowCount - 1;
                infoSectionRow = rowCount++;

                if (isBot && userInfo != null && userInfo.starref_program != null && (userInfo.starref_program.flags & 2) == 0 && getMessagesController().starrefConnectAllowed) {
                    affiliateRow = rowCount++;
                    infoAffiliateRow = rowCount++;
                }

                if (isBot) {
                    if (botLocation == null && getContext() != null)
                        botLocation = BotLocation.get(getContext(), currentAccount, userId);
                    if (botBiometry == null && getContext() != null)
                        botBiometry = BotBiometry.get(getContext(), currentAccount, userId);
                    final boolean containsPermissionLocation = botLocation != null && botLocation.asked();
                    final boolean containsPermissionBiometry = botBiometry != null && botBiometry.asked();
                    final boolean containsPermissionEmojiStatus = userInfo != null && userInfo.bot_can_manage_emoji_status || SetupEmojiStatusSheet.getAccessRequested(getContext(), currentAccount, userId);

                    if (containsPermissionEmojiStatus || containsPermissionLocation || containsPermissionBiometry) {
                        botPermissionsHeader = rowCount++;
                        if (containsPermissionEmojiStatus) {
                            botPermissionEmojiStatus = rowCount++;
                        }
                        if (containsPermissionLocation) {
                            botPermissionLocation = rowCount++;
                        }
                        if (containsPermissionBiometry) {
                            botPermissionBiometry = rowCount++;
                        }
                        botPermissionsDivider = rowCount++;
                    }
                }

                if (currentEncryptedChat instanceof TLRPC.TL_encryptedChat) {
                    settingsTimerRow = rowCount++;
                    settingsKeyRow = rowCount++;
                    secretSettingsSectionRow = rowCount++;
                }

                if (user != null && !isBot && currentEncryptedChat == null && user.id != getUserConfig().getClientUserId()) {
                    if (userBlocked) {
                        unblockRow = rowCount++;
                        lastSectionRow = rowCount++;
                    }
                }


                boolean divider = false;
                if (user != null && user.bot) {
                    if (userInfo != null && userInfo.can_view_revenue && BotStarsController.getInstance(currentAccount).getTONBalance(userId) > 0) {
                        botTonBalanceRow = rowCount++;
                    }
                    if (BotStarsController.getInstance(currentAccount).getBotStarsBalance(userId).amount > 0 || BotStarsController.getInstance(currentAccount).hasTransactions(userId)) {
                        botStarsBalanceRow = rowCount++;
                    }
                }

                if (user != null && isBot && !user.bot_nochats) {
                    addToGroupButtonRow = rowCount++;
                    addToGroupInfoRow = rowCount++;
                } else if (botStarsBalanceRow >= 0) {
                    divider = true;
                }

                if (!myProfile && showAddToContacts && user != null && !user.contact && !user.bot && !UserObject.isService(user.id)) {
                    addToContactsRow = rowCount++;
                    divider = true;
                }
                if (!myProfile && reportReactionMessageId != 0 && !ContactsController.getInstance(currentAccount).isContact(userId)) {
                    reportReactionRow = rowCount++;
                    divider = true;
                }
                if (divider) {
                    reportDividerRow = rowCount++;
                }

                if (hasMedia || (user != null && user.bot && user.bot_can_edit) || userInfo != null && userInfo.common_chats_count != 0 || myProfile) {
                    sharedMediaRow = rowCount++;
                } else if (lastSectionRow == -1) {
                    sendMessageRow = rowCount++;
                    reportRow = rowCount++;
                    lastSectionRow = rowCount++;
                }
            }
        } else if (isTopic) {
            infoHeaderRow = rowCount++;
            usernameRow = rowCount++;
            notificationsSimpleRow = rowCount++;
            infoSectionRow = rowCount++;
            if (hasMedia) {
                sharedMediaRow = rowCount++;
            }
        } else if (chatId != 0) {
            if (chatInfo != null && (!TextUtils.isEmpty(chatInfo.about) || chatInfo.location instanceof TLRPC.TL_channelLocation) || ChatObject.isPublic(currentChat)) {
                if (LocaleController.isRTL && ChatObject.isChannel(currentChat) && chatInfo != null && !currentChat.megagroup && chatInfo.linked_chat_id != 0) {
                    emptyRow = rowCount++;
                }
                infoHeaderRow = rowCount++;
                if (chatInfo != null) {
                    if (!TextUtils.isEmpty(chatInfo.about)) {
                        channelInfoRow = rowCount++;
                    }
                    if (chatInfo.location instanceof TLRPC.TL_channelLocation) {
                        locationRow = rowCount++;
                    }
                }
                if (ChatObject.isPublic(currentChat)) {
                    usernameRow = rowCount++;
                }
            }
//            if (infoHeaderRow != -1) {
//                notificationsDividerRow = rowCount++;
//            }
            notificationsRow = rowCount++;
            infoSectionRow = rowCount++;

            if (ChatObject.isChannel(currentChat) && !currentChat.megagroup) {
                if (chatInfo != null && (currentChat.creator || chatInfo.can_view_participants)) {
                    membersHeaderRow = rowCount++;
                    subscribersRow = rowCount++;
                    if (chatInfo != null && chatInfo.requests_pending > 0) {
                        subscribersRequestsRow = rowCount++;
                    }
                    administratorsRow = rowCount++;
                    if (chatInfo != null && (chatInfo.banned_count != 0 || chatInfo.kicked_count != 0)) {
                        blockedUsersRow = rowCount++;
                    }
                    if (chatInfo != null && chatInfo.can_view_stars_revenue && (BotStarsController.getInstance(currentAccount).getBotStarsBalance(-chatId).amount > 0 || BotStarsController.getInstance(currentAccount).hasTransactions(-chatId)) || chatInfo != null && chatInfo.can_view_revenue && BotStarsController.getInstance(currentAccount).getTONBalance(-chatId) > 0) {
                        channelBalanceRow = rowCount++;
                    }
                    settingsRow = rowCount++;
                    channelBalanceSectionRow = rowCount++;
                }
            } else {
                if (chatInfo != null && chatInfo.can_view_stars_revenue && (BotStarsController.getInstance(currentAccount).getBotStarsBalance(-chatId).amount > 0 || BotStarsController.getInstance(currentAccount).hasTransactions(-chatId)) || chatInfo != null && chatInfo.can_view_revenue && BotStarsController.getInstance(currentAccount).getTONBalance(-chatId) > 0) {
                    channelBalanceRow = rowCount++;
                    channelBalanceSectionRow = rowCount++;
                }
            }

            if (ChatObject.isChannel(currentChat)) {
                if (!isTopic && chatInfo != null && currentChat.megagroup && chatInfo.participants != null && chatInfo.participants.participants != null && !chatInfo.participants.participants.isEmpty()) {
                    if (!ChatObject.isNotInChat(currentChat) && ChatObject.canAddUsers(currentChat) && chatInfo.participants_count < getMessagesController().maxMegagroupCount) {
                        addMemberRow = rowCount++;
                    }
                    int count = chatInfo.participants.participants.size();
                    if ((count <= 5 || !hasMedia || usersForceShowingIn == 1) && usersForceShowingIn != 2) {
                        if (addMemberRow == -1) {
                            membersHeaderRow = rowCount++;
                        }
                        membersStartRow = rowCount;
                        rowCount += count;
                        membersEndRow = rowCount;
                        membersSectionRow = rowCount++;
                        visibleChatParticipants.addAll(chatInfo.participants.participants);
                        if (sortedUsers != null) {
                            visibleSortedUsers.addAll(sortedUsers);
                        }
                        usersForceShowingIn = 1;
                        if (sharedMediaLayout != null) {
                            sharedMediaLayout.setChatUsers(null, null);
                        }
                    } else {
                        if (addMemberRow != -1) {
                            membersSectionRow = rowCount++;
                        }
                        if (sharedMediaLayout != null) {
                            if (!sortedUsers.isEmpty()) {
                                usersForceShowingIn = 2;
                            }
                            sharedMediaLayout.setChatUsers(sortedUsers, chatInfo);
                        }
                    }
                } else {
                    if (!ChatObject.isNotInChat(currentChat) && ChatObject.canAddUsers(currentChat) && chatInfo != null && chatInfo.participants_hidden) {
                        addMemberRow = rowCount++;
                        membersSectionRow = rowCount++;
                    }
                    if (sharedMediaLayout != null) {
                        sharedMediaLayout.updateAdapters();
                    }
                }

                if (lastSectionRow == -1 && currentChat.left && !currentChat.kicked) {
                    long requestedTime = MessagesController.getNotificationsSettings(currentAccount).getLong("dialog_join_requested_time_" + dialogId, -1);
                    if (!(requestedTime > 0 && System.currentTimeMillis() - requestedTime < 1000 * 60 * 2)) {
                        joinRow = rowCount++;
                        lastSectionRow = rowCount++;
                    }
                }
            } else if (chatInfo != null) {
                if (!isTopic && chatInfo.participants != null && chatInfo.participants.participants != null && !(chatInfo.participants instanceof TLRPC.TL_chatParticipantsForbidden)) {
                    if (ChatObject.canAddUsers(currentChat) || currentChat.default_banned_rights == null || !currentChat.default_banned_rights.invite_users) {
                        addMemberRow = rowCount++;
                    }
                    int count = chatInfo.participants.participants.size();
                    if (count <= 5 || !hasMedia) {
                        if (addMemberRow == -1) {
                            membersHeaderRow = rowCount++;
                        }
                        membersStartRow = rowCount;
                        rowCount += chatInfo.participants.participants.size();
                        membersEndRow = rowCount;
                        membersSectionRow = rowCount++;
                        visibleChatParticipants.addAll(chatInfo.participants.participants);
                        if (sortedUsers != null) {
                            visibleSortedUsers.addAll(sortedUsers);
                        }
                        if (sharedMediaLayout != null) {
                            sharedMediaLayout.setChatUsers(null, null);
                        }
                    } else {
                        if (addMemberRow != -1) {
                            membersSectionRow = rowCount++;
                        }
                        if (sharedMediaLayout != null) {
                            sharedMediaLayout.setChatUsers(sortedUsers, chatInfo);
                        }
                    }
                } else {
                    if (!ChatObject.isNotInChat(currentChat) && ChatObject.canAddUsers(currentChat) && chatInfo.participants_hidden) {
                        addMemberRow = rowCount++;
                        membersSectionRow = rowCount++;
                    }
                    if (sharedMediaLayout != null) {
                        sharedMediaLayout.updateAdapters();
                    }
                }
            }

            if (hasMedia) {
                sharedMediaRow = rowCount++;
            }
        }
        if (sharedMediaRow == -1) {
            bottomPaddingRow = rowCount++;
        }
        final int actionBarHeight = actionBar != null ? ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) : 0;
        // TODO
//        if (listView == null || prevRowsCount > rowCount || listContentHeight != 0 && listContentHeight + actionBarHeight + AndroidUtilities.dp(88) < listView.getMeasuredHeight()) {
//            lastMeasuredContentWidth = 0;
//        }
        if (listView != null) {
            listView.setTranslateSelectorPosition(bizHoursRow);
        }
    }

    public int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }


    private void updateProfileData(boolean reload) {
//        if (avatarContainer == null || nameTextView == null || getParentActivity() == null) {
        if (avatarContainer == null || getParentActivity() == null) {
            return;
        }
        String onlineTextOverride;
        int currentConnectionState = getConnectionsManager().getConnectionState();
        if (currentConnectionState == ConnectionsManager.ConnectionStateWaitingForNetwork) {
            onlineTextOverride = LocaleController.getString(R.string.WaitingForNetwork);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateConnecting) {
            onlineTextOverride = LocaleController.getString(R.string.Connecting);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateUpdating) {
            onlineTextOverride = LocaleController.getString(R.string.Updating);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateConnectingToProxy) {
            onlineTextOverride = LocaleController.getString(R.string.ConnectingToProxy);
        } else {
            onlineTextOverride = null;
        }

        BaseFragment prevFragment = null;
        if (parentLayout != null && parentLayout.getFragmentStack().size() >= 2) {
            BaseFragment fragment = parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2);
            if (fragment instanceof ChatActivityInterface) {
                prevFragment = fragment;
            }
            if (fragment instanceof DialogsActivity) {
                DialogsActivity dialogsActivity = (DialogsActivity) fragment;
                if (dialogsActivity.rightSlidingDialogContainer != null && dialogsActivity.rightSlidingDialogContainer.currentFragment instanceof ChatActivityInterface) {
                    previousTransitionMainFragment = dialogsActivity;
                    prevFragment = dialogsActivity.rightSlidingDialogContainer.currentFragment;
                }
            }
        }
        final boolean copyFromChatActivity = prevFragment instanceof ChatActivity && ((ChatActivity) prevFragment).avatarContainer != null && ((ChatActivity) prevFragment).getChatMode() == ChatActivity.MODE_SUGGESTIONS;

        TLRPC.TL_forumTopic topic = null;
        boolean shortStatus;

        hasFallbackPhoto = false;
        hasCustomPhoto = false;
        if (userId != 0) {
            TLRPC.User user = getMessagesController().getUser(userId);
            if (user == null) {
                return;
            }
            shortStatus = user.photo != null && user.photo.personal;
            TLRPC.FileLocation photoBig = null;
            if (user.photo != null) {
                photoBig = user.photo.photo_big;
            }
            avatarDrawable.setInfo(currentAccount, user);

            final MessagesController.PeerColor wasPeerColor = peerColor;
            peerColor = MessagesController.PeerColor.fromCollectible(user.emoji_status);
            if (peerColor == null) {
                final int colorId = UserObject.getProfileColorId(user);
                final MessagesController.PeerColors peerColors = MessagesController.getInstance(currentAccount).profilePeerColors;
                peerColor = peerColors == null ? null : peerColors.getColor(colorId);
            }
            if (wasPeerColor != peerColor) {
                updatedPeerColor();
            }
            if (topView != null) {
                topView.setBackgroundEmojiId(UserObject.getProfileEmojiId(user), user != null && user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible, true);
            }
            setCollectibleGiftStatus(user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible ? (TLRPC.TL_emojiStatusCollectible) user.emoji_status : null);

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
            if (avatar == null) {
                avatarsViewPager.initIfEmpty(vectorAvatarThumbDrawable, imageLocation, thumbLocation, reload);
            }
            if (avatarBig == null) {
                if (vectorAvatar != null) {
                    avatarImage.setImageDrawable(vectorAvatarThumbDrawable);
                } else if (videoThumbLocation != null && !user.photo.personal) {
                    avatarImage.getImageReceiver().setVideoThumbIsSame(true);
                    avatarImage.setImage(videoThumbLocation, "avatar", thumbLocation, "50_50", avatarDrawable, user);
                } else {
                    avatarImage.setImage(videoLocation, ImageLoader.AUTOPLAY_FILTER, thumbLocation, "50_50", avatarDrawable, user);
                }
            }

            if (thumbLocation != null && setAvatarRow != -1 || thumbLocation == null && setAvatarRow == -1) {
                updateListAnimated(false);
                checkLayout();
            }
            if (imageLocation != null && (prevLoadedImageLocation == null || imageLocation.photoId != prevLoadedImageLocation.photoId)) {
                prevLoadedImageLocation = imageLocation;
                getFileLoader().loadFile(imageLocation, user, null, FileLoader.PRIORITY_LOW, 1);
            }

            CharSequence newString = UserObject.getUserName(user);
            String newString2;
            boolean hiddenStatusButton = false;
            if (user.id == getUserConfig().getClientUserId()) {
                if (UserObject.hasFallbackPhoto(getUserInfo())) {
                    newString2 = "";
                    hasFallbackPhoto = true;
                    TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(getUserInfo().fallback_photo.sizes, 1000);
                    if (smallSize != null) {
                        fallbackImage.setImage(ImageLocation.getForPhoto(smallSize, getUserInfo().fallback_photo), "50_50", null, 0, null, UserConfig.getInstance(currentAccount).getCurrentUser(), 0);
                    }
                } else {
                    newString2 = LocaleController.getString(R.string.Online);
                }
            } else if (user.id == UserObject.VERIFY) {
                newString2 = LocaleController.getString(R.string.VerifyCodesNotifications);
            } else if (user.id == 333000 || user.id == 777000 || user.id == 42777) {
                newString2 = LocaleController.getString(R.string.ServiceNotifications);
            } else if (MessagesController.isSupportUser(user)) {
                newString2 = LocaleController.getString(R.string.SupportStatus);
            } else if (isBot) {
                if (user.bot_active_users != 0) {
                    newString2 = LocaleController.formatPluralStringComma("BotUsers", user.bot_active_users, ',');
                } else {
                    newString2 = LocaleController.getString(R.string.Bot);
                }
            } else {
                isOnline[0] = false;
                newString2 = LocaleController.formatUserStatus(currentAccount, user, isOnline, shortStatus ? new boolean[1] : null);
                hiddenStatusButton = user != null && !isOnline[0] && !getUserConfig().isPremium() && user.status != null && (user.status instanceof TLRPC.TL_userStatusRecently || user.status instanceof TLRPC.TL_userStatusLastMonth || user.status instanceof TLRPC.TL_userStatusLastWeek) && user.status.by_me;
                if (onlineTextView[1] != null && !mediaHeaderVisible) {
                    int key = isOnline[0] && peerColor == null ? Theme.key_profile_status : Theme.key_avatar_subtitleInProfileBlue;
                    onlineTextView[1].setTag(key);
                    if (!isPulledDown) {
                        onlineTextView[1].setTextColor(applyPeerColor(getThemedColor(key), true, isOnline[0]));
                    }
                }
            }
            hasCustomPhoto = user.photo != null && user.photo.personal;
            try {
                newString = Emoji.replaceEmoji(newString, nameTextView[1].getPaint().getFontMetricsInt(), false);
            } catch (Exception ignore) {
            }
            if (copyFromChatActivity) {
                ChatActivity chatActivity = (ChatActivity) prevFragment;
                BackupImageView fromAvatarImage = chatActivity.avatarContainer.getAvatarImageView();
                avatarImage.setAnimateFromImageReceiver(fromAvatarImage.getImageReceiver());
            }
            for (int a = 0; a < 2; a++) {
                if (nameTextView[a] == null) {
                    continue;
                }
                if (a == 0 && copyFromChatActivity) {
                    ChatActivity chatActivity = (ChatActivity) prevFragment;
                    SimpleTextView titleTextView = chatActivity.avatarContainer.getTitleTextView();
                    nameTextView[a].setText(titleTextView.getText());
                    nameTextView[a].setRightDrawable(titleTextView.getRightDrawable());
                    nameTextView[a].setRightDrawable2(titleTextView.getRightDrawable2());
                } else if (a == 0 && user.id != getUserConfig().getClientUserId() && !MessagesController.isSupportUser(user) && user.phone != null && user.phone.length() != 0 && getContactsController().contactsDict.get(user.id) == null &&
                        (getContactsController().contactsDict.size() != 0 || !getContactsController().isLoadingContacts())) {
                    nameTextView[a].setText(PhoneFormat.getInstance().format("+" + user.phone));
                } else {
                    nameTextView[a].setText(newString);
                }
                if (a == 0 && onlineTextOverride != null) {
                    onlineTextView[a].setText(onlineTextOverride);
                } else if (a == 0 && copyFromChatActivity) {
                    ChatActivity chatActivity = (ChatActivity) prevFragment;
                    if (chatActivity.avatarContainer.getSubtitleTextView() instanceof SimpleTextView) {
                        SimpleTextView textView = (SimpleTextView) chatActivity.avatarContainer.getSubtitleTextView();
                        onlineTextView[a].setText(textView.getText());
                    } else if (chatActivity.avatarContainer.getSubtitleTextView() instanceof AnimatedTextView) {
                        AnimatedTextView textView = (AnimatedTextView) chatActivity.avatarContainer.getSubtitleTextView();
                        onlineTextView[a].setText(textView.getText());
                    }
                } else {
                    onlineTextView[a].setText(newString2);
                }
                onlineTextView[a].setDrawablePadding(dp(9));
                onlineTextView[a].setRightDrawableInside(true);
                onlineTextView[a].setRightDrawable(a == 1 && hiddenStatusButton ? getShowStatusButton() : null);
                onlineTextView[a].setRightDrawableOnClick(a == 1 && hiddenStatusButton ? v -> {
                    MessagePrivateSeenView.showSheet(getContext(), currentAccount, getDialogId(), true, null, () -> {
                        getMessagesController().reloadUser(getDialogId());
                    }, resourcesProvider);
                } : null);
                Drawable leftIcon = currentEncryptedChat != null ? getLockIconDrawable() : null;
                boolean rightIconIsPremium = false, rightIconIsStatus = false;
                nameTextView[a].setRightDrawableOutside(a == 0);
                if (a == 0 && !copyFromChatActivity) {
                    if (user.scam || user.fake) {
                        nameTextView[a].setRightDrawable2(getScamDrawable(user.scam ? 0 : 1));
                        nameTextViewRightDrawable2ContentDescription = LocaleController.getString(R.string.ScamMessage);
                    } else if (user.verified) {
                        nameTextView[a].setRightDrawable2(getVerifiedCrossfadeDrawable(a));
                        nameTextViewRightDrawable2ContentDescription = LocaleController.getString(R.string.AccDescrVerified);
                    } else if (getMessagesController().isDialogMuted(dialogId != 0 ? dialogId : userId, topicId)) {
                        nameTextView[a].setRightDrawable2(getThemedDrawable(Theme.key_drawable_muteIconDrawable));
                        nameTextViewRightDrawable2ContentDescription = LocaleController.getString(R.string.NotificationsMuted);
                    } else {
                        nameTextView[a].setRightDrawable2(null);
                        nameTextViewRightDrawable2ContentDescription = null;
                    }
                    if (user != null && !getMessagesController().premiumFeaturesBlocked() && !MessagesController.isSupportUser(user) && DialogObject.getEmojiStatusDocumentId(user.emoji_status) != 0) {
                        rightIconIsStatus = true;
                        rightIconIsPremium = false;
                        nameTextView[a].setRightDrawable(getEmojiStatusDrawable(user.emoji_status, false, false, a));
                        nameTextViewRightDrawableContentDescription = LocaleController.getString(R.string.AccDescrPremium);
                    } else if (getMessagesController().isPremiumUser(user)) {
                        rightIconIsStatus = false;
                        rightIconIsPremium = true;
                        nameTextView[a].setRightDrawable(getEmojiStatusDrawable(null, false, false, a));
                        nameTextViewRightDrawableContentDescription = LocaleController.getString(R.string.AccDescrPremium);
                    } else {
                        nameTextView[a].setRightDrawable(null);
                        nameTextViewRightDrawableContentDescription = null;
                    }
                } else if (a == 1) {
                    if (user.scam || user.fake) {
                        nameTextView[a].setRightDrawable2(getScamDrawable(user.scam ? 0 : 1));
                    } else if (user.verified) {
                        nameTextView[a].setRightDrawable2(getVerifiedCrossfadeDrawable(a));
                    } else {
                        nameTextView[a].setRightDrawable2(null);
                    }
                    if (!getMessagesController().premiumFeaturesBlocked() && user != null && !MessagesController.isSupportUser(user) && DialogObject.getEmojiStatusDocumentId(user.emoji_status) != 0) {
                        rightIconIsStatus = true;
                        rightIconIsPremium = false;
                        nameTextView[a].setRightDrawable(getEmojiStatusDrawable(user.emoji_status, true, true, a));
                    } else if (getMessagesController().isPremiumUser(user)) {
                        rightIconIsStatus = false;
                        rightIconIsPremium = true;
                        nameTextView[a].setRightDrawable(getEmojiStatusDrawable(null, true, true, a));
                    } else {
                        nameTextView[a].setRightDrawable(null);
                    }
                }
                if (leftIcon == null && currentEncryptedChat == null && user.bot_verification_icon != 0) {
                    nameTextView[a].setLeftDrawableOutside(true);
                    leftIcon = getBotVerificationDrawable(user.bot_verification_icon, false, a);
                } else {
                    nameTextView[a].setLeftDrawableOutside(false);
                }
                nameTextView[a].setLeftDrawable(leftIcon);
                if (a == 1 && (rightIconIsStatus || rightIconIsPremium)) {
                    nameTextView[a].setRightDrawableOutside(true);
                }
                if (user.self && getMessagesController().isPremiumUser(user)) {
                    nameTextView[a].setRightDrawableOnClick(v -> {
                        showStatusSelect();
                    });
                }
                if (!user.self && getMessagesController().isPremiumUser(user)) {
                    final SimpleTextView textView = nameTextView[a];
                    nameTextView[a].setRightDrawableOnClick(v -> {
                        if (user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible) {
                            TLRPC.TL_emojiStatusCollectible status = (TLRPC.TL_emojiStatusCollectible) user.emoji_status;
                            if (status != null) {
                                Browser.openUrl(getContext(), "https://" + getMessagesController().linkPrefix + "/nft/" + status.slug);
                            }
                            return;
                        }
                        PremiumPreviewBottomSheet premiumPreviewBottomSheet = new PremiumPreviewBottomSheet(DebugProfile.this, currentAccount, user, resourcesProvider);
                        int[] coords = new int[2];
                        textView.getLocationOnScreen(coords);
                        premiumPreviewBottomSheet.startEnterFromX = textView.rightDrawableX;
                        premiumPreviewBottomSheet.startEnterFromY = textView.rightDrawableY;
                        premiumPreviewBottomSheet.startEnterFromScale = textView.getScaleX();
                        premiumPreviewBottomSheet.startEnterFromX1 = textView.getLeft();
                        premiumPreviewBottomSheet.startEnterFromY1 = textView.getTop();
                        premiumPreviewBottomSheet.startEnterFromView = textView;
                        if (textView.getRightDrawable() == emojiStatusDrawable[1] && emojiStatusDrawable[1] != null && emojiStatusDrawable[1].getDrawable() instanceof AnimatedEmojiDrawable) {
                            premiumPreviewBottomSheet.startEnterFromScale *= 0.98f;
                            TLRPC.Document document = ((AnimatedEmojiDrawable) emojiStatusDrawable[1].getDrawable()).getDocument();
                            if (document != null) {
                                BackupImageView icon = new BackupImageView(getContext());
                                String filter = "160_160";
                                ImageLocation mediaLocation;
                                String mediaFilter;
                                SvgHelper.SvgDrawable thumbDrawable = DocumentObject.getSvgThumb(document.thumbs, Theme.key_windowBackgroundWhiteGrayIcon, 0.2f);
                                TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(document.thumbs, 90);
                                if ("video/webm".equals(document.mime_type)) {
                                    mediaLocation = ImageLocation.getForDocument(document);
                                    mediaFilter = filter + "_" + ImageLoader.AUTOPLAY_FILTER;
                                    if (thumbDrawable != null) {
                                        thumbDrawable.overrideWidthAndHeight(512, 512);
                                    }
                                } else {
                                    if (thumbDrawable != null && MessageObject.isAnimatedStickerDocument(document, false)) {
                                        thumbDrawable.overrideWidthAndHeight(512, 512);
                                    }
                                    mediaLocation = ImageLocation.getForDocument(document);
                                    mediaFilter = filter;
                                }
                                icon.setLayerNum(7);
                                icon.setRoundRadius(AndroidUtilities.dp(4));
                                icon.setImage(mediaLocation, mediaFilter, ImageLocation.getForDocument(thumb, document), "140_140", thumbDrawable, document);
                                if (((AnimatedEmojiDrawable) emojiStatusDrawable[1].getDrawable()).canOverrideColor()) {
                                    icon.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_windowBackgroundWhiteBlueIcon), PorterDuff.Mode.SRC_IN));
                                    premiumPreviewBottomSheet.statusStickerSet = MessageObject.getInputStickerSet(document);
                                } else {
                                    premiumPreviewBottomSheet.statusStickerSet = MessageObject.getInputStickerSet(document);
                                }
                                premiumPreviewBottomSheet.overrideTitleIcon = icon;
                                premiumPreviewBottomSheet.isEmojiStatus = true;
                            }
                        }
                        showDialog(premiumPreviewBottomSheet);
                    });
                }
            }

            if (userId == UserConfig.getInstance(currentAccount).clientUserId) {
                onlineTextView[2].setText(LocaleController.getString(R.string.FallbackTooltip));
                onlineTextView[3].setText(LocaleController.getString(R.string.Online));
            } else {
                if (user.photo != null && user.photo.personal && user.photo.has_video) {
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(newString2);
                    spannableStringBuilder.setSpan(new EmptyStubSpan(), 0, newString2.length(), 0);
                    spannableStringBuilder.append(" d ");
                    spannableStringBuilder.append(LocaleController.getString(R.string.CustomAvatarTooltipVideo));
                    spannableStringBuilder.setSpan(new DotDividerSpan(), newString2.length() + 1, newString2.length() + 2, 0);
                    onlineTextView[2].setText(spannableStringBuilder);
                } else {
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(newString2);
                    spannableStringBuilder.setSpan(new EmptyStubSpan(), 0, newString2.length(), 0);
                    spannableStringBuilder.append(" d ");
                    spannableStringBuilder.append(LocaleController.getString(R.string.CustomAvatarTooltip));
                    spannableStringBuilder.setSpan(new DotDividerSpan(), newString2.length() + 1, newString2.length() + 2, 0);
                    onlineTextView[2].setText(spannableStringBuilder);
                }
            }

            onlineTextView[2].setVisibility(View.VISIBLE);
            if (!searchMode) {
                onlineTextView[3].setVisibility(View.VISIBLE);
            }

            // TODO
//            if (previousTransitionFragment != null) {
//                previousTransitionFragment.checkAndUpdateAvatar();
//            }
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.isShowingImage(photoBig) && (getLastStoryViewer() == null || getLastStoryViewer().transitionViewHolder.view != avatarImage), storyView != null);
        } else if (chatId != 0) {
            TLRPC.Chat chat = getMessagesController().getChat(chatId);
            if (chat != null) {
                currentChat = chat;
            } else {
                chat = currentChat;
            }
            if (flagSecure != null) {
                flagSecure.invalidate();
            }

            final MessagesController.PeerColor wasPeerColor = peerColor;
            peerColor = MessagesController.PeerColor.fromCollectible(chat.emoji_status);
            if (peerColor == null) {
                final int colorId = ChatObject.getProfileColorId(chat);
                MessagesController.PeerColors peerColors = MessagesController.getInstance(currentAccount).profilePeerColors;
                peerColor = peerColors == null ? null : peerColors.getColor(colorId);
            }
            if (wasPeerColor != peerColor) {
                updatedPeerColor();
            }
            if (topView != null) {
                topView.setBackgroundEmojiId(ChatObject.getProfileEmojiId(chat), chat != null && chat.emoji_status instanceof TLRPC.TL_emojiStatusCollectible, true);
            }
            setCollectibleGiftStatus(chat.emoji_status instanceof TLRPC.TL_emojiStatusCollectible ? (TLRPC.TL_emojiStatusCollectible) chat.emoji_status : null);

            if (isTopic) {
                topic = getMessagesController().getTopicsController().findTopic(chatId, topicId);
            }

            CharSequence statusString;
            CharSequence profileStatusString;
            boolean profileStatusIsButton = false;
            if (ChatObject.isChannel(chat)) {
                if (!isTopic && (chatInfo == null || !currentChat.megagroup && (chatInfo.participants_count == 0 || ChatObject.hasAdminRights(currentChat) || chatInfo.can_view_participants))) {
                    if (currentChat.megagroup) {
                        statusString = profileStatusString = LocaleController.getString(R.string.Loading).toLowerCase();
                    } else {
                        if (ChatObject.isPublic(chat)) {
                            statusString = profileStatusString = LocaleController.getString(R.string.ChannelPublic).toLowerCase();
                        } else {
                            statusString = profileStatusString = LocaleController.getString(R.string.ChannelPrivate).toLowerCase();
                        }
                    }
                } else {
                    if (isTopic) {
                        int count = 0;
                        if (topic != null) {
                            count = topic.totalMessagesCount - 1;
                        }
                        if (count > 0) {
                            statusString = LocaleController.formatPluralString("messages", count, count);
                        } else {
                            statusString = formatString("TopicProfileStatus", R.string.TopicProfileStatus, chat.title);
                        }
                        SpannableString arrowString = new SpannableString(">");
                        arrowString.setSpan(new ColoredImageSpan(R.drawable.arrow_newchat), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        profileStatusString = new SpannableStringBuilder(chat.title).append(' ').append(arrowString);
                        profileStatusIsButton = true;
                    } else if (currentChat.megagroup) {
                        if (onlineCount > 1 && chatInfo.participants_count != 0) {
                            statusString = String.format("%s, %s", LocaleController.formatPluralString("Members", chatInfo.participants_count), LocaleController.formatPluralString("OnlineCount", Math.min(onlineCount, chatInfo.participants_count)));
                            profileStatusString = String.format("%s, %s", LocaleController.formatPluralStringComma("Members", chatInfo.participants_count), LocaleController.formatPluralStringComma("OnlineCount", Math.min(onlineCount, chatInfo.participants_count)));
                        } else {
                            if (chatInfo.participants_count == 0) {
                                if (chat.has_geo) {
                                    statusString = profileStatusString = LocaleController.getString(R.string.MegaLocation).toLowerCase();
                                } else if (ChatObject.isPublic(chat)) {
                                    statusString = profileStatusString = LocaleController.getString(R.string.MegaPublic).toLowerCase();
                                } else {
                                    statusString = profileStatusString = LocaleController.getString(R.string.MegaPrivate).toLowerCase();
                                }
                            } else {
                                statusString = LocaleController.formatPluralString("Members", chatInfo.participants_count);
                                profileStatusString = LocaleController.formatPluralStringComma("Members", chatInfo.participants_count);
                            }
                        }
                    } else {
                        int[] result = new int[1];
                        String shortNumber = LocaleController.formatShortNumber(chatInfo.participants_count, result);
                        if (currentChat.megagroup) {
                            statusString = LocaleController.formatPluralString("Members", chatInfo.participants_count);
                            profileStatusString = LocaleController.formatPluralStringComma("Members", chatInfo.participants_count);
                        } else {
                            statusString = LocaleController.formatPluralString("Subscribers", chatInfo.participants_count);
                            profileStatusString = LocaleController.formatPluralStringComma("Subscribers", chatInfo.participants_count);
                        }
                    }
                }
            } else {
                if (ChatObject.isKickedFromChat(chat)) {
                    statusString = profileStatusString = LocaleController.getString(R.string.YouWereKicked);
                } else if (ChatObject.isLeftFromChat(chat)) {
                    statusString = profileStatusString = LocaleController.getString(R.string.YouLeft);
                } else {
                    int count = chat.participants_count;
                    if (chatInfo != null && chatInfo.participants != null) {
                        count = chatInfo.participants.participants.size();
                    }
                    if (count != 0 && onlineCount > 1) {
                        statusString = profileStatusString = String.format("%s, %s", LocaleController.formatPluralString("Members", count), LocaleController.formatPluralString("OnlineCount", onlineCount));
                    } else {
                        statusString = profileStatusString = LocaleController.formatPluralString("Members", count);
                    }
                }
            }
            if (copyFromChatActivity) {
                ChatActivity chatActivity = (ChatActivity) prevFragment;
                if (chatActivity.avatarContainer.getSubtitleTextView() instanceof SimpleTextView) {
                    statusString = ((SimpleTextView) chatActivity.avatarContainer.getSubtitleTextView()).getText();
                } else if (chatActivity.avatarContainer.getSubtitleTextView() instanceof AnimatedTextView) {
                    statusString = ((AnimatedTextView) chatActivity.avatarContainer.getSubtitleTextView()).getText();
                }
                BackupImageView fromAvatarImage = chatActivity.avatarContainer.getAvatarImageView();
                avatarImage.setAnimateFromImageReceiver(fromAvatarImage.getImageReceiver());
            }

            // TODO
            boolean changed = false;
            for (int a = 0; a < 2; a++) {
                if (nameTextView[a] == null) {
                    continue;
                }
                if (a == 0 && copyFromChatActivity) {
                    ChatActivity chatActivity = (ChatActivity) prevFragment;
                    SimpleTextView titleTextView = chatActivity.avatarContainer.getTitleTextView();
                    if (nameTextView[a].setText(titleTextView.getText())) {
                        changed = true;
                    }
                    if (nameTextView[a].setRightDrawable(titleTextView.getRightDrawable())) {
                        changed = true;
                    }
                    if (nameTextView[a].setRightDrawable2(titleTextView.getRightDrawable2())) {
                        changed = true;
                    }
                } else if (isTopic) {
                    CharSequence title = topic == null ? "" : topic.title;
                    try {
                        title = Emoji.replaceEmoji(title, nameTextView[a].getPaint().getFontMetricsInt(), false);
                    } catch (Exception ignore) {
                    }
                    if (nameTextView[a].setText(title)) {
                        changed = true;
                    }
                } else if (ChatObject.isMonoForum(chat)) {
                    CharSequence title = getString(R.string.ChatMessageSuggestions);
                    if (nameTextView[a].setText(title)) {
                        changed = true;
                    }
                } else if (chat.title != null) {
                    CharSequence title = chat.title;
                    try {
                        title = Emoji.replaceEmoji(title, nameTextView[a].getPaint().getFontMetricsInt(), false);
                    } catch (Exception ignore) {
                    }
                    if (nameTextView[a].setText(title)) {
                        changed = true;
                    }
                }
                nameTextView[a].setLeftDrawableOutside(false);
                nameTextView[a].setLeftDrawable(null);
                nameTextView[a].setRightDrawableOutside(a == 0);
                nameTextView[a].setRightDrawableOnClick(null);
                if (a != 0) {
                    if (chat.scam || chat.fake) {
                        nameTextView[a].setRightDrawable2(getScamDrawable(chat.scam ? 0 : 1));
                        nameTextViewRightDrawableContentDescription = LocaleController.getString(R.string.ScamMessage);
                    } else if (chat.verified) {
                        nameTextView[a].setRightDrawable2(getVerifiedCrossfadeDrawable(a));
                        nameTextViewRightDrawableContentDescription = LocaleController.getString(R.string.AccDescrVerified);
                    } else {
                        nameTextView[a].setRightDrawable2(null);
                        nameTextViewRightDrawableContentDescription = null;
                    }
                    if (DialogObject.getEmojiStatusDocumentId(chat.emoji_status) != 0) {
                        nameTextView[a].setRightDrawable(getEmojiStatusDrawable(chat.emoji_status, true, false, a));
                        nameTextView[a].setRightDrawableOutside(true);
                        nameTextViewRightDrawableContentDescription = null;
                        if (ChatObject.canChangeChatInfo(chat)) {
                            nameTextView[a].setRightDrawableOnClick(v -> {
                                showStatusSelect();
                            });
                            if (preloadedChannelEmojiStatuses) {
                                preloadedChannelEmojiStatuses = true;
                                getMediaDataController().loadRestrictedStatusEmojis();
                            }
                        } else if (chat.emoji_status instanceof TLRPC.TL_emojiStatusCollectible) {
                            final String slug = ((TLRPC.TL_emojiStatusCollectible) chat.emoji_status).slug;
                            nameTextView[a].setRightDrawableOnClick(v -> {
                                Browser.openUrl(getContext(), "https://" + getMessagesController().linkPrefix + "/nft/" + slug);
                            });
                        }
                    }
                } else if (!copyFromChatActivity) {
                    if (chat.scam || chat.fake) {
                        nameTextView[a].setRightDrawable2(getScamDrawable(chat.scam ? 0 : 1));
                    } else if (chat.verified) {
                        nameTextView[a].setRightDrawable2(getVerifiedCrossfadeDrawable(a));
                    } else if (getMessagesController().isDialogMuted(-chatId, topicId)) {
                        nameTextView[a].setRightDrawable2(getThemedDrawable(Theme.key_drawable_muteIconDrawable));
                    } else {
                        nameTextView[a].setRightDrawable2(null);
                    }
                    if (DialogObject.getEmojiStatusDocumentId(chat.emoji_status) != 0) {
                        nameTextView[a].setRightDrawable(getEmojiStatusDrawable(chat.emoji_status, false, false, a));
                        nameTextView[a].setRightDrawableOutside(true);
                    } else {
                        nameTextView[a].setRightDrawable(null);
                    }
                }
                if (chat.bot_verification_icon != 0) {
                    nameTextView[a].setLeftDrawableOutside(true);
                    nameTextView[a].setLeftDrawable(getBotVerificationDrawable(chat.bot_verification_icon, false, a));
                } else {
                    nameTextView[a].setLeftDrawable(null);
                }
                if (a == 0 && onlineTextOverride != null) {
                    onlineTextView[a].setText(onlineTextOverride);
                } else {
                    if (copyFromChatActivity || (currentChat.megagroup && chatInfo != null && onlineCount > 0) || isTopic) {
                        onlineTextView[a].setText(a == 0 ? statusString : profileStatusString);
                    } else if (a == 0 && ChatObject.isChannel(currentChat) && chatInfo != null && chatInfo.participants_count != 0 && (currentChat.megagroup || currentChat.broadcast)) {
                        int[] result = new int[1];
                        boolean ignoreShort = AndroidUtilities.isAccessibilityScreenReaderEnabled();
                        String shortNumber = ignoreShort ? String.valueOf(result[0] = chatInfo.participants_count) : LocaleController.formatShortNumber(chatInfo.participants_count, result);
                        if (currentChat.megagroup) {
                            if (chatInfo.participants_count == 0) {
                                if (chat.has_geo) {
                                    onlineTextView[a].setText(LocaleController.getString(R.string.MegaLocation).toLowerCase());
                                } else if (ChatObject.isPublic(chat)) {
                                    onlineTextView[a].setText(LocaleController.getString(R.string.MegaPublic).toLowerCase());
                                } else {
                                    onlineTextView[a].setText(LocaleController.getString(R.string.MegaPrivate).toLowerCase());
                                }
                            } else {
                                onlineTextView[a].setText(LocaleController.formatPluralString("Members", result[0]).replace(String.format("%d", result[0]), shortNumber));
                            }
                        } else {
                            onlineTextView[a].setText(LocaleController.formatPluralString("Subscribers", result[0]).replace(String.format("%d", result[0]), shortNumber));
                        }
                    } else {
                        onlineTextView[a].setText(a == 0 ? statusString : profileStatusString);
                    }
                }
                if (a == 1 && isTopic) {
                    if (profileStatusIsButton) {
                        onlineTextView[a].setOnClickListener(e -> goToForum());
                    } else {
                        onlineTextView[a].setOnClickListener(null);
                        onlineTextView[a].setClickable(false);
                    }
                }
            }
            if (changed) {
                checkLayout();
            }

            TLRPC.FileLocation photoBig = null;
            if (chat.photo != null && !isTopic) {
                photoBig = chat.photo.photo_big;
            }

            final ImageLocation imageLocation;
            final ImageLocation thumbLocation;
            final ImageLocation videoLocation;
            if (isTopic) {
                imageLocation = null;
                thumbLocation = null;
                videoLocation = null;
                ForumUtilities.setTopicIcon(avatarImage, topic, true, true, resourcesProvider);
            } else if (ChatObject.isMonoForum(currentChat)) {
                TLRPC.Chat channel = getMessagesController().getMonoForumLinkedChat(currentChat.id);
                avatarDrawable.setInfo(currentAccount, channel);
                imageLocation = ImageLocation.getForUserOrChat(channel, ImageLocation.TYPE_BIG);
                thumbLocation = ImageLocation.getForUserOrChat(channel, ImageLocation.TYPE_SMALL);
                videoLocation = avatarsViewPager.getCurrentVideoLocation(thumbLocation, imageLocation);
            } else {
                avatarDrawable.setInfo(currentAccount, chat);
                imageLocation = ImageLocation.getForUserOrChat(chat, ImageLocation.TYPE_BIG);
                thumbLocation = ImageLocation.getForUserOrChat(chat, ImageLocation.TYPE_SMALL);
                videoLocation = avatarsViewPager.getCurrentVideoLocation(thumbLocation, imageLocation);
            }

            boolean initied = avatarsViewPager.initIfEmpty(null, imageLocation, thumbLocation, reload);
            if ((imageLocation == null || initied) && isPulledDown) {
                final View view = layoutManager.findViewByPosition(0);
                if (view != null) {
                    listView.smoothScrollBy(0, view.getTop() - AndroidUtilities.dp(88), CubicBezierInterpolator.EASE_OUT_QUINT);
                }
            }
            String filter;
            if (videoLocation != null && videoLocation.imageType == FileLoader.IMAGE_TYPE_ANIMATION) {
                filter = ImageLoader.AUTOPLAY_FILTER;
            } else {
                filter = null;
            }
            if (avatarBig == null && !isTopic) {
                avatarImage.setImage(videoLocation, filter, thumbLocation, "50_50", avatarDrawable, chat);
            }
            if (imageLocation != null && (prevLoadedImageLocation == null || imageLocation.photoId != prevLoadedImageLocation.photoId)) {
                prevLoadedImageLocation = imageLocation;
                getFileLoader().loadFile(imageLocation, chat, null, FileLoader.PRIORITY_LOW, 1);
            }
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.isShowingImage(photoBig) && (getLastStoryViewer() == null || getLastStoryViewer().transitionViewHolder.view != avatarImage), storyView != null);
        }

//        if (qrItem != null) {
//            updateQrItemVisibility(true);
//        }
        AndroidUtilities.runOnUIThread(this::updateEmojiStatusEffectPosition);
    }

    public void goToForum() {
        if (getParentLayout() != null && getParentLayout().getFragmentStack() != null) {
            for (int i = 0; i < getParentLayout().getFragmentStack().size(); ++i) {
                BaseFragment fragment = getParentLayout().getFragmentStack().get(i);
                if (fragment instanceof DialogsActivity) {
                    if (((DialogsActivity) fragment).rightSlidingDialogContainer != null) {
                        BaseFragment previewFragment = ((DialogsActivity) fragment).rightSlidingDialogContainer.getFragment();
                        if (previewFragment instanceof TopicsFragment && ((TopicsFragment) previewFragment).getDialogId() == getDialogId()) {
                            ((DialogsActivity) fragment).rightSlidingDialogContainer.finishPreview();
                        }
                    }
                } else if (fragment instanceof ChatActivity) {
                    if (((ChatActivity) fragment).getDialogId() == getDialogId()) {
                        getParentLayout().removeFragmentFromStack(fragment);
                        i--;
                    }
                } else if (fragment instanceof TopicsFragment) {
                    if (((TopicsFragment) fragment).getDialogId() == getDialogId()) {
                        getParentLayout().removeFragmentFromStack(fragment);
                        i--;
                    }
                } else if (fragment instanceof DebugProfile) {
                    if (fragment != this && ((DebugProfile) fragment).getDialogId() == getDialogId() && ((DebugProfile) fragment).isTopic) {
                        getParentLayout().removeFragmentFromStack(fragment);
                        i--;
                    }
                }
            }
        }

        playProfileAnimation = 0;

        Bundle args = new Bundle();
        args.putLong("chat_id", chatId);
        presentFragment(TopicsFragment.getTopicsOrChat(this, args));
    }

    public void showStatusSelect() {
        if (selectAnimatedEmojiDialog != null) {
            return;
        }
        final SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow[] popup = new SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow[1];
        int xoff, yoff;
        getEmojiStatusLocation(AndroidUtilities.rectTmp2);
        int topMarginDp = nameTextView[1].getScaleX() < 1.5f ? 16 : 32;
        yoff = -(contentView.getHeight() - AndroidUtilities.rectTmp2.centerY()) - AndroidUtilities.dp(topMarginDp);
        int popupWidth = (int) Math.min(AndroidUtilities.dp(340 - 16), AndroidUtilities.displaySize.x * .95f);
        int ecenter = AndroidUtilities.rectTmp2.centerX();
        xoff = MathUtils.clamp(ecenter - popupWidth / 2, 0, AndroidUtilities.displaySize.x - popupWidth);
        ecenter -= xoff;
        SelectAnimatedEmojiDialog popupLayout = new SelectAnimatedEmojiDialog(this, getContext(), true, Math.max(0, ecenter), currentChat == null ? SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS : SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS_CHANNEL, true, resourcesProvider, topMarginDp) {
            @Override
            protected boolean willApplyEmoji(View view, Long documentId, TLRPC.Document document, TL_stars.TL_starGiftUnique gift, Integer until) {
                if (gift != null) {
                    final TL_stars.SavedStarGift savedStarGift = StarsController.getInstance(currentAccount).findUserStarGift(gift.id);
                    return savedStarGift == null || MessagesController.getGlobalMainSettings().getInt("statusgiftpage", 0) >= 2;
                }
                return true;
            }

            @Override
            public long getDialogId() {
                return DebugProfile.this.getDialogId();
            }

            @Override
            protected void onEmojiSelected(View emojiView, Long documentId, TLRPC.Document document, TL_stars.TL_starGiftUnique gift, Integer until) {
                final TLRPC.EmojiStatus emojiStatus;
                if (gift != null) {
                    final TL_stars.SavedStarGift savedStarGift = StarsController.getInstance(currentAccount).findUserStarGift(gift.id);
                    if (savedStarGift != null && MessagesController.getGlobalMainSettings().getInt("statusgiftpage", 0) < 2) {
                        MessagesController.getGlobalMainSettings().edit().putInt("statusgiftpage", MessagesController.getGlobalMainSettings().getInt("statusgiftpage", 0) + 1).apply();
                        new StarGiftSheet(getContext(), currentAccount, UserConfig.getInstance(currentAccount).getClientUserId(), resourcesProvider)
                                .set(savedStarGift, null)
                                .setupWearPage()
                                .show();
                        if (popup[0] != null) {
                            selectAnimatedEmojiDialog = null;
                            popup[0].dismiss();
                        }
                        return;
                    }
                    final TLRPC.TL_inputEmojiStatusCollectible status = new TLRPC.TL_inputEmojiStatusCollectible();
                    status.collectible_id = gift.id;
                    if (until != null) {
                        status.flags |= 1;
                        status.until = until;
                    }
                    emojiStatus = status;
                } else if (documentId == null) {
                    emojiStatus = new TLRPC.TL_emojiStatusEmpty();
                } else {
                    final TLRPC.TL_emojiStatus status = new TLRPC.TL_emojiStatus();
                    status.document_id = documentId;
                    if (until != null) {
                        status.flags |= 1;
                        status.until = until;
                    }
                    emojiStatus = status;
                }
                emojiStatusGiftId = gift != null ? gift.id : null;
                getMessagesController().updateEmojiStatus(currentChat == null ? 0 : -currentChat.id, emojiStatus, gift);
                for (int a = 0; a < 2; ++a) {
                    if (emojiStatusDrawable[a] != null) {
                        if (documentId == null && currentChat == null) {
                            emojiStatusDrawable[a].set(getPremiumCrossfadeDrawable(a), true);
                        } else if (documentId != null) {
                            emojiStatusDrawable[a].set(documentId, true);
                        } else {
                            emojiStatusDrawable[a].set((Drawable) null, true);
                        }
                        emojiStatusDrawable[a].setParticles(gift != null, true);
                    }
                }
                if (documentId != null) {
                    animatedStatusView.animateChange(ReactionsLayoutInBubble.VisibleReaction.fromCustomEmoji(documentId));
                }
                updateEmojiStatusDrawableColor();
                updateEmojiStatusEffectPosition();
                if (popup[0] != null) {
                    selectAnimatedEmojiDialog = null;
                    popup[0].dismiss();
                }
            }
        };
        TLRPC.User user = getMessagesController().getUser(userId);
        if (user != null) {
            popupLayout.setExpireDateHint(DialogObject.getEmojiStatusUntil(user.emoji_status));
        }
        if (emojiStatusGiftId != null) {
            popupLayout.setSelected(emojiStatusGiftId);
        } else {
            popupLayout.setSelected(emojiStatusDrawable[1] != null && emojiStatusDrawable[1].getDrawable() instanceof AnimatedEmojiDrawable ? ((AnimatedEmojiDrawable) emojiStatusDrawable[1].getDrawable()).getDocumentId() : null);
        }
        popupLayout.setSaveState(3);
        popupLayout.setScrimDrawable(emojiStatusDrawable[1], nameTextView[1]);
        popup[0] = selectAnimatedEmojiDialog = new SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT) {
            @Override
            public void dismiss() {
                super.dismiss();
                selectAnimatedEmojiDialog = null;
            }
        };
        int[] loc = new int[2];
        if (nameTextView[1] != null) {
            nameTextView[1].getLocationOnScreen(loc);
        }
        popup[0].showAsDropDown(fragmentView, xoff, yoff, Gravity.TOP | Gravity.LEFT);
        popup[0].dimBehind();
    }

    public void getEmojiStatusLocation(Rect rect) {
        if (nameTextView[1] == null) {
            return;
        }
        if (nameTextView[1].getRightDrawable() == null) {
            rect.set(nameTextView[1].getWidth() - 1, nameTextView[1].getHeight() / 2 - 1, nameTextView[1].getWidth() + 1, nameTextView[1].getHeight() / 2 + 1);
            return;
        }
        rect.set(nameTextView[1].getRightDrawable().getBounds());
        rect.offset((int) (rect.centerX() * (nameTextView[1].getScaleX() - 1f)), 0);
        rect.offset((int) nameTextView[1].getX(), (int) nameTextView[1].getY());
    }

    private Drawable getBotVerificationDrawable(long icon, boolean animated, int a) {
        if (botVerificationDrawable[a] == null) {
            botVerificationDrawable[a] = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(nameTextView[a], AndroidUtilities.dp(17), a == 0 ? AnimatedEmojiDrawable.CACHE_TYPE_EMOJI_STATUS : AnimatedEmojiDrawable.CACHE_TYPE_KEYBOARD);
            botVerificationDrawable[a].offset(0, dp(1));
            if (fragmentViewAttached) {
                botVerificationDrawable[a].attach();
            }
        }
        if (icon != 0) {
            botVerificationDrawable[a].set(icon, animated);
        } else {
            botVerificationDrawable[a].set((Drawable) null, animated);
        }
        updateEmojiStatusDrawableColor();
        return botVerificationDrawable[a];
    }

    private void updateEmojiStatusDrawableColor() {
        updateEmojiStatusDrawableColor(lastEmojiStatusProgress);
    }

    private void updateEmojiStatusDrawableColor(float progress) {
        for (int a = 0; a < 2; ++a) {
            final int fromColor;
            if (peerColor != null && a == 1) {
                fromColor = ColorUtils.blendARGB(peerColor.getStoryColor1(Theme.isCurrentThemeDark()), 0xFFFFFFFF, 0.25f);
            } else {
                fromColor = AndroidUtilities.getOffsetColor(getThemedColor(Theme.key_profile_verifiedBackground), getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress, 1.0f);
            }
            final int color = ColorUtils.blendARGB(ColorUtils.blendARGB(fromColor, 0xffffffff, progress), getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress);
            if (emojiStatusDrawable[a] != null) {
                emojiStatusDrawable[a].setColor(color);
            }
            if (botVerificationDrawable[a] != null) {
                botVerificationDrawable[a].setColor(ColorUtils.blendARGB(ColorUtils.blendARGB(fromColor, 0x99ffffff, progress), getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress));
            }
            if (a == 1) {
                animatedStatusView.setColor(color);
            }
        }
        lastEmojiStatusProgress = progress;
    }

    private Drawable getEmojiStatusDrawable(TLRPC.EmojiStatus emojiStatus, boolean switchable, boolean animated, int a) {
        if (emojiStatusDrawable[a] == null) {
            emojiStatusDrawable[a] = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(nameTextView[a], AndroidUtilities.dp(24), a == 0 ? AnimatedEmojiDrawable.CACHE_TYPE_EMOJI_STATUS : AnimatedEmojiDrawable.CACHE_TYPE_KEYBOARD);
            if (fragmentViewAttached) {
                emojiStatusDrawable[a].attach();
            }
        }
        if (a == 1) {
            emojiStatusGiftId = null;
        }
        if (emojiStatus instanceof TLRPC.TL_emojiStatus) {
            final TLRPC.TL_emojiStatus status = (TLRPC.TL_emojiStatus) emojiStatus;
            if ((status.flags & 1) == 0 || status.until > (int) (System.currentTimeMillis() / 1000)) {
                emojiStatusDrawable[a].set(status.document_id, animated);
                emojiStatusDrawable[a].setParticles(false, animated);
            } else {
                emojiStatusDrawable[a].set(getPremiumCrossfadeDrawable(a), animated);
                emojiStatusDrawable[a].setParticles(false, animated);
            }
        } else if (emojiStatus instanceof TLRPC.TL_emojiStatusCollectible) {
            final TLRPC.TL_emojiStatusCollectible status = (TLRPC.TL_emojiStatusCollectible) emojiStatus;
            if ((status.flags & 1) == 0 || status.until > (int) (System.currentTimeMillis() / 1000)) {
                if (a == 1) {
                    emojiStatusGiftId = status.collectible_id;
                }
                emojiStatusDrawable[a].set(status.document_id, animated);
                emojiStatusDrawable[a].setParticles(true, animated);
            } else {
                emojiStatusDrawable[a].set(getPremiumCrossfadeDrawable(a), animated);
                emojiStatusDrawable[a].setParticles(false, animated);
            }
        } else {
            emojiStatusDrawable[a].set(getPremiumCrossfadeDrawable(a), animated);
            emojiStatusDrawable[a].setParticles(false, animated);
        }
        updateEmojiStatusDrawableColor();
        return emojiStatusDrawable[a];
    }

    private Drawable getPremiumCrossfadeDrawable(int a) {
        if (premiumCrossfadeDrawable[a] == null) {
            premiumStarDrawable[a] = ContextCompat.getDrawable(getParentActivity(), R.drawable.msg_premium_liststar).mutate();
            int color = getThemedColor(Theme.key_profile_verifiedBackground);
            if (a == 1) {
                color = dontApplyPeerColor(color);
            }
            premiumStarDrawable[a].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            premiumCrossfadeDrawable[a] = new CrossfadeDrawable(premiumStarDrawable[a], ContextCompat.getDrawable(getParentActivity(), R.drawable.msg_premium_prolfilestar).mutate());
        }
        return premiumCrossfadeDrawable[a];
    }

    private Drawable getVerifiedCrossfadeDrawable(int a) {
        if (verifiedCrossfadeDrawable[a] == null) {
            verifiedDrawable[a] = Theme.profile_verifiedDrawable.getConstantState().newDrawable().mutate();
            verifiedCheckDrawable[a] = Theme.profile_verifiedCheckDrawable.getConstantState().newDrawable().mutate();
            if (a == 1 && peerColor != null) {
                int color = Theme.adaptHSV(peerColor.hasColor6(Theme.isCurrentThemeDark()) ? peerColor.getColor5() : peerColor.getColor3(), +.1f, Theme.isCurrentThemeDark() ? -.1f : -.08f);
                verifiedDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color, getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress, 1.0f), PorterDuff.Mode.MULTIPLY);
                color = Color.WHITE;
                verifiedCheckDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color, getThemedColor(Theme.key_windowBackgroundWhite), mediaHeaderAnimationProgress, 1.0f), PorterDuff.Mode.MULTIPLY);
            }
            verifiedCrossfadeDrawable[a] = new CrossfadeDrawable(
                    new CombinedDrawable(verifiedDrawable[a], verifiedCheckDrawable[a]),
                    ContextCompat.getDrawable(getParentActivity(), R.drawable.verified_profile)
            );
        }
        return verifiedCrossfadeDrawable[a];
    }

    private Drawable getScamDrawable(int type) {
        if (scamDrawable == null) {
            scamDrawable = new ScamDrawable(11, type);
            scamDrawable.setColor(getThemedColor(Theme.key_avatar_subtitleInProfileBlue));
        }
        return scamDrawable;
    }

    private Drawable getLockIconDrawable() {
        if (lockIconDrawable == null) {
            lockIconDrawable = Theme.chat_lockIconDrawable.getConstantState().newDrawable().mutate();
        }
        return lockIconDrawable;
    }

    private int applyPeerColor(int color, boolean actionBar, Boolean online) {
        if (!actionBar && isSettings()) return color;
        if (peerColor != null) {
            if (!actionBar) {
                int index = adaptedColors.indexOfKey(color);
                if (index < 0) {
                    final int baseColor = Theme.adaptHSV(peerColor.getBgColor1(Theme.isCurrentThemeDark()), Theme.isCurrentThemeDark() ? 0 : +.05f, Theme.isCurrentThemeDark() ? -.1f : -.04f);
                    int adapted = OKLCH.adapt(color, baseColor);
                    adaptedColors.put(color, adapted);
                    return adapted;
                } else {
                    return adaptedColors.valueAt(index);
                }
            }
            final int baseColor = getThemedColor(actionBar ? Theme.key_actionBarDefault : Theme.key_windowBackgroundWhiteBlueIcon);
            final int storyColor = ColorUtils.blendARGB(peerColor.getStoryColor1(Theme.isCurrentThemeDark()), peerColor.getStoryColor2(Theme.isCurrentThemeDark()), .5f);
            int accentColor = actionBar ? storyColor : peerColor.getBgColor1(Theme.isCurrentThemeDark());
            if (!Theme.hasHue(baseColor)) {
                return online != null && !online ? Theme.adaptHSV(Theme.multAlpha(storyColor, .7f), -.2f, +.2f) : storyColor;
            }
            return Theme.changeColorAccent(baseColor, accentColor, color, Theme.isCurrentThemeDark(), online != null && !online ? Theme.multAlpha(storyColor, .7f) : storyColor);
        }
        return color;
    }

    public ShowDrawable getShowStatusButton() {
        if (showStatusButton == null) {
            showStatusButton = new ShowDrawable(LocaleController.getString(R.string.StatusHiddenShow));
            showStatusButton.setAlpha((int) (0xFF * Math.min(1f, (float) topScroll / minimizedOffset)));
            showStatusButton.setBackgroundColor(ColorUtils.blendARGB(Theme.multAlpha(Theme.adaptHSV(actionBarBackgroundColor, +0.18f, -0.1f), 0.5f), 0x23ffffff, currentExpandAnimatorValue));
        }
        return showStatusButton;
    }

    private void updateEmojiStatusEffectPosition() {
        // TODO
    }

    public TLRPC.UserFull getUserInfo() {
        return userInfo;
    }

    private void updatedPeerColor() {
        adaptedColors.clear();
        if (topView != null) {
            topView.setBackgroundColorId(peerColor, true);
        }
        // TODO
//        if (onlineTextView[1] != null) {
//            int statusColor;
//            if (onlineTextView[1].getTag() instanceof Integer) {
//                statusColor = getThemedColor((Integer) onlineTextView[1].getTag());
//            } else {
//                statusColor = getThemedColor(Theme.key_avatar_subtitleInProfileBlue);
//            }
//            onlineTextView[1].setTextColor(ColorUtils.blendARGB(applyPeerColor(statusColor, true, isOnline[0]), 0xB3FFFFFF, currentExpandAnimatorValue));
//        }
//        if (showStatusButton != null) {
//            showStatusButton.setBackgroundColor(ColorUtils.blendARGB(Theme.multAlpha(Theme.adaptHSV(actionBarBackgroundColor, +0.18f, -0.1f), 0.5f), 0x23ffffff, currentExpandAnimatorValue));
//        }
        if (actionBar != null) {
            // TODO progress 1f
            actionBar.setItemsColor(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon), getThemedColor(Theme.key_actionBarActionModeDefaultIcon), 1f), false);
            actionBar.setItemsBackgroundColor(ColorUtils.blendARGB(peerColor != null ? Theme.ACTION_BAR_WHITE_SELECTOR_COLOR : peerColor != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), getThemedColor(Theme.key_actionBarActionModeDefaultSelector), 1f), false);
        }
//        if (verifiedDrawable[1] != null) {
//            final int color1 = peerColor != null ? Theme.adaptHSV(ColorUtils.blendARGB(peerColor.getColor2(), peerColor.hasColor6(Theme.isCurrentThemeDark()) ? peerColor.getColor5() : peerColor.getColor3(), .4f), +.1f, Theme.isCurrentThemeDark() ? -.1f : -.08f) : getThemedColor(Theme.key_profile_verifiedBackground);
//            final int color2 = getThemedColor(Theme.key_player_actionBarTitle);
//            verifiedDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, mediaHeaderAnimationProgress, 1.0f), PorterDuff.Mode.MULTIPLY);
//        }
//        if (verifiedCheckDrawable[1] != null) {
//            final int color1 = peerColor != null ? Color.WHITE : dontApplyPeerColor(getThemedColor(Theme.key_profile_verifiedCheck));
//            final int color2 = getThemedColor(Theme.key_windowBackgroundWhite);
//            verifiedCheckDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, mediaHeaderAnimationProgress, 1.0f), PorterDuff.Mode.MULTIPLY);
//        }
//        if (nameTextView[1] != null) {
//            nameTextView[1].setTextColor(ColorUtils.blendARGB(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_profile_title), getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress), Color.WHITE, currentExpandAnimatorValue));
//        }
//        if (autoDeletePopupWrapper != null && autoDeletePopupWrapper.textView != null) {
//            autoDeletePopupWrapper.textView.invalidate();
//        }
//        AndroidUtilities.forEachViews(listView, view -> {
//            if (view instanceof HeaderCell) {
//                ((HeaderCell) view).setTextColor(dontApplyPeerColor(getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader), false));
//            } else if (view instanceof TextDetailCell) {
//                ((TextDetailCell) view).updateColors();
//            } else if (view instanceof TextCell) {
//                ((TextCell) view).updateColors();
//            } else if (view instanceof AboutLinkCell) {
//                ((AboutLinkCell) view).updateColors();
//            } else if (view instanceof NotificationsCheckCell) {
//                ((NotificationsCheckCell) view).getCheckBox().invalidate();
//            } else if (view instanceof ProfileHoursCell) {
//                ((ProfileHoursCell) view).updateColors();
//            } else if (view instanceof ProfileChannelCell) {
//                ((ProfileChannelCell) view).updateColors();
//            }
//        });
        if (sharedMediaLayout != null && sharedMediaLayout.scrollSlidingTextTabStrip != null) {
            sharedMediaLayout.scrollSlidingTextTabStrip.updateColors();
        }
        if (sharedMediaLayout != null && sharedMediaLayout.giftsContainer != null) {
            sharedMediaLayout.giftsContainer.updateColors();
        }
//        writeButtonSetBackground();
//        updateEmojiStatusDrawableColor();
//        if (storyView != null) {
//            storyView.update();
//        }
//        if (giftsView != null) {
//            giftsView.update();
//        }
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

    @SuppressWarnings("unchecked")
    @Override
    public void didReceivedNotification(int id, int account, final Object... args) {
        if (id == NotificationCenter.uploadStoryEnd || id == NotificationCenter.chatWasBoostedByUser) {
            checkCanSendStoryForPosting();
        } else if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            boolean infoChanged = (mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0 || (mask & MessagesController.UPDATE_MASK_EMOJI_STATUS) != 0;
            if (userId != 0) {
                if (infoChanged) {
                    updateProfileData(true);
                }
                if ((mask & MessagesController.UPDATE_MASK_PHONE) != 0) {
                    if (listView != null) {
                        RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findViewHolderForPosition(phoneRow);
                        if (holder != null) {
                            listAdapter.onBindViewHolder(holder, phoneRow);
                        }
                    }
                }
            } else if (chatId != 0) {
                if ((mask & MessagesController.UPDATE_MASK_CHAT) != 0 || (mask & MessagesController.UPDATE_MASK_CHAT_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_CHAT_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_CHAT_MEMBERS) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0 || (mask & MessagesController.UPDATE_MASK_EMOJI_STATUS) != 0) {
                    if ((mask & MessagesController.UPDATE_MASK_CHAT) != 0) {
                        updateListAnimated(true);
                    } else {
                        updateOnlineCount(true);
                    }
                    updateProfileData(true);
                }
                if (infoChanged) {
                    if (listView != null) {
                        int count = listView.getChildCount();
                        for (int a = 0; a < count; a++) {
                            View child = listView.getChildAt(a);
                            if (child instanceof UserCell) {
                                ((UserCell) child).update(mask);
                            }
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.chatOnlineCountDidLoad) {
            Long chatId = (Long) args[0];
            if (chatInfo == null || currentChat == null || currentChat.id != chatId) {
                return;
            }
            chatInfo.online_count = (Integer) args[1];
            updateOnlineCount(true);
            updateProfileData(false);
        } else if (id == NotificationCenter.contactsDidLoad || id == NotificationCenter.channelRightsUpdated) {
            createActionBarMenu(true);
        } else if (id == NotificationCenter.encryptedChatCreated) {
            AndroidUtilities.runOnUIThread(() -> {
                getNotificationCenter().removeObserver(DebugProfile.this, NotificationCenter.closeChats);
                getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                TLRPC.EncryptedChat encryptedChat = (TLRPC.EncryptedChat) args[0];
                Bundle args2 = new Bundle();
                args2.putInt("enc_id", encryptedChat.id);
                presentFragment(new ChatActivity(args2), true);
            });
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            TLRPC.EncryptedChat chat = (TLRPC.EncryptedChat) args[0];
            if (currentEncryptedChat != null && chat.id == currentEncryptedChat.id) {
                currentEncryptedChat = chat;
                updateListAnimated(false);
                if (flagSecure != null) {
                    flagSecure.invalidate();
                }
            }
        } else if (id == NotificationCenter.blockedUsersDidLoad) {
            boolean oldValue = userBlocked;
            userBlocked = getMessagesController().blockePeers.indexOfKey(userId) >= 0;
            if (oldValue != userBlocked) {
                createActionBarMenu(true);
                updateListAnimated(false);
            }
        } else if (id == NotificationCenter.groupCallUpdated) {
            Long chatId = (Long) args[0];
            if (currentChat != null && chatId == currentChat.id && ChatObject.canManageCalls(currentChat)) {
                TLRPC.ChatFull chatFull = MessagesController.getInstance(currentAccount).getChatFull(chatId);
                if (chatFull != null) {
                    if (chatInfo != null) {
                        chatFull.participants = chatInfo.participants;
                    }
                    chatInfo = chatFull;
                }
                if (sharedMediaLayout != null) {
                    sharedMediaLayout.setChatInfo(chatInfo);
                }
                if (chatInfo != null && (chatInfo.call == null && !hasVoiceChatItem || chatInfo.call != null && hasVoiceChatItem)) {
                    createActionBarMenu(false);
                }
                if (storyView != null && chatInfo != null) {
                    storyView.setStories(chatInfo.stories);
                }
                if (giftsView != null) {
                    giftsView.update();
                }
                if (avatarImage != null) {
                    avatarImage.setHasStories(needInsetForStories());
                }
                if (chatId != 0) {
                    otherItem.setSubItemShown(gift_premium, !BuildVars.IS_BILLING_UNAVAILABLE && !getMessagesController().premiumPurchaseBlocked() && chatInfo != null && chatInfo.stargifts_available);
                }
            }
        } else if (id == NotificationCenter.chatInfoDidLoad) {
            final TLRPC.ChatFull chatFull = (TLRPC.ChatFull) args[0];
            if (chatFull.id == chatId) {
                final boolean byChannelUsers = (Boolean) args[2];
                if (chatInfo instanceof TLRPC.TL_channelFull) {
                    if (chatFull.participants == null) {
                        chatFull.participants = chatInfo.participants;
                    }
                }
                final boolean loadChannelParticipants = chatInfo == null && chatFull instanceof TLRPC.TL_channelFull;
                chatInfo = chatFull;
                if (mergeDialogId == 0 && chatInfo.migrated_from_chat_id != 0) {
                    mergeDialogId = -chatInfo.migrated_from_chat_id;
                    getMediaDataController().getMediaCount(mergeDialogId, topicId, MediaDataController.MEDIA_PHOTOVIDEO, classGuid, true);
                }
                fetchUsersFromChannelInfo();
                if (avatarsViewPager != null && !isTopic) {
                    avatarsViewPager.setChatInfo(chatInfo);
                }
                updateListAnimated(true);
                TLRPC.Chat newChat = getMessagesController().getChat(chatId);
                if (newChat != null) {
                    currentChat = newChat;
                    createActionBarMenu(true);
                }
                if (flagSecure != null) {
                    flagSecure.invalidate();
                }
                if (currentChat.megagroup && (loadChannelParticipants || !byChannelUsers)) {
                    getChannelParticipants(true);
                }

                updateAutoDeleteItem();
                updateTtlIcon();
                if (storyView != null && chatInfo != null) {
                    storyView.setStories(chatInfo.stories);
                }
                if (giftsView != null) {
                    giftsView.update();
                }
                if (avatarImage != null) {
                    avatarImage.setHasStories(needInsetForStories());
                }
                if (sharedMediaLayout != null) {
                    sharedMediaLayout.setChatInfo(chatInfo);
                }
            }
        } else if (id == NotificationCenter.closeChats) {
            removeSelfFromStack(true);
        } else if (id == NotificationCenter.botInfoDidLoad) {
            final TL_bots.BotInfo info = (TL_bots.BotInfo) args[0];
            if (info.user_id == userId) {
                botInfo = info;
                updateListAnimated(false);
            }
        } else if (id == NotificationCenter.userInfoDidLoad) {
            final long uid = (Long) args[0];
            if (uid == userId) {
                userInfo = (TLRPC.UserFull) args[1];
                if (storyView != null) {
                    storyView.setStories(userInfo.stories);
                }
                if (giftsView != null) {
                    giftsView.update();
                }
                if (avatarImage != null) {
                    avatarImage.setHasStories(needInsetForStories());
                }
                if (sharedMediaLayout != null) {
                    sharedMediaLayout.setUserInfo(userInfo);
                }
                if (imageUpdater != null) {
                    if (listAdapter != null && !TextUtils.equals(userInfo.about, currentBio)) {
                        listAdapter.notifyItemChanged(bioRow);
                    }
                } else {
                    // TODO
//                    if (!openAnimationInProgress && !callItemVisible) {
                    createActionBarMenu(true);
//                    } else {
//                        recreateMenuAfterAnimation = true;
//                    }
                    updateListAnimated(false);
                    if (sharedMediaLayout != null) {
                        sharedMediaLayout.setCommonGroupsCount(userInfo.common_chats_count);
                        updateSelectedMediaTabText();
                        if (sharedMediaPreloader == null || sharedMediaPreloader.isMediaWasLoaded()) {
                            resumeDelayedFragmentAnimation();
                            checkLayout();
                        }
                    }
                }
                updateAutoDeleteItem();
                updateTtlIcon();
                if (profileChannelMessageFetcher == null && !isSettings()) {
                    profileChannelMessageFetcher = new ProfileChannelCell.ChannelMessageFetcher(currentAccount);
                    profileChannelMessageFetcher.subscribe(() -> updateListAnimated(false));
                    profileChannelMessageFetcher.fetch(userInfo);
                }
                if (!isSettings()) {
                    ProfileBirthdayEffect.BirthdayEffectFetcher oldFetcher = birthdayFetcher;
                    birthdayFetcher = ProfileBirthdayEffect.BirthdayEffectFetcher.of(currentAccount, userInfo, birthdayFetcher);
                    createdBirthdayFetcher = birthdayFetcher != oldFetcher;
                    if (birthdayFetcher != null) {
                        birthdayFetcher.subscribe(this::createBirthdayEffect);
                    }
                }
                if (otherItem != null) {
                    if (hasPrivacyCommand()) {
                        otherItem.showSubItem(bot_privacy);
                    } else {
                        otherItem.hideSubItem(bot_privacy);
                    }
                }
            }
        } else if (id == NotificationCenter.privacyRulesUpdated) {
            // TODO
//            if (qrItem != null) {
//                updateQrItemVisibility(true);
//            }
        } else if (id == NotificationCenter.didReceiveNewMessages) {
            final boolean scheduled = (Boolean) args[2];
            if (scheduled) {
                return;
            }
            final long did = getDialogId();
            if (did == (Long) args[0]) {
                boolean enc = DialogObject.isEncryptedDialog(did);
                ArrayList<MessageObject> arr = (ArrayList<MessageObject>) args[1];
                for (int a = 0; a < arr.size(); a++) {
                    MessageObject obj = arr.get(a);
                    if (currentEncryptedChat != null && obj.messageOwner.action instanceof TLRPC.TL_messageEncryptedAction && obj.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL) {
                        TLRPC.TL_decryptedMessageActionSetMessageTTL action = (TLRPC.TL_decryptedMessageActionSetMessageTTL) obj.messageOwner.action.encryptedAction;
                        if (listAdapter != null) {
                            listAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.emojiLoaded) {
            if (listView != null) {
                listView.invalidateViews();
            }
        } else if (id == NotificationCenter.reloadInterface) {
            updateListAnimated(false);
        } else if (id == NotificationCenter.newSuggestionsAvailable) {
            final int prevRow1 = passwordSuggestionRow;
            final int prevRow2 = phoneSuggestionRow;
            final int prevRow3 = graceSuggestionRow;
            updateRowsIds();
            if (prevRow1 != passwordSuggestionRow || prevRow2 != phoneSuggestionRow || prevRow3 != graceSuggestionRow) {
                listAdapter.notifyDataSetChanged();
            }
        } else if (id == NotificationCenter.topicsDidLoaded) {
            if (isTopic) {
                updateProfileData(false);
            }
        } else if (id == NotificationCenter.updateSearchSettings) {
            // TODO`
//            if (searchAdapter != null) {
//                searchAdapter.searchArray = searchAdapter.onCreateSearchArray();
//                searchAdapter.recentSearches.clear();
//                searchAdapter.updateSearchArray();
//                searchAdapter.search(searchAdapter.lastSearchString);
//            }
        } else if (id == NotificationCenter.reloadDialogPhotos) {
            updateProfileData(false);
        } else if (id == NotificationCenter.storiesUpdated || id == NotificationCenter.storiesReadUpdated) {
            if (avatarImage != null) {
                avatarImage.setHasStories(needInsetForStories());
                updateAvatarRoundRadius();
            }
            if (storyView != null) {
                if (userInfo != null) {
                    storyView.setStories(userInfo.stories);
                } else if (chatInfo != null) {
                    storyView.setStories(chatInfo.stories);
                }
            }
        } else if (id == NotificationCenter.userIsPremiumBlockedUpadted) {
            if (otherItem != null) {
                otherItem.setSubItemShown(start_secret_chat, DialogObject.isEmpty(getMessagesController().isUserContactBlocked(userId)));
            }
            updateEditColorIcon();
        } else if (id == NotificationCenter.currentUserPremiumStatusChanged) {
            updateEditColorIcon();
        } else if (id == NotificationCenter.starBalanceUpdated) {
            updateListAnimated(false);
        } else if (id == NotificationCenter.botStarsUpdated) {
            updateListAnimated(false);
        } else if (id == NotificationCenter.botStarsTransactionsLoaded) {
            updateListAnimated(false);
        } else if (id == NotificationCenter.dialogDeleted) {
            final long dialogId = (long) args[0];
            if (getDialogId() == dialogId) {
                if (parentLayout != null && parentLayout.getLastFragment() == this) {
                    finishFragment();
                } else {
                    removeSelfFromStack();
                }
            }
        } else if (id == NotificationCenter.channelRecommendationsLoaded) {
            final long dialogId = (long) args[0];
            if (sharedMediaRow < 0 && dialogId == getDialogId()) {
                updateRowsIds();
                updateSelectedMediaTabText();
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
            }
        } else if (id == NotificationCenter.starUserGiftsLoaded) {
            final long dialogId = (long) args[0];
            if (dialogId == getDialogId() && !isSettings()) {
                if (sharedMediaRow < 0) {
                    updateRowsIds();
                    updateSelectedMediaTabText();
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    AndroidUtilities.runOnUIThread(() -> {
                        if (sharedMediaLayout != null) {
                            sharedMediaLayout.updateTabs(true);
                            sharedMediaLayout.updateAdapters();
                        }
                    });
                } else if (sharedMediaLayout != null) {
                    sharedMediaLayout.updateTabs(true);
                }
            }
        }
    }

    private void updateEditColorIcon() {
        if (getContext() == null || editColorItem == null) return;
        if (getUserConfig().isPremium()) {
            editColorItem.setIcon(R.drawable.menu_profile_colors);
        } else {
            Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.menu_profile_colors_locked);
            icon.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_actionBarDefaultSubmenuItemIcon), PorterDuff.Mode.SRC_IN));
            Drawable lockIcon = ContextCompat.getDrawable(getContext(), R.drawable.msg_gallery_locked2);
            lockIcon.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(Color.WHITE, Color.BLACK, 0.5f), PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(icon, lockIcon, dp(1), -dp(1)) {
                @Override
                public void setColorFilter(ColorFilter colorFilter) {
                }
            };
            editColorItem.setIcon(combinedDrawable);
        }
    }

    public boolean hasPrivacyCommand() {
        if (!isBot) return false;
        if (userInfo == null || userInfo.bot_info == null) return false;
        if (userInfo.bot_info.privacy_policy_url != null) return true;
        for (TLRPC.TL_botCommand command : userInfo.bot_info.commands) {
            if ("privacy".equals(command.command)) {
                return true;
            }
        }
        return true;
    }

    private void createBirthdayEffect() {
//        if (fragmentView == null || !fullyVisible || birthdayFetcher == null || getContext() == null)
        if (fragmentView == null || birthdayFetcher == null || getContext() == null) return;

        if (birthdayEffect != null) {
            birthdayEffect.updateFetcher(birthdayFetcher);
            birthdayEffect.invalidate();
            return;
        }

        birthdayEffect = new ProfileBirthdayEffect(this, birthdayFetcher);
        ((FrameLayout) fragmentView).addView(birthdayEffect, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL_HORIZONTAL | Gravity.TOP));
    }

    public boolean isSettings() {
        return imageUpdater != null && !myProfile;
    }

    private void updateTtlIcon() {
        if (ttlIconView == null) {
            return;
        }
        boolean visible = false;
        if (currentEncryptedChat == null) {
            if (userInfo != null && userInfo.ttl_period > 0) {
                visible = true;
            } else if (chatInfo != null && ChatObject.canUserDoAdminAction(currentChat, ChatObject.ACTION_DELETE_MESSAGES) && chatInfo.ttl_period > 0) {
                visible = true;
            }
        }
        AndroidUtilities.updateViewVisibilityAnimated(ttlIconView, visible, 0.8f, fragmentOpened);
    }

    private void updateAutoDeleteItem() {
        if (autoDeleteItem == null || autoDeletePopupWrapper == null) {
            return;
        }
        int ttl = 0;
        if (userInfo != null || chatInfo != null) {
            ttl = userInfo != null ? userInfo.ttl_period : chatInfo.ttl_period;
        }
        autoDeleteItemDrawable.setTime(ttl);
        autoDeletePopupWrapper.updateItems(ttl);
    }

    private void getChannelParticipants(boolean reload) {
        if (loadingUsers || participantsMap == null || chatInfo == null) {
            return;
        }
        loadingUsers = true;
        final int delay = participantsMap.size() != 0 && reload ? 300 : 0;

        final TLRPC.TL_channels_getParticipants req = new TLRPC.TL_channels_getParticipants();
        req.channel = getMessagesController().getInputChannel(chatId);
        req.filter = new TLRPC.TL_channelParticipantsRecent();
        req.offset = reload ? 0 : participantsMap.size();
        req.limit = 200;
        int reqId = getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> getNotificationCenter().doOnIdle(() -> {
            if (error == null) {
                TLRPC.TL_channels_channelParticipants res = (TLRPC.TL_channels_channelParticipants) response;
                getMessagesController().putUsers(res.users, false);
                getMessagesController().putChats(res.chats, false);
                if (res.users.size() < 200) {
                    usersEndReached = true;
                }
                if (req.offset == 0) {
                    participantsMap.clear();
                    chatInfo.participants = new TLRPC.TL_chatParticipants();
                    getMessagesStorage().putUsersAndChats(res.users, res.chats, true, true);
                    getMessagesStorage().updateChannelUsers(chatId, res.participants);
                }
                for (int a = 0; a < res.participants.size(); a++) {
                    TLRPC.TL_chatChannelParticipant participant = new TLRPC.TL_chatChannelParticipant();
                    participant.channelParticipant = res.participants.get(a);
                    participant.inviter_id = participant.channelParticipant.inviter_id;
                    participant.user_id = MessageObject.getPeerId(participant.channelParticipant.peer);
                    participant.date = participant.channelParticipant.date;
                    if (participantsMap.indexOfKey(participant.user_id) < 0) {
                        if (chatInfo.participants == null) {
                            chatInfo.participants = new TLRPC.TL_chatParticipants();
                        }
                        chatInfo.participants.participants.add(participant);
                        participantsMap.put(participant.user_id, participant);
                    }
                }
            }
            loadingUsers = false;
            saveScrollPosition();
            updateListAnimated(true);
        }), delay));
        getConnectionsManager().bindRequestToGuid(reqId, classGuid);
    }

    private void saveScrollPosition() {
        // TODO
    }

    private void fetchUsersFromChannelInfo() {
        if (currentChat == null || !currentChat.megagroup) {
            return;
        }
        if (chatInfo instanceof TLRPC.TL_channelFull && chatInfo.participants != null) {
            for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                TLRPC.ChatParticipant chatParticipant = chatInfo.participants.participants.get(a);
                participantsMap.put(chatParticipant.user_id, chatParticipant);
            }
        }
    }

    public UndoView getUndoView() {
        return undoView;
    }


    public boolean isChat() {
        return chatId != 0;
    }

    public long getTopicId() {
        return topicId;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (sharedMediaLayout != null) {
            sharedMediaLayout.onConfigurationChanged(newConfig);
        }
        invalidateIsInLandscapeMode();
        if (isInLandscapeMode && isPulledDown) {
            final View view = layoutManager.findViewByPosition(0);
            if (view != null) {
                listView.scrollBy(0, view.getTop() - AndroidUtilities.dp(88));
            }
        }
        fixLayout();
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    onScroll();
                    checkLayout();
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if (sharedMediaLayout != null) {
            sharedMediaLayout.onResume();
        }
        invalidateIsInLandscapeMode();
        if (listAdapter != null) {
            saveScrollPosition();
            firstLayout = true;
            listAdapter.notifyDataSetChanged();
        }
        // TODO
//        if (!parentLayout.isInPreviewMode() && blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
//            blurredView.setVisibility(View.GONE);
//            blurredView.setBackground(null);
//        }

        if (imageUpdater != null) {
            imageUpdater.onResume();
            setParentActivityTitle(LocaleController.getString(R.string.Settings));
        }

        updateProfileData(true);
        fixLayout();
//        if (nameTextView[1] != null) {
//            setParentActivityTitle(nameTextView[1].getText());
//        }
//        if (userId != 0) {
//            final TLRPC.User user = getMessagesController().getUser(userId);
//            if (user != null && user.photo == null) {
//                if (extraHeight >= AndroidUtilities.dp(88f)) {
//                    expandAnimator.cancel();
//                    expandAnimatorValues[0] = 1f;
//                    expandAnimatorValues[1] = 0f;
//                    setAvatarExpandProgress(1f);
//                    avatarsViewPager.setVisibility(View.GONE);
//                    extraHeight = AndroidUtilities.dp(88f);
//                    allowPullingDown = false;
//                    layoutManager.scrollToPositionWithOffset(0, AndroidUtilities.dp(88) - listView.getPaddingTop());
//                }
//            }
//        }
        if (flagSecure != null) {
            flagSecure.attach();
        }
        updateItemsUsername();
    }

    private void updateItemsUsername() {
        if (!myProfile || setUsernameItem == null || linkItem == null) return;
        TLRPC.User user = getMessagesController().getUser(userId);
        if (user == null) {
            return;
        }
        final boolean hasUsername = UserObject.getPublicUsername(user) != null;
        setUsernameItem.setIcon(hasUsername ? R.drawable.menu_username_change : R.drawable.menu_username_set);
        setUsernameItem.setText(hasUsername ? getString(R.string.ProfileUsernameEdit) : getString(R.string.ProfileUsernameSet));
        linkItem.setVisibility(UserObject.getPublicUsername(user) != null ? View.VISIBLE : View.GONE);
    }

    private void invalidateIsInLandscapeMode() {
        final Point size = new Point();
        final Display display = getParentActivity().getWindowManager().getDefaultDisplay();
        display.getSize(size);
        isInLandscapeMode = size.x > size.y;
    }

    private void createActionBarMenu(boolean animated) {
        if (actionBar == null || otherItem == null) {
            return;
        }
        Context context = actionBar.getContext();
        otherItem.removeAllSubItems();
//        animatingItem = null;

        editItemVisible = false;
        callItemVisible = false;

        videoCallItemVisible = false;
        canSearchMembers = false;
        boolean selfUser = false;

        if (userId != 0) {
            TLRPC.User user = getMessagesController().getUser(userId);
            if (user == null) {
                return;
            }
            if (UserObject.isUserSelf(user)) {
                editItemVisible = myProfile;
                otherItem.addSubItem(edit_info, R.drawable.msg_edit, LocaleController.getString(R.string.EditInfo));
                if (imageUpdater != null) {
                    otherItem.addSubItem(add_photo, R.drawable.msg_addphoto, LocaleController.getString(R.string.AddPhoto));
                }
                editColorItem = otherItem.addSubItem(edit_color, R.drawable.menu_profile_colors, LocaleController.getString(R.string.ProfileColorEdit));
                updateEditColorIcon();
                if (myProfile) {
                    setUsernameItem = otherItem.addSubItem(set_username, R.drawable.menu_username_change, getString(R.string.ProfileUsernameEdit));
                    linkItem = otherItem.addSubItem(copy_link_profile, R.drawable.msg_link2, getString(R.string.ProfileCopyLink));
                    updateItemsUsername();
                }
                selfUser = true;
            } else {
                if (user.bot && user.bot_can_edit) {
                    editItemVisible = true;
                }

                if (userInfo != null && userInfo.phone_calls_available) {
                    callItemVisible = true;
                    videoCallItemVisible = Build.VERSION.SDK_INT >= 18 && userInfo.video_calls_available;
                }
                if (isBot || getContactsController().contactsDict.get(userId) == null) {
                    if (MessagesController.isSupportUser(user)) {
                        if (userBlocked) {
                            otherItem.addSubItem(block_contact, R.drawable.msg_block, LocaleController.getString(R.string.Unblock));
                        }
                        otherItem.addSubItem(add_shortcut, R.drawable.msg_home, LocaleController.getString(R.string.AddShortcut));
                    } else if (getDialogId() != UserObject.VERIFY) {
                        if (currentEncryptedChat == null) {
                            createAutoDeleteItem(context);
                        }
                        otherItem.addSubItem(add_shortcut, R.drawable.msg_home, LocaleController.getString(R.string.AddShortcut));
                        if (isBot) {
                            otherItem.addSubItem(share, R.drawable.msg_share, LocaleController.getString(R.string.BotShare));
                        } else {
                            otherItem.addSubItem(add_contact, R.drawable.msg_addcontact, LocaleController.getString(R.string.AddContact));
                        }
                        if (!TextUtils.isEmpty(user.phone)) {
                            otherItem.addSubItem(share_contact, R.drawable.msg_share, LocaleController.getString(R.string.ShareContact));
                        }
                        if (isBot) {
                            otherItem.addSubItem(bot_privacy, R.drawable.menu_privacy_policy, getString(R.string.BotPrivacyPolicy));
                            if (hasPrivacyCommand()) {
                                otherItem.showSubItem(bot_privacy);
                            } else {
                                otherItem.hideSubItem(bot_privacy);
                            }
                            otherItem.addSubItem(report, R.drawable.msg_report, LocaleController.getString(R.string.ReportBot)).setColors(getThemedColor(Theme.key_text_RedRegular), getThemedColor(Theme.key_text_RedRegular));
                            if (!userBlocked) {
                                otherItem.addSubItem(block_contact, R.drawable.msg_block2, LocaleController.getString(R.string.DeleteAndBlock)).setColors(getThemedColor(Theme.key_text_RedRegular), getThemedColor(Theme.key_text_RedRegular));
                            } else {
                                otherItem.addSubItem(block_contact, R.drawable.msg_retry, LocaleController.getString(R.string.BotRestart));
                            }
                        } else {
                            otherItem.addSubItem(block_contact, R.drawable.msg_block, !userBlocked ? LocaleController.getString(R.string.BlockContact) : LocaleController.getString(R.string.Unblock));
                        }
                    }
                } else {
                    if (currentEncryptedChat == null) {
                        createAutoDeleteItem(context);
                    }
                    if (!TextUtils.isEmpty(user.phone)) {
                        otherItem.addSubItem(share_contact, R.drawable.msg_share, LocaleController.getString(R.string.ShareContact));
                    }
                    otherItem.addSubItem(block_contact, R.drawable.msg_block, !userBlocked ? LocaleController.getString(R.string.BlockContact) : LocaleController.getString(R.string.Unblock));
                    otherItem.addSubItem(edit_contact, R.drawable.msg_edit, LocaleController.getString(R.string.EditContact));
                    otherItem.addSubItem(delete_contact, R.drawable.msg_delete, LocaleController.getString(R.string.DeleteContact));
                }
                if (!UserObject.isDeleted(user) && !isBot && currentEncryptedChat == null && !userBlocked && userId != 333000 && userId != 777000 && userId != 42777) {
                    if (!BuildVars.IS_BILLING_UNAVAILABLE && !user.self && !user.bot && !MessagesController.isSupportUser(user) && !getMessagesController().premiumPurchaseBlocked()) {
                        StarsController.getInstance(currentAccount).loadStarGifts();
                        otherItem.addSubItem(gift_premium, R.drawable.msg_gift_premium, LocaleController.getString(R.string.ProfileSendAGift));
                    }
                    otherItem.addSubItem(start_secret_chat, R.drawable.msg_secret, LocaleController.getString(R.string.StartEncryptedChat));
                    otherItem.setSubItemShown(start_secret_chat, DialogObject.isEmpty(getMessagesController().isUserContactBlocked(userId)));
                }
                if (!isBot && getContactsController().contactsDict.get(userId) != null) {
                    otherItem.addSubItem(add_shortcut, R.drawable.msg_home, LocaleController.getString(R.string.AddShortcut));
                }
            }
        } else if (chatId != 0) {
            TLRPC.Chat chat = getMessagesController().getChat(chatId);
            hasVoiceChatItem = false;

            if (topicId == 0 && ChatObject.canChangeChatInfo(chat)) {
                createAutoDeleteItem(context);
            }
            if (ChatObject.isChannel(chat)) {
                if (isTopic) {
                    if (ChatObject.canManageTopic(currentAccount, chat, topicId)) {
                        editItemVisible = true;
                    }
                } else {
                    if (ChatObject.hasAdminRights(chat) || chat.megagroup && ChatObject.canChangeChatInfo(chat)) {
                        editItemVisible = true;
                    }
                }
                if (chatInfo != null) {
                    if (ChatObject.canManageCalls(chat) && chatInfo.call == null) {
                        otherItem.addSubItem(call_item, R.drawable.msg_voicechat, chat.megagroup && !chat.gigagroup ? LocaleController.getString(R.string.StartVoipChat) : LocaleController.getString(R.string.StartVoipChannel));
                        hasVoiceChatItem = true;
                    }
                    if ((chatInfo.can_view_stats || chatInfo.can_view_revenue || chatInfo.can_view_stars_revenue || getMessagesController().getStoriesController().canPostStories(getDialogId())) && topicId == 0) {
                        otherItem.addSubItem(statistics, R.drawable.msg_stats, LocaleController.getString(R.string.Statistics));
                    }
                    ChatObject.Call call = getMessagesController().getGroupCall(chatId, false);
                    callItemVisible = call != null;
                }
                if (chat.megagroup) {
                    if (chatInfo == null || !chatInfo.participants_hidden || ChatObject.hasAdminRights(chat)) {
                        canSearchMembers = true;
                        otherItem.addSubItem(search_members, R.drawable.msg_search, LocaleController.getString(R.string.SearchMembers));
                    }
                    if (!chat.creator && !chat.left && !chat.kicked && !isTopic) {
                        otherItem.addSubItem(leave_group, R.drawable.msg_leave, LocaleController.getString(R.string.LeaveMegaMenu));
                    }
                    if (isTopic && ChatObject.canDeleteTopic(currentAccount, chat, topicId)) {
                        otherItem.addSubItem(delete_topic, R.drawable.msg_delete, LocaleController.getPluralString("DeleteTopics", 1));
                    }
                } else {
                    if (chat.creator || chat.admin_rights != null && chat.admin_rights.edit_stories) {
                        otherItem.addSubItem(channel_stories, R.drawable.msg_archive, LocaleController.getString(R.string.OpenChannelArchiveStories));
                    }
                    if (ChatObject.isPublic(chat)) {
                        otherItem.addSubItem(share, R.drawable.msg_share, LocaleController.getString(R.string.BotShare));
                    }
                    if (!BuildVars.IS_BILLING_UNAVAILABLE && !getMessagesController().premiumPurchaseBlocked()) {
                        StarsController.getInstance(currentAccount).loadStarGifts();
                        otherItem.addSubItem(gift_premium, R.drawable.msg_gift_premium, LocaleController.getString(R.string.ProfileSendAGiftToChannel));
                        otherItem.setSubItemShown(gift_premium, chatInfo != null && chatInfo.stargifts_available);
                    }
                    if (chatInfo != null && chatInfo.linked_chat_id != 0) {
                        otherItem.addSubItem(view_discussion, R.drawable.msg_discussion, LocaleController.getString(R.string.ViewDiscussion));
                    }
                    if (!currentChat.creator && !currentChat.left && !currentChat.kicked) {
                        otherItem.addSubItem(leave_group, R.drawable.msg_leave, LocaleController.getString(R.string.LeaveChannelMenu));
                    }
                }
            } else {
                if (chatInfo != null) {
                    if (ChatObject.canManageCalls(chat) && chatInfo.call == null) {
                        otherItem.addSubItem(call_item, R.drawable.msg_voicechat, LocaleController.getString(R.string.StartVoipChat));
                        hasVoiceChatItem = true;
                    }
                    ChatObject.Call call = getMessagesController().getGroupCall(chatId, false);
                    callItemVisible = call != null;
                }
                if (ChatObject.canChangeChatInfo(chat)) {
                    editItemVisible = true;
                }
                if (!ChatObject.isKickedFromChat(chat) && !ChatObject.isLeftFromChat(chat)) {
                    if (chatInfo == null || !chatInfo.participants_hidden || ChatObject.hasAdminRights(chat)) {
                        canSearchMembers = true;
                        otherItem.addSubItem(search_members, R.drawable.msg_search, LocaleController.getString(R.string.SearchMembers));
                    }
                }
                otherItem.addSubItem(leave_group, R.drawable.msg_leave, LocaleController.getString(R.string.DeleteAndExit));
            }
            if (topicId == 0) {
                otherItem.addSubItem(add_shortcut, R.drawable.msg_home, LocaleController.getString(R.string.AddShortcut));
            }
        }

        if (imageUpdater != null) {
            otherItem.addSubItem(set_as_main, R.drawable.msg_openprofile, LocaleController.getString(R.string.SetAsMain));
            otherItem.addSubItem(gallery_menu_save, R.drawable.msg_gallery, LocaleController.getString(R.string.SaveToGallery));
            //otherItem.addSubItem(edit_avatar, R.drawable.photo_paint, LocaleController.getString(R.string.EditPhoto));
            otherItem.addSubItem(delete_avatar, R.drawable.msg_delete, LocaleController.getString(R.string.Delete));
        } else {
            otherItem.addSubItem(gallery_menu_save, R.drawable.msg_gallery, LocaleController.getString(R.string.SaveToGallery));
        }
        if (getMessagesController().isChatNoForwards(currentChat)) {
            otherItem.hideSubItem(gallery_menu_save);
        }

        if (selfUser && !myProfile) {
            otherItem.addSubItem(logout, R.drawable.msg_leave, LocaleController.getString(R.string.LogOut));
        }
        if (!isPulledDown) {
            otherItem.hideSubItem(gallery_menu_save);
            otherItem.hideSubItem(set_as_main);
            otherItem.showSubItem(add_photo);
            otherItem.hideSubItem(edit_avatar);
            otherItem.hideSubItem(delete_avatar);
        }
        if (!mediaHeaderVisible) {
//            if (callItemVisible) {
//                if (callItem.getVisibility() != View.VISIBLE) {
//                    callItem.setVisibility(View.VISIBLE);
//                    if (animated) {
//                        callItem.setAlpha(0);
//                        callItem.animate().alpha(1f).setDuration(150).start();
//                    }
//                }
//            } else {
//                if (callItem.getVisibility() != View.GONE) {
//                    callItem.setVisibility(View.GONE);
//                }
//            }
//            if (videoCallItemVisible) {
//                if (videoCallItem.getVisibility() != View.VISIBLE) {
//                    videoCallItem.setVisibility(View.VISIBLE);
//                    if (animated) {
//                        videoCallItem.setAlpha(0);
//                        videoCallItem.animate().alpha(1f).setDuration(150).start();
//                    }
//                }
//            } else {
//                if (videoCallItem.getVisibility() != View.GONE) {
//                    videoCallItem.setVisibility(View.GONE);
//                }
//            }
            if (editItemVisible) {
                if (editItem.getVisibility() != View.VISIBLE) {
                    editItem.setVisibility(View.VISIBLE);
                    if (animated) {
                        editItem.setAlpha(0);
                        editItem.animate().alpha(1f).setDuration(150).start();
                    }
                }
            } else {
                if (editItem.getVisibility() != View.GONE) {
                    editItem.setVisibility(View.GONE);
                }
            }
        }
        if (avatarsViewPagerIndicatorView != null) {
            if (avatarsViewPagerIndicatorView.isIndicatorFullyVisible()) {
                if (editItemVisible) {
                    editItem.setVisibility(View.GONE);
                    editItem.animate().cancel();
                    editItem.setAlpha(1f);
                }
//                if (callItemVisible) {
//                    callItem.setVisibility(View.GONE);
//                    callItem.animate().cancel();
//                    callItem.setAlpha(1f);
//                }
//                if (videoCallItemVisible) {
//                    videoCallItem.setVisibility(View.GONE);
//                    videoCallItem.animate().cancel();
//                    videoCallItem.setAlpha(1f);
//                }
            }
        }
        if (sharedMediaLayout != null) {
            sharedMediaLayout.getSearchItem().requestLayout();
        }
        updateStoriesViewBounds(false);
    }


    private void setMediaHeaderVisible(boolean visible) {
        if (mediaHeaderVisible == visible) {
            return;
        }
        mediaHeaderVisible = visible;
        if (headerAnimatorSet != null) {
            headerAnimatorSet.cancel();
        }
        if (headerShadowAnimatorSet != null) {
            headerShadowAnimatorSet.cancel();
        }
        ActionBarMenuItem mediaSearchItem = sharedMediaLayout.getSearchItem();
        ImageView mediaOptionsItem = sharedMediaLayout.getSearchOptionsItem();
        TextView saveItem = sharedMediaLayout.getSaveItem();
        if (!mediaHeaderVisible) {
            if (editItemVisible) {
                editItem.setVisibility(View.VISIBLE);
            }
            otherItem.setVisibility(View.VISIBLE);
            if (mediaOptionsItem != null) {
                mediaOptionsItem.setVisibility(View.GONE);
            }
            if (saveItem != null) {
                saveItem.setVisibility(View.GONE);
            }
        } else {
            if (sharedMediaLayout.isSearchItemVisible()) {
                mediaSearchItem.setVisibility(View.VISIBLE);
            }
            if (mediaOptionsItem != null) {
                mediaOptionsItem.setVisibility(View.VISIBLE);
            }
            if (sharedMediaLayout.isOptionsItemVisible()) {
                sharedMediaLayout.photoVideoOptionsItem.setVisibility(View.VISIBLE);
                sharedMediaLayout.animateSearchToOptions(true, false);
            } else {
                sharedMediaLayout.photoVideoOptionsItem.setVisibility(View.INVISIBLE);
                sharedMediaLayout.animateSearchToOptions(false, false);
            }
        }
        updateStoriesViewBounds(false);

        if (actionBar != null) {
            actionBar.createMenu().requestLayout();
        }

        ArrayList<Animator> animators = new ArrayList<>();

//        animators.add(ObjectAnimator.ofFloat(callItem, View.ALPHA, visible ? 0.0f : 1.0f));
//        animators.add(ObjectAnimator.ofFloat(videoCallItem, View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(otherItem, View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(editItem, View.ALPHA, visible ? 0.0f : 1.0f));
//        animators.add(ObjectAnimator.ofFloat(callItem, View.TRANSLATION_Y, visible ? -AndroidUtilities.dp(10) : 0.0f));
//        animators.add(ObjectAnimator.ofFloat(videoCallItem, View.TRANSLATION_Y, visible ? -AndroidUtilities.dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(otherItem, View.TRANSLATION_Y, visible ? -AndroidUtilities.dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(editItem, View.TRANSLATION_Y, visible ? -AndroidUtilities.dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(mediaSearchItem, View.ALPHA, visible ? 1.0f : 0.0f));
        animators.add(ObjectAnimator.ofFloat(mediaSearchItem, View.TRANSLATION_Y, visible ? 0.0f : AndroidUtilities.dp(10)));
        animators.add(ObjectAnimator.ofFloat(sharedMediaLayout.photoVideoOptionsItem, View.ALPHA, visible ? 1.0f : 0.0f));
        animators.add(ObjectAnimator.ofFloat(sharedMediaLayout.photoVideoOptionsItem, View.TRANSLATION_Y, visible ? 0.0f : AndroidUtilities.dp(10)));
        animators.add(ObjectAnimator.ofFloat(actionBar, ACTIONBAR_HEADER_PROGRESS, visible ? 1.0f : 0.0f));
        animators.add(ObjectAnimator.ofFloat(onlineTextView[1], View.ALPHA, visible ? 0.0f : 1.0f));
        if (myProfile)
            animators.add(ObjectAnimator.ofFloat(onlineTextView[3], View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(mediaCounterTextView, View.ALPHA, visible ? 1.0f : 0.0f));
        if (visible) {
            animators.add(ObjectAnimator.ofFloat(this, HEADER_SHADOW, 0.0f));
        }
        if (storyView != null || giftsView != null) {
            ValueAnimator va = ValueAnimator.ofFloat(0, 1);
            va.addUpdateListener(a -> updateStoriesViewBounds(true));
            animators.add(va);
        }

        headerAnimatorSet = new AnimatorSet();
        headerAnimatorSet.playTogether(animators);
        headerAnimatorSet.setInterpolator(CubicBezierInterpolator.DEFAULT);
        headerAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (headerAnimatorSet != null) {
                    if (mediaHeaderVisible) {
                        if (editItemVisible) {
                            editItem.setVisibility(View.GONE);
                        }
                        otherItem.setVisibility(View.GONE);
                    } else {
                        if (sharedMediaLayout.isSearchItemVisible()) {
                            mediaSearchItem.setVisibility(View.VISIBLE);
                        }

                        sharedMediaLayout.photoVideoOptionsItem.setVisibility(View.INVISIBLE);

                        headerShadowAnimatorSet = new AnimatorSet();
                        headerShadowAnimatorSet.playTogether(ObjectAnimator.ofFloat(DebugProfile.this, HEADER_SHADOW, 1.0f));
                        headerShadowAnimatorSet.setDuration(100);
                        headerShadowAnimatorSet.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                headerShadowAnimatorSet = null;
                            }
                        });
                        headerShadowAnimatorSet.start();
                    }
                }
                updateStoriesViewBounds(false);
                headerAnimatorSet = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                headerAnimatorSet = null;
            }
        });
        headerAnimatorSet.setDuration(150);
        headerAnimatorSet.start();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
    }

    private void createAutoDeleteItem(Context context) {
        autoDeletePopupWrapper = new AutoDeletePopupWrapper(context, otherItem.getPopupLayout().getSwipeBack(), new AutoDeletePopupWrapper.Callback() {

            @Override
            public void dismiss() {
                otherItem.toggleSubMenu();
            }

            @Override
            public void setAutoDeleteHistory(int time, int action) {
                DebugProfile.this.setAutoDeleteHistory(time, action);
            }

            @Override
            public void showGlobalAutoDeleteScreen() {
                presentFragment(new AutoDeleteMessagesActivity());
                dismiss();
            }
        }, false, 0, resourcesProvider);
        if (dialogId > 0 || userId > 0) {
            int linkColor = dontApplyPeerColor(getThemedColor(Theme.key_windowBackgroundWhiteBlueText), false);
            autoDeletePopupWrapper.allowExtendedHint(linkColor);
        }
        int ttl = 0;
        if (userInfo != null || chatInfo != null) {
            ttl = userInfo != null ? userInfo.ttl_period : chatInfo.ttl_period;
        }
        autoDeleteItemDrawable = TimerDrawable.getTtlIcon(ttl);
        autoDeleteItem = otherItem.addSwipeBackItem(0, autoDeleteItemDrawable, LocaleController.getString(R.string.AutoDeletePopupTitle), autoDeletePopupWrapper.windowLayout);
        otherItem.addColoredGap();
        updateAutoDeleteItem();
    }

    private void setAutoDeleteHistory(int time, int action) {
        long did = getDialogId();
        getMessagesController().setDialogHistoryTTL(did, time);
        if (userInfo != null || chatInfo != null) {
            undoView.showWithAction(did, action, getMessagesController().getUser(did), userInfo != null ? userInfo.ttl_period : chatInfo.ttl_period, null, null);
        }
    }

    public void updateListAnimated(boolean updateOnlineCount) {
        updateListAnimated(updateOnlineCount, false);
    }

    private void updateListAnimated(boolean updateOnlineCount, boolean triedInLayout) {
        if (listAdapter == null) {
            if (updateOnlineCount) {
                updateOnlineCount(false);
            }
            updateRowsIds();
            return;
        }

        if (!triedInLayout && listView.isInLayout()) {
            if (!listView.isAttachedToWindow()) return;
            listView.post(() -> updateListAnimated(updateOnlineCount, true));
            return;
        }

        DiffCallback diffCallback = new DiffCallback();
        diffCallback.oldRowCount = rowCount;
        diffCallback.fillPositions(diffCallback.oldPositionToItem);
        diffCallback.oldChatParticipant.clear();
        diffCallback.oldChatParticipantSorted.clear();
        diffCallback.oldChatParticipant.addAll(visibleChatParticipants);
        diffCallback.oldChatParticipantSorted.addAll(visibleSortedUsers);
        diffCallback.oldMembersStartRow = membersStartRow;
        diffCallback.oldMembersEndRow = membersEndRow;
        if (updateOnlineCount) {
            updateOnlineCount(false);
        }
        saveScrollPosition();
        updateRowsIds();
        diffCallback.fillPositions(diffCallback.newPositionToItem);
        try {
            DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(listAdapter);
        } catch (Exception e) {
            FileLog.e(e);
            listAdapter.notifyDataSetChanged();
        }
//        if (savedScrollPosition >= 0) { // TODO
//            layoutManager.scrollToPositionWithOffset(savedScrollPosition, savedScrollOffset - listView.getPaddingTop());
//        }
        AndroidUtilities.updateVisibleRows(listView);
    }

    private void checkCanSendStoryForPosting() {
        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(chatId);
        if (!ChatObject.isBoostSupported(chat)) {
            return;
        }
        StoriesController storiesController = getMessagesController().getStoriesController();
        waitCanSendStoryRequest = true;
        storiesController.canSendStoryFor(getDialogId(), canSend -> {
            waitCanSendStoryRequest = false;
            showBoostsAlert = !canSend;
            hideFloatingButton(!(sharedMediaLayout == null || sharedMediaLayout.getClosestTab() == SharedMediaLayout.TAB_STORIES || sharedMediaLayout.getClosestTab() == SharedMediaLayout.TAB_ARCHIVED_STORIES));
        }, false, resourcesProvider);
    }

    private void hideFloatingButton(boolean hide) {
        // TODO
//        TLRPC.User bot = getMessagesController().getUser(userId);
//        if (bot != null && bot.bot && bot.bot_can_edit && bot.bot_has_main_app) {
//            StoriesController.BotPreviewsList list = (StoriesController.BotPreviewsList) getMessagesController().getStoriesController().getStoriesList(userId, StoriesController.StoriesList.TYPE_BOTS);
//            ArrayList<StoriesController.UploadingStory> uploadingStories = getMessagesController().getStoriesController().getUploadingStories(userId);
//            if (list != null && list.getCount() + (uploadingStories == null ? 0 : uploadingStories.size()) >= getMessagesController().botPreviewMediasMax) {
//                hide = true;
//            }
//        }
//        if (floatingHidden == hide || floatingButtonContainer == null || waitCanSendStoryRequest) {
//            return;
//        }
//        floatingHidden = hide;
//        AnimatorSet animatorSet = new AnimatorSet();
//        ValueAnimator valueAnimator = ValueAnimator.ofFloat(floatingButtonHideProgress, floatingHidden ? 1f : 0f);
//        valueAnimator.addUpdateListener(animation -> {
//            floatingButtonHideProgress = (float) animation.getAnimatedValue();
//            updateFloatingButtonOffset();
//        });
//        animatorSet.playTogether(valueAnimator);
//        animatorSet.setDuration(300);
//        animatorSet.setInterpolator(floatingInterpolator);
//        floatingButtonContainer.setClickable(!hide);
//        animatorSet.start();
    }

    @Override
    public void didUploadPhoto(final TLRPC.InputFile photo, final TLRPC.InputFile video, double videoStartTimestamp, String videoPath, TLRPC.PhotoSize bigSize, final TLRPC.PhotoSize smallSize, boolean isVideo, TLRPC.VideoSize emojiMarkup) {
        AndroidUtilities.runOnUIThread(() -> {
            if (photo != null || video != null || emojiMarkup != null) {
                if (avatar == null) {
                    return;
                }
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                if (photo != null) {
                    req.file = photo;
                    req.flags |= 1;
                }
                if (video != null) {
                    req.video = video;
                    req.flags |= 2;
                    req.video_start_ts = videoStartTimestamp;
                    req.flags |= 4;
                }
                if (emojiMarkup != null) {
                    req.video_emoji_markup = emojiMarkup;
                    req.flags |= 16;
                }
                avatarUploadingRequest = getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                    if (error == null) {
                        TLRPC.User user = getMessagesController().getUser(getUserConfig().getClientUserId());
                        if (user == null) {
                            user = getUserConfig().getCurrentUser();
                            if (user == null) {
                                return;
                            }
                            getMessagesController().putUser(user, false);
                        } else {
                            getUserConfig().setCurrentUser(user);
                        }

                        TLRPC.TL_photos_photo photos_photo = (TLRPC.TL_photos_photo) response;
                        ArrayList<TLRPC.PhotoSize> sizes = photos_photo.photo.sizes;
                        TLRPC.PhotoSize small = FileLoader.getClosestPhotoSizeWithSize(sizes, 150);
                        TLRPC.PhotoSize big = FileLoader.getClosestPhotoSizeWithSize(sizes, 800);
                        TLRPC.VideoSize videoSize = photos_photo.photo.video_sizes.isEmpty() ? null : FileLoader.getClosestVideoSizeWithSize(photos_photo.photo.video_sizes, 1000);
                        user.photo = new TLRPC.TL_userProfilePhoto();
                        user.photo.photo_id = photos_photo.photo.id;
                        if (small != null) {
                            user.photo.photo_small = small.location;
                        }
                        if (big != null) {
                            user.photo.photo_big = big.location;
                        }

                        if (small != null && avatar != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(small, true);
                            File src = FileLoader.getInstance(currentAccount).getPathToAttach(avatar, true);
                            src.renameTo(destFile);
                            String oldKey = avatar.volume_id + "_" + avatar.local_id + "@50_50";
                            String newKey = small.location.volume_id + "_" + small.location.local_id + "@50_50";
                            ImageLoader.getInstance().replaceImageInCache(oldKey, newKey, ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL), false);
                        }

                        if (videoSize != null && videoPath != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(videoSize, "mp4", true);
                            File src = new File(videoPath);
                            src.renameTo(destFile);
                        } else if (big != null && avatarBig != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(big, true);
                            File src = FileLoader.getInstance(currentAccount).getPathToAttach(avatarBig, true);
                            src.renameTo(destFile);
                        }
                        getMessagesController().getDialogPhotos(user.id).addPhotoAtStart(((TLRPC.TL_photos_photo) response).photo);
                        ArrayList<TLRPC.User> users = new ArrayList<>();
                        users.add(user);
                        getMessagesStorage().putUsersAndChats(users, null, false, true);
                        TLRPC.UserFull userFull = getMessagesController().getUserFull(userId);
                        if (userFull != null) {
                            userFull.profile_photo = photos_photo.photo;
                            getMessagesStorage().updateUserInfo(userFull, false);
                        }
                    }

//                    allowPullingDown = !AndroidUtilities.isTablet() && !isInLandscapeMode && avatarImage.getImageReceiver().hasNotThumb() && !AndroidUtilities.isAccessibilityScreenReaderEnabled();
                    avatar = null;
                    avatarBig = null;
                    avatarsViewPager.scrolledByUser = true;
                    avatarsViewPager.removeUploadingImage(uploadingImageLocation);
                    avatarsViewPager.setCreateThumbFromParent(false);
                    updateProfileData(true);
                    showAvatarProgress(false, true);
                    getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                    getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                    getUserConfig().saveConfig(true);

                }));
            } else {
                avatar = smallSize.location;
                avatarBig = bigSize.location;
                avatarImage.setImage(ImageLocation.getForLocal(avatar), "50_50", avatarDrawable, null);
                if (setAvatarRow != -1) {
                    updateRowsIds();
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    // TODO
                    checkLayout();
                }
                avatarsViewPager.addUploadingImage(uploadingImageLocation = ImageLocation.getForLocal(avatarBig), ImageLocation.getForLocal(avatar));
                showAvatarProgress(true, false);
            }
            actionBar.createMenu().requestLayout();
        });
    }


    private void openUrl(String url, Browser.Progress progress) {
        if (url.startsWith("@")) {
            getMessagesController().openByUserName(url.substring(1), DebugProfile.this, 0, progress);
        } else if (url.startsWith("#") || url.startsWith("$")) {
            DialogsActivity fragment = new DialogsActivity(null);
            fragment.setSearchString(url);
            presentFragment(fragment);
        } else if (url.startsWith("/")) {
            if (parentLayout.getFragmentStack().size() > 1) {
                BaseFragment previousFragment = parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2);
                if (previousFragment instanceof ChatActivity) {
                    finishFragment();
                    ((ChatActivity) previousFragment).chatActivityEnterView.setCommand(null, url, false, false);
                }
            }
        }
    }

    @Override
    public PhotoViewer.PlaceProviderObject getCloseIntoObject() {
        return ImageUpdater.ImageUpdaterDelegate.super.getCloseIntoObject();
    }

    @Override
    public boolean supportsBulletin() {
        return ImageUpdater.ImageUpdaterDelegate.super.supportsBulletin();
    }

    @Override
    public String getInitialSearchString() {
        return ImageUpdater.ImageUpdaterDelegate.super.getInitialSearchString();
    }

    @Override
    public void onUploadProgressChanged(float progress) {
        ImageUpdater.ImageUpdaterDelegate.super.onUploadProgressChanged(progress);
    }

    @Override
    public void didStartUpload(boolean fromAvatarConstructor, boolean isVideo) {
        ImageUpdater.ImageUpdaterDelegate.super.didStartUpload(fromAvatarConstructor, isVideo);
    }

    @Override
    public void didUploadFailed() {
        ImageUpdater.ImageUpdaterDelegate.super.didUploadFailed();
    }

    @Override
    public boolean canFinishFragment() {
        return ImageUpdater.ImageUpdaterDelegate.super.canFinishFragment();
    }

    @Override
    public void mediaCountUpdated() {

    }

    @Override
    public boolean didSelectDialogs(DialogsActivity fragment, ArrayList<MessagesStorage.TopicKey> dids, CharSequence message, boolean param, boolean notify, int scheduleDate, TopicsFragment topicsFragment) {
        long did = dids.get(0).dialogId;
        Bundle args = new Bundle();
        args.putBoolean("scrollToTopOnResume", true);
        if (DialogObject.isEncryptedDialog(did)) {
            args.putInt("enc_id", DialogObject.getEncryptedChatId(did));
        } else if (DialogObject.isUserDialog(did)) {
            args.putLong("user_id", did);
        } else if (DialogObject.isChatDialog(did)) {
            args.putLong("chat_id", -did);
        }
        if (!getMessagesController().checkCanOpenChat(args, fragment)) {
            return false;
        }

        getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
        getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
        presentFragment(new ChatActivity(args), true);
        removeSelfFromStack();
        TLRPC.User user = getMessagesController().getUser(userId);
        getSendMessagesHelper().sendMessage(SendMessagesHelper.SendMessageParams.of(user, did, null, null, null, null, notify, scheduleDate));
        if (!TextUtils.isEmpty(message)) {
            AccountInstance accountInstance = AccountInstance.getInstance(currentAccount);
            SendMessagesHelper.prepareSendingText(accountInstance, message.toString(), did, notify, scheduleDate, 0);
        }
        return true;
    }

    public void setLoadingSpan(CharacterStyle span) {
        if (loadingSpan == span) return;
        loadingSpan = span;
        AndroidUtilities.forEachViews(listView, view -> {
            if (view instanceof TextDetailCell) {
                ((TextDetailCell) view).textView.setLoading(loadingSpan);
                ((TextDetailCell) view).valueTextView.setLoading(loadingSpan);
            }
        });
    }

    private void onTextDetailCellImageClicked(View view) {
        View parent = (View) view.getParent();
        if (parent.getTag() != null && ((int) parent.getTag()) == usernameRow) {
            Bundle args = new Bundle();
            args.putLong("chat_id", chatId);
            args.putLong("user_id", userId);
            presentFragment(new QrActivity(args));
        } else if (parent.getTag() != null && ((int) parent.getTag()) == birthdayRow) {
            if (userId == getUserConfig().getClientUserId()) {
                presentFragment(new PremiumPreviewFragment("my_profile_gift"));
                return;
            }
            if (UserObject.areGiftsDisabled(userInfo)) {
                BulletinFactory.of(this).createSimpleBulletin(R.raw.error, AndroidUtilities.replaceTags(LocaleController.formatString(R.string.UserDisallowedGifts, DialogObject.getShortName(userId)))).show();
                return;
            }
            showDialog(new GiftSheet(getContext(), currentAccount, userId, null, null));
        }
    }

    public void setChatInfo(TLRPC.ChatFull value) {
        chatInfo = value;
        if (chatInfo != null && chatInfo.migrated_from_chat_id != 0 && mergeDialogId == 0) {
            mergeDialogId = -chatInfo.migrated_from_chat_id;
            getMediaDataController().getMediaCounts(mergeDialogId, topicId, classGuid);
        }
        if (sharedMediaLayout != null) {
            sharedMediaLayout.setChatInfo(chatInfo);
        }
        if (avatarsViewPager != null && !isTopic) {
            avatarsViewPager.setChatInfo(chatInfo);
        }
        if (storyView != null && chatInfo != null) {
            storyView.setStories(chatInfo.stories);
        }
        if (giftsView != null) {
            giftsView.update();
        }
        if (avatarImage != null) {
            avatarImage.setHasStories(needInsetForStories());
        }
        fetchUsersFromChannelInfo();
        if (chatId != 0) {
            otherItem.setSubItemShown(gift_premium, !BuildVars.IS_BILLING_UNAVAILABLE && !getMessagesController().premiumPurchaseBlocked() && chatInfo != null && chatInfo.stargifts_available);
        }
    }

    public void setPlayProfileAnimation(int type) {
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        if (!AndroidUtilities.isTablet()) {
            needTimerImage = type != 0;
            needStarImage = type != 0;
            if (preferences.getBoolean("view_animations", true)) {
                playProfileAnimation = type;
            } else if (type == 2) {
                expandPhoto = true;
            }
        }
    }

    public void prepareBlurBitmap() {
        if (blurredView == null) {
            return;
        }
        int w = (int) (fragmentView.getMeasuredWidth() / 6.0f);
        int h = (int) (fragmentView.getMeasuredHeight() / 6.0f);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1.0f / 6.0f, 1.0f / 6.0f);
        fragmentView.draw(canvas);
        Utilities.stackBlurBitmap(bitmap, Math.max(7, Math.max(w, h) / 180));
        blurredView.setBackground(new BitmapDrawable(bitmap));
        blurredView.setAlpha(0.0f);
        blurredView.setVisibility(View.VISIBLE);
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

    public static class ShowDrawable extends Drawable implements SimpleTextView.PressableDrawable {

        public final AnimatedTextView.AnimatedTextDrawable textDrawable;
        public final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final ButtonBounce bounce = new ButtonBounce(null) {
            @Override
            public void invalidate() {
                invalidateSelf();
            }
        };
        private int textColor;
        private float alpha = 1f, alpha2 = 1f;
        private boolean pressed;
        private View view;

        public ShowDrawable(String string) {
            textDrawable = new AnimatedTextView.AnimatedTextDrawable();
            textDrawable.setCallback(new Callback() {
                @Override
                public void invalidateDrawable(@NonNull Drawable who) {
                    if (view != null) {
                        view.invalidate();
                    }
                }

                @Override
                public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
                }

                @Override
                public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
                }
            });
            textDrawable.setText(string);
            textDrawable.setTextSize(dp(11));
            textDrawable.setGravity(Gravity.CENTER);
            backgroundPaint.setColor(0x1f000000);
        }

        public void setBackgroundColor(int backgroundColor) {
            if (backgroundPaint.getColor() != backgroundColor) {
                backgroundPaint.setColor(backgroundColor);
                invalidateSelf();
            }
        }

        public void setTextColor(int textColor) {
            if (this.textColor != textColor) {
                this.textColor = textColor;
                invalidateSelf();
            }
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            final float alpha = this.alpha * this.alpha2;
            if (alpha <= 0) return;
            AndroidUtilities.rectTmp.set(getBounds());
            canvas.save();
            final float s = bounce.getScale(0.1f);
            canvas.scale(s, s, AndroidUtilities.rectTmp.centerX(), AndroidUtilities.rectTmp.centerY());
            final int wasAlpha = backgroundPaint.getAlpha();
            backgroundPaint.setAlpha((int) (wasAlpha * alpha));
            canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(20), dp(20), backgroundPaint);
            backgroundPaint.setAlpha(wasAlpha);
            textDrawable.setTextColor(textColor);
            textDrawable.setAlpha((int) (0xFF * alpha));
            textDrawable.setBounds((int) AndroidUtilities.rectTmp.left, (int) AndroidUtilities.rectTmp.top, (int) AndroidUtilities.rectTmp.right, (int) AndroidUtilities.rectTmp.bottom);
            textDrawable.draw(canvas);
            canvas.restore();
        }

        @Override
        public void setAlpha(int alpha) {
            this.alpha = alpha / 255f;
            invalidateSelf();
        }

        public void setAlpha2(float alpha) {
            this.alpha2 = alpha;
            invalidateSelf();
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getIntrinsicWidth() {
            return (int) (textDrawable.getAnimateToWidth() + dp(11));
        }

        @Override
        public int getIntrinsicHeight() {
            return dp(17.33f);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }

        @Override
        public boolean isPressed() {
            return pressed;
        }

        @Override
        public void setPressed(boolean pressed) {
            bounce.setPressed(pressed);
            this.pressed = pressed;
        }

        public void setView(View view) {
            this.view = view;
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final static int VIEW_TYPE_HEADER = 1, VIEW_TYPE_TEXT_DETAIL = 2, VIEW_TYPE_ABOUT_LINK = 3, VIEW_TYPE_TEXT = 4, VIEW_TYPE_DIVIDER = 5, VIEW_TYPE_NOTIFICATIONS_CHECK = 6, VIEW_TYPE_SHADOW = 7, VIEW_TYPE_USER = 8, VIEW_TYPE_EMPTY = 11, VIEW_TYPE_BOTTOM_PADDING = 12, VIEW_TYPE_SHARED_MEDIA = 13, VIEW_TYPE_VERSION = 14, VIEW_TYPE_SUGGESTION = 15, VIEW_TYPE_ADDTOGROUP_INFO = 17, VIEW_TYPE_PREMIUM_TEXT_CELL = 18, VIEW_TYPE_TEXT_DETAIL_MULTILINE = 19, VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE = 20, VIEW_TYPE_LOCATION = 21, VIEW_TYPE_HOURS = 22, VIEW_TYPE_CHANNEL = 23, VIEW_TYPE_STARS_TEXT_CELL = 24, VIEW_TYPE_BOT_APP = 25, VIEW_TYPE_SHADOW_TEXT = 26, VIEW_TYPE_COLORFUL_TEXT = 27;
        private final HashMap<TLRPC.TL_username, ClickableSpan> usernameSpans = new HashMap<TLRPC.TL_username, ClickableSpan>();
        private final Context mContext;


        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_HEADER: {
                    view = new HeaderCell(mContext, 23, resourcesProvider);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case VIEW_TYPE_TEXT_DETAIL_MULTILINE:
                case VIEW_TYPE_TEXT_DETAIL:
                    final TextDetailCell textDetailCell = new TextDetailCell(mContext, resourcesProvider, viewType == VIEW_TYPE_TEXT_DETAIL_MULTILINE) {
                        @Override
                        protected int processColor(int color) {
                            return dontApplyPeerColor(color, false);
                        }
                    };
                    textDetailCell.setContentDescriptionValueFirst(true);
                    view = textDetailCell;
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_ABOUT_LINK: {
                    view = aboutLinkCell = new AboutLinkCell(mContext, DebugProfile.this, resourcesProvider) {
                        @Override
                        protected void didPressUrl(String url, Browser.Progress progress) {
                            openUrl(url, progress);
                        }

                        @Override
                        protected void didResizeEnd() {
                            layoutManager.mIgnoreTopPadding = false;
                        }

                        @Override
                        protected void didResizeStart() {
                            layoutManager.mIgnoreTopPadding = true;
                        }

                        @Override
                        protected int processColor(int color) {
                            return dontApplyPeerColor(color, false);
                        }
                    };
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case VIEW_TYPE_TEXT: {
                    view = new TextCell(mContext, resourcesProvider) {
                        @Override
                        protected int processColor(int color) {
                            return dontApplyPeerColor(color, false);
                        }
                    };
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case VIEW_TYPE_DIVIDER: {
                    view = new DividerCell(mContext, resourcesProvider);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    view.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(4), 0, 0);
                    break;
                }
                case VIEW_TYPE_NOTIFICATIONS_CHECK: {
                    view = new NotificationsCheckCell(mContext, 23, 70, false, resourcesProvider) {
                        @Override
                        protected int processColor(int color) {
                            return dontApplyPeerColor(color, false);
                        }
                    };
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE: {
                    view = new TextCheckCell(mContext, resourcesProvider);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case VIEW_TYPE_SHADOW: {
                    view = new ShadowSectionCell(mContext, resourcesProvider);
                    break;
                }
                case VIEW_TYPE_SHADOW_TEXT: {
                    view = new TextInfoPrivacyCell(mContext, resourcesProvider);
                    break;
                }
                case VIEW_TYPE_COLORFUL_TEXT: {
                    view = new AffiliateProgramFragment.ColorfulTextCell(mContext, resourcesProvider);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case VIEW_TYPE_USER: {
                    view = new UserCell(mContext, addMemberRow == -1 ? 9 : 6, 0, true, resourcesProvider);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case VIEW_TYPE_EMPTY: {
                    view = new View(mContext) {
                        @Override
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32), MeasureSpec.EXACTLY));
                        }
                    };
                    break;
                }
                case VIEW_TYPE_BOTTOM_PADDING: {
                    view = new View(mContext) {

                        private int lastPaddingHeight = 0;
                        private int lastListViewHeight = 0;

                        @Override
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            if (lastListViewHeight != listView.getMeasuredHeight()) {
                                lastPaddingHeight = 0;
                            }
                            lastListViewHeight = listView.getMeasuredHeight();
                            int n = listView.getChildCount();
                            if (n == listAdapter.getItemCount()) {
                                int totalHeight = 0;
                                for (int i = 0; i < n; i++) {
                                    View view = listView.getChildAt(i);
                                    int p = listView.getChildAdapterPosition(view);
                                    if (p >= 0 && p != bottomPaddingRow) {
                                        totalHeight += listView.getChildAt(i).getMeasuredHeight();
                                    }
                                }
                                int paddingHeight = (fragmentView == null ? 0 : fragmentView.getMeasuredHeight()) - ActionBar.getCurrentActionBarHeight() - AndroidUtilities.statusBarHeight - totalHeight;
                                if (paddingHeight > AndroidUtilities.dp(88)) {
                                    paddingHeight = 0;
                                }
                                if (paddingHeight <= 0) {
                                    paddingHeight = 0;
                                }
                                setMeasuredDimension(listView.getMeasuredWidth(), lastPaddingHeight = paddingHeight);
                            } else {
                                setMeasuredDimension(listView.getMeasuredWidth(), lastPaddingHeight);
                            }
                        }
                    };
                    view.setBackground(new ColorDrawable(Color.TRANSPARENT));
                    break;
                }
                case VIEW_TYPE_SHARED_MEDIA: {
                    if (sharedMediaLayout.getParent() != null) {
                        ((ViewGroup) sharedMediaLayout.getParent()).removeView(sharedMediaLayout);
                    }
                    view = sharedMediaLayout;
                    break;
                }
                case VIEW_TYPE_ADDTOGROUP_INFO: {
                    view = new TextInfoPrivacyCell(mContext, resourcesProvider);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case VIEW_TYPE_LOCATION:
                    view = new ProfileLocationCell(mContext, resourcesProvider);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_HOURS:
                    view = new ProfileHoursCell(mContext, resourcesProvider) {
                        @Override
                        protected int processColor(int color) {
                            return dontApplyPeerColor(color, false);
                        }
                    };
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_VERSION:
                default: {
                    TextInfoPrivacyCell cell = new TextInfoPrivacyCell(mContext, 10, resourcesProvider);
                    cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                    cell.getTextView().setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText3));
                    cell.getTextView().setMovementMethod(null);
                    try {
                        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                        int code = pInfo.versionCode / 10;
                        String abi = "";
                        switch (pInfo.versionCode % 10) {
                            case 1:
                            case 2:
                                abi = "store bundled " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                                break;
                            default:
                            case 9:
                                if (ApplicationLoader.isStandaloneBuild()) {
                                    abi = "direct " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                                } else {
                                    abi = "universal " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                                }
                                break;
                        }
                        cell.setText(formatString("TelegramVersion", R.string.TelegramVersion, String.format(Locale.US, "v%s (%d) %s", pInfo.versionName, code, abi)));
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    cell.getTextView().setPadding(0, AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14));
                    view = cell;
                    view.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                    break;
                }
                case VIEW_TYPE_SUGGESTION: {
                    view = new SettingsSuggestionCell(mContext, resourcesProvider) {
                        @Override
                        protected void onYesClick(int type) {
                            AndroidUtilities.runOnUIThread(() -> {
                                getNotificationCenter().removeObserver(DebugProfile.this, NotificationCenter.newSuggestionsAvailable);
                                if (type == SettingsSuggestionCell.TYPE_GRACE) {
                                    getMessagesController().removeSuggestion(0, "PREMIUM_GRACE");
                                    Browser.openUrl(getContext(), getMessagesController().premiumManageSubscriptionUrl);
                                } else {
                                    getMessagesController().removeSuggestion(0, type == SettingsSuggestionCell.TYPE_PHONE ? "VALIDATE_PHONE_NUMBER" : "VALIDATE_PASSWORD");
                                }
                                getNotificationCenter().addObserver(DebugProfile.this, NotificationCenter.newSuggestionsAvailable);
                                updateListAnimated(false);
                            });
                        }

                        @Override
                        protected void onNoClick(int type) {
                            if (type == SettingsSuggestionCell.TYPE_PHONE) {
                                presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
                            } else {
                                presentFragment(new TwoStepVerificationSetupActivity(TwoStepVerificationSetupActivity.TYPE_VERIFY, null));
                            }
                        }
                    };
                    break;
                }
                case VIEW_TYPE_PREMIUM_TEXT_CELL:
                case VIEW_TYPE_STARS_TEXT_CELL:
                    view = new ProfilePremiumCell(mContext, viewType == VIEW_TYPE_PREMIUM_TEXT_CELL ? 0 : 1, resourcesProvider);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_CHANNEL:
                    view = new ProfileChannelCell(DebugProfile.this) {
                        @Override
                        public int processColor(int color) {
                            return dontApplyPeerColor(color, false);
                        }
                    };
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_BOT_APP:
                    FrameLayout frameLayout = new FrameLayout(mContext);
                    ButtonWithCounterView button = new ButtonWithCounterView(mContext, resourcesProvider);
                    button.setText(LocaleController.getString(R.string.ProfileBotOpenApp), false);
                    button.setOnClickListener(v -> {
                        TLRPC.User bot = getMessagesController().getUser(userId);
                        getMessagesController().openApp(DebugProfile.this, bot, null, getClassGuid(), null);
                    });
                    frameLayout.addView(button, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.FILL, 18, 14, 18, 14));
                    view = frameLayout;
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            if (viewType != VIEW_TYPE_SHARED_MEDIA) {
                view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            if (holder.itemView == sharedMediaLayout) {
                sharedMediaLayoutAttached = true;
            }
            if (holder.itemView instanceof TextDetailCell) {
                ((TextDetailCell) holder.itemView).textView.setLoading(loadingSpan);
                ((TextDetailCell) holder.itemView).valueTextView.setLoading(loadingSpan);
            }
        }

        @Override
        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            if (holder.itemView == sharedMediaLayout) {
                sharedMediaLayoutAttached = false;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == infoHeaderRow) {
                        if (ChatObject.isChannel(currentChat) && !currentChat.megagroup && channelInfoRow != -1) {
                            headerCell.setText(LocaleController.getString(R.string.ReportChatDescription));
                        } else {
                            headerCell.setText(LocaleController.getString(R.string.Info));
                        }
                    } else if (position == membersHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.ChannelMembers));
                    } else if (position == settingsSectionRow2) {
                        headerCell.setText(LocaleController.getString(R.string.SETTINGS));
                    } else if (position == numberSectionRow) {
                        headerCell.setText(LocaleController.getString(R.string.Account));
                    } else if (position == helpHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.SettingsHelp));
                    } else if (position == debugHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.SettingsDebug));
                    } else if (position == botPermissionsHeader) {
                        headerCell.setText(LocaleController.getString(R.string.BotProfilePermissions));
                    }
                    headerCell.setTextColor(dontApplyPeerColor(getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader), false));
                    break;
                case VIEW_TYPE_TEXT_DETAIL_MULTILINE:
                case VIEW_TYPE_TEXT_DETAIL:
                    TextDetailCell detailCell = (TextDetailCell) holder.itemView;
                    boolean containsQr = false;
                    boolean containsGift = false;
                    if (position == birthdayRow) {
                        TLRPC.UserFull userFull = getMessagesController().getUserFull(userId);
                        if (userFull != null && userFull.birthday != null) {
                            final boolean today = BirthdayController.isToday(userFull);
                            final boolean withYear = (userFull.birthday.flags & 1) != 0;
                            final int age = withYear ? Period.between(LocalDate.of(userFull.birthday.year, userFull.birthday.month, userFull.birthday.day), LocalDate.now()).getYears() : -1;

                            String text = UserInfoActivity.birthdayString(userFull.birthday);

                            if (withYear) {
                                text = LocaleController.formatPluralString(today ? "ProfileBirthdayTodayValueYear" : "ProfileBirthdayValueYear", age, text);
                            } else {
                                text = LocaleController.formatString(today ? R.string.ProfileBirthdayTodayValue : R.string.ProfileBirthdayValue, text);
                            }

                            detailCell.setTextAndValue(Emoji.replaceWithRestrictedEmoji(text, detailCell.textView, () -> {
                                if (holder.getAdapterPosition() == position && birthdayRow == position && holder.getItemViewType() == VIEW_TYPE_TEXT_DETAIL) {
                                    onBindViewHolder(holder, position);
                                }
                            }), LocaleController.getString(today ? R.string.ProfileBirthdayToday : R.string.ProfileBirthday), isTopic || bizHoursRow != -1 || bizLocationRow != -1);

                            containsGift = !myProfile && today && !getMessagesController().premiumPurchaseBlocked();
                        }
                    } else if (position == phoneRow) {
                        String text;
                        TLRPC.User user = getMessagesController().getUser(userId);
                        String phoneNumber;
                        if (user != null && !TextUtils.isEmpty(vcardPhone)) {
                            text = PhoneFormat.getInstance().format("+" + vcardPhone);
                            phoneNumber = vcardPhone;
                        } else if (user != null && !TextUtils.isEmpty(user.phone)) {
                            text = PhoneFormat.getInstance().format("+" + user.phone);
                            phoneNumber = user.phone;
                        } else {
                            text = LocaleController.getString(R.string.PhoneHidden);
                            phoneNumber = null;
                        }
                        isFragmentPhoneNumber = phoneNumber != null && phoneNumber.matches("888\\d{8}");
                        detailCell.setTextAndValue(text, LocaleController.getString(isFragmentPhoneNumber ? R.string.AnonymousNumber : R.string.PhoneMobile), false);
                    } else if (position == usernameRow) {
                        String username = null;
                        CharSequence text;
                        CharSequence value;
                        ArrayList<TLRPC.TL_username> usernames = new ArrayList<>();
                        if (userId != 0) {
                            final TLRPC.User user = getMessagesController().getUser(userId);
                            if (user != null) {
                                usernames.addAll(user.usernames);
                            }
                            TLRPC.TL_username usernameObj = null;
                            if (user != null && !TextUtils.isEmpty(user.username)) {
                                usernameObj = DialogObject.findUsername(user.username, usernames);
                                username = user.username;
                            }
                            usernames = user == null ? new ArrayList<>() : new ArrayList<>(user.usernames);
                            if (TextUtils.isEmpty(username) && usernames != null) {
                                for (int i = 0; i < usernames.size(); ++i) {
                                    TLRPC.TL_username u = usernames.get(i);
                                    if (u != null && u.active && !TextUtils.isEmpty(u.username)) {
                                        usernameObj = u;
                                        username = u.username;
                                        break;
                                    }
                                }
                            }
                            value = LocaleController.getString(R.string.Username);
                            if (username != null) {
                                text = "@" + username;
                                if (usernameObj != null && !usernameObj.editable) {
                                    text = new SpannableString(text);
                                    ((SpannableString) text).setSpan(makeUsernameLinkSpan(usernameObj), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            } else {
                                text = "—";
                            }
                            containsQr = true;
                        } else if (currentChat != null) {
                            TLRPC.Chat chat = getMessagesController().getChat(chatId);
                            username = ChatObject.getPublicUsername(chat);
                            if (chat != null) {
                                usernames.addAll(chat.usernames);
                            }
                            if (ChatObject.isPublic(chat)) {
                                containsQr = true;
                                text = getMessagesController().linkPrefix + "/" + username + (topicId != 0 ? "/" + topicId : "");
                                value = LocaleController.getString(R.string.InviteLink);
                            } else {
                                text = getMessagesController().linkPrefix + "/c/" + chatId + (topicId != 0 ? "/" + topicId : "");
                                value = LocaleController.getString(R.string.InviteLinkPrivate);
                            }
                        } else {
                            text = "";
                            value = "";
                            usernames = new ArrayList<>();
                        }
                        detailCell.setTextAndValue(text, alsoUsernamesString(username, usernames, value), (isTopic || bizHoursRow != -1 || bizLocationRow != -1) && birthdayRow < 0);
                    } else if (position == locationRow) {
                        if (chatInfo != null && chatInfo.location instanceof TLRPC.TL_channelLocation) {
                            TLRPC.TL_channelLocation location = (TLRPC.TL_channelLocation) chatInfo.location;
                            detailCell.setTextAndValue(location.address, LocaleController.getString(R.string.AttachLocation), false);
                        }
                    } else if (position == numberRow) {
                        TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
                        String value;
                        if (user != null && user.phone != null && user.phone.length() != 0) {
                            value = PhoneFormat.getInstance().format("+" + user.phone);
                        } else {
                            value = LocaleController.getString(R.string.NumberUnknown);
                        }
                        detailCell.setTextAndValue(value, LocaleController.getString(R.string.TapToChangePhone), true);
                        detailCell.setContentDescriptionValueFirst(false);
                    } else if (position == setUsernameRow) {
                        TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
                        String text = "";
                        CharSequence value = LocaleController.getString(R.string.Username);
                        String username = null;
                        if (user != null && user.usernames.size() > 0) {
                            for (int i = 0; i < user.usernames.size(); ++i) {
                                TLRPC.TL_username u = user.usernames.get(i);
                                if (u != null && u.active && !TextUtils.isEmpty(u.username)) {
                                    username = u.username;
                                    break;
                                }
                            }
                            if (username == null) {
                                username = user.username;
                            }
                            if (username == null || TextUtils.isEmpty(username)) {
                                text = LocaleController.getString(R.string.UsernameEmpty);
                            } else {
                                text = "@" + username;
                            }
                            value = alsoUsernamesString(username, user.usernames, value);
                        } else {
                            username = UserObject.getPublicUsername(user);
                            if (user != null && !TextUtils.isEmpty(username)) {
                                text = "@" + username;
                            } else {
                                text = LocaleController.getString(R.string.UsernameEmpty);
                            }
                        }
                        detailCell.setTextAndValue(text, value, true);
                        detailCell.setContentDescriptionValueFirst(true);
                    }
                    if (containsGift) {
                        Drawable drawable = ContextCompat.getDrawable(detailCell.getContext(), R.drawable.msg_input_gift);
                        drawable.setColorFilter(new PorterDuffColorFilter(dontApplyPeerColor(getThemedColor(Theme.key_switch2TrackChecked), false), PorterDuff.Mode.MULTIPLY));
                        if (UserObject.areGiftsDisabled(userInfo)) {
                            detailCell.setImage(null);
                            detailCell.setImageClickListener(null);
                        } else {
                            detailCell.setImage(drawable, LocaleController.getString(R.string.GiftPremium));
                            detailCell.setImageClickListener(DebugProfile.this::onTextDetailCellImageClicked);
                        }
                    } else if (containsQr) {
                        Drawable drawable = ContextCompat.getDrawable(detailCell.getContext(), R.drawable.msg_qr_mini);
                        drawable.setColorFilter(new PorterDuffColorFilter(dontApplyPeerColor(getThemedColor(Theme.key_switch2TrackChecked), false), PorterDuff.Mode.MULTIPLY));
                        detailCell.setImage(drawable, LocaleController.getString(R.string.GetQRCode));
                        detailCell.setImageClickListener(DebugProfile.this::onTextDetailCellImageClicked);
                    } else {
                        detailCell.setImage(null);
                        detailCell.setImageClickListener(null);
                    }
                    detailCell.setTag(position);
                    detailCell.textView.setLoading(loadingSpan);
                    detailCell.valueTextView.setLoading(loadingSpan);
                    break;
                case VIEW_TYPE_ABOUT_LINK:
                    AboutLinkCell aboutLinkCell = (AboutLinkCell) holder.itemView;
                    if (position == userInfoRow) {
                        TLRPC.User user = userInfo.user != null ? userInfo.user : getMessagesController().getUser(userInfo.id);
                        boolean addlinks = isBot || (user != null && user.premium && userInfo.about != null);
                        aboutLinkCell.setTextAndValue(userInfo.about, LocaleController.getString(R.string.UserBio), addlinks);
                    } else if (position == channelInfoRow) {
                        String text = chatInfo.about;
                        while (text.contains("\n\n\n")) {
                            text = text.replace("\n\n\n", "\n\n");
                        }
                        aboutLinkCell.setText(text, ChatObject.isChannel(currentChat) && !currentChat.megagroup);
                    } else if (position == bioRow) {
                        String value;
                        if (userInfo == null || !TextUtils.isEmpty(userInfo.about)) {
                            value = userInfo == null ? LocaleController.getString(R.string.Loading) : userInfo.about;
                            aboutLinkCell.setTextAndValue(value, LocaleController.getString(R.string.UserBio), getUserConfig().isPremium());
                            currentBio = userInfo != null ? userInfo.about : null;
                        } else {
                            aboutLinkCell.setTextAndValue(LocaleController.getString(R.string.UserBio), LocaleController.getString(R.string.UserBioDetail), false);
                            currentBio = null;
                        }
                        aboutLinkCell.setMoreButtonDisabled(true);
                    }
                    break;
                case VIEW_TYPE_PREMIUM_TEXT_CELL:
                case VIEW_TYPE_STARS_TEXT_CELL:
                case VIEW_TYPE_TEXT:
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
                    textCell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                    if (position == settingsTimerRow) {
                        TLRPC.EncryptedChat encryptedChat = getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(dialogId));
                        String value;
                        if (encryptedChat.ttl == 0) {
                            value = LocaleController.getString(R.string.ShortMessageLifetimeForever);
                        } else {
                            value = LocaleController.formatTTLString(encryptedChat.ttl);
                        }
                        textCell.setTextAndValue(LocaleController.getString(R.string.MessageLifetime), value, false, false);
                    } else if (position == unblockRow) {
                        textCell.setText(LocaleController.getString(R.string.Unblock), false);
                        textCell.setColors(-1, Theme.key_text_RedRegular);
                    } else if (position == settingsKeyRow) {
                        IdenticonDrawable identiconDrawable = new IdenticonDrawable();
                        TLRPC.EncryptedChat encryptedChat = getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(dialogId));
                        identiconDrawable.setEncryptedChat(encryptedChat);
                        textCell.setTextAndValueDrawable(LocaleController.getString(R.string.EncryptionKey), identiconDrawable, false);
                    } else if (position == joinRow) {
                        textCell.setColors(-1, Theme.key_windowBackgroundWhiteBlueText2);
                        if (currentChat.megagroup) {
                            textCell.setText(LocaleController.getString(R.string.ProfileJoinGroup), false);
                        } else {
                            textCell.setText(LocaleController.getString(R.string.ProfileJoinChannel), false);
                        }
                    } else if (position == subscribersRow) {
                        if (chatInfo != null) {
                            if (ChatObject.isChannel(currentChat) && !currentChat.megagroup) {
                                textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelSubscribers), LocaleController.formatNumber(chatInfo.participants_count, ','), R.drawable.msg_groups, position != membersSectionRow - 1);
                            } else {
                                textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelMembers), LocaleController.formatNumber(chatInfo.participants_count, ','), R.drawable.msg_groups, position != membersSectionRow - 1);
                            }
                        } else {
                            if (ChatObject.isChannel(currentChat) && !currentChat.megagroup) {
                                textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelSubscribers), R.drawable.msg_groups, position != membersSectionRow - 1);
                            } else {
                                textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelMembers), R.drawable.msg_groups, position != membersSectionRow - 1);
                            }
                        }
                    } else if (position == subscribersRequestsRow) {
                        if (chatInfo != null) {
                            textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.SubscribeRequests), String.format("%d", chatInfo.requests_pending), R.drawable.msg_requests, position != membersSectionRow - 1);
                        }
                    } else if (position == administratorsRow) {
                        if (chatInfo != null) {
                            textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelAdministrators), String.format("%d", chatInfo.admins_count), R.drawable.msg_admins, position != membersSectionRow - 1);
                        } else {
                            textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelAdministrators), R.drawable.msg_admins, position != membersSectionRow - 1);
                        }
                    } else if (position == settingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelAdminSettings), R.drawable.msg_customize, position != membersSectionRow - 1);
                    } else if (position == channelBalanceRow) {
                        final TL_stars.StarsAmount stars_balance = BotStarsController.getInstance(currentAccount).getBotStarsBalance(-chatId);
                        final long ton_balance = BotStarsController.getInstance(currentAccount).getTONBalance(-chatId);
                        SpannableStringBuilder ssb = new SpannableStringBuilder();
                        if (ton_balance > 0) {
                            if (ton_balance / 1_000_000_000.0 > 1000.0) {
                                ssb.append("TON ").append(AndroidUtilities.formatWholeNumber((int) (ton_balance / 1_000_000_000.0), 0));
                            } else {
                                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                                symbols.setDecimalSeparator('.');
                                DecimalFormat formatterTON = new DecimalFormat("#.##", symbols);
                                formatterTON.setMinimumFractionDigits(2);
                                formatterTON.setMaximumFractionDigits(3);
                                formatterTON.setGroupingUsed(false);
                                ssb.append("TON ").append(formatterTON.format(ton_balance / 1_000_000_000.0));
                            }
                        }
                        if (stars_balance.amount > 0) {
                            if (ssb.length() > 0) ssb.append(" ");
                            ssb.append("XTR ").append(formatStarsAmountShort(stars_balance));
                        }
                        textCell.setTextAndValueAndIcon(getString(R.string.ChannelStars), ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), textCell.getTextView().getPaint()), R.drawable.menu_feature_paid, true);
                    } else if (position == botStarsBalanceRow) {
                        final TL_stars.StarsAmount stars_balance = BotStarsController.getInstance(currentAccount).getBotStarsBalance(userId);
                        SpannableStringBuilder ssb = new SpannableStringBuilder();
                        if (stars_balance.amount > 0) {
                            ssb.append("XTR ").append(formatStarsAmountShort(stars_balance));
                        }
                        textCell.setTextAndValueAndIcon(getString(R.string.BotBalanceStars), ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), textCell.getTextView().getPaint()), R.drawable.menu_premium_main, true);
                    } else if (position == botTonBalanceRow) {
                        long ton_balance = BotStarsController.getInstance(currentAccount).getTONBalance(userId);
                        SpannableStringBuilder ssb = new SpannableStringBuilder();
                        if (ton_balance > 0) {
                            if (ton_balance / 1_000_000_000.0 > 1000.0) {
                                ssb.append("TON ").append(AndroidUtilities.formatWholeNumber((int) (ton_balance / 1_000_000_000.0), 0));
                            } else {
                                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                                symbols.setDecimalSeparator('.');
                                DecimalFormat formatterTON = new DecimalFormat("#.##", symbols);
                                formatterTON.setMinimumFractionDigits(2);
                                formatterTON.setMaximumFractionDigits(3);
                                formatterTON.setGroupingUsed(false);
                                ssb.append("TON ").append(formatterTON.format(ton_balance / 1_000_000_000.0));
                            }
                        }
                        textCell.setTextAndValueAndIcon(getString(R.string.BotBalanceTON), ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), textCell.getTextView().getPaint()), R.drawable.msg_ton, true);
                    } else if (position == blockedUsersRow) {
                        if (chatInfo != null) {
                            textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelBlacklist), String.format("%d", Math.max(chatInfo.banned_count, chatInfo.kicked_count)), R.drawable.msg_user_remove, position != membersSectionRow - 1);
                        } else {
                            textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelBlacklist), R.drawable.msg_user_remove, position != membersSectionRow - 1);
                        }
                    } else if (position == addMemberRow) {
                        textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                        boolean isNextPositionMember = position + 1 >= membersStartRow && position + 1 < membersEndRow;
                        textCell.setTextAndIcon(LocaleController.getString(R.string.AddMember), R.drawable.msg_contact_add, membersSectionRow == -1 || isNextPositionMember);
                    } else if (position == sendMessageRow) {
                        textCell.setText(LocaleController.getString(R.string.SendMessageLocation), true);
                    } else if (position == addToContactsRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.AddToContacts), R.drawable.msg_contact_add, false);
                        textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                    } else if (position == reportReactionRow) {
                        TLRPC.Chat chat = getMessagesController().getChat(-reportReactionFromDialogId);
                        if (chat != null && ChatObject.canBlockUsers(chat)) {
                            textCell.setTextAndIcon(LocaleController.getString(R.string.ReportReactionAndBan), R.drawable.msg_block2, false);
                        } else {
                            textCell.setTextAndIcon(LocaleController.getString(R.string.ReportReaction), R.drawable.msg_report, false);
                        }

                        textCell.setColors(Theme.key_text_RedBold, Theme.key_text_RedRegular);
                        textCell.setColors(Theme.key_text_RedBold, Theme.key_text_RedRegular);
                    } else if (position == reportRow) {
                        textCell.setText(LocaleController.getString(R.string.ReportUserLocation), false);
                        textCell.setColors(-1, Theme.key_text_RedRegular);
                        textCell.setColors(-1, Theme.key_text_RedRegular);
                    } else if (position == languageRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.Language), LocaleController.getCurrentLanguageName(), false, R.drawable.msg2_language, false);
                        textCell.setImageLeft(23);
                    } else if (position == notificationRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.NotificationsAndSounds), R.drawable.msg2_notifications, true);
                    } else if (position == privacyRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.PrivacySettings), R.drawable.msg2_secret, true);
                    } else if (position == dataRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.DataSettings), R.drawable.msg2_data, true);
                    } else if (position == chatRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ChatSettings), R.drawable.msg2_discussion, true);
                    } else if (position == filtersRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.Filters), R.drawable.msg2_folder, true);
                    } else if (position == stickersRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.StickersName), R.drawable.msg2_sticker, true);
                    } else if (position == liteModeRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.PowerUsage), R.drawable.msg2_battery, true);
                    } else if (position == questionRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.AskAQuestion), R.drawable.msg2_ask_question, true);
                    } else if (position == faqRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.TelegramFAQ), R.drawable.msg2_help, true);
                    } else if (position == policyRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.PrivacyPolicy), R.drawable.msg2_policy, false);
                    } else if (position == sendLogsRow) {
                        textCell.setText(LocaleController.getString(R.string.DebugSendLogs), true);
                    } else if (position == sendLastLogsRow) {
                        textCell.setText(LocaleController.getString(R.string.DebugSendLastLogs), true);
                    } else if (position == clearLogsRow) {
                        textCell.setText(LocaleController.getString(R.string.DebugClearLogs), switchBackendRow != -1);
                    } else if (position == switchBackendRow) {
                        textCell.setText("Switch Backend", false);
                    } else if (position == devicesRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.Devices), R.drawable.msg2_devices, true);
                    } else if (position == setAvatarRow) {
                        cellCameraDrawable.setCustomEndFrame(86);
                        cellCameraDrawable.setCurrentFrame(85, false);
                        textCell.setTextAndIcon(LocaleController.getString(R.string.SetProfilePhoto), cellCameraDrawable, false);
                        textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                        textCell.getImageView().setPadding(0, 0, 0, AndroidUtilities.dp(8));
                        textCell.setImageLeft(12);
                        setAvatarCell = textCell;
                    } else if (position == addToGroupButtonRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.AddToGroupOrChannel), R.drawable.msg_groups_create, false);
                    } else if (position == premiumRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.TelegramPremium), new AnimatedEmojiDrawable.WrapSizeDrawable(PremiumGradient.getInstance().premiumStarMenuDrawable, dp(24), dp(24)), true);
                        textCell.setImageLeft(23);
                    } else if (position == starsRow) {
                        StarsController c = StarsController.getInstance(currentAccount);
                        long balance = c.getBalance().amount;
                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.MenuTelegramStars), c.balanceAvailable() && balance > 0 ? LocaleController.formatNumber((int) balance, ',') : "", new AnimatedEmojiDrawable.WrapSizeDrawable(PremiumGradient.getInstance().goldenStarMenuDrawable, dp(24), dp(24)), true);
                        textCell.setImageLeft(23);
                    } else if (position == businessRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.TelegramBusiness), R.drawable.menu_shop, true);
                        textCell.setImageLeft(23);
                    } else if (position == premiumGiftingRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.SendAGift), R.drawable.menu_gift, false);
                        textCell.setImageLeft(23);
                    } else if (position == botPermissionLocation) {
                        textCell.setTextAndCheckAndColorfulIcon(LocaleController.getString(R.string.BotProfilePermissionLocation), botLocation != null && botLocation.granted(), R.drawable.filled_access_location, getThemedColor(Theme.key_color_green), botPermissionBiometry != -1);
                    } else if (position == botPermissionBiometry) {
                        textCell.setTextAndCheckAndColorfulIcon(LocaleController.getString(R.string.BotProfilePermissionBiometry), botBiometry != null && botBiometry.granted(), R.drawable.filled_access_fingerprint, getThemedColor(Theme.key_color_orange), false);
                    } else if (position == botPermissionEmojiStatus) {
                        textCell.setTextAndCheckAndColorfulIcon(LocaleController.getString(R.string.BotProfilePermissionEmojiStatus), userInfo != null && userInfo.bot_can_manage_emoji_status, R.drawable.filled_access_sleeping, getThemedColor(Theme.key_color_lightblue), botPermissionLocation != -1 || botPermissionBiometry != -1);
                    }
                    textCell.valueTextView.setTextColor(dontApplyPeerColor(getThemedColor(Theme.key_windowBackgroundWhiteValueText), false));
                    break;
                case VIEW_TYPE_NOTIFICATIONS_CHECK:
                    NotificationsCheckCell checkCell = (NotificationsCheckCell) holder.itemView;
                    if (position == notificationsRow) {
                        SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                        long did;
                        if (dialogId != 0) {
                            did = dialogId;
                        } else if (userId != 0) {
                            did = userId;
                        } else {
                            did = -chatId;
                        }
                        String key = NotificationsController.getSharedPrefKey(did, topicId);
                        boolean enabled = false;
                        boolean custom = preferences.getBoolean("custom_" + key, false);
                        boolean hasOverride = preferences.contains("notify2_" + key);
                        int value = preferences.getInt("notify2_" + key, 0);
                        int delta = preferences.getInt("notifyuntil_" + key, 0);
                        String val;
                        if (value == 3 && delta != Integer.MAX_VALUE) {
                            delta -= getConnectionsManager().getCurrentTime();
                            if (delta <= 0) {
                                if (custom) {
                                    val = LocaleController.getString(R.string.NotificationsCustom);
                                } else {
                                    val = LocaleController.getString(R.string.NotificationsOn);
                                }
                                enabled = true;
                            } else if (delta < 60 * 60) {
                                val = formatString("WillUnmuteIn", R.string.WillUnmuteIn, LocaleController.formatPluralString("Minutes", delta / 60));
                            } else if (delta < 60 * 60 * 24) {
                                val = formatString("WillUnmuteIn", R.string.WillUnmuteIn, LocaleController.formatPluralString("Hours", (int) Math.ceil(delta / 60.0f / 60)));
                            } else if (delta < 60 * 60 * 24 * 365) {
                                val = formatString("WillUnmuteIn", R.string.WillUnmuteIn, LocaleController.formatPluralString("Days", (int) Math.ceil(delta / 60.0f / 60 / 24)));
                            } else {
                                val = null;
                            }
                        } else {
                            if (value == 0) {
                                if (hasOverride) {
                                    enabled = true;
                                } else {
                                    enabled = getNotificationsController().isGlobalNotificationsEnabled(did, false, false);
                                }
                            } else if (value == 1) {
                                enabled = true;
                            }
                            if (enabled && custom) {
                                val = LocaleController.getString(R.string.NotificationsCustom);
                            } else {
                                val = enabled ? LocaleController.getString(R.string.NotificationsOn) : LocaleController.getString(R.string.NotificationsOff);
                            }
                        }
                        if (val == null) {
                            val = LocaleController.getString(R.string.NotificationsOff);
                        }
                        if (notificationsExceptionTopics != null && !notificationsExceptionTopics.isEmpty()) {
                            val = String.format(Locale.US, LocaleController.getPluralString("NotificationTopicExceptionsDesctription", notificationsExceptionTopics.size()), val, notificationsExceptionTopics.size());
                        }
                        checkCell.setAnimationsEnabled(fragmentOpened);
                        checkCell.setTextAndValueAndCheck(LocaleController.getString(R.string.Notifications), val, enabled, botAppRow >= 0);
                    }
                    break;
                case VIEW_TYPE_SHADOW:
                    View sectionCell = holder.itemView;
                    sectionCell.setTag(position);
                    Drawable drawable;
                    if (position == infoSectionRow && lastSectionRow == -1 && secretSettingsSectionRow == -1 && sharedMediaRow == -1 && membersSectionRow == -1 || position == secretSettingsSectionRow || position == lastSectionRow || position == membersSectionRow && lastSectionRow == -1 && sharedMediaRow == -1) {
                        sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                    } else {
                        sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                    }
                    break;
                case VIEW_TYPE_SHADOW_TEXT: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    cell.setLinkTextRippleColor(null);
                    if (position == infoSectionRow) {
                        final long did = getDialogId();
                        TLObject obj = getMessagesController().getUserOrChat(did);
                        TL_bots.botVerification bot_verification = userInfo != null ? userInfo.bot_verification : chatInfo != null ? chatInfo.bot_verification : null;
                        if (botAppRow >= 0 || bot_verification != null) {
                            cell.setFixedSize(0);
                            final TLRPC.User user = getMessagesController().getUser(userId);
                            final boolean botOwner = user != null && user.bot && user.bot_can_edit;
                            SpannableStringBuilder sb = new SpannableStringBuilder();

                            if (botAppRow >= 0) {
                                sb.append(AndroidUtilities.replaceSingleTag(getString(botOwner ? R.string.ProfileBotOpenAppInfoOwner : R.string.ProfileBotOpenAppInfo), () -> {
                                    Browser.openUrl(getContext(), getString(botOwner ? R.string.ProfileBotOpenAppInfoOwnerLink : R.string.ProfileBotOpenAppInfoLink));
                                }));
                                if (bot_verification != null) {
                                    sb.append("\n\n\n");
                                }
                            }
                            if (bot_verification != null) {
                                sb.append("x");
                                sb.setSpan(new AnimatedEmojiSpan(bot_verification.icon, cell.getTextView().getPaint().getFontMetricsInt()), sb.length() - 1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                sb.append(" ");
                                SpannableString description = new SpannableString(bot_verification.description);
                                try {
                                    AndroidUtilities.addLinksSafe(description, Linkify.WEB_URLS, false, false);
                                    URLSpan[] spans = description.getSpans(0, description.length(), URLSpan.class);
                                    for (int i = 0; i < spans.length; ++i) {
                                        URLSpan span = spans[i];
                                        int start = description.getSpanStart(span);
                                        int end = description.getSpanEnd(span);
                                        final String url = span.getURL();

                                        description.removeSpan(span);
                                        description.setSpan(new URLSpan(url) {
                                            @Override
                                            public void onClick(View widget) {
                                                Browser.openUrl(getContext(), url);
                                            }

                                            @Override
                                            public void updateDrawState(@NonNull TextPaint ds) {
                                                ds.setUnderlineText(true);
                                            }
                                        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    }
                                } catch (Exception e) {
                                    FileLog.e(e);
                                }
                                sb.append(description);
                            }

                            cell.setLinkTextRippleColor(Theme.multAlpha(getThemedColor(Theme.key_windowBackgroundWhiteGrayText4), 0.2f));
                            cell.setText(sb);
                        } else {
                            cell.setFixedSize(14);
                            cell.setText(null);
                        }
                    } else if (position == infoAffiliateRow) {
                        final TLRPC.User botUser = getMessagesController().getUser(userId);
                        if (botUser != null && botUser.bot && botUser.bot_can_edit) {
                            cell.setFixedSize(0);
                            cell.setText(formatString(R.string.ProfileBotAffiliateProgramInfoOwner, UserObject.getUserName(botUser), percents(userInfo != null && userInfo.starref_program != null ? userInfo.starref_program.commission_permille : 0)));
                        } else {
                            cell.setFixedSize(0);
                            cell.setText(formatString(R.string.ProfileBotAffiliateProgramInfo, UserObject.getUserName(botUser), percents(userInfo != null && userInfo.starref_program != null ? userInfo.starref_program.commission_permille : 0)));
                        }
                    }
                    if (position == infoSectionRow && lastSectionRow == -1 && secretSettingsSectionRow == -1 && sharedMediaRow == -1 && membersSectionRow == -1 || position == secretSettingsSectionRow || position == lastSectionRow || position == membersSectionRow && lastSectionRow == -1 && sharedMediaRow == -1) {
                        cell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                    } else {
                        cell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                    }
                    break;
                }
                case VIEW_TYPE_COLORFUL_TEXT: {
                    AffiliateProgramFragment.ColorfulTextCell cell = (AffiliateProgramFragment.ColorfulTextCell) holder.itemView;
                    cell.set(getThemedColor(Theme.key_color_green), R.drawable.filled_affiliate, getString(R.string.ProfileBotAffiliateProgram), null);
                    cell.setPercent(userInfo != null && userInfo.starref_program != null ? percents(userInfo.starref_program.commission_permille) : null);
                    break;
                }
                case VIEW_TYPE_USER:
                    UserCell userCell = (UserCell) holder.itemView;
                    TLRPC.ChatParticipant part;
                    try {
                        if (!visibleSortedUsers.isEmpty()) {
                            part = visibleChatParticipants.get(visibleSortedUsers.get(position - membersStartRow));
                        } else {
                            part = visibleChatParticipants.get(position - membersStartRow);
                        }
                    } catch (Exception e) {
                        part = null;
                        FileLog.e(e);
                    }
                    if (part != null) {
                        String role;
                        if (part instanceof TLRPC.TL_chatChannelParticipant) {
                            TLRPC.ChannelParticipant channelParticipant = ((TLRPC.TL_chatChannelParticipant) part).channelParticipant;
                            if (!TextUtils.isEmpty(channelParticipant.rank)) {
                                role = channelParticipant.rank;
                            } else {
                                if (channelParticipant instanceof TLRPC.TL_channelParticipantCreator) {
                                    role = LocaleController.getString(R.string.ChannelCreator);
                                } else if (channelParticipant instanceof TLRPC.TL_channelParticipantAdmin) {
                                    role = LocaleController.getString(R.string.ChannelAdmin);
                                } else {
                                    role = null;
                                }
                            }
                        } else {
                            if (part instanceof TLRPC.TL_chatParticipantCreator) {
                                role = LocaleController.getString(R.string.ChannelCreator);
                            } else if (part instanceof TLRPC.TL_chatParticipantAdmin) {
                                role = getString(R.string.ChannelAdmin);
                            } else {
                                role = null;
                            }
                        }
                        userCell.setAdminRole(role);
                        userCell.setData(getMessagesController().getUser(part.user_id), null, null, 0, position != membersEndRow - 1);
                    }
                    break;
                case VIEW_TYPE_BOTTOM_PADDING:
                    holder.itemView.requestLayout();
                    break;
                case VIEW_TYPE_SUGGESTION:
                    SettingsSuggestionCell suggestionCell = (SettingsSuggestionCell) holder.itemView;
                    if (position == passwordSuggestionRow) {
                        suggestionCell.setType(SettingsSuggestionCell.TYPE_PASSWORD);
                    } else if (position == phoneSuggestionRow) {
                        suggestionCell.setType(SettingsSuggestionCell.TYPE_PHONE);
                    } else if (position == graceSuggestionRow) {
                        suggestionCell.setType(SettingsSuggestionCell.TYPE_GRACE);
                    }
                    break;
                case VIEW_TYPE_ADDTOGROUP_INFO:
                    TextInfoPrivacyCell addToGroupInfo = (TextInfoPrivacyCell) holder.itemView;
                    addToGroupInfo.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                    addToGroupInfo.setText(LocaleController.getString(R.string.BotAddToGroupOrChannelInfo));
                    break;
                case VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setTextAndCheck(LocaleController.getString(R.string.Notifications), !getMessagesController().isDialogMuted(getDialogId(), topicId), false);
                    break;
                case VIEW_TYPE_LOCATION:
                    ((ProfileLocationCell) holder.itemView).set(userInfo != null ? userInfo.business_location : null, notificationsDividerRow < 0 && !myProfile);
                    break;
                case VIEW_TYPE_HOURS:
                    ProfileHoursCell hoursCell = (ProfileHoursCell) holder.itemView;
                    hoursCell.setOnTimezoneSwitchClick(view -> {
                        hoursShownMine = !hoursShownMine;
                        if (!hoursExpanded) {
                            hoursExpanded = true;
                        }
                        saveScrollPosition();
                        view.requestLayout();
                        listAdapter.notifyItemChanged(bizHoursRow);
                        // TODO
//                        if (savedScrollPosition >= 0) {
//                            layoutManager.scrollToPositionWithOffset(savedScrollPosition, savedScrollOffset - listView.getPaddingTop());
//                        }
                    });
                    hoursCell.set(userInfo != null ? userInfo.business_work_hours : null, hoursExpanded, hoursShownMine, notificationsDividerRow < 0 && !myProfile || bizLocationRow >= 0);
                    break;
                case VIEW_TYPE_CHANNEL:
                    ((ProfileChannelCell) holder.itemView).set(getMessagesController().getChat(userInfo.personal_channel_id), profileChannelMessageFetcher != null ? profileChannelMessageFetcher.messageObject : null);
                    break;
                case VIEW_TYPE_BOT_APP:
                    break;
            }
        }

        private CharSequence alsoUsernamesString(String originalUsername, ArrayList<TLRPC.TL_username> alsoUsernames, CharSequence fallback) {
            if (alsoUsernames == null) {
                return fallback;
            }
            alsoUsernames = new ArrayList<>(alsoUsernames);
            for (int i = 0; i < alsoUsernames.size(); ++i) {
                if (!alsoUsernames.get(i).active || originalUsername != null && originalUsername.equals(alsoUsernames.get(i).username)) {
                    alsoUsernames.remove(i--);
                }
            }
            if (alsoUsernames.size() > 0) {
                SpannableStringBuilder usernames = new SpannableStringBuilder();
                for (int i = 0; i < alsoUsernames.size(); ++i) {
                    TLRPC.TL_username usernameObj = alsoUsernames.get(i);
                    final String usernameRaw = usernameObj.username;
                    SpannableString username = new SpannableString("@" + usernameRaw);
                    username.setSpan(makeUsernameLinkSpan(usernameObj), 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    username.setSpan(new ForegroundColorSpan(dontApplyPeerColor(getThemedColor(Theme.key_chat_messageLinkIn), false)), 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    usernames.append(username);
                    if (i < alsoUsernames.size() - 1) {
                        usernames.append(", ");
                    }
                }
                String string = getString(R.string.UsernameAlso);
                SpannableStringBuilder finalString = new SpannableStringBuilder(string);
                final String toFind = "%1$s";
                int index = string.indexOf(toFind);
                if (index >= 0) {
                    finalString.replace(index, index + toFind.length(), usernames);
                }
                return finalString;
            } else {
                return fallback;
            }
        }

        public ClickableSpan makeUsernameLinkSpan(TLRPC.TL_username usernameObj) {
            ClickableSpan span = usernameSpans.get(usernameObj);
            if (span != null) return span;

            final String usernameRaw = usernameObj.username;
            span = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    if (!usernameObj.editable) {
                        if (loadingSpan == this) return;
                        setLoadingSpan(this);
                        TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                        TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                        input.username = usernameObj.username;
                        req.collectible = input;
                        int reqId = getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                            setLoadingSpan(null);
                            if (res instanceof TL_fragment.TL_collectibleInfo) {
                                TLObject obj;
                                if (userId != 0) {
                                    obj = getMessagesController().getUser(userId);
                                } else {
                                    obj = getMessagesController().getChat(chatId);
                                }
                                if (getContext() == null) {
                                    return;
                                }
                                FragmentUsernameBottomSheet.open(getContext(), FragmentUsernameBottomSheet.TYPE_USERNAME, usernameObj.username, obj, (TL_fragment.TL_collectibleInfo) res, getResourceProvider());
                            } else {
                                BulletinFactory.showError(err);
                            }
                        }));
                        getConnectionsManager().bindRequestToGuid(reqId, getClassGuid());
                    } else {
                        setLoadingSpan(null);
                        String urlFinal = getMessagesController().linkPrefix + "/" + usernameRaw;
                        if (currentChat == null || !currentChat.noforwards) {
                            AndroidUtilities.addToClipboard(urlFinal);
                            undoView.showWithAction(0, UndoView.ACTION_USERNAME_COPIED, null);
                        }
                    }
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setUnderlineText(false);
                    ds.setColor(ds.linkColor);
                }
            };
            usernameSpans.put(usernameObj, span);
            return span;
        }

        @Override
        public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
            if (holder.getAdapterPosition() == setAvatarRow) {
                setAvatarCell = null;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            if (notificationRow != -1) {
                int position = holder.getAdapterPosition();
                return position == notificationRow || position == numberRow || position == privacyRow || position == languageRow || position == setUsernameRow || position == bioRow || position == versionRow || position == dataRow || position == chatRow || position == questionRow || position == devicesRow || position == filtersRow || position == stickersRow || position == faqRow || position == policyRow || position == sendLogsRow || position == sendLastLogsRow || position == clearLogsRow || position == switchBackendRow || position == setAvatarRow || position == addToGroupButtonRow || position == premiumRow || position == premiumGiftingRow || position == businessRow || position == liteModeRow || position == birthdayRow || position == channelRow || position == starsRow;
            }
            if (holder.itemView instanceof UserCell) {
                UserCell userCell = (UserCell) holder.itemView;
                Object object = userCell.getCurrentObject();
                if (object instanceof TLRPC.User) {
                    TLRPC.User user = (TLRPC.User) object;
                    if (UserObject.isUserSelf(user)) {
                        return false;
                    }
                }
            }
            int type = holder.getItemViewType();
            return type != VIEW_TYPE_HEADER && type != VIEW_TYPE_DIVIDER && type != VIEW_TYPE_SHADOW && type != VIEW_TYPE_EMPTY && type != VIEW_TYPE_BOTTOM_PADDING && type != VIEW_TYPE_SHARED_MEDIA && type != 9 && type != 10 && type != VIEW_TYPE_BOT_APP; // These are legacy ones, left for compatibility
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == infoHeaderRow || position == membersHeaderRow || position == settingsSectionRow2 || position == numberSectionRow || position == helpHeaderRow || position == debugHeaderRow || position == botPermissionsHeader) {
                return VIEW_TYPE_HEADER;
            } else if (position == phoneRow || position == locationRow || position == numberRow || position == birthdayRow) {
                return VIEW_TYPE_TEXT_DETAIL;
            } else if (position == usernameRow || position == setUsernameRow) {
                return VIEW_TYPE_TEXT_DETAIL_MULTILINE;
            } else if (position == userInfoRow || position == channelInfoRow || position == bioRow) {
                return VIEW_TYPE_ABOUT_LINK;
            } else if (position == settingsTimerRow || position == settingsKeyRow || position == reportRow || position == reportReactionRow || position == subscribersRow || position == subscribersRequestsRow || position == administratorsRow || position == settingsRow || position == blockedUsersRow || position == addMemberRow || position == joinRow || position == unblockRow || position == sendMessageRow || position == notificationRow || position == privacyRow || position == languageRow || position == dataRow || position == chatRow || position == questionRow || position == devicesRow || position == filtersRow || position == stickersRow || position == faqRow || position == policyRow || position == sendLogsRow || position == sendLastLogsRow || position == clearLogsRow || position == switchBackendRow || position == setAvatarRow || position == addToGroupButtonRow || position == addToContactsRow || position == liteModeRow || position == premiumGiftingRow || position == businessRow || position == botStarsBalanceRow || position == botTonBalanceRow || position == channelBalanceRow || position == botPermissionLocation || position == botPermissionBiometry || position == botPermissionEmojiStatus) {
                return VIEW_TYPE_TEXT;
            } else if (position == notificationsDividerRow) {
                return VIEW_TYPE_DIVIDER;
            } else if (position == notificationsRow) {
                return VIEW_TYPE_NOTIFICATIONS_CHECK;
            } else if (position == notificationsSimpleRow) {
                return VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE;
            } else if (position == lastSectionRow || position == membersSectionRow || position == secretSettingsSectionRow || position == settingsSectionRow || position == devicesSectionRow || position == helpSectionCell || position == setAvatarSectionRow || position == passwordSuggestionSectionRow || position == phoneSuggestionSectionRow || position == premiumSectionsRow || position == reportDividerRow || position == channelDividerRow || position == graceSuggestionSectionRow || position == balanceDividerRow || position == botPermissionsDivider || position == channelBalanceSectionRow) {
                return VIEW_TYPE_SHADOW;
            } else if (position >= membersStartRow && position < membersEndRow) {
                return VIEW_TYPE_USER;
            } else if (position == emptyRow) {
                return VIEW_TYPE_EMPTY;
            } else if (position == bottomPaddingRow) {
                return VIEW_TYPE_BOTTOM_PADDING;
            } else if (position == sharedMediaRow) {
                return VIEW_TYPE_SHARED_MEDIA;
            } else if (position == versionRow) {
                return VIEW_TYPE_VERSION;
            } else if (position == passwordSuggestionRow || position == phoneSuggestionRow || position == graceSuggestionRow) {
                return VIEW_TYPE_SUGGESTION;
            } else if (position == addToGroupInfoRow) {
                return VIEW_TYPE_ADDTOGROUP_INFO;
            } else if (position == premiumRow) {
                return VIEW_TYPE_PREMIUM_TEXT_CELL;
            } else if (position == starsRow) {
                return VIEW_TYPE_STARS_TEXT_CELL;
            } else if (position == bizLocationRow) {
                return VIEW_TYPE_LOCATION;
            } else if (position == bizHoursRow) {
                return VIEW_TYPE_HOURS;
            } else if (position == channelRow) {
                return VIEW_TYPE_CHANNEL;
            } else if (position == botAppRow) {
                return VIEW_TYPE_BOT_APP;
            } else if (position == infoSectionRow || position == infoAffiliateRow) {
                return VIEW_TYPE_SHADOW_TEXT;
            } else if (position == affiliateRow) {
                return VIEW_TYPE_COLORFUL_TEXT;
            }
            return 0;
        }
    }

    class TopView extends FrameLayout {
        public static final float D_CIRCLE_DP = 16f;
        private static final float DROPLET_WIDTH_DP = 33f;
        private static final float DROPLET_HEIGHT_DP = 13f;
        public final AnimatedFloat emojiLoadedT = new AnimatedFloat(this, 0, 440, CubicBezierInterpolator.EASE_OUT_QUINT);
        public final AnimatedFloat emojiFullT = new AnimatedFloat(this, 0, 440, CubicBezierInterpolator.EASE_OUT_QUINT);
        private final Paint paint;
        private final Path connection = new Path();
        private final Paint black = new Paint();
        private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable emoji = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(this, false, dp(20), AnimatedEmojiDrawable.CACHE_TYPE_ALERT_PREVIEW_STATIC);
        private final AnimatedFloat hasColorAnimated = new AnimatedFloat(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
        private final AnimatedColor color1Animated = new AnimatedColor(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
        private final AnimatedColor color2Animated = new AnimatedColor(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
        private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        public int color1, color2;
        private int emojiColor;
        private boolean emojiLoaded;
        private boolean hasEmoji;
        private boolean emojiIsCollectible;
        private boolean hasColorById;
        private int backgroundGradientColor1, backgroundGradientColor2, backgroundGradientHeight;
        private LinearGradient backgroundGradient;
        private int currentColor;
        private Rect blurBounds = new Rect();

        public TopView(@NonNull Context context) {
            super(context);
            setWillNotDraw(false);
            this.paint = new Paint();
            paint.setColor(getThemedColor(Theme.key_actionBarDefault));
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            int maxHeight = Math.max(topScroll, topBarsHeight);
            int height = (int) (maxHeight * (1.0f - mediaHeaderAnimationProgress));

            paint.setColor(currentColor);
            final int color1 = color1Animated.set(this.color1);
            final int color2 = color2Animated.set(this.color2);
            final int gradientHeight = AndroidUtilities.statusBarHeight + AndroidUtilities.dp(144);
            if (backgroundGradient == null || backgroundGradientColor1 != color1 || backgroundGradientColor2 != color2 || backgroundGradientHeight != gradientHeight) {
                backgroundGradient = new LinearGradient(0, 0, 0, backgroundGradientHeight = gradientHeight, new int[]{backgroundGradientColor2 = color2, backgroundGradientColor1 = color1}, new float[]{0, 1}, Shader.TileMode.CLAMP);
                backgroundPaint.setShader(backgroundGradient);
            }
            final float progressToGradient = hasColorAnimated.set(hasColorById);
            if (progressToGradient < 1) {
                canvas.drawRect(0, 0, getMeasuredWidth(), height, paint);
            }
            if (progressToGradient > 0) {
                backgroundPaint.setAlpha((int) (0xFF * progressToGradient));
                canvas.drawRect(0, 0, getMeasuredWidth(), height, backgroundPaint);
            }

            if (hasEmoji) {
                final float loadedScale = emojiLoadedT.set(isEmojiLoaded());
                final float full = emojiFullT.set(emojiIsCollectible);
                if (loadedScale > 0) {
                    canvas.save();
                    canvas.clipRect(0, 0, getMeasuredWidth(), height);
                    canvas.translate(getMeasuredWidth() / 2f, dp(90));
                    float emojiHeight = topScroll - dp(144);
                    float alpha = Math.min(1f, expandProgress);
                    StarGiftPatterns.drawPatternAnimated(canvas, StarGiftPatterns.TYPE_DEFAULT, emoji, getMeasuredWidth(), emojiHeight, alpha, 1f, 1f - expandProgress);
                    canvas.restore();
                }
            }

            if (topScroll >= minimizedOffset && topScroll <= expandedOffset && !avatarMaximizeAnimator.isRunning()) {
                float avatarSize = avatarContainer.getScaleY() * dp(AVATAR_SIZE_DP) / 2f;
                canvas.drawCircle((float) getWidth() / 2, avatarContainer.getTranslationY() + avatarSize, avatarSize, black);
                drawDroplet(canvas);
            }

            if (maxHeight != height) {
                int color = getThemedColor(Theme.key_windowBackgroundWhite);
                paint.setColor(color);
                blurBounds.set(0, maxHeight, getMeasuredWidth(), (int) height);
                contentView.drawBlurRect(canvas, getY(), blurBounds, paint, true);
            }

            if (parentLayout != null) {
                parentLayout.drawHeaderShadow(canvas, (int) (headerShadowAlpha * 255), maxHeight);
            }
        }


        private void drawDroplet(Canvas canvas) {
            float avatarTop = avatarContainer.getTranslationY();
            float avatarRadius = avatarContainer.getScaleY() * dp(AVATAR_SIZE_DP) / 2f;
            float progress = (1f - expandProgress);

            float centerX = getWidth() / 2f;
            float dropletHeight = dpf2(DROPLET_HEIGHT_DP) * progress;

            float flipProgress = 0.45f;
            float circleTouchProgress = 0.6f;
            float widenProgress = clamp01((progress - flipProgress) / (circleTouchProgress - flipProgress));

            float dropletWidth = lerp(dpf2(DROPLET_WIDTH_DP), avatarRadius * 2.8f, widenProgress);

            // Position of midpoint of cubic
            float p0X = -(dropletWidth / 2);
            float p0Y = 0;

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
                float initialAngle = (float) (2f * PI / 16f);
                float endAngle = (float) PI;
                float rotateProgress = clamp01((progress - flipProgress) / (1f - flipProgress));
                float angleFromTop = lerp(initialAngle, endAngle, rotateProgress);
                float angleRadians = (float) (-0.5f * PI) - angleFromTop;

                p3X = avatarRadius * (float) cos(angleRadians) - p0X;
                p3Y = (avatarTop + avatarRadius) + avatarRadius * (float) sin(angleRadians) - p0Y;

                float perpendicularAngle = (float) (angleRadians + 0.5f * PI);

                p2X = (float) (cos(perpendicularAngle) * dpf2(D_CIRCLE_DP) + p3X);
                p2Y = (float) (sin(perpendicularAngle) * dpf2(D_CIRCLE_DP) + p3Y);
            }

            for (int i = 0; i < 2; i++) {
                float direction = i == 0 ? -1 : 1;
                connection.reset();
                connection.moveTo(centerX + p0X * direction, p0Y);
                connection.rCubicTo(p1X * direction, p1Y, p2X * direction, p2Y, p3X * direction, p3Y);
                connection.lineTo(centerX, p0Y + p3Y);
                connection.lineTo(centerX, p0Y);
                connection.close();

                canvas.drawPath(connection, black);
            }
        }

        public void setBackgroundEmojiId(long emojiId, boolean isCollectible, boolean animated) {
            emoji.set(emojiId, animated);
            emoji.setColor(emojiColor);
            emojiIsCollectible = isCollectible;
            if (!animated) {
                emojiFullT.force(isCollectible);
            }
            hasEmoji = hasEmoji || emojiId != 0 && emojiId != -1;
            invalidate();
        }

        private boolean isEmojiLoaded() {
            if (emojiLoaded) {
                return true;
            }
            if (emoji != null && emoji.getDrawable() instanceof AnimatedEmojiDrawable) {
                AnimatedEmojiDrawable drawable = (AnimatedEmojiDrawable) emoji.getDrawable();
                if (drawable.getImageReceiver() != null && drawable.getImageReceiver().hasImageLoaded()) {
                    return emojiLoaded = true;
                }
            }
            return false;
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            emoji.attach();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            emoji.detach();
        }

        @Override
        public void setBackgroundColor(int color) {
            if (color != currentColor) {
                currentColor = color;
                paint.setColor(color);
                invalidate();
                if (!hasColorById) {
                    actionBarBackgroundColor = currentColor;
                }
            }
        }

        public void setBackgroundColorId(MessagesController.PeerColor peerColor, boolean animated) {
            if (peerColor != null) {
                hasColorById = true;
                color1 = peerColor.getBgColor1(Theme.isCurrentThemeDark());
                color2 = peerColor.getBgColor2(Theme.isCurrentThemeDark());
                actionBarBackgroundColor = ColorUtils.blendARGB(color1, color2, 0.25f);
                if (peerColor.patternColor != 0) {
                    emojiColor = peerColor.patternColor;
                } else {
                    emojiColor = PeerColorActivity.adaptProfileEmojiColor(color1);
                }
            } else {
                actionBarBackgroundColor = currentColor;
                hasColorById = false;
                if (AndroidUtilities.computePerceivedBrightness(getThemedColor(Theme.key_actionBarDefault)) > .8f) {
                    emojiColor = getThemedColor(Theme.key_windowBackgroundWhiteBlueText);
                } else if (AndroidUtilities.computePerceivedBrightness(getThemedColor(Theme.key_actionBarDefault)) < .2f) {
                    emojiColor = Theme.multAlpha(getThemedColor(Theme.key_actionBarDefaultTitle), .5f);
                } else {
                    emojiColor = PeerColorActivity.adaptProfileEmojiColor(getThemedColor(Theme.key_actionBarDefault));
                }
            }
            if (!animated) {
                color1Animated.set(color1, true);
                color2Animated.set(color2, true);
            }
            invalidate();
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
                if (editItemVisible) {
                    editItem.setScaleX(1f - value);
                    editItem.setScaleY(1f - value);
                    editItem.setAlpha(1f - value);
                }
                setScaleX(value);
                setScaleY(value);
                setAlpha(value);
            });
//            boolean expanded = expandPhoto;
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (isIndicatorVisible) {
//                        if (searchItem != null) {
//                            searchItem.setClickable(false);
//                        }
                        if (editItemVisible) {
                            editItem.setVisibility(GONE);
                        }
                    } else {
                        setVisibility(GONE);
                    }
                    updateStoriesViewBounds(false);
                }

                @Override
                public void onAnimationStart(Animator animation) {
//                    if (searchItem != null && !expanded) {
//                        searchItem.setClickable(true);
//                    }
                    if (editItemVisible) {
                        editItem.setVisibility(VISIBLE);
                    }
                    setVisibility(VISIBLE);
                    updateStoriesViewBounds(false);
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
            adapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    int count = avatarsViewPager.getRealCount();
                    if (overlayCountVisible == 0 && count > 1 && count <= 20 && overlaysView.isOverlaysVisible()) {
                        overlayCountVisible = 1;
                    }
                    invalidateIndicatorRect(false);
                    refreshVisibility(1f);
                    updateAvatarItems();
                }
            });
        }

        private void updateAvatarItemsInternal() {
            if (otherItem == null || avatarsViewPager == null) {
                return;
            }
            if (isPulledDown) {
                int position = avatarsViewPager.getRealPosition();
                if (position == 0) {
                    otherItem.hideSubItem(set_as_main);
                    otherItem.showSubItem(add_photo);
                } else {
                    otherItem.showSubItem(set_as_main);
                    otherItem.hideSubItem(add_photo);
                }
            }
        }

        private void updateAvatarItems() {
            if (imageUpdater == null) {
                return;
            }
            if (otherItem.isSubMenuShowing()) {
                AndroidUtilities.runOnUIThread(this::updateAvatarItemsInternal, 500);
            } else {
                updateAvatarItemsInternal();
            }
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
            setIndicatorVisible(isPulledDown && avatarsViewPager.getRealCount() > 20, durationFactor);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            invalidateIndicatorRect(false);
        }

        private void invalidateIndicatorRect(boolean pageChanged) {
            if (pageChanged) {
                overlaysView.saveCurrentPageProgress();
            }
            overlaysView.invalidate();
            final float textWidth = textPaint.measureText(getCurrentTitle());
            // TODO
//            indicatorRect.right = getMeasuredWidth() - AndroidUtilities.dp(54f) - (qrItem != null ? AndroidUtilities.dp(48) : 0);
            indicatorRect.right = getMeasuredWidth() - AndroidUtilities.dp(54f);
            indicatorRect.left = indicatorRect.right - (textWidth + AndroidUtilities.dpf2(16f));
            indicatorRect.top = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.dp(15f);
            indicatorRect.bottom = indicatorRect.top + AndroidUtilities.dp(26);
            setPivotX(indicatorRect.centerX());
            setPivotY(indicatorRect.centerY());
            invalidate();
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
            if (editItemVisible) {
                return editItem;
//            } else if (searchItem != null) {
//                return searchItem;
            } else {
                return null;
            }
        }
    }

    private class DiffCallback extends DiffUtil.Callback {

        int oldRowCount;

        SparseIntArray oldPositionToItem = new SparseIntArray();
        SparseIntArray newPositionToItem = new SparseIntArray();
        ArrayList<TLRPC.ChatParticipant> oldChatParticipant = new ArrayList<>();
        ArrayList<Integer> oldChatParticipantSorted = new ArrayList<>();
        int oldMembersStartRow;
        int oldMembersEndRow;

        @Override
        public int getOldListSize() {
            return oldRowCount;
        }

        @Override
        public int getNewListSize() {
            return rowCount;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            if (newItemPosition >= membersStartRow && newItemPosition < membersEndRow) {
                if (oldItemPosition >= oldMembersStartRow && oldItemPosition < oldMembersEndRow) {
                    TLRPC.ChatParticipant oldItem;
                    TLRPC.ChatParticipant newItem;
                    if (!oldChatParticipantSorted.isEmpty()) {
                        oldItem = oldChatParticipant.get(oldChatParticipantSorted.get(oldItemPosition - oldMembersStartRow));
                    } else {
                        oldItem = oldChatParticipant.get(oldItemPosition - oldMembersStartRow);
                    }

                    if (!sortedUsers.isEmpty()) {
                        newItem = visibleChatParticipants.get(visibleSortedUsers.get(newItemPosition - membersStartRow));
                    } else {
                        newItem = visibleChatParticipants.get(newItemPosition - membersStartRow);
                    }
                    return oldItem.user_id == newItem.user_id;
                }
            }
            int oldIndex = oldPositionToItem.get(oldItemPosition, -1);
            int newIndex = newPositionToItem.get(newItemPosition, -1);
            return oldIndex == newIndex && oldIndex >= 0;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return areItemsTheSame(oldItemPosition, newItemPosition);
        }

        public void fillPositions(SparseIntArray sparseIntArray) {
            sparseIntArray.clear();
            int pointer = 0;
            put(++pointer, setAvatarRow, sparseIntArray);
            put(++pointer, setAvatarSectionRow, sparseIntArray);
            put(++pointer, numberSectionRow, sparseIntArray);
            put(++pointer, numberRow, sparseIntArray);
            put(++pointer, setUsernameRow, sparseIntArray);
            put(++pointer, bioRow, sparseIntArray);
            put(++pointer, phoneSuggestionRow, sparseIntArray);
            put(++pointer, phoneSuggestionSectionRow, sparseIntArray);
            put(++pointer, passwordSuggestionRow, sparseIntArray);
            put(++pointer, passwordSuggestionSectionRow, sparseIntArray);
            put(++pointer, graceSuggestionRow, sparseIntArray);
            put(++pointer, graceSuggestionSectionRow, sparseIntArray);
            put(++pointer, settingsSectionRow, sparseIntArray);
            put(++pointer, settingsSectionRow2, sparseIntArray);
            put(++pointer, notificationRow, sparseIntArray);
            put(++pointer, languageRow, sparseIntArray);
            put(++pointer, premiumRow, sparseIntArray);
            put(++pointer, starsRow, sparseIntArray);
            put(++pointer, businessRow, sparseIntArray);
            put(++pointer, premiumSectionsRow, sparseIntArray);
            put(++pointer, premiumGiftingRow, sparseIntArray);
            put(++pointer, privacyRow, sparseIntArray);
            put(++pointer, dataRow, sparseIntArray);
            put(++pointer, liteModeRow, sparseIntArray);
            put(++pointer, chatRow, sparseIntArray);
            put(++pointer, filtersRow, sparseIntArray);
            put(++pointer, stickersRow, sparseIntArray);
            put(++pointer, devicesRow, sparseIntArray);
            put(++pointer, devicesSectionRow, sparseIntArray);
            put(++pointer, helpHeaderRow, sparseIntArray);
            put(++pointer, questionRow, sparseIntArray);
            put(++pointer, faqRow, sparseIntArray);
            put(++pointer, policyRow, sparseIntArray);
            put(++pointer, helpSectionCell, sparseIntArray);
            put(++pointer, debugHeaderRow, sparseIntArray);
            put(++pointer, sendLogsRow, sparseIntArray);
            put(++pointer, sendLastLogsRow, sparseIntArray);
            put(++pointer, clearLogsRow, sparseIntArray);
            put(++pointer, switchBackendRow, sparseIntArray);
            put(++pointer, versionRow, sparseIntArray);
            put(++pointer, emptyRow, sparseIntArray);
            put(++pointer, bottomPaddingRow, sparseIntArray);
            put(++pointer, infoHeaderRow, sparseIntArray);
            put(++pointer, phoneRow, sparseIntArray);
            put(++pointer, locationRow, sparseIntArray);
            put(++pointer, userInfoRow, sparseIntArray);
            put(++pointer, channelInfoRow, sparseIntArray);
            put(++pointer, usernameRow, sparseIntArray);
            put(++pointer, notificationsDividerRow, sparseIntArray);
            put(++pointer, reportDividerRow, sparseIntArray);
            put(++pointer, notificationsRow, sparseIntArray);
            put(++pointer, infoSectionRow, sparseIntArray);
            put(++pointer, affiliateRow, sparseIntArray);
            put(++pointer, infoAffiliateRow, sparseIntArray);
            put(++pointer, sendMessageRow, sparseIntArray);
            put(++pointer, reportRow, sparseIntArray);
            put(++pointer, reportReactionRow, sparseIntArray);
            put(++pointer, addToContactsRow, sparseIntArray);
            put(++pointer, settingsTimerRow, sparseIntArray);
            put(++pointer, settingsKeyRow, sparseIntArray);
            put(++pointer, secretSettingsSectionRow, sparseIntArray);
            put(++pointer, membersHeaderRow, sparseIntArray);
            put(++pointer, addMemberRow, sparseIntArray);
            put(++pointer, subscribersRow, sparseIntArray);
            put(++pointer, subscribersRequestsRow, sparseIntArray);
            put(++pointer, administratorsRow, sparseIntArray);
            put(++pointer, settingsRow, sparseIntArray);
            put(++pointer, blockedUsersRow, sparseIntArray);
            put(++pointer, membersSectionRow, sparseIntArray);
            put(++pointer, channelBalanceSectionRow, sparseIntArray);
            put(++pointer, sharedMediaRow, sparseIntArray);
            put(++pointer, unblockRow, sparseIntArray);
            put(++pointer, addToGroupButtonRow, sparseIntArray);
            put(++pointer, addToGroupInfoRow, sparseIntArray);
            put(++pointer, joinRow, sparseIntArray);
            put(++pointer, lastSectionRow, sparseIntArray);
            put(++pointer, notificationsSimpleRow, sparseIntArray);
            put(++pointer, bizHoursRow, sparseIntArray);
            put(++pointer, bizLocationRow, sparseIntArray);
            put(++pointer, birthdayRow, sparseIntArray);
            put(++pointer, channelRow, sparseIntArray);
            put(++pointer, botStarsBalanceRow, sparseIntArray);
            put(++pointer, botTonBalanceRow, sparseIntArray);
            put(++pointer, channelBalanceRow, sparseIntArray);
            put(++pointer, balanceDividerRow, sparseIntArray);
            put(++pointer, botAppRow, sparseIntArray);
            put(++pointer, botPermissionsHeader, sparseIntArray);
            put(++pointer, botPermissionLocation, sparseIntArray);
            put(++pointer, botPermissionEmojiStatus, sparseIntArray);
            put(++pointer, botPermissionBiometry, sparseIntArray);
            put(++pointer, botPermissionsDivider, sparseIntArray);
            put(++pointer, channelDividerRow, sparseIntArray);
        }

        private void put(int id, int position, SparseIntArray sparseIntArray) {
            if (position >= 0) {
                sparseIntArray.put(position, id);
            }
        }
    }

    private class NestedFrameLayout extends SizeNotifierFrameLayout implements NestedScrollingParent3 {

        private NestedScrollingParentHelper nestedScrollingParentHelper;

        public NestedFrameLayout(Context context) {
            super(context);
            nestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        }

        @Override
        public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, int[] consumed) {
            try {
                if (target == listView && sharedMediaLayoutAttached) {
                    RecyclerListView innerListView = sharedMediaLayout.getCurrentListView();
                    int top = sharedMediaLayout.getTop();
                    if (top == topBarsHeight) {
                        consumed[1] = dyUnconsumed;
                        innerListView.scrollBy(0, dyUnconsumed);
                    }
                }
                if (dyConsumed != 0 && type == TYPE_TOUCH) {
                    hideFloatingButton(!(sharedMediaLayout == null || sharedMediaLayout.getClosestTab() == SharedMediaLayout.TAB_STORIES || sharedMediaLayout.getClosestTab() == SharedMediaLayout.TAB_ARCHIVED_STORIES) || dyConsumed > 0);
                }
            } catch (Throwable e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> {
                    try {
                        RecyclerListView innerListView = sharedMediaLayout.getCurrentListView();
                        if (innerListView != null && innerListView.getAdapter() != null) {
                            innerListView.getAdapter().notifyDataSetChanged();
                        }
                    } catch (Throwable e2) {

                    }
                });
            }
        }

        @Override
        public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {

        }

        @Override
        public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
            return super.onNestedPreFling(target, velocityX, velocityY);
        }

        @Override
        public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int type) {
            if (target == listView && sharedMediaRow != -1 && sharedMediaLayoutAttached) {
                boolean searchVisible = actionBar.isSearchFieldVisible();
                int t = sharedMediaLayout.getTop() - topBarsHeight;
                if (dy < 0) {
                    boolean scrolledInner = false;
                    if (t <= 0) {
                        RecyclerListView innerListView = sharedMediaLayout.getCurrentListView();
                        if (innerListView != null) {
                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) innerListView.getLayoutManager();
                            int pos = linearLayoutManager.findFirstVisibleItemPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                RecyclerView.ViewHolder holder = innerListView.findViewHolderForAdapterPosition(pos);
                                int top = holder != null ? holder.itemView.getTop() : -1;
                                int paddingTop = innerListView.getPaddingTop();
                                if (top != paddingTop || pos != 0) {
                                    consumed[1] = pos != 0 ? dy : Math.max(dy, (top - paddingTop));
                                    innerListView.scrollBy(0, dy);
                                    scrolledInner = true;
                                }
                            }
                        }
                    }
                    if (searchVisible) {
                        if (!scrolledInner && t < 0) {
                            consumed[1] = dy - Math.max(t, dy);
                        } else {
                            consumed[1] = dy;
                        }
                    }
                } else {
                    if (searchVisible) {
                        RecyclerListView innerListView = sharedMediaLayout.getCurrentListView();
                        consumed[1] = dy;
                        if (t > 0) {
                            consumed[1] -= dy;
                        }
                        if (innerListView != null && consumed[1] > 0) {
                            innerListView.scrollBy(0, consumed[1]);
                        }
                    }
                }
            }
        }

        @Override
        public boolean onStartNestedScroll(View child, View target, int axes, int type) {
            return sharedMediaRow != -1 && axes == ViewCompat.SCROLL_AXIS_VERTICAL;
        }

        @Override
        public void onNestedScrollAccepted(View child, View target, int axes, int type) {
            nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        }

        @Override
        public void onStopNestedScroll(View target, int type) {
            nestedScrollingParentHelper.onStopNestedScroll(target);
        }

        @Override
        public void onStopNestedScroll(View child) {

        }

        @Override
        protected void drawList(Canvas blurCanvas, boolean top, ArrayList<IViewWithInvalidateCallback> views) {
            super.drawList(blurCanvas, top, views);
            blurCanvas.save();
            blurCanvas.translate(0, listView.getY());
            sharedMediaLayout.drawListForBlur(blurCanvas, views);
            blurCanvas.restore();
        }
    }
}

