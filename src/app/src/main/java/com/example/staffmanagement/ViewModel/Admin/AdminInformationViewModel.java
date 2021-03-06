package com.example.staffmanagement.ViewModel.Admin;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.staffmanagement.Model.Entity.User;
import com.example.staffmanagement.Model.Repository.RoleRepository;
import com.example.staffmanagement.Model.Repository.UserRepository;
import com.example.staffmanagement.View.Ultils.Constant;
import com.example.staffmanagement.View.Ultils.GeneralFunc;
import com.example.staffmanagement.Model.FirebaseDb.Base.CallBackFunc;

public class AdminInformationViewModel extends ViewModel {
    private UserRepository mRepo;
    private RoleRepository mRepoRole;
    private MutableLiveData<User> mUserLD;
    private User mUser;

    public AdminInformationViewModel() {
        this.mRepo = new UserRepository();
        this.mRepoRole = new RoleRepository();
        mUserLD = new MutableLiveData<>();
    }

    public void setUpUser(User user) {
        mUser = user;
        mUserLD.postValue(mUser);
    }

    public MutableLiveData<User> getUserLD() {
        return mUserLD;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User mUser) {
        this.mUser = mUser;
    }

    public void resetPassword(int idUser) {
        mRepo.resetPassword(idUser, new CallBackFunc<User>() {
            @Override
            public void onSuccess(User data) {
                mUser.setPassword(GeneralFunc.getMD5(Constant.DEFAULT_PASSWORD));
                mUserLD.postValue(mUser);
            }

            @Override
            public void onError(String message) {

            }
        });

    }


    public void update() {
        mRepo.updateUser(mUser, new CallBackFunc<User>() {
            @Override
            public void onSuccess(User data) {
                mUserLD.postValue(mUser);
            }

            @Override
            public void onError(String message) {

            }
        });

    }

    public void changeAvatar(Bitmap bitmap) {
        mRepo.changeAvatarUser(mUser, bitmap, new CallBackFunc<User>() {
            @Override
            public void onSuccess(User data) {
                mUser.setAvatar(data.getAvatar());
                mUserLD.postValue(mUser);
            }

            @Override
            public void onError(String message) {

            }
        });
    }

}
