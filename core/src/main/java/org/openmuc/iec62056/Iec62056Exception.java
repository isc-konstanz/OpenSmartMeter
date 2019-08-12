package org.openmuc.iec62056;

import java.io.IOException;

public class Iec62056Exception extends IOException {
	private static final long serialVersionUID = -8750634337953036515L;

	public Iec62056Exception() {
        super();
    }

    public Iec62056Exception(String s) {
        super(s);
    }

    public Iec62056Exception(Throwable cause) {
        super(cause);
    }

    public Iec62056Exception(String s, Throwable cause) {
        super(s, cause);
    }
}
