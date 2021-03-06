package com.example.staffmanagement.Model.Repository;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.staffmanagement.Model.Entity.Request;
import com.example.staffmanagement.Model.Entity.Role;
import com.example.staffmanagement.Model.Entity.User;
import com.example.staffmanagement.Model.Entity.UserState;
import com.example.staffmanagement.Model.FirebaseDb.Base.ApiResponse;
import com.example.staffmanagement.Model.FirebaseDb.Base.Resource;
import com.example.staffmanagement.Model.FirebaseDb.Request.RequestService;
import com.example.staffmanagement.Model.FirebaseDb.User.UserService;
import com.example.staffmanagement.View.Ultils.Constant;
import com.example.staffmanagement.View.Ultils.GeneralFunc;
import com.example.staffmanagement.Model.FirebaseDb.Base.CallBackFunc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserRepository {
    private UserService service;
    private MutableLiveData<List<User>> mLiveDataUser;
    private MutableLiveData<List<Integer>> mLiveDataQuantities;
    private MutableLiveData<List<Role>> mLiveDataRole;
    private MutableLiveData<List<UserState>> mLiveDataUserState;
    private MutableLiveData<List<User>> mLiveDataUserCheck;
    private MutableLiveData<List<String>> listFullName;
    private MutableLiveData<User> mUserLD;

    public UserRepository() {
        service = new UserService();
        mLiveDataUser = new MutableLiveData<>();
        mLiveDataQuantities = new MutableLiveData<>();
        mLiveDataRole = new MutableLiveData<>();
        mLiveDataUserState = new MutableLiveData<>();
        mLiveDataUserCheck = new MutableLiveData<>();
        listFullName = new MutableLiveData<>();
        mUserLD = new MutableLiveData<>();
    }


    public MutableLiveData<List<User>> getLiveData() {
        return mLiveDataUser;
    }

    public MutableLiveData<List<Integer>> getLiveDataQuantities() {
        return mLiveDataQuantities;
    }

    public MutableLiveData<List<Role>> getLiveDataRole() {
        return mLiveDataRole;
    }

    public MutableLiveData<List<UserState>> getLiveDataUserState() {
        return mLiveDataUserState;
    }

    public MutableLiveData<List<User>> getLiveDataUserCheck() {
        return mLiveDataUserCheck;
    }

    public MutableLiveData<User> getUserLD() {
        return mUserLD;
    }

    public void getLimitListUser(int offset, int numRow, Map<String, Object> criteria) {
        service.getAll(new ApiResponse<List<User>>() {
            @Override
            public void onSuccess(Resource<List<User>> success) {
                String searchString = (String) criteria.get(Constant.SEARCH_NAME_IN_ADMIN);
                List<User> userList = success.getData();
                userList = userList.stream()
                        .filter(user -> user.getRole().getId() == 2 &&
                                user.getFullName().toLowerCase().contains(searchString.toLowerCase()))
                        .skip(offset)
                        .limit(numRow)
                        .collect(Collectors.toList());

                List<User> finalUserList = userList;
                new Thread(() -> new RequestService().getAll(new ApiResponse<List<Request>>() {
                    @Override
                    public void onSuccess(Resource<List<Request>> successReq) {
                        List<Integer> quantities = new ArrayList<>();
                        for (User user : finalUserList) {
                            long count = successReq.getData()
                                    .stream()
                                    .filter(request -> request.getIdUser() == user.getId() && request.getStateRequest().getId() == 1)
                                    .count();
                            quantities.add((int) count);
                        }

                        mLiveDataQuantities.postValue(quantities);
                        mLiveDataUser.postValue(finalUserList);
                    }

                    @Override
                    public void onLoading(Resource<List<Request>> loading) {

                    }

                    @Override
                    public void onError(Resource<List<Request>> error) {

                    }
                })).start();

            }

            @Override
            public void onLoading(Resource<List<User>> loading) {

            }

            @Override
            public void onError(Resource<List<User>> error) {

            }
        });
    }

    public void getAllRoleAndUserState() {
        new RoleRepository().getAll(new CallBackFunc<List<Role>>() {
            @Override
            public void onSuccess(List<Role> dataRoles) {
                new UserStateRepository().getAll(new CallBackFunc<List<UserState>>() {
                    @Override
                    public void onSuccess(List<UserState> data) {
                        mLiveDataUserState.postValue(data);
                        mLiveDataRole.postValue(dataRoles);
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    public void populateData() {
        service.populateData();
    }

    public void updateUser(User user,CallBackFunc<User> callBackFunc) {
        service.update(user, new ApiResponse<User>() {
            @Override
            public void onSuccess(Resource<User> success) {
                callBackFunc.onSuccess(success.getData());
            }

            @Override
            public void onLoading(Resource<User> loading) {

            }

            @Override
            public void onError(Resource<User> error) {

            }
        });
    }

    public void changeAvatarUser(User user, Bitmap bitmap, CallBackFunc<User> callBackFunc) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                service.changeAvatar(user, bitmap, new ApiResponse<User>() {
                    @Override
                    public void onSuccess(Resource<User> success) {
                        callBackFunc.onSuccess(user);
                    }

                    @Override
                    public void onLoading(Resource<User> loading) {

                    }

                    @Override
                    public void onError(Resource<User> error) {

                    }
                });
            }
        }).start();
    }

    public void getUserForLogin(final int idUser) {
        service.getById(idUser, new ApiResponse<User>() {
            @Override
            public void onSuccess(Resource<User> success) {
                mUserLD.postValue(success.getData());
            }

            @Override
            public void onLoading(Resource<User> loading) {

            }

            @Override
            public void onError(Resource<User> error) {
                mUserLD.postValue(null);
            }
        });
    }

    public void getByLoginInformation(String userName, String password) {

        service.getAll(new ApiResponse<List<User>>() {
            @Override
            public void onSuccess(Resource<List<User>> success) {
                new Thread(() -> {
                    int f = 0;
                    String passMd5 = GeneralFunc.getMD5(password);
                    for (User u : success.getData()) {
                        if (u.getUserName().equals(userName) && u.getPassword().equals(passMd5)) {
                            mUserLD.postValue(u);
                            f = 1;
                            return;
                        }
                    }
                    if (f == 0)
                        mUserLD.postValue(null);
                }).start();

            }

            @Override
            public void onLoading(Resource<List<User>> loading) {

            }

            @Override
            public void onError(Resource<List<User>> error) {
                mUserLD.postValue(null);
            }
        });
    }

    public void insert(User user, final int idUser, final int offset, final Map<String, Object> mCriteria) {
        service.put(user, new ApiResponse<User>() {
            @Override
            public void onSuccess(Resource<User> success) {
             //   getLimitListUser(idUser, offset, 1, mCriteria);
            }

            @Override
            public void onLoading(Resource<User> loading) {

            }

            @Override
            public void onError(Resource<User> error) {

            }
        });
    }

    public void getCountStaff(CallBackFunc<Integer> callBackFunc) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                service.getAll(new ApiResponse<List<User>>() {
                    @Override
                    public void onSuccess(Resource<List<User>> success) {
                        long count = success.getData()
                                .stream()
                                .filter(user -> user.getRole().getId() == 2)
                                .count();
                        callBackFunc.onSuccess((int) count);
                    }

                    @Override
                    public void onLoading(Resource<List<User>> loading) {

                    }

                    @Override
                    public void onError(Resource<List<User>> error) {

                    }
                });
            }
        }).start();
    }

    public void getAllStaff(CallBackFunc<List<User>> callBackFunc) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                service.getAll(new ApiResponse<List<User>>() {
                    @Override
                    public void onSuccess(Resource<List<User>> success) {
                        List<User> list = success.getData()
                                .stream()
                                .filter(user -> user.getRole().getId() == 2)
                                .collect(Collectors.toList());
                        callBackFunc.onSuccess(list);
                    }

                    @Override
                    public void onLoading(Resource<List<User>> loading) {

                    }

                    @Override
                    public void onError(Resource<List<User>> error) {

                    }
                });
            }
        }).start();
    }

    public void changeIdUserState(int idUser, int idUserState,CallBackFunc<User> callBackFunc) {
        service.getById(idUser, new ApiResponse<User>() {
            @Override
            public void onSuccess(Resource<User> success) {
                User user = success.getData();
                new UserStateRepository().getAll(new CallBackFunc<List<UserState>>() {
                    @Override
                    public void onSuccess(List<UserState> data) {
                        for(UserState st : data){
                            if(st.getId()==idUserState){
                                user.setUserState(st);
                                service.update(user, new ApiResponse<User>() {
                                    @Override
                                    public void onSuccess(Resource<User> success) {
                                        callBackFunc.onSuccess(user);
                                    }

                                    @Override
                                    public void onLoading(Resource<User> loading) {

                                    }

                                    @Override
                                    public void onError(Resource<User> error) {
                                    }
                                });
                                break;
                            }
                        }
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }

            @Override
            public void onLoading(Resource<User> loading) {

            }

            @Override
            public void onError(Resource<User> error) {

            }
        });
    }

    public void resetPassword(int idUser,CallBackFunc<User> callBackFunc) {
        service.getById(idUser, new ApiResponse<User>() {
            @Override
            public void onSuccess(Resource<User> success) {
                User user = success.getData();
                user.setPassword(Constant.DEFAULT_PASSWORD);
                service.update(user, new ApiResponse<User>() {
                    @Override
                    public void onSuccess(Resource<User> success) {
                        callBackFunc.onSuccess(user);
                    }

                    @Override
                    public void onLoading(Resource<User> loading) {

                    }

                    @Override
                    public void onError(Resource<User> error) {

                    }
                });
            }

            @Override
            public void onLoading(Resource<User> loading) {

            }

            @Override
            public void onError(Resource<User> error) {

            }
        });
    }

    public void checkUserNameIsExisted(String userName, CallBackFunc<Boolean> callBackFunc) {
        new Thread(() -> {
            service.getAll(new ApiResponse<List<User>>() {
                @Override
                public void onSuccess(Resource<List<User>> success) {
                    List<User> list = success.getData().stream()
                            .filter(user1 -> user1.getUserName().equals(userName))
                            .collect(Collectors.toList());
                    Log.i("ADDUSER", " " + list.size());
                    if (list.size() > 0)
                        callBackFunc.onSuccess(true);
                    else
                        callBackFunc.onSuccess(false);
                }

                @Override
                public void onLoading(Resource<List<User>> loading) {

                }

                @Override
                public void onError(Resource<List<User>> error) {

                }
            });
        }).start();
    }

    public void getAll() {
        service.getAll(new ApiResponse<List<User>>() {
            @Override
            public void onSuccess(Resource<List<User>> success) {
                for (int i = 0; i < success.getData().size(); i++) {
                    Log.i("FETCH", success.getData().get(i).getFullName());
                }
            }

            @Override
            public void onLoading(Resource<List<User>> loading) {

            }

            @Override
            public void onError(Resource<List<User>> error) {
                Log.i("FETCH", error.getMessage());
            }
        });
    }

    public void countStaff(CallBackFunc<Integer> callBackFunc){
        service.getAll(new ApiResponse<List<User>>() {
            @Override
            public void onSuccess(Resource<List<User>> success) {
                int count = (int) success.getData()
                        .stream()
                        .filter(user -> user.getRole().getId() == 2)
                        .count();
                callBackFunc.onSuccess(count);
            }

            @Override
            public void onLoading(Resource<List<User>> loading) {

            }

            @Override
            public void onError(Resource error) {

            }
        });
    }

    public void countAdmin(CallBackFunc<Integer> callBackFunc){
        service.getAll(new ApiResponse<List<User>>() {
            @Override
            public void onSuccess(Resource<List<User>> success) {
                int count = (int) success.getData()
                        .stream()
                        .filter(user -> user.getRole().getId() == 1)
                        .count();
                callBackFunc.onSuccess(count);
            }

            @Override
            public void onLoading(Resource<List<User>> loading) {

            }

            @Override
            public void onError(Resource error) {

            }
        });
    }


}