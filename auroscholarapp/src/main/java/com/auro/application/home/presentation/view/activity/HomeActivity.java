package com.auro.application.home.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.auro.application.R;
import com.auro.application.core.application.AuroApp;
import com.auro.application.core.application.base_component.BaseActivity;
import com.auro.application.core.application.di.component.ViewModelFactory;
import com.auro.application.core.common.AppConstant;
import com.auro.application.core.common.CommonCallBackListner;
import com.auro.application.core.common.CommonDataModel;
import com.auro.application.core.common.FragmentUtil;
import com.auro.application.core.common.NotificationDataModel;
import com.auro.application.core.common.OnItemClickListener;
import com.auro.application.core.common.SdkCallBack;
import com.auro.application.core.common.Status;
import com.auro.application.core.database.AuroAppPref;
import com.auro.application.core.database.PrefModel;
import com.auro.application.core.util.AuroScholar;
import com.auro.application.databinding.ActivityDashboardBinding;
import com.auro.application.home.data.datasource.remote.HomeRemoteApi;
import com.auro.application.home.data.model.AuroScholarDataModel;
import com.auro.application.home.data.model.AuroScholarInputModel;
import com.auro.application.home.data.model.DashboardResModel;
import com.auro.application.home.data.model.Details;
import com.auro.application.home.data.model.response.DynamiclinkResModel;
import com.auro.application.home.presentation.view.fragment.CertificateFragment;
import com.auro.application.home.presentation.view.fragment.FriendsLeaderBoardListFragment;
import com.auro.application.home.presentation.view.fragment.KYCViewFragment;
import com.auro.application.home.presentation.viewmodel.AuroScholarDashBoardViewModel;
import com.auro.application.home.presentation.viewmodel.HomeViewModel;
import com.auro.application.payment.presentation.view.fragment.SendMoneyFragment;
import com.auro.application.teacher.data.model.response.MyClassRoomTeacherResModel;
import com.auro.application.teacher.data.model.response.MyProfileResModel;



import com.auro.application.teacher.presentation.view.fragment.TeacherKycInfoFragment;
import com.auro.application.teacher.presentation.view.fragment.InformationDashboardFragment;
import com.auro.application.teacher.presentation.view.fragment.MyClassRoomGroupFragment;
import com.auro.application.teacher.presentation.view.fragment.TeacherUserProfileFragment;
import com.auro.application.teacher.presentation.viewmodel.MyClassroomViewModel;
import com.auro.application.util.AppLogger;
import com.auro.application.util.AppUtil;
import com.auro.application.util.ConversionUtil;
import com.auro.application.util.TextUtil;
import com.auro.application.util.ViewUtil;
import com.auro.application.util.alert_dialog.LanguageChangeDialog;
import com.auro.application.util.firebaseAnalytics.AnalyticsRegistry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.instabug.apm.APM;
import com.instabug.bug.BugReporting;
import com.instabug.crash.CrashReporting;
import com.instabug.library.Feature;
import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;
import com.instabug.library.ui.onboarding.WelcomeMessage;
import com.instabug.library.visualusersteps.State;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import kotlin.LateinitKt;

public class HomeActivity extends BaseActivity implements OnItemClickListener, View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener , CommonCallBackListner {


    private final String TAG = HomeActivity.class.getSimpleName();
    InstallReferrerClient referrerClient;
    @Inject
    HomeRemoteApi remoteApi;
    Details details;
    PrefModel prefModel;
    DashboardResModel dashboardResModel;
    @Inject
    @Named("HomeActivity")
    ViewModelFactory viewModelFactory;
    AuroScholarInputModel inputModel;
    String deviceToken = "";
    private ActivityDashboardBinding binding;
    private Context mContext;
   // private MyClassroomViewModel viewModel;
   private HomeViewModel viewModel;
    private AuroScholarDashBoardViewModel auroviewModel;
    private static int LISTING_ACTIVE_FRAGMENT = 0;
    int backPress = 0;

    public static final int TEACHER_KYC_FRAGMENT = 01;
    public static final int TEACHER_KYC_UPLOAD_FRAGMENT = 02;
    public static final int TEACHER_DASHBOARD_FRAGMENT = 03;
    public static final int TEACHER_PROFILE_FRAGMENT = 04;
    public static final int TEACHER_MY_CLASSROOM_FRAGMENT = 05;
    public static final int TEACHER_BOOK_SLOT_GROUP_FRAGMENT = 06;


