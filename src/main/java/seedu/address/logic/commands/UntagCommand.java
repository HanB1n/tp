package seedu.address.logic.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Removes a tag associated with an existing person in the address book.
 */
public class UntagCommand extends Command {

    public static final String COMMAND_WORD = "untag";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The person index provided is invalid.";
    public static final String MESSAGE_TAG_NOT_FOUND = "Some tags were not found in the person's tag list.";

    private final Index index;
    private final List<Tag> tagsToRemove;

    public UntagCommand(Index index, List<Tag> tagsToRemove) {
        this.index = index;
        this.tagsToRemove = tagsToRemove;
    }

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes one or multiple tags from the person identified "
            + "by the index number used in the last person listing.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "t/[TAG]... (can specify multiple tags)\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + "t/florist t/photographer.";


    /**
     * Generates a command execution success message showing the removed tags and the person.
     *
     * @param personToEdit The person from whom the tags were removed.
     * @return A success message indicating the tags that were removed and the name of the person.
     */
    private String generateSuccessMessage(Person personToEdit) {
        String removedTags = tagsToRemove.stream()
                .map(Tag::toString)
                .collect(Collectors.joining(", "));
        return String.format("Removed tag(s) %s from %s", removedTags, personToEdit.getName().toString());
    }


    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Set<Tag> updatedTags = new HashSet<>(personToEdit.getTags()); // Copy the current tags

        if (!updatedTags.containsAll(tagsToRemove)) {
            throw new CommandException(MESSAGE_TAG_NOT_FOUND);
        }
        updatedTags.removeAll(tagsToRemove);

        Person editedPerson = new Person(
                personToEdit.getName(),
                personToEdit.getPhone(),
                personToEdit.getEmail(),
                personToEdit.getAddress(),
                updatedTags);

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(generateSuccessMessage(personToEdit));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof UntagCommand)) {
            return false;
        }

        UntagCommand otherCommand = (UntagCommand) other;
        return index.equals(otherCommand.index)
                && tagsToRemove.equals(otherCommand.tagsToRemove);
    }


}
