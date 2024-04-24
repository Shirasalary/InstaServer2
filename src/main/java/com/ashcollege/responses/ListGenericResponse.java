package com.ashcollege.responses;

import java.util.List;

public class ListGenericResponse<T> extends BasicResponse{

    private List<T> list;

    public ListGenericResponse(boolean success, Integer errorCode, List<T> allUsers) {
        super(success, errorCode);
        this.list = allUsers;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
