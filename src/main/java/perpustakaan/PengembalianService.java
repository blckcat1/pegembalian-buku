
package perpustakaan;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PengembalianService {

    private static final int DENDA_PER_HARI = 1000;

    // Data peminjaman
    public static class Peminjaman {
        private final String id;
        private final LocalDate tanggalJatuhTempo;
        private boolean sudahDikembalikan;
        private LocalDate tanggalPengembalian;
        private int denda;

        public Peminjaman(String id, LocalDate tanggalJatuhTempo,
                boolean sudahDikembalikan) {
            this.id = id;
            this.tanggalJatuhTempo = tanggalJatuhTempo;
            this.sudahDikembalikan = sudahDikembalikan;
        }

        public String getId() {
            return id;
        }

        public boolean isSudahDikembalikan() {
            return sudahDikembalikan;
        }

        public LocalDate getTanggalPengembalian() {
            return tanggalPengembalian;
        }

        public int getDenda() {
            return denda;
        }
    }

    // Interface untuk pencarian dan penyimpanan data
    public interface Repository {
        Peminjaman cari(String id);

        void simpan(Peminjaman peminjaman);
    }

    // Hasil proses pengembalian
    public static class Hasil {
        public final long hariTerlambat;
        public final int denda;
        public final String pesan;

        public Hasil(long hariTerlambat, int denda, String pesan) {
            this.hariTerlambat = hariTerlambat;
            this.denda = denda;
            this.pesan = pesan;
        }
    }

    private final Repository repository;

    public PengembalianService(Repository repository) {
        this.repository = repository;
    }

    // Proses utama pengembalian buku
    public Hasil kembalikan(String id, LocalDate tanggalKembali) {

        // Node 1: Cari peminjaman
        Peminjaman p = repository.cari(id);

        // Keputusan 1: Peminjaman ditemukan?
        if (p == null) {
            throw new IllegalArgumentException(
                    "Data peminjaman tidak ditemukan");
        }

        // Node 3: Periksa status
        // Keputusan 2: Sudah dikembalikan?
        if (p.isSudahDikembalikan()) {
            throw new IllegalStateException(
                    "Buku sudah dikembalikan");
        }

        // Node 5: Hitung hari terlambat
        long hariTerlambat = Math.max(
                0,
                ChronoUnit.DAYS.between(
                        p.tanggalJatuhTempo,
                        tanggalKembali));

        int totalDenda;

        // Keputusan 3: Hari terlambat > 0?
        if (hariTerlambat > 0) {

            // Node 6: Hitung denda
            totalDenda = Math.toIntExact(
                    hariTerlambat * DENDA_PER_HARI);

        } else {

            // Node 7: Denda = 0
            totalDenda = 0;
        }

        // Node 8: Simpan tanggal, denda, dan status
        p.tanggalPengembalian = tanggalKembali;
        p.denda = totalDenda;
        p.sudahDikembalikan = true;

        repository.simpan(p);

        return new Hasil(
                hariTerlambat,
                totalDenda,
                "Pengembalian buku berhasil");
    }
}
