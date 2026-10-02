import java.util.*;
import java.util.stream.Collectors;
import info.kgeorgiy.java.advanced.student.*;
import org.jsoup.select.Collector;

public class StudentDB implements GroupQuery {

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::firstName)
                .toList();
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return students.stream()
                .map(Student::lastName)
                .toList();
    }

    @Override
    public List<GroupName> getGroupNames(List<Student> students) {
        return students.stream()
                .map(Student::groupName)
                .toList();
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return students.stream()
                .map(s -> s.firstName() + " " + s.lastName())
                .toList();
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::firstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Student::compareTo)
                .map(Student::firstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream()
                .sorted(Student::compareTo)
                .toList();
    }


    private static final Comparator<Student> STUDENT_COMPARATOR = Comparator
            .comparing(Student::lastName, Comparator.reverseOrder())
            .thenComparing(Student::firstName, Comparator.reverseOrder())
            .thenComparing(Student::id);


    @Override
    public List<Student> sortStudentsByName(final Collection<Student> students) {
        return students
                .stream()
                .sorted(STUDENT_COMPARATOR)
                .toList();

    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return students.stream()
                .filter(s -> name.equals(s.firstName()))
                .sorted(STUDENT_COMPARATOR)
                .toList();
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return students
                .stream()
                .filter(d -> name.equals(d.lastName()))
                .sorted(STUDENT_COMPARATOR)
                .toList();
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return students.stream()
                .filter((Student s) -> s.groupName() == group)
                .sorted(STUDENT_COMPARATOR)
                .toList();
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students.stream()
                .filter((Student s) -> s.groupName() == group)
                .collect(Collectors.toMap(
                        Student::lastName,
                        Student::firstName,
                        (old, next) -> old.compareTo(next) <= 0 ? old : next));
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> collection) {
        return collection.stream()
                .collect(Collectors.groupingBy(
                        Student::groupName,
                        TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream().sorted(STUDENT_COMPARATOR).toList()
                        )
                ))
                .entrySet().stream()
                .map(e -> new Group(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> collection) {
        return collection.stream()
                .collect(Collectors.groupingBy(
                        Student::groupName,
                        TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream().sorted(Student::compareTo).toList()
                        )
                ))
                .entrySet().stream()
                .map(r -> new Group(r.getKey(), r.getValue()))
                .toList();
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> collection) {
        return collection.stream()
                .collect(Collectors.groupingBy(Student::groupName, Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.<GroupName, Long>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public GroupName getLargestGroupFirstName(final Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(
                        Student::groupName,
                        Collectors.mapping(Student::firstName, Collectors.toSet())
                ))
                .entrySet().stream()
                .max(Map.Entry.<GroupName, Set<String>>comparingByValue(
                                Comparator.comparingInt(Set::size))
                        .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}