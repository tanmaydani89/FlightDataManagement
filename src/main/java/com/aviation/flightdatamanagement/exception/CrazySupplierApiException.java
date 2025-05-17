package com.aviation.flightdatamanagement.exception;

/**
 * Custom exception to represent errors occurring during interaction
 * with the "CrazySupplier" external API.
 */
public class CrazySupplierApiException extends RuntimeException {

    public CrazySupplierApiException() {
        super();
    }


    public CrazySupplierApiException(String message) {
        super(message);
    }

    public CrazySupplierApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public CrazySupplierApiException(Throwable cause) {
        super(cause);
    }
}
