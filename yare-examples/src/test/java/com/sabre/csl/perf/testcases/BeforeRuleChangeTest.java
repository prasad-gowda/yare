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
import com.sabre.csl.perf.model.PreferencePredicates;
import com.sabre.oss.yare.core.RuleSession;
import com.sabre.oss.yare.core.RulesEngine;
import com.sabre.oss.yare.core.RulesEngineBuilder;
import com.sabre.oss.yare.core.model.Rule;
import com.sabre.oss.yare.dsl.RuleDsl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.sabre.oss.yare.dsl.RuleDsl.*;
import static com.sabre.oss.yare.invoker.java.MethodCallMetadata.method;

public class BeforeRuleChangeTest implements AbstractRuleTest {

    RuleSession session = null;

    boolean shouldWriteJsonToFile = false;

    @BeforeEach
    public void setUp() {
        List<Rule> rule = getRules(numberOfRules);

        if (shouldWriteJsonToFile)
            writeRuleJsonToFile(rule);

        RulesEngine engine = new RulesEngineBuilder()
                .withRulesRepository(i -> rule)
                .withActionMapping("collect", method(new Actions(), (action) -> action.collect(null, null)))
                .build();

        session = engine.createSession("hotels");
    }

    @Test
    public void parallelRuleExecution() throws InterruptedException {
        Instant start = Instant.now();
        List<Long> averageTime = new ArrayList<>();
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
        System.out.println("Average time taken -> " +longSummaryStatistics.getAverage());
    }

    public void checkProperty(List<HotelFact> facts, List<Long> averageTime) {
        Instant start = Instant.now();
        ArrayList<HotelFact> result = session.execute(new ArrayList<HotelFact>(), facts);
        Instant endTime = Instant.now();
        averageTime.add(Duration.between(start, endTime).toMillis());
        System.out.println("Time taken to execute -> " + Duration.between(start, endTime).toMillis() + "ms");
        System.out.println(result.size());

    }

    public Rule getRule(int i) {
        return RuleDsl.ruleBuilder()
                .name("Rule for sort order " + i)
                .attribute("active", true)
                .fact("hotelFact", HotelFact.class)
                .predicate(
                        and(
                                containsAny(
                                        RuleDsl.values(Integer.class, getGlobalProperties(i)),
                                        castToCollection(value("${hotelFact.globalPropertyIdList}"), Integer.class)))
                )
                .action("collect",
                        param("context", value("${ctx}")),
                        param("fact", value("${hotelFact}")))
                .build();
    }


    public List<Integer> getGlobalProperties(int sortOrder) {
        List<Integer> preferencePredicatesList = new ArrayList<>();
        for (int propertyId = 100000; propertyId <= 100000 + totalProperty; propertyId++) {
            if (validate.apply(propertyId, sortOrder)) {
                preferencePredicatesList.add(propertyId);
            }
        }
        return preferencePredicatesList;
    }

    @Override
    public List<HotelFact> getHotelFacts() {
        List<HotelFact> facts = new ArrayList<>();
        for (int count = 0; count <= 5000 && count <= totalProperty; count++) {
            HotelFact hotelFact = new HotelFact();
            hotelFact.setGlobalPropertyIdList(Collections.singletonList(100000 + count));
            facts.add(hotelFact);
        }
        return facts;
    }
}
