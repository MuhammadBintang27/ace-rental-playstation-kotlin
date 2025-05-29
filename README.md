# Ace Rental PlayStation

Selamat datang di repositori resmi **Ace Rental PlayStation**!  
Aplikasi mobile ini merupakan sistem informasi yang dirancang untuk mempermudah pengelolaan usaha rental PlayStation secara efisien, akurat, dan modern. Aplikasi ini dibangun menggunakan **Kotlin** dan **Supabase** untuk mendukung kebutuhan **admin** (pemilik usaha) dan **kasir** dalam mengelola transaksi, stok, dan laporan keuangan.

---

## 🕹️ Tentang Aplikasi

**Ace Rental PlayStation** adalah platform digital yang dirancang khusus untuk:

- Mencatat transaksi penyewaan PS
- Mengelola stok makanan & minuman
- Membuat laporan keuangan secara otomatis dan akurat

Dengan antarmuka yang intuitif, aplikasi ini memudahkan operasional harian untuk **admin** dan **kasir**.

---

## 🛠️ Tech Stack

- **Bahasa Pemrograman**: Kotlin
- **UI & Layout**: XML dan Material Design Components
- **Database**: Supabase
- **Navigasi**: Navigation Drawer (`DrawerLayout`)

---

## ✨ Fitur

### 👑 Fitur Admin

- **Sign In & Log In**: Autentikasi untuk dashboard admin
- **Dashboard Admin**: Ringkasan operasional
- **Riwayat Transaksi**: Melihat daftar transaksi sewa
- **Persediaan Admin**: Pengelolaan stok makanan & minuman
- **Pencatatan Pengeluaran**: Mencatat biaya operasional
- **Pop-up Tambah Pengeluaran**: Form cepat penambahan pengeluaran

### 💼 Fitur Kasir

- **Sign In & Log In**: Autentikasi untuk dashboard kasir
- **Dashboard Kasir**: Operasional kasir sehari-hari
- **Sewa PlayStation**: Proses penyewaan PS
- **Detail Sewa PS**: Informasi detail transaksi sewa
- **Pesan Snack**: Pemesanan makanan & minuman
- **Riwayat Transaksi**: Melihat transaksi yang dilakukan

---

## ⚙️ Prasyarat

Sebelum menjalankan proyek, pastikan Anda telah menginstal:

- Android Studio (disarankan versi terbaru, misalnya Arctic Fox+)
- JDK 11 atau lebih baru
- Kotlin Plugin untuk Android Studio
- Akun Supabase dan konfigurasi kunci API
- Emulator Android atau perangkat fisik
- Git

---

## 🚀 Instalasi

### 1. Kloning Repositori

```bash
git clone https://github.com/MuhammadBintang27/ace-rental-playstation-kotlin.git
cd ace-rental-playstation-kotlin
```

### 2. Konfigurasi Supabase

- Buat proyek di [Supabase](https://supabase.io/)
- Dapatkan URL dan API key
- Tambahkan ke file konfigurasi (misalnya `local.properties`)

```properties
SUPABASE_URL=your_supabase_url
SUPABASE_KEY=your_supabase_key
```

### 3. Instal Dependensi

- Buka proyek dengan Android Studio
- Klik `Sync Project with Gradle Files`

---

## ▶️ Menjalankan Aplikasi

### Buka Proyek

1. Buka Android Studio
2. Pilih `Open an existing project`
3. Arahkan ke folder `ace-rental-playstation-kotlin`

### Konfigurasi Emulator atau Perangkat

- Gunakan emulator Android (contoh: Pixel 4 API 30)  
  atau
- Sambungkan perangkat fisik via USB dengan mode debugging aktif

### Jalankan Aplikasi

- Klik tombol **Run** (ikon segitiga hijau)  
  atau gunakan terminal:

```bash
./gradlew installDebug
```

---

## 🛠 Troubleshooting

- Pastikan Gradle tersinkronisasi
- Periksa koneksi internet (khususnya untuk Supabase)
- Cek kembali API key jika terjadi error terkait Supabase

---

## 🤝 Kontribusi

Kami menyambut kontribusi dari komunitas!

1. Fork repositori ini
2. Buat branch baru:

```bash
git checkout -b fitur-baru
```

3. Lakukan perubahan & commit:

```bash
git commit -m 'Menambahkan fitur baru'
```

4. Push ke branch Anda:

```bash
git push origin fitur-baru
```

5. Buat Pull Request ke repositori ini

> Panduan kontribusi lebih lanjut akan ditambahkan segera.

---

## 📄 Lisensi

Proyek ini menggunakan lisensi **MIT**. Lihat file `LICENSE` untuk informasi lengkap.

---

## 👨‍💻 Tim Pengembang

- **Muhammad Bintang Indra Hidayat** (NPM: 2208107010023)  
- **Ahmad Syah Ramadhan** (NPM: 2208107010033)

---

Terima kasih telah menggunakan **Ace Rental PlayStation**! 🎮
