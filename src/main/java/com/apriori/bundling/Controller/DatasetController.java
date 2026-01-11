package com.apriori.bundling.Controller;

import com.apriori.bundling.model.Dataset;
import com.apriori.bundling.model.Transaction;
import com.apriori.bundling.repository.DatasetRepository;
import com.apriori.bundling.repository.TransactionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/dataset")
public class DatasetController {

    private final DatasetRepository datasetRepo;
    private final TransactionRepository transactionRepo;

    public DatasetController(DatasetRepository d, TransactionRepository t) {
        this.datasetRepo = d;
        this.transactionRepo = t;
    }

    /* ======================
       OPSI A — LIST DATASET
       ====================== */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("datasets", datasetRepo.findAll());
        model.addAttribute("page", "dataset"); // dataset.html
        return "layout";
    }

    /* ======================
       OPSI B — DETAIL DATASET
       ====================== */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Dataset dataset = datasetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Dataset tidak ditemukan"));

        List<Transaction> transactions =
                transactionRepo.findByDatasetId(id);

        model.addAttribute("dataset", dataset);
        model.addAttribute("transactions", transactions);
        model.addAttribute("page", "dataset-detail"); // dataset-detail.html

        return "layout";
    }

    /* ======================
       OPSI B — TAMBAH TRANSAKSI
       ====================== */
    @PostMapping("/{id}/add")
    public String addTransaction(
            @PathVariable Long id,
            @RequestParam String items) {

        if (items == null || items.trim().isEmpty()) {
            return "redirect:/dataset/" + id;
        }

        // normalisasi item
        String[] split = items.split(",");
        if (split.length < 2) {
            return "redirect:/dataset/" + id;
        }

        String normalized = String.join(",",
                java.util.Arrays.stream(split)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
        );

        if (normalized.split(",").length < 2) {
            return "redirect:/dataset/" + id;
        }

        Transaction t = new Transaction();
        t.setDatasetId(id);
        t.setItems(normalized);
        transactionRepo.save(t);

        return "redirect:/dataset/" + id;
    }

    /* ======================
       OPSI B — HAPUS TRANSAKSI
       ====================== */
    @PostMapping("/tx/{txId}/delete")
    public String deleteTransaction(@PathVariable Long txId) {

        Transaction t = transactionRepo.findById(txId).orElse(null);
        if (t != null) {
            Long datasetId = t.getDatasetId();
            transactionRepo.delete(t);
            return "redirect:/dataset/" + datasetId;
        }

        return "redirect:/dataset";
    }

    /* ======================
       OPSI A — HAPUS DATASET
       ====================== */
    @PostMapping("/{id}/delete")
    public String deleteDataset(@PathVariable Long id) {

        transactionRepo.deleteByDatasetId(id);
        datasetRepo.deleteById(id);

        return "redirect:/dataset";
    }
}
