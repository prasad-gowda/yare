package com.sabre.csl.perf.testcases;

import com.sabre.csl.perf.model.HotelFact;
import com.sabre.csl.perf.model.PreferencePredicates;
import com.sabre.oss.yare.core.model.Rule;
import com.sabre.oss.yare.serializer.json.RuleToJsonConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static java.nio.file.Files.write;

public interface AbstractRuleTest {
    String ruleFileLocation = "C:\\Users\\SG0308137\\Downloads\\rule.json";

    int totalProperty = 150000;

    int numberOfRules = 10;

    BiFunction<Integer, Integer, Boolean> validate =
            (Integer propertyId, Integer sortOrder) -> String.valueOf(propertyId).endsWith(String.valueOf(sortOrder));

    default List<Rule> getRules(int numberOfRules) {
        List<Rule> rules = new ArrayList<>();
        if (numberOfRules > 10) numberOfRules = 10;
        for (int i = 1; i <= numberOfRules; i++) {
            rules.add(getRule(i));
        }
        return rules;
    }

    Rule getRule(int i);

    default List<PreferencePredicates> getPreferencePredicates(int sortOrder) {
        List<PreferencePredicates> preferencePredicatesList = new ArrayList<>();
        for (int propertyId = 100000; propertyId <= 100000 + totalProperty; propertyId++) {
            if (validate.apply(propertyId, sortOrder)) {
                PreferencePredicates preferencePredicates = new PreferencePredicates();
                preferencePredicates.setId(String.valueOf(propertyId));
                preferencePredicates.setStartDate(getDates(propertyId));
                preferencePredicates.setEndDate(getDates(propertyId + 7));
                preferencePredicatesList.add(preferencePredicates);
            }
        }
        return preferencePredicatesList;
    }

    default List<HotelFact> getHotelFacts() {
        List<HotelFact> facts = new ArrayList<>();
        for (int count = 0; count <= 5000 && count <= totalProperty; count++) {
            PreferencePredicates preferencePredicates = new PreferencePredicates();
            preferencePredicates.setId(String.valueOf(100000 + count));
            preferencePredicates.setStartDate(getDates(100000 + count));
            preferencePredicates.setEndDate(getDates(100000 + count + 7));
            HotelFact hotelFact = new HotelFact();
            hotelFact.setPropertyContracts(Collections.singletonList(preferencePredicates));
            facts.add(hotelFact);
        }
        return facts;
    }

    default void writeRuleJsonToFile(List<Rule> rules) {
        RuleToJsonConverter ruleToJsonConverter = new RuleToJsonConverter();
        try {
            Files.delete(Paths.get(ruleFileLocation));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Build Rule JSON array in string format
        rules.forEach(rule -> {
            try {
                write(Paths.get(ruleFileLocation), ruleToJsonConverter.marshal(rule).concat(",").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println("Rule Write is complete");
    }

    default ZonedDateTime getDates(int addDays) {
        return ZonedDateTime.ofInstant(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()).plusDays(addDays);
    }
}