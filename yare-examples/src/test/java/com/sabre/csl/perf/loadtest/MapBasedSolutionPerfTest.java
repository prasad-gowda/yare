package com.sabre.csl.perf.loadtest;

import com.sabre.csl.perf.Action.Actions;
import com.sabre.csl.perf.model.HotelFact;
import com.sabre.csl.perf.model.PreferencePredicates;
import com.sabre.csl.perf.model.StayDates;
import com.sabre.csl.perf.model.Wrapper;
import com.sabre.csl.perf.rule.CustomRuleRepository;
import com.sabre.oss.yare.core.RulesEngineBuilder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sabre.oss.yare.invoker.java.MethodCallMetadata.method;

public class MapBasedSolutionPerfTest extends AbstractPerformanceTest {

    public MapBasedSolutionPerfTest() {
        Context.setRulesEngine(new RulesEngineBuilder()
                .withRulesRepository(new CustomRuleRepository())
                .withActionMapping("collectList", method(new Actions(), (action) -> action.collectList(null, null)))
                .withFunctionMapping("validateMap", method(new Actions(), (actions -> actions.validateMap(null, null, null))))
                .build());
    }

    @Benchmark
    public void benchmarkTest(Context benchmarkContext) {
        super.benchmarkTest(benchmarkContext);
    }

    @State(Scope.Benchmark)
    public static class Context extends AbstractPerformanceTest.Context {

        @Setup
        public void setup() {
            super.setup();
        }

        @Override
        protected List<Wrapper> getFacts() {
            List<HotelFact> facts = new ArrayList<>();
            for (int count = 0; count < numberOfFacts; count++) {
                PreferencePredicates preferencePredicates = new PreferencePredicates();

                preferencePredicates.setId(String.valueOf(100000 + count));
                preferencePredicates.setStartDate(getDates(100000 + count));
                preferencePredicates.setEndDate(getDates(100000 + count + 7));

                StayDates stayDates = new StayDates();
                stayDates.setStayStartDate(getDates(100000 + count));
                stayDates.setStayEndDate(getDates(100000 + count + 7));

                HotelFact hotelFact = new HotelFact();
                hotelFact.setPropertyContracts(Collections.singletonList(preferencePredicates));
                hotelFact.setStayDates(stayDates);
                hotelFact.setGlobalPropertyId(100000 + count);
                facts.add(hotelFact);
            }
            Wrapper wrapper = new Wrapper();
            wrapper.setHotelFacts(facts);
            return Collections.singletonList(wrapper);
        }

    }

    private static ZonedDateTime getDates(int addDays) {
        return ZonedDateTime.ofInstant(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()).plusDays(addDays);
    }
}
