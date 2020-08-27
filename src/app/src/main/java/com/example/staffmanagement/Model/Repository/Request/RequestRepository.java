package com.example.staffmanagement.Model.Repository.Request;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.example.staffmanagement.Model.Entity.Request;
import com.example.staffmanagement.Model.Entity.User;
import com.example.staffmanagement.Model.FirebaseDb.Base.ApiResponse;
import com.example.staffmanagement.Model.FirebaseDb.Base.Resource;
import com.example.staffmanagement.Model.FirebaseDb.Request.RequestService;
import com.example.staffmanagement.Model.FirebaseDb.User.UserService;
import com.example.staffmanagement.Model.Repository.AppDatabase;
import com.example.staffmanagement.Model.Ultils.RequestQuery;
import com.example.staffmanagement.View.Data.AdminRequestFilter;
import com.example.staffmanagement.View.Data.StaffRequestFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RequestRepository {
    private RequestService service;
    private MutableLiveData<List<Request>> mLiveData;
    private MutableLiveData<List<String>> fullNameListLD;

    public RequestRepository() {
        service = new RequestService();
        this.mLiveData = new MutableLiveData<>();
        fullNameListLD = new MutableLiveData<>();
    }

    public void getLimitListRequestForStaffLD(int idUser, int offset, int numRow, StaffRequestFilter criteria) {
        service.getAll(new ApiResponse<List<Request>>() {
            @Override
            public void onSuccess(Resource<List<Request>> success) {
                new Thread(() -> {
                    List<Request> list = success.getData();
                    list = list.stream()
                            .filter(request -> request.getIdUser() == idUser &&
                                    request.getTitle().toLowerCase().contains(criteria.getSearchString().toLowerCase()))
                            .collect(Collectors.toList());

                    if (criteria.getStateList().size() > 0) {
                        List<Request> temp = new ArrayList<>();
                        temp.addAll(list);
                        list.clear();
                        for (int i = 0; i < criteria.getStateList().size(); i++) {
                            if (criteria.getStateList().get(i).toString().equals(StaffRequestFilter.STATE.Waiting.toString()))
                                list.addAll(temp.stream().filter(request -> request.getIdState() == 1).collect(Collectors.toList()));
                            else if (criteria.getStateList().get(i).toString().equals(StaffRequestFilter.STATE.Accept.toString()))
                                list.addAll(temp.stream().filter(request -> request.getIdState() == 2).collect(Collectors.toList()));
                            else if (criteria.getStateList().get(i).toString().equals(StaffRequestFilter.STATE.Decline.toString()))
                                list.addAll(temp.stream().filter(request -> request.getIdState() == 3).collect(Collectors.toList()));
                        }
                        temp.clear();
                    }

                    if (criteria.getFromDateTime() != 0 && criteria.getToDateTime() != 0) {
                        list = list.stream()
                                .filter(request -> request.getDateTime() > criteria.getFromDateTime() && request.getDateTime() < criteria.getToDateTime())
                                .collect(Collectors.toList());
                    }

                    if (!criteria.getSortName().equals(StaffRequestFilter.SORT.None)) {

                        switch (criteria.getSortName()) {
                            case DateTime:
                                if (criteria.getSortType() == StaffRequestFilter.SORT_TYPE.ASC)
                                    list.sort((request, t1) -> request.getDateTime() < t1.getDateTime() ? -1 : 1);
                                else
                                    list.sort((request, t1) -> request.getDateTime() > t1.getDateTime() ? -1 : 1);
                                break;
                            case Title:
                                if (criteria.getSortType() == StaffRequestFilter.SORT_TYPE.ASC)
                                    list.sort((request, t1) -> request.getTitle().compareTo(t1.getTitle()) > 0 ? -1 : 1);
                                else
                                    list.sort((request, t1) -> request.getTitle().compareTo(t1.getTitle()) < 0 ? -1 : 1);
                                break;
                        }

                    } else
                        list.sort((request, t1) -> request.getDateTime() > t1.getDateTime() ? -1 : 1);

                    list = list.stream().skip(offset).limit(numRow).collect(Collectors.toList());
                    mLiveData.postValue(list);
                }).start();
            }

            @Override
            public void onLoading(Resource<List<Request>> loading) {

            }

            @Override
            public void onError(Resource<List<Request>> error) {
                mLiveData.postValue(new ArrayList<>());
            }
        });

    }

    public MutableLiveData<List<String>> getFullNameListLD() {
        return fullNameListLD;
    }

    public MutableLiveData<List<Request>> getLiveData() {
        return mLiveData;
    }

    public void getLimitListRequestForUser(int idUser, int offset, int numRow, AdminRequestFilter criteria) {

        service.getAll(new ApiResponse<List<Request>>() {
            @Override
            public void onSuccess(Resource<List<Request>> success) {
                new Thread(() -> {

                    List<Request> list = success.getData();
                    if (idUser != 0)
                        list = list.stream().filter(request -> request.getIdUser() == idUser).collect(Collectors.toList());

                    if (criteria.getStateList().size() > 0) {
                        List<Request> temp = new ArrayList<>();
                        temp.addAll(list);
                        list.clear();
                        for (int i = 0; i < criteria.getStateList().size(); i++) {
                            if (criteria.getStateList().get(i).toString().equals(StaffRequestFilter.STATE.Waiting.toString()))
                                list.addAll(temp.stream().filter(request -> request.getIdState() == 1).collect(Collectors.toList()));
                            else if (criteria.getStateList().get(i).toString().equals(StaffRequestFilter.STATE.Accept.toString()))
                                list.addAll(temp.stream().filter(request -> request.getIdState() == 2).collect(Collectors.toList()));
                            else if (criteria.getStateList().get(i).toString().equals(StaffRequestFilter.STATE.Decline.toString()))
                                list.addAll(temp.stream().filter(request -> request.getIdState() == 3).collect(Collectors.toList()));
                        }
                        temp.clear();
                    }

                    if (criteria.getFromDateTime() != 0 && criteria.getToDateTime() != 0) {
                        list = list.stream()
                                .filter(request -> request.getDateTime() > criteria.getFromDateTime() && request.getDateTime() < criteria.getToDateTime())
                                .collect(Collectors.toList());
                    }

                    List<Request> finalList = list;
                    new UserService().getAll(new ApiResponse<List<User>>() {
                        @Override
                        public void onSuccess(Resource<List<User>> success) {
                            List<String> fullNameList = new ArrayList<>();
                            List<Request> mainRequestList = new ArrayList<>();
                            List<User> userList = success.getData();

                            for (int i = 0; i < finalList.size(); i++) {
                                int finalI = i;
                                User u = success.getData().stream()
                                        .filter(user -> finalList.get(finalI).getIdUser() == user.getId() &&
                                                (user.getFullName().toLowerCase().contains(criteria.getSearchString().toLowerCase()) || user.getFullName().toLowerCase().contains(criteria.getSearchString().toLowerCase())))
                                        .findFirst().orElse(null);
                                if (u != null) {
                                    mainRequestList.add(finalList.get(i));
                                    fullNameList.add(u.getFullName());
                                }
                            }

                            if (!criteria.getSortName().equals(StaffRequestFilter.SORT.None)) {
                                switch (criteria.getSortName()) {
                                    case DateTime:
                                        if (criteria.getSortType() == AdminRequestFilter.SORT_TYPE.ASC) {
                                            for (int i = 0; i < mainRequestList.size(); i++) {
                                                for (int j = i + 1; j < mainRequestList.size(); j++) {
                                                    if(mainRequestList.get(i).getDateTime() > mainRequestList.get(j).getDateTime()){
                                                        Request temp = mainRequestList.get(i);
                                                        mainRequestList.set(i,mainRequestList.get(j));
                                                        mainRequestList.set(j,temp);

                                                        String tempName = fullNameList.get(i);
                                                        fullNameList.set(i,fullNameList.get(j));
                                                        fullNameList.set(j,tempName);
                                                    }
                                                }
                                            }
                                        } else
                                            for (int i = 0; i < mainRequestList.size(); i++) {
                                                for (int j = i + 1; j < mainRequestList.size(); j++) {
                                                    if(mainRequestList.get(i).getDateTime() < mainRequestList.get(j).getDateTime()){
                                                        Request temp = mainRequestList.get(i);
                                                        mainRequestList.set(i,mainRequestList.get(j));
                                                        mainRequestList.set(j,temp);

                                                        String tempName = fullNameList.get(i);
                                                        fullNameList.set(i,fullNameList.get(j));
                                                        fullNameList.set(j,tempName);
                                                    }
                                                }
                                            }
                                        break;

                                    case Title:
                                        if (criteria.getSortType() == AdminRequestFilter.SORT_TYPE.ASC){
                                            for (int i = 0; i < mainRequestList.size(); i++) {
                                                for (int j = i + 1; j < mainRequestList.size(); j++) {
                                                    if(fullNameList.get(i).compareTo(fullNameList.get(j)) > 0){
                                                        Request temp = mainRequestList.get(i);
                                                        mainRequestList.set(i,mainRequestList.get(j));
                                                        mainRequestList.set(j,temp);

                                                        String tempName = fullNameList.get(i);
                                                        fullNameList.set(i,fullNameList.get(j));
                                                        fullNameList.set(j,tempName);
                                                    }
                                                }
                                            }
                                        }
                                        else
                                            for (int i = 0; i < mainRequestList.size(); i++) {
                                                for (int j = i + 1; j < mainRequestList.size(); j++) {
                                                    if(fullNameList.get(i).compareTo(fullNameList.get(j)) < 0){
                                                        Request temp = mainRequestList.get(i);
                                                        mainRequestList.set(i,mainRequestList.get(j));
                                                        mainRequestList.set(j,temp);

                                                        String tempName = fullNameList.get(i);
                                                        fullNameList.set(i,fullNameList.get(j));
                                                        fullNameList.set(j,tempName);
                                                    }
                                                }
                                            }
                                        break;
                                }
                            } else
                                finalList.sort((request, t1) -> request.getDateTime() > t1.getDateTime() ? -1 : 1);

                            fullNameList = fullNameList.stream().skip(offset).limit(numRow).collect(Collectors.toList());
                            mainRequestList = mainRequestList.stream().skip(offset).limit(numRow).collect(Collectors.toList());
                            fullNameListLD.postValue(fullNameList);
                            mLiveData.postValue(mainRequestList);
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

            @Override
            public void onLoading(Resource<List<Request>> loading) {

            }

            @Override
            public void onError(Resource<List<Request>> error) {
                mLiveData.postValue(new ArrayList<>());
                mLiveData.postValue(new ArrayList<>());
            }
        });
    }

    public void restoreRequest(Request request) {
        service.update(request);
    }

    public void insert(Request request, final int idUser, final int offset, final StaffRequestFilter criteria) {
        service.put(request, new ApiResponse<Request>() {
            @Override
            public void onSuccess(Resource<Request> success) {
                getLimitListRequestForStaffLD(idUser, offset, 1, criteria);
            }

            @Override
            public void onLoading(Resource<Request> loading) {

            }

            @Override
            public void onError(Resource<Request> error) {

            }
        });
    }

    public void updateRequest(Request request) {
        service.update(request);
    }

    public void deleteRequest(Request request) {
        service.delete(request.getId());
    }


}


