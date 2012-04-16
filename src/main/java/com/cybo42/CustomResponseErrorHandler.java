package com.cybo42;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jcyboski
 *         Date: 4/15/12
 */
@Service("restTemplateErrorHandler")
public class CustomResponseErrorHandler extends DefaultResponseErrorHandler{
    private static Logger logger = LoggerFactory.getLogger(CustomResponseErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException{
        try{
            // First try to use the DefaultResponseHandler
            return super.hasError(response);

        }catch(IllegalArgumentException argException){
            // If we are there then status code was not in the default list
            // try check the custom list

            logger.info("ERROR CODE = {}", response.getRawStatusCode());

            return hasError(CustomHttpStatus.valueOf(response.getRawStatusCode()));

        }
    }

    // Determine if this is client side or server side error
    public boolean hasError(CustomHttpStatus statusCode) {
           return (statusCode.series() == HttpStatus.Series.CLIENT_ERROR ||
                   statusCode.series() == HttpStatus.Series.SERVER_ERROR);
    }

    /**
     * Same logic as {@see DefaultResponseErrorHandler#handleError} just modified to use
     * {@see CustomHttpStatus} and exceptions
     * {@see CustomClientErrorException} {@see CustomServerErrorException}
     *
     * @param response
     * @throws IOException
     */
    @Override
    public void handleError(ClientHttpResponse response) throws IOException{
        HttpStatus.Series series = null;
        CustomHttpStatus statusCode = CustomHttpStatus.valueOf(response.getRawStatusCode());
            series = statusCode.series();


        byte[] body = getResponseBody(response);
        switch(series){
            case CLIENT_ERROR:
                throw new CustomClientErrorException("error", response.getRawStatusCode(), statusCode, response.getHeaders(), body);
            case SERVER_ERROR:
                throw new CustomServerErrorException("error", response.getRawStatusCode(), statusCode, response.getHeaders(), body);
            default:
                throw new RestClientException("Unknown status code [" + statusCode + "]");
        }
    }


    private byte[] getResponseBody(ClientHttpResponse response){
        try{
            InputStream responseBody = response.getBody();
            if(responseBody != null){
                return FileCopyUtils.copyToByteArray(responseBody);
            }
        }catch(IOException ex){
            // ignore
        }
        return new byte[0];
    }

}

