Ace Rental PlayStation
Selamat datang di repositori resmi Ace Rental PlayStation! Aplikasi mobile ini adalah sistem informasi yang dirancang untuk mempermudah pengelolaan usaha rental PlayStation secara efisien, akurat, dan modern. Aplikasi ini dibangun menggunakan Kotlin dan Supabase untuk mendukung kebutuhan admin (pemilik usaha) dan kasir dalam mengelola transaksi, stok, dan laporan keuangan.

Tentang Aplikasi
Ace Rental PlayStation adalah platform digital yang dirancang khusus untuk mengelola usaha rental PlayStation. Aplikasi ini mempermudah pencatatan transaksi penyewaan, pengelolaan stok makanan dan minuman, serta pembuatan laporan keuangan secara otomatis dan akurat. Dengan antarmuka yang intuitif, aplikasi ini mendukung kebutuhan admin dan kasir dalam menjalankan operasional harian.
Tech Stack
Aplikasi ini dibangun dengan teknologi berikut:

Bahasa Pemrograman: Kotlin
UI & Layout: XML dan Material Design Components
Database: Supabase
Navigasi: Navigation Drawer (DrawerLayout)

Fitur
Fitur Admin

Sign In & Log In: Autentikasi untuk mengakses dashboard admin.
Dashboard Admin: Tampilan utama untuk melihat ringkasan operasional.
Riwayat Transaksi: Melihat daftar transaksi penyewaan yang telah dilakukan.
Persediaan Admin: Mengelola stok makanan dan minuman.
Pencatatan Pengeluaran: Mencatat pengeluaran operasional.
Pop-up Tambah Pengeluaran: Formulir cepat untuk menambah data pengeluaran.

Fitur Kasir

Sign In & Log In: Autentikasi untuk mengakses dashboard kasir.
Dashboard Kasir: Tampilan utama untuk operasional kasir.
Sewa PlayStation: Mengelola proses penyewaan PlayStation.
Detail Sewa PS: Melihat detail transaksi penyewaan.
Pesan Snack: Mengelola pesanan makanan dan minuman.
Riwayat Transaksi: Melihat daftar transaksi yang telah dilakukan.

Prasyarat
Sebelum menjalankan proyek ini, pastikan Anda telah menginstal:

Android Studio (versi terbaru direkomendasikan, misalnya Arctic Fox atau lebih baru)
JDK 11 atau lebih baru
Kotlin Plugin di Android Studio
Akun Supabase dan konfigurasi kunci API
Emulator Android atau perangkat fisik untuk pengujian
Git untuk kloning repositori

Instalasi
Ikuti langkah-langkah berikut untuk menyiapkan proyek di lingkungan lokal Anda:

Kloning Repositori
git clone https://github.com/MuhammadBintang27/ace-rental-playstation-kotlin.git
cd ace-rental-playstation-kotlin


Konfigurasi Supabase

Buat proyek di Supabase dan dapatkan kunci API.

Tambahkan kunci API ke file konfigurasi (misalnya, local.properties atau file khusus di proyek).

Contoh struktur file konfigurasi:
SUPABASE_ANON_KEY=your_supabase_url
SUPABASE_KEY=your_supabase_key




Instal Dependensi

Buka proyek di Android Studio.
Sinkronkan proyek dengan Gradle: Klik Sync Project with Gradle Files di toolbar Android Studio.



Cara Menjalankan Aplikasi
Untuk menjalankan aplikasi di Android Studio, ikuti langkah-langkah berikut:

Buka Proyek:

Buka Android Studio dan pilih Open an existing project.
Arahkan ke folder hasil kloning (ace-rental-playstation-kotlin).


Konfigurasi Emulator atau Perangkat:

Pastikan emulator Android sudah diatur (misalnya, Pixel 4 API 30) atau sambungkan perangkat fisik melalui USB dengan mode debugging diaktifkan.
Pilih perangkat dari dropdown di toolbar Android Studio.


Jalankan Aplikasi:

Klik tombol Run (ikon segitiga hijau) di toolbar Android Studio.

Alternatifnya, jalankan perintah berikut di terminal:
./gradlew installDebug


Aplikasi akan di-build dan dijalankan di emulator/perangkat yang dipilih.



Troubleshooting:

Pastikan semua dependensi Gradle tersinkronisasi.
Periksa koneksi internet untuk mengakses Supabase.
Jika ada error terkait API, verifikasi kunci Supabase di file konfigurasi.



Kontribusi
Kami menyambut kontribusi dari komunitas! Untuk berkontribusi:

Fork repositori ini.
Buat branch baru: git checkout -b fitur-baru.
Lakukan perubahan dan commit: git commit -m 'Menambahkan fitur baru'.
Push ke branch: git push origin fitur-baru.
Buat Pull Request di GitHub.

Silakan baca Panduan Kontribusi untuk detail lebih lanjut (jika tersedia).
Lisensi
Proyek ini dilisensikan di bawah MIT License. Lihat file LICENSE untuk informasi lebih lanjut.
Tim Pengembang
Aplikasi ini dikembangkan oleh:

Muhammad Bintang Indra Hidayat (NPM: 2208107010023)
Ahmad Syah Ramadhan (NPM: 2208107010033)

Terima kasih telah menggunakan Ace Rental PlayStation!
