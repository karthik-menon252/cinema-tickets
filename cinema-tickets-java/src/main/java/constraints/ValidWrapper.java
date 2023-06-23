package constraints;
import jakarta.validation.Valid;

public class ValidWrapper<T> {
    @Valid
    private final T[] data;

    public ValidWrapper(final T[] data) {
        this.data = data;
    }

    public T[] getData() {
        return this.data;
    }
}
