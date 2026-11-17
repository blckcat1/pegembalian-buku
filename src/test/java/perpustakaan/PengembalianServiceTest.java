package perpustakaan;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

public class PengembalianServiceTest {

    private Map<String, PengembalianService.Peminjaman> data;
    private PengembalianService service;

    private final LocalDate tanggalKembali = LocalDate.of(2026, 11, 17);

    @BeforeEach
    void setUp() {

        data = new HashMap<>();

        PengembalianService.Repository repository = new PengembalianService.Repository() {

            @Override
            public PengembalianService.Peminjaman cari(
                    String id) {
                return data.get(id);
            }

            @Override
            public void simpan(
                    PengembalianService.Peminjaman p) {
                data.put(p.getId(), p);
            }
        };

        service = new PengembalianService(repository);

        data.put("P001",
                new PengembalianService.Peminjaman(
                        "P001",
                        LocalDate.of(2026, 11, 17),
                        false));

        data.put("P002",
                new PengembalianService.Peminjaman(
                        "P002",
                        LocalDate.of(2026, 11, 14),
                        true));

        data.put("P003",
                new PengembalianService.Peminjaman(
                        "P003",
                        LocalDate.of(2026, 11, 14),
                        false));

        data.put("P004",
                new PengembalianService.Peminjaman(
                        "P004",
                        LocalDate.of(2026, 11, 17),
                        false));
    }

    @Test
    @DisplayName("TC-01: Peminjaman tidak ditemukan")
    void testPeminjamanTidakDitemukan() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.kembalikan(
                        "P999", tanggalKembali));

        assertEquals(
                "Data peminjaman tidak ditemukan",
                exception.getMessage());
    }

    @Test
    @DisplayName("TC-02: Buku sudah dikembalikan")
    void testBukuSudahDikembalikan() {

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.kembalikan(
                        "P002", tanggalKembali));

        assertEquals(
                "Buku sudah dikembalikan",
                exception.getMessage());
    }

    // P3: Buku terlambat dikembalikan
    @Test
    @DisplayName("TC-03: Pengembalian dengan denda")
    void testPengembalianTerlambat() {

        PengembalianService.Hasil hasil = service.kembalikan(
                "P003", tanggalKembali);

        assertEquals(3, hasil.hariTerlambat);
        assertEquals(3000, hasil.denda);

        assertTrue(
                data.get("P003").isSudahDikembalikan());

        assertEquals(
                tanggalKembali,
                data.get("P003").getTanggalPengembalian());

        assertEquals(
                3000,
                data.get("P003").getDenda());
    }

    // P4: Buku tidak terlambat dikembalikan
    @Test
    @DisplayName("TC-04: Pengembalian tanpa denda")
    void testPengembalianTepatWaktu() {

        PengembalianService.Hasil hasil = service.kembalikan(
                "P004", tanggalKembali);

        assertEquals(0, hasil.hariTerlambat);
        assertEquals(0, hasil.denda);

        assertTrue(
                data.get("P004").isSudahDikembalikan());

        assertEquals(
                tanggalKembali,
                data.get("P004").getTanggalPengembalian());

        assertEquals(
                0,
                data.get("P004").getDenda());
    }
}

