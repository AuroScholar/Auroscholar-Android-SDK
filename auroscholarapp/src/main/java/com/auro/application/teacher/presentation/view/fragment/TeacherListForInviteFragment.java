package com.auro.application.teacher.presentation.view.fragment;

import static android.content.Context.MODE_PRIVATE;
import static com.auro.application.core.common.Status.SEND_INVITE_CLICK;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.auro.application.R;
import com.auro.application.core.application.base_component.BaseFragment;
import com.auro.application.core.application.di.component.ViewModelFactory;
import com.auro.application.core.common.AppConstant;
import com.auro.application.core.common.CommonCallBackListner;
import com.auro.application.core.common.CommonDataModel;
import com.auro.application.core.common.FragmentUtil;
import com.auro.application.core.common.Status;
import com.auro.application.core.database.AuroAppPref;
import com.auro.application.core.database.PrefModel;
import com.auro.application.databinding.FragmentInviteteacherbuddyBinding;
import com.auro.application.home.data.model.Details;
import com.auro.application.home.data.model.response.DynamiclinkResModel;
import com.auro.application.home.presentation.view.activity.HomeActivity;
import com.auro.application.teacher.data.model.response.AcceptTeacherBuddyDataResModel;
import com.auro.application.teacher.data.model.response.AcceptTeacherBuddyResModel;
import com.auro.application.teacher.data.model.response.TeacherInviteTeacherDataResModel;
import com.auro.application.teacher.data.model.response.TeacherInviteTeacherResModel;
import com.auro.application.teacher.data.model.response.TeacherStudentPassportResModel;
import com.auro.application.teacher.presentation.view.adapter.StudentPassportListAdapter;
import com.auro.application.teacher.presentation.view.adapter.TeacherInviteTeacherAdapter;
import com.auro.application.teacher.presentation.viewmodel.StudentPassportViewModel;
import com.auro.application.util.AppLogger;
import com.auro.application.util.RemoteApi;
import com.auro.application.util.strings.AppStringTeacherDynamic;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherListForInviteFragment extends BaseFragment implements CommonCallBackListner {
    @Inject
    @Named("MyStudentPassportFragment")
    ViewModelFactory viewModelFactory;
    String TAG = "MyStudentPassportFragment";
    FragmentInviteteacherbuddyBinding binding;
    StudentPassportViewModel viewModel;
    StudentPassportListAdapter studentListAdapter;
    TeacherStudentPassportResModel resModel;
    boolean isStateRestore;
    Details details;
    String gradeid;

    List<AcceptTeacherBuddyDataResModel> listchilds = new ArrayList<>();
    List<AcceptTeacherBuddyDataResModel> list = new ArrayList<>();
    List<TeacherInviteTeacherDataResModel> listchilds1 = new ArrayList<>();
    List<TeacherInviteTeacherDataResModel> list1 = new ArrayList<>();
    public TeacherListForInviteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false);
       // ((AuroApp) getActivity().getApplication()).getAppComponent().doInjection(this);
       // viewModel = ViewModelProviders.of(this, viewModelFactory).get(StudentPassportViewModel.class);
        binding.setLifecycleOwner(this);
        setRetainInstance(true);
        details = AuroAppPref.INSTANCE.getModelInstance().getLanguageMasterDynamic().getDetails();
       // getTeacherList();
       // getTeacherBuddyList();
        return binding.getRoot();

    }

    @Override
    protected void init() {
        AppStringTeacherDynamic.setInviteTeacherBuddyFragmentStrings(binding);

    }

    @Override
    protected void setToolbar() {

    }

    @Override
    protected void setListener() {
        AppLogger.e("SummaryData", "Stem 0");
        HomeActivity.setListingActiveFragment(HomeActivity.TEACHER_MY_CLASSROOM_FRAGMENT);
        if (viewModel != null && viewModel.serviceLiveData().hasObservers()) {
            viewModel.serviceLiveData().removeObservers(this);
        } else {
            binding.btnaddbuddy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {



                    SharedPreferences prefs = getActivity().getSharedPreferences("My_Pref", MODE_PRIVATE);
                    String multiple_teacherid = prefs.getString("multiple_teacherid", "");
                    if (multiple_teacherid.equals("null")||multiple_teacherid.isEmpty()||multiple_teacherid.equals("")){
                        Toast.makeText(getActivity(), "Please select teacher", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        sentInvite(multiple_teacherid);
                    }
                }
            });
            binding.txtrefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.recyclerviewbuddylist.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                    binding.recyclerviewbuddylist.setHasFixedSize(true);
                    getTeacherList();
                }
            });
            binding.btnaddbuddyinvite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            binding.btnshare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callRefferApi();
