Berikut adalah versi dalam format Markdown (.md) yang dirapikan dan cocok untuk dokumentasi proyek:


# 📦 Supabase Backend Information

## 🌐 Project Details

- **Project URL**: [https://your-project.supabase.co](https://your-project.supabase.co)
- **Public API Key (anon)**: `YOUR_PUBLIC_ANON_KEY`

> ⚠️ **Catatan Keamanan**:  
> **JANGAN** commit service role key Anda ke GitHub. Hanya gunakan `anon` key publik dalam repository.

---

## 🗄️ Database Schema

Salin dan tempel skrip SQL di bawah ini ke **SQL Editor** Supabase, lalu klik **Run** untuk membuat semua *enum* dan tabel:

```sql
-- ENUMS
CREATE TYPE kategori1_laporan_keuangan AS ENUM ('Pemasukan', 'Pengeluaran');
CREATE TYPE kategori_pengeluaran AS ENUM ('Makanan', 'Minuman', 'Lainnya');
CREATE TYPE kategori_main_ps AS ENUM ('Personal', 'Tetap');
CREATE TYPE user_role AS ENUM ('admin', 'cashier');

-- TABEL USERS
CREATE TABLE users (
  id UUID PRIMARY KEY,
  email TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  role user_role
);

-- TABEL SHIFT
CREATE TABLE shift (
  shift_id SERIAL PRIMARY KEY,
  nama_shift VARCHAR,
  waktu_mulai TIME,
  waktu_selesai TIME,
  status VARCHAR,
  user_id UUID REFERENCES users(id)
);

-- TABEL PLAYSTATION_UNIT
CREATE TABLE playstation_unit (
  unit_id SERIAL PRIMARY KEY,
  nomor_unit VARCHAR,
  status VARCHAR,
  tipe_main kategori_main_ps,
  waktu_mulai TIMESTAMPTZ,
  waktu_selesai TIMESTAMPTZ
);

-- TABEL TRANSAKSI_RENTAL
CREATE TABLE transaksi_rental (
  transaksi_id SERIAL PRIMARY KEY,
  unit_id INT REFERENCES playstation_unit(unit_id),
  shift_id INT REFERENCES shift(shift_id),
  kategori_main kategori_main_ps,
  waktu_mulai TIMESTAMPTZ,
  waktu_selesai TIMESTAMPTZ,
  durasi INT,
  harga NUMERIC
);

-- TABEL PRODUK
CREATE TABLE produk (
  produk_id SERIAL PRIMARY KEY,
  nama_produk TEXT,
  harga NUMERIC,
  kategori TEXT,
  stok_persediaan INT
);

-- TABEL TRANSAKSI_PENJUALAN
CREATE TABLE transaksi_penjualan (
  penjualan_id SERIAL PRIMARY KEY,
  produk_id INT REFERENCES produk(produk_id),
  jumlah INT,
  total_harga NUMERIC,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- TABEL PENGELUARAN
CREATE TABLE pengeluaran (
  pengeluaran_id SERIAL PRIMARY KEY,
  kategori kategori_pengeluaran,
  tanggal DATE,
  jumlah_pengeluaran NUMERIC,
  nama_pengeluaran TEXT,
  bukti TEXT,
  stok_tambahan INT
);

-- TABEL LAPORAN_KEUANGAN
CREATE TABLE laporan_keuangan (
  id SERIAL PRIMARY KEY,
  tanggal DATE,
  jenis kategori1_laporan_keuangan,
  referensi_rental_id INT REFERENCES transaksi_rental(transaksi_id),
  referensi_penjualan_id INT REFERENCES transaksi_penjualan(penjualan_id),
  referensi_pengeluaran_id INT REFERENCES pengeluaran(pengeluaran_id),
  keterangan TEXT,
  jumlah NUMERIC,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  saldo NUMERIC
);
````

---

## ▶️ Cara Menjalankan

1. Buka **Dashboard Supabase** → masuk ke tab **SQL Editor**.
2. Paste skrip SQL di atas, lalu klik **Run**.
3. Masuk ke tab **Table Editor** untuk memverifikasi bahwa semua tabel telah dibuat.
4. Tambahkan aturan **RLS (Row-Level Security)** sesuai kebutuhan aplikasi Anda.

---

## 📱 Integrasi di Aplikasi Android (Ringkasan)

| Langkah                | Keterangan                                                                   |
| ---------------------- | ---------------------------------------------------------------------------- |
| 1. Tambahkan library   | `implementation "io.supabase.kotlin:postgrest-kt:1.1.0"`                     |
| 2. Inisialisasi client | `SupabaseClient("https://your-project.supabase.co", "YOUR_PUBLIC_ANON_KEY")` |
| 3. Autentikasi         | Gunakan `supabase.auth.signInWithEmail(...)`                                 |
| 4. Query tabel         | Contoh: `client.from("playstation_unit").select()`                           |

Jika Anda membutuhkan **contoh kode Android yang lebih lengkap**, silakan hubungi kami!



Jika kamu ingin saya bantu menyimpannya sebagai file `.md`, kirimkan perintahnya dan saya akan siapkan untuk diunduh.

