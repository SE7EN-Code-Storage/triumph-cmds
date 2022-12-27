package dev.triumphteam.cmd.core.argument.named;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public abstract class FlagsContainer implements Arguments {

    @Override
    public boolean hasFlag(final @NotNull String flag) {
        return false;
    }

    @Override
    public @NotNull <T> Optional<T> getValue(final @NotNull String flag, final @NotNull Class<T> type) {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<String> getValue(final @NotNull String flag) {
        return Optional.empty();
    }

    @Override
    public @NotNull String getText() {
        return null;
    }

    @Override
    public @NotNull String getText(final @NotNull String delimiter) {
        return null;
    }

    @Override
    public @NotNull List<@NotNull String> getArgs() {
        return null;
    }
}
