/**
 * MIT License
 *
 * Copyright (c) 2019-2021 Matt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.triumphteam.cmd.slash;

import dev.triumphteam.cmd.core.AbstractSubCommand;
import dev.triumphteam.cmd.core.argument.InternalArgument;
import dev.triumphteam.cmd.core.argument.LimitlessInternalArgument;
import dev.triumphteam.cmd.core.execution.ExecutionProvider;
import dev.triumphteam.cmd.core.suggestion.EmptySuggestion;
import dev.triumphteam.cmd.core.suggestion.SuggestionContext;
import dev.triumphteam.cmd.slash.choices.Choice;
import dev.triumphteam.cmd.slash.choices.EmptyChoice;
import dev.triumphteam.cmd.slash.util.JdaOptionUtil;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

final class SlashSubCommand<S> extends AbstractSubCommand<S> {

    private final String description;
    private final List<Choice> choices;

    public SlashSubCommand(
            @NotNull final SlashSubCommandProcessor<S> processor,
            @NotNull final String parentName,
            @NotNull final ExecutionProvider executionProvider
    ) {
        super(processor, parentName, executionProvider);
        this.description = processor.getDescription();
        this.choices = processor.getChoices();
    }

    public String getDescription() {
        return description;
    }

    public List<OptionData> getJdaOptions() {
        final List<OptionData> options = new ArrayList<>();
        final List<InternalArgument<S, ?>> internalArguments = getArguments();

        for (int i = 0; i < internalArguments.size(); i++) {
            final InternalArgument<S, ?> internalArgument = internalArguments.get(i);

            final OptionType type = JdaOptionUtil.fromType(internalArgument.getType());
            final OptionData option = new OptionData(
                    type,
                    internalArgument.getName(),
                    internalArgument.getDescription(),
                    !internalArgument.isOptional()
            );
            options.add(option);

            final Choice choice = getChoice(i);
            if (choice instanceof EmptyChoice) {
                if (internalArgument.getSuggestion() instanceof EmptySuggestion) continue;
                // For now suggestions are only allowed on Strings
                if (option.getType() != OptionType.STRING) continue;
                // Enable suggestions if available
                option.setAutoComplete(type.canSupportChoices());
                continue;
            }

            option.addChoices(choice.getChoices().stream().map(it -> new Command.Choice(it, it)).limit(25).collect(Collectors.toList()));
        }

        return options;
    }

    public List<String> getSuggestions(@NotNull final S sender, @NotNull final List<String> args) {
        final int index = args.size() - 1;
        final InternalArgument<S, ?> internalArgument = getArgument(index);
        if (internalArgument == null) return emptyList();

        final List<String> trimmed;
        if (internalArgument instanceof LimitlessInternalArgument) {
            trimmed = args.subList(getArguments().size() - 1, args.size());
        } else {
            trimmed = args.subList(index, args.size());
        }

        final SuggestionContext context = new SuggestionContext(args, getParentName(), getName());
        return internalArgument.suggestions(sender, trimmed, context);
    }

    @NotNull
    private Choice getChoice(final int index) {
        if (index >= choices.size()) return EmptyChoice.INSTANCE;
        return choices.get(index);
    }

}
