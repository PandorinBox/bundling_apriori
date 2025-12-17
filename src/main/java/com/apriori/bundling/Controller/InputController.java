package com.apriori.bundling.Controller;

import com.apriori.bundling.model.Dataset;
import com.apriori.bundling.model.Transaction;
import com.apriori.bundling.repository.DatasetRepository;
import com.apriori.bundling.repository.TransactionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@Controller
@RequestMapping("/input")
public class InputController {

    private final DatasetRepository datasetRepo;
    private final TransactionRepository transactionRepo;

    public InputController(DatasetRepository datasetRepo,
                           TransactionRepository transactionRepo) {
        this.datasetRepo = datasetRepo;
        this.transactionRepo = transactionRepo;
    }

    /* =======================
       TAMPILKAN FORM INPUT
       ======================= */
    @GetMapping
    public String form(Model model) {
        model.addAttribute("page", "input");
        return "layout";
    }

    /* =======================
       SIMPAN DATASET + TRANSAKSI
       ======================= */
    @PostMapping
    public String submitDataset(
            @RequestParam String datasetName,
            @RequestParam(required = false) Map<String, String[]> items,
            @RequestParam(required = false) MultipartFile csv
    ) throws Exception {

        /* === SIMPAN DATASET === */
        Dataset dataset = new Dataset();
        dataset.setName(datasetName);
        datasetRepo.save(dataset);

        /* === MODE CSV (20–100 TRANSAKSI) === */
        if (csv != null && !csv.isEmpty()) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(csv.getInputStream())
            );
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Transaction t = new Transaction();
                    t.setDatasetId(dataset.getId());
                    t.setItems(line.trim());
                    transactionRepo.save(t);
                }
            }
        }

        /* === MODE MANUAL MULTI TRANSAKSI === */
        if (items != null) {
            for (String key : items.keySet()) {
                String[] selectedItems = items.get(key);
                if (selectedItems != null && selectedItems.length >= 2) {
                    Transaction t = new Transaction();
                    t.setDatasetId(dataset.getId());
                    t.setItems(String.join(",", selectedItems));
                    transactionRepo.save(t);
                }
            }
        }

        return "redirect:/";
    }
}
