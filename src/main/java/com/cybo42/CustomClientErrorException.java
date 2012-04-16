package com.cybo42;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientException;

/**
 * @author jcyboski
 *         Date: 4/15/12
 */
public class CustomClientErrorException extends RestClientException{
    private int code;
    private CustomHttpStatus status;
    private HttpHeaders headers;
    private byte[] body;


    public CustomClientErrorException(String msg){
        super(msg);
    }

    public CustomClientErrorException(String msg, Throwable ex){
        super(msg, ex);
    }

    public CustomClientErrorException(String msg, int code, CustomHttpStatus status, HttpHeaders headers, byte[] body){
        super(msg);
        this.code = code;
        this.status = status;
        this.headers = headers;
        this.body = body;
    }


    public int getCode(){
        return code;
    }

    public void setCode(int code){
        this.code = code;
    }

    public CustomHttpStatus getStatus(){
        return status;
    }

    public void setStatus(CustomHttpStatus status){
        this.status = status;
    }

    public HttpHeaders getHeaders(){
        return headers;
    }

    public void setHeaders(HttpHeaders headers){
        this.headers = headers;
    }

    public byte[] getBody(){
        return body;
    }

    public void setBody(byte[] body){
        this.body = body;
    }
}
