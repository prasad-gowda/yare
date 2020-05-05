/*
 * MIT License
 *
 * Copyright 2018 Sabre GLBL Inc.
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

package com.sabre.csl.perf.testcases;

import com.google.common.collect.Lists;
import com.sabre.csl.perf.Action.Actions;
import com.sabre.csl.perf.model.HotelFact;
import com.sabre.csl.perf.model.HotelMapFact;
import com.sabre.csl.perf.model.StayDates;
import com.sabre.csl.perf.model.Wrapper;
import com.sabre.oss.yare.core.RuleSession;
import com.sabre.oss.yare.core.RulesEngine;
import com.sabre.oss.yare.core.RulesEngineBuilder;
import com.sabre.oss.yare.core.model.Rule;
import com.sabre.oss.yare.serializer.json.RuleToJsonConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sabre.oss.yare.invoker.java.MethodCallMetadata.method;

public class MapBasedSolution implements AbstractRuleTest {


    RuleSession session = null;

    boolean shouldWriteJsonToFile = true;

    RuleToJsonConverter converter = new RuleToJsonConverter();

    @BeforeEach
    public void setUp() {
        Instant start = Instant.now();
        List<Rule> rule = getRules(numberOfRules);
        Instant end = Instant.now();
        System.out.println("Time Taken for unmarshal -> " + Duration.between(start, end).toMillis() + " ms");
        if (shouldWriteJsonToFile)
            writeRuleJsonToFile(rule);

        RulesEngine engine = new RulesEngineBuilder()
                .withRulesRepository(i -> rule)
                .withActionMapping("collectList", method(new Actions(), (action) -> action.collectList(null, null)))
                .withFunctionMapping("validateMap", method(new Actions(), (actions -> actions.validateMap(null, null, null))))
                .build();

        session = engine.createSession("hotels");
    }

    @Test
    public void parallelRuleExecution() throws InterruptedException {
        List<Long> averageTime = new ArrayList<>();
        Instant start = Instant.now();
        List<HotelFact> facts = getHotelFacts();
        List<List<HotelFact>> partitions = Lists.partition(facts, 1000);
        ExecutorService executorService = Executors.newFixedThreadPool(partitions.size());
        for (List<HotelFact> hotelFacts : partitions)
            executorService.execute(() -> checkProperty(hotelFacts,averageTime));
        executorService.shutdown();
        executorService.awaitTermination(120, TimeUnit.SECONDS);
        Instant end = Instant.now();
        System.out.println("End  -> " + Duration.between(start, end).toMillis() + " ms");
    }

    @Test
    public void sequentialRuleExecution() throws InterruptedException {
        List<HotelFact> facts = getHotelFacts();
        List<Long> averageTime = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            checkProperty(facts, averageTime);
        LongSummaryStatistics longSummaryStatistics = averageTime.stream().mapToLong(Long::longValue).summaryStatistics();
        System.out.println("Average time taken -> " + longSummaryStatistics.getAverage());
    }

    private void checkProperty(List<HotelFact> facts, List<Long> averageTime) {
        Wrapper wrapper = new Wrapper();
        wrapper.setHotelFacts(facts);

        Instant start = Instant.now();
        Set<HotelFact> result = session.execute(new HashSet<>(), Collections.singleton(wrapper));
        Instant endTime = Instant.now();

        averageTime.add(Duration.between(start, endTime).toMillis());
        Set<Integer> propertyId = facts.stream().map(HotelFact::getGlobalPropertyId).collect(Collectors.toSet());
        long count = result.stream().map(HotelFact::getGlobalPropertyId).filter(id -> !propertyId.contains(id)).count();

        System.out.println("Missing property count in result -> " + count);
        System.out.println("Time taken to execute -> " + Duration.between(start, endTime).toMillis() + "ms");
        System.out.println("Total found property ->" + result.size());
    }

    private Map<Integer, List<StayDates>> getHotelMapFact(int sortOrder) {
        Map<Integer, List<StayDates>> rule = new HotelMapFact();
        if (sortOrder == 10) sortOrder = 0;
        for (int i = 100000; i < 100000 + totalProperty; i++) {
            if (validate.apply(i, sortOrder)) {
                StayDates dates = new StayDates();
                dates.setStayStartDate(getDates(i));
                dates.setStayEndDate(getDates(i + 7));
                rule.put(i, Collections.singletonList(dates));
            }
        }
        return rule;
    }

    //  5000 -> Rule 1   // Property Matching
    // 5000 -> Rule 2

    // 1 -> Rule 1  -> Action --> SetOrder 1
    // 1 -> Rule 2
    // 1 -> Rule 10
    // 2 -> Rule 1
    @Override
    public Rule getRule(int i) {

        String filePath = String.format(ruleFileLocation, i);
        try {
            String s = new String(Files.readAllBytes(Paths.get(filePath)));
            return converter.unmarshal(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
        /*return RuleDsl.ruleBuilder()
                .name("Rule for sort order " + i)
                .attribute("active", true)
                .fact("wrapper", Wrapper.class)
                .predicate(
                        isTrue(function("validateMap", Boolean.class,
                                param("hotelFactsMap", value(getHotelMapFact(i))), // JSON
                                param("searchFact", value("${wrapper.hotelFacts}")), //DYNAMIC
                                param("sortOrder", value(i)))  //JSON
                        )
                )
                .action("collectList",
                        param("context", value("${ctx}")),
                        param("fact", value("${wrapper.hotelFacts}")))
                .build();*/
    }
}

