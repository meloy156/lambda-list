# Student Query Engine

> A stateless, immutable, stream-based query engine for collections of students — built entirely on the Java Streams API with zero runtime dependencies.

[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk)](https://openjdk.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build](https://github.com/<YOUR_USERNAME>/student-query-engine/actions/workflows/build.yml/badge.svg)](https://github.com/<YOUR_USERNAME>/student-query-engine/actions/workflows/build.yml)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](http://makeapullrequest.com)

---

## Overview

**Student Query Engine** exposes a declarative, functional API for querying collections of students. Instead of imperative loops and mutable accumulators, consumers compose streams of transformations — `filter`, `map`, `sorted`, `collect` — that are evaluated lazily and produce **immutable results**.

The engine is **stateless**. There is no internal storage, no lifecycle, no side effects. Every method receives a `Collection<Student>` and returns a fresh, unmodifiable value.

```java
StudentDB db = new StudentDB();

List<Student> m3232 = db.findStudentsByGroup(students, GroupName.M3232);
List<String> names  = db.getDistinctFirstNames(students);
```

---

## Why?

Working with collections in Java usually degenerates into imperative loops:

```java
// before — imperative, mutable, 8 lines
List<String> names = new ArrayList<>();
for (Student s : students) {
    if (s.groupName() == GroupName.M3232) {
        names.add(s.firstName());
    }
}
Collections.sort(names);
```

This engine replaces it with a single declarative expression:

```java
// after — declarative, immutable, 1 expression
List<String> names = students.stream()
        .filter(s -> s.groupName() == GroupName.M3232)
        .map(Student::firstName)
        .sorted()
        .toList();
```

**Every method in the API is exactly one expression.** No temporary variables, no mutation, no loops.

---

## Features

- **Stateless & thread-safe** — no shared mutable state; safe for concurrent use
- **Fully immutable results** — every returned collection is unmodifiable
- **Single-expression methods** — no imperative loops, no temporary variables
- **Deterministic ordering** — every multi-field comparator is fully specified (ties broken by `id`)
- **Functional core** — built on `java.util.stream` and `java.util.function` only
- **Zero runtime dependencies** — pure JDK, no Guava, no Vavr
- **`O(n)` / `O(n log n)`** — single-pass algorithms wherever possible

---

## Quick Start

```bash
git clone https://github.com/meloy156/student-query-engine.git
cd student-query-engine
javac -cp "lib/*" -d out -encoding UTF-8 src/*.java
```

### Run the test suite

```bash
# Windows
java -cp "out;lib/*" info.kgeorgiy.java.advanced.student.Tester StudentQuery StudentDB

# Linux / macOS
java -cp "out:lib/*" info.kgeorgiy.java.advanced.student.Tester StudentQuery StudentDB
```

Expected output:

```
OK StudentQuery for StudentDB in <N>ms
```

---

## API

### Extraction

| Method | Description |
|---|---|
| `getFirstNames(List<Student>)` | Extract first names in input order |
| `getLastNames(List<Student>)` | Extract last names in input order |
| `getGroupNames(List<Student>)` | Extract group names |
| `getFullNames(List<Student>)` | Concatenate first + last name with a single space |
| `getDistinctFirstNames(List<Student>)` | Unique first names, lexicographically sorted |

### Aggregation

| Method | Description |
|---|---|
| `getMaxStudentFirstName(List<Student>)` | First name of the student with the highest `id` |

### Sorting

| Method | Description |
|---|---|
| `sortStudentsById(Collection<Student>)` | Sort by `id` ascending |
| `sortStudentsByName(Collection<Student>)` | Sort by last name ↓, first name ↓, id ↑ |

### Search

| Method | Description |
|---|---|
| `findStudentsByFirstName(Collection<Student>, String)` | Filter by first name, ordered by name |
| `findStudentsByLastName(Collection<Student>, String)` | Filter by last name, ordered by name |
| `findStudentsByGroup(Collection<Student>, GroupName)` | Filter by group, ordered by name |
| `findStudentNamesByGroup(Collection<Student>, GroupName)` | Map `lastName → minimal firstName` within a group |

---

## Design Notes

### Single-expression contract

Every method body is one expression. Long chains are wrapped across lines but remain a single return:

```java
@Override
public List<Student> findStudentsByGroup(final Collection<Student> students, final GroupName group) {
    return students.stream()
            .filter(s -> s.groupName() == group)
            .sorted(STUDENT_COMPARATOR)
            .toList();
}
```

This is enforced by the project's own test suite — any method with a statement beyond the final `return` fails compilation against the reference contract.

### Canonical ordering

The "ordered by name" semantics are defined once, as a single reusable `Comparator` constant:

```java
private static final Comparator<Student> STUDENT_COMPARATOR =
        Comparator.comparing(Student::lastName, Comparator.reverseOrder())
                .thenComparing(Student::firstName, Comparator.reverseOrder())
                .thenComparingInt(Student::id);
```

Where:

1. **Last name** — descending
2. **First name** — descending
3. **Id** — ascending

Centralising this rule eliminates drift between methods and makes the tie-breaking contract explicit. Every query that needs ordering reuses this constant.

### Duplicate handling

When reducing a group into a `Map<String, String>` (last name → first name), collisions are resolved by a **minimal-first-name** merge function:

```java
.collect(Collectors.toMap(
        Student::lastName,
        Student::firstName,
        BinaryOperator.minBy(Comparator.naturalOrder())
))
```

This keeps the result **deterministic** regardless of insertion order — a requirement, since `HashMap` iteration is unspecified.

### Distinctness

Unique, sorted names are produced without intermediate `distinct()` or `sorted()` calls:

```java
.collect(Collectors.toCollection(TreeSet::new))
```

`TreeSet` is a red-black tree — it enforces **uniqueness** (via `compareTo == 0` on insert) and **order** (`in-order` traversal) in `O(log n)` per insertion, replacing two stream passes with one.

### Immutability

- `Student` and `Group` are `record`s with defensive copies in their compact constructors
- Query results are collected via `Stream.toList()`, which returns an unmodifiable `List`
- No returned collection can be mutated by the caller

---

## Tech Stack

| Component | Version |
|---|---|
| Java | 17+ (records, `Stream.toList()`, `record` patterns) |
| Build | `javac` (no Maven/Gradle required) |
| Tests | JUnit 5 (Jupiter) + `info.kgeorgiy.java.advanced.student` |
| Dependencies | None at runtime |

---

## Project Structure

```
student-query-engine/
├── src/
│   └── StudentDB.java           # the engine
├── lib/
│   ├── junit-jupiter-*.jar
│   ├── info.kgeorgiy.java.advanced.student.jar
│   └── ... (test infrastructure)
├── .gitignore
├── LICENSE
└── README.md
```

---

## Roadmap

- [ ] `GroupQuery` variant — grouped aggregations (`getLargestGroup`, `getLargestGroupFirstName`)
- [ ] `AdvancedQuery` variant — id-based batch lookups
- [ ] Parallel stream mode for large datasets
- [ ] Benchmarks against naive imperative implementations
- [ ] Published javadoc site

---

## License

MIT — see [LICENSE](LICENSE).