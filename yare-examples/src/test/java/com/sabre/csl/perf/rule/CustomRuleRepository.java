package com.sabre.csl.perf.rule;

import com.sabre.oss.yare.core.RulesRepository;
import com.sabre.oss.yare.core.model.Rule;
import com.sabre.oss.yare.serializer.json.RuleToJsonConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomRuleRepository implements RulesRepository {

    String ruleFileLocation = "src/test/resources/rules/rule_%d.json";

    RuleToJsonConverter converter = new RuleToJsonConverter();

    @Override
    public Collection<Rule> get(String uri) {
        List<Rule> rules = new ArrayList<>();
        Instant start = Instant.now();
        for (int i = 1; i <= 10; i++) {
            String filePath = String.format(ruleFileLocation, i);
            try {
                rules.add(converter.unmarshal(new String(Files.readAllBytes(Paths.get(filePath)))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Instant end = Instant.now();
        System.out.println("Rules conversion (File -> Rule) time -> " + Duration.between(start, end).toMillis() + " ms");
        return rules;
    }
}
