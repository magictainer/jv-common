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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class BaseRestController extends ResponseEntityExceptionHandler {

    protected String responseModel(Object data, HttpServletRequest request, HttpServletResponse response) {
        log.info("Response (200) Data: {}", JVUtils.toJsonString(data));
        try {
            return ResponseHelpers.getInstance().responseData(data, request, response);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            throw new RestException(e.getMessage());
        }
    }

    protected String responseSuccess(String message, HttpServletRequest request, HttpServletResponse response) {
        log.info("Response (200) Success: {}", message);
        try {
            return ResponseHelpers.getInstance().responseSuccess(message, request, response);
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

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logger.error("Catch handleMethodArgumentNotValid: " + ex.getMessage());
        ResponseModel responseModel = new ResponseModel();
        responseModel.setDatetime(LocalDateTime.now().toString());
        responseModel.setTimestamp(new Date().getTime());
        responseModel.setStatus(ResponseConstants.ResponseStatus.BAD_REQUEST.getMessage());
        responseModel.setCode(ResponseConstants.ResponseStatus.BAD_REQUEST.getValue());
        responseModel.setMessage(ResponseConstants.ResponseMessage.SYSTEM_INVALID_REQUEST.getMessage());
        responseModel.setTrace(ex.getMessage());
        responseModel.setPath(((ServletWebRequest) request).getRequest().getRequestURI());
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        if (!CollectionUtils.isEmpty(fieldErrors)) {
            responseModel.setStatus(ResponseConstants.ResponseStatus.GENERAL_SERVER_ERROR.getMessage());
            responseModel.setCode(ResponseConstants.ResponseStatus.GENERAL_SERVER_ERROR.getValue());
            responseModel.setMessage(fieldErrors.get(0).getDefaultMessage());
            return new ResponseEntity<>(responseModel, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(responseModel, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logger.error("Catch handleNoResourceFoundException: " + ex.getMessage());
        ResponseModel responseModel = new ResponseModel();
        responseModel.setDatetime(LocalDateTime.now().toString());
        responseModel.setTimestamp(new Date().getTime());
        responseModel.setStatus(ResponseConstants.ResponseStatus.RESOURCE_NOT_FOUND.getMessage());
        responseModel.setCode(HttpStatus.NOT_FOUND.value());
        responseModel.setMessage(ResponseConstants.ResponseMessage.SYSTEM_RESOURCE_NOT_FOUND.getMessage());
        responseModel.setTrace(ex.getMessage());
        responseModel.setPath(((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<>(responseModel, HttpStatus.NOT_FOUND);
    }
}
