package com.apriori.bundling.Controller;

import com.apriori.bundling.model.Dataset;
import com.apriori.bundling.model.Transaction;
import com.apriori.bundling.repository.DatasetRepository;
import com.apriori.bundling.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

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
       FORM INPUT
       ======================= */
    @GetMapping
    public String form(Model model) {
        model.addAttribute("page", "input");
        return "layout";
    }

    /* =======================
       SIMPAN DATASET
       ======================= */
    @PostMapping
    public String submitDataset(
            @RequestParam String datasetName,
            @RequestParam(required = false) MultipartFile csv,
            HttpServletRequest request
    ) throws Exception {

        /* =========================
           CEK SUMBER DATA (WAJIB)
           ========================= */
        boolean hasCsv = (csv != null && !csv.isEmpty());
        boolean hasManual = request.getParameterMap()
                .keySet()
                .stream()
                .anyMatch(k -> k.startsWith("items["));

        // ❌ CSV + MANUAL TIDAK BOLEH BERSAMA
        if (hasCsv && hasManual) {
            return "redirect:/input?error=chooseone";
        }

        Dataset dataset = new Dataset();
        dataset.setName(datasetName);
        datasetRepo.save(dataset);

        boolean hasValidTransaction = false;

        /* =================================================
           MODE CSV
           ================================================= */
        if (hasCsv) {

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(csv.getInputStream())
            );

            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                String[] items = trimmed.split(",");
                if (items.length < 2) continue; // skip baris tidak valid

                // normalisasi spasi
                List<String> clean = new ArrayList<>();
                for (String s : items) {
                    String v = s.trim();
                    if (!v.isEmpty()) clean.add(v);
                }
                if (clean.size() < 2) continue;

                Transaction t = new Transaction();
                t.setDatasetId(dataset.getId());
                t.setItems(String.join(",", clean));
                transactionRepo.save(t);

                hasValidTransaction = true;
            }
        }

        /* =================================================
           MODE MANUAL
           ================================================= */
        if (!hasCsv && hasManual) {

            Map<String, String[]> paramMap = request.getParameterMap();
            Map<String, List<String>> transactionMap = new LinkedHashMap<>();

            for (String key : paramMap.keySet()) {
                if (key.startsWith("items[")) {
                    String index = key.substring(6, key.length() - 1);
                    transactionMap.putIfAbsent(index, new ArrayList<>());
                    transactionMap.get(index).addAll(
                            Arrays.asList(paramMap.get(key))
                    );
                }
            }

            for (List<String> items : transactionMap.values()) {
                if (items.size() >= 2) {
                    Transaction t = new Transaction();
                    t.setDatasetId(dataset.getId());
                    t.setItems(String.join(",", items));
                    transactionRepo.save(t);
                    hasValidTransaction = true;
                }
            }
        }

        /* =================================================
           JIKA TIDAK ADA TRANSAKSI VALID
           ================================================= */
        if (!hasValidTransaction) {
            datasetRepo.deleteById(dataset.getId());
            return "redirect:/input?error=notransaction";
        }

        /* =================================================
           SUKSES → DETAIL DATASET
           ================================================= */
        return "redirect:/dataset/" + dataset.getId();
    }
}
