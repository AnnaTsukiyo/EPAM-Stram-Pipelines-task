package com.efimchick.ifmo;

import com.efimchick.ifmo.util.CourseResult;
import com.efimchick.ifmo.util.Person;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Collecting {

    private final Map<Predicate<Double>, String> marksMapping = new HashMap<>();

    public Collecting() {
        marksMapping.put((aDouble) -> aDouble > 90.0, "A");
        marksMapping.put((aDouble) -> aDouble < 90.0 && aDouble >= 83.0, "B");
        marksMapping.put((aDouble) -> aDouble < 83.0 && aDouble >= 75.0, "C");
        marksMapping.put((aDouble) -> aDouble < 75.0 && aDouble >= 68.0, "D");
        marksMapping.put((aDouble) -> aDouble < 68.0 && aDouble >= 60.0, "E");
        marksMapping.put((aDouble) -> aDouble < 60.0, "F");
    }

    public int sum(IntStream stream) {
        return stream.sum();
    }

    public int production(IntStream stream) {
        return stream
                .reduce((left, right) -> left * right)
                .orElse(0);
    }

    public int oddSum(IntStream stream) {
        return stream
                .filter(value -> value % 2 != 0)
                .sum();
    }

    public Map<Integer, Integer> sumByRemainder(int i, IntStream stream) {
        return stream
                .boxed()
                .collect(Collectors.groupingBy(integer -> Math.floorMod(integer, i),
                        Collectors.summingInt(Integer::intValue)));
    }

    public Map<Person, Double> totalScores(Stream<CourseResult> courseResult) {
        List<CourseResult> courseResultList = courseResult.collect(Collectors.toList());
        List<Double> averageValues = getAverageValues(courseResultList);
        AtomicInteger index = new AtomicInteger(0);
        return courseResultList.stream()
                .collect(Collectors.toMap(CourseResult::getPerson,
                        course -> averageValues.get(index.getAndIncrement())));
    }

    private List<Double> getAverageValues(List<CourseResult> courseResultList) {
        double amountTasks = getAmountTasks(courseResultList);
        return courseResultList.stream()
                .map(courseResult1 -> courseResult1.getTaskResults().values()
                        .stream()
                        .mapToInt(Integer::intValue)
                        .sum())
                .map(integer -> integer / amountTasks)
                .collect(Collectors.toList());
    }

    public double averageTotalScore(Stream<CourseResult> courseResult) {
        List<CourseResult> courseResultList = courseResult.collect(Collectors.toList());
        double amountTasks = getAmountTasks(courseResultList);
        int totalSumScores = getTotalSumScores(courseResultList);
        return totalSumScores / (getAmountPersons(courseResultList) * amountTasks);
    }

    private int getTotalSumScores(List<CourseResult> courseResults) {
        return courseResults.stream()
                .map(courseResult -> courseResult.getTaskResults().values())
                .flatMap(Collection::stream)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private int getAmountTasks(List<CourseResult> courseResults) {
        return (int) courseResults.stream()
                .flatMap(courseResult -> courseResult
                        .getTaskResults()
                        .keySet()
                        .stream())
                .distinct()
                .count();
    }

    public Map<String, Double> averageScoresPerTask(Stream<CourseResult> courseResult) {
        List<CourseResult> courseResultList = courseResult.collect(Collectors.toList());
        double amountPersons = getAmountPersons(courseResultList);
        return courseResultList.stream()
                .flatMap(courseResult1 -> courseResult1.getTaskResults()
                        .entrySet()
                        .stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.summingDouble(value -> (value.getValue() / amountPersons))));
    }

    private long getAmountPersons(List<CourseResult> courseResultList) {
        return courseResultList.stream()
                .map(CourseResult::getPerson)
                .count();
    }

    public Map<Person, String> defineMarks(Stream<CourseResult> courseResult) {
        Map<Person, Double> map = totalScores(courseResult);
        Map<Person, String> stringMap = new HashMap<>();
        map.forEach((person, aDouble) -> stringMap.put(person, getMark(aDouble)));
        return stringMap;
    }

    private String getMark(Double aDouble) {
        return marksMapping.entrySet().stream()
                .filter(entry -> entry.getKey().test(aDouble))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
    }

    public String easiestTask(Stream<CourseResult> courseResult) {
        Map<String, Double> map = averageScoresPerTask(courseResult);
        return map.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .stream()
                .collect(Collectors.joining());
    }

    public Collector<CourseResult, Table, String> printableStringCollector() {
        return Collector.of(
                Table::new,
                Table::addCourseResult,
                (table, table2) -> {
                    throw new UnsupportedOperationException("Cannot be performed in parallel");
                },
                table -> {
                    StringBuilder builder = new StringBuilder();
                    table.createTable(builder);
                    return builder.toString();
                }
        );
    }
}