    String memberType;
    CommonCallBackListner commonCallBackListner;
    public static int screenHeight = 0;
    public static int screenWidth = 0;

    AuroScholarDataModel auroScholarDataModel;


    public static int getListingActiveFragment() {
        return LISTING_ACTIVE_FRAGMENT;
    }

    public static void setListingActiveFragment(int listingActiveFragment) {
        LISTING_ACTIVE_FRAGMENT = listingActiveFragment;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        ViewUtil.setLanguageonUi(this);

        details = AuroAppPref.INSTANCE.getModelInstance().getLanguageMasterDynamic().getDetails();

        referrerClient = InstallReferrerClient.newBuilder(this).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        ReferrerDetails response = null;
                        try {

                            response = referrerClient.getInstallReferrer();
                            String referrerUrl = response.getInstallReferrer();
                            long referrerClickTime = response.getReferrerClickTimestampSeconds();
                            long appInstallTime = response.getInstallBeginTimestampSeconds();
                            boolean instantExperienceLaunched = response.getGooglePlayInstantParam();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app.
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        // Connection couldn't be established.
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });








        new Instabug.Builder(getApplication(),"ed30d18815acf92a8e7a3391ddf2ac1c").
                setInvocationEvents(InstabugInvocationEvent.NONE).
                build();
        Instabug.setReproStepsState(State.ENABLED);
//        Instabug.show();
        BugReporting.setShakingThreshold(800);
        Instabug.setSessionProfilerState(Feature.State.ENABLED);
        CrashReporting.setState(Feature.State.ENABLED);
        CrashReporting.setAnrState(Feature.State.ENABLED);
        CrashReporting.reportException(new NullPointerException("Test issue"));
        CrashReporting.reportException(new NullPointerException("Test issue"), "Exception identifier");
        CrashReporting.setNDKCrashesState(Feature.State.ENABLED);
        APM.setColdAppLaunchEnabled(true);
        APM.setHotAppLaunchEnabled(true);
        APM.endAppLaunch();
        Instabug.setTrackingUserStepsState(Feature.State.ENABLED);
        Instabug.setWelcomeMessageState(WelcomeMessage.State.LIVE);
        Instabug.showWelcomeMessage(WelcomeMessage.State.LIVE);

        init();
        setListener();
        //callRefferApi();
    }

