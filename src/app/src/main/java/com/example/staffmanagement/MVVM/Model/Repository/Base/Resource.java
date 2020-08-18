package com.example.staffmanagement.MVVM.Model.Repository.Base;

public abstract class Resource<RequestType> {
    protected RequestType data;
    protected String message;

    public Resource(RequestType data) {
        this.data = data;
    }

    public Resource(RequestType data, String message) {
        this.data = data;
        this.message = message;
    }

    public RequestType getData() {
        return data;
    }

    public void setData(RequestType data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
