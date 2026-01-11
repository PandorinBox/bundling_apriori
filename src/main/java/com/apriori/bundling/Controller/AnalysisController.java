package com.apriori.bundling.Controller;

import com.apriori.bundling.model.RuleResult;
import com.apriori.bundling.model.Transaction;
import com.apriori.bundling.repository.DatasetRepository;
import com.apriori.bundling.repository.TransactionRepository;
import com.apriori.bundling.service.AprioriService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/analysis")
public class AnalysisController {

    private final DatasetRepository datasetRepo;
    private final TransactionRepository transactionRepo;
    private final AprioriService aprioriService;

    public AnalysisController(
            DatasetRepository d,
            TransactionRepository t,
            AprioriService a) {
        this.datasetRepo = d;
        this.transactionRepo = t;
        this.aprioriService = a;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("datasets", datasetRepo.findAll());
        model.addAttribute("page", "analysis");
        return "layout";
    }

    @PostMapping
    public String analyze(@RequestParam Long datasetId, Model model) {

        List<Transaction> txs =
                transactionRepo.findByDatasetId(datasetId);

        /* ======================
           NORMALISASI TRANSAKSI
           ====================== */
        List<List<String>> transactions = new ArrayList<>();
        Set<String> allItems = new TreeSet<>();

        for (Transaction t : txs) {
            List<String> items = new ArrayList<>();
            for (String s : t.getItems().split(",")) {
                String item = s.trim();
                if (!item.isEmpty()) {
                    items.add(item);
                    allItems.add(item);
                }
            }
            if (!items.isEmpty()) {
                transactions.add(items);
            }
        }

        /* ======================
           FREKUENSI ITEM (BARU)
           ====================== */
        Map<String, Integer> itemFrequency = new TreeMap<>();

        for (List<String> tx : transactions) {
            Set<String> unique = new HashSet<>(tx);
            for (String item : unique) {
                itemFrequency.merge(item, 1, Integer::sum);
            }
        }

        Map<String, Double> itemSupport = new LinkedHashMap<>();
        int totalTx = transactions.size();

        for (String item : itemFrequency.keySet()) {
            double support = itemFrequency.get(item) / (double) totalTx;
            itemSupport.put(
                    item,
                    Math.round(support * 1000.0) / 1000.0
            );
        }

        /* ======================
           MATRIX BINER
           ====================== */
        List<Map<String, Integer>> matrix = new ArrayList<>();
        for (List<String> tx : transactions) {
            Map<String, Integer> row = new LinkedHashMap<>();
            for (String item : allItems) {
                row.put(item, tx.contains(item) ? 1 : 0);
            }
            matrix.add(row);
        }

        /* ======================
           FREQUENT ITEMSETS
           ====================== */
        List<Map<String, Object>> frequentItemsets =
                aprioriService.frequentItemsets(transactions);

        /* ======================
           ASSOCIATION RULES
           ====================== */
        List<RuleResult> rules =
                aprioriService.analyze(transactions);

        /* ======================
           BUNDLING TERBAIK
           ====================== */
        List<RuleResult> bestBundling = new ArrayList<>(rules);

        bestBundling.sort((a, b) -> {
            int c = Double.compare(b.confidence, a.confidence);
            if (c == 0) {
                return Double.compare(b.lift, a.lift);
            }
            return c;
        });

        if (bestBundling.size() > 3) {
            bestBundling = bestBundling.subList(0, 3);
        }

        /* ======================
           KIRIM KE VIEW
           ====================== */
        model.addAttribute("datasets", datasetRepo.findAll());
        model.addAttribute("items", allItems);
        model.addAttribute("matrix", matrix);

        // BARU
        model.addAttribute("itemFrequency", itemFrequency);
        model.addAttribute("itemSupport", itemSupport);

        model.addAttribute("frequentItemsets", frequentItemsets);
        model.addAttribute("rules", rules);
        model.addAttribute("bestBundling", bestBundling);
        model.addAttribute("page", "analysis");

        return "layout";
    }
}
