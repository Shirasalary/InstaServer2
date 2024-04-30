
package com.ashcollege.responses;

public class BooleanResponse extends BasicResponse {
    private boolean result;

    public BooleanResponse(boolean result) {
        this.result = result;
    }

    public BooleanResponse(boolean success, Integer errorCode, boolean result) {
        super(success, errorCode);
        this.result = result;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
