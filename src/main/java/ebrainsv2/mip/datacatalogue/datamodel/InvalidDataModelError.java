package ebrainsv2.mip.datacatalogue.datamodel;

public class InvalidDataModelError extends RuntimeException {
    public InvalidDataModelError(String message) {
        super(message);
    }
}