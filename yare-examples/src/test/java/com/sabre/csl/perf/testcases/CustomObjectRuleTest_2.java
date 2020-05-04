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
import com.sabre.csl.perf.model.Wrapper;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.sabre.oss.yare.dsl.RuleDsl.*;
import static com.sabre.oss.yare.invoker.java.MethodCallMetadata.method;

public class CustomObjectRuleTest_2 implements AbstractRuleTest {

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
        List<HotelFact> facts = getHotelFacts();
        List<List<HotelFact>> partitions = Lists.partition(facts, 1000);
        ExecutorService executorService = Executors.newFixedThreadPool(partitions.size());
        for (List<HotelFact> hotelFacts : partitions)
            executorService.execute(() -> checkProperty(hotelFacts));
        executorService.shutdown();
        executorService.awaitTermination(120, TimeUnit.SECONDS);
        Instant end = Instant.now();
        System.out.println("End  -> " + Duration.between(start, end).toMillis() + " ms");
    }

    @Test
    public void sequentialRuleExecution() throws InterruptedException {
        List<HotelFact> facts = getHotelFacts();
        checkProperty(facts);

    }

    public void checkProperty(List<HotelFact> facts) {
        Wrapper wrapper = new Wrapper();
        wrapper.setHotelFacts(facts);

        Instant start = Instant.now();
        ArrayList<HotelFact> result = session.execute(new ArrayList<HotelFact>(), facts);
        Instant endTime = Instant.now();
        System.out.println("Time taken to execute -> " + Duration.between(start, endTime).toMillis() + "ms");
        System.out.println(result.size());

    }

    public Rule getRule(int i) {
        return RuleDsl.ruleBuilder()
                .name("Rule for sort order " + i)
                .attribute("active", true)
                .fact("wrapper", Wrapper.class)
                .predicate(
                        and(
                                contains(
                                        RuleDsl.values(PreferencePredicates.class, getPreferencePredicates(i)),
                                        value("${wrapper.hotelFacts.propertyContracts[*]}")))
                )
                .action("collect",
                        param("context", value("${ctx}")),
                        param("fact", value("${wrapper.hotelFacts}")))
                .build();
    }

}




