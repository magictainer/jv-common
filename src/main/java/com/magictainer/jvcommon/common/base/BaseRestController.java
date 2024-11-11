package com.magictainer.jvcommon.common.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.magictainer.jvcommon.common.constants.ResponseConstants;
import com.magictainer.jvcommon.common.exception.RestException;
import com.magictainer.jvcommon.common.helpers.ResponseHelpers;
import com.magictainer.jvcommon.common.model.ResponseModel;
import com.magictainer.jvcommon.common.utils.JVUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@RestControllerAdvice
public class BaseRestController extends ResponseEntityExceptionHandler {

    @Autowired
    private ResponseHelpers responseHelpers;

    protected String responseData(Object data, HttpServletRequest request, HttpServletResponse response) {
        log.info("Response (200) Data => {}", JVUtils.toJsonString(data));
        try {
            return responseHelpers.responseData(data, request, response);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            throw new RestException(e.getMessage());
        }
    }

    protected String responseSuccess(String message, HttpServletRequest request, HttpServletResponse response) {
        log.info("Response (200) Message Key => {}", message);
        try {
            return responseHelpers.responseSuccess(message, request, response);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            throw new RestException(e.getMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected final ResponseEntity<?> handleAllExceptions(final HttpServletRequest request,
                                                          final HttpServletResponse response,
                                                          final Exception e) {
        logger.error("Catch exception: " + e.getMessage());
        ResponseModel responseModel = new ResponseModel();
        responseModel.setDatetime(LocalDateTime.now().toString());
        responseModel.setTimestamp(new Date().getTime());
        responseModel.setStatus(ResponseConstants.ResponseStatus.GENERAL_SERVER_ERROR.getMessage());
        responseModel.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        responseModel.setMessage(ResponseConstants.ResponseMessage.SYSTEM_INTERNAL_SERVER_ERROR.getMessage());
        responseModel.setTrace(e.getMessage());
        responseModel.setPath(request.getRequestURI());
        return new ResponseEntity<>(responseModel, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RestException.class)
    protected final ResponseEntity<?> handleApiErrorExceptions(final HttpServletRequest request,
                                                               final HttpServletResponse response,
                                                               final RestException e) {
        logger.error("Catch Rest exception: " + e.getErrorMessage());
        ResponseModel responseModel = new ResponseModel();
        responseModel.setDatetime(LocalDateTime.now().toString());
        responseModel.setTimestamp(new Date().getTime());
        responseModel.setStatus(e.getErrorStatus());
        responseModel.setCode(e.getErrorCode());
        responseModel.setMessage(e.getErrorMessage());
        responseModel.setData(e.getData());
        responseModel.setTrace(e.getMessage());
        responseModel.setPath(request.getRequestURI());
        return new ResponseEntity<>(responseModel, HttpStatus.OK);
    }

}
