package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;
import seedu.address.model.task.Deadline;
import seedu.address.model.task.Event;
import seedu.address.model.task.Task;
import seedu.address.model.task.Todo;
import seedu.address.model.wedding.Wedding;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final List<JsonAdaptedWedding> weddings = new ArrayList<>();
    private final List<JsonAdaptedTask> tasks = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons, tags, weddings and tasks.
     */
    @JsonCreator
    public JsonSerializableAddressBook(
            @JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("weddings") List<JsonAdaptedWedding> weddings,
            @JsonProperty("tasks") List<JsonAdaptedTask> tasks) {
        this.persons.addAll(persons);
        this.tags.addAll(tags);
        this.weddings.addAll(weddings);
        this.tasks.addAll(tasks);
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        tags.addAll(source.getTagList().stream().map(JsonAdaptedTag::new).collect(Collectors.toList()));
        weddings.addAll(source.getWeddingList().stream().map(JsonAdaptedWedding::new).collect(Collectors.toList()));
        tasks.addAll(source.getTaskList().stream().map(this::mapToJsonAdaptedTask).collect(Collectors.toList()));;
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            // Each person will store blank weddings with only the wedding name at this point
            Person person = jsonAdaptedPerson.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(Messages.MESSAGE_DUPLICATE_PERSON);
            }
            addressBook.addPerson(person);
            if (jsonAdaptedPerson.isVendor()) {
                addressBook.addVendor(person);
            }
            if (jsonAdaptedPerson.hasTasks()) {
                addressBook.addVendor(person);
            }
        }
        for (JsonAdaptedTag jsonAdaptedTag : tags) {
            Tag tag = jsonAdaptedTag.toModelType();
            if (addressBook.hasTag(tag)) {
                throw new IllegalValueException(Messages.MESSAGE_DUPLICATE_TAG);
            }
            addressBook.addTag(tag);
        }
        for (JsonAdaptedTask jsonAdaptedTask : tasks) {
            Task task = jsonAdaptedTask.toModelType();
            if (addressBook.hasTask(task)) {
                throw new IllegalValueException(Messages.MESSAGE_DUPLICATE_TASK_IN_WEDLINKER);
            }
            addressBook.addTask(task);
        }
        for (JsonAdaptedWedding jsonAdaptedWedding : weddings) {
            Wedding wedding = jsonAdaptedWedding.toModelType();
            if (addressBook.hasWedding(wedding)) {
                throw new IllegalValueException(Messages.MESSAGE_DUPLICATE_WEDDING);
            }
            addressBook.addWedding(wedding);
            if (wedding.hasPartner1()) {
                addressBook.getPerson(wedding.getPartner1()).addWedding(wedding);
                // Replaces the Wedding object's Person with the Person object from the Wedlinker
                wedding.setPartner1(addressBook.getPerson(wedding.getPartner1()));
            }
            if (wedding.hasPartner2()) {
                addressBook.getPerson(wedding.getPartner2()).addWedding(wedding);
                // Replaces the Wedding object's Person with the Person object from the Wedlinker
                wedding.setPartner2(addressBook.getPerson(wedding.getPartner2()));
            }
            for (Person person : wedding.getGuestList()) {
                addressBook.getPerson(person).addWedding(wedding);
                // Replaces the Wedding object's Person with the Person object from the Wedlinker
                try {
                    wedding.setGuest(person, addressBook.getPerson(person));
                } catch (PersonNotFoundException p) {
                    wedding.addToGuestList(addressBook.getPerson(person));
                }
            }
        }
        // load tags and weddings from people after loading weddings and tags, because if tag or wedding already exist,
        // method will throw an error
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            loadTags(addressBook, person);
            loadWeddings(addressBook, person);
            loadTasks(addressBook, person);
        }
        return addressBook;
    }

    private void loadTags(AddressBook addressBook, Person person) {
        Set<Tag> tagList = person.getTags();
        for (Tag tag : tagList) {
            if (addressBook.hasTag(tag) || !Tag.isValidTagName(tag.getTagName().toString())) {
                continue;
            }
            addressBook.addTag(tag);
        }
    }

    private void loadWeddings(AddressBook addressBook, Person person) {
        Set<Wedding> weddingList = person.getWeddings();
        Set<Wedding> newWeddingList = new HashSet<>();
        for (Wedding wedding : weddingList) {
            // If the wedding does not contain the person, add them to the guest list of the wedding by default
            if (!addressBook.getWedding(wedding).hasPerson(person)) {
                addressBook.getWedding(wedding).addToGuestList(addressBook.getPerson(person));
            }

            if (addressBook.hasWedding(wedding) || !Wedding.isValidWeddingName(wedding.getWeddingName().toString())) {
                newWeddingList.add(addressBook.getWedding(wedding));
                continue;
            }

            addressBook.addWedding(wedding);
            newWeddingList.add(addressBook.getWedding(wedding));
        }
        person.setWeddings(newWeddingList);
    }

    private void loadTasks(AddressBook addressBook, Person person) {
        Set<Task> taskList = person.getTasks();
        for (Task task : taskList) {
            if (addressBook.hasTask(task)) {
                continue;
            }
            addressBook.addTask(task);
        }
    }

    /**
     * Maps a given Task to its corresponding JsonAdaptedTask subclass.
     */
    private JsonAdaptedTask mapToJsonAdaptedTask(Task task) {
        if (task instanceof Todo) {
            return new JsonAdaptedTodo((Todo) task);
        } else if (task instanceof Deadline) {
            return new JsonAdaptedDeadline((Deadline) task);
        } else if (task instanceof Event) {
            return new JsonAdaptedEvent((Event) task);
        } else {
            throw new IllegalArgumentException("Unknown task type");
        }
    }
}
