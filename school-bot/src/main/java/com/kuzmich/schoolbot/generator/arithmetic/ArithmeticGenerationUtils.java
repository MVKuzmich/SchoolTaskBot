package com.kuzmich.schoolbot.generator.arithmetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;

import com.kuzmich.schoolbot.generator.Task;

/**
 * Вспомогательные алгоритмы для генерации арифметических заданий.
 * <p>
 * Общая идея:
 * <ul>
 *     <li>Сначала перечисляем конечный домен всех допустимых примеров (кандидатов).</li>
 *     <li>Затем последовательно применяем уровни "ослабления" правил (L0, L1, ...).</li>
 *     <li>На каждом уровне выбираем примеры без повторов, перемешивая пул.</li>
 * </ul>
 * Такой подход позволяет:
 * <ul>
 *     <li>избежать бесконечных циклов при строгих правилах отбора (в отличие от do-while с random);</li>
 *     <li>точно контролировать, сколько "красивых" примеров мы можем получить;</li>
 *     <li>переиспользовать один и тот же алгоритм для разных типов генераторов.</li>
 * </ul>
 */
public final class ArithmeticGenerationUtils {

    private ArithmeticGenerationUtils() {
        // utility class
    }

    /**
     * Один кандидат арифметического примера.
     *
     * @param a      первый операнд
     * @param b      второй операнд
     * @param answer ответ (a op b)
     * @param key    уникальный ключ для предотвращения дублей (например, "a-b" или "a+b")
     */
    public record Candidate(int a, int b, int answer, String key) {
    }

    /**
     * Генерация заданий из заранее перечисленного домена кандидатов с многоуровневым ослаблением правил.
     * Без ослабления эвристик и без повторов (для арифметики).
     */
    public static List<Task> generateWithRelaxation(
            List<Candidate> domain,
            int quantity,
            List<Predicate<Candidate>> levels,
            ThreadLocalRandom rnd,
            Function<Candidate, Task> toTask
    ) {
        return generateWithRelaxation(domain, quantity, levels, rnd, toTask, false, false);
    }

    /**
     * Генерация заданий из домена с многоуровневым ослаблением правил.
     *
     * @param domain                    полный список всех допустимых примеров
     * @param quantity                  требуемое количество заданий
     * @param levels                    уровни правил от самого строгого к более мягким
     * @param rnd                       источник случайных чисел
     * @param toTask                    преобразование кандидата в {@link Task}
     * @param relaxedSequenceHeuristics если true, запрещаем только два одинаковых задания подряд (по key);
     *                                  иначе дополнительно запрещаем одинаковый первый операнд и ответ подряд
     * @param allowRepeats               если true, при нехватке уникальных кандидатов добиваем до quantity повторами
     * @return список заданий; при allowRepeats всегда длины quantity (если domain не пуст)
     */
    public static List<Task> generateWithRelaxation(
            List<Candidate> domain,
            int quantity,
            List<Predicate<Candidate>> levels,
            ThreadLocalRandom rnd,
            Function<Candidate, Task> toTask,
            boolean relaxedSequenceHeuristics,
            boolean allowRepeats
    ) {
        if (quantity <= 0) {
            return List.of();
        }
        if (domain.isEmpty() || levels.isEmpty()) {
            return List.of();
        }

        List<Task> result = new ArrayList<>(quantity);
        Set<String> usedKeys = new HashSet<>();
        Candidate lastCandidate = null;
        Map<Integer, Integer> countA = new HashMap<>();
        Map<Integer, Integer> countAnswer = new HashMap<>();

        for (int levelIndex = 0; levelIndex < levels.size(); levelIndex++) {
            Predicate<Candidate> level = levels.get(levelIndex);
            if (result.size() >= quantity) {
                break;
            }

            List<Candidate> pool = new ArrayList<>();
            Set<Integer> distinctA = new HashSet<>();
            Set<Integer> distinctAnswers = new HashSet<>();
            for (Candidate c : domain) {
                if (usedKeys.contains(c.key())) {
                    continue;
                }
                if (!level.test(c)) {
                    continue;
                }
                pool.add(c);
                distinctA.add(c.a());
                distinctAnswers.add(c.answer());
            }

            if (pool.isEmpty()) {
                continue;
            }

            Collections.shuffle(pool, rnd);

            int capA = computeCap(quantity, distinctA.size());
            int capAnswer = computeCap(quantity, distinctAnswers.size());

            for (Candidate candidate : pool) {
                if (result.size() >= quantity) {
                    break;
                }
                if (violatesSequenceHeuristics(lastCandidate, candidate, relaxedSequenceHeuristics)) {
                    continue;
                }
                if (!passesBalance(candidate, countA, countAnswer, capA, capAnswer)) {
                    continue;
                }
                result.add(toTask.apply(candidate));
                usedKeys.add(candidate.key());
                lastCandidate = candidate;
                increment(countA, candidate.a());
                increment(countAnswer, candidate.answer());
            }

            if (result.size() >= quantity) {
                continue;
            }

            for (Candidate candidate : pool) {
                if (result.size() >= quantity) {
                    break;
                }
                if (usedKeys.contains(candidate.key())) {
                    continue;
                }
                if (violatesSequenceHeuristics(lastCandidate, candidate, relaxedSequenceHeuristics)) {
                    continue;
                }
                result.add(toTask.apply(candidate));
                usedKeys.add(candidate.key());
                lastCandidate = candidate;
                increment(countA, candidate.a());
                increment(countAnswer, candidate.answer());
            }
        }

        // Для состава числа: при нехватке уникальных заданий добиваем повторами из домена
        if (allowRepeats && result.size() < quantity && !domain.isEmpty()) {
            while (result.size() < quantity) {
                Candidate c = domain.get(rnd.nextInt(domain.size()));
                result.add(toTask.apply(c));
            }
        }

        return result;
    }

    /**
     * Эвристики последовательности: когда {@code relaxed == false} — запрещаем подряд одинаковый ключ,
     * одинаковый первый операнд (a) и одинаковый ответ; когда {@code relaxed == true} — только одинаковый ключ
     * (для состава числа, где a всегда одно и то же).
     */
    private static boolean violatesSequenceHeuristics(Candidate last, Candidate current, boolean relaxed) {
        if (last == null) {
            return false;
        }
        if (last.key().equals(current.key())) {
            return true;
        }
        if (relaxed) {
            return false;
        }
        if (last.a() == current.a()) {
            return true;
        }
        return last.answer() == current.answer();
    }

    private static int computeCap(int quantity, int distinctValues) {
        if (distinctValues <= 0) {
            return quantity;
        }
        double base = Math.ceil(quantity / (double) distinctValues);
        int cap = (int) base + 1; // небольшой "зазор", чтобы не было слишком жёстко
        return Math.max(2, cap);
    }

    private static boolean passesBalance(
            Candidate candidate,
            Map<Integer, Integer> countA,
            Map<Integer, Integer> countAnswer,
            int capA,
            int capAnswer
    ) {
        int aCount = countA.getOrDefault(candidate.a(), 0);
        if (aCount >= capA) {
            return false;
        }
        int answerCount = countAnswer.getOrDefault(candidate.answer(), 0);
        return answerCount < capAnswer;
    }

    private static void increment(Map<Integer, Integer> counter, int key) {
        counter.merge(key, 1, Integer::sum);
    }
}
