package com.sabre.csl.perf.Action;

import com.sabre.csl.perf.model.HotelFact;
import com.sabre.csl.perf.model.PreferencePredicates;

import java.util.List;
import java.util.Map;

public class Actions {
    public void collect(List<HotelFact> context, HotelFact fact) {
        context.add(fact);
    }

    public boolean validate(List<PreferencePredicates> hotelFacts, List<PreferencePredicates> searchFacts) {
        for (PreferencePredicates hotelFact : hotelFacts) {
            if (searchFacts.contains(hotelFact))
                return true;
        }
        return false;
    }


    public boolean validateMap(List<PreferencePredicates> hotelFacts, Map<String, PreferencePredicates> searchFacts) {
        for (PreferencePredicates hotelFact : hotelFacts) {
            if (searchFacts.containsKey(hotelFact.getId()) && searchFacts.get(hotelFact.getId()).equals(hotelFact))
                return true;
        }
        return false;
    }
}