//                    Intent sendIntent = new Intent();
//                    sendIntent.setAction(Intent.ACTION_SEND);
//                    sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
//                    sendIntent.setType("text/plain");
//                    Intent shareIntent = Intent.createChooser(sendIntent, null);
//                    startActivity(shareIntent);
                }
            });
            binding.imgbuddy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFragment(new TeacherBuddyFragment());
                }
            });
            binding.recyclerviewbuddylist.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            binding.recyclerviewbuddylist.setHasFixedSize(true);
            getTeacherList();

           // observeServiceResponse();
        }


    }

    private void sentInvite(String teacherid)
    {
        PrefModel prefModel = AuroAppPref.INSTANCE.getModelInstance();
        String languageid = prefModel.getUserLanguageId();
        HashMap<String,String> map_data = new HashMap<>();
        String userid = AuroAppPref.INSTANCE.getModelInstance().getStudentData().getUserId();
        map_data.put("user_id",userid);
        map_data.put("teacher_ids", teacherid);  //"576232"
        RemoteApi.Companion.invoke().getSendInvite(map_data)
                .enqueue(new Callback<TeacherInviteTeacherResModel>()
                {
                    @Override
                    public void onResponse(Call<TeacherInviteTeacherResModel> call, Response<TeacherInviteTeacherResModel> response)
                    {
                        try {
                            if (response.isSuccessful()) {

                                Toast.makeText(getActivity(), response.body().getMessage().toString(), Toast.LENGTH_SHORT).show();

                                SharedPreferences.Editor editor = getActivity().getSharedPreferences("My_Pref", MODE_PRIVATE).edit();
                                editor.putString("multiple_teacherid", "");
                                editor.apply();
                                openFragment(new TeacherBuddyFragment());
                            }
                            else {

                                Toast.makeText(getActivity(), response.message(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<TeacherInviteTeacherResModel> call, Throwable t)
                    {
                        Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });




    }
    public void callRefferApi() {
        AppLogger.e("callRefferApi","step 1");
        try {
            DynamiclinkResModel dynamiclinkResModel = new DynamiclinkResModel();
            dynamiclinkResModel.setReffeUserId(AuroAppPref.INSTANCE.getModelInstance().getStudentData().getUserId());
            dynamiclinkResModel.setSource(AppConstant.AURO_ID);
            dynamiclinkResModel.setNavigationTo(AppConstant.NavigateToScreen.STUDENT_DASHBOARD);
            dynamiclinkResModel.setReffer_type("" + AppConstant.UserType.TEACHER);
            viewModel.checkInternet(dynamiclinkResModel, Status.DYNAMIC_LINK_API);
        }catch (Exception e)
        {

        }
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_inviteteacherbuddy;
    }



    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
        setToolbar();
        setListener();
    }



    private void getTeacherList()
    {
        ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(details.getProcessing());
        progress.setMessage(details.getProcessing());
        progress.setCancelable(true); // disable dismiss by tapping outside of the dialog
        progress.show();
        PrefModel prefModel = AuroAppPref.INSTANCE.getModelInstance();
        String languageid = prefModel.getUserLanguageId();
        HashMap<String,String> map_data = new HashMap<>();
        String userid = AuroAppPref.INSTANCE.getModelInstance().getStudentData().getUserId();
        map_data.put("user_id",userid);    //"576232"
        RemoteApi.Companion.invoke().getInviteTeacherList(map_data)
                .enqueue(new Callback<TeacherInviteTeacherResModel>()
                {
                    @Override
                    public void onResponse(Call<TeacherInviteTeacherResModel> call, Response<TeacherInviteTeacherResModel> response)
                    {
                        try {
                            if (response.isSuccessful()) {
                                progress.dismiss();
                                list1.clear();
                                listchilds1.clear();
                                if (response.body().getStatus().equals("success")){
                                    if (!(response.body().getTeacherinvitedata() == null || response.body().getTeacherinvitedata().isEmpty() || response.body().getTeacherinvitedata().equals("") || response.body().getTeacherinvitedata().equals("null"))) {

                                        binding.mainParentLayoutInvite.setVisibility(View.GONE);
                                        binding.mainParentLayout.setVisibility(View.VISIBLE);
                                        listchilds1 = response.body().getTeacherinvitedata();
                                        for (int i = 0; i < listchilds1.size(); i++) {
                                            list1.add(listchilds1.get(i));
                                        }

                                        TeacherInviteTeacherAdapter studentListAdapter = new TeacherInviteTeacherAdapter(getActivity(), list1);
                                        binding.recyclerviewbuddylist.setAdapter(studentListAdapter);
                                    }
                                      else{
                                        binding.mainParentLayoutInvite.setVisibility(View.GONE);
                                        binding.mainParentLayout.setVisibility(View.GONE);
                                        Toast.makeText(getActivity(), "No Teacher Found", Toast.LENGTH_SHORT).show();
                                    }
                                }



                            }
                            else {
                                progress.dismiss();
                               Toast.makeText(getActivity(), response.message(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            progress.dismiss();
                            Toast.makeText(getActivity(), "Internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<TeacherInviteTeacherResModel> call, Throwable t)
                    {
                        progress.dismiss();
                        Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });




    }

    private void getTeacherBuddyList()
    {
        PrefModel prefModel = AuroAppPref.INSTANCE.getModelInstance();
        String languageid = prefModel.getUserLanguageId();
        HashMap<String,String> map_data = new HashMap<>();
        String userid = AuroAppPref.INSTANCE.getModelInstance().getStudentData().getUserId();
        map_data.put("user_id",userid);    //"576232"
        RemoteApi.Companion.invoke().getTeacherBuddyList(map_data)
                .enqueue(new Callback<AcceptTeacherBuddyResModel>()
                {
                    @Override
                    public void onResponse(Call<AcceptTeacherBuddyResModel> call, Response<AcceptTeacherBuddyResModel> response)
                    {
                        try {
                            if (response.isSuccessful()) {
                                list.clear();
                                listchilds.clear();
                                if (response.body().getStatus().equals("success")){
                                    if (!(response.body().getTeacherinvitedata() == null || response.body().getTeacherinvitedata().isEmpty() || response.body().getTeacherinvitedata().equals("") || response.body().getTeacherinvitedata().equals("null"))) {
                                        listchilds = response.body().getTeacherinvitedata();
                                        openFragment(new TeacherBuddyFragment());
                                        //for (int i = 0; i < listchilds.size(); i++) {
                                           // if (listchilds.get(i).getStatus().equals("1") || listchilds.get(i).getStatus().equals(1)) {

                                               // list.add(listchilds.get(i));
                                           // }
                                      //  }


//                                        AcceptBuddyAdapter studentListAdapter = new AcceptBuddyAdapter(getActivity(), list);
//                                        binding.recyclerviewbuddylist.setAdapter(studentListAdapter);
                                    }
                                    else{
                                        binding.mainParentLayoutInvite.setVisibility(View.VISIBLE);
                                        binding.mainParentLayout.setVisibility(View.GONE);
                                    }

                                }
                                else{
                                    binding.mainParentLayoutInvite.setVisibility(View.VISIBLE);
                                    binding.mainParentLayout.setVisibility(View.GONE);
                                }




                            }
                            else {

                                Toast.makeText(getActivity(), response.message(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AcceptTeacherBuddyResModel> call, Throwable t)
                    {
                        Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });




    }


    @Override
    public void commonEventListner(CommonDataModel commonDataModel) {

        if (commonDataModel.getClickType()==SEND_INVITE_CLICK)
        {
            TeacherInviteTeacherDataResModel stateDataModel = (TeacherInviteTeacherDataResModel) commonDataModel.getObject();
            String userid = stateDataModel.getUser_id();
        }

    }


    public void openFragment(Fragment fragment) {
        FragmentUtil.replaceFragment(getActivity(), fragment, R.id.home_container, false, AppConstant.NEITHER_LEFT_NOR_RIGHT);
    }

}