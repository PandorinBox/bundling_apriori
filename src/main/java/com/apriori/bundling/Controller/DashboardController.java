package com.apriori.bundling.Controller;

import com.apriori.bundling.model.Dataset;
import com.apriori.bundling.model.RuleResult;
import com.apriori.bundling.model.Transaction;
import com.apriori.bundling.repository.DatasetRepository;
import com.apriori.bundling.repository.TransactionRepository;
import com.apriori.bundling.service.AprioriService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class DashboardController {

    private final DatasetRepository datasetRepo;
    private final TransactionRepository transactionRepo;
    private final AprioriService aprioriService;

    public DashboardController(
            DatasetRepository d,
            TransactionRepository t,
            AprioriService a) {
        this.datasetRepo = d;
        this.transactionRepo = t;
        this.aprioriService = a;
    }

    /* ======================================
       ROOT → REDIRECT KE DASHBOARD
       ====================================== */
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/dashboard";
    }

    /* ======================================
       DASHBOARD UTAMA
       ====================================== */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        Dataset lastDataset = datasetRepo.findTopByOrderByIdDesc();

        /* === KASUS: BELUM ADA DATASET === */
        if (lastDataset == null) {
            model.addAttribute("datasetName", "-");
            model.addAttribute("totalTransactions", 0);
            model.addAttribute("totalProducts", 0);
            model.addAttribute("bestBundling", Collections.emptyList());
            model.addAttribute("page", "dashboard");
            return "layout";
        }

        List<Transaction> txs =
                transactionRepo.findByDatasetId(lastDataset.getId());

        /* === NORMALISASI TRANSAKSI === */
        List<List<String>> transactions = new ArrayList<>();
        Set<String> allItems = new HashSet<>();

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

        /* === JALANKAN APRIORI JIKA ADA TRANSAKSI === */
        List<RuleResult> rules = new ArrayList<>();
        if (!transactions.isEmpty()) {
            rules = aprioriService.analyze(transactions);
        }

        /* === AMBIL MAKSIMAL 3 BUNDLING TERBAIK === */
        List<RuleResult> bestBundling = new ArrayList<>(rules);
        bestBundling.sort((a, b) ->
                Double.compare(b.confidence, a.confidence)
        );

        if (bestBundling.size() > 3) {
            bestBundling = bestBundling.subList(0, 3);
        }

        /* === KIRIM KE VIEW === */
        model.addAttribute("datasetName", lastDataset.getName());
        model.addAttribute("totalTransactions", transactions.size());
        model.addAttribute("totalProducts", allItems.size());
        model.addAttribute("bestBundling", bestBundling);
        model.addAttribute("page", "dashboard");

        return "layout";
    }
}
