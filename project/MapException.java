package project;

public class MapException extends RuntimeException {
    // // harita islemede hata olursa kullanıcaz
    public MapException(String message) {
        super(message);
    }
}