/**
 * MIT License
 * <p>
 * Copyright (c) 2019-2021 Matt
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.triumphteam.cmd.core.processor;

import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.AnnotationContainer;
import dev.triumphteam.cmd.core.annotations.Description;
import dev.triumphteam.cmd.core.command.Command;
import dev.triumphteam.cmd.core.exceptions.CommandRegistrationException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractRootCommandProcessor<S> {

    private final BaseCommand baseCommand;

    private final String name;
    private final List<String> alias;
    private final String description;

    public AbstractRootCommandProcessor(final @NotNull BaseCommand baseCommand) {
        this.baseCommand = baseCommand;

        this.name = nameOf();
        this.alias = aliasOf();
        this.description = descriptionOf();
    }

    public String getName() {
        return name;
    }

    public List<String> getAlias() {
        return alias;
    }

    public String getDescription() {
        return description;
    }

    public AnnotationContainer createAnnotationContainer() {
        return null;
    }

    public @NotNull List<Command<S>> commands() {
        final Class<? extends BaseCommand> klass = baseCommand.getClass();

        final List<Command<S>> subCommands = new ArrayList<>();
        subCommands.addAll(methodCommands(klass.getDeclaredMethods()));
        subCommands.addAll(classCommands(klass.getDeclaredClasses()));

        return subCommands;
    }

    private @NotNull List<Command<S>> methodCommands(final @NotNull Method[] methods) {
        return Arrays.stream(methods).map(method -> {
            // Ignore non-public methods
            if (!Modifier.isPublic(method.getModifiers())) return null;

            /*final SubCommandProcessor<S> processor = new SubCommandProcessor<>(
                    name,
                    baseCommand,
                    method
            );

            // Not a command, ignore the method
            if (processor.getName() == null) return null;

*/
            return (Command<S>) () -> null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private @NotNull List<Command<S>> classCommands(final @NotNull Class<?>[] classes) {
        final List<Command<S>> subCommands = new ArrayList<>();
        for (final Class<?> klass : classes) {
            // Ignore non-public methods
            if (!Modifier.isPublic(klass.getModifiers())) continue;

            final String name = "";
            // Not a command, ignore the method
            if (name == null) continue;

            System.out.println("Sub boy -> " + name);

            subCommands.addAll(methodCommands(klass.getDeclaredMethods()));
            subCommands.addAll(classCommands(klass.getDeclaredClasses()));
        }

        return subCommands;
    }

    private @NotNull String nameOf() {
        final Class<? extends @NotNull BaseCommand> commandClass = baseCommand.getClass();
        final dev.triumphteam.cmd.core.annotations.Command commandAnnotation = commandClass.getAnnotation(dev.triumphteam.cmd.core.annotations.Command.class);

        final String name;
        if (commandAnnotation == null) {
            final String commandName = baseCommand.getCommand();
            if (commandName == null) {
                throw new CommandRegistrationException("Command name or \"@" + dev.triumphteam.cmd.core.annotations.Command.class.getSimpleName() + "\" annotation missing", baseCommand.getClass());
            }

            name = commandName;
        } else {
            name = commandAnnotation.value();
        }

        if (name.isEmpty() || name.equals(dev.triumphteam.cmd.core.annotations.Command.DEFAULT_CMD_NAME)) {
            throw new CommandRegistrationException("Command name must not be empty", baseCommand.getClass());
        }

        return name;
    }

    private @NotNull List<String> aliasOf() {
        final dev.triumphteam.cmd.core.annotations.Command commandAnnotation = baseCommand.getClass().getAnnotation(dev.triumphteam.cmd.core.annotations.Command.class);
        return commandAnnotation == null ? baseCommand.getAlias() : Arrays.asList(commandAnnotation.alias());
    }

    private @NotNull String descriptionOf() {
        final Description commandAnnotation = baseCommand.getClass().getAnnotation(Description.class);
        return commandAnnotation == null ? baseCommand.getDescription() : commandAnnotation.value();
    }
}