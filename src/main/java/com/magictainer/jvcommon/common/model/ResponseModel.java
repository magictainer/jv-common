package com.magictainer.jvcommon.common.model;

import lombok.Data;

@Data
public class ResponseModel {

    private String datetime;
    private long timestamp;
    private String status;
    private int code;
    private String message;
    private Object data;
    private String trace;
    private String path;

}
