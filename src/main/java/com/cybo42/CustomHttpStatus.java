package com.cybo42;

/**
 * Based on {@see HttpStatus} used to store custom HTTP Status codes
 * @author jcyboski
 *         Date: 4/15/12
 */

import org.springframework.http.HttpStatus;

public enum CustomHttpStatus{
    VALIDATION_ERROR(450, "Validation Error");

      public static CustomHttpStatus valueOf(int statusCode) {
          for (CustomHttpStatus status : values()) {
              if (status.value == statusCode) {
                  return status;
              }
          }
          throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
      }


    private final int value;
    private final String reasonPhrase;


    private CustomHttpStatus(int value, String reasonPhrase){
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Return the integer value of this status code.
     */
    public int value(){
        return this.value;
    }

    /**
     * Return the reason phrase of this status code.
     */
    public String getReasonPhrase(){
        return reasonPhrase;
    }

    /**
     * Returns the HTTP status series of this status code.
     *
     * @see HttpStatus.Series
     */
    public HttpStatus.Series series(){
        int seriesCode = this.value() / 100;

        for(HttpStatus.Series series : HttpStatus.Series.values()){
            if(series.value() == seriesCode){
                return series;
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + this + "]");

    }

    /**
     * Return a string representation of this status code.
     */
    @Override
    public String toString(){
        return Integer.toString(value);
    }

}


