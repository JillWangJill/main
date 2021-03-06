package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.event.Event;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.reminder.Reminder;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final VersionedAddressBook versionedAddressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final FilteredList<Event> filteredEvents;
    private final FilteredList<Reminder> filteredReminders;
    private final SimpleObjectProperty<Person> selectedPerson = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Event> selectedEvent = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Reminder> selectedReminder = new SimpleObjectProperty<>();

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        super();
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        versionedAddressBook = new VersionedAddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(versionedAddressBook.getPersonList());
        filteredPersons.addListener(this::ensureSelectedPersonIsValid);
        filteredEvents = new FilteredList<>(versionedAddressBook.getEventList());
        filteredEvents.addListener(this::ensureSelectedEventIsValid);
        filteredReminders = new FilteredList<>(versionedAddressBook.getReminderList());
        filteredReminders.addListener(this::ensureSelectedReminderIsValid);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs);
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public Path getAddressBookFilePath() {
        return userPrefs.getAddressBookFilePath();
    }

    @Override
    public void setAddressBookFilePath(Path addressBookFilePath) {
        requireNonNull(addressBookFilePath);
        userPrefs.setAddressBookFilePath(addressBookFilePath);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        versionedAddressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return versionedAddressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return versionedAddressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        versionedAddressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        versionedAddressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        versionedAddressBook.setPerson(target, editedPerson);
    }

    @Override
    public boolean hasEvent(Event event) {
        requireNonNull(event);
        return versionedAddressBook.hasEvent(event);
    }

    @Override
    public void deleteEvent(Event target) {
        versionedAddressBook.removeEvent(target);
    }

    @Override
    public void addEvent(Event event) {
        versionedAddressBook.addEvent(event);
        updateFilteredEventList(PREDICATE_SHOW_ALL_EVENTS);
    }

    @Override
    public void setEvent(Event target, Event editedEvent) {
        requireAllNonNull(target, editedEvent);

        versionedAddressBook.setEvent(target, editedEvent);
    }

    @Override
    public boolean hasReminder(Reminder reminder) {
        requireNonNull(reminder);
        return versionedAddressBook.hasReminder(reminder);
    }

    @Override
    public void deleteReminder(Reminder target) {
        versionedAddressBook.removeReminder(target);
    }

    @Override
    public void deleteReminder(Event target) {
        versionedAddressBook.removeReminder(target);
    }

    @Override
    public void addReminder(Reminder reminder) {
        versionedAddressBook.addReminder(reminder);
        updateFilteredReminderList(PREDICATE_SHOW_ALL_REMINDERS);
    }

    @Override
    public void addShownReminder(Reminder reminder) {
        //versionedAddressBook.addReminder(reminder);
        updateFilteredReminderList(PREDICATE_SHOW_ALL_REMINDERS);
    }

    /*@Override
    public void setReminder(Reminder target, Reminder editedReminder) {
        requireAllNonNull(target, editedReminder);

        versionedAddressBook.setReminder(target, editedReminder);
    }*/

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    //=========== Filtered Event List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Event} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Event> getFilteredEventList() {
        return filteredEvents;
    }

    @Override
    public void updateFilteredEventList(Predicate<Event> predicate) {
        requireNonNull(predicate);
        filteredEvents.setPredicate(predicate);
    }

    //=========== Filtered Reminder List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Event} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Reminder> getFilteredReminderList() {
        return filteredReminders;
    }

    @Override
    public void updateFilteredReminderList(Predicate<Reminder> predicate) {
        requireNonNull(predicate);
        filteredReminders.setPredicate(predicate);
    }


    //=========== Undo/Redo =================================================================================

    @Override
    public boolean canUndoAddressBook() {
        return versionedAddressBook.canUndo();
    }

    @Override
    public boolean canRedoAddressBook() {
        return versionedAddressBook.canRedo();
    }

    @Override
    public void undoAddressBook() {
        versionedAddressBook.undo();
    }

    @Override
    public void redoAddressBook() {
        versionedAddressBook.redo();
    }

    @Override
    public void commitAddressBook() {
        versionedAddressBook.commit();
    }

    //=========== Selected person ===========================================================================

    @Override
    public ReadOnlyProperty<Person> selectedPersonProperty() {
        return selectedPerson;
    }

    @Override
    public Person getSelectedPerson() {
        return selectedPerson.getValue();
    }

    @Override
    public void setSelectedPerson(Person person) {
        if (person != null && !filteredPersons.contains(person)) {
            throw new PersonNotFoundException();
        }
        selectedPerson.setValue(person);
    }

    /**
     * Ensures {@code selectedPerson} is a valid person in {@code filteredPersons}.
     */
    private void ensureSelectedPersonIsValid(ListChangeListener.Change<? extends Person> change) {
        while (change.next()) {
            if (selectedPerson.getValue() == null) {
                // null is always a valid selected person, so we do not need to check that it is valid anymore.
                return;
            }

            boolean wasSelectedPersonReplaced = change.wasReplaced() && change.getAddedSize() == change.getRemovedSize()
                    && change.getRemoved().contains(selectedPerson.getValue());
            if (wasSelectedPersonReplaced) {
                // Update selectedPerson to its new value.
                int index = change.getRemoved().indexOf(selectedPerson.getValue());
                selectedPerson.setValue(change.getAddedSubList().get(index));
                continue;
            }

            boolean wasSelectedPersonRemoved = change.getRemoved().stream()
                    .anyMatch(removedPerson -> selectedPerson.getValue().isSamePerson(removedPerson));
            if (wasSelectedPersonRemoved) {
                // Select the person that came before it in the list,
                // or clear the selection if there is no such person.
                selectedPerson.setValue(change.getFrom() > 0 ? change.getList().get(change.getFrom() - 1) : null);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return versionedAddressBook.equals(other.versionedAddressBook)
                && userPrefs.equals(other.userPrefs)
                && filteredPersons.equals(other.filteredPersons)
                && filteredEvents.equals(other.filteredEvents)
                && Objects.equals(selectedPerson.get(), other.selectedPerson.get())
                && Objects.equals(selectedEvent.get(), other.selectedEvent.get());
    }
    //=========== Selected event ===========================================================================

    @Override
    public ReadOnlyProperty<Event> selectedEventProperty() {
        return selectedEvent;
    }

    @Override
    public Event getSelectedEvent() {
        return selectedEvent.getValue();
    }

    @Override
    public void setSelectedEvent(Event event) {
        if (event != null && !filteredEvents.contains(event)) {
            throw new EventNotFoundException();
        }
        selectedEvent.setValue(event);
    }

    /**
     * Ensures {@code selectedEvent} is a valid event in {@code filteredEvents}.
     */
    private void ensureSelectedEventIsValid(ListChangeListener.Change<? extends Event> change) {
        while (change.next()) {
            if (selectedEvent.getValue() == null) {
                // null is always a valid selected event, so we do not need to check that it is valid anymore.
                return;
            }

            boolean wasSelectedEventReplaced = change.wasReplaced() && change.getAddedSize() == change.getRemovedSize()
                    && change.getRemoved().contains(selectedEvent.getValue());
            if (wasSelectedEventReplaced) {
                // Update selectedEvent to its new value.
                int index = change.getRemoved().indexOf(selectedEvent.getValue());
                selectedEvent.setValue(change.getAddedSubList().get(index));
                continue;
            }

            boolean wasSelectedEventRemoved = change.getRemoved().stream()
                    .anyMatch(removedEvent -> selectedEvent.getValue().isSameEvent(removedEvent));
            if (wasSelectedEventRemoved) {
                // Select the event that came before it in the list,
                // or clear the selection if there is no such event.
                selectedEvent.setValue(change.getFrom() > 0 ? change.getList().get(change.getFrom() - 1) : null);
            }
        }
    }

    //=========== Selected reminder ===========================================================================

    @Override
    public ReadOnlyProperty<Reminder> selectedReminderProperty() {
        return selectedReminder;
    }

    @Override
    public Reminder getSelectedReminder() {
        return selectedReminder.getValue();
    }

    @Override
    public void setSelectedReminder(Reminder reminder) {
        if (reminder != null && !filteredReminders.contains(reminder)) {
            throw new EventNotFoundException();
        }
        selectedReminder.setValue(reminder);
    }

    /**
     * Ensures {@code selectedReminder} is a valid reminder in {@code filteredReminders}.
     */
    private void ensureSelectedReminderIsValid(ListChangeListener.Change<? extends Reminder> change) {
        while (change.next()) {
            if (selectedReminder.getValue() == null) {
                // null is always a valid selected reminder, so we do not need to check that it is valid anymore.
                return;
            }

            boolean wasSelectedReminderReplaced = change.wasReplaced()
                    && change.getAddedSize() == change.getRemovedSize()
                    && change.getRemoved().contains(selectedReminder.getValue());
            if (wasSelectedReminderReplaced) {
                // Update selectedReminder to its new value.
                int index = change.getRemoved().indexOf(selectedReminder.getValue());
                selectedReminder.setValue(change.getAddedSubList().get(index));
                continue;
            }

            boolean wasSelectedReminderRemoved = change.getRemoved().stream()
                    .anyMatch(removedReminder -> selectedReminder.getValue().isSameReminder(removedReminder));
            if (wasSelectedReminderRemoved) {
                // Select the reminder that came before it in the list,
                // or clear the selection if there is no such reminder.
                selectedReminder.setValue(change.getFrom() > 0 ? change.getList().get(change.getFrom() - 1) : null);
            }
        }
    }

}