//    public void callRefferApi() {
//        AppLogger.e("callRefferApi","step 1");
//        try {
//
//            DynamiclinkResModel dynamiclinkResModel = new DynamiclinkResModel();
//            dynamiclinkResModel.setReffeUserId(AuroAppPref.INSTANCE.getModelInstance().getStudentData().getUserId());
//            dynamiclinkResModel.setSource(AppConstant.AURO_ID);
//            dynamiclinkResModel.setNavigationTo(AppConstant.NavigateToScreen.STUDENT_DASHBOARD);
//            dynamiclinkResModel.setReffer_type("" + AppConstant.UserType.TEACHER);
//            viewModel.checkInternet(dynamiclinkResModel, Status.DYNAMIC_LINK_API);
//        }catch (Exception e)
//        {
//
//        }
//    }

    @Override
    protected void init() {
        memberType = "Member";
        AppUtil.commonCallBackListner = this;
        binding = DataBindingUtil.setContentView(this, getLayout());
        ((AuroApp) this.getApplication()).getAppComponent().doInjection(this);
        //view model and handler setup
       // viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyClassroomViewModel.class);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeViewModel.class);

        binding.setLifecycleOwner(this);
        mContext = HomeActivity.this;
        //funnelTeacherDashBoard();
        setLightStatusBar(this);
        if (getIntent() != null && getIntent().getParcelableExtra(AppConstant.AURO_DATA_MODEL) != null) {
            auroScholarDataModel = (AuroScholarDataModel) getIntent().getParcelableExtra(AppConstant.AURO_DATA_MODEL);
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
        // printHashKey(this);
        setHomeFragmentTab();

        PrefModel prefModel = AuroAppPref.INSTANCE.getModelInstance();
        prefModel.setTeacherProfileScreen(false);
        prefModel.setLogin(true);
        AuroAppPref.INSTANCE.setPref(prefModel);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            auroStudentscholarSdk(1);
        }
        // setHomeFragmentTab();

//        DynamiclinkResModel dynamiclinkResModel = new DynamiclinkResModel();
//        dynamiclinkResModel.setNavigationTo(AppConstant.NavigateToScreen.STUDENT_DASHBOARD);
//        dynamiclinkResModel.setRefferMobileno("9289180019");
//        dynamiclinkResModel.setSource("AuroScholar");
//        dynamiclinkResModel.setNavigationTo(AppConstant.NavigateToScreen.STUDENT_DASHBOARD);
//        dynamiclinkResModel.setReffer_type(AppConstant.NavigateToScreen.TEACHER);
//        viewModel.getDynamicData(dynamiclinkResModel);
        //callProfileApi();

    }

    @Override
    protected void setListener() {
        /*set listner here*/
        if (viewModel != null && viewModel.serviceLiveData().hasObservers()) {
            viewModel.serviceLiveData().removeObservers(this);
        } else {
            observeServiceResponse();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public void auroStudentscholarSdk(int status) {
        prefModel = AuroAppPref.INSTANCE.getModelInstance();
        inputModel = new AuroScholarInputModel();
        // Important Params
        //  inputModel.setMobileNumber(prefModel.getUserMobile());
//        if (!prefModel.getCheckUserResModel().getUserDetails().get(0).getUserMobile().isEmpty()||!prefModel.getCheckUserResModel().getUserDetails().get(0).getUserMobile().equals("")){
//            inputModel.setMobileNumber(prefModel.getCheckUserResModel().getUserDetails().get(0).getUserMobile());
//        }
//        else{
//
//            inputModel.setMobileNumber(prefModel.getStudentData().getUserMobile());
//        }
        inputModel.setMobileNumber("9289180019");

        String newdeviceToken = deviceToken;
        if (!TextUtil.isEmpty(newdeviceToken)) {
            inputModel.setDeviceToken(newdeviceToken);
            AppLogger.v("sdkDeviceToken", newdeviceToken);
        }

        //Mandatory
        inputModel.setStudentClass(prefModel.getStudentData().getGrade());

        if (prefModel.getDynamiclinkResModel() != null && !TextUtil.isEmpty(prefModel.getDynamiclinkResModel().getSource())) {
            inputModel.setRegitrationSource(prefModel.getDynamiclinkResModel().getSource());
        } else {
            inputModel.setRegitrationSource("AuroScholr");
        }

        inputModel.setActivity(this); //Mandatory
        inputModel.setFragmentContainerUiId(R.id.home_container);
        //Mandatory
        inputModel.setReferralLink("");
        inputModel.setPartnerSource(AppConstant.AURO_ID); //this id is provided by auroscholar for valid partner
        //  inputModel.setPartnerSource("IDREMDvF4g");  //Demo
        inputModel.setSdkcallback(new SdkCallBack() {
            @Override
            public void callBack(String message) {

            }

            @Override
            public void logOut() {
                AppLogger.e("Chhonker", "Logout");
                int userType = prefModel.getUserType();
                //   mgoogleSignInHelper.signOut();
                SharedPreferences preferences =getSharedPreferences("My_Pref",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();
                SharedPreferences.Editor editor1 = getSharedPreferences("My_Pref", MODE_PRIVATE).edit();
                editor1.putString("statusparentprofile", "false");
                editor1.putString("statusfillstudentprofile", "false");
                editor1.putString("statussetpasswordscreen", "false");
                editor1.putString("statuschoosegradescreen", "false");
                editor1.putString("statuschoosedashboardscreen", "false");
                editor1.putString("statusopenprofilewithoutpin", "false");
                editor.putString("statussubjectpref","false");

                editor1.apply();
                AuroAppPref.INSTANCE.clearPref();

                funnelStudentLogOut();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                }
            }

            @Override
            public void commonCallback(Status status, Object o) {
                switch (status) {
                    case NAV_CHANGE_GRADE_CLICK:

                        break;
                }
            }
        });
        setRequiredData();
        /*Update Dynamic  to empty*/
        PrefModel prefModel = AuroAppPref.INSTANCE.getModelInstance();
        if (status == 0) {
            AppLogger.e("notification ", "step 9 ");

            DynamiclinkResModel model = prefModel.getDynamiclinkResModel();
            dashboardResModel = prefModel.getDashboardResModel();
            if (model != null && !TextUtil.isEmpty(model.getNavigationTo()) && model.getNavigationTo().equalsIgnoreCase(AppConstant.NavigateToScreen.STUDENT_KYC)) {

                if (prefModel.getDashboardResModel() != null && auroviewModel.homeUseCase.checkKycStatus(dashboardResModel)) {
                    openKYCViewFragment(dashboardResModel, 1);
                } else {
                    openKYCViewFragment(dashboardResModel, 1);
                }

            } else if (model != null && !TextUtil.isEmpty(model.getNavigationTo()) && model.getNavigationTo().equalsIgnoreCase(AppConstant.NavigateToScreen.STUDENT_CERTIFICATE)) {

                openCertificate();

            } else if (model != null && !TextUtil.isEmpty(model.getNavigationTo()) && model.getNavigationTo().equalsIgnoreCase(AppConstant.NavigateToScreen.PAYMENT_TRANSFER)) {

                if (prefModel.getDashboardResModel() != null) {
                    int approvedMoney = ConversionUtil.INSTANCE.convertStringToInteger(dashboardResModel.getApproved_scholarship_money());
                    if (approvedMoney > 0) {
                        openSendMoneyFragment();
                    } else {
                        openFragment(AuroScholar.startAuroSDK(inputModel));
                    }
                } else {
                    openFragment(AuroScholar.startAuroSDK(inputModel));
                }


            } else {
                openFragment(AuroScholar.startAuroSDK(inputModel));
            }
        } else {
            AppLogger.e("notification ", "step 10 ");

            NotificationDataModel notificationDataModel = prefModel.getNotificationDataModel();
            if (notificationDataModel.getNavigateto().equalsIgnoreCase(AppConstant.NavigateToScreen.STUDENT_KYC)) {
                AppLogger.e("notification ", "step 11 ");
                if (prefModel.getDashboardResModel() != null && auroviewModel.homeUseCase.checkKycStatus(dashboardResModel)) {
                    openKYCViewFragment(dashboardResModel, 1);
                    AppLogger.e("notification ", "step 12 ");
                } else {
                    openKYCViewFragment(dashboardResModel, 1);
                    AppLogger.e("notification ", "step 13 ");

                }

            } else if (notificationDataModel.getNavigateto().equalsIgnoreCase(AppConstant.NavigateToScreen.STUDENT_CERTIFICATE)) {
                openCertificate();
            } else if (notificationDataModel.getNavigateto().equalsIgnoreCase(AppConstant.NavigateToScreen.PAYMENT_TRANSFER)) {
                if (prefModel.getDashboardResModel() != null) {

                    int approvedMoney = ConversionUtil.INSTANCE.convertStringToInteger(dashboardResModel.getApproved_scholarship_money());
                    if (approvedMoney > 0) {
                        openSendMoneyFragment();
                    } else {
                        openFragment(AuroScholar.startAuroSDK(inputModel));
                    }
                } else {
                    openFragment(AuroScholar.startAuroSDK(inputModel));
                }


            } else if (notificationDataModel.getNavigateto().equalsIgnoreCase(AppConstant.NavigateToScreen.FRIENDS_LEADERBOARD)) {
                openLeaderBoardFragment(new FriendsLeaderBoardListFragment());

            } else {
                openFragment(AuroScholar.startAuroSDK(inputModel));
            }
            prefModel.setNotificationDataModel(null);

            // AppLogger.e("setStringPref-", "first time 1---");
            AuroAppPref.INSTANCE.setPref(prefModel);
        }

        /* else if (model.getSource().equalsIgnoreCase(AppConstant.NavigateToScreen.FRIENDS_LEADERBOARD)) {
        }*/

    }
    private void setRequiredData() {
        /*Set data before call any fragment in auroscholarActivity*/
        AuroScholarDataModel auroScholarDataModel = new AuroScholarDataModel();
        auroScholarDataModel.setMobileNumber(inputModel.getMobileNumber());
        auroScholarDataModel.setStudentClass(inputModel.getStudentClass());
        auroScholarDataModel.setRegitrationSource(inputModel.getRegitrationSource());
        auroScholarDataModel.setActivity(inputModel.getActivity());
        auroScholarDataModel.setFragmentContainerUiId(inputModel.getFragmentContainerUiId());
        auroScholarDataModel.setReferralLink(inputModel.getReferralLink());
        if (TextUtil.isEmpty(inputModel.getPartnerSource())) {
            auroScholarDataModel.setPartnerSource("");
        } else {
            auroScholarDataModel.setPartnerSource(inputModel.getPartnerSource());
        }
        AuroApp.setAuroModel(auroScholarDataModel);
    }

    private void funnelStudentLogOut() {
        AnalyticsRegistry.INSTANCE.getModelInstance().trackStudentLogOut();

    }

    public void openKYCViewFragment(DashboardResModel dashboardResModel, int status) {
        Bundle bundle = new Bundle();
        KYCViewFragment kycViewFragment = new KYCViewFragment();
        bundle.putParcelable(AppConstant.DASHBOARD_RES_MODEL, dashboardResModel);
        if (status != 0) {
            bundle.putString(AppConstant.COMING_FROM, AppConstant.SENDING_DATA.DYNAMIC_LINK);
        }
        kycViewFragment.setArguments(bundle);
        openFragment(kycViewFragment);
    }
    public void openLeaderBoardFragment(Fragment fragment) {
        ((AppCompatActivity) (this)).getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.home_container, fragment, DashBoardMainActivity.class
                        .getSimpleName())
                .addToBackStack(null)
                .commitAllowingStateLoss();


    }
    public void openCertificate() {
        Bundle bundle = new Bundle();
        CertificateFragment certificateFragment = new CertificateFragment();
        bundle.putParcelable(AppConstant.DASHBOARD_RES_MODEL, dashboardResModel);
        bundle.putString(AppConstant.COMING_FROM, AppConstant.SENDING_DATA.DYNAMIC_LINK);
        certificateFragment.setArguments(bundle);
        openFragment(certificateFragment);
    }
    public void openSendMoneyFragment() {
        Bundle bundle = new Bundle();
        SendMoneyFragment sendMoneyFragment = new SendMoneyFragment();
        bundle.putParcelable(AppConstant.DASHBOARD_RES_MODEL, dashboardResModel);
        bundle.putString(AppConstant.COMING_FROM, AppConstant.SENDING_DATA.DYNAMIC_LINK);
        sendMoneyFragment.setArguments(bundle);
        openFragment(sendMoneyFragment);
    }



    @Override
    protected int getLayout() {
        return R.layout.activity_dashboard;
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    private void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility(); // get current flag
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // add LIGHT_STATUS_BAR to flag
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue_color)); // optional
        }
    }

    @Override
    public void onItemClick(int position) {


    }

    private void setText(String text) {
        popBackStack();
        binding.naviagtionContent.errorMesssage.setVisibility(View.VISIBLE);
        binding.naviagtionContent.errorMesssage.setText(text);

    }

    public void openFragment(Fragment fragment) {
        FragmentUtil.replaceFragment(mContext, fragment, R.id.home_container, false, AppConstant.NEITHER_LEFT_NOR_RIGHT);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onBackPressed() {
        backStack();

    }

    private synchronized void backStack() {

        switch (LISTING_ACTIVE_FRAGMENT) {
            case TEACHER_DASHBOARD_FRAGMENT:
            case TEACHER_MY_CLASSROOM_FRAGMENT:
            case TEACHER_KYC_FRAGMENT:
            case TEACHER_PROFILE_FRAGMENT:
                dismissApplication();
                break;
            case TEACHER_KYC_UPLOAD_FRAGMENT:
                openFragment(new TeacherKycInfoFragment());
            default:
                popBackStack();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        AppLogger.e("chhonker", "Activity requestCode=" + requestCode);
    }

    private void popBackStack() {
        backPress = 0;
        getSupportFragmentManager().popBackStack();
    }

    private void dismissApplication() {
        if (backPress == 0) {
            backPress++;
            ViewUtil.showSnackBar(binding.naviagtionContent.homeContainer, details.getPressAgainForExit());
        } else {
            finish();
            //  finishAffinity();
        }
    }

    public void setHomeFragmentTab() {
        binding.naviagtionContent.bottomNavigation.setOnNavigationItemSelectedListener(this);
        openFragment(new InformationDashboardFragment());
        selectNavigationMenu(0);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.action_profile) {
            openFragment(new TeacherUserProfileFragment());//TeacherProfileFragment
            selectNavigationMenu(2);
        } else if (itemId == R.id.action_dashboard) {
            openFragment(new InformationDashboardFragment());
            selectNavigationMenu(0);
        } else if (itemId == R.id.action_kyc) {
            openFragment(new TeacherKycInfoFragment());
            selectNavigationMenu(3);
        } else if (itemId == R.id.action_info) {
            openFragment(new MyClassRoomGroupFragment());
            selectNavigationMenu(1);
        }

        return false;
    }

    public void selectNavigationMenu(int pos) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            binding.naviagtionContent.bottomNavigation.getMenu().getItem(pos).setChecked(true);
        }

    }

    public void printHashKey(Activity context) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
                    hashKey = new String(Base64.encode(md.digest(), 0));
                }
                Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "printHashKey()", e);
        } catch (Exception e) {
            Log.e(TAG, "printHashKey()", e);
        }
    }

    private void observeServiceResponse() {

        viewModel.serviceLiveData().observeForever(responseApi -> {
            switch (responseApi.status) {

                case SUCCESS:
                    if (responseApi.apiTypeStatus == Status.DYNAMIC_LINK_API) {
                        AppLogger.e("Error", responseApi.data.toString());
                        DynamiclinkResModel dynamiclinkResModel = (DynamiclinkResModel) responseApi.data;
                        AuroApp.getAuroScholarModel().setReferralLink(dynamiclinkResModel.getLink());
                        PrefModel prefModel = AuroAppPref.INSTANCE.getModelInstance();
                        if (prefModel != null) {
                            prefModel.setDynamiclinkResModel(dynamiclinkResModel);
                            AuroAppPref.INSTANCE.setPref(prefModel);
                        }
                        sendRefferCallback(dynamiclinkResModel, 1);
                    } else if (responseApi.apiTypeStatus == Status.GET_PROFILE_TEACHER_API) {
                        MyProfileResModel model = (MyProfileResModel) responseApi.data;
                        PrefModel prefModel = AuroAppPref.INSTANCE.getModelInstance();
                        prefModel.setTeacherProfileResModel(model);
                        AuroAppPref.INSTANCE.setPref(prefModel);
                    }
                    break;

                case FAIL:
                case NO_INTERNET:
                    AppLogger.e("Error", (String) responseApi.data);
                    sendRefferCallback(null, 0);
                    break;


                default:
                    AppLogger.e("Error", (String) responseApi.data);
                    sendRefferCallback(null, 0);
                    break;
            }

        });
    }

    public void setCommonCallBackListner(CommonCallBackListner listner) {
        commonCallBackListner = listner;
    }

    public void sendRefferCallback(DynamiclinkResModel dynamiclinkResModel, int status) {
        if (commonCallBackListner != null) {
            if (status == 1) {
                commonCallBackListner.commonEventListner(AppUtil.getCommonClickModel(status, Status.REFFER_API_SUCCESS, dynamiclinkResModel));
            } else {
                commonCallBackListner.commonEventListner(AppUtil.getCommonClickModel(status, Status.REFFER_API_ERROR, dynamiclinkResModel));
            }
        }
    }

    private void funnelTeacherDashBoard() {
        AnalyticsRegistry.INSTANCE.getModelInstance().trackTeacherDashBoard();
    }

    private void generatePossibleString(String str, String ans) {
        // If string is empty
        if (str.length() == 0) {
            System.out.print(ans + " ");
            return;
        }

        for (int i = 0; i < str.length(); i++) {

            // ith character of str
            char ch = str.charAt(i);

            // Rest of the string after excluding
            // the ith character
            String ros = str.substring(0, i) +
                    str.substring(i + 1);

            // Recurvise call
            generatePossibleString(ros, ans + ch);
        }
    }

    void generate(String input) {
        int n = input.length();
        int max = 1 << n;
        input = input.toLowerCase();
        for (int i = 0; i < max; i++) {
            char combination[] = input.toCharArray();
            for (int j = 0; j < n; j++) {
                if (((i >> j) & 1) == 1)
                    combination[j] = (char) (combination[j] - 32);
            }
            System.out.print(new String(combination));
            System.out.print(" ");
        }

    }

//    void callProfileApi() {
//        viewModel.checkInternet("", Status.GET_PROFILE_TEACHER_API);
//    }

    public void openChangeLanguageDialog() {
        LanguageChangeDialog languageChangeDialog = new LanguageChangeDialog(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(languageChangeDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        languageChangeDialog.getWindow().setAttributes(lp);
        Objects.requireNonNull(languageChangeDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        languageChangeDialog.setCancelable(true);
        languageChangeDialog.show();

    }


    @Override
    public void commonEventListner(CommonDataModel commonDataModel) {
        AppLogger.e("chhonker-","-commonEventListner");
        switch (commonDataModel.getClickType()) {
            case GET_TEACHER_DASHBOARD_API:
                MyClassRoomTeacherResModel data = (MyClassRoomTeacherResModel) commonDataModel.getObject();
                hideBottomNavigation();

                break;
        }
    }
    public void hideBottomNavigation()
    {
        binding.naviagtionContent.bottomNavigation.setVisibility(View.GONE);
    }
}
