package com.sabre.csl.perf.testcases;

import com.sabre.csl.perf.model.PreferencePredicates;
import com.sabre.oss.yare.core.model.Rule;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main implements AbstractRuleTest {


    @Test
    public void verifyConversionToSet() {
        List<PreferencePredicates> hotelFacts = getPreferencePredicates(2);
        Set<PreferencePredicates> preferencePredicates = new HashSet<>(hotelFacts);
        System.out.println(hotelFacts.size());

       hotelFacts.forEach(hotelFact -> preferencePredicates.contains(hotelFact));
    }

    @Override
    public Rule getRule(int i) {
        return null;
    }
}
