package com.magictainer.jvcommon.common.exception;

import com.magictainer.jvcommon.common.constants.ResponseConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RestException extends RuntimeException {

    private String errorStatus;
    private int errorCode;
    private String errorMessage;
    private Object data;

    public RestException(String errorStatus, int errorCode, String errorMessage, Object data) {
        this.errorStatus = errorStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public RestException(ResponseConstants.ResponseStatus status, String errorMessage, Object data) {
        this.errorStatus = status.getMessage();
        this.errorCode = status.getValue();
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public RestException(ResponseConstants.ResponseStatus status, String errorMessage) {
        this.errorStatus = status.getMessage();
        this.errorCode = status.getValue();
        this.errorMessage = errorMessage;
    }

    public RestException(String errorMessage) {
        this.errorStatus = ResponseConstants.ResponseStatus.GENERAL_SERVER_ERROR.getMessage();
        this.errorCode = ResponseConstants.ResponseStatus.GENERAL_SERVER_ERROR.getValue();
        this.errorMessage = errorMessage;
    }
}
