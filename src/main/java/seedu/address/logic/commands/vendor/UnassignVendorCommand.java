package seedu.address.logic.commands.vendor;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Unassigns an existing contact in the address book to become a vendor.
 * The person must currently be a vendor in the address book.
 */
public class UnassignVendorCommand extends Command {

    public static final String COMMAND_WORD = "unassign-vendor";

    public static final String COMMAND_KEYWORD = "uv";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Unassigns the vendor identified by the index number used in the displayed person list.\n"
            + " The person becomes a normal contact in the addressbook and cannot have tasks assigned to it.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "%1$s has been unassigned and is no longer a vendor.";

    public static final String MESSAGE_UNASSIGN_VENDOR_FAILURE_TASK_EXISTS = "This vendor cannot be unassigned "
        + "as it still has a task.";

    public static final String MESSAGE_UNASSIGN_VENDOR_FAILURE_NOT_VENDOR = "%1$s is not a vendor.";
    private final Index targetIndex;

    /**
     * Creates an UnassignVendorCommand to assign the specified {@code Person} as a vendor.
     */
    public UnassignVendorCommand(Index targetIndex) {
        assert targetIndex != null : "Target index must not be null";
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {

        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUnassign = lastShownList.get(targetIndex.getZeroBased());
        assert personToUnassign != null : "Person to unassign must not be null";


        // need to change to check if model has the vendor already existing
        if (!model.hasVendor(personToUnassign)) {
            throw new CommandException(String.format(MESSAGE_UNASSIGN_VENDOR_FAILURE_NOT_VENDOR,
                    personToUnassign.getName()));
        }

        model.unassignVendor(personToUnassign);
        assert !model.hasVendor(personToUnassign) : "Vendor was not unassigned correctly";

        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(personToUnassign)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UnassignVendorCommand)) {
            return false;
        }

        UnassignVendorCommand otherUnassignVendorCommand = (UnassignVendorCommand) other;
        return targetIndex.equals(otherUnassignVendorCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toUnassignVendor", targetIndex)
                .toString();
    }

}
