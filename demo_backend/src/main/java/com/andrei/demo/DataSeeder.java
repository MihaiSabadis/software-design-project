package com.andrei.demo;

import com.andrei.demo.model.GamePatch;
import com.andrei.demo.model.PriceHistory;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.repository.GamePatchRepository;
import com.andrei.demo.repository.PriceHistoryRepository;
import com.andrei.demo.repository.VideoGameRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final VideoGameRepository videoGameRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final GamePatchRepository gamePatchRepository;

    public DataSeeder(VideoGameRepository videoGameRepository,
                      PriceHistoryRepository priceHistoryRepository,
                      GamePatchRepository gamePatchRepository) {
        this.videoGameRepository = videoGameRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.gamePatchRepository = gamePatchRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verificăm dacă avem deja istoric de prețuri ca să nu inserăm dubluri la fiecare restart
        if (priceHistoryRepository.count() == 0) {

            // 1. Luăm primul joc din baza ta de date (ex: Minecraft, FIFA, ce ai tu deja acolo)
            List<VideoGame> games = videoGameRepository.findAll();
            if (games.isEmpty()) {
                System.out.println("Nu ai niciun joc în DB! Creează un joc mai întâi din Angular.");
                return;
            }

            VideoGame targetGame = games.getFirst(); // Folosim primul joc găsit
            System.out.println("Generăm date de Analytics pentru jocul: " + targetGame.getTitle());

            // 2. Generăm istoricul de PREȚURI (Evoluția din Ianuarie până în Mai)
            createPrice(targetGame, LocalDate.of(2024, 1, 10), 69.99); // Lansare plină de bug-uri
            createPrice(targetGame, LocalDate.of(2024, 2, 1), 69.99);  // Prețul stă sus
            createPrice(targetGame, LocalDate.of(2024, 2, 28), 49.99); // Lumea se plânge, prețul scade
            createPrice(targetGame, LocalDate.of(2024, 4, 15), 39.99); // Scade și mai mult
            createPrice(targetGame, LocalDate.of(2024, 5, 20), 29.99); // Reducere mare de vară

            // 3. Generăm PATCH-URILE (Momentele cheie)
            createPatch(targetGame, LocalDate.of(2024, 2, 15), "v1.1", "Day-One Bug Fixes & Stability");
            createPatch(targetGame, LocalDate.of(2024, 4, 10), "v2.0", "Massive Overhaul & Performance Boost");

            System.out.println("Datele pentru grafic au fost generate cu succes!");
        }
    }

    // Funcții utilitare pentru a face codul de mai sus curat
    private void createPrice(VideoGame game, LocalDate date, Double priceValue) {
        PriceHistory price = new PriceHistory();
        price.setVideoGame(game);
        price.setRecordedAt(date);
        price.setPrice(priceValue);
        priceHistoryRepository.save(price);
    }

    private void createPatch(VideoGame game, LocalDate date, String version, String description) {
        GamePatch patch = new GamePatch();
        patch.setVideoGame(game);
        patch.setReleaseDate(date);
        patch.setVersion(version);
        patch.setDescription(description);
        gamePatchRepository.save(patch);
    }
}