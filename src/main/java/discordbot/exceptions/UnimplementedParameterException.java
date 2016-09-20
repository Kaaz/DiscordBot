package discordbot.exceptions;

import java.sql.SQLException;

public class UnimplementedParameterException extends SQLException {

	private String s;

	public UnimplementedParameterException(Object parameter) {
		s = "Parameter not implemented at for: " + parameter;
	}

	public UnimplementedParameterException(Object parameter, int pos) {
		s = "Parameter not implemented! parameter:" + parameter + " - position:" + pos;
	}

	@Override
	public String toString() {
		return s;
	}
}
