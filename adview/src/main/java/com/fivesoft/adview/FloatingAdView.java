package com.fivesoft.adview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.fivesoft.smartutil.ViewUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class FloatingAdView extends LinearLayout {

    public static final String AD_UNIT_TEST = "ca-app-pub-3940256099942544/6300978111";

    private boolean promotePremiumVersion = true;
    private boolean initializeMobileAds = true;
    private String premiumPackage;
    private int minGetPremiumPromoDisplay = 5000;
    private String adUnitId = AD_UNIT_TEST;
    private int adColor = Color.WHITE;

    private AdView adView;
    private View goPremium;
    private LinearLayout mainLinear;

    private OnUserWantsPremiumListener listener;

    private long promoDisplayStart = 0;

    private int titleColor;
    private int subtitleColor;

    private String title;
    private String subtitle;

    public FloatingAdView(@NonNull Context context) {
        super(context);
        init();
    }

    public FloatingAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setAttrs(attrs);
        init();
    }

    public FloatingAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttrs(attrs);
        init();
    }

    /**
     * Sets listener called when user clicks on
     * promotion banner of your app's premium version.
     *
     * When you set it to null, user will be directed to your app's page in
     * Google Play. (You must set premium version package name in xml as parameter "premiumVersionPackageName" in xml firstly,
     * otherwise they will be directed to normal app's page.)
     *
     * WARNING!
     * If you want to promote PREMIUM, you must set "promotePremium" parameter
     * to true in xml, otherwise the banner will never be displayed.
     * @param listener The listener.
     */

    public void setOnUserWantsPremiumListener(OnUserWantsPremiumListener listener){
        this.listener = listener;
    }

    public interface OnUserWantsPremiumListener{
        void onUserWantsPremium();
    }

    private void setAttrs(AttributeSet attrs){
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.FloatingAdView, 0, 0);

        try {
            adUnitId = a.getString(R.styleable.FloatingAdView_adUnitId);
            if(adUnitId == null || adUnitId.equalsIgnoreCase("TEST"))
                adUnitId = AD_UNIT_TEST;
            promotePremiumVersion = a.getBoolean(R.styleable.FloatingAdView_promotePremium, false);
            premiumPackage = a.getString(R.styleable.FloatingAdView_premiumVersionPackageName);
            if(premiumPackage == null)
                premiumPackage = getContext().getPackageName();
            minGetPremiumPromoDisplay = a.getInt(R.styleable.FloatingAdView_minPremiumPromoDisplayTime, 5000);
            adColor = a.getColor(R.styleable.FloatingAdView_adColor, Color.WHITE);

            titleColor = a.getColor(R.styleable.FloatingAdView_adTitleTextColor, getColorFromAttr(R.attr.colorPrimary));
            subtitleColor = a.getColor(R.styleable.FloatingAdView_adSubtitleTextColor, Color.parseColor("#494949"));

            title = a.getString(R.styleable.FloatingAdView_adTitle);
            subtitle = a.getString(R.styleable.FloatingAdView_adSubtitle);

            initializeMobileAds = a.getBoolean(R.styleable.FloatingAdView_initializeMobileAds, true);

            if(title == null)
                title = "Go Premium!";

            if(subtitle == null)
                subtitle = "Don't want ads?";

        } finally {
            a.recycle();
        }
    }

    private void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.fivesoft_floating_ad_view_main, this);
        mainLinear = findViewById(R.id.main_linear);
        goPremium = initGoPremiumView();
        mainLinear.removeAllViews();
        mainLinear.addView(goPremium);
        promoDisplayStart = System.currentTimeMillis();
        setBackgroundColor(adColor);

        if(initializeMobileAds) {
            MobileAds.initialize(getContext(), initializationStatus -> {
                loadAd(adUnitId);
            });
        } else {
            loadAd(adUnitId);
        }
    }

    private void loadAd(String adUnit){
        post(() -> {

            adView = new AdView(getContext());
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(adUnit);
            adView.setBackgroundColor(Color.RED);
            mainLinear.addView(adView);
            ViewUtil.setViewHeight(adView, mainLinear.getHeight());

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    if(a == null)
                        return;
                    try {
                        a.cancel();
                        goPremium.setAlpha(1);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAdLoaded() {
                    postDelayed(() -> {
                        updateAdViewBackgroundColor();
                        setAdViewVisible(true);
                    }, promotePremiumVersion ? Math.max(0, minGetPremiumPromoDisplay - (System.currentTimeMillis() - promoDisplayStart)) : 300);
                }

                @Override
                public void onAdImpression() {
                    updateAdViewBackgroundColor();
                }
            });

            if(!adUnitId.equalsIgnoreCase("ALWAYS_LOADING"))
                try {
                    adView.loadAd(adRequest);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
        });
    }

    private void updateAdViewBackgroundColor(){
        new Thread(() -> post(() -> {
            try {
                adView.setBackgroundColor(getAdDominantColor());
            } catch (Exception e){
                e.printStackTrace();
            }
        })).start();
    }

    private Bitmap loadBitmapFromView(View v) {
        try {
            try {
                Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                v.draw(c);
                return b;
            } catch (OutOfMemoryError e){
                return  null;
            }
        } catch (Exception e){
            return null;
        }
    }

    private int getAdDominantColor(){
        try {
            Bitmap adBitmap = loadBitmapFromView(adView);

            if (adBitmap == null)
                return Color.TRANSPARENT;

            return adBitmap.getPixel(100, 50);
        } catch (Exception e){
            return Color.TRANSPARENT;
        }
    }

    private ObjectAnimator a;

    private View initGoPremiumView(){
        View view = LayoutInflater.from(getContext()).inflate(promotePremiumVersion ? R.layout.fivesoft_floating_ad_view_go_premium_view : R.layout.fivesoft_floating_ad_view_ad_loading_view, mainLinear, false);

        ImageView icon = view.findViewById(R.id.icon);

        LinearLayout main = view.findViewById(R.id.main);

        TextView titleTv = view.findViewById(R.id.title);
        TextView subtitleTv = view.findViewById(R.id.subtitle);

        if(promotePremiumVersion){
            icon.setImageDrawable(getContext().getApplicationInfo().loadIcon(getContext().getPackageManager()));
            main.setOnClickListener(v -> {
                if(listener == null)
                    openAppPlayStore(premiumPackage);
                else
                    listener.onUserWantsPremium();
            });

            titleTv.setTextColor(titleColor);
            subtitleTv.setTextColor(subtitleColor);

            titleTv.setText(title);
            subtitleTv.setText(subtitle);

        } else {

            titleTv.setBackgroundColor(subtitleColor);
            titleTv.setAlpha(0.5f);
            subtitleTv.setBackgroundColor(subtitleColor);
            subtitleTv.setAlpha(0.3f);
            icon.setImageBitmap(null);
            icon.setBackgroundColor(subtitleColor);
            icon.setAlpha(0.4f);

            a = ObjectAnimator.ofFloat(view, "alpha", 0.1f, 1);
            a.setDuration(800);
            a.setInterpolator(new AccelerateDecelerateInterpolator());
            a.setRepeatCount(ObjectAnimator.INFINITE);
            a.setRepeatMode(ObjectAnimator.REVERSE);
            a.start();
        }

        return view;
    }

    private void setAdViewVisible(boolean b){

        if(b ? goPremium.getY() != 0.0f : goPremium.getY() == 0.0f) {
            return;
        }

        ValueAnimator a = ValueAnimator.ofFloat(0, getHeight());
        a.addUpdateListener(animation -> {
            goPremium.setY((float) animation.getAnimatedValue() * (b ? -1 : 1));
            adView.setY(getHeight() + (float) animation.getAnimatedValue() * (b ? -1 : 1));
        });
        a.setDuration(500);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        a.start();

    }

    private void openAppPlayStore(String packageName){
        try {
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        CardView cardView = findViewById(R.id.main_cv);
        cardView.setCardBackgroundColor(color);
    }

    private int getColorFromAttr(@AttrRes int colorRes) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[] { colorRes });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }
}
