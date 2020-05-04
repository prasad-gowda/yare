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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.sabre.oss.yare.dsl.RuleDsl.*;
import static com.sabre.oss.yare.invoker.java.MethodCallMetadata.method;

public class CustomFunctionWithMapRuleTest implements AbstractRuleTest {

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
                .withFunctionMapping("validateMap", method(new Actions(), (actions -> actions.validateMap(null, null))))
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
                .fact("hotelFact", HotelFact.class)
                .predicate(
                        isTrue(function("validateMap", Boolean.class,
                                param("hotelFacts", value("${hotelFact.propertyContracts}")),
                                param("searchFacts", value(getPreferencePredicatesMap(i))))
                        )
                )
                .action("collect",
                        param("context", value("${ctx}")),
                        param("fact", value("${hotelFact}")))
                .build();
    }

    public Map<String, PreferencePredicates> getPreferencePredicatesMap(int sortOrder) {
        Map<String, PreferencePredicates> preferencePredicatesList = new HashMap<>();
        for (int propertyId = 100000; propertyId <= 100000 + totalProperty; propertyId++) {
            if (validate.apply(propertyId, sortOrder)) {
                PreferencePredicates preferencePredicates = new PreferencePredicates();
                preferencePredicates.setId(String.valueOf(propertyId));
                preferencePredicates.setStartDate(getDates(propertyId));
                preferencePredicates.setEndDate(getDates(propertyId + 7));
                preferencePredicatesList.put(String.valueOf(propertyId), preferencePredicates);
            }
        }
        return preferencePredicatesList;
    }
}
