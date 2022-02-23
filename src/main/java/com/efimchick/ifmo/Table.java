package com.efimchick.ifmo;

import com.efimchick.ifmo.util.CourseResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Table {
    private final List<CourseResult> resultList = new ArrayList<>();
    private static final String STUDENT = "Student";
    private static final String TOTAL = "Total";
    private static final String MARK = "Mark";
    private static final String AVERAGE = "Average";

    public void addCourseResult(CourseResult courseResult) {
        resultList.add(courseResult);
    }

    public void createTable(StringBuilder stringBuilder) {
        stringBuilder.append(STUDENT)
                .append(addSpacesBetweenStringAndChar(STUDENT.length()));
        appendTasksToHead(stringBuilder);
        stringBuilder.append(" | ")
                .append(TOTAL)
                .append(" | ")
                .append(MARK)
                .append(" |")
                .append("\n");
        appendPersonsWithScores(stringBuilder);
        stringBuilder.append(AVERAGE)
                .append(addSpacesBetweenStringAndChar(AVERAGE.length()))
                .append(" | ");
        averageScoresInTask(stringBuilder);
        stringBuilder.append(getFormattedDoubleForAverage(getAverageTotalScoreInTask()))
                .append(" |");
        stringBuilder.append(getSpacesFromString(MARK.length()))
                .append(defineMarks(getAverageTotalScoreInTask()))
                .append(" |");
    }

    public void averageScoresInTask(StringBuilder stringBuilder) {
        AtomicInteger index = new AtomicInteger(0);
        getAverageIntegers()
                .forEach(aDouble ->
                        stringBuilder.append(getSpacesFromString(getDifferenceForAverage(index, aDouble)))
                                .append(getFormattedDoubleForAverage(aDouble))
                                .append(" | "));
    }

    private double getAverageTotalScoreInTask() {
        List<Double> doubleList = getAverageIntegers();
        double amountTasks = getAmountTasks();
        return doubleList.stream()
                .mapToDouble(Double::doubleValue)
                .sum() / amountTasks;
    }

    private List<Double> getAverageIntegers() {
        double amountPersons = getAmountPersons();
        return resultList.stream()
                .flatMap(courseResult1 -> courseResult1.getTaskResults()
                        .entrySet()
                        .stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.summingDouble(value -> (value.getValue() / amountPersons))))
                .entrySet()
                .stream().sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private String getFormattedDoubleForAverage(Double aDouble) {
        return String.format("%.2f", aDouble);
    }

    private int getDifferenceForAverage(AtomicInteger index, Double aDouble) {
        return getTasksLength().get(index.getAndIncrement()) - getFormattedDoubleForAverage(aDouble).length();
    }

    private long getAmountPersons() {
        return resultList.stream()
                .map(CourseResult::getPerson)
                .count();
    }

    private void appendPersonsWithScores(StringBuilder stringBuilder) {
        resultList.stream()
                .map(courseResult ->
                        getPersonToString(courseResult) +
                                getSpacesFromString(getDifferenceBetweenPersonAndChar(courseResult)) +
                                getScoresToString(courseResult) + " | " +
                                getFormattedTotalScore(courseResult) + " |" +
                                getSpacesFromString(MARK.length()) +
                                defineMarks(getTotalScores(courseResult)) + " |"
                )
                .sorted()
                .forEach(str -> stringBuilder.append(str).append("\n"));
    }

    private String defineMarks(double totalScore) {
        if (totalScore > 90.0)
            return "A";
        if (totalScore < 90.0 && totalScore >= 83.0)
            return "B";
        if (totalScore < 83.0 && totalScore >= 75.0)
            return "C";
        if (totalScore < 75.0 && totalScore >= 68.0)
            return "D";
        if (totalScore < 68.0 && totalScore >= 60.0)
            return "E";
        else
            return "F";
    }

    private String getFormattedTotalScore(CourseResult courseResult) {
        return String.format("%.2f", getTotalScores(courseResult));
    }

    private double getTotalScores(CourseResult courseResult) {
        return courseResult.getTaskResults().values()
                .stream()
                .mapToDouble(Integer::intValue)
                .sum() / getAmountTasks();
    }

    private int getAmountTasks() {
        return (int) resultList.stream()
                .flatMap(courseResult -> courseResult
                        .getTaskResults()
                        .keySet()
                        .stream())
                .distinct()
                .count();
    }

    private long getDifferenceBetweenPersonAndChar(CourseResult courseResult) {
        return maxStudentLength() - getPersonToString(courseResult).length();
    }

    private String getPersonToString(CourseResult courseResult) {
        return courseResult.getPerson().getLastName() + " " +
                courseResult.getPerson().getFirstName();
    }

    private String getScoresToString(CourseResult courseResult) {
        StringBuilder builder = new StringBuilder();
        List<Integer> integers = getScoresWithAllTasks(courseResult);
        AtomicInteger index = new AtomicInteger(0);
        integers.forEach(integer ->
                builder.append(" | ")
                        .append(getSpacesFromString(getDifferenceBetweenTaskAndScore(index, integer)))
                        .append(integer));
        return builder.toString();
    }

    private int getDifferenceBetweenTaskAndScore(AtomicInteger index, Integer integer) {
        return getTasksLength().get(index.getAndIncrement()) - String.valueOf(integer).length();
    }

    private List<Integer> getTasksLength() {
        return resultList.stream()
                .map(courseResult -> courseResult.getTaskResults().keySet())
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .mapToInt(String::length)
                .boxed()
                .collect(Collectors.toList());
    }

    private Map<String, Integer> getTasksWithScores(CourseResult courseResult) {
        return courseResult.getTaskResults().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, Integer> getEmptyTasks() {
        return resultList.stream()
                .map(courseResult -> courseResult.getTaskResults().keySet())
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toMap(String::toString, s -> 0));
    }

    private List<Integer> getScoresWithAllTasks(CourseResult courseResult) {
        Map<String, Integer> tasks = getEmptyTasks();
        getTasksWithScores(courseResult).forEach((s, integer) ->
                tasks.computeIfPresent(s, (key, value) -> value + integer));
        return new ArrayList<>(new TreeMap<>(tasks).values());
    }

    private void appendTasksToHead(StringBuilder stringBuilder) {
        resultList.stream()
                .map(courseResult -> courseResult.getTaskResults().keySet())
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .forEach(strings -> stringBuilder.append(" | ").append(strings));
    }

    private String addSpacesBetweenStringAndChar(long stringLength) {
        return getSpacesFromString((maxStudentLength() - stringLength));
    }

    private long maxStudentLength() {
        return resultList.stream()
                .map(courseResult ->
                        courseResult.getPerson().getLastName() + " " + courseResult.getPerson().getFirstName())
                .map(String::length)
                .max(Integer::compareTo)
                .orElse(0);
    }

    private String getSpacesFromString(long value) {
        return " ".repeat((int) value);
    }
}

