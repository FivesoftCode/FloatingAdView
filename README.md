# FloatingAdView
Neat floating ad view. (AdMob)

### Usage

Implementation: 

    implementation 'com.github.FivesoftCode:FloatingAdView:2.1.0'
    
Creating ad view in xml:

    <com.fivesoft.adview.FloatingAdView
        android:id="@+id/adView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:adUnitId="ca-app-pub-6136869055982189/5541117125"
        app:testDeviceId="6317935EBEA69A3942EB498613892463"
        app:adColor="@color/white"
        app:adSubtitleTextColor="@color/greyDark2"
        app:promotePremium="true"
        app:minPremiumPromoDisplayTime="5500"
        app:layout_constraintBottom_toTopOf="@+id/navigation_bar"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent">
        

