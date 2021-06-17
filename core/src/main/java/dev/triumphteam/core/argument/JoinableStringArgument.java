package dev.triumphteam.core.argument;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JoinableStringArgument implements Argument {

    private final CharSequence delimiter;

    public JoinableStringArgument(@NotNull final CharSequence delimiter) {
        this.delimiter = delimiter;
    }

    @Nullable
    @Override
    public Object resolve(@NotNull final Object value) {
        if (!(value instanceof String[])) {
            return null;
        }

        return String.join(delimiter, (String[]) value);
    }

}
