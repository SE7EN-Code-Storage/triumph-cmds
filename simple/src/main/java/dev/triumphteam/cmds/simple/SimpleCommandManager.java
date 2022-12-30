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
package dev.triumphteam.cmds.simple;

import dev.triumphteam.cmd.core.CommandManager;
import dev.triumphteam.cmd.core.command.execution.AsyncExecutionProvider;
import dev.triumphteam.cmd.core.command.execution.ExecutionProvider;
import dev.triumphteam.cmd.core.command.execution.SyncExecutionProvider;
import dev.triumphteam.cmd.core.extention.CommandOptions;
import dev.triumphteam.cmd.core.extention.registry.RegistryContainer;
import dev.triumphteam.cmd.core.message.MessageKey;
import dev.triumphteam.cmd.core.message.context.DefaultMessageContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class SimpleCommandManager<S> extends CommandManager<S, S> {

    private final Map<String, SimpleCommand<S>> commands = new HashMap<>();

    private final RegistryContainer<S> registryContainer = new RegistryContainer<>();

    private final ExecutionProvider syncExecutionProvider = new SyncExecutionProvider();
    private final ExecutionProvider asyncExecutionProvider = new AsyncExecutionProvider();

    private SimpleCommandManager(final @NotNull CommandOptions<S, S> commandOptions) {
        super(commandOptions);
    }

    @Contract("_ -> new")
    public static <S> @NotNull SimpleCommandManager<S> create(final @NotNull Consumer<SimpleCommandOptions.Builder<S>> builder) {
        final SimpleCommandOptions.Builder<S> extensionBuilder = new SimpleCommandOptions.Builder<>();
        builder.accept(extensionBuilder);
        return new SimpleCommandManager<>(extensionBuilder.build());
    }

    @Override
    public void registerCommand(final @NotNull Object command) {
        final SimpleCommandProcessor<S> processor = new SimpleCommandProcessor<>(
                command,
                getRegistryContainer(),
                getCommandOptions().getCommandExtensions()
        );

        final String name = processor.getName();

        final SimpleCommand<S> simpleCommand = commands.get(name);
        if (simpleCommand != null) {
            // TODO: Command exists, only care about adding subs
            return;
        }

        // Command does not exist, proceed to add new!
        final SimpleCommand<S> newSimpleCommand = commands.computeIfAbsent(processor.getName(), it -> new SimpleCommand<>(processor, getRegistryContainer().getMessageRegistry()));
        processor.commands(newSimpleCommand.getMeta()).forEach(it -> newSimpleCommand.addSubCommand(it, false));

        processor.getAlias().forEach(it -> {
            final SimpleCommand<S> aliasCommand = commands.computeIfAbsent(it, ignored -> new SimpleCommand<>(processor, getRegistryContainer().getMessageRegistry()));
            // Adding sub commands.
            processor.commands(newSimpleCommand.getMeta()).forEach(sub -> aliasCommand.addSubCommand(sub, false));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected @NotNull RegistryContainer<S> getRegistryContainer() {
        return registryContainer;
    }

    @Override
    public void unregisterCommand(final @NotNull Object command) {
        // TODO add a remove functionality
    }

    /**
     * Execute the commands given the passed arguments.
     *
     * @param sender The provided sender.
     * @param args   The provided arguments.
     */
    public void executeCommand(final @NotNull S sender, final @NotNull List<@NotNull String> args) {
        if (args.isEmpty()) return;
        final String commandName = args.get(0);

        final SimpleCommand<S> command = commands.get(commandName);
        if (command == null) {
            registryContainer.getMessageRegistry().sendMessage(
                    MessageKey.UNKNOWN_COMMAND,
                    sender,
                    new DefaultMessageContext(commandName, "")
            );
            return;
        }

        command.execute(sender, args.subList(1, args.size()));
    }
}
