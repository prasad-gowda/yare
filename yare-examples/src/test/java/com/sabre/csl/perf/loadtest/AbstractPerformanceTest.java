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

package com.sabre.csl.perf.loadtest;

import com.sabre.csl.perf.model.HotelFact;
import com.sabre.csl.perf.model.Wrapper;
import com.sabre.oss.yare.core.RuleSession;
import com.sabre.oss.yare.core.RulesEngine;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractPerformanceTest {

    static {
        prepareResultFolder();
    }

    private static void prepareResultFolder() {
        try {
            Path path = Paths.get("benchmarks");
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            System.out.println("Error during preparations to tests: " + e);
        }
    }

    @Test
    public void runBenchmarks() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(this.getClass().getSimpleName())
                .mode(Mode.AverageTime)
                .timeUnit(MILLISECONDS)
                .warmupIterations(2)
                .warmupTime(TimeValue.seconds(2))
                .measurementIterations(10)
                .measurementTime(TimeValue.seconds(2))
                .threads(1)
                .warmupForks(0)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .result("benchmarks/performance-results.csv")
                .resultFormat(ResultFormatType.CSV)
                .jvmArgs("-server", "-Xms2048M", "-Xmx2048M", "-XX:+UseG1GC")
                .build();
        new Runner(opt).run();
    }

    void benchmarkTest(Context benchmarkContext) {
        //given
        RulesEngine rulesEngine = benchmarkContext.rulesEngine;
        RuleSession ruleSession = rulesEngine.createSession("hotels");
        //when
        Set<HotelFact> result = ruleSession.execute(new HashSet<>(), benchmarkContext.facts);
        //then
        int expectedResultSize = benchmarkContext.numberOfFacts;
        assertThat(result.size()).isEqualTo(expectedResultSize);
    }

    @State(Scope.Benchmark)
    public abstract static class Context {

        @Param({"10", "50", "250", "1000", "2500", "5000"})
        int numberOfFacts;

        List<Wrapper> facts;

        static RulesEngine rulesEngine;

        public static void setRulesEngine(RulesEngine rulesEngineInput) {
            rulesEngine = rulesEngineInput;
        }

        protected void setup() {
            facts = getFacts();
        }

        protected abstract List<Wrapper> getFacts();

    }

}
