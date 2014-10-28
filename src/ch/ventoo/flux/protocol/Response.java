package ch.ventoo.flux.protocol;

/**
 * Created by nano on 09/10/14.
 */
public interface Response {
    public int getType();
    public byte[] getBody();
}
