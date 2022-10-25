package io.openim.android.demo.ui.user;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityPersonInfoBinding;
import io.openim.android.demo.databinding.ActivityPersonalInfoBinding;
import io.openim.android.demo.ui.main.EditTextActivity;
import io.openim.android.demo.vm.FriendVM;
import io.openim.android.demo.vm.PersonalVM;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.SlideButton;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.UserInfo;

public class PersonDataActivity extends BaseActivity<PersonalVM, ActivityPersonInfoBinding> {

    private ChatVM chatVM;
    private FriendVM friendVM = new FriendVM();
    private WaitDialog waitDialog;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(PersonalVM.class, true);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityPersonInfoBinding.inflate(getLayoutInflater()));
        sink();
        init();
        listener();
        uid = getIntent().getStringExtra(Constant.K_ID);
        if (TextUtils.isEmpty(uid)) {
            chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
            uid = chatVM.otherSideID;
            vm.getUserInfo(uid);
        } else
            vm.getUserInfo(uid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            removeCacheVM();
    }

    private void init() {
        waitDialog = new WaitDialog(this);
        friendVM.waitDialog = waitDialog;
        friendVM.setContext(this);
        friendVM.setIView(this);
        friendVM.getBlacklist();
    }


    private void listener() {
        view.moreData.setOnClickListener(v -> {
            startActivity(new Intent(this, MoreDataActivity.class));
        });
        view.remark.setOnClickListener(view -> {
            if (null == vm.userInfo.getValue()) return;
            resultLauncher.launch(new Intent(this, EditTextActivity.class)
                .putExtra(EditTextActivity.TITLE, getString(io.openim.android.ouicore.R.string.remark))
                .putExtra(EditTextActivity.INIT_TXT, vm.userInfo.getValue().getFriendInfo().getRemark()));
        });
        friendVM.blackListUser.observe(this, userInfos -> {
            boolean isCon = false;
            for (UserInfo userInfo : userInfos) {
                if (userInfo.getUserID()
                    .equals(uid)) {
                    isCon = true;
                    break;
                }
            }
            view.slideButton.setCheckedWithAnimation(isCon);
        });
        view.joinBlackList.setOnClickListener(v -> {
            if (view.slideButton.isChecked())
                friendVM.removeBlacklist(uid);
            else {
                addBlackList();
            }
        });
        view.slideButton.setOnSlideButtonClickListener(isChecked -> {
            if (isChecked)
                addBlackList();
            else
                friendVM.removeBlacklist(uid);
        });
    }

    private void addBlackList() {
        CommonDialog commonDialog = new CommonDialog(this);
        commonDialog.setCanceledOnTouchOutside(false);
        commonDialog.setCancelable(false);
        commonDialog.getMainView().tips.setText("确认对" + vm.userInfo.getValue().getNickname() + "拉黑吗？");
        commonDialog.getMainView().cancel.setOnClickListener(v -> {
            commonDialog.dismiss();
            friendVM.blackListUser.setValue(friendVM.blackListUser.getValue());
        });
        commonDialog.getMainView().confirm.setOnClickListener(v -> {
            commonDialog.dismiss();
            friendVM.addBlacklist(uid);
        });
        commonDialog.show();
    }

    private ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK) return;
        String resultStr = result.getData().getStringExtra(Constant.K_RESULT);

        waitDialog.show();
        OpenIMClient.getInstance().friendshipManager.setFriendRemark(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                waitDialog.dismiss();
                toast(error + code);
            }

            @Override
            public void onSuccess(String data) {
                waitDialog.dismiss();
                vm.userInfo.getValue().setRemark(resultStr);
                Obs.newMessage(Constant.Event.USER_INFO_UPDATA);
            }
        }, uid, resultStr);
    });
}
