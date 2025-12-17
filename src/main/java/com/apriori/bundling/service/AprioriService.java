package com.apriori.bundling.service;

import com.apriori.bundling.model.RuleResult;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AprioriService {

    // ===== PARAMETER APRIORI (BISA DIUBAH) =====
    private static final double MIN_SUPPORT = 0.10;
    private static final double MIN_CONFIDENCE = 0.60;

    /* =====================================================
       1. FREQUENT ITEMSETS (UNTUK TABEL SUPPORT)
       ===================================================== */
    public List<Map<String, Object>> frequentItemsets(List<List<String>> transactions) {

        int totalTx = transactions.size();
        Map<Set<String>, Integer> countMap = new HashMap<>();

        for (List<String> tx : transactions) {
            Set<String> unique = new HashSet<>(tx);

            // 1-itemset
            for (String item : unique) {
                countMap.merge(Set.of(item), 1, Integer::sum);
            }

            // 2-itemset
            List<String> list = new ArrayList<>(unique);
            for (int i = 0; i < list.size(); i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    countMap.merge(
                            Set.of(list.get(i), list.get(j)),
                            1,
                            Integer::sum
                    );
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Set<String> itemset : countMap.keySet()) {
            double support = countMap.get(itemset) / (double) totalTx;
            if (support >= MIN_SUPPORT) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("support", round(support));
                row.put("items", itemset.toString());
                result.add(row);
            }
        }

        result.sort((a, b) ->
                Double.compare((double) b.get("support"), (double) a.get("support"))
        );

        return result;
    }

    /* =====================================================
       2. ASSOCIATION RULES (APRlORI)
       ===================================================== */
    public List<RuleResult> analyze(List<List<String>> transactions) {

        int totalTx = transactions.size();

        Map<String, Integer> itemCount = new HashMap<>();
        Map<Set<String>, Integer> pairCount = new HashMap<>();

        // Hitung frekuensi item & pasangan
        for (List<String> tx : transactions) {
            Set<String> unique = new HashSet<>(tx);

            for (String item : unique) {
                itemCount.merge(item, 1, Integer::sum);
            }

            List<String> list = new ArrayList<>(unique);
            for (int i = 0; i < list.size(); i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    pairCount.merge(
                            Set.of(list.get(i), list.get(j)),
                            1,
                            Integer::sum
                    );
                }
            }
        }

        List<RuleResult> results = new ArrayList<>();

        for (Set<String> pair : pairCount.keySet()) {

            Iterator<String> it = pair.iterator();
            String A = it.next();
            String B = it.next();

            double supportAB = pairCount.get(pair) / (double) totalTx;
            double supportA = itemCount.get(A) / (double) totalTx;
            double supportB = itemCount.get(B) / (double) totalTx;

            double confidenceAB = supportAB / supportA;
            double confidenceBA = supportAB / supportB;

            // RULE A → B
            if (supportAB >= MIN_SUPPORT && confidenceAB >= MIN_CONFIDENCE) {
                results.add(buildRule(A, B, supportAB, supportA, supportB, confidenceAB));
            }

            // RULE B → A
            if (supportAB >= MIN_SUPPORT && confidenceBA >= MIN_CONFIDENCE) {
                results.add(buildRule(B, A, supportAB, supportB, supportA, confidenceBA));
            }
        }

        return results;
    }

    /* =====================================================
       3. HITUNG METRIK RULE
       ===================================================== */
    private RuleResult buildRule(
            String A, String B,
            double supportAB,
            double supportA,
            double supportB,
            double confidence) {

        RuleResult r = new RuleResult();
        r.antecedents = A;
        r.consequents = B;
        r.support = round(supportAB);
        r.confidence = round(confidence);
        r.lift = round(confidence / supportB);
        r.leverage = round(supportAB - (supportA * supportB));

        if (confidence < 1.0) {
            r.conviction = round((1 - supportB) / (1 - confidence));
        } else {
            r.conviction = Double.POSITIVE_INFINITY;
        }

        return r;
    }

    private double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}
