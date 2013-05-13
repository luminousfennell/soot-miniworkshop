import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TypeSpec<T> {
	private final List<T> inputs;
	private final T output;

	public interface ResultType<T> {
		boolean success();

		String getError();

		T getType();
	}

	public TypeSpec(List<T> inputs, T output) {
		this.inputs = Collections.unmodifiableList(inputs);
		this.output = output;
	}

	public T getOuput() {
		return output;
	}

	public ResultType<T> apply(Comparator<T> cmp, List<T> args) {
		if (inputs.size() != args.size()) {
			return fail("Wrong number of arguments (" + "inputs:" + inputs.size() + 
					    " args:" + args.size() + ")... should not happen.");
		} else {
			for (int i = 0; i < inputs.size(); i++) {
				if (cmp.compare(args.get(i), inputs.get(i)) > 0) {
					return fail("Type mismatch at arpos " + i);
				}
			}
			return success(output);
		}
	}

	private ResultType<T> fail(final String message) {
		return new ResultType<T>() {

			@Override
			public boolean success() {
				return false;
			}

			@Override
			public T getType() {
				throw new IllegalArgumentException(
						"Trying to get a result from a failed typing.");
			}

			@Override
			public String getError() {
				return message;
			}
		};
	}

	private ResultType<T> success(final T result) {
		return new ResultType<T>() {

			@Override
			public boolean success() {
				return true;
			}

			@Override
			public T getType() {
				return result;
			}

			@Override
			public String getError() {
				throw new IllegalArgumentException(
						"Trying to get an error out of a successful typing.");
			}
		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + ((output == null) ? 0 : output.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypeSpec other = (TypeSpec) obj;
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		if (output == null) {
			if (other.output != null)
				return false;
		} else if (!output.equals(other.output))
			return false;
		return true;
	}

}
