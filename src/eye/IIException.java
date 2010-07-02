/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eye;

import java.io.IOException;

/**
 * IncorrectImageException
 * @author spr1ng
 * @version $Id: IIException.java 24 2010-06-30 07:43:45Z spr1ng $
 */
public class IIException extends IOException {

    public IIException(String message) {
        super(message);
    }

    public IIException(Throwable cause) {
        super(cause);
    }

    public IIException(String message, Throwable cause) {
        super(message, cause);
    }

}
