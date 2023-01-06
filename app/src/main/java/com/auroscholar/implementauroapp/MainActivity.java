package com.auroscholar.implementauroapp;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.fragment.app.Fragment;

import com.auro.application.core.application.AuroApp;
import com.auro.application.core.common.AppConstant;
import com.auro.application.core.common.NotificationDataModel;
import com.auro.application.core.common.SdkCallBack;
import com.auro.application.core.common.Status;
import com.auro.application.core.database.AuroAppPref;
import com.auro.application.core.database.PrefModel;
import com.auro.application.core.util.AuroScholar;
import com.auro.application.home.data.model.AuroScholarDataModel;
import com.auro.application.home.data.model.AuroScholarInputModel;
import com.auro.application.home.data.model.response.DynamiclinkResModel;
import com.auro.application.home.presentation.view.activity.DashBoardMainActivity;
import com.auro.application.home.presentation.view.activity.HomeActivity;
import com.auro.application.home.presentation.view.activity.LoginActivity;
import com.auro.application.home.presentation.view.activity.SplashScreenAnimationActivity;
import com.auro.application.home.presentation.view.fragment.FriendsLeaderBoardListFragment;
import com.auro.application.home.presentation.view.fragment.MainQuizHomeFragment;
import com.auro.application.util.AppLogger;
import com.auro.application.util.ConversionUtil;
import com.auro.application.util.TextUtil;


public class MainActivity extends Activity {
TextView bt_sdk;
EditText language;
    private PrefModel prefModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PrefModel prefModel = AuroAppPref.INSTANCE.getModelInstance();

        bt_sdk=findViewById(R.id.bt_sdk);
        language=findViewById(R.id.language);
        bt_sdk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobileNumber ="9289180019";
                String student_class = "8";
                if (TextUtil.isEmpty(mobileNumber) && mobileNumber.length() != 10) {
                    Toast.makeText(MainActivity.this, "Please Enter valid mobile number", Toast.LENGTH_SHORT).show();
                } else if (TextUtil.isEmpty(student_class)) {
                    Toast.makeText(MainActivity.this, "Please Enter class", Toast.LENGTH_SHORT).show();
                } else {
                    prefModel.setUserMobile(mobileNumber);
                    prefModel.setUserLoginId(mobileNumber);
                    prefModel.setUserType(0);

                   // ((LoginActivity) getApplicationContext()).checkUserType();

                    startAuroSDK();
                }
            }
        });
//        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//        startActivity(intent);

    }



    public Fragment startAuroSDK() {
        AuroScholarDataModel auroScholarDataModel = new AuroScholarDataModel();
        auroScholarDataModel.setMobileNumber("9289180019");
        auroScholarDataModel.setStudentClass("2");
        auroScholarDataModel.setRegitrationSource("AuroScholr");
        auroScholarDataModel.setActivity(this);
      //  auroScholarDataModel.setFragmentContainerUiId(R.id.home_cont);
        auroScholarDataModel.setReferralLink("");
        auroScholarDataModel.setDevicetoken("");
            auroScholarDataModel.setPartnerSource("");

        AuroApp.setAuroModel(auroScholarDataModel);
        Intent intent = new Intent(MainActivity.this, DashBoardMainActivity.class);
        startActivity(intent);
       // MainQuizHomeFragment quizHomeFragment = new MainQuizHomeFragment();
      //  return quizHomeFragment;


        return null;
    }
}