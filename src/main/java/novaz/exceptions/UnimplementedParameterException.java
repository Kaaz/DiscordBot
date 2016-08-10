package novaz.exceptions;

import java.sql.SQLException;

public class UnimplementedParameterException extends SQLException {

	private String s;

	public UnimplementedParameterException(Object parameter) {
		s = "Probleem parameter niet geimplmenteerd bij preparedstatement.set[Paramtype](i,p). parameter:" + parameter;
	}

	public UnimplementedParameterException(Object parameter, int pos) {
		s = "Probleem parameter niet geimplmenteerd bij preparedstatement.set[Paramtype](i,p). parameter:" + parameter + " - pos:" + pos;
	}

	@Override
	public String toString() {
		return s;
	}
}
