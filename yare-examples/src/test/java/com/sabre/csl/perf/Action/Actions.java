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

package com.sabre.csl.perf.Action;

import com.sabre.csl.perf.model.HotelFact;
import com.sabre.csl.perf.model.HotelMapFact;
import com.sabre.csl.perf.model.PreferencePredicates;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Actions {
    public void collect(List<HotelFact> context, HotelFact fact) {
        ///fact.setSelected(true);
        context.add(fact);
    }

    public void collectList(Set<HotelFact> context, List<HotelFact> fact) {
        fact.stream().filter(hotelFact -> hotelFact.getSortOrder() != null).forEach(context::add);
    }

    public boolean validate(List<PreferencePredicates> hotelFacts, List<PreferencePredicates> searchFacts) {
        for (PreferencePredicates hotelFact : hotelFacts) {
            if (searchFacts.contains(hotelFact))
                return true;
        }
        return false;
    }

    // 5000 -- SearchFacts
    //

    public boolean validateMap(HotelMapFact rule, List<HotelFact> searchFacts, Integer sortOrder) {
        AtomicBoolean returnValue = new AtomicBoolean(false);
        searchFacts.forEach(hotelFact -> {
            if (rule.containsKey(hotelFact.getGlobalPropertyId())
                    && rule.get(hotelFact.getGlobalPropertyId()).contains(hotelFact.getStayDates())) {
                hotelFact.setSortOrder(sortOrder);
                returnValue.set(true);
            }
        });
        return returnValue.get();
    }
}
